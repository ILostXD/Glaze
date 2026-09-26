package com.liquidglass.shared

import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Home
import com.composables.icons.materialsymbols.roundedfilled.Library_music
import com.composables.icons.materialsymbols.roundedfilled.More_vert
import com.composables.icons.materialsymbols.roundedfilled.Pause
import com.composables.icons.materialsymbols.roundedfilled.Play_arrow
import com.composables.icons.materialsymbols.roundedfilled.Playlist_play
import com.composables.icons.materialsymbols.roundedfilled.Queue_music
import com.composables.icons.materialsymbols.roundedfilled.Search
import com.composables.icons.materialsymbols.roundedfilled.Check
import com.composables.icons.materialsymbols.roundedfilled.Keyboard_arrow_down
import com.composables.icons.materialsymbols.roundedfilled.Settings
import com.composables.icons.materialsymbols.roundedfilled.Skip_next
import com.composables.icons.materialsymbols.roundedfilled.Skip_previous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import com.liquidglass.shared.resources.Res
import com.liquidglass.shared.resources.glaze_mark
import com.liquidglass.shared.resources.be_vietnam_pro_bold
import com.liquidglass.shared.resources.be_vietnam_pro_medium
import com.liquidglass.shared.resources.be_vietnam_pro_regular
import com.liquidglass.shared.resources.be_vietnam_pro_semibold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val ink: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground
private val muted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val accent: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val glass: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
private val homeInk = Color(0xFF101114)

private fun TextStyle.withFont(font: FontFamily): TextStyle = copy(fontFamily = font)
private val homeMuted = Color(0xFF777B83)
private val homeBlue = Color(0xFF287CE7)

data class GestureConfig(
    val miniPlayerSwipe: Boolean = true,
    val playerSwipeDown: Boolean = true,
    val miniPlayerLongPress: Boolean = true,
    val sensitivityDp: Float = 80f,
)

data class AppSettings(
    val gestures: GestureConfig = GestureConfig(),
    val glassIntensity: Float = 0.5f,
    val smartShuffle: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.System,
    val miniPlayerSize: MiniPlayerSize = MiniPlayerSize.Medium,
    val navigationSize: NavigationSize = NavigationSize.Medium,
    val navigationStyle: NavigationStyle = NavigationStyle.Spotify,
    val searchInNavigation: Boolean = true,
    val navigationLabels: Boolean = true,
)

enum class ThemePreference { System, Light, Dark }
enum class MiniPlayerSize { Small, Medium, Large }
enum class NavigationSize { Small, Medium, Large }
enum class NavigationStyle { Spotify, Glaze }

private enum class Tab { Home, Artists, Playlists, Search }
internal enum class ArtistSort { Name, NameReverse, MostAlbums, FewestAlbums }
internal fun sortArtists(artists: List<Artist>, order: ArtistSort): List<Artist> = when (order) {
    ArtistSort.Name -> artists.sortedBy { it.name.lowercase() }
    ArtistSort.NameReverse -> artists.sortedByDescending { it.name.lowercase() }
    ArtistSort.MostAlbums -> artists.sortedWith(compareByDescending<Artist> { it.albumCount }.thenBy { it.name.lowercase() })
    ArtistSort.FewestAlbums -> artists.sortedWith(compareBy<Artist> { it.albumCount }.thenBy { it.name.lowercase() })
}
internal enum class ArtistSection { TopSongs, Albums, Singles }
private sealed interface Detail {
    data class ArtistPage(val artist: Artist) : Detail
    data class ArtistSectionPage(val artist: Artist, val section: ArtistSection) : Detail
    data class AlbumPage(val album: Album) : Detail
    data class PlaylistPage(val playlist: Playlist) : Detail
    data object QueuePage : Detail
    data object SettingsPage : Detail
}

@Composable
fun MusicApp(
    credentials: ServerCredentials?,
    onConnect: suspend (String, String, String) -> Result<Unit>,
    onDisconnect: () -> Unit,
    nowPlaying: Song?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    playerColor: Color,
    playerBackdropColor: Color,
    queue: List<Song>,
    currentIndex: Int,
    positionMs: Long,
    durationMs: Long,
    onPlay: (Song, List<Song>) -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onAddNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onShareCollection: (String) -> Unit,
    onArtworkColor: suspend (String?) -> Color,
    onRemoveFromQueue: (Int) -> Unit,
    onRestoreQueueItem: (Song, Int) -> Unit,
    onMoveInQueue: (Int, Int) -> Unit,
    onPlayQueueIndex: (Int) -> Unit,
    onShuffleSongs: (List<Song>) -> Unit,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onReadPosition: () -> Pair<Long, Long>,
    onLightSystemBars: (Boolean) -> Unit,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onClearUpcoming: () -> Unit,
    playbackSpeed: Float,
    onChangePlaybackSpeed: (Float) -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkMode = when (settings.themePreference) {
        ThemePreference.System -> systemDark
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }
    val beVietnamPro = FontFamily(
        Font(Res.font.be_vietnam_pro_regular, weight = FontWeight.W400),
        Font(Res.font.be_vietnam_pro_medium, weight = FontWeight.W500),
        Font(Res.font.be_vietnam_pro_semibold, weight = FontWeight.W600),
        Font(Res.font.be_vietnam_pro_bold, weight = FontWeight.W700),
    )
    val palette = if (darkMode) darkColorScheme(
        primary = Color.White, onPrimary = Color.Black,
        primaryContainer = Color(0xFFE8E8E8), onPrimaryContainer = Color.Black,
        secondary = Color.White, onSecondary = Color.Black,
        secondaryContainer = Color(0xFF242424), onSecondaryContainer = Color.White,
        tertiary = Color.White, onTertiary = Color.Black,
        tertiaryContainer = Color(0xFF242424), onTertiaryContainer = Color.White,
        background = Color.Black, onBackground = Color.White,
        surface = Color.Black, onSurface = Color.White,
        surfaceVariant = Color(0xFF1B1B1B), onSurfaceVariant = Color(0xFFB8B8B8),
        surfaceTint = Color.White, outline = Color(0xFF777777),
        outlineVariant = Color(0xFF444444),
        inverseSurface = Color.White, inverseOnSurface = Color.Black,
        inversePrimary = Color.Black, error = Color.White, onError = Color.Black,
        errorContainer = Color(0xFF242424), onErrorContainer = Color.White,
        surfaceDim = Color.Black, surfaceBright = Color(0xFF222222),
        surfaceContainerLowest = Color.Black, surfaceContainerLow = Color(0xFF101010),
        surfaceContainer = Color(0xFF151515), surfaceContainerHigh = Color(0xFF1B1B1B),
        surfaceContainerHighest = Color(0xFF242424),
    ) else lightColorScheme(
        primary = Color.Black, onPrimary = Color.White,
        primaryContainer = Color(0xFF222222), onPrimaryContainer = Color.White,
        secondary = Color.Black, onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8E8E8), onSecondaryContainer = Color.Black,
        tertiary = Color.Black, onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE8E8E8), onTertiaryContainer = Color.Black,
        background = Color.White, onBackground = Color.Black,
        surface = Color.White, onSurface = Color.Black,
        surfaceVariant = Color(0xFFE8E8E8), onSurfaceVariant = Color(0xFF606060),
        surfaceTint = Color.Black, outline = Color(0xFF777777),
        outlineVariant = Color(0xFFBBBBBB),
        inverseSurface = Color.Black, inverseOnSurface = Color.White,
        inversePrimary = Color.White, error = Color.Black, onError = Color.White,
        errorContainer = Color(0xFFE8E8E8), onErrorContainer = Color.Black,
        surfaceDim = Color(0xFFEEEEEE), surfaceBright = Color.White,
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF8F8F8),
        surfaceContainer = Color(0xFFF3F3F3), surfaceContainerHigh = Color(0xFFEDEDED),
        surfaceContainerHighest = Color(0xFFE6E6E6),
    )
    LaunchedEffect(credentials, darkMode) {
        if (credentials == null) onLightSystemBars(!darkMode)
    }
    val baseType = Typography()
    val beVietnamProType = Typography(
        displayLarge = baseType.displayLarge.withFont(beVietnamPro),
        displayMedium = baseType.displayMedium.withFont(beVietnamPro),
        displaySmall = baseType.displaySmall.withFont(beVietnamPro),
        headlineLarge = baseType.headlineLarge.withFont(beVietnamPro),
        headlineMedium = baseType.headlineMedium.withFont(beVietnamPro),
        headlineSmall = baseType.headlineSmall.withFont(beVietnamPro),
        titleLarge = baseType.titleLarge.withFont(beVietnamPro),
        titleMedium = baseType.titleMedium.withFont(beVietnamPro),
        titleSmall = baseType.titleSmall.withFont(beVietnamPro),
        bodyLarge = baseType.bodyLarge.withFont(beVietnamPro),
        bodyMedium = baseType.bodyMedium.withFont(beVietnamPro),
        bodySmall = baseType.bodySmall.withFont(beVietnamPro),
        labelLarge = baseType.labelLarge.withFont(beVietnamPro),
        labelMedium = baseType.labelMedium.withFont(beVietnamPro),
        labelSmall = baseType.labelSmall.withFont(beVietnamPro),
    )
    MaterialTheme(colorScheme = palette, typography = beVietnamProType) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
        Box(Modifier.fillMaxSize().background(palette.background)) {
            if (credentials == null) {
                ConnectScreen(onConnect)
            } else {
                val client = remember(credentials) { SubsonicClient(credentials) }
                DisposableEffect(client) { onDispose { client.close() } }
                LibraryScreen(client, nowPlaying, isPlaying, isBuffering, playerColor, playerBackdropColor, queue, currentIndex, positionMs,
                    durationMs, onPlay, onTogglePlayback, onSkipNext, onSkipPrevious, onSeek,
                    onAddNext, onAddToQueue, onShareSong, onShareCollection, onArtworkColor,
                    onRemoveFromQueue, onRestoreQueueItem, onMoveInQueue, onPlayQueueIndex,
                    onShuffleSongs, onDisconnect, settings, onSettingsChange, onReadPosition,
                    darkMode = darkMode,
                    onLightSystemBars = onLightSystemBars,
                    isShuffleEnabled = isShuffleEnabled, repeatMode = repeatMode,
                    onToggleShuffle = onToggleShuffle, onCycleRepeat = onCycleRepeat,
                    onClearUpcoming = onClearUpcoming,
                    playbackSpeed = playbackSpeed,
                    onChangePlaybackSpeed = onChangePlaybackSpeed)
            }
        }
        }
    }
}

