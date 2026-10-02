package com.glaze


import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.SimpleBasePlayer
import com.glaze.shared.JamQueueEntry
import com.glaze.shared.JamViewState
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

class JamPlayerCommandsTest {
    @Test fun guestReconnectDoesNotInheritLocalLoadingOrPlaybackErrors() {
        val item = MediaItem.Builder().setMediaId("song").build()
        val playlist = listOf(SimpleBasePlayer.MediaItemData.Builder("song").setMediaItem(item).build())
        val buffering = SimpleBasePlayer.State.Builder().setPlaylist(playlist).setCurrentMediaItemIndex(0)
            .setPlaybackState(Player.STATE_BUFFERING).setIsLoading(true).build()
        val guest = JamViewState(sessionId = "jam", memberId = "guest", hostId = "host")
        val reconnecting = jamPlayerState(buffering, guest, null, null) { 0L }
        assertEquals(Player.STATE_IDLE, reconnecting.playbackState)
        assertFalse(reconnecting.isLoading)
        assertTrue(reconnecting.playlist.isEmpty())
        assertTrue(jamPlayerState(buffering, guest.copy(memberId = "host"), null, null) { 0L }.isLoading)
        val failed = SimpleBasePlayer.State.Builder()
            .setPlayerError(object : PlaybackException("Local audio failed", null,
                PlaybackException.ERROR_CODE_IO_UNSPECIFIED, android.os.Bundle.EMPTY, 0L) {}).build()
        val connected = jamPlayerState(failed, guest, item, 180_000L) { 1_000L }
        assertEquals(Player.STATE_READY, connected.playbackState)
        assertNull(connected.playerError)
        assertFalse(connected.isLoading)
    }

    @Test fun previousRestartsOrSeeksIntoHostHistoryEvenWhileBuffering() {
        var position = 15_000L
        var current = 2
        var previous = 1
        var sought: Pair<Int, Long>? = null
        val player = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) { _, method, args ->
            when (method.name) {
                "getMediaItemCount" -> 3
                "getCurrentMediaItemIndex" -> current
                "getPreviousMediaItemIndex" -> previous
                "getCurrentPosition" -> position
                "getMaxSeekToPreviousPosition" -> 3_000L
                "seekTo" -> { sought = (args!![0] as Int) to (args[1] as Long); null }
                else -> error("Unexpected player call: ${method.name}")
            }
        } as Player
        assertFalse(seekJamPrevious(player))
        assertEquals(2 to 0L, sought)
        position = 0
        assertTrue(seekJamPrevious(player))
        assertEquals(1 to 0L, sought)
        current = 0; previous = -1
        assertFalse(seekJamPrevious(player))
        assertEquals(0 to 0L, sought)
    }

    @Test fun guestPermissionsAndNativeSkipFollowTheSharedQueue() {
        val guest = JamViewState(sessionId = "jam", memberId = "guest", hostId = "host",
            queue = listOf(JamQueueEntry("item", "song", "host", emptySet())))
        assertTrue(jamCommandAllowed(Player.COMMAND_PLAY_PAUSE, true, guest))
        assertTrue(jamCommandAllowed(Player.COMMAND_SEEK_TO_NEXT, true, guest))
        assertTrue(jamCommandAllowed(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM, true, guest))
        for (command in listOf(Player.COMMAND_SET_SHUFFLE_MODE, Player.COMMAND_SET_REPEAT_MODE,
            Player.COMMAND_SEEK_TO_PREVIOUS, Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            assertTrue(jamCommandAllowed(command, false, guest))
            assertTrue(jamCommandAllowed(command, false, guest.copy(listenLocally = true)))
            assertFalse(jamCommandAllowed(command, true, guest.copy(guestPlayback = false)))
        }
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
