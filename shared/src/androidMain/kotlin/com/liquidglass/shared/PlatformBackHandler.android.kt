package com.liquidglass.shared

import androidx.activity.compose.BackHandler
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
internal actual fun PlatformPredictiveBackHandler(
    enabled: Boolean, onBack: suspend (Flow<BackGestureEvent>) -> Unit,
) {
    PredictiveBackHandler(enabled = enabled) { events ->
        onBack(events.map { BackGestureEvent(it.progress, it.swipeEdge == BackEventCompat.EDGE_LEFT) })
    }
}
