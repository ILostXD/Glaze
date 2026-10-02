package com.glaze


import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.SimpleBasePlayer
import com.glaze.shared.JamViewState

internal fun jamPlayerState(state: SimpleBasePlayer.State, jam: JamViewState,
                            item: MediaItem?, durationMs: Long?, position: () -> Long): SimpleBasePlayer.State {
    val builder = state.buildUpon()
    if (jam.sessionId.isNotEmpty() && !jam.isHost) {
        val playlist = if (item == null) emptyList() else listOf(
            SimpleBasePlayer.MediaItemData.Builder(item.mediaId).setMediaItem(item).setIsSeekable(true)
                .setDurationUs(durationMs?.takeIf { it > 0 }?.times(1_000L) ?: C.TIME_UNSET).build())
        builder.setPlaylist(playlist).setCurrentMediaItemIndex(if (playlist.isEmpty()) C.INDEX_UNSET else 0)
            .setPlayWhenReady(jam.playback?.playing == true && jam.connection == "Connected", Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE)
            .setPlaybackState(if (playlist.isEmpty()) Player.STATE_IDLE else Player.STATE_READY)
            // Local audio may still be preparing, or have failed, while remote metadata is loading.
            .setIsLoading(false).setPlayerError(null)
            .setPlaybackSuppressionReason(Player.PLAYBACK_SUPPRESSION_REASON_NONE)
            .setShuffleModeEnabled(jam.playback?.shuffle == true).setRepeatMode(jam.playback?.repeat ?: 0)
            .setContentPositionMs(SimpleBasePlayer.PositionSupplier { position() })
    }
    return builder.build()
}

internal fun seekJamPrevious(player: Player): Boolean {
    if (player.mediaItemCount == 0) return false
    val current = player.currentMediaItemIndex
    val previous = player.previousMediaItemIndex
    val target = if (previous >= 0 && player.currentPosition <= player.maxSeekToPreviousPosition) previous else current
    // Explicit seeks also work while the host is buffering a new timeline.
    player.seekTo(target, 0L)
    return target != current
}

internal fun jamPlayerCommands(commands: Player.Commands, jam: JamViewState): Player.Commands {
    if (jam.sessionId.isEmpty()) return commands
    val candidates = List(commands.size()) { commands[it] } + listOf(
        Player.COMMAND_PLAY_PAUSE, Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
        Player.COMMAND_SEEK_TO_PREVIOUS, Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
        Player.COMMAND_SET_REPEAT_MODE, Player.COMMAND_SET_SHUFFLE_MODE,
        Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
    return Player.Commands.Builder().addAll(*candidates.distinct().filter {
        jamCommandAllowed(it, commands.contains(it), jam)
    }.toIntArray()).build()
}

internal fun jamCommandAllowed(command: Int, nativeAvailable: Boolean, jam: JamViewState): Boolean {
    if (jam.sessionId.isEmpty()) return nativeAvailable
    if (command == Player.COMMAND_SET_SPEED_AND_PITCH) return false
    if (command in setOf(Player.COMMAND_SEEK_TO_MEDIA_ITEM, Player.COMMAND_SEEK_FORWARD,
        Player.COMMAND_SEEK_BACK, Player.COMMAND_CHANGE_MEDIA_ITEMS,
        Player.COMMAND_SET_MEDIA_ITEM, Player.COMMAND_STOP)) return false
    return when (command) {
        Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM ->
            (jam.isHost || jam.guestPlayback) && (jam.queue.isNotEmpty() || jam.playback?.repeat == Player.REPEAT_MODE_ALL)
        Player.COMMAND_PLAY_PAUSE, Player.COMMAND_SEEK_TO_PREVIOUS,
        Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM, Player.COMMAND_SET_REPEAT_MODE,
        Player.COMMAND_SET_SHUFFLE_MODE, Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM -> jam.isHost || jam.guestPlayback
        else -> nativeAvailable
    }
}
