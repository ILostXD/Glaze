package com.liquidglass.shared

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeliberateSwipeTest {
    @Test fun fastShortFlickDoesNotAct() {
        assertFalse(isDeliberateSwipe(150f, 400f))
        assertFalse(isDeliberateSwipe(-150f, 400f))
        assertFalse(isDeliberateSwipe(0f, 0f))
        assertTrue(isDeliberateSwipe(300f, 400f))
        assertTrue(isDeliberateSwipe(-300f, 400f))
    }

    @Test fun actionRunsOncePerGesture() {
        val latch = SwipeActionLatch()
        assertTrue(latch.take())
        assertFalse(latch.take())
        latch.reset()
        assertTrue(latch.take())
    }
}
