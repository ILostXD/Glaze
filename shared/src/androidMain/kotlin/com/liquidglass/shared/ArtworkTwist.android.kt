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
    uniform float time;

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
        float2 position = (point - center) / radius;
        float diagonal = sin((position.x + position.y) * 5.2 + time * 0.31);
        float2 flow = radius * float2(
            0.14 * sin(position.y * 5.1 + time * 0.37) + 0.08 * diagonal,
            0.14 * sin(position.x * 4.5 - time * 0.29) - 0.08 * diagonal
        );
        float edge = min(min(point.x, point.y),
            min(center.x * 2.0 - point.x, center.y * 2.0 - point.y));
        point += flow * smoothstep(0.0, radius * 0.22, edge);
        float2 warped = twist(point, center, radius, angle);
        warped = twist(warped, center + float2(-radius * 0.22, radius * 0.18),
            radius * 0.82, angle * 0.95);
        return content.eval(warped);
    }
"""

@Composable
internal actual fun Modifier.artworkTwist(seconds: Float): Modifier {
    if (Build.VERSION.SDK_INT < 33) return this
    val shader = remember { RuntimeShader(TWIST_SHADER) }
    val effect = remember(shader) {
        AndroidRenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
    }
    return this.graphicsLayer {
        shader.setFloatUniform("center", size.width / 2f, size.height / 2f)
        shader.setFloatUniform("radius", size.width * 0.9f)
        shader.setFloatUniform("angle", -4.35f)
        shader.setFloatUniform("time", seconds)
        renderEffect = effect
    }
}
