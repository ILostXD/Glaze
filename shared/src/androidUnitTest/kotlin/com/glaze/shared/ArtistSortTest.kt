package com.glaze.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class ArtistSortTest {
    @Test fun ordersArtistsByNameAndAlbumCount() {
        val artists = listOf(Artist("b", "Beta", albumCount = 2),
            Artist("a", "alpha", albumCount = 5), Artist("c", "Charlie", albumCount = 2))
        assertEquals(listOf("a", "b", "c"), sortArtists(artists, ArtistSort.Name).map { it.id })
        assertEquals(listOf("c", "b", "a"), sortArtists(artists, ArtistSort.NameReverse).map { it.id })
        assertEquals(listOf("a", "b", "c"), sortArtists(artists, ArtistSort.MostAlbums).map { it.id })
        assertEquals(listOf("b", "c", "a"), sortArtists(artists, ArtistSort.FewestAlbums).map { it.id })
    }
}
