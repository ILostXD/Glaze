package com.liquidglass

import androidx.media3.exoplayer.source.ShuffleOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackOrderTest {
    @Test fun playbackFollowsVisibleQueueAfterAddsRemovalsAndMoves() {
        var order: ShuffleOrder = ShuffleOrder.UnshuffledShuffleOrder(8)
        fun checkOrder() {
            val visited = mutableListOf<Int>()
            var index = order.firstIndex
            while (index != -1) {
                visited += index
                index = order.getNextIndex(index)
            }
            assertEquals((0 until order.length).toList(), visited)
        }
        checkOrder()
        order = order.cloneAndInsert(3, 2) // Play next / add to queue.
        checkOrder()
        order = order.cloneAndRemove(1, 2)
        checkOrder()
        order = order.cloneAndRemove(6, 7).cloneAndInsert(3, 1) // Queue reorder.
        checkOrder()
        order = order.cloneAndClear().cloneAndInsert(0, 5) // A new playlist.
        checkOrder()
    }
}
