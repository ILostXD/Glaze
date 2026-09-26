package com.liquidglass.shared

import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Favorite as FavoriteOutline
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Arrow_forward
import com.composables.icons.materialsymbols.roundedfilled.Favorite
import com.composables.icons.materialsymbols.roundedfilled.More_vert
import com.composables.icons.materialsymbols.roundedfilled.Play_arrow
import com.composables.icons.materialsymbols.roundedfilled.Share
import com.composables.icons.materialsymbols.roundedfilled.Shuffle

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import kotlinx.coroutines.launch

@Composable
internal fun ArtistReferenceScreen(
    artist: Artist,
    albums: List<Album>,
    topSongs: List<Song>,
    info: ArtistInfo,
    client: SubsonicClient,
    darkMode: Boolean,
    onBack: () -> Unit,
    onAlbum: (Album) -> Unit,
    onSong: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onAddNext: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onShareArtist: (String) -> Unit,
    onArtist: (Artist) -> Unit,
    onSection: (ArtistSection) -> Unit,
) {
    val sky = rememberSky()
    // Artist coverArt is often an album cover. A missing portrait is better than a wrong one.
    val portraitUrl = info.imageUrl ?: artist.imageUrl
    val base = MaterialTheme.colorScheme.background
    val ink = MaterialTheme.colorScheme.onBackground
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
    val releases = albums.sortedByDescending { it.releaseOrder }
    val fullAlbums = releases.filterNot(::isSingleOrEp)
    val singles = releases.filter(::isSingleOrEp)
    val featured = fullAlbums.firstOrNull() ?: releases.firstOrNull()
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var favorite by remember(artist.id) { mutableStateOf(artist.starred) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(artist.starred) { favorite = artist.starred }

    Box(Modifier.fillMaxSize().background(base)) {
        Box(Modifier.fillMaxSize().sky(sky)) {
            AmbientArtwork(portraitUrl, darkMode) { sky.invalidate() }
            LazyColumn(contentPadding = PaddingValues(bottom = 220.dp)) {
                item {
                    Box(Modifier.fillMaxWidth().height(560.dp)) {
                        HeroArtwork(portraitUrl) { sky.invalidate() }
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.16f),
                            0.55f to Color.Transparent,
                            1f to Color.Transparent,
                        )))
                        Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                artist.name,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 31.sp,
                                    lineHeight = 36.sp,
                                    shadow = Shadow(Color.Black.copy(alpha = 0.85f),
                                        offset = Offset.Zero, blurRadius = 12f),
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(22.dp))
                            Row(horizontalArrangement = Arrangement.Center) {
                                RoundAction(if (favorite) MaterialSymbols.RoundedFilled.Favorite
                                    else MaterialSymbols.Rounded.FavoriteOutline,
                                    if (favorite) "Unfavorite ${artist.name}" else "Favorite ${artist.name}", darkMode,
                                    tint = if (favorite) Color(0xFFFF4D71) else ink.copy(alpha = 0.60f)) {
                                    val next = !favorite
                                    favorite = next
                                    scope.launch {
                                        try { client.setArtistStarred(artist.id, next) }
                                        catch (_: Exception) { favorite = !next }
                                    }
                                }
                                Spacer(Modifier.width(17.dp))
                                RoundAction(MaterialSymbols.RoundedFilled.Play_arrow,
                                    "Play ${artist.name}", darkMode, onClick = onPlayAll)
                                Spacer(Modifier.width(17.dp))
                                RoundAction(MaterialSymbols.RoundedFilled.Shuffle,
                                    "Shuffle ${artist.name}", darkMode, onClick = onShuffle)
                            }
                        }
                    }
                }
                if (featured != null) item {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(if (darkMode) Color.White.copy(alpha = 0.07f)
                                else Color.Black.copy(alpha = 0.035f))
                            .border(1.dp, if (darkMode) Color.White.copy(alpha = 0.14f)
                                else Color.Black.copy(alpha = 0.08f),
                                RoundedCornerShape(26.dp))
                            .clickable { onAlbum(featured) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DetailArtwork(client, featured.coverArt, Modifier.size(72.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("FEATURED ALBUM", color = quiet,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.2.sp, fontWeight = FontWeight.Medium))
                            Spacer(Modifier.height(5.dp))
                            Text(featured.name, color = ink,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                                modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                            Text("${featured.songCount} songs", color = quiet,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(MaterialSymbols.RoundedFilled.Arrow_forward, contentDescription = null, tint = ink,
                            modifier = Modifier.size(22.dp))
                    }
                }
                if (topSongs.isNotEmpty()) item {
                    ArtistSectionHeading("Top Songs", Modifier.padding(top = 34.dp, bottom = 14.dp)) {
                        onSection(ArtistSection.TopSongs)
                    }
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(topSongs.chunked(4)) { page ->
                            Column(Modifier.width(330.dp)) {
                                page.forEach { song ->
                                    ArtistSongRow(song, client, artist.name, onSong) {
                                        selectedSong = song
                                    }
                                }
                            }
                        }
                    }
                }
                if (fullAlbums.isNotEmpty()) item {
                    ArtistSectionHeading("Albums", Modifier.padding(top = 36.dp, bottom = 15.dp),
                        onClick = if (fullAlbums.size > 10) ({ onSection(ArtistSection.Albums) }) else null)
                    ArtistReleaseRow(fullAlbums, client, onAlbum)
                }
                if (singles.isNotEmpty()) item {
                    ArtistSectionHeading("Singles & EPs", Modifier.padding(top = 36.dp, bottom = 15.dp),
                        onClick = if (singles.size > 10) ({ onSection(ArtistSection.Singles) }) else null)
                    ArtistReleaseRow(singles, client, onAlbum)
                }
                item { ArtistAbout(info, client, darkMode, onArtist) }
            }
        }
        DetailTopBar(
            darkMode = darkMode, onBack = onBack,
            onShare = { onShareArtist(artist.name) },
            modifier = Modifier.align(Alignment.TopCenter),
            sky = sky,
        )
        selectedSong?.let { song ->
            CollectionSongSheet(song, client, onDismiss = { selectedSong = null },
                onPlayNext = { onAddNext(song) }, onAlbum = onAlbum,
                onArtist = onArtist, onShare = onShareSong)
        }
    }
}

