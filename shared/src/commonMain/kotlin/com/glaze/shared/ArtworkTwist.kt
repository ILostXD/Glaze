package com.glaze.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun FluidArtworkSurface(
    artUrl: String,
    seconds: () -> Float,
    onArtworkReady: () -> Unit,
    modifier: Modifier = Modifier,
)
