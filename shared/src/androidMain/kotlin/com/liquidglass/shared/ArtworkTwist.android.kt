package com.liquidglass.shared

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Portrait composition: side/viewport-width, anchor x/y, initial rotation (radians).
// All fragments drift on independent paths; the smaller two also orbit their anchors.
private val layerLayouts = arrayOf(
    floatArrayOf(2.80f, 0.40f, 0.68f, 0.80f),
    floatArrayOf(1.60f, 0.15f, -0.16f, -0.90f),
    floatArrayOf(1.25f, 0.98f, 0.32f, -0.45f),
    floatArrayOf(1.05f, -0.08f, 0.92f, 1.35f),
)
// Each copy has its own offset x/y, radius and angle in artwork coordinates.
private val layerTwists = arrayOf(
    floatArrayOf(0.50f, 0.50f, 1.20f, 1.15f),
    floatArrayOf(0.18f, 0.72f, 1.10f, -1.65f),
    floatArrayOf(0.76f, 0.30f, 1.00f, 2.10f),
    floatArrayOf(0.30f, 0.75f, 0.80f, -2.50f),
)
private val spinSpeeds = floatArrayOf(0.09f, -0.24f, -0.18f, 0.12f)
private val orbitPhases = floatArrayOf(0.40f, 2.40f)
private const val ORBIT_RADIUS = 0.25f
private const val ORBIT_SPEED = 0.75f
private const val DRIFT_X = 0.18f // Viewport-width amplitude.
private const val DRIFT_Y = 0.10f // Viewport-height amplitude.
private const val DRIFT_SPEED_X = 0.27f
private const val DRIFT_SPEED_Y = 0.19f
private const val SATURATION = 1.35f
private const val BLUR_WIDTH = 0.085f // Set to zero to inspect the pre-blur composite.

private const val ARTWORK_SHADER = """
    uniform shader content;
    uniform float2 resolution;
    uniform float time;
    uniform float4 layout0;
    uniform float4 layout1;
    uniform float4 layout2;
    uniform float4 layout3;
    uniform float4 twist0;
    uniform float4 twist1;
    uniform float4 twist2;
    uniform float4 twist3;
    uniform float2 orbitPhases;
    uniform float4 speeds;
    uniform float orbitRadius;
    uniform float orbitSpeed;
    uniform float saturation;
    uniform float2 driftAmplitude;
    uniform float2 driftSpeed;

    float2 rotate(float2 point, float angle) {
        float s = sin(angle), c = cos(angle);
        return float2(point.x * c - point.y * s, point.x * s + point.y * c);
    }

    float2 twist(float2 coord, float4 distortion) {
        float2 offset = distortion.xy;
        float twistRadius = distortion.z;
        float twistAngle = distortion.w;
        coord -= offset;
        float dist = length(coord);
        if (dist < twistRadius) {
            float ratio = (twistRadius - dist) / twistRadius;
            coord = rotate(coord, ratio * ratio * twistAngle);
        }
        return coord + offset;
    }

    float2 drift(float phase) {
        // Different frequencies give each fragment a two-dimensional, non-circular path.
        // Subtract the initial offset to preserve the composed starting arrangement.
        float2 phases = float2(phase, phase * 0.7);
        return resolution * driftAmplitude * (sin(time * driftSpeed + phases) - sin(phases));
    }

    float4 evolvingTwist(float4 distortion, float phase) {
        // Move the pivot as well as bending harder: otherwise the same recognizable
        // face just rotates intact. Both changes freeze with the playback clock.
        distortion.xy += float2(0.16 * sin(time * 0.23 + phase),
                                0.12 * cos(time * 0.17 + phase));
        distortion.w *= 2.4 + 0.8 * sin(time * 0.21 + phase);
        return distortion;
    }

    half4 artworkCopy(float2 point, float fraction, float spin, float2 center, float4 distortion) {
        // Inverse rotation into square coordinates, then per-copy distortion.
        // Mask AFTER twisting so the silhouette bends without square clipping.
        float side = resolution.x * fraction;
        float2 uv = twist(rotate(point - center, -spin) / side + 0.5, distortion);
        float edge = min(min(uv.x, uv.y), min(1.0 - uv.x, 1.0 - uv.y));
        float coverage = smoothstep(0.0, 1.5 / side, edge);
        // Out-of-bounds artwork stays outside the viewport; never stretch its edges.
        float2 samplePoint = clamp(uv * resolution, float2(0.5), resolution - 0.5);
        half4 color = content.eval(samplePoint);
        half luma = dot(color.rgb, half3(0.2126, 0.7152, 0.0722));
        color.rgb = clamp(mix(half3(luma), color.rgb, half(saturation)), half3(0.0), half3(color.a));
        return color * half(coverage);
    }

    half4 over(half4 top, half4 bottom) {
        return top + bottom * (1.0 - top.a);
    }

    half4 main(float2 point) {
        float4 spin = time * speeds + float4(layout0.w, layout1.w, layout2.w, layout3.w);
        float2 phases = time * speeds.zw * orbitSpeed + orbitPhases;
        float2 orbit2 = float2(cos(phases.x), sin(phases.x));
        float2 orbit3 = float2(cos(phases.y), sin(phases.y));
        half4 result = artworkCopy(point, layout0.x, spin.x, resolution * layout0.yz + drift(0.0), evolvingTwist(twist0, 0.0));
        result = over(artworkCopy(point, layout1.x, spin.y, resolution * layout1.yz + drift(1.7), evolvingTwist(twist1, 1.7)), result);
        result = over(artworkCopy(point, layout2.x, spin.z,
            resolution * layout2.yz + drift(3.4) + resolution.x * orbitRadius * orbit2, evolvingTwist(twist2, 3.4)), result);
        result = over(artworkCopy(point, layout3.x, spin.w,
            resolution * layout3.yz + drift(5.1) + resolution.x * orbitRadius * orbit3, evolvingTwist(twist3, 5.1)), result);
        return result;
    }
"""

