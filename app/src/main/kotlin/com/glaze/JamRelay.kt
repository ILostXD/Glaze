package com.glaze


import com.glaze.shared.JamMember
import com.glaze.shared.JamPlayback
import com.glaze.shared.JamQueueEntry
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

internal fun validatedJamUrl(input: String): String = com.glaze.shared.validatedCompanionUrl(input)

internal fun parseJamInvite(value: String): Pair<String, String>? {
    val parts = value.trim().split(':')
    if (parts.size != 2 || !parts[0].matches(Regex("[0-9a-f]{32}")) ||
        !parts[1].matches(Regex("[0-9a-f]{64}"))) return null
    return parts[0] to parts[1]
}

internal fun jamInviteLink(url: String, sessionId: String, inviteToken: String): String =
    "glaze://jam/join?server=${URLEncoder.encode(url, "UTF-8")}" +
        "&code=${URLEncoder.encode("$sessionId:$inviteToken", "UTF-8")}"

internal fun parseJamInviteLink(value: String): Pair<String, String>? = runCatching {
    val uri = URI(value)
    if (uri.scheme != "glaze" || uri.host != "jam" || uri.path != "/join" ||
        uri.fragment != null || uri.userInfo != null) return null
    val params = uri.rawQuery.orEmpty().split('&').mapNotNull { part ->
        val pair = part.split('=', limit = 2)
        if (pair.size == 2) pair[0] to URLDecoder.decode(pair[1], "UTF-8") else null
    }.toMap()
    val server = validatedJamUrl(params["server"] ?: return null)
    val code = params["code"] ?: return null
    if (parseJamInvite(code) == null) return null
    server to code
}.getOrNull()

internal data class JamIdentity(val sessionId: String, val memberId: String,
                                val memberToken: String, val inviteToken: String, val snapshot: JamSnapshot)
internal data class JamSnapshot(val id: String, val hostId: String, val revision: Long,
                                val members: List<JamMember>, val queue: List<JamQueueEntry>,
                                val playback: JamPlayback, val guestPlayback: Boolean)

internal fun JamPlayback.targetPosition(serverNowMs: Long): Long =
    if (playing) (positionMs + (serverNowMs - updatedAtMs).coerceAtLeast(0L))
        .coerceAtMost(604_800_000L) else positionMs

internal fun parseJamSnapshot(json: JSONObject): JamSnapshot {
    val members = json.getJSONArray("members")
    val queue = json.getJSONArray("queue")
    val playback = json.getJSONObject("playback")
    return JamSnapshot(json.getString("id"), json.getString("host_id"),
        json.getLong("revision"),
        (0 until members.length()).map { i -> members.getJSONObject(i).let {
            val avatar = runCatching { Base64.decode(it.optString("avatar"), Base64.DEFAULT) }
                .getOrNull()?.takeIf { bytes -> bytes.size <= 12288 && bytes.isNotEmpty() }
                ?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }?.asImageBitmap()
            JamMember(it.getString("id"), it.getString("name"), avatar) } },
        (0 until queue.length()).map { i -> queue.getJSONObject(i).let { item ->
            val votes = item.getJSONObject("votes")
            JamQueueEntry(item.getString("id"), item.getString("track_id"),
                item.getString("added_by"), votes.keys().asSequence().toSet())
        } },
        JamPlayback(playback.getString("track_id"), playback.getBoolean("playing"),
            playback.getLong("position_ms"), playback.getLong("updated_at_ms"),
            json.getLong("server_time_ms"), playback.optBoolean("shuffle"),
            playback.optInt("repeat")), json.optBoolean("guest_playback"))
}


internal class JamRelay(private val url: String) : AutoCloseable {
    private val http = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS).followRedirects(false)
        .followSslRedirects(false).build()

    suspend fun create(name: String, username: String, salt: String, token: String): JamIdentity =
        identity("/api/v1/jam/sessions", JSONObject().put("name", name),
            mapOf("X-Jam-Navidrome-User" to username,
                "X-Jam-Navidrome-Salt" to salt, "X-Jam-Navidrome-Token" to token))

    suspend fun join(sessionId: String, inviteToken: String, name: String): JamIdentity = identity(
        "/api/v1/jam/sessions/${pathPart(sessionId)}/join",
        JSONObject().put("name", name).put("invite_token", inviteToken))

    suspend fun leave(sessionId: String, memberToken: String) {
        request("/api/v1/jam/sessions/${pathPart(sessionId)}/leave", "POST", null, memberToken)
    }

    private suspend fun identity(path: String, body: JSONObject,
                                 headers: Map<String, String> = emptyMap()): JamIdentity {
        val result = request(path, "POST", body, null, headers)
        return JamIdentity(result.getJSONObject("session").getString("id"),
            result.getString("member_id"), result.getString("member_token"),
            result.optString("invite_token"),
            parseJamSnapshot(result.getJSONObject("session")))
    }

    private suspend fun request(path: String, method: String, body: JSONObject?,
                                memberToken: String?, headers: Map<String, String> = emptyMap()): JSONObject = withContext(Dispatchers.IO) {
        val builder = Request.Builder().url(url + path)
        headers.forEach { (key, value) -> builder.header(key, value) }
        memberToken?.let { builder.header("X-Jam-Member-Token", it) }
        val payload = (body?.toString() ?: "").toRequestBody("application/json".toMediaType())
        http.newCall(builder.method(method, payload).build()).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Companion returned HTTP ${response.code}")
            JSONObject(response.body.string())
        }
    }

    fun connect(sessionId: String, memberToken: String, listener: WebSocketListener): WebSocket {
        val path = "/api/v1/jam/ws?session_id=${URLEncoder.encode(sessionId, "UTF-8")}"
        val request = Request.Builder().url(url + path)
            .header("X-Jam-Member-Token", memberToken).build()
        return http.newWebSocket(request, listener)
    }

    override fun close() { http.dispatcher.executorService.shutdown() }

    private fun pathPart(value: String): String {
        require(value.matches(Regex("[A-Za-z0-9_-]{8,128}"))) { "Invalid session code" }
        return value
    }
}
