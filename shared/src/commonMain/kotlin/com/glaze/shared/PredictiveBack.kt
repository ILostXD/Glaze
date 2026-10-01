package com.glaze.shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal data class BackGestureEvent(val progress: Float, val fromLeft: Boolean)

internal fun backCardAlpha(progress: Float): Float =
    if (progress >= 1f) 0f else 1f - ((progress - 0.72f) / 0.28f).coerceIn(0f, 1f)

internal suspend fun consumeBackGesture(
    events: Flow<BackGestureEvent>,
    onProgress: suspend (BackGestureEvent) -> Unit,
    onComplete: suspend () -> Unit,
    onCancel: () -> Unit,
) {
    try {
        events.collect { onProgress(it.copy(progress = it.progress.coerceIn(0f, 1f))) }
        onComplete()
    } catch (cancelled: CancellationException) {
        onCancel()
        throw cancelled
    }
}

/** Uses the page as last seen; a canceled gesture never changes the back stack. */
@Composable
internal fun PredictiveBackContent(
    previous: ImageBitmap?, enabled: Boolean, destinationReady: Boolean, onBack: () -> Unit,
    captureLayer: GraphicsLayer, content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    val handoffAlpha = remember { Animatable(0f) }
    val settle = remember { spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh) }
    val scope = rememberCoroutineScope()
    var fromLeft by remember { mutableStateOf(true) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    var handoffJob by remember { mutableStateOf<Job?>(null) }
    var handoffPreview by remember { mutableStateOf<ImageBitmap?>(null) }
    val latestPrevious by rememberUpdatedState(previous)
    val latestOnBack by rememberUpdatedState(onBack)
    val latestDestinationReady by rememberUpdatedState(destinationReady)

    LaunchedEffect(previous) {
        settleJob?.cancelAndJoin()
        progress.snapTo(0f)
    }
    // Register before content so lyrics and modal sheets retain back-handler priority.
    PlatformPredictiveBackHandler(enabled && previous != null) { events ->
        val destination = latestPrevious
        settleJob?.cancelAndJoin()
        handoffJob?.cancelAndJoin()
        handoffPreview = null
        progress.snapTo(0f)
        consumeBackGesture(events,
            onProgress = { event ->
                if (latestPrevious !== destination)
                    throw CancellationException("Navigation changed during back gesture")
                fromLeft = event.fromLeft
                progress.snapTo(event.progress)
            },
            onComplete = {
                if (destination != null && latestPrevious === destination) {
                    progress.animateTo(1f, settle)
                    handoffPreview = destination
                    handoffAlpha.snapTo(1f)
                    latestOnBack()
                    handoffJob = scope.launch {
                        withFrameNanos { }
                        snapshotFlow { latestDestinationReady }.first { it }
                        withFrameNanos { }
                        handoffAlpha.animateTo(0f, tween(90))
                        handoffPreview = null
                    }
                }
            },
            onCancel = {
                settleJob = scope.launch {
                    progress.animateTo(0f, settle)
                }
            },
        )
    }

    Box(Modifier.fillMaxSize()) {
        if (previous != null && progress.value > 0f) {
            Image(previous, null, Modifier.fillMaxSize().graphicsLayer {
                val scale = 0.96f + progress.value * 0.04f
                scaleX = scale
                scaleY = scale
            }, contentScale = ContentScale.FillBounds)
        }
        Box(Modifier.fillMaxSize()
            .drawWithContent {
                // The page is already captured at rest; re-recording the whole UI on every
                // gesture frame makes the back preview visibly stutter on slower devices.
                if (progress.value == 0f)
                    captureLayer.record { this@drawWithContent.drawContent() }
                drawLayer(captureLayer)
            }
            .graphicsLayer {
                val fraction = progress.value
                scaleX = 1f - 0.08f * fraction
                scaleY = scaleX
                translationX = (if (fromLeft) 1f else -1f) * size.width * 0.04f * fraction
                alpha = backCardAlpha(fraction)
                shape = RoundedCornerShape(28.dp * fraction)
                clip = fraction > 0f
            }.background(MaterialTheme.colorScheme.background)) { content() }
        handoffPreview?.let { snapshot ->
            Image(snapshot, null, Modifier.fillMaxSize().graphicsLayer {
                alpha = handoffAlpha.value
            }, contentScale = ContentScale.FillBounds)
        }
    }
}