// Dither after upscaling, in screen pixels: noise in the small blur surface would
// itself become visible 4x4 blocks. Fixed spatial noise also stays still on pause.
private const val DITHER_SHADER = """
    uniform shader content;
    half4 main(float2 point) {
        float4 color = float4(content.eval(point));
        float noise = fract(52.9829189 * fract(dot(floor(point), float2(0.06711056, 0.00583715)))) - 0.5;
        // +/- one 8-bit level also breaks up values rounded by earlier passes.
        color.rgb = clamp(color.rgb + noise * color.a * (2.0 / 255.0), float3(0.0), float3(color.a));
        return half4(color);
    }
"""

@Composable
internal actual fun FluidArtworkSurface(
    artUrl: String,
    seconds: () -> Float,
    onArtworkReady: () -> Unit,
    modifier: Modifier,
) {
    if (Build.VERSION.SDK_INT >= 33) {
        ShaderArtwork(artUrl, seconds, onArtworkReady, modifier)
    } else {
        FallbackArtwork(artUrl, seconds, onArtworkReady, modifier)
    }
}

@RequiresApi(33)
@Composable
private fun ShaderArtwork(
    artUrl: String, seconds: () -> Float, onArtworkReady: () -> Unit, modifier: Modifier,
) {
    val shader = remember {
        RuntimeShader(ARTWORK_SHADER).apply {
            layerLayouts.forEachIndexed { i, layout -> setFloatUniform("layout$i", layout) }
            layerTwists.forEachIndexed { i, twist -> setFloatUniform("twist$i", twist) }
            setFloatUniform("orbitPhases", orbitPhases)
            setFloatUniform("speeds", spinSpeeds)
            setFloatUniform("orbitRadius", ORBIT_RADIUS)
            setFloatUniform("orbitSpeed", ORBIT_SPEED)
            setFloatUniform("saturation", SATURATION)
            setFloatUniform("driftAmplitude", DRIFT_X, DRIFT_Y)
            setFloatUniform("driftSpeed", DRIFT_SPEED_X, DRIFT_SPEED_Y)
        }
    }
    val dither = remember {
        AndroidRenderEffect.createRuntimeShaderEffect(RuntimeShader(DITHER_SHADER), "content")
            .asComposeRenderEffect()
    }
    val renderScale = if (BLUR_WIDTH > 0f) 4f else 1f
    // Blur discards fine detail. Render this background at quarter resolution,
    // then upscale the finished composite; controls/artwork stay full resolution.
    BoxWithConstraints(modifier.graphicsLayer { renderEffect = dither }) {
        AsyncImage(
            model = artUrl, contentDescription = null,
            // The shader restores square aspect ratios when sampling the four copies.
            // The stretched input texture is never shown directly.
            contentScale = ContentScale.FillBounds,
            onSuccess = { onArtworkReady() },
            modifier = Modifier.requiredSize(maxWidth / renderScale, maxHeight / renderScale)
                .align(Alignment.Center)
                .graphicsLayer { scaleX = renderScale; scaleY = renderScale }
                .graphicsLayer {
                    shader.setFloatUniform("resolution", size.width, size.height)
                    shader.setFloatUniform("time", seconds())
                    var effect = AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
                    if (BLUR_WIDTH > 0f) {
                        // Blur the final twisted composite with a continuous Gaussian.
                        // Sparse, widely spaced Kawase taps left a grid on high-contrast art.
                        // Android maps radius to sigma with radius * 0.57735 + 0.5;
                        // preserve the previous kernel's variance instead of adding haze.
                        val radius = ((size.width * BLUR_WIDTH - 0.5f) / 0.57735f).coerceAtLeast(0.01f)
                        effect = AndroidRenderEffect.createBlurEffect(
                            radius, radius, effect, Shader.TileMode.CLAMP,
                        )
                    }
                    renderEffect = effect.asComposeRenderEffect()
                },
        )
    }
}

