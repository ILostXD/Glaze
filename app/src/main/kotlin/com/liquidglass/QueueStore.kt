package com.liquidglass

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.liquidglass.shared.ServerCredentials
import com.liquidglass.shared.Song
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

internal data class QueueSnapshot(
    val songs: List<Song>, val index: Int, val positionMs: Long,
    val shuffleEnabled: Boolean = false, val unshuffledUpcomingIds: List<String>? = null,
)

/** Stores song identities and metadata; authenticated media URLs are rebuilt on restore. */
internal class QueueStore @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("playback_queue", Context.MODE_PRIVATE)

    fun load(credentials: ServerCredentials): QueueSnapshot? =
        preferences.getString("queue", null)?.let { decode(it, credentials) }

    fun save(credentials: ServerCredentials, snapshot: QueueSnapshot) {
        if (snapshot.songs.isEmpty()) {
            clear()
        } else {
            preferences.edit().putString("queue", encode(credentials, snapshot)).apply()
        }
    }

    fun clear() { preferences.edit().remove("queue").apply() }

    companion object {
        internal fun encode(credentials: ServerCredentials, snapshot: QueueSnapshot): String =
            JSONObject()
                .put("serverUrl", credentials.serverUrl)
                .put("username", credentials.username)
                .put("index", snapshot.index)
                .put("positionMs", snapshot.positionMs)
                .put("shuffleEnabled", snapshot.shuffleEnabled)
                .put("unshuffledUpcomingIds", snapshot.unshuffledUpcomingIds?.let { JSONArray(it) })
                .put("songs", JSONArray().apply {
                    snapshot.songs.forEach { song ->
                        put(JSONObject()
                            .put("id", song.id)
                            .put("title", song.title)
                            .put("artist", song.artist)
                            .put("album", song.album)
                            .put("artistId", song.artistId)
                            .put("albumId", song.albumId)
                            .put("coverArt", song.coverArt)
                            .put("durationSeconds", song.durationSeconds)
                            .put("track", song.track)
                            .put("genre", song.genre)
                            .put("playCount", song.playCount)
                            .put("created", song.created)
                            .put("starred", song.starred)
                            .put("suffix", song.suffix)
                            .put("samplingRate", song.samplingRate)
                            .put("bitRate", song.bitRate)
                            .put("isExplicit", song.isExplicit))
                    }
                }).toString()

        internal fun decode(raw: String, credentials: ServerCredentials): QueueSnapshot? {
            return try {
                val saved = JSONObject(raw)
                if (saved.getString("serverUrl") != credentials.serverUrl ||
                    saved.getString("username") != credentials.username) return null
                val items = saved.getJSONArray("songs")
                val songs = (0 until items.length()).map { index ->
                    val item = items.getJSONObject(index)
                    Song(
                        id = item.getString("id").also { require(it.isNotBlank()) },
                        title = item.getString("title"),
                        artist = item.getString("artist"),
                        album = item.getString("album"),
                        artistId = item.optString("artistId").takeIf(String::isNotEmpty),
                        albumId = item.optString("albumId").takeIf(String::isNotEmpty),
                        coverArt = item.optString("coverArt").takeIf(String::isNotEmpty),
                        durationSeconds = item.optInt("durationSeconds").coerceAtLeast(0),
                        track = item.optInt("track").takeIf { item.has("track") },
                        genre = item.optString("genre").takeIf(String::isNotEmpty),
                        playCount = item.optInt("playCount"),
                        created = item.optString("created").takeIf(String::isNotEmpty),
                        starred = item.optBoolean("starred"),
                        suffix = item.optString("suffix").takeIf(String::isNotEmpty),
                        samplingRate = item.optInt("samplingRate").takeIf { item.has("samplingRate") },
                        bitRate = item.optInt("bitRate").takeIf { item.has("bitRate") },
                        isExplicit = item.optBoolean("isExplicit"),
                    )
                }
                if (songs.isEmpty()) null else QueueSnapshot(
                    songs,
                    saved.optInt("index").coerceIn(songs.indices),
                    saved.optLong("positionMs").coerceAtLeast(0L),
                    saved.optBoolean("shuffleEnabled"),
                    saved.optJSONArray("unshuffledUpcomingIds")?.let { ids ->
                        (0 until ids.length()).map { ids.getString(it) }
                    },
                )
            } catch (_: Exception) { null }
        }
    }
}
