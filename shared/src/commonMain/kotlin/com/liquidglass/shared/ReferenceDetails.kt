package com.liquidglass.shared

import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Arrow_forward
import com.composables.icons.materialsymbols.roundedfilled.Library_music
import com.composables.icons.materialsymbols.roundedfilled.More_vert
import com.composables.icons.materialsymbols.roundedfilled.Play_arrow
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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

@Composable
internal fun ArtistReferenceScreen(
    artist: Artist,
    albums: List<Album>,
    topSongs: List<Song>,
    client: SubsonicClient,
    darkMode: Boolean,
    onBack: () -> Unit,
    onAlbum: (Album) -> Unit,
    onSong: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
) {
    val sky = rememberSky()
    val artId = artist.coverArt ?: albums.firstOrNull()?.coverArt
    val artUrl = remember(client, artId) { artId?.let { client.coverArtUrl(it, 1024) } }
    val base = MaterialTheme.colorScheme.background
    val ink = MaterialTheme.colorScheme.onBackground
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
    val featured = albums.firstOrNull()
    var menuOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(base)) {
        Box(Modifier.fillMaxSize().sky(sky)) {
            AmbientArtwork(artUrl, darkMode) { sky.invalidate() }
            LazyColumn(contentPadding = PaddingValues(bottom = 220.dp)) {
                item {
                    Box(Modifier.fillMaxWidth().height(390.dp)) {
                        HeroArtwork(artUrl) { sky.invalidate() }
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.32f),
                            0.48f to Color.Transparent,
                            0.90f to Color.Black.copy(alpha = 0.62f),
                            1f to base.copy(alpha = 0.98f),
                        )))
                        Text(
                            artist.name,
                            modifier = Modifier.align(Alignment.BottomStart)
                                .padding(start = 25.dp, end = 25.dp, bottom = 26.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 43.sp,
                                lineHeight = 46.sp,
                                shadow = Shadow(Color.Black.copy(alpha = 0.85f),
                                    offset = Offset.Zero, blurRadius = 12f),
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 3.dp, bottom = 29.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        RoundAction(MaterialSymbols.RoundedFilled.Shuffle, "Shuffle ${artist.name}", darkMode, onShuffle)
                        Spacer(Modifier.width(17.dp))
                        RoundAction(MaterialSymbols.RoundedFilled.Play_arrow, "Play ${artist.name}", darkMode, onPlayAll)
                        if (featured != null) {
                            Spacer(Modifier.width(17.dp))
                            RoundAction(MaterialSymbols.RoundedFilled.Library_music, "Open featured album", darkMode) {
                                onAlbum(featured)
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
                if (topSongs.isNotEmpty()) {
                    item { DetailHeading("Songs", Modifier.padding(top = 32.dp, bottom = 10.dp)) }
                    items(topSongs, key = { it.id }) { song ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onSong(song) }
                                .padding(horizontal = 24.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DetailArtwork(client, song.coverArt, Modifier.size(57.dp))
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(song.title, color = ink,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                                    modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (song.isExplicit) {
                                        ExplicitBadge(color = quiet)
                                        Spacer(Modifier.width(5.dp))
                                    }
                                    Text(song.album.ifBlank { artist.name }, color = quiet,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                                        modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE))
                                }
                            }
                            Icon(MaterialSymbols.RoundedFilled.Play_arrow, contentDescription = null, tint = quiet)
                        }
                    }
                }
                if (albums.size > 1) {
                    item { DetailHeading("Albums", Modifier.padding(top = 30.dp, bottom = 10.dp)) }
                    items(albums, key = { it.id }) { album ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onAlbum(album) }
                                .padding(horizontal = 24.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DetailArtwork(client, album.coverArt, Modifier.size(66.dp))
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(album.name, color = ink,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                                    modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                                Text(listOfNotNull(album.year?.toString(),
                                    "${album.songCount} songs").joinToString(" · "), color = quiet,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(MaterialSymbols.RoundedFilled.Arrow_forward, contentDescription = null, tint = quiet,
                                modifier = Modifier.size(19.dp))
                        }
                    }
                }
            }
        }
        DetailTopBar(
            darkMode = darkMode, onBack = onBack,
            onMore = { menuOpen = true }, menuOpen = menuOpen,
            onDismissMenu = { menuOpen = false },
            menuItems = listOf("Play all" to onPlayAll, "Shuffle" to onShuffle),
            modifier = Modifier.align(Alignment.TopCenter),
            sky = sky,
        )
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
) = AlbumCollectionScreen(
    album, songs, client, darkMode, onBack, onPlaySong, onPlayAll,
    onShuffle, onAddNext, onShare, onArtworkColor, onAlbum,
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
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(
        Color(0xFF555555), Color(0xFF111111))))) {
        if (url != null) AsyncImage(model = url, contentDescription = null,
            onSuccess = { onImageLoaded() },
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
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
            tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(29.dp))
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
    onMore: () -> Unit,
    menuOpen: Boolean,
    onDismissMenu: () -> Unit,
    menuItems: List<Pair<String, () -> Unit>>,
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
        Box {
            DetailGlassIcon(MaterialSymbols.RoundedFilled.More_vert, "More options", darkMode, sky, onMore)
            DropdownMenu(expanded = menuOpen, onDismissRequest = onDismissMenu) {
                menuItems.forEach { (label, action) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = {
                        onDismissMenu()
                        action()
                    })
                }
            }
        }
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
