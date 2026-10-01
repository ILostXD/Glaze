package com.glaze


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

    @Test fun substantialCoverColorWinsButTinyAccentDoesNot() {
        val black = Color(0xFF101010)
        val teal = Color(0xFF174750)
        val red = Color(0xFFDA2727)
        assertEquals(teal, artworkBackdropColor(black, red,
            listOf(black to 55, teal to 40, red to 5)))
        assertEquals(black, artworkBackdropColor(black, red,
            listOf(black to 95, red to 5)))
    }
}
