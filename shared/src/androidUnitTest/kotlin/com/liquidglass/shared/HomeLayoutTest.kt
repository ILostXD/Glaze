package com.liquidglass.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeLayoutTest {
    @Test fun homeLayoutRoundTripsAndMovesOnlyVisibleSections() {
        val layout = listOf(HomeShelf.Playlists, HomeShelf.NewLibrary, HomeShelf.Recent)
        assertEquals(layout, parseHomeSections(layout.joinToString(",") { it.name }))
        assertEquals(listOf(HomeShelf.NewLibrary, HomeShelf.Playlists, HomeShelf.Recent),
            moveHomeSection(layout, HomeShelf.Playlists, 1))
        assertEquals(layout, moveHomeSection(layout, HomeShelf.Playlists, -1))
        assertEquals(layout, moveHomeSection(layout, HomeShelf.Mixes, 1))
        assertEquals(HomeShelf.entries, parseHomeSections(null))
        assertEquals(HomeShelf.entries, parseHomeSections("Unknown"))
        assertEquals(emptyList(), parseHomeSections(""))
        assertEquals(listOf(HomeShelf.Recent), parseHomeSections("Recent,Unknown,Recent"))
    }

    @Test fun hiddenSectionsKeepTheirPlaceWhenRestored() {
        val (order, hidden) = restoreHomeLayout("Playlists,Recent", null)
        assertEquals(listOf(HomeShelf.Playlists, HomeShelf.Recent, HomeShelf.Mixes, HomeShelf.NewLibrary), order)
        assertEquals(setOf(HomeShelf.Mixes, HomeShelf.NewLibrary), hidden)
        assertEquals(order to hidden,
            restoreHomeLayout(order.joinToString(",") { it.name }, hidden.joinToString(",") { it.name }))
        assertEquals(order, AppSettings(homeSections = order, hiddenHomeSections = hidden)
            .copy(hiddenHomeSections = hidden - HomeShelf.Mixes).homeSections)
    }
}
