package com.glaze.shared

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertTrue

class ArtworkAccentTest {
    @Test fun accentContrastsAgainstControlBackground() {
        val background = Color(0.40f, 0.40f, 0.40f).luminance()
        for (red in 0..5) for (green in 0..5) for (blue in 0..5) {
            val accent = artworkAccent(Color(red / 5f, green / 5f, blue / 5f))
            assertTrue((accent.luminance() + 0.05f) / (background + 0.05f) >= 3f)
        }
    }

    @Test fun coloredArtworkKeepsAVisibleHue() {
        val accent = artworkAccent(Color(0.1f, 0.25f, 0.85f))
        assertTrue(accent.blue > accent.red + 0.15f)
    }
}
