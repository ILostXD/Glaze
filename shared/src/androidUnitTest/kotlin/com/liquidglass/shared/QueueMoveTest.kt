package com.liquidglass.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class QueueMoveTest {
    @Test fun playNextWorksFromEitherSideOfCurrentTrack() {
        assertEquals(2, playNextQueueIndex(sourceIndex = 4, currentIndex = 1))
        assertEquals(2, playNextQueueIndex(sourceIndex = 0, currentIndex = 2))
    }

    @Test fun dragPreviewMakesRoomBeforeDrop() {
        val order = listOf(0, 1, 2, 3)
        assertEquals(listOf(0, 1, 3, 2), queuePreviewMoved(order, 2, 3))
        assertEquals(listOf(0, 2, 1, 3), queuePreviewMoved(order, 2, 1))
        assertEquals(listOf(0, 1, 2, 3), order)
    }
}
