package com.liquidglass.shared

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Check
import com.composables.icons.materialsymbols.roundedfilled.Keyboard_arrow_down
import com.composables.icons.materialsymbols.roundedfilled.Search

@Composable
internal fun LibrarySearchField(value: String, onValueChange: (String) -> Unit,
    placeholder: String, modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    BasicTextField(value, onValueChange = onValueChange, singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = ink),
        cursorBrush = SolidColor(ink),
        modifier = modifier.fillMaxWidth().clip(CircleShape)
            .background(ink.copy(alpha = 0.08f))
            .border(1.dp, ink.copy(alpha = 0.11f), CircleShape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        decorationBox = { field -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(MaterialSymbols.RoundedFilled.Search, null, tint = muted,
                modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(placeholder, color = muted)
                field()
            }
        } })
}

@Composable
internal fun rememberPullSearchConnection(canScrollBackward: () -> Boolean,
    onReveal: () -> Unit, onHide: () -> Unit): NestedScrollConnection {
    val canScroll by rememberUpdatedState(canScrollBackward)
    val reveal by rememberUpdatedState(onReveal)
    val hide by rememberUpdatedState(onHide)
    val pullDistance = with(LocalDensity.current) { 48.dp.toPx() }
    val hideDistance = with(LocalDensity.current) { 24.dp.toPx() }
    return remember(pullDistance, hideDistance) { object : NestedScrollConnection {
        var pull = 0f
        var away = 0f
        override fun onPostScroll(consumed: Offset, available: Offset,
            source: NestedScrollSource): Offset {
            if (source == NestedScrollSource.UserInput && !canScroll() && available.y > 0f) {
                pull += available.y
                away = 0f
                if (pull >= pullDistance) { reveal(); pull = 0f }
            } else if (source == NestedScrollSource.UserInput && consumed.y < 0f) {
                away -= consumed.y
                pull = 0f
                if (away >= hideDistance) { hide(); away = 0f }
            } else if (source == NestedScrollSource.UserInput) {
                pull = 0f
                away = 0f
            }
            return Offset.Zero
        }
    } }
}

internal enum class AlbumSort { Library, Name, NameReverse, Newest, Oldest }
internal enum class PlaylistSort { Library, Name, NameReverse, MostSongs, FewestSongs }
internal enum class SongSort { Top, Name, NameReverse, MostPlayed }

internal fun sortSongs(songs: List<Song>, sort: SongSort): List<Song> = when (sort) {
    SongSort.Top -> songs
    SongSort.Name -> songs.sortedBy { it.title.lowercase() }
    SongSort.NameReverse -> songs.sortedByDescending { it.title.lowercase() }
    SongSort.MostPlayed -> songs.sortedWith(compareByDescending<Song> { it.playCount }
        .thenBy { it.title.lowercase() })
}

internal fun sortAlbums(albums: List<Album>, sort: AlbumSort): List<Album> = when (sort) {
    AlbumSort.Library -> albums
    AlbumSort.Name -> albums.sortedBy { it.name.lowercase() }
    AlbumSort.NameReverse -> albums.sortedByDescending { it.name.lowercase() }
    AlbumSort.Newest -> albums.sortedWith(compareByDescending<Album> {
        it.releaseOrder.takeIf { date -> date > 0 } ?: Int.MIN_VALUE
    }.thenBy { it.name.lowercase() })
    AlbumSort.Oldest -> albums.sortedWith(compareBy<Album> {
        it.releaseOrder.takeIf { date -> date > 0 } ?: Int.MAX_VALUE
    }.thenBy { it.name.lowercase() })
}

