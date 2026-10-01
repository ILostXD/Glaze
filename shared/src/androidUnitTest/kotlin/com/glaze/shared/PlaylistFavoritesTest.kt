package com.glaze.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PlaylistFavoritesTest {
    @Test fun settingOneFavoriteIsIdempotentAndPreservesOtherFavorites() {
        val account = ServerCredentials("https://music.test", "andy", "secret")
        val one = playlistFavoriteKey(account, "one")
        val two = playlistFavoriteKey(account, "two")
        var favorites = emptySet<String>()
        favorites = favorites.withPlaylistFavorite(one, true)
        favorites = favorites.withPlaylistFavorite(two, true)
        favorites = favorites.withPlaylistFavorite(two, true)
        assertEquals(setOf(one, two), favorites)
        favorites = favorites.withPlaylistFavorite(one, false)
        assertEquals(setOf(two), favorites)
        assertEquals(setOf(two), favorites.withPlaylistFavorite(one, false))
    }

    @Test fun favoritesStayFirstAndBelongToTheirAccount() {
        val account = ServerCredentials("https://music.test", "andy", "secret")
        val otherUser = ServerCredentials("https://music.test", "brother", "secret")
        val otherServer = ServerCredentials("https://other.test", "andy", "secret")
        val playlists = listOf("one", "two", "three", "four").map { Playlist(it, it) }
        val keys = setOf(playlistFavoriteKey(account, "two"), playlistFavoriteKey(account, "four"))
        assertEquals(listOf("two", "four", "one", "three"),
            favoriteFirstPlaylists(playlists, account, keys).map { it.id })
        assertEquals(playlists, favoriteFirstPlaylists(playlists, otherUser, keys))
        assertEquals(playlists, favoriteFirstPlaylists(playlists, otherServer, keys))
        assertEquals(playlistFavoriteKey(account, "two"), playlistFavoriteKey(
            ServerCredentials("https://music.test/", "andy", "changed-password"), "two"))
        assertNotEquals(playlistFavoriteKey(account, "two"), playlistFavoriteKey(account, "three"))
        assertEquals(NavigationStyle.Glaze, AppSettings().navigationStyle)
        assertTrue(AppSettings().searchInNavigation)
    }
}
