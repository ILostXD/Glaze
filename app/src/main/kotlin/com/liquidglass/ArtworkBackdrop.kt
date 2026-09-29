package com.liquidglass

import androidx.compose.ui.graphics.Color

internal fun artworkBackdropColor(
    dominant: Color?, accent: Color, swatches: List<Pair<Color, Int>> = emptyList(),
): Color {
    val total = swatches.sumOf { it.second }
    return swatches.asSequence()
        .filter { (color, population) ->
            population * 10 >= total &&
                maxOf(color.red, color.green, color.blue) > 0.14f &&
                maxOf(color.red, color.green, color.blue) -
                    minOf(color.red, color.green, color.blue) > 0.045f
        }
        .maxByOrNull { it.second }?.first ?: dominant ?: accent
}
