package com.glaze.shared

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.glaze.shared.db.HistoryDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking

class HistoryStoreTest {
    @Test fun migratedOutboxRetriesOnlyUnacknowledgedReportsAndKeepsAccountsSeparate() = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HistoryDatabase.Schema.create(driver)
        driver.execute(null, "DROP TABLE scrobble_outbox", 0)
        HistoryStore(driver).use { history ->
            history.record("server|alice", PlayEvent("old", "Old", null, "Artist", null, "Album", null, 5, 1_000, false))
            HistoryDatabase.Schema.migrate(driver, 1, 2)
            assertEquals(1_000, history.totalListenMs("server|alice"))
            history.queueScrobble("server|alice", "playlist-track", 10)
            history.queueScrobble("server|alice", "playlist-track", 10)
            history.queueScrobble("server|alice", "last-track", 20)
            history.queueScrobble("server|bob", "private", 30)
            val sent = mutableListOf<String>()
            assertFailsWith<IllegalStateException> {
                history.submitPendingScrobbles("server|alice") { id, _ ->
                    if (id == "last-track") error("Offline")
                    sent += id
                }
            }
            assertTrue(history.hasPendingScrobbles("server|alice"))
            history.submitPendingScrobbles("server|alice") { id, timestamp ->
                assertEquals(20, timestamp)
                sent += id
            }
            assertEquals(listOf("playlist-track", "last-track"), sent)
            assertFalse(history.hasPendingScrobbles("server|alice"))
            assertTrue(history.hasPendingScrobbles("server|bob"))
        }
    }
    @Test
    fun logsAreAccountScopedAndAggregated() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HistoryDatabase.Schema.create(driver)
        HistoryStore(driver).use { history ->
            val first = PlayEvent("one", "One", "artist", "Artist", "album", "Album", "Pop", 10, 1_000, false)
            history.record("server|alice", first)
            history.record("server|alice", first.copy(timestampMs = 20, listenedMs = 2_000))
            history.record("server|alice", first.copy(trackId = "two", timestampMs = 30, skipped = true))
            history.record("server|bob", first.copy(trackId = "private", timestampMs = 40))

            assertEquals(listOf("two", "one"), history.recentTrackIds("server|alice"))
            assertEquals(listOf("one"), history.topTrackIds("server|alice"))
            assertEquals(4_000, history.totalListenMs("server|alice"))
            assertEquals(listOf("private"), history.recentTrackIds("server|bob"))
            assertEquals(0, history.totalListenMs("server|alice", fromMs = 31))
        }
    }
}
