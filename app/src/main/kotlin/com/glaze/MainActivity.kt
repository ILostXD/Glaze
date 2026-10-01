package com.glaze

import com.liquidglass.PlaybackService
import com.liquidglass.ReleaseNotifications

import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.util.Base64
import android.widget.Toast
import android.nfc.NfcAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import com.google.common.util.concurrent.ListenableFuture
import com.glaze.shared.MusicApp
import com.glaze.shared.JamActions
import com.glaze.shared.JamViewState
import com.glaze.shared.AppSettings
import com.glaze.shared.ServerCredentials
import com.glaze.shared.Song
import com.glaze.shared.SubsonicClient
import com.glaze.shared.shuffleSongs
import com.glaze.shared.PlaybackRequest
import com.glaze.shared.playbackRequest
import com.glaze.shared.playbackLink
import com.glaze.shared.parsePlaybackLink
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.lifecycleScope
import org.json.JSONObject
import java.net.URL
import java.io.ByteArrayOutputStream
import javax.inject.Inject

internal fun shouldShowBuffering(playWhenReady: Boolean, itemCount: Int, state: Int): Boolean =
    playWhenReady && itemCount > 0 && (state == Player.STATE_IDLE || state == Player.STATE_BUFFERING)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject internal lateinit var saved: CredentialStore
    @Inject internal lateinit var queueStore: QueueStore
    @Inject internal lateinit var settingsStore: SettingsStore
    @Inject internal lateinit var history: ListeningHistory
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
    @Inject internal lateinit var jamSession: JamSession
    private val jam get() = jamSession.jam
    private val profileAvatar = mutableStateOf<ImageBitmap?>(null)
    private val releaseToOpen = mutableStateOf<String?>(null)
    private val requestedPlayback = mutableStateOf<PlaybackRequest?>(null)
    private val playerPresentation = mutableStateOf<Boolean?>(null)
    private var playbackIntentConsumed = false
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) checkReleaseNotifications()
    }
    private fun checkReleaseNotifications() {
        val account = credentials.value ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { ReleaseNotifications.check(this@MainActivity, account, appSettings.value.companionUrl) }
        }
    }
    private fun enableReleaseNotifications() {
        if (appSettings.value.companionUrl.isBlank()) return
        ReleaseNotifications.schedule(this, credentials.value != null)
        val prefs = getSharedPreferences("release_notifications", MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED && !prefs.getBoolean("permission_requested", false)) {
            prefs.edit().putBoolean("permission_requested", true).apply()
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else checkReleaseNotifications()
    }
    private val avatarPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) setProfileAvatar(uri)
    }

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = updatePlayerState(player)
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

    private fun sampleArtworkColors(account: ServerCredentials, artworkId: String): Pair<Color, Color> {
        val client = SubsonicClient(account)
        return try {
            val publicCover = runCatching { URL(artworkId) }.getOrNull()?.takeIf {
                it.protocol == "https" && (it.host == "cdn-images.dzcdn.net" || it.host.endsWith(".mzstatic.com")) &&
                    it.userInfo == null && it.port in listOf(-1, 443)
            }
            val connection = (publicCover ?: URL(client.coverArtUrl(artworkId, 96))).openConnection().apply {
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
        profileAvatar.value = saved.loadJamAvatar().takeIf(String::isNotEmpty)?.let { encoded ->
            runCatching { Base64.decode(encoded, Base64.DEFAULT).let { bytes ->
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
            } }.getOrNull()
        }
        handleJamInviteIntent(intent)
        playbackIntentConsumed = savedInstanceState?.getBoolean("playback_intent_consumed") == true
        if (!playbackIntentConsumed) handlePlaybackIntent(intent)
        releaseToOpen.value = intent.getStringExtra(ReleaseNotifications.RELEASE_ID)
        appSettings.value = settingsStore.load().let { settings ->
            if (!settingsStore.hasCompanionAddress()) saved.loadJam()?.url?.let { legacy ->
                runCatching { com.glaze.shared.validatedCompanionUrl(legacy) }.getOrNull()
            }?.let { settings.copy(companionUrl = it).also(settingsStore::save) } ?: settings
            else settings
        }
        jamSession.configureAddress(appSettings.value.companionUrl)
        ReleaseNotifications.schedule(this, credentials.value != null && appSettings.value.companionUrl.isNotBlank())
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
            LaunchedEffect(requestedPlayback.value, credentials.value) {
                val request = requestedPlayback.value ?: return@LaunchedEffect
                val account = credentials.value ?: return@LaunchedEffect
                try {
                    check(jam.value.sessionId.isEmpty()) { "Leave your Jam to use a playback link." }
                    val tracks = withContext(Dispatchers.IO) {
                        SubsonicClient(account).use { client ->
                            when (request.kind) {
                                "playlist" -> client.playlistSongs(request.id)
                                "album" -> client.albumSongs(request.id)
                                else -> listOfNotNull(client.songById(request.id))
                            }
                        }
                    }
                    check(tracks.isNotEmpty()) { "This library item has no playable songs." }
                    if (request.shuffle) shuffle(tracks) else play(tracks.first(), tracks)
                    playerPresentation.value = request.openNowPlaying
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) {
                    Toast.makeText(this@MainActivity, if (error is IllegalStateException) error.message
                        else "Could not play this library item. Check your connection.", Toast.LENGTH_LONG).show()
                } finally {
                    if (requestedPlayback.value == request) {
                        requestedPlayback.value = null
                        playbackIntentConsumed = true
                    }
                }
            }
            MusicApp(
                playerPresentation = playerPresentation.value,
                onPlayerPresentationHandled = { playerPresentation.value = null },
                onEnableReleaseNotifications = ::enableReleaseNotifications,
                releaseToOpen = releaseToOpen.value, onReleaseOpened = { releaseToOpen.value = null },
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
                        ReleaseNotifications.schedule(this@MainActivity, appSettings.value.companionUrl.isNotBlank())
                    }
                },
                onDisconnect = {
                    leaveJam()
                    saved.clearJam()
                    jam.value = JamViewState()
                    controller?.stop()
                    controller?.clearMediaItems()
                    nowPlaying.value = null
                    queueStore.clear()
                    saved.clear()
                    credentials.value = null
                    ReleaseNotifications.schedule(this@MainActivity, false)
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
                    jamSession.configureAddress(updated.companionUrl)
                    ReleaseNotifications.schedule(this@MainActivity, credentials.value != null && updated.companionUrl.isNotBlank())
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
                    if (jam.value.sessionId.isNotEmpty() && !jam.value.isHost) {
                        toggleGuestPlayback(); return@let
                    }
                    if (it.isPlaying || shouldShowBuffering(it.playWhenReady,
                            it.mediaItemCount, it.playbackState))
                        it.pause() else it.play()
                } },
                onSkipNext = { if (jam.value.sessionId.isEmpty()) controller?.seekToNextMediaItem()
                    else if (jam.value.isHost || jam.value.guestPlayback) nextJamTrack() },
                onSkipPrevious = { if (jam.value.sessionId.isEmpty() || jam.value.isHost)
                    controller?.seekToPrevious() },
                onSeek = { if (jam.value.sessionId.isEmpty() || jam.value.isHost) controller?.seekTo(it) },
                onAddNext = ::addNext,
                onAddToQueue = ::addToQueue,
                onShareSong = { song ->
                    val message = "${song.title} — ${song.artist}\n${song.album}\n${playbackLink("song", song.id)}"
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
                onRemoveFromQueue = { index -> if (jam.value.sessionId.isEmpty()) controller?.removeMediaItem(index) },
                onRestoreQueueItem = { song, index ->
                    val player = controller
                    val account = credentials.value
                    if (player != null && account != null)
                        player.addMediaItem(index.coerceIn(0, player.mediaItemCount),
                            SubsonicClient(account).use { song.toMediaItem(it) })
                },
                onMoveInQueue = { from, to -> if (jam.value.sessionId.isEmpty()) controller?.moveMediaItem(from, to) },
                onPlayQueueIndex = { index ->
                    controller?.seekToDefaultPosition(index)
                    controller?.play()
                },
                onShuffleSongs = ::shuffle,
                jam = jam.value,
                profileAvatar = profileAvatar.value,
                onPickProfileAvatar = { avatarPicker.launch("image/*") },
                jamActions = JamActions(
                    connect = ::startJam,
                    leave = ::leaveJam,
                    add = { trackId -> sendJam(JSONObject().put("type", "queue.add")
                        .put("track_id", trackId)) },
                    remove = { itemId -> sendJam(JSONObject().put("type", "queue.remove")
                        .put("item_id", itemId)) },
                    clear = { sendJam(JSONObject().put("type", "queue.clear")) },
                    next = ::nextJamTrack,
                    share = ::shareJamInvite,
                    move = { itemId, to -> sendJam(JSONObject().put("type", "queue.move")
                        .put("item_id", itemId).put("to_index", to)) },
                    vote = { itemId -> sendJam(JSONObject().put("type", "queue.vote").put("item_id", itemId)
                        .put("vote", jam.value.queue.firstOrNull { it.id == itemId }?.voters?.contains(jam.value.memberId) != true)) },
                    setGuestPlayback = { enabled -> sendJam(JSONObject().put("type", "guest_controls.set")
                        .put("guest_playback", enabled)) },
                ),
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleJamInviteIntent(intent)
        handlePlaybackIntent(intent)
        releaseToOpen.value = intent.getStringExtra(ReleaseNotifications.RELEASE_ID)
    }

    private fun handleJamInviteIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val invite = parseJamInviteLink(intent.dataString.orEmpty()) ?: return
        if (jam.value.sessionId.isNotEmpty()) {
            jam.value = jam.value.copy(error = "Leave your current Jam before joining another")
            return
        }
        jam.value = jam.value.copy(url = invite.first, pendingInvite = invite.second, error = null)
    }

    private fun handlePlaybackIntent(intent: Intent) {
        val isLink = intent.action in listOf(Intent.ACTION_VIEW, NfcAdapter.ACTION_NDEF_DISCOVERED) &&
            intent.data?.scheme == "glaze" && intent.data?.host == "play"
        if (!isLink && intent.action !in listOf("com.glaze.action.PLAY", "com.liquidglass.action.PLAY")) return
        val request = if (isLink) parsePlaybackLink(intent.dataString.orEmpty()) else runCatching {
            playbackRequest(intent.getStringExtra("kind").orEmpty(), intent.getStringExtra("id").orEmpty(),
                intent.getBooleanExtra("shuffle", false), intent.getBooleanExtra("open_now_playing", true))
        }.getOrNull()
        if (request == null) Toast.makeText(this, "Invalid Glaze playback link", Toast.LENGTH_LONG).show()
        else {
            playbackIntentConsumed = false
            requestedPlayback.value = request
        }
    }

    private fun setProfileAvatar(uri: Uri) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Could not read that picture" }
            val sample = (minOf(bounds.outWidth, bounds.outHeight) / 128).coerceAtLeast(1)
            val source = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: error("Could not read that picture")
            val edge = minOf(source.width, source.height)
            val crop = Bitmap.createBitmap(source, (source.width - edge) / 2,
                (source.height - edge) / 2, edge, edge)
            val small = Bitmap.createScaledBitmap(crop, 96, 96, true)
            val output = ByteArrayOutputStream()
            small.compress(Bitmap.CompressFormat.JPEG, 60, output)
            val bytes = output.toByteArray()
            require(bytes.size <= 12288) { "Choose a smaller picture" }
            source.recycle(); crop.recycle(); small.recycle()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }.onSuccess { avatar ->
            saved.saveJamAvatar(avatar)
            val bytes = Base64.decode(avatar, Base64.DEFAULT)
            profileAvatar.value = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
            if (jam.value.sessionId.isNotEmpty())
                sendJam(JSONObject().put("type", "profile.set").put("avatar", avatar))
        }.onFailure { jam.value = jam.value.copy(error = it.message ?: "Could not use that picture") }
    }

    private fun shuffle(songs: List<Song>) {
        if (songs.isEmpty()) return
        val first = shuffleSongs(songs, smart = appSettings.value.smartShuffle,
            recentSongIds = credentials.value?.let(history::recent).orEmpty()).first()
        val ordered = songs.toMutableList().apply { remove(first); add(0, first) }
        play(first, ordered, shuffled = true)
    }

    private fun play(song: Song, queue: List<Song>, shuffled: Boolean = false) {
        if (jam.value.sessionId.isNotEmpty() && !jam.value.isHost) {
            if (jam.value.guestPlayback) sendJam(JSONObject().put("type", "playback.set")
                .put("track_id", song.id).put("playing", true).put("position_ms", 0))
            return
        }
        val player = controller ?: run { pendingPlay = Triple(song, queue, shuffled); return }
        val account = credentials.value ?: return
        if (jam.value.isHost && player.currentMediaItemIndex >= 0) {
            val item = SubsonicClient(account).use { song.toMediaItem(it) }
            val index = player.currentMediaItemIndex
            player.replaceMediaItem(index, item)
            player.seekToDefaultPosition(index)
            player.prepare()
            player.play()
            return
        }
        val ordered = queue.ifEmpty { listOf(song) }
        val items = SubsonicClient(account).use { client -> ordered.map { it.toMediaItem(client) } }
        // Start each explicitly selected queue in its requested mode; never inherit a hidden order.
        player.shuffleModeEnabled = false
        player.setMediaItems(items, ordered.indexOfFirst { it.id == song.id }.coerceAtLeast(0), 0L)
        player.shuffleModeEnabled = shuffled
        player.prepare()
        player.play()
    }

    private fun addNext(song: Song) {
        if (jam.value.sessionId.isNotEmpty()) {
            sendJam(JSONObject().put("type", "queue.add").put("track_id", song.id).put("next", true)); return
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

    private fun startJam(url: String, name: String, sessionId: String?) =
        jamSession.startJam(if (sessionId == null) appSettings.value.companionUrl else url, name, sessionId)
    private fun sendJam(command: JSONObject) = jamSession.sendJam(command)
    private fun nextJamTrack() = jamSession.nextJamTrack()
    private fun toggleGuestPlayback() = jamSession.toggleGuestPlayback()
    private fun leaveJam() = jamSession.leaveJam()
    private fun shareJamInvite() {
        val text = jamSession.inviteText() ?: return
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, "Share Jam invite"))
    }

    override fun onDestroy() {
        controller?.removeListener(listener)
        controller = null
        controllerFuture?.let(MediaController::releaseFuture)
        controllerFuture = null
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("playback_intent_consumed", playbackIntentConsumed)
        super.onSaveInstanceState(outState)
    }
}
