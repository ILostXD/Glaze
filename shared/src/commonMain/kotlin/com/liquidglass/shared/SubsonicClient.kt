package com.liquidglass.shared

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

class ServerCredentials(val serverUrl: String, val username: String, val password: String) {
    init {
        val url = Url(serverUrl)
        require(url.protocol.name in setOf("http", "https") && url.host.isNotBlank()) {
            "Enter a valid HTTP or HTTPS server URL"
        }
        require(username.isNotBlank() && password.isNotBlank()) { "Username and password are required" }
    }

    override fun toString(): String = "ServerCredentials(serverUrl=$serverUrl, username=$username, password=<redacted>)"
}

data class Artist(
    val id: String, val name: String, val coverArt: String? = null, val albumCount: Int = 0,
    val imageUrl: String? = null,
)
data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val coverArt: String? = null,
    val songCount: Int = 0,
    val year: Int? = null,
    val starred: Boolean = false,
    val releaseDate: String? = null,
)
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artistId: String? = null,
    val albumId: String? = null,
    val coverArt: String? = null,
    val durationSeconds: Int = 0,
    val track: Int? = null,
    val genre: String? = null,
    val playCount: Int = 0,
    val created: String? = null,
    val starred: Boolean = false,
    val suffix: String? = null,
    val samplingRate: Int? = null,
    val bitRate: Int? = null,
    val isExplicit: Boolean = false,
)
data class Playlist(val id: String, val name: String, val songCount: Int = 0, val coverArt: String? = null)
data class SearchResults(val artists: List<Artist>, val albums: List<Album>, val songs: List<Song>)
data class LyricLine(val startMs: Long?, val text: String)
data class SongLyrics(val lines: List<LyricLine>, val synced: Boolean)

/** One authenticated Subsonic request per method. The password never enters a request URL. */
class SubsonicClient(
    val credentials: ServerCredentials,
    private val http: HttpClient = platformHttpClient(),
) : AutoCloseable {
    private val json = Json { ignoreUnknownKeys = true }

    fun url(endpoint: String, vararg parameters: Pair<String, String>): String {
        val (salt, token) = saltedToken(credentials.password)
        val root = credentials.serverUrl.trimEnd('/')
        return URLBuilder("$root/rest/$endpoint.view").apply {
            this.parameters.append("u", credentials.username)
            this.parameters.append("t", token)
            this.parameters.append("s", salt)
            this.parameters.append("v", "1.16.1")
            this.parameters.append("c", "Glaze")
            this.parameters.append("f", "json")
            parameters.forEach { (key, value) -> this.parameters.append(key, value) }
        }.buildString()
    }

    fun streamUrl(songId: String) = url("stream", "id" to songId)
    fun downloadUrl(songId: String) = url("download", "id" to songId)
    fun coverArtUrl(coverArtId: String, size: Int = 512) =
        url("getCoverArt", "id" to coverArtId, "size" to size.toString())

    suspend fun ping() { request("ping") }

    suspend fun setSongStarred(id: String, starred: Boolean) {
        request(if (starred) "star" else "unstar", "id" to id)
    }

    suspend fun setAlbumStarred(id: String, starred: Boolean) {
        request(if (starred) "star" else "unstar", "albumId" to id)
    }

    suspend fun artists(): List<Artist> = request("getArtists").obj("artists")
        .items("index").flatMap { it.items("artist") }.mapNotNull(::artist)

    suspend fun artistAlbums(id: String): List<Album> = request("getArtist", "id" to id)
        .obj("artist").items("album").mapNotNull(::album)

    suspend fun albumSongs(id: String): List<Song> = request("getAlbum", "id" to id)
        .obj("album").items("song").mapNotNull(::song)

    suspend fun albumDetails(id: String): Album? = album(request("getAlbum", "id" to id).obj("album"))

    suspend fun songById(id: String): Song? = song(request("getSong", "id" to id).obj("song"))

    /** OpenSubsonic's per-track artist references, including featured artists. */
    suspend fun songArtists(id: String): List<Artist> {
        val song = request("getSong", "id" to id).obj("song")
        return song.items("artists").mapNotNull(::artist).distinctBy { it.id }
            .ifEmpty {
                val artistId = song.string("artistId")
                if (artistId == null) emptyList()
                else listOf(Artist(artistId, song.string("artist") ?: "Unknown artist"))
            }
    }

    suspend fun artistImageUrl(id: String): String? = request("getArtistInfo2", "id" to id)
        .obj("artistInfo2").string("mediumImageUrl")

    suspend fun newestAlbums(size: Int = 30, offset: Int = 0): List<Album> =
        request("getAlbumList2", "type" to "newest", "size" to size.coerceIn(1, 500).toString(),
            "offset" to offset.coerceAtLeast(0).toString())
            .obj("albumList2").items("album").mapNotNull(::album)

    /** Enumerate the ID3 library; getAlbumList2 is paged and does not include songs. */
    suspend fun allSongs(): List<Song> {
        val albums = mutableListOf<Album>()
        val seenAlbumIds = mutableSetOf<String>()
        var offset = 0
        do {
            val page = request("getAlbumList2", "type" to "alphabeticalByName",
                "size" to "500", "offset" to offset.toString())
                .obj("albumList2").items("album").mapNotNull(::album)
            val newAlbums = page.filter { seenAlbumIds.add(it.id) }
            albums += newAlbums
            offset += page.size
        } while (page.size == 500 && newAlbums.isNotEmpty())
        return albums.flatMap { albumSongs(it.id) }.distinctBy { it.id }
    }

    suspend fun playlists(): List<Playlist> = request("getPlaylists").obj("playlists")
        .items("playlist").mapNotNull(::playlist)

    suspend fun playlistSongs(id: String): List<Song> = request("getPlaylist", "id" to id)
        .obj("playlist").items("entry").mapNotNull(::song)

    suspend fun similarSongs(id: String, count: Int = 12): List<Song> =
        request("getSimilarSongs2", "id" to id, "count" to count.toString())
            .obj("similarSongs2").items("song").mapNotNull(::song)

    suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        request("updatePlaylist", "playlistId" to playlistId, "songIdToAdd" to songId)
    }

    suspend fun lyricsBySongId(id: String): SongLyrics? {
        val candidates = request("getLyricsBySongId", "id" to id).obj("lyricsList")
            .items("structuredLyrics")
            .filter { it.string("kind") in listOf(null, "main") }
            .mapNotNull { lyrics ->
                val offset = lyrics.long("offset") ?: 0L
                val lines = lyrics.items("line").mapNotNull { line ->
                    line.string("value")?.let { value ->
                        LyricLine(line.long("start")?.minus(offset)?.coerceAtLeast(0L), value)
                    }
                }
                if (lines.none { it.text.isNotBlank() }) null
                else SongLyrics(lines, lyrics.string("synced") == "true")
            }
        return candidates.firstOrNull { it.synced } ?: candidates.firstOrNull()
    }

    suspend fun search(query: String): SearchResults {
        if (query.isBlank()) return SearchResults(emptyList(), emptyList(), emptyList())
        val result = request("search3", "query" to query.trim()).obj("searchResult3")
        return SearchResults(
            result.items("artist").mapNotNull(::artist),
            result.items("album").mapNotNull(::album),
            result.items("song").mapNotNull(::song),
        )
    }

    private suspend fun request(endpoint: String, vararg parameters: Pair<String, String>): JsonObject {
        val body = http.get(url(endpoint, *parameters)).body<String>()
        val envelope = json.parseToJsonElement(body).asObject().obj("subsonic-response")
        if (envelope.string("status") != "ok") {
            val error = envelope.obj("error")
            throw SubsonicException(error.string("message") ?: "Subsonic request failed", error.int("code"))
        }
        return envelope
    }

    override fun close() = http.close()
}

