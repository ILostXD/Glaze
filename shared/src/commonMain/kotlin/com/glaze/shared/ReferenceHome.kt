package com.glaze.shared

import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Favorite as FavoriteOutline
import com.composables.icons.materialsymbols.roundedfilled.Favorite
import com.composables.icons.materialsymbols.roundedfilled.Chevron_right
import com.composables.icons.materialsymbols.roundedfilled.Home
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Library_music
import com.composables.icons.materialsymbols.roundedfilled.Pause
import com.composables.icons.materialsymbols.roundedfilled.Play_arrow
import com.composables.icons.materialsymbols.roundedfilled.Playlist_play
import com.composables.icons.materialsymbols.roundedfilled.Search
import com.composables.icons.materialsymbols.roundedfilled.Skip_next
import com.composables.icons.materialsymbols.roundedfilled.Tune
import com.composables.icons.materialsymbols.roundedfilled.Drag_handle
import com.composables.icons.materialsymbols.roundedfilled.Shuffle

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Switch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.glaze.shared.resources.Res
import com.glaze.shared.resources.glaze_wordmark
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.cloudy
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ReferenceHomeScreen(
    albums: List<Album>,
    recentAlbums: List<Album>,
    playlists: List<Playlist>,
    client: SubsonicClient,
    darkMode: Boolean,
    loading: Boolean,
    error: String?,
    sections: List<HomeShelf>,
    bottomPadding: Dp,
    onAlbum: (Album) -> Unit,
    onPlaylist: (Playlist) -> Unit,
    onSection: (HomeSection) -> Unit,
    onPlaylists: () -> Unit,
    isFavorite: (Playlist) -> Boolean,
    onFavorite: (Playlist, Boolean) -> Unit,
    onSettings: () -> Unit,
    onCustomize: () -> Unit,
    profileAvatar: ImageBitmap? = null,
    refreshing: Boolean = false, onRefresh: () -> Unit = {},
    downloading: Boolean = false,
    upcoming: List<UpcomingAlbum> = emptyList(), onUpcoming: (UpcomingAlbum) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val ink = colors.onBackground
    val muted = colors.onSurfaceVariant
    val newAlbums = albums.take(12)
    val returningAlbums = recentAlbums.distinctBy { it.id }
    // Keep lazy measurement independent of which preview cards are currently visible.
    val mixHeight = 212.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    PullToRefreshBox(isRefreshing = refreshing, onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize().background(colors.background).statusBarsPadding()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(top = 20.dp, bottom = bottomPadding),
        ) {
            item("toolbar") { AppToolbar(null, client.credentials.username, darkMode, onSettings,
                profileAvatar = profileAvatar,
                downloading = downloading,
                onScrollTop = { scope.launch { listState.animateScrollToItem(0) } }) }
            if (error != null) item("error") { HomeNotice(error) }
            sections.forEach { section -> when (section) {
                HomeShelf.Upcoming -> if (upcoming.isNotEmpty()) {
                    item("upcoming-heading") { HomeSectionHeading(section.title, "From your favorite artists") }
                    item("upcoming-albums") {
                        LazyRow(contentPadding = PaddingValues(horizontal = 22.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(upcoming, key = { it.id }) { album ->
                                Column(Modifier.width(176.dp).clickable { onUpcoming(album) }) {
                                    coil3.compose.AsyncImage(album.cover, album.title,
                                        Modifier.size(176.dp).clip(RoundedCornerShape(16.dp)))
                                    Spacer(Modifier.height(9.dp))
                                    Text(album.title, color = ink, style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(artistLabel(album.artist), color = muted, style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(if (album.saved) "Pre-Saved · ${readableReleaseDate(album.releaseDate)}"
                                        else readableReleaseDate(album.releaseDate), color = muted,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                HomeShelf.Mixes -> {
                    item("mix-heading") {
                        HomeSectionHeading(section.title, "Mix previews · personalization coming later")
                    }
                    item("mixes") {
                        LazyRow(Modifier.height(mixHeight), contentPadding = PaddingValues(horizontal = 22.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(homeMixPreviews, key = { it.title }) {
                                MixPreviewCard(it, Modifier.width(176.dp).height(mixHeight))
                            }
                        }
                    }
                }
                HomeShelf.NewLibrary -> {
                    item("new-heading") {
                        HomeSectionHeading(section.title, "Your latest additions") {
                            onSection(HomeSection.NewLibrary)
                        }
                    }
                    if (newAlbums.isEmpty()) item("new-empty") {
                        HomeNotice(if (loading) "Loading your library…" else "New additions will appear here.")
                    } else item("new-albums") {
                        HomeAlbumCarousel(newAlbums, client, onAlbum)
                    }
                }
                HomeShelf.Playlists -> {
                    item("playlist-heading") {
                        HomeSectionHeading(section.title,
                            if (playlists.any(isFavorite)) "Your favorites, always first" else "Keep your favorites close",
                            onSeeAll = onPlaylists)
                    }
                    if (playlists.isEmpty()) item("playlist-empty") {
                        HomeNotice(if (loading) "Loading your playlists…" else "Playlists from your server will appear here.")
                    } else item("playlists") {
                        LazyRow(contentPadding = PaddingValues(horizontal = 22.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(playlists.take(12), key = { it.id }) { playlist ->
                                Column(Modifier.width(154.dp).clickable { onPlaylist(playlist) }) {
                                    Box {
                                        AlbumImage(client, playlist.coverArt,
                                            Modifier.size(154.dp).clip(RoundedCornerShape(16.dp)))
                                        FavoritePlaylistButton(playlist.name, isFavorite(playlist),
                                            Modifier.align(Alignment.TopEnd).padding(6.dp)
                                                .clip(CircleShape).background(colors.background.copy(alpha = 0.82f)),
                                            onCheckedChange = { onFavorite(playlist, it) })
                                    }
                                    Spacer(Modifier.height(9.dp))
                                    Text(playlist.name, color = ink, style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${playlist.songCount} songs", color = muted, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                HomeShelf.Recent -> if (returningAlbums.isNotEmpty()) {
                    item("recent-heading") {
                        HomeSectionHeading(section.title, "From your listening history") {
                            onSection(HomeSection.Recent)
                        }
                    }
                    item("recent-albums") {
                        LazyRow(contentPadding = PaddingValues(horizontal = 22.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(returningAlbums.take(12), key = { it.id }) { album ->
                                Column(Modifier.width(154.dp).clickable { onAlbum(album) }) {
                                    AlbumImage(client, album.coverArt,
                                        Modifier.size(154.dp).clip(RoundedCornerShape(16.dp)))
                                    HomeAlbumCaption(album, modifier = Modifier.heightIn(
                                        min = 68.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)))
                                }
                            }
                        }
                    }
                }
            } }
            item("customize") {
                TextButton(onClick = onCustomize, modifier = Modifier.padding(start = 14.dp, top = 12.dp)) {
                    Icon(MaterialSymbols.RoundedFilled.Tune, null, tint = ink, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Customize Home", color = ink, style = MaterialTheme.typography.labelLarge,
                        textDecoration = TextDecoration.Underline)
                    Spacer(Modifier.width(6.dp))
                    Icon(MaterialSymbols.RoundedFilled.Chevron_right, null, tint = ink,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
internal fun ProfileAvatar(name: String, avatar: ImageBitmap?, size: Dp, modifier: Modifier = Modifier,
    darkMode: Boolean = MaterialTheme.colorScheme.background == Color.Black) {
    Box(modifier.size(size).clip(CircleShape).background(if (darkMode) Color.White else Color.Black),
        contentAlignment = Alignment.Center) {
        if (avatar != null) Image(avatar, "$name's profile picture", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text(name.take(2).uppercase(), color = if (darkMode) Color.Black else Color.White,
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun AppToolbar(title: String?, username: String, darkMode: Boolean,
                        onSettings: (() -> Unit)?, onBack: (() -> Unit)? = null,
                        profileAvatar: ImageBitmap? = null,
                        downloading: Boolean = false,
                        searchField: (@Composable () -> Unit)? = null,
                        onScrollTop: () -> Unit) {
    val ink = MaterialTheme.colorScheme.onBackground
    val avatar: @Composable () -> Unit = {
        Box(Modifier.size(48.dp).clip(CircleShape)
            .semantics { contentDescription = "Open settings" }
            .then(if (onSettings != null) Modifier.clickable(onClick = onSettings) else Modifier),
            contentAlignment = Alignment.Center) {
            ProfileAvatar(username, profileAvatar, 40.dp, darkMode = darkMode)
            if (downloading) CircularProgressIndicator(Modifier.fillMaxSize()
                .semantics { contentDescription = "Download in progress" },
                color = if (darkMode) Color.White else Color.Black, strokeWidth = 2.dp)
        }
    }
    if (title != null && searchField == null) {
        StickyTopBar(title, onScrollTop, onBack,
            actionCount = if (onBack == null) 1 else 0) {
            if (onBack == null) avatar()
        }
        return
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp).heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (searchField != null) Box(Modifier.weight(1f)) { searchField() }
        else Image(
            painter = painterResource(Res.drawable.glaze_wordmark),
            contentDescription = "Glaze",
            modifier = Modifier.weight(1f).height(43.dp).clickable(onClick = onScrollTop),
            alignment = Alignment.CenterStart,
            colorFilter = ColorFilter.tint(ink),
        )
        Spacer(Modifier.width(12.dp))
        avatar()
    }
}

private data class HomeMixPreview(val title: String, val subtitle: String, val colors: List<Color>)

private val homeMixPreviews = listOf(
    HomeMixPreview("Daily Mix", "Your taste, mixed together", listOf(Color(0xFF247F81), Color(0xFF103537))),
    HomeMixPreview("On Repeat", "The songs you keep coming back to", listOf(Color(0xFF8254A5), Color(0xFF30213F))),
    HomeMixPreview("After Hours", "A slower soundtrack for late nights", listOf(Color(0xFF435D9A), Color(0xFF1B2441))),
    HomeMixPreview("Discovery Mix", "A fresh direction for your next listen", listOf(Color(0xFFA65362), Color(0xFF44242D))),
)

@Composable
private fun MixPreviewCard(mix: HomeMixPreview, modifier: Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Box(modifier.clip(shape)
        .background(Brush.linearGradient(mix.colors))
        .border(1.dp, Color.White.copy(alpha = 0.16f), shape)) {
        Canvas(Modifier.fillMaxSize().blur(20.dp)) {
            val center = Offset(size.width * 0.85f, size.height * 0.30f)
            val radius = size.width * 0.54f
            drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                center = center, radius = radius), radius, center)
            repeat(6) { ring ->
                drawCircle(Color.White.copy(alpha = 0.12f), radius * (0.55f + ring * 0.09f), center,
                    style = Stroke(1.dp.toPx()))
            }
            drawCircle(Color.White.copy(alpha = 0.15f), radius * 0.08f, center)
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(Res.drawable.glaze_wordmark), contentDescription = null,
                modifier = Modifier.width(50.dp).height(18.dp), colorFilter = ColorFilter.tint(Color.White))
            Spacer(Modifier.weight(1f))
            Text("Preview", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp,
                modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp))
        }
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(mix.title, color = Color.White, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(mix.subtitle, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun HomeSectionHeading(title: String, subtitle: String, onSeeAll: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 16.dp, top = 24.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.onBackground, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(artistLabel(subtitle), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        if (onSeeAll != null) IconButton(onClick = onSeeAll) {
            Icon(MaterialSymbols.RoundedFilled.Chevron_right, "See all $title", tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun HomeNotice(message: String) {
    val colors = MaterialTheme.colorScheme
    Text(message, color = colors.onSurfaceVariant, fontSize = 14.sp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp)).background(colors.onBackground.copy(alpha = 0.06f))
            .padding(16.dp))
}

@Composable
internal fun FavoritePlaylistButton(
    name: String, favorite: Boolean, modifier: Modifier = Modifier, onCheckedChange: (Boolean) -> Unit,
) {
    IconToggleButton(checked = favorite, onCheckedChange = onCheckedChange, modifier = modifier) {
        Icon(if (favorite) MaterialSymbols.RoundedFilled.Favorite else MaterialSymbols.Rounded.FavoriteOutline,
            if (favorite) "Unfavorite $name" else "Favorite $name",
            tint = if (favorite) favoriteRed else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(22.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeAlbumCarousel(albums: List<Album>, client: SubsonicClient, onAlbum: (Album) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val coverSize = (maxWidth - 72.dp).coerceAtMost(360.dp)
        HorizontalUncontainedCarousel(
            state = rememberCarouselState { albums.size },
            itemWidth = coverSize,
            modifier = Modifier.fillMaxWidth().height(coverSize),
            itemSpacing = 14.dp,
            contentPadding = PaddingValues(horizontal = 22.dp),
        ) { index ->
            val album = albums[index]
            Box(Modifier.fillMaxWidth().height(coverSize).maskClip(RoundedCornerShape(22.dp))
                .clickable { onAlbum(album) }) {
                AlbumImage(client, album.coverArt, Modifier.fillMaxSize())
                Box(Modifier.fillMaxWidth().height(112.dp * LocalDensity.current.fontScale)
                    .align(Alignment.BottomCenter).background(Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text(album.name, color = Color.White, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(artistLabel(album.artist), color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun HomeAlbumCaption(album: Album, subtitle: String = album.artist, modifier: Modifier = Modifier) {
    Column(modifier) {
        Spacer(Modifier.height(9.dp))
        Text(album.name, color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(artistLabel(subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun HomeAlbumGrid(albums: List<Album>, client: SubsonicClient, bottomPadding: Dp,
    gridState: LazyGridState = rememberLazyGridState(), showYear: Boolean = false,
    columns: Int, onColumns: (Int) -> Unit,
    onShuffle: (() -> Unit)? = null, onAlbum: (Album) -> Unit) {
    var sort by rememberSaveable { mutableStateOf(AlbumSort.Library) }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val visible = remember(albums, sort, searchQuery) { sortAlbums(albums.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true)
    }, sort) }
    val searchConnection = rememberPullSearchConnection({ gridState.canScrollBackward },
        { searchVisible = true }, { searchVisible = false })
    LazyVerticalGrid(columns = GridCells.Fixed(columns), state = gridState,
        modifier = Modifier.fillMaxSize().nestedScroll(searchConnection),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = bottomPadding),
        horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(key = "controls", span = { GridItemSpan(maxLineSpan) }) {
            Column {
            if (onShuffle != null) Button(onShuffle, enabled = albums.isNotEmpty(), shape = CircleShape,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background)) {
                Icon(MaterialSymbols.RoundedFilled.Shuffle, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Shuffle", fontWeight = FontWeight.SemiBold)
            }
            if (searchVisible) LibrarySearchField(searchQuery, { searchQuery = it }, "Find an album")
            BrowserControls(columns, onColumns, sort,
                when (sort) { AlbumSort.Library -> "Library"; AlbumSort.Name -> "A–Z"
                    AlbumSort.NameReverse -> "Z–A"; AlbumSort.Newest -> "Newest"; AlbumSort.Oldest -> "Oldest" },
                listOf(AlbumSort.Library to "Library order", AlbumSort.Name to "Name A–Z",
                    AlbumSort.NameReverse to "Name Z–A", AlbumSort.Newest to "Newest releases",
                    AlbumSort.Oldest to "Oldest releases"), { sort = it }, "Sort albums")
            }
        }
        if (visible.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
            HomeNotice(if (searchQuery.isBlank()) "No albums in this section yet." else "No matching albums")
        }
        gridItems(visible, key = { it.id }) { album ->
            val subtitle = if (showYear) album.year?.toString().orEmpty() else album.artist
            if (columns == 1) Row(Modifier.fillMaxWidth().clickable { onAlbum(album) },
                verticalAlignment = Alignment.CenterVertically) {
                AlbumImage(client, album.coverArt, Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(album.name, color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(artistLabel(subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else Column(Modifier.clickable { onAlbum(album) }) {
                AlbumImage(client, album.coverArt, Modifier.fillMaxWidth().aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)))
                HomeAlbumCaption(album, subtitle)
            }
        }
    }
}

@Composable
internal fun HomeCustomizationScreen(settings: AppSettings, onChange: ((AppSettings) -> AppSettings) -> Unit,
    bottomPadding: Dp, listState: LazyListState) {
    val colors = MaterialTheme.colorScheme
    val saved by rememberUpdatedState(settings.homeSections)
    val hidden by rememberUpdatedState(settings.hiddenHomeSections)
    val update by rememberUpdatedState(onChange)
    var preview by remember { mutableStateOf<List<HomeShelf>?>(null) }
    var dragShelf by remember { mutableStateOf<HomeShelf?>(null) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val order = preview ?: saved
    val rowHeight = 76.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
    val rowHeightPx = with(LocalDensity.current) { rowHeight.toPx() }
    LazyColumn(state = listState, contentPadding = PaddingValues(start = 22.dp, end = 22.dp,
        top = 12.dp, bottom = bottomPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item("help") {
            Text("Make Home yours", color = colors.onBackground,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Hold a handle to reorder. Use the switches to show or hide sections. Changes save automatically.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp, bottom = 14.dp))
        }
        items(order, key = { it.name }) { shelf ->
            val shown = shelf !in hidden
            val shape = RoundedCornerShape(20.dp)
            Row(Modifier.animateItem(placementSpec = if (dragShelf == shelf) null else androidx.compose.animation.core.spring())
                .zIndex(if (dragShelf == shelf) 1f else 0f)
                .offset { IntOffset(0, if (dragShelf == shelf) dragY.roundToInt() else 0) }
                .fillMaxWidth().height(rowHeight).clip(shape)
                .background(colors.onBackground.copy(alpha = if (shown) 0.08f else 0.035f))
                .border(1.dp, colors.onBackground.copy(alpha = 0.1f), shape)
                .semantics {
                    if (shown) customActions = listOf(
                        CustomAccessibilityAction("Move ${shelf.title} up") {
                            update { it.copy(homeSections = moveHomeSection(it.homeSections, shelf, -1)) }; true
                        },
                        CustomAccessibilityAction("Move ${shelf.title} down") {
                            update { it.copy(homeSections = moveHomeSection(it.homeSections, shelf, 1)) }; true
                        })
                }.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(shelf.title, color = colors.onBackground,
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(if (shown) "Position ${order.indexOf(shelf) + 1}" else "Hidden",
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = shown, enabled = dragShelf == null,
                    modifier = Modifier.semantics { contentDescription = "Show ${shelf.title} on Home" },
                    onCheckedChange = { show -> update { it.copy(hiddenHomeSections =
                        if (show) it.hiddenHomeSections - shelf else it.hiddenHomeSections + shelf) } })
                Box(Modifier.size(48.dp).semantics { contentDescription = "Reorder ${shelf.title}" }
                    .then(if (shown) Modifier.pointerInput(shelf, rowHeightPx) {
                        var before = emptyList<HomeShelf>()
                        detectDragGesturesAfterLongPress(
                            onDragStart = { before = saved; preview = before; dragShelf = shelf; dragY = 0f },
                            onDrag = { change, amount ->
                                val currentPreview = preview
                                if (currentPreview != null && dragShelf == shelf) {
                                    var order: List<HomeShelf> = currentPreview
                                    dragY += amount.y
                                    val distance = rowHeightPx + 8.dp.toPx()
                                    var index = order.indexOf(shelf)
                                    while (dragY > distance / 2 && index < order.lastIndex) {
                                        order = moveHomeSection(order, shelf, 1)
                                        dragY -= distance
                                        index++
                                    }
                                    while (dragY < -distance / 2 && index > 0) {
                                        order = moveHomeSection(order, shelf, -1)
                                        dragY += distance
                                        index--
                                    }
                                    preview = order
                                    change.consume()
                                }
                            },
                            onDragEnd = {
                                val result = preview
                                if (result != null) update {
                                    if (it.homeSections == before) it.copy(homeSections = result) else it
                                }
                                dragShelf = null; preview = null; dragY = 0f
                            },
                            onDragCancel = { dragShelf = null; preview = null; dragY = 0f },
                        )
                    } else Modifier), contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbols.RoundedFilled.Drag_handle, null,
                        tint = colors.onSurfaceVariant.copy(alpha = if (shown) 1f else 0.25f))
                }
            }
        }
        item("reset") {
            TextButton(enabled = dragShelf == null,
                onClick = { update { it.copy(homeSections = HomeShelf.entries,
                    hiddenHomeSections = emptySet()) } }) {
                Text("Reset Home layout", color = colors.onBackground,
                    textDecoration = TextDecoration.Underline)
            }
        }
    }
}

internal fun miniPlayerHeight(size: MiniPlayerSize): Dp = when (size) {
    MiniPlayerSize.Small -> 52.dp; MiniPlayerSize.Medium -> 62.dp; MiniPlayerSize.Large -> 78.dp
}

internal fun navigationHeight(settings: AppSettings): Dp = when (settings.navigationSize) {
    NavigationSize.Small -> 55.dp; NavigationSize.Medium -> 65.dp; NavigationSize.Large -> 75.dp
} + if (settings.navigationLabels) 10.dp else 0.dp

@Composable
internal fun chromeContentPadding(settings: AppSettings, hasPlayer: Boolean): Dp =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 2.dp +
        navigationHeight(settings) + (if (hasPlayer) miniPlayerHeight(settings.miniPlayerSize) + 14.dp else 0.dp) + 8.dp

@Composable
internal fun BoxScope.ReferenceChrome(
    sky: Sky,
    client: SubsonicClient,
    song: Song?,
    isPlaying: Boolean,
    darkMode: Boolean,
    selectedTab: Int,
    settings: AppSettings,
    onHome: () -> Unit,
    onArtists: () -> Unit,
    onPlaylists: () -> Unit,
    onSearch: () -> Unit,
    onExpandPlayer: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onAlbum: (Album) -> Unit,
    onArtist: (Artist) -> Unit,
    onShareSong: (Song) -> Unit,
) {
    val pill = RoundedCornerShape(50)
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val intensity = settings.glassIntensity.coerceIn(0f, 1f)
    val tint = (if (darkMode) Color.Black else Color.White)
        .copy(alpha = 0.10f + intensity * 0.60f)
    val sheen = Brush.linearGradient(listOf(
        Color.White.copy(alpha = if (darkMode) 0.48f else 0.98f),
        Color.White.copy(alpha = if (darkMode) 0.06f else 0.30f),
    ))
    var quickSheetOpen by remember { mutableStateOf(false) }
    var swipeX by remember(song?.id) { mutableFloatStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { settings.gestures.sensitivityDp.dp.toPx() }
    val miniHeight = miniPlayerHeight(settings.miniPlayerSize)
    val coverSize = when (settings.miniPlayerSize) {
        MiniPlayerSize.Small -> 34.dp
        MiniPlayerSize.Medium -> 43.dp
        MiniPlayerSize.Large -> 54.dp
    }
    val navHeight = navigationHeight(settings)
    val navIconSize = when (settings.navigationSize) {
        NavigationSize.Small -> 23.dp
        NavigationSize.Medium -> 27.dp
        NavigationSize.Large -> 31.dp
    }
    val spotifyNav = settings.navigationStyle == NavigationStyle.Spotify
    val showSearchInNavigation = spotifyNav || settings.searchInNavigation
    val scrimColor = if (darkMode) Color.Black else Color.White
    val miniSurface = if (spotifyNav) {
        (if (darkMode) Color(0xFF151515) else Color.White).copy(alpha = 0.04f + intensity * 0.40f)
    } else Color.White.copy(alpha = 0.02f + intensity * (if (darkMode) 0.18f else 0.24f))
    if (spotifyNav) Box(
        Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(160.dp)
            .background(Brush.verticalGradient(
                0f to Color.Transparent,
                0.35f to scrimColor.copy(alpha = 0.38f),
                0.70f to scrimColor.copy(alpha = 0.76f),
                1f to scrimColor.copy(alpha = 0.95f),
            ))
    )
    Column(
        Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            .navigationBarsPadding().padding(start = 17.dp, end = 17.dp, top = 10.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (song != null) {
            Row(
                Modifier.fillMaxWidth().height(miniHeight)
                    .shadow(18.dp, pill).clip(pill)
                    .cloudy(sky = sky, radius = 44, tint = tint, shape = pill)
                    .background(miniSurface)
                    .border(1.dp, sheen, pill)
                    .then(if (settings.gestures.miniPlayerSwipe) Modifier.pointerInput(song.id, swipeThreshold) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, amount -> swipeX += amount; change.consume() },
                            onDragEnd = {
                                if (swipeX < -swipeThreshold) onNext()
                                else if (swipeX > swipeThreshold) onPrevious()
                                swipeX = 0f
                            },
                            onDragCancel = { swipeX = 0f },
                        )
                    } else Modifier)
                    .combinedClickable(onClick = onExpandPlayer,
                        onLongClick = if (settings.gestures.miniPlayerLongPress)
                            ({ quickSheetOpen = true }) else null)
                    .padding(start = if (settings.miniPlayerSize == MiniPlayerSize.Large) 16.dp else 14.dp,
                        end = if (settings.miniPlayerSize == MiniPlayerSize.Large) 12.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlbumImage(client, song.coverArt,
                    Modifier.size(coverSize).clip(RoundedCornerShape(
                        if (settings.miniPlayerSize == MiniPlayerSize.Large) 11.dp else 9.dp)))
                Spacer(Modifier.width(if (settings.miniPlayerSize == MiniPlayerSize.Large) 12.dp else 10.dp))
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = ink,
                        fontSize = if (settings.miniPlayerSize == MiniPlayerSize.Large) 16.sp else 13.sp,
                        fontWeight = FontWeight.W600, maxLines = 1,
                        softWrap = false, overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (song.isExplicit) {
                            ExplicitBadge(color = muted)
                            Spacer(Modifier.width(5.dp))
                        }
                        Text(artistLabel(song.artist), color = muted,
                            fontSize = if (settings.miniPlayerSize == MiniPlayerSize.Large) 14.sp else 11.sp,
                            maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                            modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE))
                    }
                }
                IconButton(onClick = onToggle) {
                    Icon(if (isPlaying) MaterialSymbols.RoundedFilled.Pause else MaterialSymbols.RoundedFilled.Play_arrow,
                        if (isPlaying) "Pause" else "Play", tint = ink,
                        modifier = Modifier.size(if (settings.miniPlayerSize == MiniPlayerSize.Large) 31.dp else 27.dp))
                }
                IconButton(onClick = onNext) {
                    Icon(MaterialSymbols.RoundedFilled.Skip_next, "Next", tint = ink,
                        modifier = Modifier.size(if (settings.miniPlayerSize == MiniPlayerSize.Large) 31.dp else 27.dp))
                }
            }
            if (quickSheetOpen) CollectionSongSheet(song, client,
                onDismiss = { quickSheetOpen = false }, onPlayNext = null,
                onAlbum = onAlbum, onArtist = onArtist, onShare = onShareSong)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                Modifier.weight(1f).height(navHeight)
                    .then(if (settings.navigationStyle == NavigationStyle.Glaze)
                        Modifier.shadow(18.dp, pill).clip(pill)
                            .cloudy(sky = sky, radius = 48, tint = tint, shape = pill)
                            .background(Color.White.copy(alpha = 0.02f + intensity * (if (darkMode) 0.12f else 0.20f)))
                            .border(1.dp, sheen, pill).padding(5.dp)
                    else Modifier),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val tabs = listOf(
                    Triple(MaterialSymbols.RoundedFilled.Home, "Home", onHome),
                    Triple(MaterialSymbols.RoundedFilled.Library_music, "Artists", onArtists),
                    Triple(MaterialSymbols.RoundedFilled.Playlist_play, "Playlists", onPlaylists),
                    Triple(MaterialSymbols.RoundedFilled.Search, "Search", onSearch),
                )
                val order = tabs.indices
                order.filter { showSearchInNavigation || it != 3 }.forEach { index ->
                    val tab = tabs[index]
                    Column(
                        Modifier.weight(1f).fillMaxSize().clip(pill)
                            .background(if (settings.navigationStyle == NavigationStyle.Glaze && selectedTab == index)
                                (if (darkMode) Color.White.copy(alpha = 0.15f)
                                 else Color.Black.copy(alpha = 0.10f))
                                else Color.Transparent)
                            .clickable(onClick = tab.third),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(tab.first, tab.second,
                            tint = if (selectedTab == index) ink else muted,
                            modifier = Modifier.size(navIconSize))
                        if (settings.navigationLabels) Text(tab.second,
                            color = if (selectedTab == index) ink else muted,
                            fontSize = (if (settings.navigationSize == NavigationSize.Large) 12 else 11).sp,
                            fontWeight = FontWeight.W500)
                    }
                }
            }
            if (!showSearchInNavigation) Box(
                Modifier.size(navHeight).shadow(18.dp, CircleShape).clip(CircleShape)
                    .cloudy(sky = sky, radius = 48, tint = tint, shape = CircleShape)
                    .background(Color.White.copy(alpha = 0.02f + intensity * (if (darkMode) 0.12f else 0.20f)))
                    .border(1.dp, sheen, CircleShape).clickable(onClick = onSearch),
                contentAlignment = Alignment.Center,
            ) {
                Icon(MaterialSymbols.RoundedFilled.Search, "Search",
                    tint = if (selectedTab == 3) ink else muted, modifier = Modifier.size(navIconSize))
            }
        }
    }
}

@Composable
internal fun AlbumImage(client: SubsonicClient, id: String?, modifier: Modifier = Modifier) {
    val url = remember(client, id) { id?.let { client.coverArtUrl(it, 600) } }
    Box(
        modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)),
    ) {
        if (url != null) AsyncImage(
            model = url, contentDescription = "Album artwork",
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
        )
    }
}
