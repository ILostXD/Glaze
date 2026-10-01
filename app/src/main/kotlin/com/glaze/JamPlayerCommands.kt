package com.glaze


import androidx.media3.common.Player
import com.glaze.shared.JamViewState

internal fun jamPlayerCommands(commands: Player.Commands, jam: JamViewState): Player.Commands {
    if (jam.sessionId.isEmpty()) return commands
    val candidates = List(commands.size()) { commands[it] } + listOf(
        Player.COMMAND_PLAY_PAUSE, Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
    return Player.Commands.Builder().addAll(*candidates.distinct().filter {
        jamCommandAllowed(it, commands.contains(it), jam)
    }.toIntArray()).build()
}

internal fun jamCommandAllowed(command: Int, nativeAvailable: Boolean, jam: JamViewState): Boolean {
    if (jam.sessionId.isEmpty()) return nativeAvailable
    if (command in setOf(Player.COMMAND_SET_SPEED_AND_PITCH, Player.COMMAND_SET_REPEAT_MODE,
        Player.COMMAND_SET_SHUFFLE_MODE)) return false
    if (!jam.isHost && command in setOf(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
        Player.COMMAND_SEEK_TO_MEDIA_ITEM, Player.COMMAND_SEEK_TO_PREVIOUS,
        Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM, Player.COMMAND_SEEK_FORWARD,
        Player.COMMAND_SEEK_BACK, Player.COMMAND_CHANGE_MEDIA_ITEMS,
        Player.COMMAND_SET_MEDIA_ITEM, Player.COMMAND_STOP)) return false
    return when (command) {
        Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM ->
            (jam.isHost || jam.guestPlayback) && jam.queue.isNotEmpty()
        Player.COMMAND_PLAY_PAUSE -> jam.isHost || jam.guestPlayback
        else -> nativeAvailable
    }
}
