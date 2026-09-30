package com.liquidglass.shared

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.liquidglass.shared.resources.Res
import com.liquidglass.shared.resources.am_lossless
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.Album
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Chevron_right
import com.composables.icons.materialsymbols.roundedfilled.Check
import com.composables.icons.materialsymbols.roundedfilled.Close
import com.composables.icons.materialsymbols.roundedfilled.Delete
import com.composables.icons.materialsymbols.roundedfilled.Drag_handle
import com.composables.icons.materialsymbols.roundedfilled.Edit
import com.composables.icons.materialsymbols.roundedfilled.Favorite
import com.composables.icons.materialsymbols.rounded.Favorite as FavoriteOutline
import com.composables.icons.materialsymbols.roundedfilled.More_vert
import com.composables.icons.materialsymbols.roundedfilled.Person
import com.composables.icons.materialsymbols.roundedfilled.Play_arrow
import com.composables.icons.materialsymbols.roundedfilled.Playlist_add
import com.composables.icons.materialsymbols.roundedfilled.Queue_music
import com.composables.icons.materialsymbols.roundedfilled.Share
import com.composables.icons.materialsymbols.roundedfilled.Shuffle
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.math.abs

@Composable
internal fun AlbumCollectionScreen(
    album: Album, songs: List<Song>, client: SubsonicClient, darkMode: Boolean,
    onBack: () -> Unit, onPlaySong: (Song) -> Unit, onPlayAll: () -> Unit,
    onShuffle: () -> Unit, onAddNext: (Song) -> Unit,
    onShare: (String) -> Unit, onArtworkColor: suspend (String?) -> Color,
    onAlbum: (Album) -> Unit, onArtist: (Artist) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var favorite by remember(client, album.id) { mutableStateOf(album.starred) }
    var favoriteLoading by remember(client, album.id) { mutableStateOf(true) }
    var favoritePending by remember(client, album.id) { mutableStateOf(false) }
    var favoriteChanged by remember(client, album.id) { mutableStateOf(false) }
    val snackbar = remember(client, album.id) { SnackbarHostState() }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var albumArtists by remember(album.id) { mutableStateOf(album.artists) }
    var moreFromArtists by remember(album.id) { mutableStateOf(emptyList<Pair<Artist, List<Album>>>()) }
    var similarAlbums by remember(album.id) { mutableStateOf(emptyList<Album>()) }
    var similarLoading by remember(album.id) { mutableStateOf(true) }
    var searchQuery by remember(album.id) { mutableStateOf("") }
    var searchVisible by remember(album.id) { mutableStateOf(false) }
    var releaseDate by remember(album.id) { mutableStateOf(album.releaseDate) }
    var canonicalAlbum by remember(album.id) { mutableStateOf<Album?>(null) }
    var showAlbumArtists by remember(album.id) { mutableStateOf(false) }
    LaunchedEffect(client, album.id, songs) {
        val details = try { client.albumDetails(album.id) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { null }
        canonicalAlbum = details
        if (!favoriteChanged) favorite = details?.starred ?: album.starred
        favoriteLoading = false
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
            val candidates = try { client.similarSongs(seed.id, 12) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { emptyList() }
            related = candidates.filter {
                it.albumId != null && it.albumId != album.id && it.artist != album.artist
            }
            if (related.isNotEmpty()) break
        }
        similarAlbums = recommendationAlbums(client, related)
        similarLoading = false
    }
    val displayAlbum = canonicalAlbum ?: album
    val visibleSongs = songs.filter { searchQuery.isBlank() ||
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.artist.contains(searchQuery, ignoreCase = true) }
    CollectionSurface(album.name, displayAlbum.coverArt ?: album.coverArt, darkMode, onArtworkColor, onBack,
        onShare = { onShare("${displayAlbum.name} — ${displayAlbum.artist}") }, snackbar = snackbar,
        onPullAtTop = { searchVisible = true },
        onScrollAway = { if (searchVisible) { searchVisible = false; searchQuery = "" } },
        headerItemIndex = 3) {
        item { if (searchVisible) LibrarySearchField(searchQuery, { searchQuery = it },
            "Find in album", Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) }
        item { Spacer(Modifier.height(32.dp)) }
        item {
            val lossless = songs.any { song -> song.suffix.equals("flac", true) }
            val metadata = listOfNotNull(
                songs.firstOrNull()?.genre?.takeIf { it.isNotBlank() },
                displayAlbum.year?.toString(),
            ).joinToString(" · ").ifBlank { null }
            CollectionHeader(displayAlbum.name, displayAlbum.artist, metadata,
                displayAlbum.coverArt ?: album.coverArt, client,
                lossless = lossless,
                onSubtitleClick = if (albumArtists.isEmpty()) null else {{
                    if (albumArtists.size == 1) onArtist(albumArtists.first())
                    else showAlbumArtists = true
                }})
        }
        item {
            CollectionControls(
                darkMode, onShuffle, onPlayAll,
                favorite = favorite,
                favoriteEnabled = !favoriteLoading && !favoritePending,
                onFavorite = {
                    if (!favoriteLoading && !favoritePending) {
                        val before = favorite
                        favorite = !before
                        favoriteChanged = true
                        favoritePending = true
                        scope.launch {
                            var failed = false
                            try { client.setAlbumStarred(album.id, !before) }
                            catch (cancelled: CancellationException) { favorite = before; throw cancelled }
                            catch (_: Exception) { favorite = before; failed = true }
                            finally { favoritePending = false }
                            if (failed) snackbar.showSnackbar("Could not update album favorite")
                        }
                    }
                },
            )
        }
        item { CollectionDivider() }
        itemsIndexed(visibleSongs, key = { _, song -> "track-${song.id}" }) { rowIndex, song ->
            CollectionSwipeRow(false, onPlayNext = { onAddNext(song) }) {
                CollectionTrackRow(song, song.track, false, client, onPlaySong,
                    dividerAbove = rowIndex > 0) { selectedSong = song }
            }
        }
        if (searchQuery.isNotBlank() && visibleSongs.isEmpty()) item {
            Text("No matching songs", color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(22.dp))
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
    if (showAlbumArtists) AlbumArtistSheet(albumArtists, displayAlbum.coverArt ?: album.coverArt,
        client, onDismiss = { showAlbumArtists = false }, onArtist = {
            showAlbumArtists = false
            onArtist(it)
        })
}

internal suspend fun recommendationAlbums(client: SubsonicClient, songs: List<Song>): List<Album> =
    coroutineScope {
        songs.mapNotNull { it.albumId }.distinct().take(6).map { id -> async {
            try { client.albumDetails(id) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { null }
        } }.awaitAll().filterNotNull()
    }

@Composable
internal fun PlaylistReferenceScreen(
    playlist: Playlist, songs: List<Song>, client: SubsonicClient, darkMode: Boolean,
    onBack: () -> Unit, onPlaySong: (Song) -> Unit, onPlayAll: () -> Unit,
    onShuffle: () -> Unit, onAddNext: (Song) -> Unit,
    onShare: (String) -> Unit, onArtworkColor: suspend (String?) -> Color,
    onPlaylistChanged: () -> Unit, onAlbum: (Album) -> Unit,
    onArtist: (Artist) -> Unit,
    favorite: Boolean, onFavorite: (Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var selectedSong by remember { mutableStateOf<IndexedValue<Song>?>(null) }
    var recommendations by remember(playlist.id) { mutableStateOf(emptyList<Song>()) }
    var addingId by remember { mutableStateOf<String?>(null) }
    var orderedSongs by remember(playlist.id) { mutableStateOf(songs) }
    var editing by remember(playlist.id) { mutableStateOf(false) }
    var saving by remember(playlist.id) { mutableStateOf(false) }
    var editError by remember(playlist.id) { mutableStateOf<String?>(null) }
    var searchQuery by remember(playlist.id) { mutableStateOf("") }
    var searchVisible by remember(playlist.id) { mutableStateOf(false) }
    val snackbar = remember(playlist.id) { SnackbarHostState() }
    var previewOrder by remember(playlist.id) { mutableStateOf<List<Int>?>(null) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var dragPointerY by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(songs) { if (!editing && !saving && draggingIndex == null) orderedSongs = songs }
    val rowHeightPx = with(LocalDensity.current) { 63.dp.toPx() }
    val edgePx = with(LocalDensity.current) { 72.dp.toPx() }
    val scrollStepPx = with(LocalDensity.current) { 14.dp.toPx() }
    fun advanceDragPreview() {
        val index = draggingIndex ?: return
        var order = previewOrder ?: return
        var position = order.indexOf(index)
        if (position < 0) return
        while (dragY > rowHeightPx / 2f && position < order.lastIndex) {
            order = queuePreviewMoved(order, position, position + 1)
            position++
            dragY -= rowHeightPx
        }
        while (dragY < -rowHeightPx / 2f && position > 0) {
            order = queuePreviewMoved(order, position, position - 1)
            position--
            dragY += rowHeightPx
        }
        previewOrder = order
    }
    LaunchedEffect(draggingIndex) {
        while (draggingIndex != null) {
            val layout = listState.layoutInfo
            val position = draggingIndex?.let { previewOrder?.indexOf(it) } ?: -1
            val step = when {
                position > 0 && dragPointerY < layout.viewportStartOffset + edgePx -> -scrollStepPx
                position in 0 until orderedSongs.lastIndex &&
                    dragPointerY > layout.viewportEndOffset - edgePx -> scrollStepPx
                else -> 0f
            }
            if (step != 0f) {
                val scrolled = listState.scrollBy(step)
                if (scrolled != 0f) { dragY += scrolled; advanceDragPreview() }
            }
            delay(16)
        }
    }
    val dragModifier = if (editing && !saving) Modifier.pointerInput(orderedSongs) {
        val handleWidth = 64.dp.toPx()
        detectDragGesturesAfterLongPress(
            onDragStart = { start ->
                val item = listState.layoutInfo.visibleItemsInfo.firstOrNull {
                    start.y >= it.offset && start.y < it.offset + it.size
                }
                val index = item?.key?.toString()?.substringAfter("playlist-track:")?.toIntOrNull()
                if (start.x >= size.width - handleWidth && index != null && index in orderedSongs.indices) {
                    draggingIndex = index
                    dragY = 0f
                    dragPointerY = start.y
                    previewOrder = orderedSongs.indices.toList()
                }
            },
            onDrag = { change, amount ->
                if (draggingIndex != null) {
                    dragY += amount.y
                    dragPointerY = change.position.y
                    advanceDragPreview()
                    change.consume()
                }
            },
            onDragEnd = {
                val from = draggingIndex
                val to = from?.let { previewOrder?.indexOf(it) }
                draggingIndex = null
                dragY = 0f
                previewOrder = null
                if (from != null && to != null && to != from) {
                    val before = orderedSongs
                    orderedSongs = queuePreviewMoved(before.indices.toList(), from, to)
                        .map { before[it] }
                }
            },
            onDragCancel = { draggingIndex = null; dragY = 0f; previewOrder = null },
        )
    } else Modifier
    val visibleRows = (previewOrder ?: orderedSongs.indices.toList())
        .map { IndexedValue(it, orderedSongs[it]) }
        .filter { (_, song) -> searchQuery.isBlank() ||
            song.title.contains(searchQuery, ignoreCase = true) ||
            song.artist.contains(searchQuery, ignoreCase = true) }
    LaunchedEffect(client, playlist.id, songs) {
        val inPlaylist = songs.mapTo(mutableSetOf()) { it.id }
        recommendations = songs.take(3).flatMap { seed ->
            try { client.similarSongs(seed.id, 12) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { emptyList() }
        }.filter { it.id !in inPlaylist }.distinctBy { it.id }.take(8)
    }
    val artworkId = playlist.coverArt ?: orderedSongs.firstOrNull()?.coverArt
    CollectionSurface(playlist.name, artworkId, darkMode, onArtworkColor, onBack,
        onShare = { onShare(playlist.name) }, listState = listState, listModifier = dragModifier,
        onEdit = { searchQuery = ""; searchVisible = false; editing = true; editError = null }, editing = editing,
        onPullAtTop = { if (!editing) searchVisible = true },
        onScrollAway = { if (searchVisible) { searchVisible = false; searchQuery = "" } },
        onCancel = {
            if (!saving) {
                snackbar.currentSnackbarData?.dismiss()
                orderedSongs = songs
                editing = false
                editError = null
            }
        },
        onDone = {
            if (!saving) scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                if (orderedSongs.map { it.id } == songs.map { it.id }) {
                    editing = false
                } else {
                    saving = true
                    try {
                        client.replacePlaylistSongs(playlist.id, orderedSongs)
                        val saved = client.playlistSongs(playlist.id)
                        if (saved.map { it.id } == orderedSongs.map { it.id }) {
                            orderedSongs = saved
                            editing = false
                            editError = null
                            onPlaylistChanged()
                        } else editError = "Server did not keep the requested changes"
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { editError = "Could not save playlist changes" }
                    finally { saving = false }
                }
            }
        }, snackbar = snackbar, headerItemIndex = 3) {
        item { if (!editing && searchVisible) {
            LibrarySearchField(searchQuery, { searchQuery = it }, "Find in playlist",
                Modifier.padding(horizontal = 18.dp, vertical = 8.dp))
        } }
        item { Spacer(Modifier.height(32.dp)) }
        item {
            CollectionHeader(
                playlist.name, "Playlist",
                "${orderedSongs.size} songs · ${formatQueueDuration(orderedSongs.sumOf { it.durationSeconds.toLong() })}",
                artworkId, client,
            )
        }
        item { CollectionControls(darkMode, onShuffle, onPlayAll,
            favorite = favorite, favoriteLabel = "playlist",
            onFavorite = { onFavorite(!favorite) }) }
        if (editError != null) item {
            Text(editError!!, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp))
        }
        item { CollectionDivider() }
        itemsIndexed(visibleRows, key = { _, row -> "playlist-track:${row.index}" }) { rowIndex, (index, song) ->
            CollectionSwipeRow(editing, enabled = !saving, allowPlayNext = !editing,
                onPlayNext = { onAddNext(song) }, onRemove = {
                    if (editing && !saving && index in orderedSongs.indices) {
                        orderedSongs = orderedSongs.toMutableList().apply { removeAt(index) }
                        scope.launch {
                            if (snackbar.showSnackbar("${song.title} was deleted", "Restore",
                                    duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed && editing) {
                                orderedSongs = orderedSongs.toMutableList().apply {
                                    add(index.coerceAtMost(size), song)
                                }
                            }
                        }
                    }
                }, modifier = Modifier.animateItem(
                    fadeInSpec = null, fadeOutSpec = null,
                    placementSpec = if (draggingIndex == index) null else spring(stiffness = Spring.StiffnessMediumLow),
                ).zIndex(if (draggingIndex == index) 1f else 0f)
                    .offset { IntOffset(0, if (draggingIndex == index) dragY.roundToInt() else 0) }) {
                CollectionTrackRow(song, null, true, client, onPlaySong,
                    editing = editing, dividerAbove = rowIndex > 0,
                    onMore = { selectedSong = IndexedValue(index, song) })
            }
        }
        if (searchQuery.isNotBlank() && visibleRows.isEmpty()) item {
            Text("No matching songs", color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(22.dp))
        }
        if (recommendations.isNotEmpty() && searchQuery.isBlank()) {
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
                        Text(song.artist, maxLines = 1, overflow = TextOverflow.Clip,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            softWrap = false,
                            modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
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
    selectedSong?.let { (index, song) ->
        CollectionSongSheet(song, client, onDismiss = { selectedSong = null },
            onPlayNext = { onAddNext(song) }, onAlbum = onAlbum, onArtist = onArtist,
            onRemoveFromPlaylist = if (editing || saving) null else {{
                selectedSong = null
                scope.launch {
                    if (index !in orderedSongs.indices || orderedSongs[index].id != song.id) {
                        snackbar.showSnackbar("Playlist changed; open the song menu again")
                        return@launch
                    }
                    try {
                        client.removeSongFromPlaylist(playlist.id, index)
                        orderedSongs = orderedSongs.toMutableList().apply { removeAt(index) }
                        onPlaylistChanged()
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { snackbar.showSnackbar("Could not remove ${song.title}") }
                }
            }})
    }
}

@Composable
private fun CollectionSurface(
    title: String, artworkId: String?, darkMode: Boolean,
    onArtworkColor: suspend (String?) -> Color,
    onBack: () -> Unit, onShare: () -> Unit,
    listState: LazyListState = rememberLazyListState(), listModifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null, editing: Boolean = false,
    onCancel: (() -> Unit)? = null, onDone: (() -> Unit)? = null,
    snackbar: SnackbarHostState? = null,
    onPullAtTop: (() -> Unit)? = null, onScrollAway: (() -> Unit)? = null,
    headerItemIndex: Int = 1,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val titleThreshold = with(LocalDensity.current) { 160.dp.roundToPx() }
    val titleIsPast by remember(listState, headerItemIndex, titleThreshold) { derivedStateOf {
        listState.firstVisibleItemIndex > headerItemIndex ||
            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == headerItemIndex }
                ?.let { it.offset + it.size <= titleThreshold } == true
    } }
    var sampled by remember(artworkId) { mutableStateOf(Color(0xFF626262)) }
    LaunchedEffect(artworkId) {
        sampled = try { onArtworkColor(artworkId) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { Color(0xFF626262) }
    }
    val (base, glow) = collectionBackdropColors(sampled, darkMode)
    val barColor by animateColorAsState(
        if (titleIsPast) base else Color.Transparent,
        animationSpec = tween(240, easing = FastOutSlowInEasing), label = "Collection bar color")
    val sky = rememberSky()
    val pullAction by rememberUpdatedState(onPullAtTop)
    val hideAction by rememberUpdatedState(onScrollAway)
    val pullDistance = with(LocalDensity.current) { 48.dp.toPx() }
    val hideDistance = with(LocalDensity.current) { 24.dp.toPx() }
    val scrollConnection = remember(listState, pullDistance, hideDistance) {
        object : NestedScrollConnection {
            var pull = 0f
            var away = 0f
            override fun onPostScroll(consumed: Offset, available: Offset,
                source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput &&
                    !listState.canScrollBackward && available.y > 0f) {
                    pull += available.y
                    away = 0f
                    if (pull >= pullDistance) { pullAction?.invoke(); pull = 0f }
                } else if (source == NestedScrollSource.UserInput && consumed.y < 0f) {
                    away -= consumed.y
                    pull = 0f
                    if (away >= hideDistance && listState.firstVisibleItemIndex <= headerItemIndex) {
                        hideAction?.invoke(); away = 0f
                    }
                } else if (source == NestedScrollSource.UserInput) {
                    pull = 0f
                    away = 0f
                }
                return Offset.Zero
            }
        }
    }
    Box(Modifier.fillMaxSize().background(base)) {
        Box(Modifier.fillMaxSize().sky(sky)) {
            Box(Modifier.fillMaxSize().drawWithCache {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                val header = visibleItems.firstOrNull { it.index == headerItemIndex }
                val glowY = when {
                    header != null -> header.offset +
                        (size.width * 0.68f).coerceAtMost(330.dp.toPx()) * 0.5f
                    visibleItems.isEmpty() -> 290.dp.toPx()
                    else -> -size.width
                }
                val brush = Brush.radialGradient(
                    0f to glow, 1f to base,
                    center = Offset(size.width * 0.5f, glowY),
                    radius = size.width * 0.86f,
                )
                onDrawBehind { drawRect(brush) }
            })
            LazyColumn(modifier = listModifier.nestedScroll(scrollConnection), state = listState,
                contentPadding = PaddingValues(bottom = 220.dp)) {
                item { Spacer(Modifier.height(104.dp)) }
                content()
            }
        }
        Box(Modifier.fillMaxWidth().background(barColor).statusBarsPadding().padding(top = 8.dp)) {
            StickyTopBar(title, titleVisible = titleIsPast,
                onScrollTop = { scope.launch { listState.animateScrollToItem(0) } }, onBack = onBack,
                actionCount = if (editing) 2 else 1 + (if (onEdit != null) 1 else 0)) {
                if (editing) {
                    IconButton(onClick = { onCancel?.invoke() }) {
                        Icon(MaterialSymbols.RoundedFilled.Close, "Cancel edits",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { onDone?.invoke() }) {
                        Icon(MaterialSymbols.RoundedFilled.Check, "Save edits",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                } else {
                    IconButton(onClick = onShare) {
                        Icon(MaterialSymbols.RoundedFilled.Share, "Share",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                    if (onEdit != null) IconButton(onClick = onEdit) {
                        Icon(MaterialSymbols.RoundedFilled.Edit, "Edit playlist",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
        if (snackbar != null) SnackbarHost(snackbar,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 68.dp)) { data ->
            val shape = RoundedCornerShape(18.dp)
            Row(Modifier.fillMaxWidth().shadow(12.dp, shape).clip(shape)
                .cloudy(sky = sky, radius = 32,
                    tint = base.copy(alpha = 0.65f), shape = shape)
                .background(base.copy(alpha = 0.18f))
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.24f), shape)
                .padding(horizontal = 17.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(data.visuals.message, Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium)
                data.visuals.actionLabel?.let { label ->
                    Spacer(Modifier.width(12.dp))
                    Text(label, color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.clickable { data.performAction() }
                            .padding(horizontal = 5.dp, vertical = 7.dp))
                }
            }
        }
    }
}

internal fun collectionBackdropColors(sampled: Color, darkMode: Boolean): Pair<Color, Color> {
    val brightest = maxOf(sampled.red, sampled.green, sampled.blue)
    val darkest = minOf(sampled.red, sampled.green, sampled.blue)
    val nearBlack = brightest < 0.08f || (brightest < 0.16f && brightest - darkest < 0.035f)
    val base = if (darkMode && nearBlack) Color(0xFF080808)
        else if (darkMode) lerp(Color(0xFF101416), sampled, 0.55f)
        else lerp(Color(0xFFF9F9F9), sampled, 0.18f)
    val glow = if (darkMode && nearBlack) Color(0xFF292929)
        else if (darkMode) lerp(base, Color.White, 0.17f)
        else lerp(base, Color.White, 0.32f)
    return base to glow
}

@Composable
private fun CollectionHeader(
    title: String, subtitle: String, metadata: String?,
    artworkId: String?, client: SubsonicClient,
    lossless: Boolean = false,
    onSubtitleClick: (() -> Unit)? = null,
) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val coverSize = (maxWidth * 0.68f).coerceAtMost(330.dp)
        CollectionArtwork(client, artworkId,
            Modifier.size(coverSize).shadow(18.dp, RoundedCornerShape(8.dp)))
    }
    Spacer(Modifier.height(19.dp))
    Text(title, color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
    Spacer(Modifier.height(3.dp))
    Text(subtitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
        style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center,
        maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth().then(if (onSubtitleClick != null)
            Modifier.clickable(onClick = onSubtitleClick) else Modifier)
            .padding(horizontal = 20.dp))
    if (metadata != null || lossless) {
        Spacer(Modifier.height(5.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            val color = MaterialTheme.colorScheme.onSurfaceVariant
            if (metadata != null) {
                Text(metadata, color = color, style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false))
            }
            if (lossless) {
                if (metadata != null) Text(" · ", color = color,
                    style = MaterialTheme.typography.bodySmall)
                Icon(painterResource(Res.drawable.am_lossless), contentDescription = null,
                    tint = color, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(3.dp))
                Text("Lossless", color = color, style = MaterialTheme.typography.bodySmall)
            }
        }
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
internal fun CollectionControls(
    darkMode: Boolean, onShuffle: () -> Unit, onPlay: () -> Unit,
    favorite: Boolean? = null, onFavorite: (() -> Unit)? = null,
    favoriteEnabled: Boolean = true, favoriteLabel: String = "album",
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
                .clickable(enabled = favoriteEnabled, onClick = onFavorite), contentAlignment = Alignment.Center) {
                Icon(if (favorite) MaterialSymbols.RoundedFilled.Favorite else MaterialSymbols.Rounded.FavoriteOutline,
                    if (favorite) "Unfavorite $favoriteLabel" else "Favorite $favoriteLabel",
                    tint = if (favorite) favoriteRed.copy(alpha = if (favoriteEnabled) 1f else 0.45f)
                        else ink.copy(alpha = if (favoriteEnabled) 0.65f else 0.25f))
            }
        } else Spacer(Modifier.size(51.dp))
    }
}

@Composable
private fun CollectionDivider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 17.dp).height(1.dp)
        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f)))
}

internal fun isDeliberateSwipe(offset: Float, width: Float): Boolean =
    width > 0f && abs(offset) >= width * 0.70f

internal class SwipeActionLatch {
    private var fired = false
    fun reset() { fired = false }
    fun take(): Boolean = if (fired) false else { fired = true; true }
}

@Composable
internal fun rememberDeliberateDismissState(
    onDismiss: (SwipeToDismissBoxValue) -> Unit,
): Pair<SwipeToDismissBoxState, Modifier> {
    var width by remember { mutableFloatStateOf(0f) }
    var stateRef by remember { mutableStateOf<SwipeToDismissBoxState?>(null) }
    val latch = remember { SwipeActionLatch() }
    val currentAction by rememberUpdatedState(onDismiss)
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            // Fast flicks can bypass positionalThreshold; require an intentional full-width drag.
            if (value != SwipeToDismissBoxValue.Settled &&
                isDeliberateSwipe(stateRef?.requireOffset() ?: 0f, width) && latch.take())
                currentAction(value)
            false
        },
        positionalThreshold = { it * 0.82f },
    )
    SideEffect { stateRef = state }
    return state to Modifier.onSizeChanged { width = it.width.toFloat() }
        .pointerInput(state) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                latch.reset()
                var pressed: Boolean
                do {
                    pressed = awaitPointerEvent().changes.any { it.pressed }
                } while (pressed)
            }
        }
}

@Composable
private fun CollectionSwipeRow(
    canRemove: Boolean, enabled: Boolean = true,
    allowPlayNext: Boolean = true,
    onPlayNext: () -> Unit, onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier, content: @Composable () -> Unit,
) {
    val playNextAction by rememberUpdatedState(onPlayNext)
    val removeAction by rememberUpdatedState(onRemove)
    val playNextAllowed by rememberUpdatedState(allowPlayNext)
    val removeAllowed by rememberUpdatedState(canRemove)
    val (dismiss, swipeModifier) = rememberDeliberateDismissState { value ->
        when (value) {
            SwipeToDismissBoxValue.StartToEnd -> if (playNextAllowed) playNextAction()
            SwipeToDismissBoxValue.EndToStart -> if (removeAllowed) removeAction?.invoke()
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }
    SwipeToDismissBox(
        state = dismiss, modifier = modifier.then(swipeModifier),
        enableDismissFromStartToEnd = enabled && allowPlayNext,
        enableDismissFromEndToStart = enabled && canRemove,
        backgroundContent = {
            when (dismiss.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Row(
                    Modifier.fillMaxSize().background(Color(0xFF6937B8)).padding(start = 20.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(MaterialSymbols.RoundedFilled.Queue_music, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Play next", color = Color.White)
                }
                SwipeToDismissBoxValue.EndToStart -> Row(
                    Modifier.fillMaxSize().background(Color(0xFFB51529)).padding(end = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Remove", color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Icon(MaterialSymbols.RoundedFilled.Delete, null, tint = Color.White)
                }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
    ) {
        Box(Modifier.fillMaxWidth().background(
            if (dismiss.dismissDirection != SwipeToDismissBoxValue.Settled)
                MaterialTheme.colorScheme.background else Color.Transparent)) { content() }
    }
}

@Composable
private fun CollectionTrackRow(
    song: Song, trackNumber: Int?, showArtist: Boolean, client: SubsonicClient,
    onPlay: (Song) -> Unit, editing: Boolean = false,
    dividerAbove: Boolean = true, onMore: () -> Unit,
) {
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
    if (dividerAbove) Box(Modifier.fillMaxWidth()
        .padding(start = if (showArtist) 76.dp else 55.dp, end = 18.dp).height(1.dp)
        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)))
    Row(Modifier.fillMaxWidth().height(62.dp)
        .then(if (editing) Modifier else Modifier.clickable { onPlay(song) })
        .padding(start = 20.dp, end = 9.dp),
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
                    maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                    modifier = if (showArtist) Modifier.weight(1f)
                        .basicMarquee(iterations = Int.MAX_VALUE) else Modifier)
            }
        }
        if (editing) Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(MaterialSymbols.RoundedFilled.Drag_handle, "Drag ${song.title}", tint = quiet)
        } else IconButton(onClick = onMore) {
            Icon(MaterialSymbols.RoundedFilled.More_vert, "Options for ${song.title}", tint = quiet)
        }
    }
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
                Column(Modifier.heightIn(min = 66.dp * LocalDensity.current.fontScale.coerceAtLeast(1f))) {
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
            Column(Modifier.width(148.dp)) {
                Box(Modifier.size(148.dp).clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)))
                Spacer(Modifier.height(7.dp))
                Spacer(Modifier.height(66.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumArtistSheet(
    artists: List<Artist>, artworkId: String?, client: SubsonicClient,
    onDismiss: () -> Unit, onArtist: (Artist) -> Unit,
) {
    var choices by remember(artists) { mutableStateOf(artists) }
    LaunchedEffect(artists, client) {
        val library = try { client.artists().associateBy { it.id } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { emptyMap() }
        choices = artists.map { library[it.id] ?: it }
        choices.filter { it.imageUrl == null }.forEach { artist ->
            val url = try { client.artistImageUrl(artist.id) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { null }
            if (url != null) choices = choices.map {
                if (it.id == artist.id) it.copy(imageUrl = url) else it
            }
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        sheetGesturesEnabled = false, containerColor = Color.Transparent,
        contentColor = Color.White, scrimColor = Color.Black.copy(alpha = 0.28f),
        dragHandle = null, contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) {
        PlayerSheetSurface(artworkId?.let { client.coverArtUrl(it, 600) }, Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp)) {
                SheetHandle()
                Text("GO TO ARTIST", color = Color.LightGray,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
                    modifier = Modifier.padding(start = 26.dp, top = 22.dp, bottom = 12.dp))
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.09f))) {
                    choices.forEach { artist ->
                        CollectionArtistOptionRow(artist) { onArtist(artist) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionSongSheet(
    song: Song, client: SubsonicClient,
    onDismiss: () -> Unit, onPlayNext: (() -> Unit)?,
    onAlbum: ((Album) -> Unit)? = null, onArtist: (Artist) -> Unit,
    albumArtists: List<Artist> = emptyList(),
    onShare: ((Song) -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    var showPlaylists by remember(song.id) { mutableStateOf(false) }
    var showArtists by remember(song.id) { mutableStateOf(false) }
    var artistChoices by remember(song.id) { mutableStateOf(emptyList<Artist>()) }
    var playlists by remember(song.id) { mutableStateOf(emptyList<Playlist>()) }
    var loading by remember(song.id) { mutableStateOf(false) }
    var message by remember(client, song.id) { mutableStateOf<String?>(null) }
    var favorite by remember(client, song.id) { mutableStateOf(song.starred) }
    var favoriteLoading by remember(client, song.id) { mutableStateOf(true) }
    var favoritePending by remember(client, song.id) { mutableStateOf(false) }
    val currentSong by rememberUpdatedState(client to song.id)
    LaunchedEffect(client, song.id) {
        try { favorite = client.songById(song.id)?.starred ?: favorite }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { }
        finally { favoriteLoading = false }
    }
    LaunchedEffect(client, song.id, albumArtists) {
        artistChoices = try {
            client.songArtists(song.id)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { emptyList() }
        artistChoices = artistChoices.ifEmpty { albumArtists }
        if (artistChoices.isEmpty() && song.artistId != null)
            artistChoices = listOf(Artist(song.artistId, song.artist))
    }
    LaunchedEffect(showArtists, client, song.id) {
        if (showArtists) {
            val library = try { client.artists().associateBy { it.id } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { emptyMap() }
            artistChoices = artistChoices.map { library[it.id] ?: it }
            artistChoices.filter { it.imageUrl == null }.forEach { artist ->
                val url = try { client.artistImageUrl(artist.id) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { null }
                if (url != null) artistChoices = artistChoices.map {
                    if (it.id == artist.id) it.copy(imageUrl = url) else it
                }
            }
        }
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
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
                    Text(song.artist, color = Color.LightGray, maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE),
                        style = MaterialTheme.typography.bodySmall)
                    Text(song.album, color = Color.LightGray.copy(alpha = 0.82f), maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE),
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
                    CollectionArtistOptionRow(artist) {
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
                SongOptionRow(if (favorite) MaterialSymbols.RoundedFilled.Favorite
                    else MaterialSymbols.Rounded.FavoriteOutline,
                    if (favoriteLoading) "Checking favorite…" else if (favoritePending) "Updating favorite…"
                    else if (favorite) "Remove from favorites" else "Add to favorites",
                    iconTint = if (favorite) favoriteRed else Color.LightGray) {
                    if (!favoriteLoading && !favoritePending) {
                        val before = favorite
                        favorite = !before
                        favoritePending = true
                        message = null
                        scope.launch {
                            try {
                                client.setSongStarred(song.id, !before)
                                if (currentSong == (client to song.id)) onDismiss()
                            }
                            catch (cancelled: CancellationException) { favorite = before; throw cancelled }
                            catch (_: Exception) { favorite = before; message = "Could not update favorite" }
                            finally { favoritePending = false }
                        }
                    }
                }
                SongOptionRow(MaterialSymbols.RoundedFilled.Playlist_add, "Add to playlist") {
                    showPlaylists = true
                }
                if (onPlayNext != null) SongOptionRow(MaterialSymbols.RoundedFilled.Queue_music, "Play next") {
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
                if (onShare != null) SongOptionRow(MaterialSymbols.RoundedFilled.Share, "Share") {
                    onDismiss(); onShare(song)
                }
                if (onRemoveFromPlaylist != null)
                    SongOptionRow(MaterialSymbols.RoundedFilled.Delete, "Remove from playlist",
                        iconTint = favoriteRed) { onRemoveFromPlaylist() }
                if (message != null) Text(message!!, color = Color.LightGray)
            }
            }
        }
        }
    }
}

@Composable
private fun CollectionArtistOptionRow(artist: Artist, onClick: () -> Unit) {
    val imageUrl = artist.imageUrl
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(horizontal = 26.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            if (imageUrl != null) AsyncImage(imageUrl, "${artist.name} portrait",
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Icon(MaterialSymbols.RoundedFilled.Person, null,
                tint = Color.LightGray, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(artist.name, color = Color.White,
            style = MaterialTheme.typography.bodyLarge, maxLines = 1,
            overflow = TextOverflow.Ellipsis)
    }
}