@Composable
private fun ConnectScreen(onConnect: suspend (String, String, String) -> Result<Unit>) {
    var url by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Image(painterResource(Res.drawable.glaze_mark), "Glaze",
            Modifier.size(64.dp), colorFilter = ColorFilter.tint(ink))
        Spacer(Modifier.height(12.dp))
        Text("Your music,\nbeautifully yours.", color = ink, fontSize = 36.sp, lineHeight = 40.sp,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(28.dp))
        Surface(color = glass, shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, ink.copy(alpha = 0.18f))) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Connect to Navidrome", color = ink, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                OutlinedTextField(url, { url = it }, label = { Text("Server URL") },
                    placeholder = { Text("https://music.example.com") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(user, { user = it }, label = { Text("Username") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(password, { password = it }, label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(), singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                Button(
                    onClick = {
                        busy = true
                        error = null
                        scope.launch {
                            onConnect(url.trim(), user.trim(), password).onFailure {
                                error = it.message ?: "Could not connect to server"
                            }
                            busy = false
                        }
                    },
                    enabled = !busy && url.isNotBlank() && user.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (busy) "Connecting…" else "Connect") }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Use HTTPS for servers outside your trusted local network.", color = muted, fontSize = 12.sp)
    }
}

@Composable
private fun LibraryScreen(
    client: SubsonicClient,
    nowPlaying: Song?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    playerColor: Color,
    playerBackdropColor: Color,
    queue: List<Song>,
    currentIndex: Int,
    positionMs: Long,
    durationMs: Long,
    onPlay: (Song, List<Song>) -> Unit,
    onTogglePlayback: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onAddNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onShareSong: (Song) -> Unit,
    onShareCollection: (String) -> Unit,
    onArtworkColor: suspend (String?) -> Color,
    onRemoveFromQueue: (Int) -> Unit,
    onRestoreQueueItem: (Song, Int) -> Unit,
    onMoveInQueue: (Int, Int) -> Unit,
    onPlayQueueIndex: (Int) -> Unit,
    onShuffleSongs: (List<Song>) -> Unit,
    onDisconnect: () -> Unit,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onReadPosition: () -> Pair<Long, Long>,
    darkMode: Boolean,
    onLightSystemBars: (Boolean) -> Unit,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onClearUpcoming: () -> Unit,
    playbackSpeed: Float,
    onChangePlaybackSpeed: (Float) -> Unit,
) {
    var tab by remember { mutableStateOf(Tab.Home) }
    var detail by remember { mutableStateOf<Detail?>(null) }
    val detailBackStack = remember { mutableStateListOf<Detail>() }
    fun openDetail(next: Detail) {
        detail?.let(detailBackStack::add)
        detail = next
    }
    fun goBack() {
        if (detailBackStack.isNotEmpty()) detail = detailBackStack.removeAt(detailBackStack.lastIndex)
        else detail = null
    }
    fun selectTab(next: Tab) {
        tab = next
        detail = null
        detailBackStack.clear()
    }
    var albums by remember { mutableStateOf(emptyList<Album>()) }
    var artistAlbums by remember { mutableStateOf(emptyList<Album>()) }
    var artistSongs by remember { mutableStateOf(emptyList<Song>()) }
    var artistInfo by remember { mutableStateOf(ArtistInfo()) }
    var artistDetails by remember { mutableStateOf<Artist?>(null) }
    var freshSongs by remember { mutableStateOf(emptyList<Song>()) }
    var artists by remember { mutableStateOf(emptyList<Artist>()) }
    var artistColumns by remember { mutableStateOf(2) }
    var artistSort by remember { mutableStateOf(ArtistSort.Name) }
    var playlists by remember { mutableStateOf(emptyList<Playlist>()) }
    var songs by remember { mutableStateOf(emptyList<Song>()) }
    var songsRevision by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(SearchResults(emptyList(), emptyList(), emptyList())) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var playerExpanded by remember { mutableStateOf(false) }
    var shufflingLibrary by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PlatformBackHandler(enabled = playerExpanded || detail != null || tab != Tab.Home) {
        when {
            playerExpanded -> playerExpanded = false
            detail != null -> goBack()
            else -> selectTab(Tab.Home)
        }
    }

    LaunchedEffect(client) {
        loading = true
        try {
            albums = client.newestAlbums()
            freshSongs = albums.take(3).flatMap { album ->
                runCatching { client.albumSongs(album.id) }.getOrDefault(emptyList())
            }.distinctBy { it.id }.take(12)
            artists = client.artists()
            playlists = client.playlists()
            error = null
        } catch (t: Throwable) {
            error = t.message ?: "Could not load your library"
        } finally {
            loading = false
        }
    }
    LaunchedEffect(client, detail, songsRevision) {
        songs = emptyList()
        artistAlbums = emptyList()
        artistSongs = emptyList()
        artistInfo = ArtistInfo()
        artistDetails = null
        try {
            when (val page = detail) {
                is Detail.ArtistPage -> {
                    artistAlbums = runCatching { client.artistAlbums(page.artist.id) }
                        .getOrDefault(emptyList()).sortedByDescending { it.releaseOrder }
                    artistDetails = runCatching { client.artistDetails(page.artist.id) }.getOrNull()
                    artistInfo = runCatching { client.artistInfo(page.artist.id) }.getOrDefault(ArtistInfo())
                    artistSongs = runCatching { client.artistTopSongs(page.artist.name) }
                        .getOrDefault(emptyList()).ifEmpty {
                            artistAlbums.take(12).flatMap { album ->
                                runCatching { client.albumSongs(album.id) }.getOrDefault(emptyList())
                            }.distinctBy { it.id }.sortedByDescending { it.playCount }
                        }
                }
                is Detail.ArtistSectionPage -> {
                    artistAlbums = runCatching { client.artistAlbums(page.artist.id) }
                        .getOrDefault(emptyList()).sortedByDescending { it.releaseOrder }
                    artistSongs = runCatching { client.artistTopSongs(page.artist.name) }
                        .getOrDefault(emptyList()).ifEmpty {
                            artistAlbums.take(12).flatMap { album ->
                                runCatching { client.albumSongs(album.id) }.getOrDefault(emptyList())
                            }.distinctBy { it.id }.sortedByDescending { it.playCount }
                        }
                }
                is Detail.AlbumPage -> songs = client.albumSongs(page.album.id)
                is Detail.PlaylistPage -> songs = client.playlistSongs(page.playlist.id)
                else -> Unit
            }
        } catch (t: Throwable) { error = t.message ?: "Could not load songs" }
    }
    LaunchedEffect(client, query, tab) {
        if (tab == Tab.Search && query.isNotBlank()) {
            delay(300)
            try { results = client.search(query); error = null }
            catch (t: Throwable) { error = t.message ?: "Search failed" }
        } else results = SearchResults(emptyList(), emptyList(), emptyList())
    }

    val isHome = detail == null && tab == Tab.Home
    val lightBars = !darkMode && !(playerExpanded && nowPlaying != null)
    LaunchedEffect(lightBars) {
        onLightSystemBars(lightBars)
    }

    val shuffleAll: () -> Unit = {
        if (!shufflingLibrary) scope.launch {
            shufflingLibrary = true
            try {
                val all = client.allSongs()
                if (all.isNotEmpty()) onShuffleSongs(all)
                else error = "Your library has no songs to shuffle"
            } catch (t: Throwable) {
                error = t.message ?: "Could not shuffle your library"
            } finally {
                shufflingLibrary = false
            }
        }
    }

    val chromeSky = rememberSky()
    LaunchedEffect(tab, detail, loading, songs, artists, playlists, results) {
        chromeSky.invalidate(durationMillis = 240)
    }
    val chromeSpace = if (nowPlaying == null) 120.dp else when (settings.miniPlayerSize) {
        MiniPlayerSize.Small -> 180.dp
        MiniPlayerSize.Medium -> 194.dp
        MiniPlayerSize.Large -> 210.dp
    }
    Box(Modifier.fillMaxSize()) {
    if (isHome || detail is Detail.ArtistPage || detail is Detail.ArtistSectionPage || detail is Detail.AlbumPage || detail is Detail.PlaylistPage) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().sky(chromeSky)) {
                when (val page = detail) {
                    is Detail.ArtistPage -> ArtistReferenceScreen(
                        artistDetails ?: page.artist, artistAlbums, artistSongs, artistInfo, client, darkMode,
                        onBack = ::goBack,
                        onAlbum = { openDetail(Detail.AlbumPage(it)) },
                        onSong = { onPlay(it, artistSongs) },
                        onPlayAll = { artistSongs.firstOrNull()?.let { song ->
                            onPlay(song, artistSongs)
                        } },
                        onShuffle = { onShuffleSongs(artistSongs) },
                        onAddNext = onAddNext,
                        onShareSong = onShareSong,
                        onShareArtist = onShareCollection,
                        onArtist = { openDetail(Detail.ArtistPage(it)) },
                        onSection = { openDetail(Detail.ArtistSectionPage(page.artist, it)) },
                    )
                    is Detail.ArtistSectionPage -> ArtistSectionScreen(
                        page.artist, page.section, artistAlbums, artistSongs, client, darkMode,
                        onBack = ::goBack,
                        onAlbum = { openDetail(Detail.AlbumPage(it)) },
                        onSong = { onPlay(it, artistSongs) },
                        onAddNext = onAddNext,
                        onShareSong = onShareSong,
                        onArtist = { openDetail(Detail.ArtistPage(it)) },
                    )
                    is Detail.AlbumPage -> AlbumReferenceScreen(
                        page.album, songs, client, darkMode,
                        onBack = ::goBack,
                        onPlaySong = { onPlay(it, songs) },
                        onPlayAll = { songs.firstOrNull()?.let { song -> onPlay(song, songs) } },
                        onShuffle = { onShuffleSongs(songs) },
                        onAddNext = onAddNext,
                        onShare = onShareCollection,
                        onArtworkColor = onArtworkColor,
                        onAlbum = { openDetail(Detail.AlbumPage(it)) },
                        onArtist = { openDetail(Detail.ArtistPage(it)) },
                    )
                    is Detail.PlaylistPage -> PlaylistReferenceScreen(
                        page.playlist, songs, client, darkMode,
                        onBack = ::goBack,
                        onPlaySong = { onPlay(it, songs) },
                        onPlayAll = { songs.firstOrNull()?.let { song -> onPlay(song, songs) } },
                        onShuffle = { onShuffleSongs(songs) },
                        onAddNext = onAddNext,
                        onShare = onShareCollection,
                        onArtworkColor = onArtworkColor,
                        onPlaylistChanged = { songsRevision++ },
                        onAlbum = { openDetail(Detail.AlbumPage(it)) },
                        onArtist = { openDetail(Detail.ArtistPage(it)) },
                    )
                    else -> ReferenceHomeScreen(
                        albums, freshSongs, client, darkMode, loading, error,
                        shufflingLibrary, shuffleAll,
                        onAlbum = { openDetail(Detail.AlbumPage(it)) },
                        onPlaySong = { onPlay(it, freshSongs) },
                        onAddNext = onAddNext,
                        onAddToQueue = onAddToQueue,
                        onSettings = { openDetail(Detail.SettingsPage) },
                    )
                }
            }
        }
    } else {
    Box(Modifier.fillMaxSize().sky(chromeSky)) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .statusBarsPadding().padding(top = 20.dp)) {
        AppToolbar(
            title = when (val page = detail) {
                is Detail.ArtistPage -> page.artist.name
                is Detail.ArtistSectionPage -> page.artist.name
                is Detail.AlbumPage -> page.album.name
                is Detail.PlaylistPage -> page.playlist.name
                Detail.QueuePage -> "Queue"
                Detail.SettingsPage -> "Settings"
                null -> when (tab) { Tab.Home -> "Listen Now"; Tab.Artists -> "Artists";
                    Tab.Playlists -> "Playlists"; Tab.Search -> "Search" }
            },
            username = client.credentials.username,
            darkMode = darkMode,
            onSettings = if (detail == Detail.SettingsPage) null else ({ openDetail(Detail.SettingsPage) }),
            onBack = if (detail != null) ::goBack else null,
        )
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

        Box(Modifier.weight(1f)) {
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = accent)
            else when (val page = detail) {
                is Detail.AlbumPage -> SongList(songs, client, onPlay, onAddNext, onAddToQueue,
                    onShuffleSongs, chromeSpace)
                is Detail.PlaylistPage -> SongList(songs, client, onPlay, onAddNext, onAddToQueue,
                    onShuffleSongs, chromeSpace)
                is Detail.ArtistPage -> AlbumList(artistAlbums, client, chromeSpace) {
                    openDetail(Detail.AlbumPage(it))
                }
                is Detail.ArtistSectionPage -> Unit
                Detail.QueuePage -> QueueScreen(queue, currentIndex, client,
                    onPlayQueueIndex, onRemoveFromQueue, onRestoreQueueItem, onMoveInQueue, onAddNext, onAddToQueue,
                    chromeSpace)
                Detail.SettingsPage -> SettingsScreen(settings, onSettingsChange, onDisconnect, chromeSpace)
                null -> when (tab) {
                    Tab.Home -> Unit
                    Tab.Artists -> ArtistList(artists, client, chromeSpace, artistColumns,
                        { artistColumns = it }, artistSort, { artistSort = it }) {
                        openDetail(Detail.ArtistPage(it))
                    }
                    Tab.Playlists -> PlaylistList(playlists, client, chromeSpace) { openDetail(Detail.PlaylistPage(it)) }
                    Tab.Search -> SearchContent(query, { query = it }, results, client,
                        { openDetail(Detail.ArtistPage(it)) }, { openDetail(Detail.AlbumPage(it)) },
                        { onPlay(it, listOf(it)) }, onAddNext, onAddToQueue, chromeSpace)
                }
            }
        }
    }
    }
    }
    ReferenceChrome(
        chromeSky, client, nowPlaying, isPlaying, darkMode,
        selectedTab = when (tab) {
            Tab.Home -> 0; Tab.Artists -> 1; Tab.Playlists -> 2; Tab.Search -> 3
        },
        settings = settings,
        onHome = { selectTab(Tab.Home) },
        onArtists = { selectTab(Tab.Artists) },
        onPlaylists = { selectTab(Tab.Playlists) },
        onSearch = { selectTab(Tab.Search) },
        onExpandPlayer = { playerExpanded = true },
        onToggle = onTogglePlayback,
        onNext = onSkipNext,
        onPrevious = onSkipPrevious,
        onAddNext = onAddNext,
        onAddToQueue = onAddToQueue,
    )
    AnimatedVisibility(
        visible = playerExpanded && nowPlaying != null,
        modifier = Modifier.fillMaxSize().zIndex(1f),
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            initialOffsetY = { it },
        ),
        exit = slideOutVertically(animationSpec = tween(250), targetOffsetY = { it }),
        label = "Now playing rise",
    ) {
        nowPlaying?.let { song ->
            ReferencePlayerScreen(
                client, song, isPlaying, isBuffering, playerColor, playerBackdropColor, positionMs, durationMs,
                queue, currentIndex, settings, onTogglePlayback, onSkipNext, onSkipPrevious,
                onSeek, { playerExpanded = false }, onPlayQueueIndex, onRemoveFromQueue, onRestoreQueueItem,
                onMoveInQueue,
                onViewAlbum = { selected ->
                    selected.albumId?.let { id ->
                        openDetail(Detail.AlbumPage(Album(id, selected.album, selected.artist, selected.coverArt)))
                        playerExpanded = false
                    }
                },
                onViewArtist = { selected ->
                    openDetail(Detail.ArtistPage(selected))
                    playerExpanded = false
                },
                onShareSong = onShareSong,
                onReadPosition = onReadPosition,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                onClearUpcoming = onClearUpcoming,
                playbackSpeed = playbackSpeed,
                onChangePlaybackSpeed = onChangePlaybackSpeed,
            )
        }
    }
    }
}

