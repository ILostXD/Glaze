package com.glaze.shared

internal data class LibrarySnapshot(
    val albums: List<Album>, val artists: List<Artist>, val playlists: List<Playlist>,
    val recentAlbums: List<Album>, val frequentAlbums: List<Album>, val genres: List<Genre>,
)

/** Fetch a complete snapshot before replacing any displayed library data. */
internal suspend fun loadLibrary(client: SubsonicClient, onProgress: (Float, String) -> Unit): LibrarySnapshot {
    onProgress(0f, "Albums")
    val albums = client.allAlbums()
    onProgress(1f / 6, "Artists")
    val artists = client.artists()
    onProgress(2f / 6, "Playlists")
    val playlists = client.playlists()
    onProgress(3f / 6, "Listening history")
    val recent = client.recentlyPlayedAlbums()
    onProgress(4f / 6, "Your rotation")
    val frequent = client.frequentlyPlayedAlbums()
    onProgress(5f / 6, "Genres")
    val genres = client.genres()
    onProgress(1f, "Library synchronized")
    return LibrarySnapshot(albums, artists, playlists, recent, frequent, genres)
}
