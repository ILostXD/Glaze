package com.liquidglass

import androidx.compose.ui.graphics.Color

internal fun neutralArtworkBackdrop(pixels: IntArray): Color? {
    if (pixels.isEmpty()) return null
    var dark = 0
    var light = 0
    for (pixel in pixels) {
        val red = pixel ushr 16 and 0xff
        val green = pixel ushr 8 and 0xff
        val blue = pixel and 0xff
        if (red < 64 && green < 64 && blue < 64) dark++
        if (red > 220 && green > 220 && blue > 220) light++
    }
    return when {
        dark * 100 >= pixels.size * 55 -> Color(0xFF101010)
        light * 100 >= pixels.size * 55 -> Color(0xFF454545)
        else -> null
    }
}

internal fun artworkBackdropColor(pixels: IntArray, dominant: Color?, accent: Color): Color =
    neutralArtworkBackdrop(pixels) ?: dominant ?: accent