class SubsonicException(message: String, val code: Int?) : Exception(message)

private fun JsonElement?.asObject(): JsonObject = this as? JsonObject ?: JsonObject(emptyMap())
private fun JsonObject.obj(key: String): JsonObject = this[key].asObject()
private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
private fun JsonObject.int(key: String): Int? = (this[key] as? JsonPrimitive)?.intOrNull
private fun JsonObject.long(key: String): Long? = (this[key] as? JsonPrimitive)?.longOrNull
private fun JsonObject.items(key: String): List<JsonObject> = when (val value = this[key]) {
    is JsonArray -> value.mapNotNull { it as? JsonObject }
    is JsonObject -> listOf(value)
    else -> emptyList()
}
private fun artist(value: JsonObject): Artist? = value.string("id")?.let {
    Artist(it, value.string("name") ?: "Unknown artist", value.string("coverArt"),
        value.int("albumCount") ?: 0, value.string("artistImageUrl"))
}
private fun album(value: JsonObject): Album? = value.string("id")?.let {
    Album(it, value.string("name") ?: "Unknown album", value.string("artist") ?: "Unknown artist",
        value.string("coverArt"), value.int("songCount") ?: 0, value.int("year"),
        value.string("starred") != null, formatReleaseDate(value.obj("releaseDate"))
            ?: formatReleaseDate(value.obj("originalReleaseDate")))
}

private fun formatReleaseDate(value: JsonObject): String? {
    val year = value.int("year") ?: return null
    val month = value.int("month")?.takeIf { it in 1..12 } ?: return year.toString()
    val name = listOf("January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December")[month - 1]
    val day = value.int("day")?.takeIf { it in 1..31 }
    return if (day == null) "$name $year" else "$name $day, $year"
}
private fun song(value: JsonObject): Song? = value.string("id")?.let {
    Song(it, value.string("title") ?: "Unknown title", value.string("artist") ?: "Unknown artist",
        value.string("album") ?: "Unknown album", value.string("artistId"), value.string("albumId"),
        value.string("coverArt"), value.int("duration") ?: 0, value.int("track"), value.string("genre"),
        value.int("playCount") ?: 0, value.string("created"), value.string("starred") != null,
        value.string("suffix"), value.int("samplingRate"), value.int("bitRate"),
        value.string("explicitStatus")?.equals("explicit", ignoreCase = true) == true)
}
private fun playlist(value: JsonObject): Playlist? = value.string("id")?.let {
    Playlist(it, value.string("name") ?: "Untitled playlist", value.int("songCount") ?: 0,
        value.string("coverArt"))
}
