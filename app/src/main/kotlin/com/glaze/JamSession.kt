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
import org.json.JSONArray
import kotlinx.coroutines.CancellationException
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
    val remoteSong = mutableStateOf<Song?>(null)
    var remoteItem: MediaItem? = null
        private set
    private val songs = mutableMapOf<String, Song>()
    private val consumed = mutableSetOf<String>()
    private val pendingConsumption = linkedSetOf<String>()
    private var hostCommand: Job? = null
    private var lastPublished: String? = null
    private var guestApplying = false
    private var restoringQueue: List<String>? = null
    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (!jam.value.isHost) return
            (mediaItem?.localConfiguration?.tag as? String)?.takeIf { id ->
                id !in consumed && jam.value.queue.any { it.id == id }
            }?.let(pendingConsumption::add)
        }
        override fun onEvents(player: Player, events: Player.Events) {
            if (!jam.value.isHost) {
                if (jam.value.sessionId.isNotEmpty()) {
                    if (jam.value.listenLocally) syncGuestAudio()
                    else if (player.playWhenReady) player.pause()
                }
                return
            }
            // Native repeat-all wraps through the retained playback history.
            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) && player.repeatMode == Player.REPEAT_MODE_ALL &&
                player.currentMediaItemIndex == 0 && player.mediaItemCount > 1) {
                restoreUpcoming(player)
            }
            publishHostPlayback(player)
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
            listenLocally = it.listenLocally,
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
            // Reconnecting must not reset the host's playback modes.
            if (jamSaved?.inviteToken.isNullOrEmpty()) { player.pause(); player.stop() }
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

    fun guestPlayback(playing: Boolean) = requestPlayback(if (playing) "play" else "pause")

    fun requestPlayback(action: String, trackId: String? = null, position: Long? = null,
                        shuffle: Boolean? = null, repeat: Int? = null, itemId: String? = null) {
        val state = jam.value
        if (!state.isHost && (state.connection != "Connected" || !state.guestPlayback)) return
        val command = JSONObject().put("action", action)
        trackId?.let { command.put("track_id", it) }
        position?.let { command.put("position_ms", it) }
        shuffle?.let { command.put("shuffle", it) }
        repeat?.let { command.put("repeat", it) }
        itemId?.let { command.put("item_id", it) }
        if (state.isHost) applyHostCommand(command)
        else sendJam(command.put("type", "playback.request"))
    }

    fun remotePosition(): Long = jam.value.playback?.targetPosition(
        System.currentTimeMillis() + jamClockOffsetMs) ?: 0L

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
                    if (sessionId != null) { pause(); stop() }
                }
                jam.value = jam.value.copy(sessionId = joined.sessionId,
                    inviteToken = joined.inviteToken, memberId = joined.memberId,
                    hostId = identity.snapshot.hostId, members = identity.snapshot.members,
                    queue = identity.snapshot.queue, playback = identity.snapshot.playback,
                    guestPlayback = identity.snapshot.guestPlayback,
                    listenLocally = false, chooseOutput = sessionId != null,
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
                            if (jam.value.isHost) controller?.let(::publishHostPlayback)
                            else if (jam.value.listenLocally) syncGuestAudio()
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
            if (!jam.value.isHost) controller?.apply { pause(); stop() }
            onStateChanged()
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
                    jam.value = jam.value.copy(hostId = snapshot.hostId,
                        members = snapshot.members, queue = snapshot.queue,
                        playback = snapshot.playback, guestPlayback = snapshot.guestPlayback,
                        connection = "Connected", error = null)
                    consumed.retainAll(snapshot.queue.map { it.id }.toSet())
                    pendingConsumption.retainAll(snapshot.queue.map { it.id }.toSet())
                    onStateChanged()
                    if (jam.value.isHost) {
                        val pending = jamSeedTrackIds
                        if (pending != null && snapshot.queue.isEmpty() && pending.isNotEmpty()) {
                            jamSeedTrackIds = null
                            sendJam(JSONObject().put("type", "queue.seed").put("track_ids", JSONArray(pending)))
                        } else {
                            jamSeedTrackIds = null
                            if (restoringQueue == snapshot.queue.map { it.trackId }) restoringQueue = null
                            if (restoringQueue == null) syncHostQueue()
                        }
                        controller?.let(::publishHostPlayback)
                    } else {
                        if (!jam.value.listenLocally) controller?.pause()
                        loadRemoteSong(snapshot.playback.trackId)
                        syncGuestAudio()
                    }
                }
                "playback.request" -> if (jam.value.isHost) applyHostCommand(event.getJSONObject("command"),
                    fromGuest = event.optString("member_id") != jam.value.hostId)

                "heartbeat" -> {
                    val sent = event.optString("nonce").toLongOrNull()
                    if (sent != null) jamClockOffsetMs = event.getLong("server_time_ms") -
                        (sent + System.currentTimeMillis()) / 2
                }
                "error" -> {
                    restoringQueue = null
                    lastPublished = null
                    consumed.clear()
                    pendingConsumption.clear()
                    jam.value = jam.value.copy(error = "Jam action failed: ${event.optString("error")}")
                    if (event.optString("error") in listOf("item_not_found", "invalid_queue_order"))
                        controller?.let(::publishHostPlayback)
                }
                "session_ended" -> clearJamSession()
            }
        } catch (_: Exception) { jam.value = jam.value.copy(error = "Invalid relay response") }
    }

    private fun publishHostPlayback(player: Player) {
        if (!jam.value.isHost || jam.value.connection != "Connected") return
        // Publish a new song only after the host has prepared it, never while loading it.
        if (player.mediaItemCount > 0 && player.playbackState !in listOf(Player.STATE_READY, Player.STATE_ENDED)) return
        val item = player.currentMediaItem
        val track = item?.mediaId.orEmpty()
        val playing = track.isNotEmpty() && player.isPlaying
        val position = if (track.isEmpty()) 0L else player.currentPosition.coerceAtLeast(0L)
        val entry = (item?.localConfiguration?.tag as? String)?.takeIf { id ->
            id !in consumed && jam.value.queue.any { it.id == id && it.trackId == track }
        }
        val fingerprint = "$track:$playing:${position / 700}:${player.shuffleModeEnabled}:${player.repeatMode}:$entry"
        if (lastPublished == fingerprint && pendingConsumption.isEmpty()) return
        val command = JSONObject().put("type", "playback.set").put("track_id", track)
            .put("playing", playing).put("position_ms", position)
            .put("shuffle", player.shuffleModeEnabled).put("repeat", player.repeatMode)
        entry?.let { command.put("item_id", it) }
        command.put("item_ids", JSONArray(pendingConsumption.filter { it != entry }))
        val ids = (player.currentMediaItemIndex + 1 until player.mediaItemCount)
            .mapNotNull { player.getMediaItemAt(it).localConfiguration?.tag as? String }
        val expected = jam.value.queue.filter { it.id !in consumed && it.id !in pendingConsumption && it.id != entry }.map { it.id }
        if (ids.size == expected.size && ids.toSet() == expected.toSet())
            command.put("queue_ids", JSONArray(ids))
        if (sendJam(command)) {
            lastPublished = fingerprint
            entry?.let(consumed::add)
            consumed.addAll(pendingConsumption)
            pendingConsumption.clear()
        }
    }

    private fun syncHostQueue() {
        val player = controller ?: return
        val currentTag = player.currentMediaItem?.localConfiguration?.tag as? String
        val upcoming = jam.value.queue.filter { it.id !in consumed && it.id !in pendingConsumption && it.id != currentTag }
        val currentIDs = (player.currentMediaItemIndex + 1 until player.mediaItemCount)
            .map { player.getMediaItemAt(it).localConfiguration?.tag as? String }
        if (currentIDs == upcoming.map { it.id }) return
        jamApply?.cancel()
        val generation = jamGeneration
        jamApply = scope.launch {
            try {
                // Seeded albums/playlists already have metadata. Fetch only newly added songs.
                (0 until player.mediaItemCount).forEach { index ->
                    player.getMediaItemAt(index).toSong().let { songs[it.id] = it }
                }
                val account = saved.load() ?: return@launch
                val items = SubsonicClient(account).use { client -> upcoming.map { entry ->
                    val song = songs[entry.trackId] ?: (client.songById(entry.trackId) ?: error("Song unavailable")).also { songs[it.id] = it }
                    song.toMediaItem(client).buildUpon().setTag(entry.id).build()
                } }
                if (generation != jamGeneration || !jam.value.isHost ||
                    upcoming.map { it.id } != jam.value.queue.filter { it.id !in consumed && it.id !in pendingConsumption && it.id != currentTag }.map { it.id }) return@launch
                val start = (player.currentMediaItemIndex + 1).coerceAtLeast(0)
                player.replaceMediaItems(start, player.mediaItemCount, items)
                player.prepare()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { jam.value = jam.value.copy(error = "Could not load the Jam queue") }
        }
    }

    private fun loadRemoteSong(trackId: String) {
        if (remoteSong.value?.id == trackId) return
        jamApply?.cancel()
        remoteSong.value = null
        remoteItem = null
        if (trackId.isEmpty()) return
        val generation = jamGeneration
        jamApply = scope.launch {
            try {
                val account = saved.load() ?: return@launch
                val song = songs[trackId] ?: (SubsonicClient(account).use { it.songById(trackId) } ?: error("Song unavailable")).also { songs[trackId] = it }
                if (generation == jamGeneration && jam.value.playback?.trackId == trackId) {
                    remoteSong.value = song
                    remoteItem = SubsonicClient(account).use { song.toMediaItem(it) }
                    syncGuestAudio()
                    onStateChanged()
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { jam.value = jam.value.copy(error = "Song unavailable in this Navidrome library") }
        }
    }

    private fun applyHostCommand(command: JSONObject, fromGuest: Boolean = false) {
        val previous = hostCommand
        val generation = jamGeneration
        hostCommand = scope.launch {
            previous?.join()
            val action = command.getString("action")
            // Only commands using the future queue need to wait for its metadata.
            if (action in listOf("next", "queue_item", "shuffle")) jamApply?.join()
            val player = controller ?: return@launch
            if (generation != jamGeneration || !jam.value.isHost) return@launch
            if (fromGuest && (jam.value.connection != "Connected" || !jam.value.guestPlayback)) return@launch
            try {
                when (action) {
                    "play" -> player.play()
                    "pause" -> player.pause()
                    "next" -> player.seekToNextMediaItem()
                    "previous" -> {
                        if (seekJamPrevious(player)) {
                            jamApply?.cancel()
                            jamApply = null
                            restoreUpcoming(player)
                        }
                    }
                    "seek" -> player.seekTo(command.getLong("position_ms"))
                    "shuffle" -> player.shuffleModeEnabled = command.getBoolean("shuffle")
                    "repeat" -> player.repeatMode = command.getInt("repeat")
                    "queue_item" -> {
                        val id = command.getString("item_id")
                        val index = (player.currentMediaItemIndex + 1 until player.mediaItemCount)
                            .firstOrNull { player.getMediaItemAt(it).localConfiguration?.tag == id } ?: return@launch
                        player.moveMediaItem(index, player.currentMediaItemIndex + 1)
                        player.seekToNextMediaItem()
                        player.play()
                    }
                    "track" -> {
                        val account = saved.load() ?: return@launch
                        val trackId = command.getString("track_id")
                        val song = songs[trackId] ?: (SubsonicClient(account).use { it.songById(trackId) } ?: error("Song unavailable"))
                        if (generation != jamGeneration || !jam.value.isHost) return@launch
                        val item = SubsonicClient(account).use { song.toMediaItem(it) }
                        val index = player.currentMediaItemIndex.coerceAtLeast(0)
                        if (player.mediaItemCount == 0) player.setMediaItem(item)
                        else player.replaceMediaItem(index, item)
                        player.seekToDefaultPosition(index)
                        player.prepare()
                        player.play()
                    }
                }
                if (action == "previous" || action == "seek") lastPublished = null
                publishHostPlayback(player)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { jam.value = jam.value.copy(error = "Could not apply the Jam playback command") }
        }
    }

    fun sendJam(command: JSONObject): Boolean {
        if (jam.value.connection == "Connected" && jamSocket?.send(command.toString()) == true) return true
        jam.value = jam.value.copy(error = "Jam is not connected yet")
        return false
    }

    fun toggleGuestPlayback() = guestPlayback(jam.value.playback?.playing != true)
    fun nextJamTrack() = requestPlayback("next")

    private fun restoreUpcoming(player: Player) {
        restoringQueue = (player.currentMediaItemIndex + 1 until player.mediaItemCount)
            .take(1000).map { player.getMediaItemAt(it).mediaId }
        if (!sendJam(JSONObject().put("type", "queue.restore").put("track_ids", JSONArray(restoringQueue))))
            restoringQueue = null
    }

    fun chooseOutput() { jam.value = jam.value.copy(chooseOutput = true) }

    fun setListenLocally(enabled: Boolean) {
        if (jam.value.isHost || jam.value.sessionId.isEmpty()) return
        jamSaved = jamSaved?.copy(listenLocally = enabled)?.also(saved::saveJam)
        jam.value = jam.value.copy(listenLocally = enabled, chooseOutput = false)
        if (enabled) syncGuestAudio() else controller?.apply { pause(); stop() }
    }

    private fun syncGuestAudio() {
        val state = jam.value
        if (state.isHost || !state.listenLocally || state.connection != "Connected" || guestApplying) return
        val player = controller ?: return
        val target = state.playback ?: return
        val song = remoteSong.value
        guestApplying = true
        try {
            if (target.trackId.isEmpty()) { player.pause(); player.clearMediaItems(); return }
            if (song?.id != target.trackId) { player.pause(); return }
            if (player.currentMediaItem?.mediaId != song.id || player.mediaItemCount != 1) {
                val account = saved.load() ?: return
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.shuffleModeEnabled = false
                player.setMediaItem(SubsonicClient(account).use { song.toMediaItem(it) })
                player.prepare()
                player.seekTo(remotePosition())
            }
            if (player.playbackState == Player.STATE_IDLE) player.prepare()
            // Re-anchor after buffering as well as snapshots/heartbeats; guests never advance themselves.
            val desired = remotePosition().coerceAtMost(song.durationSeconds.takeIf { it > 0 }?.times(1000L) ?: 604800000L)
            if (abs(player.currentPosition - desired) > 350) player.seekTo(desired)
            if (player.playbackState == Player.STATE_READY) player.playWhenReady = target.playing
            else if (!target.playing) player.pause()
        } finally { guestApplying = false }
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
        val wasGuest = jam.value.sessionId.isNotEmpty() && !jam.value.isHost
        closeJamTransport()
        saved.clearJamSession()
        jamSaved = saved.loadJam()
        jam.value = JamViewState(url = settingsStore.load().companionUrl,
            name = jamSaved?.name ?: saved.load()?.username.orEmpty())
        if (wasGuest) controller?.apply { pause(); stop() }
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
        hostCommand?.cancel()
        restoringQueue = null
        consumed.clear()
        pendingConsumption.clear()
        remoteSong.value = null
        remoteItem = null
        songs.clear()
        lastPublished = null
    }

}
