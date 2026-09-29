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
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaConstants
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import com.google.common.util.concurrent.ListenableFuture
import com.liquidglass.shared.MusicApp
import com.liquidglass.shared.JamActions
import com.liquidglass.shared.JamPlayback
import com.liquidglass.shared.JamViewState
import com.liquidglass.shared.AppSettings
import com.liquidglass.shared.ServerCredentials
import com.liquidglass.shared.Song
import com.liquidglass.shared.SubsonicClient
import com.liquidglass.shared.shuffleSongs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import androidx.lifecycle.lifecycleScope
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.net.URL
import kotlin.math.abs
import javax.inject.Inject

internal fun shouldShowBuffering(playWhenReady: Boolean, itemCount: Int, state: Int): Boolean =
    playWhenReady && itemCount > 0 && (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject internal lateinit var saved: CredentialStore
    @Inject internal lateinit var queueStore: QueueStore
    @Inject internal lateinit var settingsStore: SettingsStore
    private val credentials = mutableStateOf<ServerCredentials?>(null)
    private val appSettings = mutableStateOf(AppSettings())
    private val nowPlaying = mutableStateOf<Song?>(null)
    private val isPlaying = mutableStateOf(false)
    private val isBuffering = mutableStateOf(false)
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
    private val jam = mutableStateOf(JamViewState())
    private var jamSaved: JamSaved? = null
    private var jamRelay: JamRelay? = null
    private var jamSocket: WebSocket? = null
    private var jamHeartbeat: Job? = null
    private var jamReconnect: Job? = null
    private var jamApply: Job? = null
    private var jamGeneration = 0
    private var jamRevision = -1L
    private var jamClockOffsetMs = 0L
    private var jamApplyingUntil = 0L
    private var jamAwaitingNext = false
    private var jamAdvancing = false
    private var jamLoadingTrack: String? = null
    private var lastPublished: Triple<String, Boolean, Long>? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updatePlayerState(player)
            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED) ||
                events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                if (jam.value.isHost && player.playbackState == Player.STATE_ENDED &&
                    jam.value.queue.isNotEmpty() && !jamAdvancing) {
                    jamAdvancing = true
                    nextJamTrack()
                } else publishHostPlayback(player)
            }
        }
    }

    private fun updatePlayerState(player: Player) {
        isPlaying.value = player.isPlaying
        isBuffering.value = shouldShowBuffering(player.playWhenReady,
            player.mediaItemCount, player.playbackState)
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
                accent to artworkBackdropColor(
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
        jamSaved = saved.loadJam()
        if (jamSaved == null) jam.value = JamViewState(name = credentials.value?.username.orEmpty())
        jamSaved?.let { jam.value = JamViewState(url = it.url, name = it.name,
            hasToken = it.apiToken.isNotEmpty(), sessionId = it.sessionId, inviteToken = it.inviteToken,
            memberId = it.memberId,
            connection = if (it.sessionId.isEmpty()) "Disconnected" else "Reconnecting") }
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

        jamSaved?.takeIf { it.sessionId.isNotEmpty() }?.let { resumeJam(it) }

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
                    leaveJam()
                    saved.clearJam()
                    jamSaved = null
                    jam.value = JamViewState()
                    controller?.stop()
                    controller?.clearMediaItems()
                    nowPlaying.value = null
                    queueStore.clear()
                    saved.clear()
                    credentials.value = null
                },
                nowPlaying = nowPlaying.value,
                isPlaying = isPlaying.value,
                isBuffering = isBuffering.value,
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
                onSettingsChange = { update ->
                    val updated = update(appSettings.value)
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
                onTogglePlayback = { controller?.let {
                    if (jam.value.sessionId.isNotEmpty() && !jam.value.isHost) return@let
                    if (it.isPlaying || shouldShowBuffering(it.playWhenReady,
                            it.mediaItemCount, it.playbackState))
                        it.pause() else it.play()
                } },
                onSkipNext = { if (jam.value.isHost) nextJamTrack()
                    else if (jam.value.sessionId.isEmpty()) controller?.seekToNextMediaItem() },
                onSkipPrevious = { if (jam.value.sessionId.isEmpty() || jam.value.isHost)
                    controller?.seekToPreviousMediaItem() },
                onSeek = { if (jam.value.sessionId.isEmpty() || jam.value.isHost) controller?.seekTo(it) },
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
                onShareCollection = { message ->
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    startActivity(Intent.createChooser(send, "Share from Glaze"))
                },
                onArtworkColor = { artworkId ->
                    val account = credentials.value
                    if (artworkId == null || account == null) Color.Black
                    else withContext(Dispatchers.IO) { sampleArtworkColors(account, artworkId).second }
                },
                onRemoveFromQueue = { index -> controller?.removeMediaItem(index) },
                onRestoreQueueItem = { song, index ->
                    val player = controller
                    val account = credentials.value
                    if (player != null && account != null)
                        player.addMediaItem(index.coerceIn(0, player.mediaItemCount),
                            SubsonicClient(account).use { song.toMediaItem(it) })
                },
                onMoveInQueue = { from, to -> controller?.moveMediaItem(from, to) },
                onPlayQueueIndex = { index ->
                    controller?.seekToDefaultPosition(index)
                    controller?.play()
                },
                onShuffleSongs = ::shuffle,
                jam = jam.value,
                jamActions = JamActions(
                    connect = ::startJam,
                    leave = ::leaveJam,
                    add = { trackId -> sendJam(JSONObject().put("type", "queue.add")
                        .put("track_id", trackId)) },
                    vote = { itemId, vote -> sendJam(JSONObject().put("type", "queue.vote")
                        .put("item_id", itemId).put("vote", vote)) },
                    remove = { itemId -> sendJam(JSONObject().put("type", "queue.remove")
                        .put("item_id", itemId)) },
                    next = ::nextJamTrack,
                    toggle = { if (jam.value.isHost) controller?.let {
                        if (it.isPlaying) it.pause() else it.play()
                    } },
                    share = ::shareJamInvite,
                ),
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
        if (jam.value.sessionId.isNotEmpty() && !jam.value.isHost) return
        val player = controller ?: run { pendingPlay = Triple(song, queue, shuffled); return }
        val account = credentials.value ?: return
        val ordered = if (jam.value.isHost) listOf(song) else queue.ifEmpty { listOf(song) }
        val items = SubsonicClient(account).use { client -> ordered.map { it.toMediaItem(client) } }
        // Start each explicitly selected queue in its requested mode; never inherit a hidden order.
        player.shuffleModeEnabled = false
        player.setMediaItems(items, ordered.indexOfFirst { it.id == song.id }.coerceAtLeast(0), 0L)
        player.shuffleModeEnabled = shuffled
        player.prepare()
        player.play()
    }

    @OptIn(UnstableApi::class)
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
                        if (isExplicit) putLong(MediaConstants.EXTRAS_KEY_IS_EXPLICIT,
                            MediaConstants.EXTRAS_VALUE_ATTRIBUTE_PRESENT)
                    })
                    .build())
                .build()

    private fun addNext(song: Song) {
        if (jam.value.sessionId.isNotEmpty()) {
            sendJam(JSONObject().put("type", "queue.add").put("track_id", song.id)); return
        }
        val player = controller ?: return
        val account = credentials.value ?: return
        if (player.mediaItemCount == 0) { play(song, listOf(song)); return }
        player.addMediaItem((player.currentMediaItemIndex + 1).coerceAtMost(player.mediaItemCount),
            SubsonicClient(account).use { song.toMediaItem(it) })
    }

    private fun addToQueue(song: Song) {
        if (jam.value.sessionId.isNotEmpty()) {
            sendJam(JSONObject().put("type", "queue.add").put("track_id", song.id)); return
        }
        val player = controller ?: return
        val account = credentials.value ?: return
        if (player.mediaItemCount == 0) { play(song, listOf(song)); return }
        player.addMediaItem(SubsonicClient(account).use { song.toMediaItem(it) })
    }

    private fun startJam(url: String, token: String, name: String, sessionId: String?) {
        val base = runCatching { validatedJamUrl(url) }.getOrElse {
            jam.value = jam.value.copy(error = it.message ?: "Invalid companion address"); return
        }
        val actualToken = token.ifBlank { jamSaved?.apiToken.orEmpty() }
        val invite = sessionId?.let(::parseJamInvite)
        if (name.trim().isBlank() || (sessionId == null && actualToken.isBlank()) ||
            (sessionId != null && invite == null)) {
            jam.value = jam.value.copy(error = "Enter your name and a valid host token or invite code")
            return
        }
        closeJamTransport()
        val generation = jamGeneration
        val config = JamSaved(base, if (sessionId == null) actualToken else "", name.trim())
        jamSaved = config
        saved.saveJam(config)
        jam.value = JamViewState(base, config.name, hasToken = config.apiToken.isNotEmpty(), connection = "Connecting")
        val relay = JamRelay(base, config.apiToken)
        jamRelay = relay
        lifecycleScope.launch {
            runCatching {
                if (sessionId == null) relay.create(config.name)
                else relay.join(invite!!.first, invite.second, config.name)
            }.onSuccess { identity ->
                if (generation != jamGeneration) return@onSuccess
                val joined = config.copy(sessionId = identity.sessionId,
                    memberId = identity.memberId, memberToken = identity.memberToken,
                    inviteToken = identity.inviteToken)
                jamSaved = joined
                saved.saveJam(joined)
                jam.value = jam.value.copy(sessionId = joined.sessionId,
                    inviteToken = joined.inviteToken, memberId = joined.memberId,
                    hostId = identity.snapshot.hostId, members = identity.snapshot.members,
                    queue = identity.snapshot.queue, playback = identity.snapshot.playback,
                    connection = "Connecting", error = null)
                openJamSocket(joined, relay, generation)
            }.onFailure {
                if (generation == jamGeneration)
                    jam.value = jam.value.copy(connection = "Disconnected",
                        error = it.message ?: "Could not reach the companion")
            }
        }
    }

    private fun resumeJam(config: JamSaved) {
        if (config.memberToken.isEmpty()) return
        closeJamTransport()
        val generation = jamGeneration
        val relay = JamRelay(config.url, config.apiToken)
        jamRelay = relay
        openJamSocket(config, relay, generation)
    }

    private fun openJamSocket(config: JamSaved, relay: JamRelay, generation: Int) {
        jamSocket = relay.connect(config.sessionId, config.memberToken, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                runOnUiThread {
                    if (generation != jamGeneration) return@runOnUiThread
                    jam.value = jam.value.copy(connection = "Connected", error = null)
                    jamHeartbeat?.cancel()
                    jamHeartbeat = lifecycleScope.launch {
                        while (isActive) {
                            webSocket.send(JSONObject().put("type", "heartbeat")
                                .put("nonce", System.currentTimeMillis().toString()).toString())
                            if (!jam.value.isHost) syncJamPlayback()
                            delay(5_000)
                        }
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runOnUiThread {
                    if (generation == jamGeneration) handleJamMessage(text)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) =
                scheduleJamReconnect(generation)

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                runOnUiThread {
                    if (generation == jamGeneration) jam.value = jam.value.copy(
                        connection = "Reconnecting",
                        error = response?.let { "Relay returned HTTP ${it.code}" } ?: "Relay connection lost")
                }
                scheduleJamReconnect(generation)
            }
        })
    }

    private fun scheduleJamReconnect(generation: Int) {
        runOnUiThread {
            if (generation != jamGeneration || jamSaved?.sessionId.isNullOrEmpty()) return@runOnUiThread
            jamHeartbeat?.cancel()
            jam.value = jam.value.copy(connection = "Reconnecting")
            if (jamReconnect?.isActive == true) return@runOnUiThread
            jamReconnect = lifecycleScope.launch {
                delay(3_000)
                val config = jamSaved ?: return@launch
                val relay = jamRelay ?: return@launch
                if (generation == jamGeneration) openJamSocket(config, relay, generation)
            }
        }
    }

    private fun handleJamMessage(text: String) {
        try {
            val event = JSONObject(text)
            when (event.getString("type")) {
                "connected", "state" -> {
                    val snapshot = parseJamSnapshot(event.getJSONObject("session"))
                    if (snapshot.revision < jamRevision) return
                    if (jamRevision < 0) jamClockOffsetMs =
                        snapshot.playback.serverTimeMs - System.currentTimeMillis()
                    jamRevision = snapshot.revision
                    val wasConnected = jam.value.hostId.isNotEmpty()
                    jam.value = jam.value.copy(hostId = snapshot.hostId,
                        members = snapshot.members, queue = snapshot.queue,
                        playback = snapshot.playback, connection = "Connected", error = null)
                    if (jam.value.isHost) {
                        if (jamAwaitingNext || (!wasConnected && snapshot.playback.trackId.isNotEmpty())) {
                            jamAwaitingNext = false
                            jamAdvancing = false
                            syncJamPlayback(forceHost = true)
                        } else if (snapshot.playback.trackId.isEmpty() && nowPlaying.value != null)
                            controller?.let(::publishHostPlayback)
                    } else syncJamPlayback()
                }
                "heartbeat" -> {
                    val sent = event.optString("nonce").toLongOrNull()
                    if (sent != null) jamClockOffsetMs = event.getLong("server_time_ms") -
                        (sent + System.currentTimeMillis()) / 2
                }
                "error" -> {
                    jamAdvancing = false
                    jamAwaitingNext = false
                    jam.value = jam.value.copy(error = "Jam action failed: ${event.optString("error")}")
                }
                "session_ended" -> clearJamSession()
            }
        } catch (_: Exception) { jam.value = jam.value.copy(error = "Invalid relay response") }
    }

    private fun publishHostPlayback(player: Player) {
        if (!jam.value.isHost || jam.value.connection != "Connected" ||
            jamAwaitingNext || System.currentTimeMillis() < jamApplyingUntil) return
        val track = player.currentMediaItem?.mediaId.orEmpty()
        val playing = track.isNotEmpty() && player.playWhenReady && player.playbackState != Player.STATE_ENDED
        val position = if (track.isEmpty()) 0L else player.currentPosition.coerceAtLeast(0L)
        val previous = lastPublished
        if (previous?.first == track && previous.second == playing &&
            abs(previous.third - position) < 700) return
        lastPublished = Triple(track, playing, position)
        sendJam(JSONObject().put("type", "playback.set").put("track_id", track)
            .put("playing", playing).put("position_ms", position))
    }

    private fun syncJamPlayback(forceHost: Boolean = false) {
        if (jam.value.isHost && !forceHost) return
        val target = jam.value.playback ?: return
        val player = controller ?: return
        if (target.trackId.isEmpty()) {
            if (player.currentMediaItem != null) {
                jamApplyingUntil = System.currentTimeMillis() + 1_500
                player.stop()
                player.clearMediaItems()
            }
            return
        }
        if (player.currentMediaItem?.mediaId != target.trackId) {
            if (jamLoadingTrack == target.trackId) return
            jamApply?.cancel()
            jamLoadingTrack = target.trackId
            jamApply = lifecycleScope.launch {
                try {
                    val account = credentials.value ?: return@launch
                    val song = runCatching { SubsonicClient(account).use { it.songById(target.trackId) } }
                        .getOrNull()
                    if (jam.value.playback?.trackId != target.trackId) return@launch
                    if (song == null) {
                        jam.value = jam.value.copy(error = "Song unavailable in this Navidrome library")
                        return@launch
                    }
                    jamApplyingUntil = System.currentTimeMillis() + 1_500
                    val item = SubsonicClient(account).use { song.toMediaItem(it) }
                    player.setMediaItem(item)
                    player.prepare()
                    player.seekTo(target.targetPosition(System.currentTimeMillis() + jamClockOffsetMs))
                    if (target.playing) player.play() else player.pause()
                } finally {
                    jamLoadingTrack = null
                }
            }
            return
        }
        val desired = target.targetPosition(System.currentTimeMillis() + jamClockOffsetMs)
        if (abs(player.currentPosition - desired) > 1_200) {
            jamApplyingUntil = System.currentTimeMillis() + 1_500
            player.seekTo(desired)
        }
        if (player.playWhenReady != target.playing) {
            jamApplyingUntil = System.currentTimeMillis() + 1_500
            if (target.playing) player.play() else player.pause()
        }
    }

    private fun sendJam(command: JSONObject): Boolean {
        if (jam.value.connection == "Connected" && jamSocket?.send(command.toString()) == true) return true
        jam.value = jam.value.copy(error = "Jam is not connected yet")
        return false
    }

    private fun nextJamTrack() {
        if (!jam.value.isHost || jamAwaitingNext) return
        jamAwaitingNext = sendJam(JSONObject().put("type", "playback.next"))
        if (!jamAwaitingNext) jamAdvancing = false
    }

    private fun leaveJam() {
        val config = jamSaved ?: return
        if (config.sessionId.isNotEmpty()) lifecycleScope.launch {
            runCatching { JamRelay(config.url, config.apiToken).use {
                it.leave(config.sessionId, config.memberToken) } }
        }
        clearJamSession()
    }

    private fun shareJamInvite() {
        val config = jamSaved ?: return
        if (config.inviteToken.isEmpty() || config.sessionId.isEmpty()) return
        val text = "Join my Glaze Jam\nServer: ${config.url}\nInvite code: ${config.sessionId}:${config.inviteToken}"
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, "Share Jam invite"))
    }

    private fun clearJamSession() {
        closeJamTransport()
        saved.clearJamSession()
        jamSaved = saved.loadJam()
        jam.value = JamViewState(url = jamSaved?.url.orEmpty(), name = jamSaved?.name.orEmpty(),
            hasToken = !jamSaved?.apiToken.isNullOrEmpty())
    }

    private fun closeJamTransport() {
        jamGeneration++
        jamHeartbeat?.cancel()
        jamReconnect?.cancel()
        jamApply?.cancel()
        jamSocket?.close(1000, null)
        jamSocket = null
        jamRelay?.close()
        jamRelay = null
        jamRevision = -1L
        jamAwaitingNext = false
        jamAdvancing = false
        jamLoadingTrack = null
        lastPublished = null
    }

    override fun onDestroy() {
        closeJamTransport()
        controller?.removeListener(listener)
        controller = null
        controllerFuture?.let(MediaController::releaseFuture)
        controllerFuture = null
        super.onDestroy()
    }
}
