package com.glaze


import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListeningTimeTest {
    @Test fun submitsAtThresholdBeforeTrackChangesAndOnlyOnce() {
        val time = ListeningTime()
        time.update(0, true)
        time.update(59_000, false)
        assertFalse(time.takeSubmission(120))
        time.update(100_000, true) // A pause contributes no listening time.
        time.update(101_000, false) // Pausing or ending the last song still submits it.
        assertTrue(time.takeSubmission(120))
        assertFalse(time.takeSubmission(120))
        time.update(110_000, true)
        time.update(120_000, false)
        assertFalse(time.takeSubmission(120))
        val next = ListeningTime()
        next.update(0, true)
        next.update(240_000, false)
        assertTrue(next.takeSubmission(900)) // Long tracks qualify after four minutes.
    }
    @Test fun countsOnlyPlayingIntervalsAndIgnoresClockReversal() {
        val time = ListeningTime()
        time.update(1_000, true)
        time.update(3_000, false)
        time.update(20_000, false)
        time.update(30_000, true)
        time.update(31_000, true)
        time.update(30_500, false)
        assertEquals(3_000L, time.listenedMs)
    }
}
