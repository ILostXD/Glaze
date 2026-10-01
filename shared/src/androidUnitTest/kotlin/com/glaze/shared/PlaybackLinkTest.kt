package com.glaze.shared

import kotlin.test.*

class PlaybackLinkTest {
    @Test fun playbackLinksRoundTripAndRejectAmbiguousOrExternalTargets() {
        for (kind in listOf("song", "album", "playlist")) {
            assertEquals(PlaybackRequest(kind, "nav-id", true, false),
                parsePlaybackLink(playbackLink(kind, "nav-id", shuffle = true, openNowPlaying = false)))
            assertEquals(PlaybackRequest(kind, "nav-id"), parsePlaybackLink("glaze://play/$kind/nav-id"))
        }
        for (link in listOf("https://play/playlist/id", "glaze://jam/playlist/id", "glaze://play/artist/id",
            "glaze://play/playlist", "glaze://play/playlist/%2Fetc", "glaze://owner:secret@play/playlist/id",
            "glaze://play/playlist/id?shuffle=yes", "glaze://play/playlist/id?shuffle=true&shuffle=false",
            "glaze://play/playlist/id?server=https://other.test", "glaze://play/playlist/id#extra")) {
            assertNull(parsePlaybackLink(link), link)
        }
        assertFailsWith<IllegalArgumentException> { playbackRequest("playlist", "") }
    }
}
