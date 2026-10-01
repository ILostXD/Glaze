package com.liquidglass

import com.glaze.*

import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.common.ForwardingSimpleBasePlayer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import androidx.media3.session.DefaultMediaNotificationProvider
import com.glaze.shared.SubsonicClient
import com.glaze.shared.shuffledUpcomingIndices
import com.glaze.shared.restoredUpcomingIndices
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaLibraryService() {
    private var player: ExoPlayer? = null
    private var librarySession: MediaLibrarySession? = null
    private val handler = Handler(Looper.getMainLooper())
    @Inject internal lateinit var queueStore: QueueStore
    @Inject internal lateinit var credentialStore: CredentialStore
    @Inject internal lateinit var settingsStore: SettingsStore
    @Inject internal lateinit var jamSession: JamSession
    @Inject internal lateinit var history: ListeningHistory
    private var unshuffledUpcomingIds: List<String>? = null
    private val savePosition = object : Runnable {
        override fun run() {
            player?.let {
                history.playing(it.isPlaying)
                if (it.isPlaying) saveQueue(it)
            }
            handler.postDelayed(this, 5_000L)
        }
    }
    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            history.transition(mediaItem?.toSong(), credentialStore.load(), player?.isPlaying == true)
        }
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            val player = player ?: return
            val start = player.currentMediaItemIndex + 1
            val items = (0 until player.mediaItemCount).map(player::getMediaItemAt)
            if (start !in 0..items.size) return
            val upcoming = items.drop(start)
            val reordered = if (shuffleModeEnabled) {
                unshuffledUpcomingIds = upcoming.map { it.mediaId }
                shuffledUpcomingIndices(items.map { it.toSong() }, start - 1,
                    smart = settingsStore.load().smartShuffle,
                    recentSongIds = credentialStore.load()?.let(history::recent).orEmpty()).map(items::get)
            } else {
                val original = unshuffledUpcomingIds ?: return
                unshuffledUpcomingIds = null
                restoredUpcomingIndices(upcoming.map { it.mediaId }, original).map(upcoming::get)
            }
            // Replace only the future: current media, position and playback history stay untouched.
            if (reordered != upcoming) player.replaceMediaItems(start, items.size, reordered)
        }

        override fun onEvents(player: Player, events: Player.Events) {
            history.playing(player.isPlaying)
            if (events.contains(Player.EVENT_TIMELINE_CHANGED) ||
                events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED)) saveQueue(player)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build().apply {
            setSmallIcon(R.drawable.glaze_launcher_foreground)
        })
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        val exoPlayer = ExoPlayer.Builder(this).setMaxSeekToPreviousPositionMs(3_000L).build().apply {
            // The actual playlist is the visible playback order, including when shuffled.
            // This order also stays sequential across insertions, removals and queue moves.
            setShuffleOrder(ShuffleOrder.UnshuffledShuffleOrder(0))
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }
        credentialStore.load()?.let { credentials ->
            queueStore.load(credentials)?.let { saved ->
                val client = SubsonicClient(credentials)
                try {
                    exoPlayer.setMediaItems(saved.songs.map { it.toMediaItem(client) },
                        saved.index, saved.positionMs)
                    unshuffledUpcomingIds = saved.unshuffledUpcomingIds
                    exoPlayer.shuffleModeEnabled = saved.shuffleEnabled
                    exoPlayer.playWhenReady = false
                    exoPlayer.prepare()
                } finally { client.close() }
            }
        }
        exoPlayer.addListener(playerListener)
        player = exoPlayer
        history.transition(exoPlayer.currentMediaItem?.toSong(), credentialStore.load(), exoPlayer.isPlaying)
        jamSession.attach(exoPlayer)
        val sessionPlayer = object : ForwardingSimpleBasePlayer(exoPlayer) {
            fun refreshCommands() = invalidateState()
            override fun getState(): State {
                val state = super.getState()
                return state.buildUpon().setAvailableCommands(jamPlayerCommands(state.availableCommands, jamSession.jam.value)).build()
            }
            override fun handleSetPlayWhenReady(ready: Boolean): ListenableFuture<*> {
                val jam = jamSession.jam.value
                if (jam.sessionId.isNotEmpty() && !jam.isHost) {
                    jamSession.guestPlayback(ready)
                    return Futures.immediateVoidFuture()
                }
                return super.handleSetPlayWhenReady(ready)
            }
            override fun handleSeek(index: Int, positionMs: Long, command: Int): ListenableFuture<*> {
                if (jamSession.jam.value.sessionId.isNotEmpty() && command in setOf(
                    Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)) {
                    jamSession.nextJamTrack()
                    return Futures.immediateVoidFuture()
                }
                return super.handleSeek(index, positionMs, command)
            }
        }
        jamSession.onStateChanged = sessionPlayer::refreshCommands
        librarySession = MediaLibrarySession.Builder(this, sessionPlayer,
            object : MediaLibrarySession.Callback {})
            .setSessionActivity(PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .build()
        handler.postDelayed(savePosition, 5_000L)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        librarySession

    override fun onDestroy() {
        handler.removeCallbacks(savePosition)
        jamSession.detach()
        history.transition(null, null, false)
        player?.let {
            saveQueue(it)
            it.removeListener(playerListener)
        }
        librarySession?.release()
        librarySession = null
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun saveQueue(player: Player) {
        if (player.mediaItemCount == 0) {
            queueStore.clear()
            return
        }
        val credentials = credentialStore.load() ?: return
        val songs = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).toSong() }
        queueStore.save(credentials, QueueSnapshot(songs,
            player.currentMediaItemIndex.coerceIn(songs.indices),
            player.currentPosition.coerceAtLeast(0L), player.shuffleModeEnabled,
            unshuffledUpcomingIds))
    }

}