internal fun isSingleOrEp(album: Album): Boolean {
    val types = album.releaseTypes.map { it.lowercase() }
    if (types.isNotEmpty()) return types.any { it == "single" || it == "ep" }
    // ponytail: track-count fallback until the server exposes releaseTypes for every album.
    return album.name.endsWith("- Single", true) || album.name.endsWith("- EP", true) ||
        album.songCount in 1..4
}

@Composable
private fun ArtistSectionHeading(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        if (onClick != null) Icon(MaterialSymbols.RoundedFilled.Arrow_forward, contentDescription = "See all $text",
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ArtistSongRow(song: Song, client: SubsonicClient, artistName: String,
    onSong: (Song) -> Unit, onMore: () -> Unit) {
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
    Row(Modifier.fillMaxWidth().height(70.dp).clickable { onSong(song) },
        verticalAlignment = Alignment.CenterVertically) {
        DetailArtwork(client, song.coverArt, Modifier.size(52.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isExplicit) { ExplicitBadge(color = quiet); Spacer(Modifier.width(5.dp)) }
                Text(song.album.ifBlank { artistName }, color = quiet,
                    style = MaterialTheme.typography.bodySmall, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
        }
        IconButton(onClick = onMore, modifier = Modifier.size(36.dp)) {
            Icon(MaterialSymbols.RoundedFilled.More_vert,
                contentDescription = "More options for ${song.title}", tint = quiet)
        }
    }
}

@Composable
private fun ArtistReleaseRow(albums: List<Album>, client: SubsonicClient, onAlbum: (Album) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(albums, key = { it.id }) { album ->
            ArtistReleaseCard(album, client, Modifier.width(158.dp), onAlbum)
        }
    }
}

@Composable
private fun ArtistReleaseCard(album: Album, client: SubsonicClient, modifier: Modifier,
    onAlbum: (Album) -> Unit) {
    Column(modifier.clickable { onAlbum(album) }) {
        DetailArtwork(client, album.coverArt, Modifier.fillMaxWidth().height(158.dp))
        Spacer(Modifier.height(8.dp))
        Text(album.name, color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(album.year?.toString().orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ArtistAbout(info: ArtistInfo, client: SubsonicClient, darkMode: Boolean,
    onArtist: (Artist) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(top = 42.dp)) {
        Text("About", modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        info.biography?.let { biography ->
            val plain = remember(biography) { biography.replace(Regex("<[^>]*>"), " ")
                .replace(Regex("\\s+"), " ").trim() }
            Text(plain, modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                .clickable { expanded = !expanded },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis)
            if (!expanded && plain.length > 240) Text("More", color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { expanded = true }.padding(horizontal = 24.dp))
        } ?: Text("No artist biography available yet.",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
        if (info.similarArtists.isNotEmpty()) {
            Text("Similar Artists", modifier = Modifier.padding(start = 24.dp, top = 30.dp, bottom = 14.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                items(info.similarArtists, key = { it.id }) { artist ->
                    SimilarArtistCard(artist, client, darkMode) { onArtist(artist) }
                }
            }
        }
    }
}

@Composable
private fun SimilarArtistCard(artist: Artist, client: SubsonicClient, darkMode: Boolean,
    onClick: () -> Unit) {
    var portrait by remember(artist.id) { mutableStateOf(artist.imageUrl) }
    LaunchedEffect(artist.id, client) {
        if (portrait == null) portrait = runCatching { client.artistImageUrl(artist.id) }.getOrNull()
    }
    Column(Modifier.width(108.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(104.dp).clip(CircleShape)
            .background(if (darkMode) Color.White.copy(alpha = 0.12f)
                else Color.Black.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            if (portrait != null) AsyncImage(model = portrait, contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text(artist.name.take(1), style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(8.dp))
        Text(artist.name, color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun ArtistSectionScreen(artist: Artist, section: ArtistSection, albums: List<Album>,
    songs: List<Song>, client: SubsonicClient, darkMode: Boolean, onBack: () -> Unit,
    onAlbum: (Album) -> Unit, onSong: (Song) -> Unit, onAddNext: (Song) -> Unit,
    onShareSong: (Song) -> Unit, onArtist: (Artist) -> Unit) {
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    val title = when (section) {
        ArtistSection.TopSongs -> "Top Songs"
        ArtistSection.Albums -> "Albums"
        ArtistSection.Singles -> "Singles & EPs"
    }
    val releases = albums.filter { isSingleOrEp(it) == (section == ArtistSection.Singles) }
        .sortedByDescending { it.releaseOrder }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(MaterialSymbols.RoundedFilled.Arrow_back, contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(title, modifier = Modifier.padding(start = 12.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge)
        }
        if (section == ArtistSection.TopSongs) LazyColumn(
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 220.dp)) {
            items(songs, key = { it.id }) { song ->
                ArtistSongRow(song, client, artist.name, onSong) { selectedSong = song }
            }
        } else LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 220.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            items(releases.chunked(2)) { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    pair.forEach { album ->
                        ArtistReleaseCard(album, client, Modifier.weight(1f), onAlbum)
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
    selectedSong?.let { song ->
        CollectionSongSheet(song, client, onDismiss = { selectedSong = null },
            onPlayNext = { onAddNext(song) }, onAlbum = onAlbum,
            onArtist = onArtist, onShare = onShareSong)
    }
}

@Composable
internal fun AlbumReferenceScreen(
    album: Album,
    songs: List<Song>,
    client: SubsonicClient,
    darkMode: Boolean,
    onBack: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onAddNext: (Song) -> Unit,
    onShare: (String) -> Unit,
    onArtworkColor: suspend (String?) -> Color,
    onAlbum: (Album) -> Unit,
    onArtist: (Artist) -> Unit,
) = AlbumCollectionScreen(
    album, songs, client, darkMode, onBack, onPlaySong, onPlayAll,
    onShuffle, onAddNext, onShare, onArtworkColor, onAlbum, onArtist,
)

@Composable
private fun AmbientArtwork(url: String?, darkMode: Boolean, onImageLoaded: () -> Unit) {
    if (url != null) AsyncImage(
        model = url, contentDescription = null,
        onSuccess = { onImageLoaded() },
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().blur(96.dp),
    )
    Box(Modifier.fillMaxSize().background(
        if (darkMode) Color.Black.copy(alpha = if (url == null) 1f else 0.78f)
        else Color.White.copy(alpha = if (url == null) 1f else 0.86f)))
}

@Composable
private fun HeroArtwork(url: String?, onImageLoaded: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        if (url != null) AsyncImage(model = url, contentDescription = null,
            onSuccess = { onImageLoaded() },
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(Brush.verticalGradient(
                        0f to Color.White,
                        0.54f to Color.White,
                        1f to Color.Transparent,
                    ), blendMode = BlendMode.DstIn)
                })
        else Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
            Color(0xFF42464A), Color(0xFF24272A), Color.Transparent))))
    }
}

@Composable
private fun DetailArtwork(client: SubsonicClient, id: String?, modifier: Modifier) {
    val url = remember(client, id) { id?.let { client.coverArtUrl(it, 320) } }
    Box(modifier.clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))) {
        if (url != null) AsyncImage(model = url, contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun RoundAction(
    image: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    darkMode: Boolean,
    tint: Color? = null,
    onClick: () -> Unit,
) {
    val shape = CircleShape
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp).clip(shape)
            .background(if (darkMode) Color.White.copy(alpha = 0.12f)
                else Color.Black.copy(alpha = 0.08f))
            .border(1.dp, if (darkMode) Color.White.copy(alpha = 0.22f)
                else Color.Black.copy(alpha = 0.13f), shape),
    ) {
        Icon(image, contentDescription = description,
            tint = tint ?: MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(29.dp))
    }
}

@Composable
private fun DetailHeading(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier.padding(horizontal = 24.dp),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
}

@Composable
private fun DetailTopBar(
    darkMode: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier,
    sky: com.skydoves.cloudy.Sky,
) {
    Row(
        modifier.fillMaxWidth().statusBarsPadding()
            .padding(start = 19.dp, end = 19.dp, top = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DetailGlassIcon(MaterialSymbols.RoundedFilled.Arrow_back, "Back", darkMode, sky, onBack)
        DetailGlassIcon(MaterialSymbols.RoundedFilled.Share, "Share artist", darkMode, sky, onShare)
    }
}

@Composable
private fun DetailGlassIcon(
    image: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    darkMode: Boolean,
    sky: com.skydoves.cloudy.Sky,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(52.dp)
            .cloudy(sky = sky, radius = 42,
                tint = if (darkMode) Color.Black.copy(alpha = 0.22f)
                else Color.Black.copy(alpha = 0.42f), shape = CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.27f), CircleShape),
    ) {
        Icon(image, contentDescription = description, tint = Color.White)
    }
}