@Composable
private fun AppleHomeScreen(
    albums: List<Album>, client: SubsonicClient, nowPlaying: Song?, isPlaying: Boolean,
    loading: Boolean, error: String?, shuffling: Boolean, onShuffleAll: () -> Unit,
    settings: AppSettings, onAlbum: (Album) -> Unit, onSettings: () -> Unit,
    onExpandPlayer: () -> Unit, onTogglePlayback: () -> Unit, onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit, onAddNext: (Song) -> Unit, onAddToQueue: (Song) -> Unit,
    onNavigate: (Tab) -> Unit,
) {
    val sky = rememberSky()
    val pill = RoundedCornerShape(50.dp)
    val glassTint = Color.White.copy(alpha = 0.76f + settings.glassIntensity * 0.14f)
    val glassEdge = Brush.linearGradient(listOf(Color.White, Color.White.copy(alpha = 0.38f)))
    var quickMenuOpen by remember { mutableStateOf(false) }
    var swipeX by remember(nowPlaying?.id) { mutableFloatStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { settings.gestures.sensitivityDp.dp.toPx() }

    Box(Modifier.fillMaxSize().background(Color(0xFFFAFAFC))) {
        LazyColumn(Modifier.fillMaxSize().sky(sky),
            contentPadding = PaddingValues(top = 14.dp, bottom = 192.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 21.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Home", color = homeInk, fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Box(Modifier.size(46.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFB8CAE5), Color(0xFF6679AE))))
                        .clickable(onClick = onSettings), contentAlignment = Alignment.Center) {
                        Text(client.credentials.username.take(2).uppercase(), color = Color.White,
                            fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            item {
                Column(Modifier.fillMaxWidth().padding(start = 21.dp, end = 21.dp, top = 25.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFF2F2F8)).padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(58.dp).clip(RoundedCornerShape(17.dp))
                            .background(Color(0xFFFFE5E9)), contentAlignment = Alignment.Center) {
                            Icon(MaterialSymbols.RoundedFilled.Library_music, null, tint = Color(0xFFE34855),
                                modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Text("Your music, ready whenever you are.", color = homeInk,
                            fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(22.dp))
                    Box(Modifier.fillMaxWidth().height(50.dp).clip(pill)
                        .background(Color(0xFFE3EFFF)).clickable(enabled = !shuffling, onClick = onShuffleAll),
                        contentAlignment = Alignment.Center) {
                        Text(if (shuffling) "Loading your library…" else "Shuffle Your Library",
                            color = homeBlue, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (error != null) item {
                Text(error, color = Color(0xFFB12632), fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 21.dp, vertical = 12.dp))
            }
            item {
                Text("Fresh in Your Library", color = homeInk, fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 21.dp, top = 28.dp, bottom = 14.dp))
            }
            item {
                if (loading) CircularProgressIndicator(
                    modifier = Modifier.padding(30.dp), color = homeBlue)
                else LazyRow(contentPadding = PaddingValues(horizontal = 21.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(albums.take(12), key = { it.id }) { album ->
                        HomeAlbumCard(album, client, onAlbum)
                    }
                }
            }
            item {
                Text("Recently Added", color = homeInk, fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 21.dp, top = 34.dp, bottom = 14.dp))
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 21.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(albums.drop(2).take(12), key = { it.id }) { album ->
                        HomeAlbumCard(album, client, onAlbum)
                    }
                }
            }
        }

        Column(Modifier.align(Alignment.BottomCenter).padding(horizontal = 19.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (nowPlaying != null) {
                Row(Modifier.fillMaxWidth().height(68.dp)
                    .shadow(17.dp, pill).clip(pill)
                    .cloudy(sky = sky, radius = 48, tint = glassTint, shape = pill)
                    .background(Color.White.copy(alpha = 0.62f))
                    .border(1.dp, glassEdge, pill)
                    .then(if (settings.gestures.miniPlayerSwipe) Modifier.pointerInput(nowPlaying.id, swipeThreshold) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, amount -> swipeX += amount; change.consume() },
                            onDragEnd = {
                                if (swipeX < -swipeThreshold) onSkipNext()
                                else if (swipeX > swipeThreshold) onSkipPrevious()
                                swipeX = 0f
                            }, onDragCancel = { swipeX = 0f })
                    } else Modifier)
                    .combinedClickable(onClick = onExpandPlayer,
                        onLongClick = if (settings.gestures.miniPlayerLongPress) ({ quickMenuOpen = true }) else null)
                    .padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Artwork(client, nowPlaying.coverArt, Modifier.size(48.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(nowPlaying.title, color = homeInk, fontWeight = FontWeight.Medium,
                            fontSize = 14.sp, maxLines = 1, softWrap = false,
                            overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                        Text(nowPlaying.artist, color = homeMuted, fontSize = 11.sp,
                            maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                            modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                    }
                    IconButton(onClick = onTogglePlayback) {
                        Icon(if (isPlaying) MaterialSymbols.RoundedFilled.Pause else MaterialSymbols.RoundedFilled.Play_arrow,
                            if (isPlaying) "Pause" else "Play", tint = homeInk,
                            modifier = Modifier.size(30.dp))
                    }
                    IconButton(onClick = onSkipNext) {
                        Icon(MaterialSymbols.RoundedFilled.Skip_next, "Next", tint = homeInk,
                            modifier = Modifier.size(30.dp))
                    }
                }
                DropdownMenu(expanded = quickMenuOpen, onDismissRequest = { quickMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("Play next") }, onClick = {
                        quickMenuOpen = false; onAddNext(nowPlaying)
                    })
                    DropdownMenuItem(text = { Text("Add to queue") }, onClick = {
                        quickMenuOpen = false; onAddToQueue(nowPlaying)
                    })
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.weight(1f).height(74.dp)
                    .shadow(17.dp, pill).clip(pill)
                    .cloudy(sky = sky, radius = 52, tint = glassTint, shape = pill)
                    .background(Color.White.copy(alpha = 0.26f))
                    .border(1.dp, glassEdge, pill).padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically) {
                    listOf(Tab.Home to MaterialSymbols.RoundedFilled.Home,
                        Tab.Artists to MaterialSymbols.RoundedFilled.Library_music,
                        Tab.Playlists to MaterialSymbols.RoundedFilled.Playlist_play).forEach { (tab, icon) ->
                        Column(Modifier.weight(1f).fillMaxSize().clip(pill)
                            .background(if (tab == Tab.Home) Color(0x1F666A78) else Color.Transparent)
                            .clickable { onNavigate(tab) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Icon(icon, tab.name, tint = if (tab == Tab.Home) Color(0xFFCF333B) else homeInk,
                                modifier = Modifier.size(25.dp))
                            Text(tab.name, color = if (tab == Tab.Home) Color(0xFFCF333B) else homeInk,
                                fontSize = 10.sp, fontWeight = FontWeight.Normal)
                        }
                    }
                }
                Box(Modifier.size(74.dp).shadow(17.dp, CircleShape).clip(CircleShape)
                    .cloudy(sky = sky, radius = 52, tint = glassTint, shape = CircleShape)
                    .background(Color.White.copy(alpha = 0.26f))
                    .border(1.dp, glassEdge, CircleShape)
                    .clickable { onNavigate(Tab.Search) }, contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbols.RoundedFilled.Search, "Search", tint = homeInk,
                        modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeAlbumCard(album: Album, client: SubsonicClient, onAlbum: (Album) -> Unit) {
    val artUrl = remember(client, album.coverArt) { album.coverArt?.let { client.coverArtUrl(it, 600) } }
    Column(Modifier.width(252.dp).clickable { onAlbum(album) }) {
        Box(Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFD8E0EB))) {
            if (artUrl != null) AsyncImage(model = artUrl, contentDescription = "Album artwork",
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxWidth().height(84.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.68f)))))
            Text(album.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
        }
        Spacer(Modifier.height(7.dp))
        Text(album.artist, color = homeMuted, fontSize = 13.sp,
            maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
    }
}

@Composable
private fun AlbumList(albums: List<Album>, client: SubsonicClient, bottomPadding: Dp,
                      onAlbum: (Album) -> Unit) =
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPadding)) {
        items(albums, key = { it.id }) { AlbumRow(it, client) { onAlbum(it) } }
    }

@Composable
private fun AlbumRow(album: Album, client: SubsonicClient, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Artwork(client, album.coverArt, Modifier.size(62.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(album.name, color = ink, maxLines = 1, softWrap = false,
                overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
            Text(album.artist, color = muted, fontSize = 13.sp, maxLines = 1,
                softWrap = false, overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistList(artists: List<Artist>, client: SubsonicClient, bottomPadding: Dp,
                       columns: Int, onColumns: (Int) -> Unit,
                       sort: ArtistSort, onSort: (ArtistSort) -> Unit,
                       onArtist: (Artist) -> Unit) {
    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortMenu by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    val visible = remember(artists, searchQuery, sort) {
        sortArtists(artists.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }, sort)
    }
    val pullDistance = with(LocalDensity.current) { 48.dp.toPx() }
    val hideDistance = with(LocalDensity.current) { 24.dp.toPx() }
    val scrollConnection = remember(gridState, pullDistance, hideDistance) {
        object : NestedScrollConnection {
            var pull = 0f
            var away = 0f
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && !gridState.canScrollBackward && available.y > 0f) {
                    pull += available.y
                    away = 0f
                    if (pull >= pullDistance) { searchVisible = true; pull = 0f }
                } else if (source == NestedScrollSource.UserInput && consumed.y < 0f) {
                    away -= consumed.y
                    pull = 0f
                    if (away >= hideDistance) { searchVisible = false; searchQuery = ""; away = 0f }
                } else if (source == NestedScrollSource.UserInput) {
                    pull = 0f
                    away = 0f
                }
                return Offset.Zero
            }
        }
    }
    LazyVerticalGrid(columns = GridCells.Fixed(columns), state = gridState,
        modifier = Modifier.fillMaxSize().nestedScroll(scrollConnection),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = bottomPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.padding(top = 18.dp, bottom = 20.dp)) {
                if (searchVisible) {
                    val searchShape = CircleShape
                    BasicTextField(searchQuery, onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = ink, fontSize = 16.sp),
                        cursorBrush = SolidColor(ink),
                        modifier = Modifier.fillMaxWidth().clip(searchShape)
                            .background(ink.copy(alpha = 0.08f))
                            .border(1.dp, ink.copy(alpha = 0.11f), searchShape)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        decorationBox = { field ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(MaterialSymbols.RoundedFilled.Search, null, tint = muted,
                                    modifier = Modifier.size(21.dp))
                                Spacer(Modifier.width(12.dp))
                                Box(Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) Text("Find an artist", color = muted)
                                    field()
                                }
                            }
                        })
                    Spacer(Modifier.height(16.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.clip(CircleShape).background(glass)
                        .border(1.dp, ink.copy(alpha = 0.12f), CircleShape).padding(4.dp)) {
                        listOf(1 to "List", 2 to "2", 3 to "3").forEach { (count, label) ->
                            Box(Modifier.width(if (count == 1) 52.dp else 40.dp).height(32.dp)
                                .clip(CircleShape)
                                .background(if (columns == count) ink.copy(alpha = 0.16f) else Color.Transparent)
                                .clickable { onColumns(count) }, contentAlignment = Alignment.Center) {
                                Text(label, color = if (columns == count) ink else muted, fontSize = 13.sp,
                                    fontWeight = if (columns == count) FontWeight.SemiBold else FontWeight.Medium)
                            }
                        }
                    }
                    Row(Modifier.height(40.dp).clip(CircleShape).background(glass)
                        .border(1.dp, ink.copy(alpha = 0.12f), CircleShape)
                        .clickable { sortMenu = true }.padding(horizontal = 13.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Sort · " + when (sort) {
                            ArtistSort.Name -> "A–Z"; ArtistSort.NameReverse -> "Z–A"
                            ArtistSort.MostAlbums -> "Most"; ArtistSort.FewestAlbums -> "Fewest"
                        }, color = ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(3.dp))
                        Icon(MaterialSymbols.RoundedFilled.Keyboard_arrow_down, null, tint = muted,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        items(visible.size, key = { visible[it].id }) { index ->
            val artist = visible[index]
            ArtistTile(artist, client, columns) { onArtist(artist) }
        }
    }
    if (sortMenu) ModalBottomSheet(onDismissRequest = { sortMenu = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.28f),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }, dragHandle = null) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(MaterialTheme.colorScheme.surface).navigationBarsPadding().padding(bottom = 22.dp)) {
            SheetHandle()
            Text("Sort artists", color = ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 24.dp, top = 14.dp, bottom = 15.dp))
            listOf(ArtistSort.Name to "Name A–Z", ArtistSort.NameReverse to "Name Z–A",
                ArtistSort.MostAlbums to "Most albums", ArtistSort.FewestAlbums to "Fewest albums")
                .forEach { (order, label) ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (sort == order) glass else Color.Transparent)
                        .clickable { onSort(order); sortMenu = false }
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = ink, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        if (sort == order) Icon(MaterialSymbols.RoundedFilled.Check, null, tint = ink)
                    }
                }
        }
    }
}

@Composable
private fun ArtistTile(artist: Artist, client: SubsonicClient, columns: Int, onClick: () -> Unit) {
    var portrait by remember(artist.id) { mutableStateOf(artist.imageUrl) }
    LaunchedEffect(artist.id, client) {
        if (portrait == null) portrait = runCatching { client.artistImageUrl(artist.id) }.getOrNull()
    }
    val picture: @Composable (Modifier) -> Unit = { modifier ->
        Box(modifier.clip(CircleShape).background(glass), contentAlignment = Alignment.Center) {
            if (portrait != null) AsyncImage(portrait, "${artist.name} portrait",
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text(artist.name.take(1), color = ink, style = MaterialTheme.typography.headlineMedium)
        }
    }
    if (columns == 1) Row(Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(vertical = 7.dp, horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        picture(Modifier.size(60.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(artist.name, color = ink, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${artist.albumCount} albums", color = muted, fontSize = 13.sp)
        }
    } else Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        picture(Modifier.fillMaxWidth().aspectRatio(1f))
        Spacer(Modifier.height(8.dp))
        Text(artist.name, color = ink, fontSize = if (columns == 2) 15.sp else 13.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PlaylistList(playlists: List<Playlist>, client: SubsonicClient, bottomPadding: Dp,
                         onPlaylist: (Playlist) -> Unit) =
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPadding)) {
        items(playlists, key = { it.id }) { playlist ->
        Row(Modifier.fillMaxWidth().clickable { onPlaylist(playlist) }.padding(horizontal = 22.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Artwork(client, playlist.coverArt, Modifier.size(60.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(playlist.name, color = ink, fontSize = 17.sp, maxLines = 1,
                    softWrap = false, overflow = TextOverflow.Clip,
                    modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                Text("${playlist.songCount} songs", color = muted, fontSize = 13.sp)
            }
        }
    } }

@Composable
private fun SongList(songs: List<Song>, client: SubsonicClient,
                     onPlay: (Song, List<Song>) -> Unit,
                     onAddNext: (Song) -> Unit, onAddToQueue: (Song) -> Unit,
                     onShuffle: (List<Song>) -> Unit, bottomPadding: Dp) =
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPadding)) {
        if (songs.isNotEmpty()) item {
            TextButton(onClick = { onShuffle(songs) }, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Shuffle", color = accent)
            }
        }
        items(songs, key = { it.id }) { song ->
        SongRow(song, client, onClick = { onPlay(song, songs) },
            onAddNext = { onAddNext(song) }, onAddToQueue = { onAddToQueue(song) })
        }
    }

@Composable
private fun SongRow(song: Song, client: SubsonicClient, onClick: () -> Unit,
                    onAddNext: () -> Unit, onAddToQueue: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Artwork(client, song.coverArt, Modifier.size(52.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = ink, maxLines = 1, softWrap = false,
                overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isExplicit) {
                    ExplicitBadge(color = muted)
                    Spacer(Modifier.width(5.dp))
                }
                Text(song.artist, color = muted, fontSize = 13.sp, maxLines = 1,
                    softWrap = false, overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE))
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(MaterialSymbols.RoundedFilled.More_vert, "Song actions", tint = muted)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Play next") }, onClick = {
                    menuOpen = false
                    onAddNext()
                })
                DropdownMenuItem(text = { Text("Add to queue") }, onClick = {
                    menuOpen = false
                    onAddToQueue()
                })
            }
        }
    }
}

@Composable
private fun SearchContent(
    query: String,
    onQuery: (String) -> Unit,
    results: SearchResults,
    client: SubsonicClient,
    onArtist: (Artist) -> Unit,
    onAlbum: (Album) -> Unit,
    onSong: (Song) -> Unit,
    onAddNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    bottomPadding: Dp,
) {
    Column {
        OutlinedTextField(query, onQuery, label = { Text("Search your library") },
            leadingIcon = { Icon(MaterialSymbols.RoundedFilled.Search, null) }, singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp))
        LazyColumn(contentPadding = PaddingValues(bottom = bottomPadding)) {
            if (results.artists.isNotEmpty()) item { SectionTitle("Artists") }
            items(results.artists, key = { "artist:${it.id}" }) { artist ->
                Row(Modifier.fillMaxWidth().clickable { onArtist(artist) }.padding(22.dp)) {
                    Text(artist.name, color = ink)
                }
            }
            if (results.albums.isNotEmpty()) item { SectionTitle("Albums") }
            items(results.albums, key = { "album:${it.id}" }) { AlbumRow(it, client) { onAlbum(it) } }
            if (results.songs.isNotEmpty()) item { SectionTitle("Songs") }
            items(results.songs, key = { "song:${it.id}" }) { song ->
                SongRow(song, client, onClick = { onSong(song) },
                    onAddNext = { onAddNext(song) }, onAddToQueue = { onAddToQueue(song) })
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) = Text(text, color = accent, fontSize = 18.sp,
    fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 22.dp, top = 18.dp, bottom = 5.dp))

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    onChange: (AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    bottomPadding: Dp,
) {
    val gestures = settings.gestures
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Text("Appearance", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 20.dp, bottom = 14.dp))
        Text("Theme", color = ink, fontSize = 16.sp)
        Text("Choose a light or dark look, or follow your phone.", color = muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemePreference.entries.forEach { mode ->
                val selected = settings.themePreference == mode
                Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                    .background(if (selected) ink else ink.copy(alpha = 0.09f))
                    .selectable(selected = selected, role = Role.RadioButton,
                        onClick = { onChange(settings.copy(themePreference = mode)) })
                    .padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(mode.name, color = if (selected) MaterialTheme.colorScheme.background else ink,
                        fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Mini player size", color = ink, fontSize = 16.sp)
        Text("Choose a compact, balanced, or larger player.", color = muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        SettingsChoices(MiniPlayerSize.entries.map { it.name }, settings.miniPlayerSize.ordinal) { index ->
            onChange(settings.copy(miniPlayerSize = MiniPlayerSize.entries[index]))
        }
        Spacer(Modifier.height(20.dp))
        Text("Navigation style", color = ink, fontSize = 16.sp)
        Text("A clean, flat bar or Glaze’s glass pill.", color = muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        SettingsChoices(listOf("Minimal", "Glaze"), settings.navigationStyle.ordinal) { index ->
            onChange(settings.copy(navigationStyle = NavigationStyle.entries[index]))
        }
        Spacer(Modifier.height(20.dp))
        Text("Navigation size", color = ink, fontSize = 16.sp)
        Text("Adjust the height and icon size of the bottom bar.", color = muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        SettingsChoices(NavigationSize.entries.map { it.name }, settings.navigationSize.ordinal) { index ->
            onChange(settings.copy(navigationSize = NavigationSize.entries[index]))
        }
        if (settings.navigationStyle == NavigationStyle.Glaze) {
            SettingsToggle("Search in navigation", "Turn off for a separate search button",
                settings.searchInNavigation) {
                onChange(settings.copy(searchInNavigation = it))
            }
        }
        SettingsToggle("Navigation labels", "Show text below the navigation icons",
            settings.navigationLabels) {
            onChange(settings.copy(navigationLabels = it))
        }
        Spacer(Modifier.height(12.dp))
        Text("Glass intensity", color = ink, fontSize = 16.sp)
        Text("Adjust the translucency of player controls and navigation.", color = muted, fontSize = 13.sp)
        Slider(value = settings.glassIntensity.coerceIn(0f, 1f),
            onValueChange = { onChange(settings.copy(glassIntensity = it)) })
        Text("Gestures", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 18.dp, bottom = 10.dp))
        SettingsToggle("Swipe mini player", "Skip to the previous or next song",
            gestures.miniPlayerSwipe) {
            onChange(settings.copy(gestures = gestures.copy(miniPlayerSwipe = it)))
        }
        SettingsToggle("Swipe player down", "Close the full-screen player",
            gestures.playerSwipeDown) {
            onChange(settings.copy(gestures = gestures.copy(playerSwipeDown = it)))
        }
        SettingsToggle("Long-press mini player", "Open quick song actions",
            gestures.miniPlayerLongPress) {
            onChange(settings.copy(gestures = gestures.copy(miniPlayerLongPress = it)))
        }
        Spacer(Modifier.height(18.dp))
        Text("Swipe distance · ${gestures.sensitivityDp.roundToInt()} dp", color = ink, fontSize = 16.sp)
        Text("Increase to make gestures less sensitive.", color = muted, fontSize = 13.sp)
        Slider(value = gestures.sensitivityDp.coerceIn(40f, 160f), valueRange = 40f..160f,
            onValueChange = { onChange(settings.copy(gestures = gestures.copy(sensitivityDp = it))) })
        Text("Playback", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 18.dp, bottom = 10.dp))
        SettingsToggle("Smart shuffle", "Explore underplayed songs and avoid recent repeats",
            settings.smartShuffle) {
            onChange(settings.copy(smartShuffle = it))
        }
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onDisconnect) {
            Text("Disconnect from server", color = ink)
        }
        Spacer(Modifier.height(bottomPadding))
    }
}

@Composable
private fun SettingsChoices(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            val active = selected == index
            Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                .background(if (active) ink else ink.copy(alpha = 0.09f))
                .selectable(selected = active, role = Role.RadioButton, onClick = { onSelect(index) })
                .padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text(label, color = if (active) MaterialTheme.colorScheme.background else ink,
                    fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SettingsToggle(title: String, description: String, checked: Boolean,
                           onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().toggleable(value = checked, onValueChange = onChange)
        .padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = ink, fontSize = 16.sp)
            Text(description, color = muted, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun MiniPlayer(client: SubsonicClient, song: Song, isPlaying: Boolean,
                       onToggle: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit,
                       onExpand: () -> Unit, onQueue: () -> Unit,
                       onAddNext: () -> Unit, onAddToQueue: () -> Unit,
                       onGoAlbum: () -> Unit, onGoArtist: () -> Unit,
                       gestureConfig: GestureConfig, glassIntensity: Float) {
    var quickMenuOpen by remember { mutableStateOf(false) }
    var swipeX by remember(song.id) { mutableFloatStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { gestureConfig.sensitivityDp.dp.toPx() }
    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp)) {
    Row(Modifier.fillMaxWidth()
        .glass(RoundedCornerShape(18.dp), glassIntensity)
        .then(if (gestureConfig.miniPlayerSwipe) Modifier.pointerInput(song.id, swipeThreshold) {
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
        .combinedClickable(onClick = onExpand,
            onLongClick = if (gestureConfig.miniPlayerLongPress) ({ quickMenuOpen = true }) else null)
        .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Artwork(client, song.coverArt, Modifier.size(48.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = ink, maxLines = 1, softWrap = false,
                overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isExplicit) {
                    ExplicitBadge(color = muted)
                    Spacer(Modifier.width(5.dp))
                }
                Text(song.artist, color = muted, fontSize = 12.sp, maxLines = 1,
                    softWrap = false, overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE))
            }
        }
        IconButton(onClick = onToggle) {
            Icon(if (isPlaying) MaterialSymbols.RoundedFilled.Pause else MaterialSymbols.RoundedFilled.Play_arrow,
                if (isPlaying) "Pause" else "Play", tint = ink)
        }
        IconButton(onClick = onNext) { Icon(MaterialSymbols.RoundedFilled.Skip_next, "Next", tint = ink) }
        IconButton(onClick = onQueue) { Icon(MaterialSymbols.RoundedFilled.Queue_music, "Queue", tint = ink) }
    }
    DropdownMenu(expanded = quickMenuOpen, onDismissRequest = { quickMenuOpen = false }) {
        DropdownMenuItem(text = { Text("Play next") }, onClick = { quickMenuOpen = false; onAddNext() })
        DropdownMenuItem(text = { Text("Add to queue") }, onClick = { quickMenuOpen = false; onAddToQueue() })
        DropdownMenuItem(text = { Text("Go to album") }, onClick = { quickMenuOpen = false; onGoAlbum() },
            enabled = song.albumId != null)
        DropdownMenuItem(text = { Text("Go to artist") }, onClick = { quickMenuOpen = false; onGoArtist() },
            enabled = song.artistId != null)
    }
    }
}

@Composable
private fun FullPlayer(client: SubsonicClient, song: Song, isPlaying: Boolean,
                       playerColor: Color,
                       positionMs: Long, durationMs: Long,
                       onToggle: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit,
                       onSeek: (Long) -> Unit, onQueue: () -> Unit,
                       gestureConfig: GestureConfig, glassIntensity: Float,
                       onReadPosition: () -> Pair<Long, Long>,
                       onDismiss: () -> Unit) {
    var scrubPosition by remember(song.id) { mutableStateOf<Float?>(null) }
    var dismissDrag by remember(song.id) { mutableFloatStateOf(0f) }
    var livePositionMs by remember(song.id) { mutableLongStateOf(positionMs) }
    var liveDurationMs by remember(song.id) { mutableLongStateOf(durationMs) }
    LaunchedEffect(song.id, positionMs, durationMs) {
        livePositionMs = positionMs
        liveDurationMs = durationMs
    }
    LaunchedEffect(song.id, isPlaying) {
        while (true) {
            val (position, duration) = onReadPosition()
            livePositionMs = position
            liveDurationMs = duration
            delay(500)
        }
    }
    val dismissThreshold = with(LocalDensity.current) { gestureConfig.sensitivityDp.dp.toPx() }
    Box(Modifier.fillMaxSize()
        .offset { IntOffset(0, dismissDrag.roundToInt()) }
        .then(if (gestureConfig.playerSwipeDown) Modifier.pointerInput(song.id, dismissThreshold) {
            detectVerticalDragGestures(
                onVerticalDrag = { change, amount ->
                    dismissDrag = (dismissDrag + amount).coerceAtLeast(0f)
                    change.consume()
                },
                onDragEnd = {
                    if (dismissDrag > dismissThreshold) onDismiss()
                    dismissDrag = 0f
                },
                onDragCancel = { dismissDrag = 0f },
            )
        } else Modifier)) {
    val backdrop = remember(client, song.coverArt) { song.coverArt?.let { client.coverArtUrl(it) } }
    if (backdrop != null) AsyncImage(model = backdrop, contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().blur(72.dp))
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
        playerColor.copy(alpha = 0.72f), Color(0xE7151420), Color(0xFF101019)))))
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.Start)) {
            Text("⌄  Library", color = ink)
        }
        Spacer(Modifier.weight(0.7f))
        Artwork(client, song.coverArt, Modifier.fillMaxWidth().aspectRatio(1f))
        Spacer(Modifier.weight(0.7f))
        Text(song.title, color = ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
        Text(song.artist, color = accent, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        val progress = scrubPosition ?: if (liveDurationMs > 0) {
            (livePositionMs.toFloat() / liveDurationMs).coerceIn(0f, 1f)
        } else 0f
        Slider(value = progress, onValueChange = { scrubPosition = it },
            onValueChangeFinished = {
                val target = ((scrubPosition ?: progress) * liveDurationMs).toLong()
                livePositionMs = target
                onSeek(target)
                scrubPosition = null
            }, enabled = liveDurationMs > 0, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime((progress * liveDurationMs).toLong()), color = muted, fontSize = 12.sp)
            Text(formatTime(liveDurationMs), color = muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) { Icon(MaterialSymbols.RoundedFilled.Skip_previous, "Previous", tint = ink,
                modifier = Modifier.size(35.dp)) }
            IconButton(onClick = onToggle, modifier = Modifier.size(76.dp)
                .glass(RoundedCornerShape(50), glassIntensity)) {
                Icon(if (isPlaying) MaterialSymbols.RoundedFilled.Pause else MaterialSymbols.RoundedFilled.Play_arrow,
                    if (isPlaying) "Pause" else "Play", tint = ink, modifier = Modifier.size(38.dp))
            }
            IconButton(onClick = onNext) { Icon(MaterialSymbols.RoundedFilled.Skip_next, "Next", tint = ink,
                modifier = Modifier.size(35.dp)) }
        }
        Spacer(Modifier.weight(0.7f))
        TextButton(onClick = onQueue) {
            Icon(MaterialSymbols.RoundedFilled.Queue_music, null, tint = ink)
            Spacer(Modifier.width(8.dp))
            Text("Queue", color = ink)
        }
        Spacer(Modifier.height(24.dp))
    }
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000).coerceAtLeast(0)
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

