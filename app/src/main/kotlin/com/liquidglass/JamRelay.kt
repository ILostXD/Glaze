package com.liquidglass

import com.liquidglass.shared.JamMember
import com.liquidglass.shared.JamPlayback
import com.liquidglass.shared.JamQueueEntry
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
import java.util.concurrent.TimeUnit

internal fun validatedJamUrl(input: String): String {
    val uri = URI(input.trim())
    val local = uri.host == "localhost" || uri.host == "127.0.0.1"
    require(uri.scheme == "https" || (local && uri.scheme == "http")) {
        "Use an HTTPS companion address"
    }
    require(uri.host != null && uri.userInfo == null && uri.rawQuery == null &&
        uri.rawFragment == null && (uri.rawPath.isNullOrEmpty() || uri.rawPath == "/")) {
        "Enter only the companion server address"
    }
    return "${uri.scheme}://${uri.rawAuthority}"
}

internal fun parseJamInvite(value: String): Pair<String, String>? {
    val parts = value.trim().split(':')
    if (parts.size != 2 || !parts[0].matches(Regex("[0-9a-f]{32}")) ||
        !parts[1].matches(Regex("[0-9a-f]{64}"))) return null
    return parts[0] to parts[1]
}

internal data class JamIdentity(val sessionId: String, val memberId: String,
                                val memberToken: String, val inviteToken: String, val snapshot: JamSnapshot)
internal data class JamSnapshot(val id: String, val hostId: String, val revision: Long,
                                val members: List<JamMember>, val queue: List<JamQueueEntry>,
                                val playback: JamPlayback)

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
            JamMember(it.getString("id"), it.getString("name")) } },
        (0 until queue.length()).map { i -> queue.getJSONObject(i).let { item ->
            val votes = item.getJSONObject("votes")
            JamQueueEntry(item.getString("id"), item.getString("track_id"),
                item.getString("added_by"), votes.keys().asSequence().toSet())
        } },
        JamPlayback(playback.getString("track_id"), playback.getBoolean("playing"),
            playback.getLong("position_ms"), playback.getLong("updated_at_ms"),
            json.getLong("server_time_ms")))
}

internal class JamRelay(private val url: String, private val apiToken: String) : AutoCloseable {
    private val http = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS).followRedirects(false)
        .followSslRedirects(false).build()

    suspend fun create(name: String): JamIdentity = identity("/api/v1/jam/sessions",
        JSONObject().put("name", name))

    suspend fun join(sessionId: String, inviteToken: String, name: String): JamIdentity = identity(
        "/api/v1/jam/sessions/${pathPart(sessionId)}/join",
        JSONObject().put("name", name).put("invite_token", inviteToken))

    suspend fun leave(sessionId: String, memberToken: String) {
        request("/api/v1/jam/sessions/${pathPart(sessionId)}/leave", "POST", null, memberToken)
    }

    private suspend fun identity(path: String, body: JSONObject): JamIdentity {
        val result = request(path, "POST", body, null)
        return JamIdentity(result.getJSONObject("session").getString("id"),
            result.getString("member_id"), result.getString("member_token"),
            result.optString("invite_token"),
            parseJamSnapshot(result.getJSONObject("session")))
    }

    private suspend fun request(path: String, method: String, body: JSONObject?,
                                memberToken: String?): JSONObject = withContext(Dispatchers.IO) {
        val builder = Request.Builder().url(url + path)
        if (path == "/api/v1/jam/sessions") builder.header("Authorization", "Bearer $apiToken")
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
