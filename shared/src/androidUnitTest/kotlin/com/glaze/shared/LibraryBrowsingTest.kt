package com.glaze.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryBrowsingTest {
    @Test fun topSongSortKeepsTheServerRankingUnlessAnotherOrderIsChosen() {
        val songs = listOf(Song("z", "Zulu", "Artist", "Album", playCount = 2),
            Song("a", "alpha", "Artist", "Album", playCount = 10),
            Song("b", "Bravo", "Artist", "Album", playCount = 2))
        assertEquals(songs, sortSongs(songs, SongSort.Top))
        assertEquals(listOf("a", "b", "z"), sortSongs(songs, SongSort.Name).map { it.id })
        assertEquals(listOf("z", "b", "a"), sortSongs(songs, SongSort.NameReverse).map { it.id })
        assertEquals(listOf("a", "b", "z"), sortSongs(songs, SongSort.MostPlayed).map { it.id })
    }

    @Test fun albumSortsPreserveLibraryOrderAndKeepUnknownDatesLast() {
        val albums = listOf(
            Album("unknown", "Beta", "Artist"),
            Album("new", "zeta", "Artist", releaseOrder = 20260101),
            Album("old", "Alpha", "Artist", releaseOrder = 20200101),
        )
        assertEquals(albums, sortAlbums(albums, AlbumSort.Library))
        assertEquals(listOf("old", "unknown", "new"), sortAlbums(albums, AlbumSort.Name).map { it.id })
        assertEquals(listOf("new", "unknown", "old"), sortAlbums(albums, AlbumSort.NameReverse).map { it.id })
        assertEquals(listOf("new", "old", "unknown"), sortAlbums(albums, AlbumSort.Newest).map { it.id })
        assertEquals(listOf("old", "new", "unknown"), sortAlbums(albums, AlbumSort.Oldest).map { it.id })
    }

    @Test fun playlistSortsAlwaysKeepFavoritesFirstAndSortWithinEachGroup() {
        val playlists = listOf(Playlist("a", "Alpha", 100), Playlist("c", "Charlie", 30),
            Playlist("b", "bravo", 10), Playlist("d", "Delta", 20))
        val favorite: (Playlist) -> Boolean = { it.id == "b" || it.id == "d" }
        assertEquals(listOf("b", "d", "a", "c"), sortPlaylists(playlists, PlaylistSort.Name, favorite).map { it.id })
        assertEquals(listOf("d", "b", "c", "a"), sortPlaylists(playlists, PlaylistSort.NameReverse, favorite).map { it.id })
        assertEquals(listOf("d", "b", "a", "c"), sortPlaylists(playlists, PlaylistSort.MostSongs, favorite).map { it.id })
        assertEquals(listOf("b", "d", "c", "a"), sortPlaylists(playlists, PlaylistSort.FewestSongs, favorite).map { it.id })
        assertEquals(listOf("b", "d", "a", "c"), sortPlaylists(playlists, PlaylistSort.Library, favorite).map { it.id })
        assertEquals(listOf("a", "b", "c", "d"), sortPlaylists(playlists, PlaylistSort.Name) { false }.map { it.id })
    }

    @Test fun chromeHeightsFollowTheSameSizeAndLabelSettingsAsTheControls() {
        assertEquals(62f, miniPlayerHeight(MiniPlayerSize.Medium).value)
        assertEquals(75f, navigationHeight(AppSettings()).value)
        assertEquals(55f, navigationHeight(AppSettings(navigationSize = NavigationSize.Small,
            navigationLabels = false)).value)
        assertEquals(85f, navigationHeight(AppSettings(navigationSize = NavigationSize.Large)).value)
    }
}
