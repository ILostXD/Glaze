package com.glaze.shared

import app.cash.sqldelight.db.SqlDriver
import com.glaze.shared.db.HistoryDatabase

data class PlayEvent(
    val trackId: String,
    val title: String,
    val artistId: String?,
    val artist: String,
    val albumId: String?,
    val album: String,
    val genre: String?,
    val timestampMs: Long,
    val listenedMs: Long,
    val skipped: Boolean,
)

/** One persistent log per installation; accountKey keeps servers and users separate. */
class HistoryStore(private val driver: SqlDriver) : AutoCloseable {
    private val queries = HistoryDatabase(driver).playEventQueries

    fun record(accountKey: String, event: PlayEvent) {
        require(accountKey.isNotBlank() && event.trackId.isNotBlank())
        queries.insertEvent(
            account_key = accountKey,
            track_id = event.trackId,
            title = event.title,
            artist_id = event.artistId,
            artist = event.artist,
            album_id = event.albumId,
            album = event.album,
            genre = event.genre,
            timestamp_ms = event.timestampMs,
            listened_ms = event.listenedMs.coerceAtLeast(0),
            skipped = if (event.skipped) 1 else 0,
        )
    }

    fun recentTrackIds(accountKey: String, limit: Int = 100): List<String> =
        queries.recentTrackIds(accountKey, limit.coerceAtLeast(0).toLong()).executeAsList()

    fun totalListenMs(accountKey: String, fromMs: Long = 0): Long =
        queries.totalListenMs(accountKey, fromMs).executeAsOne()

    fun topTrackIds(accountKey: String, fromMs: Long = 0, limit: Int = 20): List<String> =
        queries.topTrackIds(accountKey, fromMs, limit.coerceAtLeast(0).toLong()).executeAsList()

    fun queueScrobble(accountKey: String, trackId: String, timestampMs: Long) {
        require(accountKey.isNotBlank() && trackId.isNotBlank())
        queries.queueScrobble(accountKey, trackId, timestampMs)
    }

    fun hasPendingScrobbles(accountKey: String): Boolean = queries.pendingScrobbles(accountKey).executeAsList().isNotEmpty()

    /** Delete only acknowledged reports. A failed request leaves this and subsequent reports queued. */
    suspend fun submitPendingScrobbles(accountKey: String, send: suspend (String, Long) -> Unit) {
        for (event in queries.pendingScrobbles(accountKey).executeAsList()) {
            send(event.track_id, event.timestamp_ms)
            queries.acknowledgeScrobble(accountKey, event.track_id, event.timestamp_ms)
        }
    }

    override fun close() = driver.close()
}
