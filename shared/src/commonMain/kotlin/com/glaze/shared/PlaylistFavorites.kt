package com.glaze.shared

/** Local favorites: the Subsonic star endpoint does not support playlists. */
internal fun playlistFavoriteKey(credentials: ServerCredentials, playlistId: String): String {
    val server = credentials.serverUrl.trimEnd('/')
    val user = credentials.username
    return "${server.length}:$server${user.length}:$user$playlistId"
}

internal fun favoriteFirstPlaylists(
    playlists: List<Playlist>, credentials: ServerCredentials, favorites: Set<String>,
): List<Playlist> = playlists.sortedByDescending { playlistFavoriteKey(credentials, it.id) in favorites }

internal fun Set<String>.withPlaylistFavorite(key: String, favorite: Boolean): Set<String> =
    if (favorite) this + key else this - key
