package com.glaze.shared

import io.ktor.http.Url
import io.ktor.http.encodeURLPathPart

data class PlaybackRequest(val kind: String, val id: String, val shuffle: Boolean = false,
    val openNowPlaying: Boolean = true)

fun playbackRequest(kind: String, id: String, shuffle: Boolean = false,
    openNowPlaying: Boolean = true): PlaybackRequest {
    require(kind in listOf("song", "album", "playlist")) { "Unknown playback target" }
    require(id.length in 1..256 && id != "." && id != ".." &&
        id.none { it.isWhitespace() || it.isISOControl() || it in "/\\?#" }) { "Invalid library ID" }
    return PlaybackRequest(kind, id, shuffle, openNowPlaying)
}

fun playbackLink(kind: String, id: String, shuffle: Boolean = false, openNowPlaying: Boolean = true): String {
    playbackRequest(kind, id, shuffle, openNowPlaying)
    return "glaze://play/$kind/${id.encodeURLPathPart()}?shuffle=$shuffle&nowPlaying=$openNowPlaying"
}

fun parsePlaybackLink(value: String): PlaybackRequest? = runCatching {
    val url = Url(value)
    require(url.protocol.name == "glaze" && url.host == "play" && url.user == null &&
        url.password == null && url.fragment.isEmpty())
    val path = url.segments
    require(path.size == 2)
    require(url.parameters.names().all { it in setOf("shuffle", "nowPlaying") })
    fun flag(key: String, default: Boolean): Boolean {
        val values = url.parameters.getAll(key) ?: return default
        require(values.size == 1)
        return values.single().toBooleanStrict()
    }
    playbackRequest(path[0], path[1], flag("shuffle", false), flag("nowPlaying", true))
}.getOrNull()
