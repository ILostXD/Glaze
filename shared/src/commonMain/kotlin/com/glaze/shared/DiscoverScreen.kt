package com.glaze.shared

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.*

internal fun LazyListScope.discoverResults(
    query: String, results: DiscoverResults?, loading: Boolean, error: String?,
    onAlbum: (DiscoverAlbum) -> Unit,
    onRequest: (LibraryRequest) -> Unit, onRetry: () -> Unit,
) {
    when {
        query.isBlank() -> item {
            Text("Search for an artist, album or song above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
                modifier = Modifier.padding(start = 36.dp, end = 22.dp, top = 2.dp, bottom = 8.dp))
        }
        loading -> item {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(28.dp))
            }
        }
        error != null -> item {
            Text(error, color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
            TextButton(onClick = onRetry, modifier = Modifier.padding(horizontal = 12.dp)) { Text("Retry") }
        }
        results != null -> {
            if (results.albums.isNotEmpty()) item {
                SectionTitle("Albums", 14.dp)
            }
            items(results.albums, key = { "discover-album:${it.id}" }) { album ->
                DiscoverRow(album.title, album.artist, album.cover,
                    listOfNotNull(album.tracks.size.takeIf { it > 0 }?.let { "$it tracks" }, album.releaseDate.take(4).takeIf { it.isNotBlank() }).joinToString(" · "), "View album",
                    onClick = { onAlbum(album) },
                    onDownload = { onRequest(LibraryRequest(album.artist, album.title, "album")) })
            }
            if (results.tracks.isNotEmpty()) item {
                SectionTitle("Songs", 14.dp)
            }
            items(results.tracks, key = { "discover-track:${it.id}" }) { track ->
                DiscoverRow(track.title, track.artist, track.cover, track.albumTitle, "Download",
                    explicit = track.explicit,
                    onClick = { onAlbum(DiscoverAlbum(track.albumId, track.albumTitle, track.artist, track.cover, "", "", listOf(track))) },
                    onDownload = { onRequest(LibraryRequest(track.artist, track.title, "track")) })
            }
            if (results.albums.isEmpty() && results.tracks.isEmpty()) item {
                Text("No released matches outside your library.", color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(22.dp))
            }
        }
    }
}

@Composable
internal fun DiscoverArtistScreen(artist: DiscoverArtist, client: SubsonicClient, darkMode: Boolean,
    onBack: () -> Unit, onAlbum: (DiscoverAlbum) -> Unit, onRequest: (LibraryRequest) -> Unit) {
    val catalogue = remember(client) { DiscoverClient() }
    DisposableEffect(catalogue) { onDispose { catalogue.close() } }
    var albums by remember(artist.id) { mutableStateOf<List<DiscoverAlbum>>(emptyList()) }
    var loading by remember(artist.id) { mutableStateOf(true) }
    LaunchedEffect(artist.id, client) {
        loading = true
        try { albums = catalogue.artistAlbums(artist.name, client.allAlbums(), artist.id) } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { albums = emptyList() }
        finally { loading = false }
    }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 180.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(440.dp)) {
                AsyncImage(artist.picture, artist.name, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.36f)))
                IconButton(onClick = onBack,
                    modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(16.dp)) {
                    Icon(MaterialSymbols.RoundedFilled.Arrow_back, "Back", tint = Color.White)
                }
                Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(artistLabel(artist.name), color = Color.White,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))

                }
            }
        }
        if (loading) item { CircularProgressIndicator(Modifier.padding(24.dp).size(28.dp)) }
        if (albums.isNotEmpty()) item { SectionTitle("Albums", 14.dp) }
        items(albums, key = { it.id }) { album ->
            DiscoverRow(album.title, album.artist, album.cover,
                listOfNotNull(album.tracks.size.takeIf { it > 0 }?.let { "$it tracks" }, album.releaseDate.take(4).takeIf { it.isNotBlank() }).joinToString(" · "), "View album",
                onClick = { onAlbum(album) },
                onDownload = { onRequest(LibraryRequest(album.artist, album.title, "album")) })
        }
    }
}

@Composable
private fun DiscoverCover(url: String, description: String, modifier: Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Box(modifier.clip(shape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))) {
        AsyncImage(url, description, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.22f)))
        Icon(MaterialSymbols.RoundedFilled.Download, null, tint = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).size(20.dp)
                .background(Color.Black.copy(alpha = 0.40f), RoundedCornerShape(6.dp)).padding(2.dp))
    }
}

@Composable
internal fun DiscoverRow(title: String, artist: String, cover: String, subtitle: String,
    action: String, explicit: Boolean = false,
    onClick: () -> Unit, onDownload: () -> Unit = onClick) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp)
        .clickable(onClickLabel = action, onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        DiscoverCover(cover, "$title catalogue artwork", Modifier.size(60.dp))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (explicit) {
                    ExplicitBadge(MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(5.dp))
                }
                Text(artist, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
            Text(artistLabel(subtitle), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onDownload) {
            Icon(MaterialSymbols.RoundedFilled.Download, "Download $title", Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
internal fun DiscoverAlbumScreen(album: DiscoverAlbum, client: SubsonicClient, darkMode: Boolean,
    onArtworkColor: suspend (String?) -> Color, onBack: () -> Unit,
    onShare: (String) -> Unit, onRequest: (LibraryRequest) -> Unit) {
    val catalogue = remember(client) { DiscoverClient() }
    DisposableEffect(catalogue) { onDispose { catalogue.close() } }
    var detail by remember(album.id) { mutableStateOf(album) }
    LaunchedEffect(album.id) {
        try { detail = catalogue.album(album.id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { /* Keep the metadata already shown in search. */ }
    }
    CollectionSurface(detail.title, detail.cover, darkMode, onArtworkColor, onBack,
        onShare = { onShare("${detail.title} — ${detail.artist}") }, headerItemIndex = 3) {
        item { }
        item { Spacer(Modifier.height(32.dp)) }
        item {
            CollectionHeader(detail.title, detail.artist,
                listOf(detail.genre, detail.releaseDate.take(4)).filter { it.isNotBlank() }.joinToString(" · "),
                null, client, artworkUrl = detail.cover, artworkDimmed = true)
        }
        item {
            CollectionControls(darkMode, onShuffle = {},
                onPlay = { onRequest(LibraryRequest(detail.artist, detail.title, "album")) },
                favorite = false, onFavorite = {}, favoriteEnabled = false, download = true)
        }
        item { CollectionDivider() }
        itemsIndexed(detail.tracks, key = { _, track -> track.id }) { index, track ->
            CollectionTrackRow(Song(id = "discover:${track.id}", title = track.title,
                artist = track.artist, album = detail.title, durationSeconds = track.duration,
                isExplicit = track.explicit), index + 1, false, client, onPlay = {},
                dividerAbove = index > 0, downloadOnly = true) {
                onRequest(LibraryRequest(track.artist, track.title, "track"))
            }
        }
        item { AlbumFooter(detail.tracks.size, detail.tracks.sumOf { it.duration.toLong() }, detail.releaseDate) }
    }
}
