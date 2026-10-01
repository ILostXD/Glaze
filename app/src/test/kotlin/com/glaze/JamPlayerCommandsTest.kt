package com.glaze


import androidx.media3.common.Player
import com.glaze.shared.JamQueueEntry
import com.glaze.shared.JamViewState
import org.junit.Assert.*
import org.junit.Test

class JamPlayerCommandsTest {
    @Test fun guestPermissionsAndNativeSkipFollowTheSharedQueue() {
        val guest = JamViewState(sessionId = "jam", memberId = "guest", hostId = "host",
            queue = listOf(JamQueueEntry("item", "song", "host", emptySet())))
        assertTrue(jamCommandAllowed(Player.COMMAND_PLAY_PAUSE, true, guest))
        assertTrue(jamCommandAllowed(Player.COMMAND_SEEK_TO_NEXT, true, guest))
        assertFalse(jamCommandAllowed(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM, true, guest))
        assertFalse(jamCommandAllowed(Player.COMMAND_CHANGE_MEDIA_ITEMS, true, guest))
        assertFalse(jamCommandAllowed(Player.COMMAND_SET_SPEED_AND_PITCH, true, guest))
        assertFalse(jamCommandAllowed(Player.COMMAND_PLAY_PAUSE, true, guest.copy(guestPlayback = false)))
        assertFalse(jamCommandAllowed(Player.COMMAND_SEEK_TO_NEXT, true, guest.copy(queue = emptyList())))
        assertTrue(jamCommandAllowed(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM, true, guest.copy(memberId = "host")))
        assertTrue(jamCommandAllowed(Player.COMMAND_SET_SPEED_AND_PITCH, true, JamViewState()))
        assertFalse(jamCommandAllowed(Player.COMMAND_SEEK_TO_NEXT, false, JamViewState()))
        assertTrue(jamCommandAllowed(Player.COMMAND_SEEK_TO_NEXT, false, guest))
    }
}
