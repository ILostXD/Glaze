package com.glaze.shared

import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

/** A complete permutation: every input song appears exactly once. */
fun shuffleSongs(
    songs: List<Song>,
    recentSongIds: Set<String> = emptySet(),
    smart: Boolean = true,
    random: Random = Random.Default,
): List<Song> {
    if (!smart) return songs.shuffled(random) // Kotlin's Fisher-Yates shuffle.
    val addedDays = songs.mapNotNull { it.created?.take(10) }.sortedDescending()
    val newestDay = addedDays.getOrNull((addedDays.size - 1) / 4)
    return songs.map { song ->
        var weight = 1.0 / sqrt(1.0 + song.playCount.coerceAtLeast(0))
        if (newestDay != null && song.created?.take(10)?.let { it >= newestDay } == true) weight *= 1.6
        if (song.id in recentSongIds) weight *= 0.02
        // Exponential race samples a weighted permutation without replacement.
        val key = -ln(random.nextDouble().coerceAtLeast(Double.MIN_VALUE)) / weight
        key to song
    }.sortedBy { it.first }.map { it.second }
}