internal fun sortPlaylists(playlists: List<Playlist>, sort: PlaylistSort,
    isFavorite: (Playlist) -> Boolean): List<Playlist> = when (sort) {
    PlaylistSort.Library -> playlists
    PlaylistSort.Name -> playlists.sortedBy { it.name.lowercase() }
    PlaylistSort.NameReverse -> playlists.sortedByDescending { it.name.lowercase() }
    PlaylistSort.MostSongs -> playlists.sortedWith(compareByDescending<Playlist> { it.songCount }
        .thenBy { it.name.lowercase() })
    PlaylistSort.FewestSongs -> playlists.sortedWith(compareBy<Playlist> { it.songCount }
        .thenBy { it.name.lowercase() })
}.sortedByDescending(isFavorite)

@Composable
internal fun StickyTopBar(title: String?, onScrollTop: () -> Unit,
    onBack: (() -> Unit)? = null, actionCount: Int = 0,
    titleVisible: Boolean = title != null,
    actions: @Composable RowScope.() -> Unit = {}) {
    val ink = MaterialTheme.colorScheme.onBackground
    val titleAlpha by animateFloatAsState(if (titleVisible) 1f else 0f,
        animationSpec = tween(240, easing = FastOutSlowInEasing), label = "Sticky title")
    Box(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp)
        .clickable(role = Role.Button, onClickLabel = "Scroll to top", onClick = onScrollTop)) {
        if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(MaterialSymbols.RoundedFilled.Arrow_back, "Back", tint = ink)
        }
        if (title != null) Text(title,
            modifier = Modifier.align(Alignment.Center).fillMaxWidth()
                .padding(horizontal = (48 * maxOf(1, actionCount)).dp)
                .graphicsLayer { alpha = titleAlpha },
            color = ink, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically,
            content = actions)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> BrowserControls(columns: Int, onColumns: (Int) -> Unit,
    sort: T, sortLabel: String, options: List<Pair<T, String>>, onSort: (T) -> Unit,
    title: String) {
    val colors = MaterialTheme.colorScheme
    val ink = colors.onBackground
    val muted = colors.onSurfaceVariant
    val glass = ink.copy(alpha = 0.08f)
    var sorting by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.clip(CircleShape).background(glass)
            .border(1.dp, ink.copy(alpha = 0.12f), CircleShape).padding(4.dp).selectableGroup()) {
            listOf(1 to "List", 2 to "2", 3 to "3").forEach { (count, label) ->
                Box(Modifier.width(if (count == 1) 52.dp else 40.dp).height(40.dp).clip(CircleShape)
                    .background(if (columns == count) ink.copy(alpha = 0.16f) else Color.Transparent)
                    .selectable(selected = columns == count, role = Role.RadioButton,
                        onClick = { onColumns(count) })
                    .semantics { contentDescription = if (count == 1) "List view" else "$count column grid" },
                    contentAlignment = Alignment.Center) {
                    Text(label, color = if (columns == count) ink else muted,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (columns == count) FontWeight.SemiBold else FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Row(Modifier.weight(1f, fill = false).height(48.dp).clip(CircleShape).background(glass)
            .border(1.dp, ink.copy(alpha = 0.12f), CircleShape)
            .clickable(role = Role.Button, onClickLabel = title) { sorting = true }
            .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Sort · $sortLabel", color = ink, style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f, fill = false),
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(MaterialSymbols.RoundedFilled.Keyboard_arrow_down, null, tint = muted,
                modifier = Modifier.size(18.dp))
        }
    }
    if (sorting) ModalBottomSheet(onDismissRequest = { sorting = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface, scrimColor = Color.Black.copy(alpha = 0.28f),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }, dragHandle = null) {
        Column(Modifier.fillMaxWidth().background(colors.surface)
            .navigationBarsPadding().padding(bottom = 22.dp)) {
            SheetHandle()
            Text(title, color = ink, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 24.dp, top = 14.dp, bottom = 15.dp))
            options.forEach { (order, label) ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (sort == order) glass else Color.Transparent)
                    .selectable(selected = sort == order, role = Role.RadioButton,
                        onClick = { onSort(order); sorting = false })
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(label, color = ink, style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f))
                    if (sort == order) Icon(MaterialSymbols.RoundedFilled.Check, null, tint = ink)
                }
            }
        }
    }
}
