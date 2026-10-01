package com.glaze.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class AcquisitionClientTest {
    @Test fun historyDatesAndStorageRoundTrip() = runBlocking {
        val api = AcquisitionClient("https://companion.test", ServerCredentials("https://library.test", "owner", "secret"),
            HttpClient(MockEngine { request ->
                assertEquals("owner", request.headers["X-Jam-Navidrome-User"])
                val body = if (request.url.encodedPath.endsWith("storage"))
                    """{"library":{"totalBytes":4000000000000,"availableBytes":3000000000000},"downloads":null,"sharedDisk":false}"""
                else """[{"ID":"old","CreatedAt":"2026-10-01T13:00:00Z"},{"ID":"new","CreatedAt":"2026-10-01T13:00:00.900Z"}]"""
                respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }))
        try {
            assertEquals(listOf("new", "old"), api.jobs().sortedByDescending { it.createdAt }.map { it.id })
            val storage = api.storage()
            assertEquals(3_000_000_000_000, storage.library!!.availableBytes)
            assertNull(storage.downloads)
            assertEquals("3.0 TB", formatStorageBytes(storage.library.availableBytes))
            assertEquals("1.5 GB", formatStorageBytes(1_500_000_000))
        } finally { api.close() }
    }

    @Test fun catalogueRequestUsesExistingOwnerAuthenticatedPipeline() = runBlocking {
        var posts = 0
        val response = """{"ID":"job1","Artist":"Future","Title":"Honest","Kind":"album","Source":"slskd","Status":"searching","Error":""}"""
        val api = AcquisitionClient("https://companion.test", ServerCredentials(
            "https://library.test", "owner", "secret"), HttpClient(MockEngine { request ->
            assertEquals("owner", request.headers["X-Jam-Navidrome-User"])
            assertEquals(24, request.headers["X-Jam-Navidrome-Salt"]!!.length)
            assertEquals(32, request.headers["X-Jam-Navidrome-Token"]!!.length)
            assertNull(request.headers["Authorization"])
            assertTrue("secret" !in request.url.toString())
            if (request.method == HttpMethod.Post) {
                posts++
                assertEquals("/api/v1/acquisition/jobs", request.url.encodedPath)
                val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                assertEquals("Future", body["artist"]!!.jsonPrimitive.content)
                assertEquals("Honest", body["title"]!!.jsonPrimitive.content)
                assertEquals("album", body["kind"]!!.jsonPrimitive.content)
            } else assertTrue(request.url.encodedPath in listOf("/api/v1/acquisition/jobs/job1", "/api/v1/acquisition/jobs"))
            respond(if (request.method == HttpMethod.Get && request.url.encodedPath.endsWith("/jobs")) "[$response]"
                else response, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }))
        try {
            val job = api.create("Future", "Honest", "album")
            assertEquals("job1", job.id)
            assertEquals("searching", api.get(job.id).status)
            assertTrue(api.jobs().single().pending)
            assertFalse(job.copy(status = "rescanned").pending)
            assertFalse(job.copy(status = "failed").pending)
            assertEquals(1, posts)
        } finally { api.close() }
    }
}
