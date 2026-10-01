package com.glaze.shared

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

data class ReleaseTrack(val id: String, val title: String, val artist: String,
    val duration: Int, val explicit: Boolean, val released: Boolean)

data class UpcomingAlbum(val id: String, val artistId: String, val title: String,
    val artist: String, val releaseDate: String, val releaseAt: Long, val cover: String,
    val trackCount: Int, val tracks: List<ReleaseTrack>, val saved: Boolean,
    val jobId: String, val status: String, val error: String, val followed: Boolean = false) {
    val saveLabel: String get() = when (status) {
        "rescanned" -> "Added to library"
        "failed" -> "Waiting to retry"
        "searching", "downloading", "moved" -> "Adding to library"
        else -> if (saved) "Pre-Saved" else "Pre-Save"
    }
}

class ReleaseClient(private val address: String, private val credentials: ServerCredentials,
    private val http: HttpClient = platformHttpClient().config {
        install(HttpTimeout) { requestTimeoutMillis = 180_000; socketTimeoutMillis = 180_000 }
    }) : AutoCloseable {
    private val json = Json { ignoreUnknownKeys = true }
    private fun base() = validatedCompanionUrl(address)
    private fun HttpRequestBuilder.ownerHeaders() {
        val (salt, token) = saltedToken(credentials.password)
        header("X-Jam-Navidrome-User", credentials.username)
        header("X-Jam-Navidrome-Salt", salt)
        header("X-Jam-Navidrome-Token", token)
    }
    suspend fun albums(artistId: String? = null): List<UpcomingAlbum> {
        // Short connection failures during resume should not replace the last good feed with an error.
        repeat(3) { attempt ->
            try {
                return json.parseToJsonElement(http.get("${base()}/api/v1/releases") {
                    ownerHeaders(); artistId?.let { parameter("artist_id", it) }
                }.body<String>()).jsonArray.map { parse(it.jsonObject) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) {
                if (error is ClientRequestException || attempt == 2) throw error
                delay(1_000L * (attempt + 1))
            }
        }
        error("Release refresh failed")
    }

    suspend fun album(id: String): UpcomingAlbum {
        require(id.isNotEmpty() && id.all(Char::isDigit))
        return parse(json.parseToJsonElement(http.get("${base()}/api/v1/releases/$id") {
            ownerHeaders()
        }.body<String>()).jsonObject)
    }
    suspend fun save(id: String, saved: Boolean): UpcomingAlbum {
        require(id.isNotEmpty() && id.all(Char::isDigit))
        return parse(json.parseToJsonElement(http.put("${base()}/api/v1/releases/$id/presave") {
            ownerHeaders(); contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("saved", saved) }.toString())
        }.body<String>()).jsonObject)
    }
    private fun parse(o: JsonObject): UpcomingAlbum {
        fun text(key: String) = o[key]?.jsonPrimitive?.content.orEmpty()
        return UpcomingAlbum(text("id"), text("artistId"), text("title"), text("artist"),
            text("releaseDate"), o["releaseAt"]?.jsonPrimitive?.longOrNull ?: 0L, text("cover"),
            o["trackCount"]?.jsonPrimitive?.intOrNull ?: 0,
            o["tracks"]?.jsonArray.orEmpty().map { item ->
                val t = item.jsonObject
                ReleaseTrack(t["id"]!!.jsonPrimitive.content, t["title"]!!.jsonPrimitive.content,
                    t["artist"]!!.jsonPrimitive.content, t["duration"]?.jsonPrimitive?.intOrNull ?: 0,
                    t["explicit"]?.jsonPrimitive?.booleanOrNull ?: false,
                    t["released"]?.jsonPrimitive?.booleanOrNull ?: false)
            }, o["saved"]?.jsonPrimitive?.booleanOrNull ?: false, text("jobId"), text("status"), text("error"),
            o["followed"]?.jsonPrimitive?.booleanOrNull ?: false)
    }
    override fun close() = http.close()
}

internal fun releaseCountdown(releaseAt: Long, now: Long): List<Long> {
    val seconds = ((releaseAt - now).coerceAtLeast(0) + 999) / 1_000
    return listOf(seconds / 86_400, seconds / 3_600 % 24, seconds / 60 % 60, seconds % 60)
}
