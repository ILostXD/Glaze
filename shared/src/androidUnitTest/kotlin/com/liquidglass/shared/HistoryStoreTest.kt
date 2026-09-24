package com.liquidglass.shared

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.liquidglass.shared.db.HistoryDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryStoreTest {
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
