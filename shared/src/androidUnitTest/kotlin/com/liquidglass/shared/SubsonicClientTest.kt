package com.liquidglass.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.http.HttpMethod
import kotlinx.coroutines.runBlocking
import java.security.MessageDigest
import java.net.InetSocketAddress
import java.net.URLDecoder
import com.sun.net.httpserver.HttpServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SubsonicClientTest {
    private val credentials = ServerCredentials("https://music.example.test", "andy", "sëcret")

    @Test fun authenticatesEveryRequestWithSaltedToken() = runBlocking {
        val salts = mutableListOf<String>()
        val engine = MockEngine { request ->
            val params = request.url.parameters
            val salt = requireNotNull(params["s"])
            salts += salt
            val expected = MessageDigest.getInstance("MD5")
                .digest((credentials.password + salt).toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            assertEquals(expected, params["t"])
            assertEquals("andy", params["u"])
            assertEquals("1.16.1", params["v"])
            respond("""{"subsonic-response":{"status":"ok"}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            client.ping()
            client.ping()
            assertEquals(2, salts.distinct().size)
        } finally { client.close() }
    }

    @Test fun parsesArtistAlbumAndSongResponses() = runBlocking {
        val engine = MockEngine { request ->
            val response = when (request.url.encodedPath.substringAfterLast('/')) {
                "getArtists.view" -> """{"subsonic-response":{"status":"ok","artists":{"index":[{"name":"A","artist":[{"id":"a1","name":"Artist","albumCount":1,"coverArt":"ar-a1","artistImageUrl":"https://example.test/artist.jpg"}]}]}}}"""
                "getArtist.view" -> """{"subsonic-response":{"status":"ok","artist":{"id":"a1","album":[{"id":"b1","name":"Album","artist":"Artist"}]}}}"""
                else -> """{"subsonic-response":{"status":"ok","album":{"id":"b1","song":[{"id":"s1","title":"Song","artist":"Artist","album":"Album","duration":182,"starred":"2026-09-01T10:00:00Z","suffix":"flac","samplingRate":44100,"bitRate":828,"explicitStatus":"explicit"},{"id":"s2","title":"Other","starred":null,"explicitStatus":"clean"}]}}}"""
            }
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            assertEquals("Artist", client.artists().single().name)
            assertEquals("ar-a1", client.artists().single().coverArt)
            assertEquals("https://example.test/artist.jpg", client.artists().single().imageUrl)
            assertEquals("Album", client.artistAlbums("a1").single().name)
            val songs = client.albumSongs("b1")
            assertEquals(182, songs.first().durationSeconds)
            assertTrue(songs.first().starred)
            assertEquals("flac", songs.first().suffix)
            assertEquals(44100, songs.first().samplingRate)
            assertEquals(828, songs.first().bitRate)
            assertTrue(songs.first().isExplicit)
            assertEquals(false, songs.last().starred)
            assertEquals(false, songs.last().isExplicit)
        } finally { client.close() }
    }

    @Test fun readsAlbumReleaseDateWhenProvided() = runBlocking {
        val engine = MockEngine {
            respond("""{"subsonic-response":{"status":"ok","album":{"id":"b1","name":"Album","artist":"Artist 1 • Artist 2","artists":[{"id":"a1","name":"Artist 1"},{"id":"a2","name":"Artist 2"}],"releaseDate":{"year":2026,"month":9,"day":18}}}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            val album = requireNotNull(client.albumDetails("b1"))
            assertEquals("September 18, 2026", album.releaseDate)
            assertEquals(listOf("a1", "a2"), album.artists.map { it.id })
        } finally { client.close() }
    }

    @Test fun playlistEditsAddressPositionsAndPreserveSongOrder() = runBlocking {
        val calls = mutableListOf<Pair<String, List<String>>>()
        val engine = MockEngine { request ->
            calls += request.url.encodedPath.substringAfterLast('/') to
                (request.url.parameters.getAll("songId") ?: listOfNotNull(
                    request.url.parameters["songIndexToRemove"]))
            assertEquals("playlist-1", request.url.parameters["playlistId"])
            respond("""{"subsonic-response":{"status":"ok"}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            client.removeSongFromPlaylist("playlist-1", 2)
            client.replacePlaylistSongs("playlist-1", listOf(
                Song("s2", "Two", "Artist", "Album"),
                Song("s1", "One", "Artist", "Album"),
                Song("s2", "Two", "Artist", "Album"),
            ))
            assertEquals(listOf("updatePlaylist.view" to listOf("2"),
                "createPlaylist.view" to listOf("s2", "s1", "s2")), calls)
        } finally { client.close() }
    }

    @Test fun largePlaylistReorderUsesFormPost() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("createPlaylist.view", request.url.encodedPath.substringAfterLast('/'))
            assertEquals(HttpMethod.Post, request.method)
            respond("""{"subsonic-response":{"status":"ok"}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            client.replacePlaylistSongs("playlist-1", (0..80).map {
                Song("song-$it", "Song $it", "Artist", "Album")
            })
        } finally { client.close() }
    }

    @Test fun starsAndUnstarsSongById() = runBlocking {
        val endpoints = mutableListOf<String>()
        val engine = MockEngine { request ->
            endpoints += request.url.encodedPath.substringAfterLast('/')
            assertEquals("song/one", request.url.parameters["id"])
            respond("""{"subsonic-response":{"status":"ok"}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            client.setSongStarred("song/one", true)
            client.setSongStarred("song/one", false)
            assertEquals(listOf("star.view", "unstar.view"), endpoints)
        } finally { client.close() }
    }

    @Test fun fetchesSongDetailsForRestoredPlayback() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("getSong.view", request.url.encodedPath.substringAfterLast('/'))
            assertEquals("song/one", request.url.parameters["id"])
            respond("""{"subsonic-response":{"status":"ok","song":{"id":"song/one","title":"Song","artist":"Artist","album":"Album","suffix":"flac","samplingRate":44100,"bitRate":828}}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            val song = requireNotNull(client.songById("song/one"))
            assertEquals("flac", song.suffix)
            assertEquals(44100, song.samplingRate)
            assertEquals(828, song.bitRate)
        } finally { client.close() }
    }

    @Test fun readsIndividualArtistIdsForCollaborativeTracks() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("getSong.view", request.url.encodedPath.substringAfterLast('/'))
            respond("""{"subsonic-response":{"status":"ok","song":{"id":"s1","artist":"A\u2022 B","artistId":"a1","artists":[{"id":"a1","name":"A"},{"id":"b2","name":"B"}]}}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            assertEquals(listOf(Artist("a1", "A"), Artist("b2", "B")), client.songArtists("s1"))
        } finally { client.close() }
    }

    @Test fun getsArtistPortraitWhenMissingFromArtistIndex() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("getArtistInfo2.view", request.url.encodedPath.substringAfterLast('/'))
            assertEquals("featured-id", request.url.parameters["id"])
            respond("""{"subsonic-response":{"status":"ok","artistInfo2":{"mediumImageUrl":"https://example.test/featured.jpg"}}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            assertEquals("https://example.test/featured.jpg", client.artistImageUrl("featured-id"))
        } finally { client.close() }
    }

    @Test fun artistPageUsesServerPortraitTopSongsAndReleaseTypes() = runBlocking {
        val engine = MockEngine { request ->
            val response = when (request.url.encodedPath.substringAfterLast('/')) {
                "getArtistInfo2.view" -> """{"subsonic-response":{"status":"ok","artistInfo2":{"largeImageUrl":"https://example.test/portrait.jpg","biography":"An artist","similarArtist":[{"id":"friend","name":"Friend"}]}}}"""
                "getTopSongs.view" -> {
                    assertEquals("Artist", request.url.parameters["artist"])
                    """{"subsonic-response":{"status":"ok","topSongs":{"song":[{"id":"hit","title":"Hit"}]}}}"""
                }
                "getArtist.view" -> """{"subsonic-response":{"status":"ok","artist":{"id":"a1","name":"Artist","starred":"2026-09-01","album":[{"id":"new","name":"Newest","songCount":10,"year":2026,"releaseDate":{"year":2026,"month":6,"day":1},"releaseTypes":["Album"]},{"id":"single","name":"Track","songCount":1,"releaseTypes":["Single"]}]}}}"""
                "star.view" -> {
                    assertEquals("a1", request.url.parameters["artistId"])
                    """{"subsonic-response":{"status":"ok"}}"""
                }
                else -> error("Unexpected endpoint")
            }
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            assertTrue(requireNotNull(client.artistDetails("a1")).starred)
            val albums = client.artistAlbums("a1")
            assertEquals(20260601, albums.first().releaseOrder)
            assertEquals(listOf("Album"), albums.first().releaseTypes)
            assertEquals(false, isSingleOrEp(albums.first()))
            assertTrue(isSingleOrEp(albums.last()))
            assertEquals("https://example.test/portrait.jpg", client.artistInfo("a1").imageUrl)
            assertEquals("Friend", client.artistInfo("a1").similarArtists.single().name)
            assertEquals("Hit", client.artistTopSongs("Artist").single().title)
            client.setArtistStarred("a1", true)
        } finally { client.close() }
    }

    @Test fun addsSongToSelectedPlaylist() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("updatePlaylist.view", request.url.encodedPath.substringAfterLast('/'))
            assertEquals("playlist/one", request.url.parameters["playlistId"])
            assertEquals("song/two", request.url.parameters["songIdToAdd"])
            respond("""{"subsonic-response":{"status":"ok"}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try { client.addSongToPlaylist("playlist/one", "song/two") }
        finally { client.close() }
    }

    @Test fun starsAlbumsAndReadsSimilarSongs() = runBlocking {
        val engine = MockEngine { request ->
            val response = when (request.url.encodedPath.substringAfterLast('/')) {
                "star.view" -> {
                    assertEquals("album/one", request.url.parameters["albumId"])
                    """{"subsonic-response":{"status":"ok"}}"""
                }
                "getSimilarSongs2.view" -> {
                    assertEquals("song/one", request.url.parameters["id"])
                    assertEquals("6", request.url.parameters["count"])
                    """{"subsonic-response":{"status":"ok","similarSongs2":{"song":[{"id":"s2","title":"Related","artist":"Other","album":"Another","albumId":"a2"}]}}}"""
                }
                else -> error("Unexpected endpoint")
            }
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            client.setAlbumStarred("album/one", true)
            assertEquals("a2", client.similarSongs("song/one", 6).single().albumId)
        } finally { client.close() }
    }

    @Test fun enumeratesPagedAlbumsAndParsesShuffleMetadata() = runBlocking {
        val requestedAlbums = mutableListOf<String>()
        val engine = MockEngine { request ->
            val response = when (request.url.encodedPath.substringAfterLast('/')) {
                "getAlbumList2.view" -> {
                    assertEquals("alphabeticalByName", request.url.parameters["type"])
                    when (request.url.parameters["offset"]) {
                        "0" -> """{"subsonic-response":{"status":"ok","albumList2":{"album":[${
                            List(500) { """{"id":"first","name":"First","artist":"Artist"}""" }.joinToString(",")
                        }]}}}"""
                        "500" -> """{"subsonic-response":{"status":"ok","albumList2":{"album":[{"id":"last","name":"Last","artist":"Artist"}]}}}"""
                        else -> error("Unexpected page")
                    }
                }
                "getAlbum.view" -> {
                    val id = requireNotNull(request.url.parameters["id"])
                    requestedAlbums += id
                    """{"subsonic-response":{"status":"ok","album":{"song":[{"id":"$id-song","title":"Song","playCount":3,"created":"2026-09-01T10:00:00Z"}]}}}"""
                }
                else -> error("Unexpected endpoint")
            }
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            val songs = client.allSongs()
            assertEquals(listOf("first", "last"), requestedAlbums)
            assertEquals(listOf("first-song", "last-song"), songs.map { it.id })
            assertEquals(3, songs.first().playCount)
            assertEquals("2026-09-01T10:00:00Z", songs.first().created)
        } finally { client.close() }
    }

    @Test fun surfacesSubsonicErrors() = runBlocking {
        val engine = MockEngine {
            respond("""{"subsonic-response":{"status":"failed","error":{"code":40,"message":"Wrong username or password"}}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            val error = assertFailsWith<SubsonicException> { client.ping() }
            assertEquals(40, error.code)
            assertTrue(error.message!!.contains("Wrong username"))
        } finally { client.close() }
    }

    @Test fun prefersSyncedMainLyricsAndFallsBackToPlain() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("getLyricsBySongId.view", request.url.encodedPath.substringAfterLast('/'))
            val response = when (request.url.parameters["id"]) {
                "synced" -> """{"subsonic-response":{"status":"ok","lyricsList":{"structuredLyrics":[{"kind":"translation","synced":true,"line":[{"start":100,"value":"Translation"}]},{"synced":false,"line":[{"value":"Plain"}]},{"kind":"main","synced":true,"offset":100,"line":[{"start":500,"value":"First"},{"start":1200,"value":"Second"}]}]}}}"""
                "plain" -> """{"subsonic-response":{"status":"ok","lyricsList":{"structuredLyrics":[{"synced":false,"line":[{"value":"Only plain"}]}]}}}"""
                else -> error("Unexpected song ID")
            }
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            assertEquals(SongLyrics(listOf(LyricLine(400, "First"), LyricLine(1100, "Second")), true),
                client.lyricsBySongId("synced"))
            assertEquals(SongLyrics(listOf(LyricLine(null, "Only plain")), false),
                client.lyricsBySongId("plain"))
        } finally { client.close() }
    }

    @Test fun emptyLyricsReturnNullButServerErrorsStillSurface() = runBlocking {
        val engine = MockEngine { request ->
            val response = if (request.url.parameters["id"] == "empty")
                """{"subsonic-response":{"status":"ok","lyricsList":{"structuredLyrics":[]}}}"""
            else
                """{"subsonic-response":{"status":"failed","error":{"code":70,"message":"Not found"}}}"""
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = SubsonicClient(credentials, HttpClient(engine))
        try {
            assertEquals(null, client.lyricsBySongId("empty"))
            assertEquals(70, assertFailsWith<SubsonicException> { client.lyricsBySongId("missing") }.code)
        } finally { client.close() }
    }

    @Test fun reachesARealHttpServerUnderItsConfiguredBasePath() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var authenticated = false
        server.createContext("/navidrome/rest/ping.view") { exchange ->
            val params = exchange.requestURI.rawQuery.split('&').associate { part ->
                val key = part.substringBefore('=')
                key to URLDecoder.decode(part.substringAfter('=', ""), "UTF-8")
            }
            val salt = params.getValue("s")
            val token = MessageDigest.getInstance("MD5")
                .digest(("password" + salt).toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            authenticated = params["u"] == "tester" && params["t"] == token
            val body = """{"subsonic-response":{"status":"ok"}}""".toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        val client = SubsonicClient(ServerCredentials(
            "http://127.0.0.1:${server.address.port}/navidrome", "tester", "password"))
        try {
            client.ping()
            assertTrue(authenticated)
        } finally {
            client.close()
            server.stop(0)
        }
    }
}
