package com.liquidglass.shared

import kotlin.random.Random

/** Queue positions, rather than song IDs, keep repeated tracks distinct. */
fun shuffledUpcomingIndices(
    songs: List<Song>, currentIndex: Int, smart: Boolean, random: Random = Random.Default,
): List<Int> {
    if (currentIndex !in songs.indices) return emptyList()
    val upcoming = (currentIndex + 1 until songs.size).toList()
    val shuffled = shuffleSongs(upcoming.map { songs[it].copy(id = it.toString()) },
        smart = smart, random = random).map { it.id.toInt() }
    // Toggling shuffle should visibly change an upcoming queue with multiple entries.
    return if (shuffled.size > 1 && shuffled == upcoming) shuffled.drop(1) + shuffled.first() else shuffled
}

/** Restore surviving upcoming occurrences; keep newly added tracks without resurrecting removals. */
fun restoredUpcomingIndices(currentIds: List<String>, originalIds: List<String>): List<Int> {
    val remaining = currentIds.indices.toMutableList()
    val restored = originalIds.mapNotNull { id ->
        remaining.firstOrNull { currentIds[it] == id }?.also { remaining.remove(it) }
    }
    return restored + remaining
}
