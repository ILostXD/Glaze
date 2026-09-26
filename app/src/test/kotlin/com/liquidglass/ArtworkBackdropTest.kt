package com.liquidglass

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtworkBackdropTest {
    @Test fun dominantColorWinsOverVibrantAccent() {
        assertEquals(Color(0xFF568BAB), artworkBackdropColor(
            Color(0xFF568BAB), Color(0xFFE31518)))
        assertEquals(Color(0xFF101010), artworkBackdropColor(
            Color(0xFF101010), Color(0xFFE31518)))
        assertEquals(Color(0xFFE31518), artworkBackdropColor(null, Color(0xFFE31518)))
    }
}