@Composable
private fun QueueScreen(
    queue: List<Song>,
    currentIndex: Int,
    client: SubsonicClient,
    onSelect: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onRestore: (Song, Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onAddNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    bottomPadding: Dp,
) {
    if (queue.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Your queue is empty", color = muted)
        }
        return
    }
    val rowHeightPx = with(LocalDensity.current) { 72.dp.toPx() }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var removalLocked by remember { mutableStateOf(false) }
    LaunchedEffect(removalLocked) {
        if (removalLocked) {
            delay(600)
            removalLocked = false
        }
    }
    Box(Modifier.fillMaxSize()) {
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPadding)) {
        items(queue.size, key = { "${queue[it].id}:$it" }) { index ->
            val song = queue[index]
            if (index == currentIndex) SectionTitle("Playing now")
            if (index == currentIndex + 1) {
                Column {
                    Text("UP NEXT", color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 4.dp))
                    Box(Modifier.fillMaxWidth().padding(horizontal = 22.dp).height(1.dp)
                        .background(accent.copy(alpha = 0.45f)))
                }
            }
            var dragY by remember(index, song.id) { mutableFloatStateOf(0f) }
            val dragModifier = if (index > currentIndex) Modifier
                .zIndex(if (dragY == 0f) 0f else 1f)
                .offset { IntOffset(0, dragY.roundToInt()) }
                .pointerInput(index, currentIndex, queue.size) {
                    detectDragGesturesAfterLongPress(
                        onDrag = { change, amount ->
                            dragY += amount.y
                            change.consume()
                        },
                        onDragEnd = {
                            val target = (index + (dragY / rowHeightPx).roundToInt())
                                .coerceIn(currentIndex + 1, queue.lastIndex)
                            if (target != index) onMove(index, target)
                            dragY = 0f
                        },
                        onDragCancel = { dragY = 0f },
                    )
                } else Modifier
            val (dismissState, swipeModifier) = rememberDeliberateDismissState { value ->
                if (value == SwipeToDismissBoxValue.EndToStart && index != currentIndex && !removalLocked) {
                    removalLocked = true
                    onRemove(index)
                    scope.launch {
                        if (snackbar.showSnackbar("${song.title} was deleted", "Restore",
                                duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed)
                            onRestore(song, index)
                    }
                }
            }
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = index != currentIndex,
                backgroundContent = {
                    val removing = dismissState.targetValue != SwipeToDismissBoxValue.Settled ||
                        dismissState.currentValue != SwipeToDismissBoxValue.Settled
                    Box(Modifier.fillMaxSize().background(
                        if (removing) ink.copy(alpha = 0.16f) else Color.Transparent).padding(end = 24.dp),
                        contentAlignment = Alignment.CenterEnd) {
                        if (removing) Text("Remove", color = ink)
                    }
                },
                modifier = dragModifier.then(swipeModifier),
            ) {
                SongRow(song, client, onClick = { onSelect(index) },
                    onAddNext = { onAddNext(song) }, onAddToQueue = { onAddToQueue(song) })
            }
        }
    }
    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding))
    }
}

@Composable
private fun Artwork(client: SubsonicClient, id: String?, modifier: Modifier = Modifier) {
    val url = remember(client, id) { id?.let { client.coverArtUrl(it) } }
    Box(modifier.clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF383838), Color(0xFF141414))))) {
        if (url != null) AsyncImage(model = url, contentDescription = "Album artwork",
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun Modifier.glass(shape: RoundedCornerShape, intensity: Float): Modifier = this.clip(shape)
    .background(ink.copy(alpha = 0.04f + intensity.coerceIn(0f, 1f) * 0.16f))
    .border(1.dp, ink.copy(alpha = 0.08f + intensity.coerceIn(0f, 1f) * 0.18f), shape)
