package com.glaze.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class DiscoverClientTest {
    @Test fun artistDiscographyResolvesNamesakesUsingOwnedReleases() = runBlocking {
        val catalogue = DiscoverClient(HttpClient(MockEngine { request ->
            val body = when (request.url.encodedPath) {
                "/search/artist" -> """{"data":[{"id":1,"name":"Artist"},{"id":2,"name":"Artist"}]}"""
                "/artist/1/albums" -> """{"data":[{"id":10,"title":"Owned"},{"id":11,"title":"Missing","release_date":"2020-01-01"},{"id":12,"title":"Future","release_date":"2999-01-01"}]}"""
                "/artist/2/albums" -> """{"data":[{"id":20,"title":"Unrelated","release_date":"2020-01-01"}]}"""
                else -> error("Unexpected path")
            }
            respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }))
        try {
            val owned = listOf(Album(id="a", name="Owned", artist="Artist"))
            assertEquals(listOf("Missing"), catalogue.artistAlbums("Artist", owned).map { it.title })
            assertEquals(listOf("Unrelated"), catalogue.artistAlbums("Artist", owned, "2").map { it.title })
        } finally { catalogue.close() }
    }

    @Test fun combinedLibraryCreditsExcludeOwnedAlbumsAndSongsWithoutMatchingUnrelatedArtists() = runBlocking {
        val catalogue = DiscoverClient(HttpClient(MockEngine { request ->
            val body = when (request.url.encodedPath) {
                "/search/album" -> """{"data":[{"id":1,"title":"Her Loss","artist":{"name":"Drake"}},{"id":2,"title":"Her Loss","artist":{"name":"21 Savage"}},{"id":3,"title":"Her Loss","artist":{"name":"Drake Bell"}},{"id":4,"title":"Her Loss","artist":{"name":"Someone Else"}}]}"""
                "/search/track" -> """{"data":[{"id":50,"title":"Her Loss Single","artist":{"name":"Drake"},"album":{"id":5,"title":"Her Loss Single"}}]}"""
                "/search/artist" -> """{"data":[]}"""
                "/album/3", "/album/4", "/album/5" -> {
                    val id = request.url.encodedPath.substringAfterLast('/').toInt()
                    val artist = when (id) { 3 -> "Drake Bell"; 4 -> "Someone Else"; else -> "Drake" }
                    val title = if (id == 5) "Her Loss Single" else "Her Loss"
                    """{"id":$id,"title":"$title","artist":{"name":"$artist"},"release_date":"2022-11-04","nb_tracks":0,"tracks":{"data":[]}}"""
                }
                else -> error("Owned album must not be fetched: ${request.url.encodedPath}")
            }
            respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }))
        val library = SubsonicClient(ServerCredentials("https://library.test", "owner", "secret"),
            HttpClient(MockEngine { request ->
                val body = when (request.url.encodedPath.substringAfterLast('/')) {
                    "getAlbumList2.view" -> """{"subsonic-response":{"status":"ok","albumList2":{"album":[{"id":"owned","name":"Her Loss","artist":"Drake, 21 Savage"}]}}}"""
                    "search3.view" -> """{"subsonic-response":{"status":"ok","searchResult3":{"song":[{"id":"owned-song","title":"Her Loss Single","artist":"Drake, 21 Savage","album":"Another Release"}]}}}"""
                    else -> error("Unexpected library request")
                }
                respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }))
        try {
            val results = catalogue.search("Her Loss", library)
            assertEquals(listOf("Drake Bell", "Someone Else"), results.albums.map { it.artist })
            assertTrue(results.tracks.isEmpty())
        } finally { catalogue.close(); library.close() }
    }

    @Test fun catalogueExcludesOwnedAndFutureMusicAndLoadsCompleteTracklists() = runBlocking {
        val credentials = ServerCredentials("https://library.test", "owner", "secret")
        val catalogue = DiscoverClient(HttpClient(MockEngine { request ->
            assertNull(request.headers["X-Jam-Navidrome-Token"])
            assertNull(request.url.parameters["t"])
            val body = when (request.url.encodedPath) {
                "/search/album" -> {
                    assertEquals("Artist", request.url.parameters["q"])
                    """{"data":[{"id":1,"title":"Already Here","artist":{"name":"Artist"}},{"id":2,"title":"New Album","artist":{"name":"Artist"}},{"id":3,"title":"Future","artist":{"name":"Artist"}}]}"""
                }
                "/search/track" -> """{"data":[{"id":20,"title":"Track Two","artist":{"name":"Artist"},"album":{"id":2,"title":"New Album"}},{"id":30,"title":"Future Single","artist":{"name":"Artist"},"album":{"id":3,"title":"Future"}}]}"""
                "/album/2" -> """{"id":2,"title":"New Album","artist":{"name":"Artist"},"release_date":"2020-01-01","nb_tracks":2,"tracks":{"data":[{"id":21,"title":"Track One","artist":{"name":"Artist"}}]}}"""
                "/album/2/tracks" -> {
                    assertEquals("1", request.url.parameters["index"])
                    """{"data":[{"id":20,"title":"Track Two","artist":{"name":"Artist"},"duration":181,"explicit_lyrics":true}]}"""
                }
                "/album/3" -> """{"id":3,"title":"Future","artist":{"name":"Artist"},"release_date":"2999-01-01","nb_tracks":0,"tracks":{"data":[]}}"""
                else -> error("Unexpected catalogue request: ${request.url.encodedPath}")
            }
            respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }))
        val library = SubsonicClient(credentials, HttpClient(MockEngine { request ->
            val body = when (request.url.encodedPath.substringAfterLast('/')) {
                "getAlbumList2.view" -> """{"subsonic-response":{"status":"ok","albumList2":{"album":[{"id":"owned","name":"Already here! (Deluxe Edition)","artist":"Artist & Guest","artists":[{"id":"artist","name":"ARTIST"}]}]}}}"""
                "search3.view" -> {
                    assertEquals("Track Two", request.url.parameters["query"])
                    """{"subsonic-response":{"status":"ok","searchResult3":{"song":[{"id":"local","title":"Track Two","artist":"Artist","album":"Other Release"}]}}}"""
                }
                else -> error("Unexpected library request")
            }
            respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }))
        try {
            val results = catalogue.search("Artist", library)
            assertEquals(emptyList<DiscoverArtist>(), results.artists)
            assertEquals(listOf("New Album"), results.albums.map { it.title })
            assertEquals(listOf("Track One", "Track Two"), results.albums.single().tracks.map { it.title })
            assertTrue(results.albums.single().tracks.last().explicit)
            assertTrue(results.tracks.isEmpty()) // owned on another release; future single also excluded
            assertFalse(releasedCatalogueDate("2026", "2026-09-30"))
            assertTrue(releasedCatalogueDate("2026-09-30", "2026-09-30"))
            assertTrue(catalogueQueryMatches("Future Honest", "Future", "Honest"))
            assertFalse(catalogueQueryMatches("Future Honest", "Future", "Monster"))
            assertEquals(albumOwnershipKey("Artist", "Album"), albumOwnershipKey("ARTIST", "Album [Expanded Edition]"))
            assertNotEquals(albumOwnershipKey("Artist", "Album"), albumOwnershipKey("Artist", "Album (Part Two)"))
            assertNotEquals(albumOwnershipKey("Artist", "Album"), albumOwnershipKey("Artist", "Album (Special Place)"))
            assertNotEquals(albumOwnershipKey("Artist", "Album"), albumOwnershipKey("Someone Else", "Album"))
        } finally { catalogue.close(); library.close() }
    }

    @Test fun librarySynchronizationReportsRealStepsAndFailsWithoutReturningPartialData() = runBlocking {
        val requests = mutableListOf<String>()
        var fail = false
        val library = SubsonicClient(ServerCredentials("https://library.test", "owner", "secret"),
            HttpClient(MockEngine { request ->
                val endpoint = request.url.encodedPath.substringAfterLast('/').substringBefore('.')
                requests += endpoint
                val data = when (endpoint) {
                    "getAlbumList2" -> if (request.url.parameters["type"] == "newest")
                        """"albumList2":{"album":[{"id":"album","name":"New Album","artist":"Artist"}]}"""
                        else """"albumList2":{"album":[]}"""
                    "getArtists" -> """"artists":{"index":[{"artist":[{"id":"artist","name":"Artist"}]}]}"""
                    "getPlaylists" -> """"playlists":{"playlist":[]}"""
                    "getGenres" -> """"genres":{"genre":[]}"""
                    else -> error(endpoint)
                }
                respond(if (fail && endpoint == "getPlaylists")
                    """{"subsonic-response":{"status":"failed","error":{"code":40,"message":"Offline"}}}"""
                    else """{"subsonic-response":{"status":"ok",$data}}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }))
        try {
            val progress = mutableListOf<Float>()
            val snapshot = loadLibrary(library) { value, _ -> progress += value }
            assertEquals("New Album", snapshot.albums.single().name)
            assertEquals("Artist", snapshot.artists.single().name)
            assertEquals(listOf(0f, 1f / 6, 2f / 6, 3f / 6, 4f / 6, 5f / 6, 1f), progress)
            assertEquals(6, requests.size)
            fail = true
            requests.clear()
            assertFailsWith<SubsonicException> { loadLibrary(library) { _, _ -> } }
            assertEquals(listOf("getAlbumList2", "getArtists", "getPlaylists"), requests)
        } finally { library.close() }
    }

    @Test fun ownershipIndexPagesPastFirstFiveHundredAlbums() = runBlocking {
        val offsets = mutableListOf<String>()
        val library = SubsonicClient(ServerCredentials("https://library.test", "owner", "secret"),
            HttpClient(MockEngine { request ->
                val offset = request.url.parameters["offset"]!!
                offsets += offset
                val count = if (offset == "0") 500 else 1
                val entries = (0 until count).joinToString(",") {
                    """{"id":"$offset-$it","name":"Album $it","artist":"Artist"}"""
                }
                respond("""{"subsonic-response":{"status":"ok","albumList2":{"album":[$entries]}}}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }))
        try {
            assertEquals(501, library.allAlbums().size)
            assertEquals(listOf("0", "500"), offsets)
        } finally { library.close() }
    }
}
