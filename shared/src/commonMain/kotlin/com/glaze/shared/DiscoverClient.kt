package com.glaze.shared

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.async
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.*
import kotlin.time.Clock

internal data class DiscoverTrack(
    val id: String, val title: String, val artist: String, val duration: Int,
    val explicit: Boolean, val albumId: String, val albumTitle: String, val cover: String,
)

internal data class DiscoverAlbum(
    val id: String, val title: String, val artist: String, val cover: String,
    val releaseDate: String, val genre: String, val tracks: List<DiscoverTrack>,
)

internal data class DiscoverArtist(val id: String, val name: String, val picture: String)

internal data class DiscoverResults(val artists: List<DiscoverArtist>, val albums: List<DiscoverAlbum>, val tracks: List<DiscoverTrack>)
internal data class LibraryRequest(val artist: String, val title: String, val kind: String, val catalogueAlbumId: String? = null, val albumTitle: String? = null)

internal fun catalogueKey(artist: String, title: String): String =
    listOf(artist, title).joinToString("|") { value ->
        value.lowercase().filter { it.isLetterOrDigit() }
    }

internal fun albumOwnershipKey(artist: String, title: String): String {
    // Edition labels are not a different album; keep meaningful subtitles and artist identity.
    val baseTitle = title.replace(Regex(
        "\\s*[\\[(](?:(?:deluxe|expanded|special|standard)(?: edition| version)?|(?:\\d+(?:st|nd|rd|th) )?anniversary edition|(?:\\d{4} )?remaster(?:ed)?(?: \\d{4})?|(?:explicit|clean)(?: version)?)[\\])]\\s*$",
        RegexOption.IGNORE_CASE), "")
    return catalogueKey(artist, baseTitle)
}

internal fun catalogueArtistCredits(credit: String, artists: List<Artist> = emptyList()): List<String> {
    val names = artists.map { it.name }
    // ponytail: legacy comma/semicolon credits are ambiguous for band names; prefer structured artists.
    return (listOf(credit) + if (names.size > 1) names else
        (names + credit).flatMap { it.split(',', ';') }).map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}

private fun albumOwnershipKeys(artist: String, title: String, artists: List<Artist> = emptyList()) =
    catalogueArtistCredits(artist, artists).map { albumOwnershipKey(it, title) }

internal fun releasedCatalogueDate(date: String, today: String): Boolean =
    Regex("\\d{4}-\\d{2}-\\d{2}").matches(date) && date <= today

internal fun catalogueQueryMatches(query: String, artist: String, title: String): Boolean {
    val text = catalogueKey(artist, title).replace("|", "")
    return query.lowercase().split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.isNotBlank() }.all { it in text }
}

