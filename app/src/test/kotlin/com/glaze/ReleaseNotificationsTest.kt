package com.glaze

import com.liquidglass.releaseNotificationEvent

import com.glaze.shared.UpcomingAlbum
import org.junit.Assert.*
import org.junit.Test

class ReleaseNotificationsTest {
    @Test fun onlyFollowedUpcomingAlbumsAndCompletedPresavesNotify() {
        val album = UpcomingAlbum("42", "nav", "Album", "Artist", "2026-10-02", 2000,
            "", 14, emptyList(), false, "", "", "", followed = true)
        assertEquals("42:announced", releaseNotificationEvent(album, 1000))
        assertNull(releaseNotificationEvent(album.copy(followed = false), 1000))
        assertNull(releaseNotificationEvent(album, 2000))
        assertNull(releaseNotificationEvent(album.copy(saved = true, status = "downloading", followed = false), 3000))
        assertEquals("42:added", releaseNotificationEvent(album.copy(saved = true, status = "rescanned", followed = false), 3000))
        assertNull(releaseNotificationEvent(album.copy(status = "rescanned", followed = false), 3000))
    }
}
