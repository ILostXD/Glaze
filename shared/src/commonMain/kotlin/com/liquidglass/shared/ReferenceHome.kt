package com.liquidglass.shared

import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.Home
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Library_music
import com.composables.icons.materialsymbols.roundedfilled.More_horiz
import com.composables.icons.materialsymbols.roundedfilled.Pause
import com.composables.icons.materialsymbols.roundedfilled.Play_arrow
import com.composables.icons.materialsymbols.roundedfilled.Playlist_play
import com.composables.icons.materialsymbols.roundedfilled.Search
import com.composables.icons.materialsymbols.roundedfilled.Skip_next

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.liquidglass.shared.resources.Res
import com.liquidglass.shared.resources.glaze_wordmark
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.cloudy
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ReferenceHomeScreen(
    albums: List<Album>,
    freshSongs: List<Song>,
    client: SubsonicClient,
    darkMode: Boolean,
    loading: Boolean,
    error: String?,
    shuffling: Boolean,
    onShuffle: () -> Unit,
    onAlbum: (Album) -> Unit,
    onPlaySong: (Song) -> Unit,
    onAddNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onSettings: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val ink = colors.onBackground
    val muted = colors.onSurfaceVariant
    val surface = colors.background
    Box(Modifier.fillMaxSize().background(surface)) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 220.dp),
        ) {
            item {
                AppToolbar(null, client.credentials.username, darkMode, onSettings)
            }
            if (error != null) item {
                Text(error, color = ink, fontSize = 13.sp,
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp))
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 27.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("NEW IN YOUR LIBRARY", color = muted, fontSize = 11.sp,
                        fontWeight = FontWeight.W600, letterSpacing = 1.sp,
                        modifier = Modifier.weight(1f))
                    if (albums.size > 1) Text("Swipe to browse", color = muted, fontSize = 11.sp)
                }
            }
            item {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val cardWidth = maxWidth - 72.dp
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(albums.take(12), key = { it.id }) { album ->
                            Column(Modifier.width(cardWidth).clickable { onAlbum(album) }) {
                                Text(album.name, color = ink, fontSize = 20.sp, lineHeight = 23.sp,
                                    fontWeight = FontWeight.W600, maxLines = 1,
                                    softWrap = false, overflow = TextOverflow.Clip,
                                    modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                                Text(album.artist, color = muted, fontSize = 17.sp,
                                    fontWeight = FontWeight.W400, maxLines = 1,
                                    softWrap = false, overflow = TextOverflow.Clip,
                                    modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                                Spacer(Modifier.height(10.dp))
                                AlbumImage(client, album.coverArt,
                                    Modifier.fillMaxWidth().height(245.dp).clip(RoundedCornerShape(17.dp)))
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 31.dp, bottom = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Recently Added Songs", color = ink, fontSize = 22.sp,
                        fontWeight = FontWeight.W600, modifier = Modifier.weight(1f))
                    Text(
                        if (shuffling) "Loading…" else "Shuffle", color = ink,
                        fontSize = 13.sp, fontWeight = FontWeight.W500,
                        modifier = Modifier.clickable(enabled = !shuffling, onClick = onShuffle)
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                    )
                }
            }
            if (loading) item {
                Text("Loading your library…", color = muted,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp))
            } else if (freshSongs.isEmpty()) item {
                Text("No songs here yet.", color = muted,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp))
            } else items(freshSongs.take(8), key = { it.id }) { song ->
                HomeSongRow(song, client,
                    onClick = { onPlaySong(song) },
                    onAddNext = { onAddNext(song) },
                    onAddToQueue = { onAddToQueue(song) })
            }
            if (albums.size > 1) {
                item {
                    Text("Albums", color = ink, fontSize = 22.sp,
                        fontWeight = FontWeight.W600,
                        modifier = Modifier.padding(start = 22.dp, top = 31.dp, bottom = 12.dp))
                }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                        items(albums.drop(1).take(12), key = { it.id }) { album ->
                            Column(Modifier.width(144.dp).clickable { onAlbum(album) }) {
                                AlbumImage(client, album.coverArt,
                                    Modifier.size(144.dp).clip(RoundedCornerShape(12.dp)))
                                Spacer(Modifier.height(6.dp))
                                Text(album.name, color = ink, fontSize = 13.sp,
                                    fontWeight = FontWeight.W500, maxLines = 1,
                                    softWrap = false, overflow = TextOverflow.Clip,
                                    modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                                Text(album.artist, color = muted, fontSize = 11.sp,
                                    maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                                    modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AppToolbar(title: String?, username: String, darkMode: Boolean,
                        onSettings: (() -> Unit)?, onBack: (() -> Unit)? = null) {
    val ink = MaterialTheme.colorScheme.onBackground
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp).height(44.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack) {
            Icon(MaterialSymbols.RoundedFilled.Arrow_back, "Back", tint = ink)
        }
        if (title == null) Image(
            painter = painterResource(Res.drawable.glaze_wordmark),
            contentDescription = "Glaze",
            modifier = Modifier.weight(1f).height(43.dp),
            alignment = Alignment.CenterStart,
            colorFilter = ColorFilter.tint(ink),
        ) else Text(title, modifier = Modifier.weight(1f), color = ink,
            fontSize = 29.sp, fontWeight = FontWeight.SemiBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Box(Modifier.size(44.dp).clip(CircleShape)
            .background(if (darkMode) Color.White else Color.Black)
            .border(1.dp, ink.copy(alpha = 0.25f), CircleShape)
            .then(if (onSettings != null) Modifier.clickable(onClick = onSettings) else Modifier),
            contentAlignment = Alignment.Center) {
            Text(username.take(2).uppercase(), color = if (darkMode) Color.Black else Color.White,
                fontSize = 15.sp, fontWeight = FontWeight.W600)
        }
    }
}

@Composable
private fun HomeSongRow(
    song: Song, client: SubsonicClient,
    onClick: () -> Unit, onAddNext: () -> Unit, onAddToQueue: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumImage(client, song.coverArt,
            Modifier.size(50.dp).clip(RoundedCornerShape(7.dp)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = colors.onBackground, fontSize = 15.sp,
                fontWeight = FontWeight.W400, maxLines = 1, softWrap = false,
                overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isExplicit) {
                    ExplicitBadge(color = colors.onSurfaceVariant)
                    Spacer(Modifier.width(5.dp))
                }
                Text(song.artist, color = colors.onSurfaceVariant, fontSize = 12.sp,
                    maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE))
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(MaterialSymbols.RoundedFilled.More_horiz, "Song actions",
                    tint = colors.onSurfaceVariant)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Play next") }, onClick = {
                    menuOpen = false; onAddNext()
                })
                DropdownMenuItem(text = { Text("Add to queue") }, onClick = {
                    menuOpen = false; onAddToQueue()
                })
            }
        }
    }
}

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
    onAddNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
) {
    val pill = RoundedCornerShape(50)
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val tint = if (darkMode)
        Color.Black.copy(alpha = 0.43f + settings.glassIntensity * 0.22f)
    else Color.White.copy(alpha = 0.35f + settings.glassIntensity * 0.22f)
    val sheen = Brush.linearGradient(listOf(
        Color.White.copy(alpha = if (darkMode) 0.48f else 0.98f),
        Color.White.copy(alpha = if (darkMode) 0.06f else 0.30f),
    ))
    var menuOpen by remember { mutableStateOf(false) }
    var swipeX by remember(song?.id) { mutableFloatStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { settings.gestures.sensitivityDp.dp.toPx() }
    val miniHeight = when (settings.miniPlayerSize) {
        MiniPlayerSize.Small -> 52.dp
        MiniPlayerSize.Medium -> 62.dp
        MiniPlayerSize.Large -> 78.dp
    }
    val coverSize = when (settings.miniPlayerSize) {
        MiniPlayerSize.Small -> 38.dp
        MiniPlayerSize.Medium -> 47.dp
        MiniPlayerSize.Large -> 60.dp
    }
    val navHeight = if (settings.navigationLabels) 67.dp else 57.dp
    val spotifyNav = settings.navigationStyle == NavigationStyle.Spotify
    val showSearchInNavigation = spotifyNav || settings.searchInNavigation
    val scrimColor = if (darkMode) Color.Black else Color.White
    val miniSurface = if (spotifyNav) {
        (if (darkMode) Color(0xFF151515) else Color.White).copy(alpha = 0.94f)
    } else Color.White.copy(alpha = if (darkMode) 0.06f else 0.14f)
    if (spotifyNav) Box(
        Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(210.dp)
            .background(Brush.verticalGradient(
                0f to Color.Transparent,
                0.35f to scrimColor.copy(alpha = 0.54f),
                0.70f to scrimColor.copy(alpha = 0.92f),
                1f to scrimColor,
            ))
    )
    Column(
        Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            .navigationBarsPadding().padding(horizontal = 17.dp, vertical = 10.dp),
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
                            ({ menuOpen = true }) else null)
                    .padding(horizontal = if (settings.miniPlayerSize == MiniPlayerSize.Large) 9.dp else 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlbumImage(client, song.coverArt,
                    Modifier.size(coverSize).clip(RoundedCornerShape(
                        if (settings.miniPlayerSize == MiniPlayerSize.Large) 15.dp else 12.dp)))
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
                        Text(song.artist, color = muted,
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
                if (settings.miniPlayerSize != MiniPlayerSize.Small) IconButton(onClick = onNext) {
                    Icon(MaterialSymbols.RoundedFilled.Skip_next, "Next", tint = ink,
                        modifier = Modifier.size(if (settings.miniPlayerSize == MiniPlayerSize.Large) 31.dp else 27.dp))
                }
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Play next") }, onClick = {
                    menuOpen = false; onAddNext(song)
                })
                DropdownMenuItem(text = { Text("Add to queue") }, onClick = {
                    menuOpen = false; onAddToQueue(song)
                })
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                Modifier.weight(1f).height(navHeight)
                    .then(if (settings.navigationStyle == NavigationStyle.Glaze)
                        Modifier.shadow(18.dp, pill).clip(pill)
                            .cloudy(sky = sky, radius = 48, tint = tint, shape = pill)
                            .background(Color.White.copy(alpha = if (darkMode) 0.04f else 0.11f))
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
                val order = if (settings.navigationStyle == NavigationStyle.Spotify)
                    listOf(0, 3, 1, 2) else listOf(0, 1, 2, 3)
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
                            modifier = Modifier.size(23.dp))
                        if (settings.navigationLabels) Text(tab.second,
                            color = if (selectedTab == index) ink else muted,
                            fontSize = 10.sp, fontWeight = FontWeight.W500)
                    }
                }
            }
            if (!showSearchInNavigation) Box(
                Modifier.size(navHeight).shadow(18.dp, CircleShape).clip(CircleShape)
                    .cloudy(sky = sky, radius = 48, tint = tint, shape = CircleShape)
                    .background(Color.White.copy(alpha = if (darkMode) 0.04f else 0.11f))
                    .border(1.dp, sheen, CircleShape).clickable(onClick = onSearch),
                contentAlignment = Alignment.Center,
            ) {
                Icon(MaterialSymbols.RoundedFilled.Search, "Search",
                    tint = if (selectedTab == 3) ink else muted, modifier = Modifier.size(27.dp))
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
