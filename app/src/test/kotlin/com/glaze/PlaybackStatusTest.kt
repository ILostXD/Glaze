package com.glaze


import androidx.media3.common.Player
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackStatusTest {
    @Test fun bufferingOnlyWhileTryingToPlay() {
        assertTrue(shouldShowBuffering(true, 1, Player.STATE_BUFFERING))
        assertTrue(shouldShowBuffering(true, 1, Player.STATE_IDLE))
        assertFalse(shouldShowBuffering(false, 1, Player.STATE_BUFFERING))
        assertFalse(shouldShowBuffering(true, 0, Player.STATE_BUFFERING))
        assertFalse(shouldShowBuffering(true, 1, Player.STATE_READY))
    }
}
