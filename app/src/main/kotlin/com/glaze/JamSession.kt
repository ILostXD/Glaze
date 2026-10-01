package com.glaze


import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.glaze.shared.JamPlayback
import com.glaze.shared.JamViewState
import com.glaze.shared.Song
import com.glaze.shared.SubsonicClient
import com.glaze.shared.saltedToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import kotlin.math.abs
import javax.inject.Inject

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Singleton

/** The service owns transport and synchronization; the Activity only observes this state. */
@Singleton
internal class JamSession @Inject constructor(
    private val saved: CredentialStore, private val settingsStore: SettingsStore,
) {
    val jam = mutableStateOf(JamViewState())
    var onStateChanged: () -> Unit = {}
    private var controller: Player? = null
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var jamSeedTrackIds: List<String>? = null
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
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && jam.value.isHost &&
                jam.value.queue.isNotEmpty()) nextJamTrack()
        }

        override fun onEvents(player: Player, events: Player.Events) {
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


    init {
        jamSaved = saved.loadJam()?.let { stored ->
            if (stored.apiToken.isNotEmpty()) stored.copy(apiToken = "").also(saved::saveJam)
            else stored
        }
        if (jamSaved == null) jam.value = JamViewState(url = settingsStore.load().companionUrl, name = saved.load()?.username.orEmpty())
        jamSaved?.let { jam.value = JamViewState(url = if (it.sessionId.isEmpty()) settingsStore.load().companionUrl else it.url, name = it.name,
            sessionId = it.sessionId, inviteToken = it.inviteToken,
            memberId = it.memberId,
            connection = if (it.sessionId.isEmpty()) "Disconnected" else "Reconnecting",
            inviteQr = if (it.sessionId.isNotEmpty() && it.inviteToken.isNotEmpty())
                makeJamQr(jamInviteLink(it.url, it.sessionId, it.inviteToken)) else null) }
    }

    fun attach(player: Player) {
        controller = player
        scope.cancel()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        player.addListener(listener)
        if (jam.value.sessionId.isNotEmpty()) {
            player.setPlaybackSpeed(1f)
            player.repeatMode = Player.REPEAT_MODE_OFF
            player.shuffleModeEnabled = false
        }
        jamSaved?.takeIf { it.sessionId.isNotEmpty() }?.let(::resumeJam)
    }

    fun detach() {
        onStateChanged = {}
        controller?.removeListener(listener)
        closeJamTransport()
        scope.cancel()
        controller = null
    }

    private fun runOnUiThread(action: () -> Unit) {
        Handler(Looper.getMainLooper()).post(action)
    }

    fun inviteText(): String? = jamSaved?.takeIf {
        it.sessionId.isNotEmpty() && it.inviteToken.isNotEmpty()
    }?.let { "Join my Glaze Jam: ${jamInviteLink(it.url, it.sessionId, it.inviteToken)}" }

    fun configureAddress(url: String) {
        if (jam.value.sessionId.isEmpty() && jam.value.pendingInvite.isBlank())
            jam.value = jam.value.copy(url = url)
    }

    fun guestPlayback(playing: Boolean) {
        val state = jam.value
        if (!state.guestPlayback || state.connection != "Connected") return
        val current = state.playback ?: return
        if (current.trackId.isEmpty()) return
        sendJam(JSONObject().put("type", "playback.set").put("track_id", current.trackId)
            .put("playing", playing)
            .put("position_ms", current.targetPosition(System.currentTimeMillis() + jamClockOffsetMs)))
    }
    private fun makeJamQr(link: String) = runCatching {
        val size = 512
        val matrix = QRCodeWriter().encode(link, BarcodeFormat.QR_CODE, size, size,
            mapOf(EncodeHintType.MARGIN to 1))
        val pixels = IntArray(size * size) { index ->
            if (matrix[index % size, index / size]) android.graphics.Color.BLACK
            else android.graphics.Color.WHITE
        }
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, size, 0, 0, size, size)
        }.asImageBitmap()
    }.getOrNull()

    fun startJam(url: String, name: String, sessionId: String?) {
        if (controller == null) {
            jam.value = jam.value.copy(error = "Playback is connecting. Try again in a moment.")
            return
        }
        val inviteLink = sessionId?.let(::parseJamInviteLink)
        val base = runCatching { validatedJamUrl(inviteLink?.first ?: url) }.getOrElse {
            jam.value = jam.value.copy(error = it.message ?: "Invalid companion address"); return
        }
        val account = saved.load() ?: return
        val invite = (inviteLink?.second ?: sessionId)?.let(::parseJamInvite)
        if (sessionId != null && invite == null) {
            jam.value = jam.value.copy(error = "Enter a valid invite link or code")
            return
        }
        jamSeedTrackIds = if (sessionId == null) (0 until (controller?.mediaItemCount ?: 0)).map { controller!!.getMediaItemAt(it).toSong() }.drop((controller?.currentMediaItemIndex ?: -1) + 1)
            .take(1000).map(Song::id) else null
        closeJamTransport()
        val generation = jamGeneration
        val config = JamSaved(base, "", name.trim().ifBlank { account.username })
        jamSaved = config
        saved.saveJam(config)
        jam.value = JamViewState(base, config.name, connection = "Connecting")
        val relay = JamRelay(base)
        jamRelay = relay
        scope.launch {
            runCatching {
                if (sessionId == null) {
                    val (salt, hash) = saltedToken(account.password)
                    relay.create(config.name, account.username, salt, hash)
                }
                else relay.join(invite!!.first, invite.second, config.name)
            }.onSuccess { identity ->
                if (generation != jamGeneration) return@onSuccess
                val joined = config.copy(sessionId = identity.sessionId,
                    memberId = identity.memberId, memberToken = identity.memberToken,
                    inviteToken = identity.inviteToken)
                jamSaved = joined
                saved.saveJam(joined)
                controller?.apply {
                    setPlaybackSpeed(1f)
                    repeatMode = Player.REPEAT_MODE_OFF
                    shuffleModeEnabled = false
                }
                jam.value = jam.value.copy(sessionId = joined.sessionId,
                    inviteToken = joined.inviteToken, memberId = joined.memberId,
                    hostId = identity.snapshot.hostId, members = identity.snapshot.members,
                    queue = identity.snapshot.queue, playback = identity.snapshot.playback,
                    guestPlayback = identity.snapshot.guestPlayback,
                    connection = "Connecting", error = null, pendingInvite = "",
                    inviteQr = if (sessionId == null) makeJamQr(jamInviteLink(
                        base, joined.sessionId, joined.inviteToken)) else null)
                onStateChanged()
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
        val relay = JamRelay(config.url)
        jamRelay = relay
        openJamSocket(config, relay, generation)
    }

    private fun openJamSocket(config: JamSaved, relay: JamRelay, generation: Int) {
        jamSocket = relay.connect(config.sessionId, config.memberToken, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                runOnUiThread {
                    if (generation != jamGeneration) return@runOnUiThread
                    jam.value = jam.value.copy(connection = "Connected", error = null)
                    saved.loadJamAvatar().takeIf(String::isNotEmpty)?.let { avatar ->
                        webSocket.send(JSONObject().put("type", "profile.set").put("avatar", avatar).toString())
                    }
                    jamHeartbeat?.cancel()
                    jamHeartbeat = scope.launch {
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
                    if (generation == jamGeneration && response?.code in listOf(401, 403, 404)) {
                        clearJamSession()
                        jam.value = jam.value.copy(error = "This Jam has ended. Start or join another Jam.")
                        return@runOnUiThread
                    }
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
            jamReconnect = scope.launch {
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
                        playback = snapshot.playback, guestPlayback = snapshot.guestPlayback,
                        connection = "Connected", error = null)
                    onStateChanged()
                    if (jam.value.isHost) {
                        jamSeedTrackIds?.let { tracks ->
                            jamSeedTrackIds = null
                            if (tracks.isNotEmpty() && snapshot.queue.isEmpty())
                                sendJam(JSONObject().put("type", "queue.seed")
                                    .put("track_ids", org.json.JSONArray(tracks)))
                        }
                        if (snapshot.playback.trackId.isEmpty() && controller?.currentMediaItem != null) {
                            jamAwaitingNext = false
                            jamAdvancing = false
                            controller?.let(::publishHostPlayback)
                        } else if (jamAwaitingNext || playerStateDiffers(snapshot.playback) ||
                            (!wasConnected && snapshot.playback.trackId.isNotEmpty())) {
                            jamAwaitingNext = false
                            jamAdvancing = false
                            syncJamPlayback(forceHost = true)
                        }
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
            jamApply = scope.launch {
                try {
                    val account = saved.load() ?: return@launch
                    val song = runCatching { SubsonicClient(account).use { it.songById(target.trackId) } }
                        .getOrNull()
                    if (jam.value.playback?.trackId != target.trackId) return@launch
                    if (song == null) {
                        jam.value = jam.value.copy(error = "Song unavailable in this Navidrome library")
                        return@launch
                    }
                    jamApplyingUntil = System.currentTimeMillis() + 1_500
                    val item = SubsonicClient(account).use { song.toMediaItem(it) }
                    if (jam.value.isHost && player.currentMediaItemIndex >= 0)
                        player.replaceMediaItem(player.currentMediaItemIndex, item)
                    else player.setMediaItem(item)
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

    fun sendJam(command: JSONObject): Boolean {
        if (jam.value.connection == "Connected" && jamSocket?.send(command.toString()) == true) return true
        jam.value = jam.value.copy(error = "Jam is not connected yet")
        return false
    }

    private fun playerStateDiffers(target: JamPlayback): Boolean {
        val player = controller ?: return false
        return player.currentMediaItem?.mediaId.orEmpty() != target.trackId ||
            (target.trackId.isNotEmpty() && player.playWhenReady != target.playing)
    }

    fun toggleGuestPlayback() {
        val state = jam.value
        if (!state.guestPlayback || state.connection != "Connected") return
        val current = state.playback ?: return
        if (current.trackId.isEmpty()) return
        sendJam(JSONObject().put("type", "playback.set").put("track_id", current.trackId)
            .put("playing", !current.playing)
            .put("position_ms", current.targetPosition(System.currentTimeMillis() + jamClockOffsetMs)))
    }

    fun nextJamTrack() {
        if ((!jam.value.isHost && !jam.value.guestPlayback) || jamAwaitingNext) return
        val sent = sendJam(JSONObject().put("type", "playback.next"))
        jamAwaitingNext = sent && jam.value.isHost
        if (!sent) jamAdvancing = false
    }

    fun leaveJam() {
        val config = jamSaved ?: return
        if (config.sessionId.isNotEmpty()) scope.launch {
            runCatching { JamRelay(config.url).use {
                it.leave(config.sessionId, config.memberToken) } }
        }
        clearJamSession()
    }

    fun clearJamSession() {
        closeJamTransport()
        saved.clearJamSession()
        jamSaved = saved.loadJam()
        jam.value = JamViewState(url = settingsStore.load().companionUrl,
            name = jamSaved?.name ?: saved.load()?.username.orEmpty())
        onStateChanged()
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

}
