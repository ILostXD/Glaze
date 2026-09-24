package com.liquidglass.shared

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

private const val TWIST_SHADER = """
    uniform shader content;
    uniform float2 center;
    uniform float radius;
    uniform float angle;

    float2 twist(float2 point, float2 pivot, float reach, float strength) {
        float2 relative = point - pivot;
        float distance = length(relative);
        if (distance < reach) {
            float ratio = (reach - distance) / reach;
            float rotation = ratio * ratio * strength;
            float sine = sin(rotation);
            float cosine = cos(rotation);
            relative = float2(
                relative.x * cosine - relative.y * sine,
                relative.x * sine + relative.y * cosine
            );
        }
        return pivot + relative;
    }

    half4 main(float2 point) {
        float2 warped = twist(point, center, radius, angle);
        warped = twist(warped, center + float2(-radius * 0.22, radius * 0.18),
            radius * 0.82, angle * 1.12);
        return content.eval(warped);
    }
"""

@Composable
internal actual fun Modifier.artworkTwist(): Modifier {
    if (Build.VERSION.SDK_INT < 33) return this
    val shader = remember { RuntimeShader(TWIST_SHADER) }
    val effect = remember(shader) {
        AndroidRenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
    }
    return this.graphicsLayer {
        shader.setFloatUniform("center", size.width / 2f, size.height / 2f)
        shader.setFloatUniform("radius", size.width * 0.9f)
        shader.setFloatUniform("angle", -5.8f)
        renderEffect = effect
    }
}
