package com.liquidglass.shared

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.Album
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Chevron_right
import com.composables.icons.materialsymbols.roundedfilled.Favorite
import com.composables.icons.materialsymbols.roundedfilled.More_vert
import com.composables.icons.materialsymbols.roundedfilled.Person
import com.composables.icons.materialsymbols.roundedfilled.Play_arrow
import com.composables.icons.materialsymbols.roundedfilled.Playlist_add
import com.composables.icons.materialsymbols.roundedfilled.Queue_music
import com.composables.icons.materialsymbols.roundedfilled.Share
import com.composables.icons.materialsymbols.roundedfilled.Shuffle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun AlbumCollectionScreen(
    album: Album, songs: List<Song>, client: SubsonicClient, darkMode: Boolean,
    onBack: () -> Unit, onPlaySong: (Song) -> Unit, onPlayAll: () -> Unit,
    onShuffle: () -> Unit, onAddNext: (Song) -> Unit,
    onShare: (String) -> Unit, onArtworkColor: suspend (String?) -> Color,
    onAlbum: (Album) -> Unit, onArtist: (Artist) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var favorite by remember(album.id) { mutableStateOf(album.starred) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var albumArtists by remember(album.id) { mutableStateOf(album.artists) }
    var moreFromArtists by remember(album.id) { mutableStateOf(emptyList<Pair<Artist, List<Album>>>()) }
    var similarAlbums by remember(album.id) { mutableStateOf(emptyList<Album>()) }
    var similarLoading by remember(album.id) { mutableStateOf(true) }
    var releaseDate by remember(album.id) { mutableStateOf(album.releaseDate) }
    var canonicalAlbum by remember(album.id) { mutableStateOf<Album?>(null) }
    LaunchedEffect(client, album.id, songs) {
        val details = try { client.albumDetails(album.id) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { null }
        canonicalAlbum = details
        releaseDate = details?.releaseDate ?: album.releaseDate
        val artists = (details?.artists.orEmpty().ifEmpty { album.artists }).ifEmpty {
            try { songs.firstOrNull()?.let { client.songArtists(it.id) }.orEmpty()
                .filter { album.artist.contains(it.name, ignoreCase = true) } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { emptyList() }
        }.ifEmpty {
            songs.firstOrNull { it.artistId != null }?.let {
                listOf(Artist(it.artistId!!, album.artist))
            }.orEmpty()
        }.distinctBy { it.id }
        albumArtists = artists
        moreFromArtists = coroutineScope {
            artists.map { artist -> async {
                artist to try { client.artistAlbums(artist.id)
                    .filter { it.id != album.id }.take(12) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { emptyList() }
            } }.awaitAll().filter { it.second.isNotEmpty() }
        }
    }
    LaunchedEffect(client, album.id, songs) {
        similarLoading = songs.isNotEmpty()
        similarAlbums = emptyList()
        var related = emptyList<Song>()
        for (seed in songs.take(2)) {
            val candidates = try { client.similarSongs(seed.id, 18) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { emptyList() }
            related = candidates.filter {
                it.albumId != null && it.albumId != album.id && it.artist != album.artist
            }
            if (related.isNotEmpty()) break
        }
        similarAlbums = related
            .distinctBy { it.albumId }.take(12).map { related ->
                Album(related.albumId!!, related.album, related.artist, related.coverArt)
            }
        similarLoading = false
    }
    val displayAlbum = canonicalAlbum ?: album
    CollectionSurface(album.name, displayAlbum.coverArt ?: album.coverArt, darkMode, onArtworkColor, onBack,
        onShare = { onShare("${displayAlbum.name} — ${displayAlbum.artist}") }) {
        item {
            val metadata = listOfNotNull(
                songs.firstOrNull()?.genre?.takeIf { it.isNotBlank() },
                displayAlbum.year?.toString(),
                "Lossless".takeIf { songs.any { song -> song.suffix.equals("flac", true) } },
            ).joinToString(" · ").ifBlank { null }
            CollectionHeader(displayAlbum.name, displayAlbum.artist, metadata,
                displayAlbum.coverArt ?: album.coverArt, client)
        }
        item {
            CollectionControls(
                darkMode, onShuffle, onPlayAll,
                favorite = favorite,
                onFavorite = {
                    val next = !favorite
                    scope.launch {
                        try { client.setAlbumStarred(album.id, next); favorite = next }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { }
                    }
                },
            )
        }
        item { CollectionDivider() }
        items(songs, key = { "track-${it.id}" }) { song ->
            CollectionTrackRow(song, song.track, false, client, onPlaySong) { selectedSong = song }
        }
        item {
            AlbumFooter(songs.size, songs.sumOf { it.durationSeconds.toLong() },
                releaseDate ?: displayAlbum.year?.toString())
        }
        item {
            Column(Modifier.fillMaxWidth()) {
                if (moreFromArtists.isNotEmpty() || similarLoading || similarAlbums.isNotEmpty())
                    CollectionDivider()
                moreFromArtists.forEach { (artist, albums) ->
                    AlbumCarousel("More from ${artist.name}", albums, client, onAlbum,
                        onMore = { onArtist(artist) })
                }
                if (similarAlbums.isNotEmpty())
                    AlbumCarousel("Similar albums you may like", similarAlbums, client, onAlbum)
                else if (similarLoading) SimilarAlbumsLoading()
            }
        }
    }
    selectedSong?.let { song ->
        CollectionSongSheet(song, client, onDismiss = { selectedSong = null },
            onPlayNext = { onAddNext(song) }, onArtist = onArtist,
            albumArtists = albumArtists)
    }
}

@Composable
internal fun PlaylistReferenceScreen(
    playlist: Playlist, songs: List<Song>, client: SubsonicClient, darkMode: Boolean,
    onBack: () -> Unit, onPlaySong: (Song) -> Unit, onPlayAll: () -> Unit,
    onShuffle: () -> Unit, onAddNext: (Song) -> Unit,
    onShare: (String) -> Unit, onArtworkColor: suspend (String?) -> Color,
    onPlaylistChanged: () -> Unit, onAlbum: (Album) -> Unit,
    onArtist: (Artist) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var recommendations by remember(playlist.id) { mutableStateOf(emptyList<Song>()) }
    var addingId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(client, playlist.id, songs) {
        val inPlaylist = songs.mapTo(mutableSetOf()) { it.id }
        recommendations = songs.take(3).flatMap { seed ->
            try { client.similarSongs(seed.id, 12) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { emptyList() }
        }.filter { it.id !in inPlaylist }.distinctBy { it.id }.take(8)
    }
    val artworkId = playlist.coverArt ?: songs.firstOrNull()?.coverArt
    CollectionSurface(playlist.name, artworkId, darkMode, onArtworkColor, onBack,
        onShare = { onShare(playlist.name) }) {
        item {
            CollectionHeader(
                playlist.name, "Playlist",
                "${songs.size} songs · ${formatQueueDuration(songs.sumOf { it.durationSeconds.toLong() })}",
                artworkId, client,
            )
        }
        item { PlaylistControls(darkMode, onShuffle, onPlayAll) }
        item { CollectionDivider() }
        items(songs, key = { "track-${it.id}" }) { song ->
            CollectionTrackRow(song, null, true, client, onPlaySong) { selectedSong = song }
        }
        if (recommendations.isNotEmpty()) {
            item {
                Text("Recommended songs", color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(start = 22.dp, top = 34.dp, bottom = 2.dp))
                Text("Based on the songs in this playlist",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 22.dp, bottom = 14.dp))
            }
            items(recommendations, key = { "recommended-${it.id}" }) { song ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    CollectionArtwork(client, song.coverArt ?: song.albumId,
                        Modifier.size(52.dp), 160)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground)
                        Text(song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = {
                        if (addingId == null) scope.launch {
                            addingId = song.id
                            try {
                                client.addSongToPlaylist(playlist.id, song.id)
                                recommendations = recommendations.filterNot { it.id == song.id }
                                onPlaylistChanged()
                            } catch (cancelled: CancellationException) { throw cancelled }
                            catch (_: Exception) { }
                            finally { addingId = null }
                        }
                    }) {
                        if (addingId == song.id) CircularProgressIndicator(Modifier.size(18.dp))
                        else Icon(MaterialSymbols.RoundedFilled.Playlist_add,
                            contentDescription = "Add ${song.title} to playlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    selectedSong?.let { song ->
        CollectionSongSheet(song, client, onDismiss = { selectedSong = null },
            onPlayNext = { onAddNext(song) }, onAlbum = onAlbum, onArtist = onArtist)
    }
}

@Composable
private fun CollectionSurface(
    title: String, artworkId: String?, darkMode: Boolean,
    onArtworkColor: suspend (String?) -> Color,
    onBack: () -> Unit, onShare: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    val listState = rememberLazyListState()
    val titleIsPast by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }
    var sampled by remember(artworkId) { mutableStateOf(Color(0xFF626262)) }
    LaunchedEffect(artworkId) {
        sampled = try { onArtworkColor(artworkId) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { Color(0xFF626262) }
    }
    val accent = if (sampled == Color.Black) Color(0xFF626262) else sampled
    val base = if (darkMode) lerp(Color(0xFF141414), accent, 0.20f)
        else lerp(Color(0xFFF9F9F9), accent, 0.12f)
    val glow = if (darkMode) lerp(base, accent, 0.20f)
        else lerp(base, accent, 0.12f)
    val barColor by animateColorAsState(
        if (titleIsPast) base else Color.Transparent,
        animationSpec = tween(140), label = "Collection bar color")
    Box(Modifier.fillMaxSize().background(base)) {
        Box(Modifier.fillMaxSize().drawWithCache {
            val brush = Brush.radialGradient(
                0f to glow, 1f to base,
                center = Offset(size.width * 0.5f, size.height * 0.28f),
                radius = size.width * 1.0f,
            )
            onDrawBehind { drawRect(brush) }
        })
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 220.dp)) {
            item { Spacer(Modifier.height(104.dp)) }
            content()
        }
        Row(Modifier.fillMaxWidth()
            .background(barColor)
            .statusBarsPadding().padding(start = 12.dp, end = 12.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(MaterialSymbols.RoundedFilled.Arrow_back, "Back",
                    tint = MaterialTheme.colorScheme.onBackground)
            }
            Box(Modifier.weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                Crossfade(titleIsPast, animationSpec = tween(140), label = "Collection title") { visible ->
                    if (visible) Text(title, color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = onShare) {
                Icon(MaterialSymbols.RoundedFilled.Share, "Share",
                    tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun CollectionHeader(
    title: String, subtitle: String, metadata: String?,
    artworkId: String?, client: SubsonicClient,
) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val coverSize = (maxWidth * 0.68f).coerceAtMost(330.dp)
        CollectionArtwork(client, artworkId,
            Modifier.size(coverSize).shadow(18.dp, RoundedCornerShape(8.dp)))
    }
    Spacer(Modifier.height(22.dp))
    Text(title, color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
    Spacer(Modifier.height(3.dp))
    Text(subtitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
        style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
        maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
    if (metadata != null) {
        Spacer(Modifier.height(5.dp))
        Text(metadata, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun CollectionArtwork(client: SubsonicClient, id: String?, modifier: Modifier,
    requestSize: Int = 600) {
    val url = remember(client, id, requestSize) { id?.let { client.coverArtUrl(it, requestSize) } }
    Box(modifier.clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.09f))) {
        if (url != null) AsyncImage(model = url, contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun PlaylistControls(darkMode: Boolean, onShuffle: () -> Unit, onPlay: () -> Unit) {
    val ink = MaterialTheme.colorScheme.onBackground
    Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 25.dp, bottom = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.weight(1f).height(50.dp).clip(CircleShape)
            .background(ink.copy(alpha = if (darkMode) 0.13f else 0.09f))
            .clickable(onClick = onShuffle),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(MaterialSymbols.RoundedFilled.Shuffle, null, tint = ink)
            Spacer(Modifier.width(8.dp))
            Text("Shuffle", color = ink,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
        Row(Modifier.weight(1f).height(50.dp).clip(CircleShape)
            .background(ink).clickable(onClick = onPlay),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(MaterialSymbols.RoundedFilled.Play_arrow, null,
                tint = MaterialTheme.colorScheme.background)
            Spacer(Modifier.width(8.dp))
            Text("Play", color = MaterialTheme.colorScheme.background,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun AlbumFooter(songCount: Int, durationSeconds: Long, releaseDate: String?) {
    Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 28.dp, bottom = 38.dp)) {
        releaseDate?.let {
            Text(it, color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(3.dp))
        }
        Text("$songCount songs · ${formatQueueDuration(durationSeconds)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CollectionControls(
    darkMode: Boolean, onShuffle: () -> Unit, onPlay: () -> Unit,
    favorite: Boolean? = null, onFavorite: (() -> Unit)? = null,
) {
    val ink = MaterialTheme.colorScheme.onBackground
    val quietFill = ink.copy(alpha = if (darkMode) 0.10f else 0.07f)
    Row(Modifier.fillMaxWidth().padding(top = 25.dp, bottom = 22.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(51.dp).clip(CircleShape).background(quietFill)
            .clickable(onClick = onShuffle), contentAlignment = Alignment.Center) {
            Icon(MaterialSymbols.RoundedFilled.Shuffle, "Shuffle", tint = ink)
        }
        Spacer(Modifier.width(16.dp))
        Row(Modifier.width(168.dp).height(52.dp).clip(CircleShape)
            .background(ink).clickable(onClick = onPlay),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(MaterialSymbols.RoundedFilled.Play_arrow, null,
                tint = MaterialTheme.colorScheme.background)
            Spacer(Modifier.width(5.dp))
            Text("Play", color = MaterialTheme.colorScheme.background,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
        Spacer(Modifier.width(16.dp))
        if (favorite != null && onFavorite != null) {
            Box(Modifier.size(51.dp).clip(CircleShape).background(quietFill)
                .clickable(onClick = onFavorite), contentAlignment = Alignment.Center) {
                Icon(MaterialSymbols.RoundedFilled.Favorite,
                    if (favorite) "Remove favorite" else "Favorite album",
                    tint = ink.copy(alpha = if (favorite) 1f else 0.45f))
            }
        } else Spacer(Modifier.size(51.dp))
    }
}

@Composable
private fun CollectionDivider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 17.dp).height(1.dp)
        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f)))
}

@Composable
private fun CollectionTrackRow(
    song: Song, trackNumber: Int?, showArtist: Boolean, client: SubsonicClient,
    onPlay: (Song) -> Unit, onMore: () -> Unit,
) {
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
    Row(Modifier.fillMaxWidth().height(62.dp)
        .clickable { onPlay(song) }.padding(start = 20.dp, end = 9.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (showArtist) {
            CollectionArtwork(client, song.coverArt ?: song.albumId,
                Modifier.size(44.dp), 160)
            Spacer(Modifier.width(12.dp))
        } else Text(trackNumber?.toString() ?: "", color = quiet,
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(35.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isExplicit) {
                    ExplicitBadge(quiet)
                    Spacer(Modifier.width(5.dp))
                }
                Text(if (showArtist) song.artist else
                    "${song.durationSeconds / 60}:${(song.durationSeconds % 60).toString().padStart(2, '0')}",
                    color = quiet, style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        IconButton(onClick = onMore) {
            Icon(MaterialSymbols.RoundedFilled.More_vert, "Options for ${song.title}", tint = quiet)
        }
    }
    Box(Modifier.fillMaxWidth().padding(start = if (showArtist) 76.dp else 55.dp, end = 18.dp).height(1.dp)
        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)))
}

@Composable
private fun AlbumCarousel(
    title: String, albums: List<Album>, client: SubsonicClient, onAlbum: (Album) -> Unit,
    onMore: (() -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth().then(if (onMore != null) Modifier.clickable(onClick = onMore)
        else Modifier).padding(start = 22.dp, end = 20.dp, top = 38.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f))
        if (onMore != null) Icon(MaterialSymbols.RoundedFilled.Chevron_right,
            contentDescription = "View ${title.removePrefix("More from ")} artist profile",
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp)) {
        items(albums, key = { it.id }) { album ->
            Column(Modifier.width(148.dp).clickable { onAlbum(album) }) {
                CollectionArtwork(client, album.coverArt, Modifier.size(148.dp))
                Spacer(Modifier.height(7.dp))
                Text(album.name, color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(album.artist, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SimilarAlbumsLoading() {
    Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 38.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text("Similar albums you may like", color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f))
        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp)) {
        items(3) {
            Box(Modifier.size(148.dp).clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionSongSheet(
    song: Song, client: SubsonicClient,
    onDismiss: () -> Unit, onPlayNext: () -> Unit,
    onAlbum: ((Album) -> Unit)? = null, onArtist: (Artist) -> Unit,
    albumArtists: List<Artist> = emptyList(),
) {
    val scope = rememberCoroutineScope()
    var showPlaylists by remember(song.id) { mutableStateOf(false) }
    var showArtists by remember(song.id) { mutableStateOf(false) }
    var artistChoices by remember(song.id) {
        mutableStateOf(albumArtists.ifEmpty {
            song.artistId?.let { listOf(Artist(it, song.artist)) }.orEmpty()
        })
    }
    var playlists by remember(song.id) { mutableStateOf(emptyList<Playlist>()) }
    var loading by remember(song.id) { mutableStateOf(false) }
    var message by remember(song.id) { mutableStateOf<String?>(null) }
    var favorite by remember(song.id) { mutableStateOf(song.starred) }
    LaunchedEffect(client, song.id) {
        try { favorite = client.songById(song.id)?.starred ?: favorite }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { }
    }
    LaunchedEffect(client, song.id, albumArtists) {
        artistChoices = if (albumArtists.isNotEmpty()) albumArtists else try {
            client.songArtists(song.id)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { emptyList() }
        if (artistChoices.isEmpty() && song.artistId != null)
            artistChoices = listOf(Artist(song.artistId, song.artist))
    }
    LaunchedEffect(showPlaylists, client) {
        if (showPlaylists) {
            loading = true
            try { playlists = client.playlists() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { message = "Could not load playlists" }
            finally { loading = false }
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        sheetGesturesEnabled = false,
        containerColor = Color.Transparent, contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.28f),
        dragHandle = null, contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) {
        PlayerSheetSurface((song.coverArt ?: song.albumId)?.let { client.coverArtUrl(it, 600) },
            Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp)) {
            SheetHandle()
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                CollectionArtwork(client, song.coverArt ?: song.albumId,
                    Modifier.size(62.dp), 160)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = Color.White, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
                    Text(song.artist, color = Color.LightGray, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall)
                    Text(song.album, color = Color.LightGray.copy(alpha = 0.82f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(if (showPlaylists) "ADD TO PLAYLIST" else if (showArtists) "GO TO ARTIST"
                else "SONG ACTIONS",
                color = Color.LightGray, style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
                modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 10.dp))
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.09f)).heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState())) {
            if (showArtists) {
                SongOptionRow(MaterialSymbols.RoundedFilled.Arrow_back, "Back to song actions") {
                    showArtists = false
                }
                artistChoices.forEach { artist ->
                    SongOptionRow(MaterialSymbols.RoundedFilled.Person, artist.name) {
                        onDismiss(); onArtist(artist)
                    }
                }
            } else if (showPlaylists) {
                SongOptionRow(MaterialSymbols.RoundedFilled.Arrow_back, "Back to song actions") {
                    showPlaylists = false
                }
                if (loading) CircularProgressIndicator(Modifier.padding(20.dp))
                else if (message != null) Text(message!!, color = Color.LightGray)
                else if (playlists.isEmpty()) Text("No playlists available", color = Color.LightGray)
                else playlists.forEach { playlist ->
                    SongOptionRow(MaterialSymbols.RoundedFilled.Playlist_add, playlist.name) {
                        if (!loading) scope.launch {
                            loading = true
                            try { client.addSongToPlaylist(playlist.id, song.id); onDismiss() }
                            catch (cancelled: CancellationException) { throw cancelled }
                            catch (_: Exception) { message = "Could not add to ${playlist.name}" }
                            finally { loading = false }
                        }
                    }
                }
            } else {
                SongOptionRow(MaterialSymbols.RoundedFilled.Playlist_add, "Add to playlist") {
                    showPlaylists = true
                }
                SongOptionRow(MaterialSymbols.RoundedFilled.Queue_music, "Play next") {
                    onPlayNext(); onDismiss()
                }
                if (onAlbum != null && song.albumId != null)
                    SongOptionRow(MaterialSymbols.RoundedFilled.Album, "Go to album") {
                        onDismiss()
                        onAlbum(Album(song.albumId, song.album, song.artist, song.coverArt))
                    }
                if (artistChoices.isNotEmpty())
                    SongOptionRow(MaterialSymbols.RoundedFilled.Person, "Go to artist") {
                        if (artistChoices.size == 1) {
                            onDismiss(); onArtist(artistChoices.first())
                        } else showArtists = true
                    }
                SongOptionRow(MaterialSymbols.RoundedFilled.Favorite,
                    if (favorite) "Remove from favorites" else "Add to favorites") {
                    val next = !favorite
                    scope.launch {
                        try { client.setSongStarred(song.id, next); favorite = next; onDismiss() }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { message = "Could not update favorite" }
                    }
                }
                if (message != null) Text(message!!, color = Color.LightGray)
            }
            }
        }
        }
    }
}
