package com.liquidglass

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtworkBackdropTest {
    @Test fun blackAndWhiteFieldsOutweighSmallMotifs() {
        val darkCover = IntArray(100) { if (it < 75) 0xFF101010.toInt() else 0xFFCC5511.toInt() }
        val lightCover = IntArray(100) { if (it < 65) 0xFFF8F8F8.toInt() else 0xFF161616.toInt() }
        val mixedCover = IntArray(100) { if (it < 50) 0xFF161616.toInt() else 0xFFCC5511.toInt() }
        assertEquals(Color(0xFF101010), neutralArtworkBackdrop(darkCover))
        assertEquals(Color(0xFF454545), neutralArtworkBackdrop(lightCover))
        assertEquals(null, neutralArtworkBackdrop(mixedCover))
    }

    @Test fun vividDetailDoesNotBecomeTheWholeBackdrop() {
        val pixels = IntArray(100) { if (it < 85) 0xFF568BAB.toInt() else 0xFFE31518.toInt() }
        assertEquals(Color(0xFF568BAB), artworkBackdropColor(pixels,
            Color(0xFF568BAB), Color(0xFFE31518)))
    }
}
