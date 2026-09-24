package com.liquidglass

import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import com.google.common.util.concurrent.ListenableFuture
import com.liquidglass.shared.MusicApp
import com.liquidglass.shared.AppSettings
import com.liquidglass.shared.ServerCredentials
import com.liquidglass.shared.Song
import com.liquidglass.shared.SubsonicClient
import com.liquidglass.shared.shuffleSongs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject internal lateinit var saved: CredentialStore
    @Inject internal lateinit var queueStore: QueueStore
    @Inject internal lateinit var settingsStore: SettingsStore
    private val credentials = mutableStateOf<ServerCredentials?>(null)
    private val appSettings = mutableStateOf(AppSettings())
    private val nowPlaying = mutableStateOf<Song?>(null)
    private val isPlaying = mutableStateOf(false)
    private val queue = mutableStateOf<List<Song>>(emptyList())
    private val currentIndex = mutableIntStateOf(0)
    private val positionMs = mutableLongStateOf(0L)
    private val durationMs = mutableLongStateOf(0L)
    private val playerColor = mutableStateOf(Color.Black)
    private val playerBackdropColor = mutableStateOf(Color.Black)
    private val isShuffleEnabled = mutableStateOf(false)
    private val repeatMode = mutableIntStateOf(Player.REPEAT_MODE_OFF)
    private val playbackSpeed = mutableFloatStateOf(1f)
    private var lastArtworkId: String? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var pendingPlay: Triple<Song, List<Song>, Boolean>? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updatePlayerState(player)
        }
    }

    private fun updatePlayerState(player: Player) {
        isPlaying.value = player.isPlaying
        isShuffleEnabled.value = player.shuffleModeEnabled
        repeatMode.intValue = player.repeatMode
        playbackSpeed.floatValue = player.playbackParameters.speed
        queue.value = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).toSong() }
        currentIndex.intValue = player.currentMediaItemIndex.coerceAtLeast(0)
        nowPlaying.value = player.currentMediaItem?.toSong()
        val artworkId = nowPlaying.value?.coverArt
        if (artworkId != lastArtworkId) {
            lastArtworkId = artworkId
            playerColor.value = Color.Black
            playerBackdropColor.value = Color.Black
            val account = credentials.value
            if (artworkId != null && account != null) lifecycleScope.launch {
                val sampled = withContext(Dispatchers.IO) { sampleArtworkColors(account, artworkId) }
                if (lastArtworkId == artworkId) {
                    playerColor.value = sampled.first
                    playerBackdropColor.value = sampled.second
                }
            }
        }
        positionMs.longValue = player.currentPosition.coerceAtLeast(0L)
        durationMs.longValue = player.duration.takeUnless { it == C.TIME_UNSET || it < 0 } ?: 0L
    }

    private fun MediaItem.toSong() = Song(mediaId,
        mediaMetadata.title?.toString() ?: "Unknown title",
        mediaMetadata.artist?.toString() ?: "Unknown artist",
        mediaMetadata.albumTitle?.toString() ?: "Unknown album",
        artistId = mediaMetadata.extras?.getString("artistId"),
        albumId = mediaMetadata.extras?.getString("albumId"),
        coverArt = mediaMetadata.extras?.getString("coverArtId"),
        durationSeconds = mediaMetadata.extras?.getInt("durationSeconds") ?: 0,
        track = mediaMetadata.extras?.let { if (it.containsKey("track")) it.getInt("track") else null },
        genre = mediaMetadata.extras?.getString("genre"),
        playCount = mediaMetadata.extras?.getInt("playCount") ?: 0,
        created = mediaMetadata.extras?.getString("created"),
        starred = mediaMetadata.extras?.getBoolean("starred") ?: false,
        suffix = mediaMetadata.extras?.getString("suffix"),
        samplingRate = mediaMetadata.extras?.let {
            if (it.containsKey("samplingRate")) it.getInt("samplingRate") else null },
        bitRate = mediaMetadata.extras?.let {
            if (it.containsKey("bitRate")) it.getInt("bitRate") else null },
        isExplicit = mediaMetadata.extras?.getBoolean("isExplicit") ?: false)

    private fun sampleArtworkColors(account: ServerCredentials, artworkId: String): Pair<Color, Color> {
        val client = SubsonicClient(account)
        return try {
            val connection = URL(client.coverArtUrl(artworkId, 96)).openConnection().apply {
                connectTimeout = 5_000
                readTimeout = 5_000
            }
            val bitmap = connection.getInputStream().use { stream ->
                BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 4 })
            } ?: return Color.Black to Color.Black
            try {
                val palette = Palette.from(bitmap).generate()
                val swatch = palette.vibrantSwatch ?: palette.lightVibrantSwatch
                    ?: palette.darkVibrantSwatch ?: palette.mutedSwatch
                    ?: palette.lightMutedSwatch ?: palette.dominantSwatch
                val accent = swatch?.let { Color(it.rgb) } ?: Color.Black
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                accent to artworkBackdropColor(pixels,
                    palette.dominantSwatch?.let { Color(it.rgb) }, accent)
            } finally {
                bitmap.recycle()
            }
        } catch (_: Exception) { Color.Black to Color.Black }
        finally { client.close() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        credentials.value = saved.load()
        appSettings.value = settingsStore.load()
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync().also { future ->
            future.addListener({
                runCatching { future.get() }.onSuccess {
                    controller = it
                    it.addListener(listener)
                    updatePlayerState(it)
                    pendingPlay?.let { (song, queue, shuffled) -> play(song, queue, shuffled) }
                    pendingPlay = null
                }
            }, ContextCompat.getMainExecutor(this))
        }

        setContent {
            MusicApp(
                credentials = credentials.value,
                onConnect = { url, username, password ->
                    runCatching {
                        val candidate = ServerCredentials(url, username, password)
                        withContext(Dispatchers.IO) {
                            SubsonicClient(candidate).let { client ->
                                try { client.ping() } finally { client.close() }
                            }
                        }
                        saved.save(candidate)
                        credentials.value = candidate
                    }
                },
                onDisconnect = {
                    controller?.stop()
                    controller?.clearMediaItems()
                    nowPlaying.value = null
                    queueStore.clear()
                    saved.clear()
                    credentials.value = null
                },
                nowPlaying = nowPlaying.value,
                isPlaying = isPlaying.value,
                playerColor = playerColor.value,
                playerBackdropColor = playerBackdropColor.value,
                queue = queue.value,
                currentIndex = currentIndex.intValue,
                positionMs = positionMs.longValue,
                durationMs = durationMs.longValue,
                isShuffleEnabled = isShuffleEnabled.value,
                repeatMode = repeatMode.intValue,
                playbackSpeed = playbackSpeed.floatValue,
                settings = appSettings.value,
                onSettingsChange = { updated ->
                    appSettings.value = updated
                    settingsStore.save(updated)
                },
                onReadPosition = {
                    controller?.let { player ->
                        player.currentPosition.coerceAtLeast(0L) to
                            (player.duration.takeUnless { it == C.TIME_UNSET || it < 0 } ?: 0L)
                    } ?: (positionMs.longValue to durationMs.longValue)
                },
                onLightSystemBars = { light ->
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = light
                        isAppearanceLightNavigationBars = light
                    }
                },
                onToggleShuffle = {
                    controller?.let { player ->
                        if (player.isCommandAvailable(Player.COMMAND_SET_SHUFFLE_MODE)) {
                            player.shuffleModeEnabled = !player.shuffleModeEnabled
                        }
                    }
                },
                onCycleRepeat = {
                    controller?.let { player ->
                        if (player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)) {
                            player.repeatMode = when (player.repeatMode) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                else -> Player.REPEAT_MODE_OFF
                            }
                        }
                    }
                },
                onChangePlaybackSpeed = { speed ->
                    controller?.let { player ->
                        if (player.isCommandAvailable(Player.COMMAND_SET_SPEED_AND_PITCH)) {
                            player.setPlaybackSpeed(speed)
                        }
                    }
                },
                onClearUpcoming = {
                    controller?.let { player ->
                        val current = player.currentMediaItemIndex
                        if (player.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS) &&
                            current >= 0 && current + 1 < player.mediaItemCount) {
                            player.removeMediaItems(current + 1, player.mediaItemCount)
                        }
                    }
                },
                onPlay = { song, songs -> play(song, songs) },
                onTogglePlayback = { controller?.let { if (it.isPlaying) it.pause() else it.play() } },
                onSkipNext = { controller?.seekToNextMediaItem() },
                onSkipPrevious = { controller?.seekToPreviousMediaItem() },
                onSeek = { controller?.seekTo(it) },
                onAddNext = ::addNext,
                onAddToQueue = ::addToQueue,
                onShareSong = { song ->
                    val message = "${song.title} — ${song.artist}\n${song.album}"
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    startActivity(Intent.createChooser(send, "Share song"))
                },
                onRemoveFromQueue = { index -> controller?.removeMediaItem(index) },
                onMoveInQueue = { from, to -> controller?.moveMediaItem(from, to) },
                onPlayQueueIndex = { index ->
                    controller?.seekToDefaultPosition(index)
                    controller?.play()
                },
                onShuffleSongs = ::shuffle,
            )
        }
    }

    private fun shuffle(songs: List<Song>) {
        if (songs.isEmpty()) return
        val first = shuffleSongs(songs, smart = appSettings.value.smartShuffle).first()
        val ordered = songs.toMutableList().apply { remove(first); add(0, first) }
        play(first, ordered, shuffled = true)
    }

    private fun play(song: Song, queue: List<Song>, shuffled: Boolean = false) {
        val player = controller ?: run { pendingPlay = Triple(song, queue, shuffled); return }
        val account = credentials.value ?: return
        val ordered = queue.ifEmpty { listOf(song) }
        val items = SubsonicClient(account).use { client -> ordered.map { it.toMediaItem(client) } }
        // Start each explicitly selected queue in its requested mode; never inherit a hidden order.
        player.shuffleModeEnabled = false
        player.setMediaItems(items, ordered.indexOfFirst { it.id == song.id }.coerceAtLeast(0), 0L)
        player.shuffleModeEnabled = shuffled
        player.prepare()
        player.play()
    }

    private fun Song.toMediaItem(client: SubsonicClient): MediaItem =
        MediaItem.Builder()
                .setMediaId(id)
                .setUri(client.streamUrl(id))
                .setMediaMetadata(MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(coverArt?.let { Uri.parse(client.coverArtUrl(it)) })
                    .setExtras(Bundle().apply {
                        putString("coverArtId", coverArt)
                        putString("artistId", artistId)
                        putString("albumId", albumId)
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
                    })
                    .build())
                .build()

    private fun addNext(song: Song) {
        val player = controller ?: return
        val account = credentials.value ?: return
        if (player.mediaItemCount == 0) { play(song, listOf(song)); return }
        player.addMediaItem((player.currentMediaItemIndex + 1).coerceAtMost(player.mediaItemCount),
            SubsonicClient(account).use { song.toMediaItem(it) })
    }

    private fun addToQueue(song: Song) {
        val player = controller ?: return
        val account = credentials.value ?: return
        if (player.mediaItemCount == 0) { play(song, listOf(song)); return }
        player.addMediaItem(SubsonicClient(account).use { song.toMediaItem(it) })
    }

    override fun onDestroy() {
        controller?.removeListener(listener)
        controller = null
        controllerFuture?.let(MediaController::releaseFuture)
        controllerFuture = null
        super.onDestroy()
    }
}
