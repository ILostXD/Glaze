package com.glaze


import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.glaze.shared.PlayEvent
import com.glaze.shared.ServerCredentials
import com.glaze.shared.Song
import com.glaze.shared.SubsonicClient
import com.glaze.shared.openHistoryStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

/** Measure listening time, rather than seek position or time spent paused/buffering. */
internal class ListeningTime {
    private var lastMs = 0L
    private var playing = false
    var listenedMs = 0L
        private set
    private var submitted = false
    fun update(nowMs: Long, isPlaying: Boolean) {
        if (playing) listenedMs += (nowMs - lastMs).coerceAtLeast(0)
        lastMs = nowMs
        playing = isPlaying
    }
    fun qualifies(durationSeconds: Int): Boolean = listenedMs >=
        if (durationSeconds > 0) minOf(durationSeconds * 500L, 240_000L) else 240_000L

    fun takeSubmission(durationSeconds: Int): Boolean {
        if (submitted || !qualifies(durationSeconds)) return false
        submitted = true
        return true
    }
}

@Singleton
internal class ListeningHistory @Inject constructor(@ApplicationContext context: Context) {
    private val store = openHistoryStore(context)
    private val submissions = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var song: Song? = null
    private var account: ServerCredentials? = null
    private var startedMs = 0L
    private var time = ListeningTime()
    private var announced = false
    private var delivery: Job? = null

    private fun key(account: ServerCredentials) = "${account.serverUrl.trimEnd('/')}|${account.username}"
    fun recent(account: ServerCredentials): Set<String> = store.recentTrackIds(key(account)).toSet()

    fun playing(isPlaying: Boolean) {
        time.update(SystemClock.elapsedRealtime(), isPlaying)
        val current = song ?: return
        val owner = account ?: return
        if (isPlaying && startedMs == 0L) startedMs = System.currentTimeMillis()
        if (isPlaying && !announced) {
            announced = true
            val timestamp = startedMs
            submissions.launch {
                try { SubsonicClient(owner).use { it.scrobble(current.id, timestamp, submission = false) } }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { Log.w("GlazePlayback", "Now-playing notification failed") }
            }
        }
        if (time.takeSubmission(current.durationSeconds)) store.queueScrobble(key(owner), current.id, startedMs)
        flush(owner)
    }

    private fun flush(owner: ServerCredentials) {
        if (delivery?.isActive == true) return
        delivery = submissions.launch {
            val accountKey = key(owner)
            if (!store.hasPendingScrobbles(accountKey)) return@launch
            try {
                SubsonicClient(owner).use { client ->
                    store.submitPendingScrobbles(accountKey) { id, timestamp -> client.scrobble(id, timestamp) }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { Log.w("GlazePlayback", "Play reports queued for retry") }
        }
    }

    fun transition(next: Song?, credentials: ServerCredentials?, isPlaying: Boolean) {
        playing(false)
        val previous = song
        val owner = account
        if (previous != null && owner != null && time.listenedMs > 0) {
            val skipped = !time.qualifies(previous.durationSeconds)
            store.record(key(owner), PlayEvent(previous.id, previous.title, previous.artistId,
                previous.artist, previous.albumId, previous.album, previous.genre,
                startedMs, time.listenedMs, skipped))
        }
        song = next
        account = credentials
        startedMs = if (isPlaying) System.currentTimeMillis() else 0
        announced = false
        time = ListeningTime().apply { update(SystemClock.elapsedRealtime(), isPlaying) }
        if (credentials != null) flush(credentials)
    }
}
