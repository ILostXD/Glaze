package com.liquidglass.shared

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class QueueOrderTest {
    private val songs = List(12) { Song("${it / 2}", "Track $it", "Artist", "Album") }

    @Test fun shufflePreservesHistoryCurrentAndEveryUpcomingOccurrence() {
        for (smart in listOf(false, true)) for (current in songs.indices) repeat(25) { seed ->
            val order = shuffledUpcomingIndices(songs, current, smart, Random(seed))
            val expected = (current + 1 until songs.size).toList()
            assertEquals(expected, order.sorted())
            if (expected.size > 1) assertNotEquals(expected, order)
            val result = songs.take(current + 1) + order.map(songs::get)
            assertEquals(songs.take(current + 1), result.take(current + 1))
            assertEquals(songs[current], result[current])
        }
    }

    @Test fun turningShuffleOffRestoresOnlyRemainingSongsAndKeepsAdditions() {
        // B was already played and C removed; A occurs twice, and X was newly queued.
        val current = listOf("D", "A", "X", "A")
        val order = restoredUpcomingIndices(current, listOf("A", "B", "A", "C", "D"))
        assertEquals(listOf("A", "A", "D", "X"), order.map(current::get))
        assertEquals(current.indices.toList(), order.sorted())
    }

    @Test fun emptyAndFinishedQueuesAreSafe() {
        assertTrue(shuffledUpcomingIndices(emptyList(), 0, true).isEmpty())
        assertTrue(shuffledUpcomingIndices(songs, -1, true).isEmpty())
        assertTrue(shuffledUpcomingIndices(songs, songs.lastIndex, true).isEmpty())
        assertTrue(restoredUpcomingIndices(emptyList(), listOf("A")).isEmpty())
    }
}
