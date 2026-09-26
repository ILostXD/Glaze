package com.liquidglass.shared

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollectionBackdropTest {
    @Test fun nearBlackArtworkKeepsBlackBackgroundAndCharcoalGlow() {
        val (base, glow) = collectionBackdropColors(Color(0xFF101010), darkMode = true)
        assertEquals(Color(0xFF080808), base)
        assertEquals(Color(0xFF292929), glow)
        val (lightBase, _) = collectionBackdropColors(Color(0xFF101010), darkMode = false)
        assertTrue(lightBase.red > 0.75f)
    }

    @Test fun darkTintRetainsItsHueAndAlwaysHasLighterGlow() {
        val (base, glow) = collectionBackdropColors(Color(0xFF351422), darkMode = true)
        assertTrue(base.red > base.green)
        assertTrue(base.red > base.blue)
        assertTrue(base.red > 0.08f)
        assertTrue(glow.red > base.red)
        assertTrue(glow.green > base.green)
        assertTrue(glow.blue > base.blue)
    }
}
