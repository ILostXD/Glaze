package com.liquidglass.shared

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PredictiveBackTest {
    @Test fun completedGestureNavigatesOnceAndClampsProgress() = runBlocking {
        val received = mutableListOf<Float>()
        var navigations = 0
        var cancellations = 0
        consumeBackGesture(flowOf(BackGestureEvent(-1f, true), BackGestureEvent(2f, false)),
            onProgress = { received += it.progress },
            onComplete = { navigations++ },
            onCancel = { cancellations++ })
        assertEquals(listOf(0f, 1f), received)
        assertEquals(1, navigations)
        assertEquals(0, cancellations)
    }

    @Test fun cancelledGestureNeverNavigates() = runBlocking {
        var navigations = 0
        var cancellations = 0
        assertFailsWith<CancellationException> {
            consumeBackGesture(flow {
                emit(BackGestureEvent(0.4f, true))
                throw CancellationException("Gesture cancelled")
            }, onProgress = {}, onComplete = { navigations++ }, onCancel = { cancellations++ })
        }
        assertEquals(0, navigations)
        assertEquals(1, cancellations)
    }

    @Test fun currentPageFadesOnlyAtEndOfBackGesture() {
        assertEquals(1f, backCardAlpha(0.5f))
        assertEquals(0.5f, backCardAlpha(0.86f), 0.001f)
        assertEquals(0f, backCardAlpha(1f))
    }
}
