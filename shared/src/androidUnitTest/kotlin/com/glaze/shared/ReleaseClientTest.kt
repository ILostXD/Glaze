package com.glaze.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ReleaseClientTest {
    @Test fun transientReleaseFailureRetriesAndCancellationDoesNot() = runBlocking {
        var attempts = 0
        val api = ReleaseClient("https://companion.test", ServerCredentials("https://nav.test", "andy", "password"),
            HttpClient(MockEngine {
                attempts++
                if (attempts == 1) throw java.io.IOException("Connection interrupted")
                respond("[]", headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }))
        try { assertTrue(api.albums().isEmpty()); assertEquals(2, attempts) } finally { api.close() }
        attempts = 0
        val cancelled = ReleaseClient("https://companion.test", ServerCredentials("https://nav.test", "andy", "password"),
            HttpClient(MockEngine { attempts++; throw kotlinx.coroutines.CancellationException("Page closed") }))
        try {
            assertFailsWith<kotlinx.coroutines.CancellationException> { cancelled.albums() }
            assertEquals(1, attempts)
        } finally { cancelled.close() }
    }

    @Test fun countdownAndAuthenticatedPresaveRoundTrip() = runBlocking {
        assertEquals(listOf(1L, 2L, 3L, 4L), releaseCountdown(93_784_000, 0))
        assertEquals(listOf(0L, 0L, 0L, 0L), releaseCountdown(100, 200))
        assertEquals(listOf(0L, 0L, 0L, 1L), releaseCountdown(100, 0))
        val http = HttpClient(MockEngine { request ->
            assertEquals("andy", request.headers["X-Jam-Navidrome-User"])
            assertEquals(32, request.headers["X-Jam-Navidrome-Token"]?.length)
            assertFalse(request.url.toString().contains("password"))
            val album = """{"id":"42","artistId":"nav","title":"QRÖMELIFE","artist":"Quavo","releaseAt":1790924400000,"trackCount":14,"tracks":[{"id":"1","title":"Pending","artist":"Quavo","released":false}],"saved":true,"followed":true}"""
            respond(if (request.url.encodedPath.endsWith("releases")) "[$album]" else album,
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val api = ReleaseClient("https://companion.example", ServerCredentials("https://nav.example", "andy", "password"), http)
        try {
            val album = api.albums().single()
            assertTrue(album.followed); assertTrue(album.saved); assertFalse(album.tracks.single().released)
            assertEquals("Pre-Saved", api.save("42", true).saveLabel)
            assertFailsWith<IllegalArgumentException> { api.album("../admin") }
        } finally { api.close() }
        val (order, hidden) = restoreHomeLayout("Mixes,NewLibrary,Playlists,Recent", "")
        assertEquals(HomeShelf.Upcoming, order[1]); assertFalse(HomeShelf.Upcoming in hidden)
    }
}
