package com.liquidglass

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaConstants
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.DefaultMediaNotificationProvider
import com.liquidglass.shared.Song
import com.liquidglass.shared.SubsonicClient
import com.liquidglass.shared.shuffledUpcomingIndices
import com.liquidglass.shared.restoredUpcomingIndices
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
    private var unshuffledUpcomingIds: List<String>? = null
    private val savePosition = object : Runnable {
        override fun run() {
            player?.takeIf { it.isPlaying }?.let(::saveQueue)
            handler.postDelayed(this, 5_000L)
        }
    }
    private val playerListener = object : Player.Listener {
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            val player = player ?: return
            val start = player.currentMediaItemIndex + 1
            val items = (0 until player.mediaItemCount).map(player::getMediaItemAt)
            if (start !in 0..items.size) return
            val upcoming = items.drop(start)
            val reordered = if (shuffleModeEnabled) {
                unshuffledUpcomingIds = upcoming.map { it.mediaId }
                shuffledUpcomingIndices(items.map { it.toSong() }, start - 1,
                    smart = settingsStore.load().smartShuffle).map(items::get)
            } else {
                val original = unshuffledUpcomingIds ?: return
                unshuffledUpcomingIds = null
                restoredUpcomingIndices(upcoming.map { it.mediaId }, original).map(upcoming::get)
            }
            // Replace only the future: current media, position and playback history stay untouched.
            if (reordered != upcoming) player.replaceMediaItems(start, items.size, reordered)
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_TIMELINE_CHANGED) ||
                events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED)) saveQueue(player)
        }
    }

    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build().apply {
            setSmallIcon(R.drawable.glaze_launcher_foreground)
        })
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        val exoPlayer = ExoPlayer.Builder(this).build().apply {
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
        librarySession = MediaLibrarySession.Builder(this, exoPlayer,
            object : MediaLibrarySession.Callback {}).build()
        handler.postDelayed(savePosition, 5_000L)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        librarySession

    override fun onDestroy() {
        handler.removeCallbacks(savePosition)
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

    private fun MediaItem.toSong(): Song {
        val extras = mediaMetadata.extras
        return Song(
            id = mediaId,
            title = mediaMetadata.title?.toString() ?: "Unknown title",
            artist = mediaMetadata.artist?.toString() ?: "Unknown artist",
            album = mediaMetadata.albumTitle?.toString() ?: "Unknown album",
            artistId = extras?.getString("artistId"),
            albumId = extras?.getString("albumId"),
            coverArt = extras?.getString("coverArtId"),
            durationSeconds = extras?.getInt("durationSeconds") ?: 0,
            track = extras?.getInt("track")?.takeIf { extras.containsKey("track") },
            genre = extras?.getString("genre"),
            playCount = extras?.getInt("playCount") ?: 0,
            created = extras?.getString("created"),
            starred = extras?.getBoolean("starred") ?: false,
            suffix = extras?.getString("suffix"),
            samplingRate = extras?.getInt("samplingRate")?.takeIf { extras.containsKey("samplingRate") },
            bitRate = extras?.getInt("bitRate")?.takeIf { extras.containsKey("bitRate") },
            isExplicit = extras?.getBoolean("isExplicit") ?: false,
        )
    }

    @OptIn(UnstableApi::class)
    private fun Song.toMediaItem(client: SubsonicClient): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setUri(client.streamUrl(id))
        .setMediaMetadata(MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(coverArt?.let { Uri.parse(client.coverArtUrl(it)) })
            .setExtras(Bundle().apply {
                putString("artistId", artistId)
                putString("albumId", albumId)
                putString("coverArtId", coverArt)
                putInt("durationSeconds", durationSeconds)
                track?.let { putInt("track", it) }
                putString("genre", genre)
                putInt("playCount", playCount)
                putString("created", created)
                putBoolean("starred", starred)
                putString("suffix", suffix)
                samplingRate?.let { putInt("samplingRate", it) }
                bitRate?.let { putInt("bitRate", it) }
                putBoolean("isExplicit", isExplicit)
                if (isExplicit) putLong(MediaConstants.EXTRAS_KEY_IS_EXPLICIT,
                    MediaConstants.EXTRAS_VALUE_ATTRIBUTE_PRESENT)
            }).build())
        .build()
}
