package com.glaze.shared

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmartShuffleTest {
    private fun song(id: String, plays: Int = 0, created: String? = null) =
        Song(id, id, "Artist", "Album", playCount = plays, created = created)

    @Test fun trueRandomMatchesUniformFisherYates() {
        val songs = (1..40).map { song(it.toString()) }
        assertEquals(songs.shuffled(Random(42)), shuffleSongs(songs, smart = false, random = Random(42)))
    }

    @Test fun smartShuffleIsDeterministicAndNeverLosesSongs() {
        val songs = (1..40).map { song(it.toString(), it, "2026-09-${(it % 28 + 1).toString().padStart(2, '0')}") }
        val first = shuffleSongs(songs, setOf("1", "2"), random = Random(42))
        assertEquals(first, shuffleSongs(songs, setOf("1", "2"), random = Random(42)))
        assertEquals(songs.map { it.id }.toSet(), first.map { it.id }.toSet())
        assertEquals(songs.size, first.size)
    }

    @Test fun recentlyPlayedIsStronglyDownweighted() {
        val songs = listOf(song("recent"), song("other"))
        val recentFirst = (0 until 1000).count { seed ->
            shuffleSongs(songs, setOf("recent"), random = Random(seed)).first().id == "recent"
        }
        assertTrue(recentFirst < 100, "Recent song led $recentFirst/1000 shuffles")
    }

    @Test fun newUnderplayedSongsGetARealBias() {
        val songs = listOf(song("fresh", created = "2026-09-01"),
            song("old", plays = 100, created = "2010-01-01"))
        val freshFirst = (0 until 1000).count { seed ->
            shuffleSongs(songs, random = Random(seed)).first().id == "fresh"
        }
        assertTrue(freshFirst > 850, "New underplayed song led only $freshFirst/1000 shuffles")
    }
}
