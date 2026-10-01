package com.glaze.shared

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal data class AcquisitionJob(val id: String, val artist: String, val title: String,
                                   val kind: String, val source: String, val status: String,
                                   val error: String, val createdAt: Long = 0) {
    val pending: Boolean get() = status in setOf("searching", "downloading", "moved")
}

internal data class StorageSpace(val totalBytes: Long, val availableBytes: Long)
internal data class CompanionStorage(val library: StorageSpace?, val downloads: StorageSpace?, val sharedDisk: Boolean)

internal class AcquisitionClient(private val url: String, private val credentials: ServerCredentials,
                                 private val http: HttpClient = platformHttpClient().config {
                                     install(HttpTimeout) { requestTimeoutMillis = 180_000; socketTimeoutMillis = 180_000 }
                                 }) : AutoCloseable {
    private val json = Json { ignoreUnknownKeys = true }
    private fun baseUrl(): String {
        check(url.isNotBlank()) { "Set your companion server address in Settings first." }
        return validatedCompanionUrl(url)
    }

    private fun io.ktor.client.request.HttpRequestBuilder.ownerHeaders() {
        val (salt, token) = saltedToken(credentials.password)
        header("X-Jam-Navidrome-User", credentials.username)
        header("X-Jam-Navidrome-Salt", salt)
        header("X-Jam-Navidrome-Token", token)
    }

    suspend fun create(artist: String, title: String, kind: String): AcquisitionJob =
        parse(http.post("${baseUrl()}/api/v1/acquisition/jobs") {
            ownerHeaders()
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("artist", artist.trim())
                put("title", title.trim())
                put("kind", kind)
            }.toString())
        }.body())

    suspend fun get(id: String): AcquisitionJob = parse(http.get(
        "${baseUrl()}/api/v1/acquisition/jobs/$id") { ownerHeaders() }.body())

    suspend fun jobs(): List<AcquisitionJob> = json.parseToJsonElement(http.get(
        "${baseUrl()}/api/v1/acquisition/jobs") { ownerHeaders() }.body<String>())
        .jsonArray.map { job(it.jsonObject) }

    suspend fun latest(): AcquisitionJob? = jobs().firstOrNull()

    suspend fun storage(): CompanionStorage {
        val obj = json.parseToJsonElement(http.get("${baseUrl()}/api/v1/storage") { ownerHeaders() }.body<String>()).jsonObject
        fun space(key: String): StorageSpace? = (obj[key] as? JsonObject)?.let {
            StorageSpace(it["totalBytes"]!!.jsonPrimitive.long, it["availableBytes"]!!.jsonPrimitive.long)
        }
        return CompanionStorage(space("library"), space("downloads"), obj["sharedDisk"]?.jsonPrimitive?.booleanOrNull == true)
    }

    private fun parse(body: String): AcquisitionJob = job(json.parseToJsonElement(body).jsonObject)

    private fun job(obj: JsonObject): AcquisitionJob = AcquisitionJob(
        obj.value("ID"), obj.value("Artist"), obj.value("Title"), obj.value("Kind"),
        obj.value("Source"), obj.value("Status"), obj.value("Error"),
        runCatching { kotlin.time.Instant.parse(obj.value("CreatedAt")).toEpochMilliseconds() }.getOrDefault(0))

    private fun JsonObject.value(name: String) = get(name)?.jsonPrimitive?.content.orEmpty()

    override fun close() = http.close()
}
