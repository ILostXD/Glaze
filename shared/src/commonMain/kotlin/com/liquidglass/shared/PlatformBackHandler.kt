package com.liquidglass.shared

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

@Composable
internal expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

@Composable
internal expect fun PlatformPredictiveBackHandler(
    enabled: Boolean, onBack: suspend (Flow<BackGestureEvent>) -> Unit,
)