/** Public metadata only. A catalogue ID must never be passed to Subsonic playback. */
internal class DiscoverClient(private val http: HttpClient = platformHttpClient(),
    private val companion: AcquisitionClient? = null) : AutoCloseable {
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun request(path: String, query: String? = null, index: Int = 0): JsonObject {
        val result = json.parseToJsonElement(http.get("https://api.deezer.com/$path") {
            query?.let { parameter("q", it); parameter("limit", "20") }
            parameter("index", index)
        }.body<String>()).jsonObject
        check(result["error"] == null) { "Catalogue unavailable. Please retry." }
        return result
    }

    suspend fun album(id: String): DiscoverAlbum {
        require(id.all(Char::isDigit) && id.isNotEmpty())
        val value = if (companion == null) request("album/$id") else try {
            json.parseToJsonElement(companion.catalogueAlbum(id)).jsonObject
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { request("album/$id") }
        val title = value.text("title")
        val cover = value.text("cover_big")
        val expected = value["nb_tracks"]?.jsonPrimitive?.intOrNull ?: error("Track count unavailable")
        val tracks = mutableListOf<DiscoverTrack>()
        var page = value["tracks"]?.jsonObject ?: error("Tracklist unavailable")
        while (true) {
            val entries = page.entries()
            tracks += entries.map { track(it, id, title, cover) }
            if (tracks.size >= expected) break
            check(entries.isNotEmpty()) { "Incomplete catalogue tracklist" }
            // Never follow a URL supplied by a remote response; page the known endpoint.
            page = request("album/$id/tracks", index = tracks.size)
        }
        check(tracks.size == expected && tracks.distinctBy { it.id }.size == expected) {
            "Incomplete catalogue tracklist. Please retry."
        }
        return DiscoverAlbum(id, title, value["artist"]!!.jsonObject.text("name"), cover,
            value.text("release_date"), value["genres"]?.jsonObject?.entries()
                ?.joinToString(" · ") { it.text("name") }.orEmpty(), tracks)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    suspend fun search(query: String, library: SubsonicClient): DiscoverResults = coroutineScope {
        val albumsSearch = async { request("search/album", query).entries() }
        val tracksSearch = async { request("search/track", query).entries() }
        val ownedAlbums = library.allAlbums().flatMap { album ->
            albumOwnershipKeys(album.artist, album.name, album.artists)
        }.toSet()
        val today = Clock.System.now().toString().take(10)
        // ponytail: first 20 catalogue matches; add paging when users need deeper results.
        val albumMatches = albumsSearch.await().filter {
            catalogueQueryMatches(query, it["artist"]!!.jsonObject.text("name"), it.text("title"))
        }.filterNot {
            albumOwnershipKeys(it["artist"]!!.jsonObject.text("name"), it.text("title")).any { key -> key in ownedAlbums }
        }
        val trackMatches = tracksSearch.await().map { value ->
            val album = value["album"]!!.jsonObject
            track(value, album.text("id"), album.text("title"), album.text("cover_big"))
        }.filter { catalogueQueryMatches(query, it.artist, "${it.title} ${it.albumTitle}") }
            .distinctBy { catalogueKey(it.artist, it.title) }.filterNot {
            albumOwnershipKeys(it.artist, it.albumTitle).any { key -> key in ownedAlbums }
        }
        val details = (albumMatches.map { it.text("id") } + trackMatches.map { it.albumId })
            .distinct().chunked(4).flatMap { batch ->
                batch.map { async { album(it) } }.awaitAll()
            }.filter { releasedCatalogueDate(it.releaseDate, today) &&
                albumOwnershipKeys(it.artist, it.title).none { key -> key in ownedAlbums } }.associateBy { it.id }
        val albums = albumMatches.mapNotNull { details[it.text("id")] }
            .distinctBy { albumOwnershipKey(it.artist, it.title) }
        val tracks = trackMatches.filter { it.albumId in details }.chunked(4).flatMap { batch ->
            batch.map { candidate -> async {
                val local = library.search(candidate.title, songCount = 500).songs
                val remoteKeys = catalogueArtistCredits(candidate.artist).map { catalogueKey(it, candidate.title) }.toSet()
                candidate.takeUnless { local.any { song ->
                    catalogueArtistCredits(song.artist, song.artists).any { catalogueKey(it, song.title) in remoteKeys }
                } }
            } }.awaitAll().filterNotNull()
        }
        DiscoverResults(emptyList(), albums, tracks)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    suspend fun artistAlbums(name: String, owned: List<Album>, artistId: String? = null): List<DiscoverAlbum> {
        val today = Clock.System.now().toString().take(10)
        val keys = owned.flatMap { albumOwnershipKeys(it.artist, it.name, it.artists) }.toSet()
        val id = artistId ?: run {
            val matches = request("search/artist", name).entries().filter { it.text("name").equals(name, true) }
            val candidates = if (matches.size <= 1) matches else matches.filter { candidate ->
                // Resolve namesakes against a known library album rather than selecting the first hit.
                request("artist/${candidate.text("id")}/albums").entries().any {
                    albumOwnershipKey(name, it.text("title")) in keys
                }
            }
            candidates.singleOrNull()?.text("id") ?: return emptyList()
        }
        require(id.isNotEmpty() && id.all(Char::isDigit))
        val albums = mutableListOf<DiscoverAlbum>()
        var index = 0
        do {
            val page = request("artist/$id/albums", index = index)
            val entries = page.entries()
            entries.forEach { value ->
                if (releasedCatalogueDate(value.text("release_date"), today) &&
                    albumOwnershipKey(name, value.text("title")) !in keys) albums += DiscoverAlbum(
                    value.text("id"), value.text("title"), name, value.text("cover_big"),
                    value.text("release_date"), "", emptyList())
            }
            index += entries.size
        } while (entries.isNotEmpty() && page["next"] != null)
        return albums.distinctBy { albumOwnershipKey(it.artist, it.title) }.sortedByDescending { it.releaseDate }
    }

    private fun track(value: JsonObject, albumId: String, albumTitle: String, cover: String) =
        DiscoverTrack(value.text("id"), value.text("title"),
            value["artist"]!!.jsonObject.text("name"),
            value["duration"]?.jsonPrimitive?.intOrNull ?: 0,
            value["explicit_lyrics"]?.jsonPrimitive?.booleanOrNull == true,
            albumId, albumTitle, cover)

    private fun JsonObject.entries() = get("data")?.jsonArray?.map { it.jsonObject }.orEmpty()
    private fun JsonObject.text(key: String) = get(key)?.jsonPrimitive?.contentOrNull.orEmpty()
    override fun close() = http.close()
}