@Composable
private fun FallbackArtwork(
    artUrl: String, seconds: () -> Float, onArtworkReady: () -> Unit, modifier: Modifier,
) {
    // Degraded below API 33: no per-pixel twist. Compose blur also needs API 31.
    val saturation = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(SATURATION) }) }
    BoxWithConstraints(modifier) {
        val width = maxWidth
        val height = maxHeight
        Box(Modifier.fillMaxSize().blur(width * BLUR_WIDTH)) {
            layerLayouts.forEachIndexed { index, layout ->
                AsyncImage(
                    model = artUrl, contentDescription = null, contentScale = ContentScale.Crop,
                    colorFilter = saturation, onSuccess = { onArtworkReady() },
                    modifier = Modifier.requiredSize(width * layout[0]).align(Alignment.Center).graphicsLayer {
                        val time = seconds()
                        val spin = time * spinSpeeds[index] + layout[3]
                        val phase = index * 1.7f
                        rotationZ = spin * (180f / PI.toFloat())
                        translationX = (width * (layout[1] - 0.5f)).toPx()
                        translationY = (height * (layout[2] - 0.5f)).toPx()
                        translationX += (width * DRIFT_X *
                            (sin(time * DRIFT_SPEED_X + phase) - sin(phase))).toPx()
                        translationY += (height * DRIFT_Y *
                            (sin(time * DRIFT_SPEED_Y + phase * 0.7f) - sin(phase * 0.7f))).toPx()
                        if (index >= 2) {
                            val orbit = time * spinSpeeds[index] * ORBIT_SPEED + orbitPhases[index - 2]
                            translationX += (width * ORBIT_RADIUS * cos(orbit)).toPx()
                            translationY += (width * ORBIT_RADIUS * sin(orbit)).toPx()
                        }
                    },
                )
            }
        }
    }
}
