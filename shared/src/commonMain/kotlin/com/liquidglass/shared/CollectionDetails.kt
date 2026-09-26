package com.liquidglass.shared

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
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
import com.composables.icons.materialsymbols.roundedfilled.Check
import com.composables.icons.materialsymbols.roundedfilled.Close
import com.composables.icons.materialsymbols.roundedfilled.Delete
import com.composables.icons.materialsymbols.roundedfilled.Drag_handle
import com.composables.icons.materialsymbols.roundedfilled.Edit
import com.composables.icons.materialsymbols.roundedfilled.Favorite
import com.composables.icons.materialsymbols.roundedfilled.More_vert
import com.composables.icons.materialsymbols.roundedfilled.Person
import com.composables.icons.materialsymbols.roundedfilled.Play_arrow
import com.composables.icons.materialsymbols.roundedfilled.Playlist_add
import com.composables.icons.materialsymbols.roundedfilled.Queue_music
import com.composables.icons.materialsymbols.roundedfilled.Share
import com.composables.icons.materialsymbols.roundedfilled.Search
import com.composables.icons.materialsymbols.roundedfilled.Shuffle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

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
                displayAlbum.coverArt ?: album.coverArt, client,
                onSubtitleClick = albumArtists.firstOrNull()?.let { artist -> { onArtist(artist) } })
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
            CollectionSwipeRow(false, onPlayNext = { onAddNext(song) }) {
                CollectionTrackRow(song, song.track, false, client, onPlaySong) { selectedSong = song }
            }
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
    val listState = rememberLazyListState()
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var recommendations by remember(playlist.id) { mutableStateOf(emptyList<Song>()) }
    var addingId by remember { mutableStateOf<String?>(null) }
    var orderedSongs by remember(playlist.id) { mutableStateOf(songs) }
    var editing by remember(playlist.id) { mutableStateOf(false) }
    var saving by remember(playlist.id) { mutableStateOf(false) }
    var editError by remember(playlist.id) { mutableStateOf<String?>(null) }
    var searchQuery by remember(playlist.id) { mutableStateOf("") }
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
        onEdit = { searchQuery = ""; editing = true; editError = null }, editing = editing,
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
        item { if (!editing) {
            BasicTextField(searchQuery, onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
                    .padding(horizontal = 15.dp, vertical = 13.dp),
                decorationBox = { field ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(MaterialSymbols.RoundedFilled.Search, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(10.dp))
                        Box {
                            if (searchQuery.isEmpty()) Text("Find in playlist",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            field()
                        }
                    }
                })
        } }
        item { Spacer(Modifier.height(32.dp)) }
        item {
            CollectionHeader(
                playlist.name, "Playlist",
                "${orderedSongs.size} songs · ${formatQueueDuration(orderedSongs.sumOf { it.durationSeconds.toLong() })}",
                artworkId, client,
            )
        }
        item { PlaylistControls(darkMode, onShuffle, onPlayAll) }
        if (editError != null) item {
            Text(editError!!, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp))
        }
        item { CollectionDivider() }
        items(visibleRows, key = { "playlist-track:${it.index}" }) { (index, song) ->
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
                    editing = editing, onMore = { selectedSong = song })
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
    listState: LazyListState = rememberLazyListState(), listModifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null, editing: Boolean = false,
    onCancel: (() -> Unit)? = null, onDone: (() -> Unit)? = null,
    snackbar: SnackbarHostState? = null,
    headerItemIndex: Int = 1,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    val titleIsPast by remember { derivedStateOf { listState.firstVisibleItemIndex > headerItemIndex } }
    var sampled by remember(artworkId) { mutableStateOf(Color(0xFF626262)) }
    LaunchedEffect(artworkId) {
        sampled = try { onArtworkColor(artworkId) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { Color(0xFF626262) }
    }
    val accent = if (sampled == Color.Black) Color(0xFF343A3C) else sampled
    val base = if (darkMode) lerp(Color(0xFF101416), accent, 0.38f)
        else lerp(Color(0xFFF9F9F9), accent, 0.18f)
    val glow = if (darkMode) lerp(base, accent, 0.60f)
        else lerp(base, accent, 0.32f)
    val barColor by animateColorAsState(
        if (titleIsPast) base else Color.Transparent,
        animationSpec = tween(140), label = "Collection bar color")
    Box(Modifier.fillMaxSize().background(base)) {
        Box(Modifier.fillMaxSize().drawWithCache {
            val brush = Brush.radialGradient(
                0f to glow, 1f to base,
                center = Offset(size.width * 0.5f, 290.dp.toPx()),
                radius = size.width * 0.86f,
            )
            onDrawBehind { drawRect(brush) }
        })
        LazyColumn(modifier = listModifier, state = listState,
            contentPadding = PaddingValues(bottom = 220.dp)) {
            item { Spacer(Modifier.height(104.dp)) }
            content()
        }
        Box(Modifier.fillMaxWidth().background(barColor).statusBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(MaterialSymbols.RoundedFilled.Arrow_back, "Back",
                    tint = MaterialTheme.colorScheme.onBackground)
            }
            Box(Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 112.dp),
                contentAlignment = Alignment.Center) {
                Crossfade(titleIsPast, animationSpec = tween(140), label = "Collection title") { visible ->
                    if (visible) Text(title, color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
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
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 190.dp))
    }
}

@Composable
private fun CollectionHeader(
    title: String, subtitle: String, metadata: String?,
    artworkId: String?, client: SubsonicClient,
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
    val dismiss = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> if (playNextAllowed) playNextAction()
                SwipeToDismissBoxValue.EndToStart -> if (removeAllowed) removeAction?.invoke()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
        positionalThreshold = { it * 0.82f },
    )
    SwipeToDismissBox(
        state = dismiss, modifier = modifier,
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
    onPlay: (Song) -> Unit, editing: Boolean = false, onMore: () -> Unit,
) {
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
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
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (editing) Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(MaterialSymbols.RoundedFilled.Drag_handle, "Drag ${song.title}", tint = quiet)
        } else IconButton(onClick = onMore) {
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
    LaunchedEffect(showArtists, client, song.id) {
        if (showArtists) {
            val library = try { client.artists().associateBy { it.id } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { emptyMap() }
            artistChoices = artistChoices.map { library[it.id] ?: it }
            artistChoices.filter { it.coverArt == null && it.imageUrl == null }.forEach { artist ->
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
                    CollectionArtistOptionRow(artist, client) {
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

@Composable
private fun CollectionArtistOptionRow(artist: Artist, client: SubsonicClient, onClick: () -> Unit) {
    val imageUrl = remember(client, artist.coverArt, artist.imageUrl) {
        artist.coverArt?.let { client.coverArtUrl(it, 160) } ?: artist.imageUrl
    }
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
