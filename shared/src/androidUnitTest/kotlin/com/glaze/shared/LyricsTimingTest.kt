package com.glaze.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class LyricsTimingTest {
    @Test
    fun noLineIsActiveBeforeFirstTimestamp() {
        val lines = listOf(LyricLine(12_000, "First line"), LyricLine(19_000, "Second line"))
        assertEquals(-1, activeLyricIndex(lines, synced = true, positionMs = 11_999))
        assertEquals(0, activeLyricIndex(lines, synced = true, positionMs = 12_000))
    }

    @Test
    fun blankPreludeDoesNotActivateLyrics() {
        val lines = listOf(LyricLine(0, ""), LyricLine(8_000, "Vocals"))
        assertEquals(-1, activeLyricIndex(lines, synced = true, positionMs = 4_000))
        assertEquals(1, activeLyricIndex(lines, synced = true, positionMs = 8_000))
    }

    @Test
    fun unsyncedLyricsHaveNoActiveLine() {
        assertEquals(-1, activeLyricIndex(listOf(LyricLine(null, "Verse")), false, 30_000))
    }
}
