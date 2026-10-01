package com.glaze.shared

import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.Download
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Arrow_forward
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
import com.composables.icons.materialsymbols.roundedfilled.Keyboard_arrow_up
import com.composables.icons.materialsymbols.roundedfilled.Settings
import com.composables.icons.materialsymbols.roundedfilled.Sync
import com.composables.icons.materialsymbols.roundedfilled.Logout
import com.composables.icons.materialsymbols.roundedfilled.Skip_next
import com.composables.icons.materialsymbols.roundedfilled.Skip_previous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import com.glaze.shared.resources.Res
import com.glaze.shared.resources.glaze_mark
import com.glaze.shared.resources.be_vietnam_pro_bold
import com.glaze.shared.resources.be_vietnam_pro_medium
import com.glaze.shared.resources.be_vietnam_pro_regular
import com.glaze.shared.resources.be_vietnam_pro_semibold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.async
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
internal val favoriteRed = Color(0xFFFF4D71)

data class GestureConfig(
    val miniPlayerSwipe: Boolean = true,
    val playerSwipeDown: Boolean = true,
    val miniPlayerLongPress: Boolean = true,
    val sensitivityDp: Float = 80f,
)

data class AppSettings(
    val companionUrl: String = "",
    val gestures: GestureConfig = GestureConfig(),
    val glassIntensity: Float = 0.5f,
    val smartShuffle: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.System,
    val miniPlayerSize: MiniPlayerSize = MiniPlayerSize.Medium,
    val navigationSize: NavigationSize = NavigationSize.Medium,
    val navigationStyle: NavigationStyle = NavigationStyle.Glaze,
    val searchInNavigation: Boolean = true,
    val navigationLabels: Boolean = true,
    val favoritePlaylistKeys: Set<String> = emptySet(),
    val homeSections: List<HomeShelf> = HomeShelf.entries,
    val hiddenHomeSections: Set<HomeShelf> = emptySet(),
    val artistViewColumns: Int = 2,
    val playlistViewColumns: Int = 1,
    val albumViewColumns: Int = 2,
    val songViewColumns: Int = 1,
)

enum class ThemePreference { System, Light, Dark }
enum class MiniPlayerSize { Small, Medium, Large }
enum class NavigationSize { Small, Medium, Large }
enum class NavigationStyle { Glaze, Spotify }
enum class HomeShelf(val title: String) {
    Mixes("Made for you"), Upcoming("On the way"), NewLibrary("New in your library"),
    Playlists("Your playlists"), Recent("Jump back in")
}

fun parseHomeSections(saved: String?): List<HomeShelf> {
    if (saved == null) return HomeShelf.entries
    val sections = saved.split(',').mapNotNull { name -> HomeShelf.entries.firstOrNull { it.name == name } }.distinct()
    return if (saved.isNotEmpty() && sections.isEmpty()) HomeShelf.entries else sections
}

fun restoreHomeLayout(savedOrder: String?, savedHidden: String?): Pair<List<HomeShelf>, Set<HomeShelf>> {
    val saved = parseHomeSections(savedOrder)
    val order = (saved + HomeShelf.entries.filterNot { it in saved || it == HomeShelf.Upcoming }).toMutableList()
        .apply { if (HomeShelf.Upcoming !in this) add(minOf(1, size), HomeShelf.Upcoming) }
    val hidden = if (savedHidden == null) HomeShelf.entries.filterNot { it in saved || it == HomeShelf.Upcoming }.toSet()
        else savedHidden.split(',').mapNotNull { name ->
            HomeShelf.entries.firstOrNull { it.name == name }
        }.toSet()
    return order to hidden
}

internal fun moveHomeSection(sections: List<HomeShelf>, section: HomeShelf, offset: Int): List<HomeShelf> {
    val index = sections.indexOf(section)
    val target = index + offset
    if (index < 0 || target !in sections.indices) return sections
    return sections.toMutableList().apply { add(target, removeAt(index)) }
}

private enum class Tab { Home, Artists, Playlists, Search }
internal enum class ArtistSort { Name, NameReverse, MostAlbums, FewestAlbums }
internal fun sortArtists(artists: List<Artist>, order: ArtistSort): List<Artist> = when (order) {
    ArtistSort.Name -> artists.sortedBy { it.name.lowercase() }
    ArtistSort.NameReverse -> artists.sortedByDescending { it.name.lowercase() }
    ArtistSort.MostAlbums -> artists.sortedWith(compareByDescending<Artist> { it.albumCount }.thenBy { it.name.lowercase() })
    ArtistSort.FewestAlbums -> artists.sortedWith(compareBy<Artist> { it.albumCount }.thenBy { it.name.lowercase() })
}
internal enum class ArtistSection { TopSongs, Albums, Singles }
internal enum class HomeSection(val title: String) {
    NewLibrary("New in your library"), Recent("Jump back in")
}
private sealed interface Detail {
    data class HomeSectionPage(val section: HomeSection) : Detail
    data class GenrePage(val genre: Genre) : Detail
    data class ArtistPage(val artist: Artist) : Detail
    data class ArtistSectionPage(val artist: Artist, val section: ArtistSection) : Detail
    data class AlbumPage(val album: Album) : Detail
    data class DiscoverAlbumPage(val album: DiscoverAlbum) : Detail
    data class UpcomingAlbumPage(val album: UpcomingAlbum) : Detail
    data class DiscoverArtistPage(val artist: DiscoverArtist) : Detail
    data class PlaylistPage(val playlist: Playlist) : Detail
    data object SettingsPage : Detail
    data object HomeSettingsPage : Detail
}

private data class LoadedPage(
    val songs: List<Song>, val sectionAlbums: List<Album>,
    val artistAlbums: List<Album>, val artistSongs: List<Song>,
    val artistInfo: ArtistInfo, val artistDetails: Artist?,
)

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
    onSettingsChange: ((AppSettings) -> AppSettings) -> Unit,
    onReadPosition: () -> Pair<Long, Long>,
    onLightSystemBars: (Boolean) -> Unit,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onClearUpcoming: () -> Unit,
    playbackSpeed: Float,
    onChangePlaybackSpeed: (Float) -> Unit,
    jam: JamViewState,
    profileAvatar: ImageBitmap?,
    onPickProfileAvatar: () -> Unit,
    jamActions: JamActions,
    onEnableReleaseNotifications: () -> Unit = {},
    releaseToOpen: String? = null, onReleaseOpened: () -> Unit = {},
    playerPresentation: Boolean? = null, onPlayerPresentationHandled: () -> Unit = {},
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
                    onChangePlaybackSpeed = onChangePlaybackSpeed,
                    jam = jam, profileAvatar = profileAvatar,
                    onPickProfileAvatar = onPickProfileAvatar, jamActions = jamActions,
                    onEnableReleaseNotifications = onEnableReleaseNotifications,
                    releaseToOpen = releaseToOpen, onReleaseOpened = onReleaseOpened,
                    playerPresentation = playerPresentation, onPlayerPresentationHandled = onPlayerPresentationHandled)
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

@OptIn(ExperimentalMaterial3Api::class)
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
    onSettingsChange: ((AppSettings) -> AppSettings) -> Unit,
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
    jam: JamViewState,
    profileAvatar: ImageBitmap?,
    onPickProfileAvatar: () -> Unit,
    jamActions: JamActions,
    onEnableReleaseNotifications: () -> Unit,
    releaseToOpen: String?, onReleaseOpened: () -> Unit,
    playerPresentation: Boolean?, onPlayerPresentationHandled: () -> Unit,
) {
    var tab by remember { mutableStateOf(Tab.Home) }
    var detail by remember { mutableStateOf<Detail?>(null) }
    val detailBackStack = remember { mutableStateListOf<Detail>() }
    val backPreviews = remember { mutableStateListOf<ImageBitmap?>() }
    var homePreview by remember { mutableStateOf<ImageBitmap?>(null) }
    val captureLayer = rememberGraphicsLayer()
    val navigationScope = rememberCoroutineScope()
    var navigationPending by remember { mutableStateOf(false) }
    suspend fun capturePage(): ImageBitmap? = try {
        captureLayer.toImageBitmap()
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (_: Exception) { null }
    fun openDetail(next: Detail) {
        if (navigationPending) return
        navigationPending = true
        navigationScope.launch {
            try {
                val preview = capturePage()
                detail?.let(detailBackStack::add)
                backPreviews.add(preview)
                detail = next
            } finally { navigationPending = false }
        }
    }
    fun goBack() {
        if (backPreviews.isNotEmpty()) backPreviews.removeAt(backPreviews.lastIndex)
        if (detailBackStack.isNotEmpty()) detail = detailBackStack.removeAt(detailBackStack.lastIndex)
        else detail = null
    }
    fun selectTab(next: Tab) {
        if (navigationPending) return
        if (detail == null && tab == Tab.Home && next != Tab.Home) {
            navigationPending = true
            navigationScope.launch {
                try { homePreview = capturePage() }
                finally {
                    tab = next
                    detail = null
                    detailBackStack.clear()
                    backPreviews.clear()
                    navigationPending = false
                }
            }
        } else {
            if (tab == Tab.Home && detail != null && next != Tab.Home)
                homePreview = backPreviews.firstOrNull()
            tab = next
            detail = null
            detailBackStack.clear()
            backPreviews.clear()
        }
    }
    var albums by remember { mutableStateOf(emptyList<Album>()) }
    var artistAlbums by remember { mutableStateOf(emptyList<Album>()) }
    var artistSongs by remember { mutableStateOf(emptyList<Song>()) }
    var artistInfo by remember { mutableStateOf(ArtistInfo()) }
    var artistDetails by remember { mutableStateOf<Artist?>(null) }
    var recentAlbums by remember(client) { mutableStateOf(emptyList<Album>()) }
    var searchRecentAlbums by remember(client) { mutableStateOf(emptyList<Album>()) }
    var searchFrequentAlbums by remember(client) { mutableStateOf(emptyList<Album>()) }
    var genres by remember(client) { mutableStateOf(emptyList<Genre>()) }
    var sectionAlbums by remember { mutableStateOf(emptyList<Album>()) }
    var sectionLoading by remember { mutableStateOf(false) }
    var artists by remember { mutableStateOf(emptyList<Artist>()) }
    var artistSort by remember { mutableStateOf(ArtistSort.Name) }
    var playlists by remember { mutableStateOf(emptyList<Playlist>()) }
    var songs by remember { mutableStateOf(emptyList<Song>()) }
    var songsRevision by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var discover by rememberSaveable { mutableStateOf(false) }
    var libraryRequest by remember { mutableStateOf<LibraryRequest?>(null) }
    var downloadsOpen by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf(SearchResults(emptyList(), emptyList(), emptyList())) }
    var loading by remember { mutableStateOf(true) }
    var syncing by remember(client) { mutableStateOf(false) }
    var syncProgress by remember(client) { mutableFloatStateOf(0f) }
    var syncError by remember(client) { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var playerExpanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(playerPresentation, nowPlaying) {
        if (playerPresentation != null && nowPlaying != null) {
            playerExpanded = playerPresentation
            onPlayerPresentationHandled()
        }
    }
    var jamOpen by remember { mutableStateOf(false) }
    var jamGuestControls by remember { mutableStateOf(false) }
    var jamQueueRequest by remember { mutableStateOf(0) }
    LaunchedEffect(jam.pendingInvite) {
        if (jam.pendingInvite.isNotEmpty()) jamOpen = true
    }
    LaunchedEffect(jam.sessionId) {
        if (jam.sessionId.isNotEmpty() && jamOpen && nowPlaying != null) {
            jamOpen = false
            playerExpanded = true
            jamQueueRequest++
            delay(250)
            jamGuestControls = false
            jamOpen = true
        }
    }
    var loadedDetail by remember { mutableStateOf<Detail?>(null) }
    val loadedPages = remember(client, songsRevision) { mutableMapOf<Detail, LoadedPage>() }
    val homeStateHolder = rememberSaveableStateHolder()
    val toolbarScope = rememberCoroutineScope()
    val artistGridState = rememberLazyGridState()
    val playlistGridState = rememberLazyGridState()
    val newLibraryGridState = rememberLazyGridState()
    val recentGridState = rememberLazyGridState()
    val sectionGridState = if ((detail as? Detail.HomeSectionPage)?.section == HomeSection.Recent)
        recentGridState else newLibraryGridState
    val searchListState = rememberLazyListState()
    val genreGridState = rememberLazyGridState()
    LaunchedEffect(detail) { if (detail is Detail.GenrePage) genreGridState.scrollToItem(0) }
    val settingsScrollState = rememberScrollState()
    val homeSettingsListState = rememberLazyListState()
    fun isFavorite(playlist: Playlist) =
        playlistFavoriteKey(client.credentials, playlist.id) in settings.favoritePlaylistKeys
    fun setFavorite(playlist: Playlist, favorite: Boolean) {
        val key = playlistFavoriteKey(client.credentials, playlist.id)
        onSettingsChange { current -> current.copy(
            favoritePlaylistKeys = current.favoritePlaylistKeys.withPlaylistFavorite(key, favorite)) }
    }
    val orderedPlaylists = remember(playlists, client, settings.favoritePlaylistKeys) {
        favoriteFirstPlaylists(playlists, client.credentials, settings.favoritePlaylistKeys)
    }

    val previousPreview = if (detail != null) backPreviews.lastOrNull()
        else if (tab != Tab.Home) homePreview else null
    PlatformBackHandler(enabled = playerExpanded ||
        (detail != null || tab != Tab.Home) && previousPreview == null) {
        when {
            playerExpanded -> playerExpanded = false
            detail != null -> goBack()
            else -> selectTab(Tab.Home)
        }
    }

    suspend fun synchronizeLibrary() {
        if (syncing) return
        syncing = true
        syncProgress = 0f
        syncError = null
        try {
            val snapshot = loadLibrary(client) { progress, _ ->
                syncProgress = progress
            }
            albums = snapshot.albums
            artists = snapshot.artists
            playlists = snapshot.playlists
            recentAlbums = snapshot.recentAlbums
            searchRecentAlbums = snapshot.recentAlbums
            searchFrequentAlbums = snapshot.frequentAlbums
            genres = snapshot.genres
            songsRevision++
            error = null
            syncProgress = 1f
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            syncError = "Could not synchronize your library. Check your connection and retry."
            error = syncError
        } finally {
            syncing = false
            loading = false
        }
    }
    fun requestLibrarySync() { toolbarScope.launch { synchronizeLibrary() } }
    LaunchedEffect(client) { synchronizeLibrary() }

    val acquisitionApi = remember(client, settings.companionUrl) { AcquisitionClient(settings.companionUrl, client.credentials) }
    val releaseApi = remember(client, settings.companionUrl) { ReleaseClient(settings.companionUrl, client.credentials) }
    DisposableEffect(releaseApi) { onDispose { releaseApi.close() } }
    var upcoming by remember(releaseApi) { mutableStateOf(emptyList<UpcomingAlbum>()) }
    var artistUpcoming by remember(releaseApi) { mutableStateOf(emptyList<UpcomingAlbum>()) }
    var releaseRevision by remember { mutableStateOf(0) }
    var releaseError by remember(releaseApi) { mutableStateOf<String?>(null) }
    LaunchedEffect(releaseApi, releaseRevision) {
        if (settings.companionUrl.isBlank()) return@LaunchedEffect
        while (isActive) {
            try {
                val updated = releaseApi.albums()
                val completed = updated.any { it.status == "rescanned" && upcoming.any { old -> old.id == it.id && old.status != "rescanned" } }
                upcoming = updated
                releaseError = null
                if (updated.isNotEmpty()) onEnableReleaseNotifications()
                if (completed && !syncing) synchronizeLibrary()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { releaseError = "Couldn’t refresh upcoming releases. Please try again shortly." }
            delay(30_000)
        }
    }
    LaunchedEffect(releaseApi, detail, releaseRevision) {
        artistUpcoming = emptyList()
        val page = detail as? Detail.ArtistPage ?: return@LaunchedEffect
        if (settings.companionUrl.isBlank()) return@LaunchedEffect
        try { artistUpcoming = releaseApi.albums(page.artist.id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { /* Keep the existing featured album when the catalogue is unavailable. */ }
    }
    LaunchedEffect(releaseApi, releaseToOpen) {
        if (releaseToOpen == null || settings.companionUrl.isBlank()) return@LaunchedEffect
        try { openDetail(Detail.UpcomingAlbumPage(releaseApi.album(releaseToOpen))); onReleaseOpened() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { error = "Couldn’t open the release. Check your companion connection."; onReleaseOpened() }
    }
    DisposableEffect(acquisitionApi) { onDispose { acquisitionApi.close() } }
    val acquisitionJobs = remember(acquisitionApi) { mutableStateMapOf<String, AcquisitionJob>() }
    var historyError by remember(acquisitionApi) { mutableStateOf<String?>(null) }
    var storage by remember(acquisitionApi) { mutableStateOf<CompanionStorage?>(null) }
    var storageError by remember(acquisitionApi) { mutableStateOf<String?>(null) }
    LaunchedEffect(acquisitionApi, detail, downloadsOpen) {
        if (settings.companionUrl.isBlank()) return@LaunchedEffect
        if (detail != Detail.SettingsPage && !downloadsOpen) return@LaunchedEffect
        while (isActive) {
            try {
                val refreshed = acquisitionApi.jobs()
                val completed = refreshed.any { it.status == "rescanned" && acquisitionJobs[it.id]?.pending == true }
                refreshed.forEach { acquisitionJobs[it.id] = it }
                historyError = null
                if (completed && !syncing) synchronizeLibrary()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { historyError = "Couldn’t refresh downloads. Please try again." }
            if (detail == Detail.SettingsPage && !downloadsOpen) {
                try { storage = acquisitionApi.storage(); storageError = null }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { storageError = "Storage information is unavailable." }
            }
            delay(10_000)
        }
    }
    LaunchedEffect(acquisitionApi) {
        if (settings.companionUrl.isBlank()) return@LaunchedEffect
        var restored = false
        while (isActive && !restored) {
            try {
                acquisitionApi.jobs().forEach { if (it.id !in acquisitionJobs) acquisitionJobs[it.id] = it }
                restored = true
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { delay(3_000) }
        }
        while (isActive) {
            val pending = snapshotFlow { acquisitionJobs.values.filter { it.pending }.map { it.id } }
                .first { it.isNotEmpty() }
            delay(3_000)
            var completed = false
            for (id in pending) {
                try {
                    val updated = acquisitionApi.get(id)
                    completed = completed || (updated.status == "rescanned" && acquisitionJobs[id]?.pending == true)
                    acquisitionJobs[id] = updated
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { /* Retry active jobs without losing their state. */ }
            }
            if (completed) {
                snapshotFlow { syncing }.first { !it }
                synchronizeLibrary()
            }
        }
    }
    LaunchedEffect(client, detail, songsRevision) {
        val requestedDetail = detail
        val cached = requestedDetail?.let(loadedPages::get)
        if (cached != null) {
            songs = cached.songs
            sectionAlbums = cached.sectionAlbums
            artistAlbums = cached.artistAlbums
            artistSongs = cached.artistSongs
            artistInfo = cached.artistInfo
            artistDetails = cached.artistDetails
            sectionLoading = false
            loadedDetail = requestedDetail
        } else {
            loadedDetail = null
            songs = emptyList()
            sectionAlbums = emptyList()
            sectionLoading = false
            artistAlbums = emptyList()
            artistSongs = emptyList()
            artistInfo = ArtistInfo()
            artistDetails = null
        }
        var loaded = false
        try {
            when (val page = requestedDetail) {
                is Detail.HomeSectionPage -> {
                    sectionLoading = cached == null
                    try {
                        sectionAlbums = when (page.section) {
                            HomeSection.NewLibrary -> client.newestAlbums(size = 100)
                            HomeSection.Recent -> client.recentlyPlayedAlbums(size = 100)
                        }
                        if (page.section == HomeSection.Recent) recentAlbums = sectionAlbums.take(12)
                        error = null
                    } finally { sectionLoading = false }
                }
                is Detail.ArtistPage -> {
                    val own = runCatching { client.artistAlbums(page.artist.id) }
                        .getOrDefault(emptyList())
                    artistAlbums = (own + cached?.artistAlbums.orEmpty()).distinctBy { it.id }
                        .sortedByDescending { it.releaseOrder }
                    artistDetails = runCatching { client.artistDetails(page.artist.id) }.getOrNull()
                    artistInfo = runCatching { client.artistInfo(page.artist.id) }.getOrDefault(ArtistInfo())
                    artistSongs = client.artistSongsWithFeatures(page.artist).ifEmpty {
                            artistAlbums.take(12).flatMap { album ->
                                runCatching { client.albumSongs(album.id) }.getOrDefault(emptyList())
                            }.distinctBy { it.id }.sortedByDescending { it.playCount }
                        }
                }
                is Detail.ArtistSectionPage -> {
                    val own = runCatching { client.artistAlbums(page.artist.id) }
                        .getOrDefault(emptyList())
                    artistAlbums = (own + cached?.artistAlbums.orEmpty()).distinctBy { it.id }
                        .sortedByDescending { it.releaseOrder }
                    artistSongs = client.artistSongsWithFeatures(page.artist).ifEmpty {
                            artistAlbums.take(12).flatMap { album ->
                                runCatching { client.albumSongs(album.id) }.getOrDefault(emptyList())
                            }.distinctBy { it.id }.sortedByDescending { it.playCount }
                        }
                }
                is Detail.AlbumPage -> songs = client.albumSongs(page.album.id)
                is Detail.PlaylistPage -> songs = client.playlistSongs(page.playlist.id)
                is Detail.GenrePage -> {
                    songs = client.allGenreSongs(page.genre.name)
                    val albumIds = songs.mapNotNullTo(mutableSetOf()) { it.albumId }
                    sectionAlbums = albums.filter { it.id in albumIds }
                }
                else -> Unit
            }
            loaded = true
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (t: Throwable) { error = t.message ?: "Could not load songs" }
        finally {
            if (loaded) requestedDetail?.let { loadedPages[it] = LoadedPage(
                songs, sectionAlbums, artistAlbums, artistSongs, artistInfo, artistDetails) }
            if (detail == requestedDetail) loadedDetail = requestedDetail
        }
    }
    LaunchedEffect(client, detail, loadedDetail, artistSongs, songsRevision) {
        val page = detail ?: return@LaunchedEffect
        if (loadedDetail != page) return@LaunchedEffect
        if (page !is Detail.ArtistPage && page !is Detail.ArtistSectionPage) return@LaunchedEffect
        val extras = client.artistFeaturedReleases(artistSongs, artistAlbums)
            .filter(::isSingleOrEp)
        if (detail == page && extras.isNotEmpty()) {
            artistAlbums = (artistAlbums + extras).distinctBy { it.id }
                .sortedByDescending { it.releaseOrder }
            loadedPages[page] = loadedPages[page]?.copy(artistAlbums = artistAlbums)
                ?: return@LaunchedEffect
        }
    }
    LaunchedEffect(client, query, tab, songsRevision) {
        if (tab == Tab.Search && query.isNotBlank()) {
            delay(300)
            try { results = client.search(query); error = null }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (t: Throwable) { error = t.message ?: "Search failed" }
        } else results = SearchResults(emptyList(), emptyList(), emptyList())
    }
    LaunchedEffect(client, tab) {
        if (tab == Tab.Search) {
            val recent = async {
                try { client.recentlyPlayedAlbums() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { emptyList() }
            }
            val frequent = async {
                try { client.frequentlyPlayedAlbums() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { emptyList() }
            }
            val browseGenres = async {
                try { client.genres() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { emptyList() }
            }
            searchRecentAlbums = recent.await()
            searchFrequentAlbums = frequent.await()
            genres = browseGenres.await()
        }
    }

    val isHome = detail == null && tab == Tab.Home
    LaunchedEffect(client, isHome, playerExpanded) {
        while (isHome && !playerExpanded && isActive) {
            try { recentAlbums = client.recentlyPlayedAlbums() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { /* Optional shelf; keep the rest of Home usable. */ }
            delay(15_000)
        }
    }
    val lightBars = !darkMode && !(playerExpanded && nowPlaying != null)
    LaunchedEffect(lightBars) {
        onLightSystemBars(lightBars)
    }

    val chromeSky = rememberSky()
    LaunchedEffect(tab, detail, loading, songs, artists, playlists, recentAlbums, sectionAlbums, settings.favoritePlaylistKeys, results) {
        chromeSky.invalidate(durationMillis = 240)
    }
    val chromeSpace = chromeContentPadding(settings, nowPlaying != null)
    Box(Modifier.fillMaxSize()) {
    PredictiveBackContent(previousPreview, !playerExpanded,
        destinationReady = detail == null || loadedDetail == detail, onBack = {
        if (detail != null) goBack() else selectTab(Tab.Home)
    }, captureLayer = captureLayer) {
    if (isHome || detail is Detail.ArtistPage || detail is Detail.DiscoverArtistPage || detail is Detail.ArtistSectionPage || detail is Detail.AlbumPage || detail is Detail.PlaylistPage || detail is Detail.DiscoverAlbumPage || detail is Detail.UpcomingAlbumPage) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().sky(chromeSky)) {
                when (val page = detail) {
                    is Detail.UpcomingAlbumPage -> UpcomingAlbumScreen(upcoming.firstOrNull { it.id == page.album.id } ?: page.album,
                        releaseApi, client, darkMode, onArtworkColor, onBack = ::goBack,
                        onShare = onShareCollection, onChanged = { updated ->
                            detail = Detail.UpcomingAlbumPage(updated)
                            upcoming = (upcoming.filterNot { it.id == updated.id } + updated.copy(followed = upcoming.firstOrNull { it.id == updated.id }?.followed == true)).sortedBy { it.releaseAt }
                            artistUpcoming = artistUpcoming.map { if (it.id == updated.id) updated else it }
                        }, onEnableNotifications = onEnableReleaseNotifications,
                        onRequest = { libraryRequest = it }, onArtist = { openDetail(Detail.ArtistPage(it)) })
                    is Detail.DiscoverAlbumPage -> DiscoverAlbumScreen(page.album, client, darkMode,
                        onArtworkColor, onBack = ::goBack, onShare = onShareCollection,
                        onRequest = { libraryRequest = it })
                    is Detail.DiscoverArtistPage -> DiscoverArtistScreen(page.artist, client, darkMode,
                        onBack = ::goBack,
                        onAlbum = { openDetail(Detail.DiscoverAlbumPage(it)) },
                        onRequest = { libraryRequest = it })
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
                        upcoming = artistUpcoming.firstOrNull(),
                        onUpcoming = { openDetail(Detail.UpcomingAlbumPage(it)) },
                        onFavoriteChanged = { favorite ->
                            artistDetails = (artistDetails ?: page.artist).copy(starred = favorite)
                            artists = artists.map { if (it.id == page.artist.id) it.copy(starred = favorite) else it }
                            releaseRevision++
                            if (favorite) onEnableReleaseNotifications()
                        },
                    )
                    is Detail.ArtistSectionPage -> homeStateHolder.SaveableStateProvider(
                        "artist-section:${page.artist.id}:${page.section.name}") { ArtistSectionScreen(
                        page.artist, page.section, artistAlbums, artistSongs, client, darkMode,
                        bottomPadding = chromeSpace,
                        songColumns = settings.songViewColumns,
                        onSongColumns = { columns -> onSettingsChange { it.copy(songViewColumns = columns) } },
                        albumColumns = settings.albumViewColumns,
                        onAlbumColumns = { columns -> onSettingsChange { it.copy(albumViewColumns = columns) } },
                        onBack = ::goBack,
                        onAlbum = { openDetail(Detail.AlbumPage(it)) },
                        onSong = { onPlay(it, artistSongs) },
                        onAddNext = onAddNext,
                        onShareSong = onShareSong,
                        onArtist = { openDetail(Detail.ArtistPage(it)) },
                    ) }
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
                        favorite = isFavorite(page.playlist),
                        onFavorite = { setFavorite(page.playlist, it) },
                    )
                    else -> homeStateHolder.SaveableStateProvider("home") {
                        ReferenceHomeScreen(
                        albums, recentAlbums, orderedPlaylists, client, darkMode, loading, error ?: releaseError,
                        upcoming = upcoming.filter { it.followed && it.releaseAt > kotlin.time.Clock.System.now().toEpochMilliseconds() },
                        onUpcoming = { openDetail(Detail.UpcomingAlbumPage(it)) },
                        sections = settings.homeSections.filterNot { it in settings.hiddenHomeSections },
                        bottomPadding = chromeSpace,
                        onAlbum = { openDetail(Detail.AlbumPage(it)) },
                        onPlaylist = { openDetail(Detail.PlaylistPage(it)) },
                        onSection = { openDetail(Detail.HomeSectionPage(it)) },
                        onPlaylists = { selectTab(Tab.Playlists) },
                        isFavorite = { isFavorite(it) },
                        onFavorite = { playlist, favorite -> setFavorite(playlist, favorite) },
                        onSettings = { openDetail(Detail.SettingsPage) },
                        onCustomize = { openDetail(Detail.HomeSettingsPage) },
                        profileAvatar = profileAvatar,
                        refreshing = syncing, onRefresh = ::requestLibrarySync,
                        downloading = acquisitionJobs.values.any { it.pending },
                        )
                    }
                }
            }
        }
    } else {
    Box(Modifier.fillMaxSize().sky(chromeSky)) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .statusBarsPadding().padding(top = if (detail == null && tab == Tab.Search) 4.dp else 8.dp)) {
        AppToolbar(
            title = when (val page = detail) {
                is Detail.HomeSectionPage -> page.section.title
                is Detail.GenrePage -> page.genre.name
                is Detail.ArtistPage -> page.artist.name
                is Detail.ArtistSectionPage -> page.artist.name
                is Detail.AlbumPage -> page.album.name
                is Detail.DiscoverAlbumPage -> page.album.title
                is Detail.UpcomingAlbumPage -> page.album.title
                is Detail.DiscoverArtistPage -> page.artist.name
                is Detail.PlaylistPage -> page.playlist.name
                Detail.SettingsPage -> "Settings"
                Detail.HomeSettingsPage -> "Customize Home"
                null -> when (tab) { Tab.Home -> "Listen Now"; Tab.Artists -> "Artists";
                    Tab.Playlists -> "Playlists"; Tab.Search -> "Search" }
            },
            username = client.credentials.username,
            darkMode = darkMode,
            profileAvatar = profileAvatar,
            downloading = acquisitionJobs.values.any { it.pending },
            onSettings = if (detail == Detail.SettingsPage) null else ({ openDetail(Detail.SettingsPage) }),
            onBack = if (detail != null) ::goBack else null,
            searchField = if (detail == null && tab == Tab.Search) ({
                LibrarySearchField(query, { query = it }, "Search artists, albums and songs")
            }) else null,
            onScrollTop = { toolbarScope.launch {
                when (detail) {
                    is Detail.HomeSectionPage -> sectionGridState.animateScrollToItem(0)
                    is Detail.GenrePage -> genreGridState.animateScrollToItem(0)
                    Detail.SettingsPage -> settingsScrollState.animateScrollTo(0)
                    Detail.HomeSettingsPage -> homeSettingsListState.animateScrollToItem(0)
                    else -> when (tab) {
                        Tab.Artists -> artistGridState.animateScrollToItem(0)
                        Tab.Playlists -> playlistGridState.animateScrollToItem(0)
                        Tab.Search -> searchListState.animateScrollToItem(0)
                        Tab.Home -> Unit
                    }
                }
            } },
        )
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

        Box(Modifier.fillMaxWidth().weight(1f).padding(top = if (detail == null && tab == Tab.Search) 4.dp else 8.dp)) {
            if (loading || (detail is Detail.HomeSectionPage && sectionLoading) ||
                (detail is Detail.GenrePage && loadedDetail != detail))
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = accent)
            else when (val page = detail) {
                is Detail.HomeSectionPage -> homeStateHolder.SaveableStateProvider("home-section:${page.section.name}") {
                    HomeAlbumGrid(sectionAlbums, client, chromeSpace, sectionGridState,
                        columns = settings.albumViewColumns,
                        onColumns = { columns -> onSettingsChange { it.copy(albumViewColumns = columns) } }) {
                        openDetail(Detail.AlbumPage(it))
                    }
                }
                is Detail.GenrePage -> homeStateHolder.SaveableStateProvider("genre:${page.genre.name}") {
                    HomeAlbumGrid(sectionAlbums, client, chromeSpace, genreGridState,
                        columns = settings.albumViewColumns,
                        onColumns = { columns -> onSettingsChange { it.copy(albumViewColumns = columns) } },
                        onShuffle = { onShuffleSongs(songs) }) { openDetail(Detail.AlbumPage(it)) }
                }
                is Detail.AlbumPage -> SongList(songs, client, onPlay, onAddNext, onAddToQueue,
                    onShuffleSongs, chromeSpace)
                is Detail.PlaylistPage -> SongList(songs, client, onPlay, onAddNext, onAddToQueue,
                    onShuffleSongs, chromeSpace)
                is Detail.ArtistPage -> AlbumList(artistAlbums, client, chromeSpace) {
                    openDetail(Detail.AlbumPage(it))
                }
                is Detail.ArtistSectionPage -> Unit
                is Detail.DiscoverAlbumPage -> Unit
                is Detail.UpcomingAlbumPage -> Unit
                is Detail.DiscoverArtistPage -> Unit
                Detail.SettingsPage -> SettingsScreen(settings, onSettingsChange, onDisconnect, chromeSpace,
                    settingsScrollState, onCustomizeHome = { openDetail(Detail.HomeSettingsPage) },
                    profileAvatar = profileAvatar, username = client.credentials.username,
                    onPickProfileAvatar = onPickProfileAvatar,
                    onSync = ::requestLibrarySync, syncing = syncing, syncProgress = syncProgress,
                    syncError = syncError,
                    storage = storage, storageError = storageError,
                    onDownloads = { downloadsOpen = true }, activeDownloads = acquisitionJobs.values.count { it.pending })
                Detail.HomeSettingsPage -> HomeCustomizationScreen(settings, onSettingsChange,
                    chromeSpace, homeSettingsListState)
                null -> when (tab) {
                    Tab.Home -> Unit
                    Tab.Artists -> ArtistList(artists, client, chromeSpace, settings.artistViewColumns,
                        { columns -> onSettingsChange { it.copy(artistViewColumns = columns) } },
                        artistSort, { artistSort = it }, artistGridState) {
                        openDetail(Detail.ArtistPage(it))
                    }
                    Tab.Playlists -> homeStateHolder.SaveableStateProvider("playlists") { PlaylistList(orderedPlaylists, client, chromeSpace,
                        { isFavorite(it) }, { playlist, favorite -> setFavorite(playlist, favorite) },
                        playlistGridState, settings.playlistViewColumns,
                        { columns -> onSettingsChange { it.copy(playlistViewColumns = columns) } }) {
                        openDetail(Detail.PlaylistPage(it))
                    } }
                    Tab.Search -> SearchContent(query, results,
                        searchRecentAlbums.ifEmpty { recentAlbums }, searchFrequentAlbums,
                        albums, genres, client,
                        { openDetail(Detail.ArtistPage(it)) }, { openDetail(Detail.AlbumPage(it)) },
                        { onPlay(it, listOf(it)) }, onAddNext, onAddToQueue,
                        { openDetail(Detail.GenrePage(it)) },
                        chromeSpace, searchListState, songsRevision, discover, { discover = it },
                        { openDetail(Detail.DiscoverAlbumPage(it)) },
                        { libraryRequest = it }, onShareSong)
                }
            }
        }
    }
    }
    }
    libraryRequest?.let { request ->
        ModalBottomSheet(onDismissRequest = { libraryRequest = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.Transparent, contentColor = Color.White,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), dragHandle = null,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) }) {
            AcquisitionSheet(client.credentials, settings.companionUrl, request,
                onJobCreated = { acquisitionJobs[it.id] = it }, currentJob = { acquisitionJobs[it] })
        }
    }
    if (downloadsOpen) ModalBottomSheet(onDismissRequest = { downloadsOpen = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent, contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }) {
        AcquisitionStatusSheet(acquisitionJobs.values.sortedByDescending { it.createdAt }, historyError)
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
        onAlbum = { openDetail(Detail.AlbumPage(it)) },
        onArtist = { openDetail(Detail.ArtistPage(it)) },
        onShareSong = onShareSong,
    )
    }
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
                jam = jam,
                jamActions = jamActions,
                onJam = { jamOpen = true },
                openQueueForJam = jamQueueRequest,
                onQueueForJamOpened = { jamQueueRequest = 0 },
                onJamInvite = { jamGuestControls = false; jamOpen = true },
                onJamSettings = { jamGuestControls = true; jamOpen = true },
            )
        }
    }
    if (jamOpen) {
        ModalBottomSheet(onDismissRequest = { jamOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.statusBarsPadding(),
            containerColor = Color.Transparent, contentColor = Color.White,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            dragHandle = null,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            scrimColor = Color.Black.copy(alpha = 0.28f)) {
            JamScreen(client, jam, jamActions, nowPlaying, jamGuestControls)
        }
    }
    }
}

@Composable
private fun AlbumList(albums: List<Album>, client: SubsonicClient, bottomPadding: Dp,
                      onAlbum: (Album) -> Unit) =
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPadding)) {
        items(albums, key = { it.id }) { AlbumRow(it, client) { onAlbum(it) } }
    }

@Composable
private fun AlbumRow(album: Album, client: SubsonicClient, flat: Boolean = false,
                     onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = if (flat) 10.dp else 18.dp,
        vertical = if (flat) 2.dp else 4.dp)
        .then(if (flat) Modifier else Modifier.glass(RoundedCornerShape(20.dp), 0.2f))
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = if (flat) 4.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Artwork(client, album.coverArt, Modifier.size(62.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(album.name, color = ink, maxLines = 1, softWrap = false,
                overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
            Text(artistLabel(album.artist), color = muted, fontSize = 13.sp, maxLines = 1,
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
                       gridState: LazyGridState,
                       onArtist: (Artist) -> Unit) {
    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val visible = remember(artists, searchQuery, sort) {
        sortArtists(artists.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }, sort)
    }
    val scrollConnection = rememberPullSearchConnection({ gridState.canScrollBackward },
        { searchVisible = true }, { searchVisible = false })
    LazyVerticalGrid(columns = GridCells.Fixed(columns), state = gridState,
        modifier = Modifier.fillMaxSize().nestedScroll(scrollConnection),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = bottomPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                if (searchVisible) {
                    LibrarySearchField(searchQuery, { searchQuery = it }, "Find an artist")
                }
                BrowserControls(columns, onColumns, sort,
                    when (sort) { ArtistSort.Name -> "A–Z"; ArtistSort.NameReverse -> "Z–A"
                        ArtistSort.MostAlbums -> "Most"; ArtistSort.FewestAlbums -> "Fewest" },
                    listOf(ArtistSort.Name to "Name A–Z", ArtistSort.NameReverse to "Name Z–A",
                        ArtistSort.MostAlbums to "Most albums", ArtistSort.FewestAlbums to "Fewest albums"),
                    onSort, "Sort artists")
            }
        }
        items(visible.size, key = { visible[it].id }) { index ->
            val artist = visible[index]
            ArtistTile(artist, client, columns) { onArtist(artist) }
        }
    }
}

@Composable
private fun ArtistTile(artist: Artist, client: SubsonicClient, columns: Int,
                       flat: Boolean = false, onClick: () -> Unit) {
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
    if (columns == 1) Row(Modifier.fillMaxWidth().padding(vertical = if (flat) 2.dp else 4.dp)
        .then(if (flat) Modifier else Modifier.glass(RoundedCornerShape(20.dp), 0.2f))
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = if (flat) 4.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        picture(Modifier.size(60.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(artistLabel(artist.name), color = ink, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${artist.albumCount} albums", color = muted, fontSize = 13.sp)
        }
    } else Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        picture(Modifier.fillMaxWidth().aspectRatio(1f))
        Spacer(Modifier.height(8.dp))
        Text(artistLabel(artist.name), color = ink, fontSize = if (columns == 2) 15.sp else 13.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PlaylistList(playlists: List<Playlist>, client: SubsonicClient, bottomPadding: Dp,
                         isFavorite: (Playlist) -> Boolean, onFavorite: (Playlist, Boolean) -> Unit,
                         gridState: LazyGridState, columns: Int, onColumns: (Int) -> Unit,
                         onPlaylist: (Playlist) -> Unit) {
    var sort by rememberSaveable { mutableStateOf(PlaylistSort.Library) }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val visible = sortPlaylists(playlists.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }, sort, isFavorite)
    val searchConnection = rememberPullSearchConnection({ gridState.canScrollBackward },
        { searchVisible = true }, { searchVisible = false })
    LazyVerticalGrid(columns = GridCells.Fixed(columns), state = gridState,
        modifier = Modifier.fillMaxSize().nestedScroll(searchConnection),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, bottom = bottomPadding),
        horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(key = "controls", span = { GridItemSpan(maxLineSpan) }) {
            Column {
            if (searchVisible) LibrarySearchField(searchQuery, { searchQuery = it }, "Find a playlist")
            BrowserControls(columns, onColumns, sort,
                when (sort) { PlaylistSort.Library -> "Library"; PlaylistSort.Name -> "A–Z"
                    PlaylistSort.NameReverse -> "Z–A"; PlaylistSort.MostSongs -> "Most"
                    PlaylistSort.FewestSongs -> "Fewest" },
                listOf(PlaylistSort.Library to "Library order", PlaylistSort.Name to "Name A–Z",
                    PlaylistSort.NameReverse to "Name Z–A", PlaylistSort.MostSongs to "Most songs",
                    PlaylistSort.FewestSongs to "Fewest songs"), { sort = it }, "Sort playlists")
            }
        }
        if (visible.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
            Text(if (searchQuery.isBlank()) "No playlists yet" else "No matching playlists", color = muted)
        }
        items(visible.size, key = { visible[it].id }) { index ->
            val playlist = visible[index]
            if (columns == 1) Row(Modifier.fillMaxWidth().clickable { onPlaylist(playlist) },
                verticalAlignment = Alignment.CenterVertically) {
                Artwork(client, playlist.coverArt, Modifier.size(60.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(playlist.name, color = ink, style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${playlist.songCount} songs", color = muted, style = MaterialTheme.typography.bodySmall)
                }
                FavoritePlaylistButton(playlist.name, isFavorite(playlist),
                    onCheckedChange = { onFavorite(playlist, it) })
            } else Column(Modifier.clickable { onPlaylist(playlist) }) {
                Box {
                    Artwork(client, playlist.coverArt, Modifier.fillMaxWidth().aspectRatio(1f))
                    FavoritePlaylistButton(playlist.name, isFavorite(playlist),
                        Modifier.align(Alignment.TopEnd).padding(4.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.82f)),
                        onCheckedChange = { onFavorite(playlist, it) })
                }
                Spacer(Modifier.height(8.dp))
                Text(playlist.name, color = ink, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${playlist.songCount} songs", color = muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SongList(songs: List<Song>, client: SubsonicClient,
                     onPlay: (Song, List<Song>) -> Unit,
                     onAddNext: (Song) -> Unit, onAddToQueue: (Song) -> Unit,
                     onShuffle: (List<Song>) -> Unit, bottomPadding: Dp,
                     listState: LazyListState = rememberLazyListState()) =
    LazyColumn(state = listState, contentPadding = PaddingValues(bottom = bottomPadding)) {
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
                    onAddNext: () -> Unit, onAddToQueue: () -> Unit,
                    onAlbum: ((Album) -> Unit)? = null, onArtist: ((Artist) -> Unit)? = null,
                    onShare: ((Song) -> Unit)? = null, flat: Boolean = false) {
    var menuOpen by remember(song.id) { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = if (flat) 10.dp else 18.dp,
        vertical = if (flat) 2.dp else 4.dp)
        .then(if (flat) Modifier else Modifier.glass(RoundedCornerShape(20.dp), 0.2f))
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = if (flat) 4.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Artwork(client, song.coverArt ?: song.albumId, Modifier.size(60.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = ink, maxLines = 1, softWrap = false,
                overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isExplicit) {
                    ExplicitBadge(color = muted)
                    Spacer(Modifier.width(5.dp))
                }
                Text(artistLabel(song.artist), color = muted, fontSize = 13.sp, maxLines = 1,
                    softWrap = false, overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE))
            }
        }
        IconButton(onClick = { menuOpen = true }) {
            Icon(MaterialSymbols.RoundedFilled.More_vert, "Song actions", tint = muted)
        }
    }
    if (menuOpen) CollectionSongSheet(song, client, onDismiss = { menuOpen = false },
        onPlayNext = onAddNext, onAddToQueue = onAddToQueue, onAlbum = onAlbum,
        onArtist = onArtist, onShare = onShare)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchContent(
    query: String,
    results: SearchResults,
    recentlyPlayed: List<Album>,
    frequentlyPlayed: List<Album>,
    libraryAlbums: List<Album>,
    genres: List<Genre>,
    client: SubsonicClient,
    onArtist: (Artist) -> Unit,
    onAlbum: (Album) -> Unit,
    onSong: (Song) -> Unit,
    onAddNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onGenre: (Genre) -> Unit,
    bottomPadding: Dp,
    listState: LazyListState,
    libraryRevision: Int,
    discover: Boolean,
    onDiscover: (Boolean) -> Unit,
    onDiscoverAlbum: (DiscoverAlbum) -> Unit,
    onRequest: (LibraryRequest) -> Unit,
    onShareSong: (Song) -> Unit,
) {
    val catalogue = remember(client) { DiscoverClient() }
    DisposableEffect(catalogue) { onDispose { catalogue.close() } }
    var discoverResults by remember { mutableStateOf<DiscoverResults?>(null) }
    var discoverLoading by remember { mutableStateOf(false) }
    var discoverError by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableStateOf(0) }
    LaunchedEffect(client, query, discover, retry, libraryRevision) {
        discoverResults = null
        discoverError = null
        discoverLoading = discover && query.isNotBlank()
        if (!discoverLoading) return@LaunchedEffect
        try {
            delay(400)
            discoverResults = catalogue.search(query.trim(), client)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { discoverError = "Could not load the catalogue or verify your library. Please retry." }
        finally { discoverLoading = false }
    }
    LaunchedEffect(query, discover) { listState.scrollToItem(0) }
    val genreArtwork = remember(client) { mutableStateMapOf<String, String?>() }
    Column {
        SearchScopeToggle(discover, onDiscover)
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = bottomPadding)) {
            if (discover) discoverResults(query, discoverResults, discoverLoading, discoverError,
                onDiscoverAlbum, onRequest, onRetry = { retry++ })
            if (!discover && query.isBlank()) {
                if (genres.isNotEmpty()) item("search-genres") {
                    Text("Browse genres", color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(start = 22.dp, top = 20.dp, bottom = 16.dp))
                    val colors = listOf(Color(0xFF4B466F), Color(0xFF17635E),
                        Color(0xFF754734), Color(0xFF3B5580), Color(0xFF773F5D),
                        Color(0xFF50672C))
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val cardWidth = (maxWidth - 56.dp) / 2
                        LazyRow(contentPadding = PaddingValues(horizontal = 22.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(genres.chunked(2)) { column ->
                                Column(Modifier.width(cardWidth),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    column.forEach { genre ->
                                        SearchGenreCard(genre, client, genreArtwork,
                                            colors[genres.indexOf(genre) % colors.size]) {
                                            onGenre(genre)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (recentlyPlayed.isNotEmpty()) item("search-recent") {
                    SearchSuggestionShelf("Recently played", recentlyPlayed, client, onAlbum)
                }
                val recentIds = recentlyPlayed.mapTo(mutableSetOf()) { it.id }
                val onRepeat = frequentlyPlayed.filterNot { it.id in recentIds }
                if (onRepeat.isNotEmpty()) item("search-frequent") {
                    SearchSuggestionShelf("In your rotation", onRepeat, client, onAlbum)
                }
                if (recentlyPlayed.isEmpty() && onRepeat.isEmpty() && libraryAlbums.isNotEmpty())
                    item("search-explore") {
                        SearchSuggestionShelf("Explore your library", libraryAlbums, client, onAlbum)
                    }
            }
            if (!discover && query.isNotBlank()) {
                if (results.artists.isNotEmpty()) item { SectionTitle("Artists", 14.dp) }
                items(results.artists, key = { "artist:${it.id}" }) { artist ->
                    Box(Modifier.padding(horizontal = 10.dp)) {
                        ArtistTile(artist, client, 1, flat = true) { onArtist(artist) }
                    }
                }
                if (results.albums.isNotEmpty()) item { SectionTitle("Albums", 14.dp) }
                items(results.albums, key = { "album:${it.id}" }) { AlbumRow(it, client, flat = true) { onAlbum(it) } }
                if (results.songs.isNotEmpty()) item { SectionTitle("Songs", 14.dp) }
                items(results.songs, key = { "song:${it.id}" }) { song ->
                    SongRow(song, client, onClick = { onSong(song) },
                        onAddNext = { onAddNext(song) }, onAddToQueue = { onAddToQueue(song) },
                        onAlbum = onAlbum, onArtist = onArtist, onShare = onShareSong, flat = true)
                }
            }
        }
    }
}

@Composable
private fun SearchScopeToggle(discover: Boolean, onDiscover: (Boolean) -> Unit) {
    val ink = MaterialTheme.colorScheme.onBackground
    Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 10.dp)
        .height(44.dp).clip(CircleShape)
        .background(ink.copy(alpha = 0.12f))
        .border(1.dp, ink.copy(alpha = 0.20f), CircleShape)) {
        listOf(false to "Library", true to "Discover").forEach { (choice, label) ->
            Box(Modifier.weight(1f).fillMaxHeight().clip(CircleShape)
                .background(if (discover == choice) ink.copy(alpha = 0.19f) else Color.Transparent)
                .clickable(onClickLabel = "Search $label", onClick = { onDiscover(choice) }),
                contentAlignment = Alignment.Center) {
                Text(label, color = ink.copy(alpha = if (discover == choice) 1f else 0.65f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (discover == choice) FontWeight.SemiBold else FontWeight.Normal))
            }
        }
    }
}

@Composable
private fun SearchGenreCard(
    genre: Genre, client: SubsonicClient, artwork: MutableMap<String, String?>,
    color: Color, onClick: () -> Unit,
) {
    LaunchedEffect(client, genre.name) {
        if (!artwork.containsKey(genre.name)) artwork[genre.name] = try {
            client.genreSongs(genre.name, count = 1).firstOrNull()?.let { it.coverArt ?: it.albumId }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { null }
    }
    Box(Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(16.dp))
        .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.72f))))
        .clickable(onClick = onClick).semantics { contentDescription = "Browse ${genre.name}" }) {
        val artworkId = artwork[genre.name]
        if (artworkId != null) Artwork(client, artworkId, Modifier.size(82.dp).align(Alignment.BottomEnd)
            .offset(x = 13.dp, y = 13.dp).rotate(14f))
        else Text(genre.name.take(1).uppercase(), color = Color.White.copy(alpha = 0.15f),
            fontSize = 84.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 6.dp, y = 24.dp))
        Text(genre.name, color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth(0.68f).padding(14.dp))
        Text("${genre.songCount} songs", color = Color.White.copy(alpha = 0.82f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomStart).padding(14.dp))
    }
}

@Composable
private fun SearchSuggestionShelf(
    title: String, albums: List<Album>, client: SubsonicClient, onAlbum: (Album) -> Unit,
) {
    Text(title, color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(start = 22.dp, top = 24.dp, bottom = 16.dp))
    LazyRow(contentPadding = PaddingValues(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(albums.take(12), key = { it.id }) { album ->
            Column(Modifier.width(148.dp).clickable { onAlbum(album) }) {
                Artwork(client, album.coverArt, Modifier.size(148.dp))
                Spacer(Modifier.height(8.dp))
                Column(Modifier.heightIn(min = 66.dp * LocalDensity.current.fontScale.coerceAtLeast(1f))) {
                    Text(album.name, color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium, maxLines = 2,
                        overflow = TextOverflow.Ellipsis)
                    Text(artistLabel(album.artist), color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
internal fun SectionTitle(text: String, topPadding: Dp = 22.dp) = Text(text, color = MaterialTheme.colorScheme.onBackground,
    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
    modifier = Modifier.padding(start = 22.dp, top = topPadding, bottom = 10.dp))

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    onChange: ((AppSettings) -> AppSettings) -> Unit,
    onDisconnect: () -> Unit,
    bottomPadding: Dp,
    scrollState: ScrollState,
    onCustomizeHome: () -> Unit,
    profileAvatar: ImageBitmap?,
    username: String,
    onPickProfileAvatar: () -> Unit,
    onSync: () -> Unit, syncing: Boolean, syncProgress: Float, syncError: String?,
    onDownloads: () -> Unit, activeDownloads: Int,
    storage: CompanionStorage?, storageError: String?,
) {
    val gestures = settings.gestures
    var companionAddress by remember { mutableStateOf(settings.companionUrl) }
    var companionError by remember { mutableStateOf<String?>(null) }
    val changeSettings by androidx.compose.runtime.rememberUpdatedState(onChange)
    val currentAddress by androidx.compose.runtime.rememberUpdatedState(companionAddress)
    DisposableEffect(Unit) {
        onDispose {
            runCatching { if (currentAddress.isBlank()) "" else validatedCompanionUrl(currentAddress) }
                .onSuccess { address -> changeSettings { it.copy(companionUrl = address) } }
        }
    }
    LaunchedEffect(companionAddress) {
        if (companionAddress == settings.companionUrl) return@LaunchedEffect
        delay(700)
        runCatching { if (companionAddress.isBlank()) "" else validatedCompanionUrl(companionAddress) }
            .onSuccess { address -> changeSettings { it.copy(companionUrl = address) }; companionError = null }
            .onFailure { companionError = it.message }
    }
    Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onPickProfileAvatar).padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(62.dp),
                contentAlignment = Alignment.Center) {
                ProfileAvatar(username, profileAvatar, 54.dp)
                if (activeDownloads > 0) CircularProgressIndicator(Modifier.fillMaxSize(), color = ink, strokeWidth = 2.dp)
            }
            Column(Modifier.padding(start = 14.dp)) {
                Text(username, color = ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text("Change profile picture", color = muted, fontSize = 13.sp)
            }
        }
        Text("Library", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp))
        SettingsLinkRow(MaterialSymbols.RoundedFilled.Sync,
            if (syncing) "Synchronizing library" else "Synchronize library",
            if (syncing) "Refreshing music from the server" else "Refresh your library from the server",
            onSync, enabled = !syncing, spinning = syncing,
            progress = syncProgress, progressError = syncError != null)
        SettingsLinkRow(MaterialSymbols.RoundedFilled.Download, "Downloads",
            if (activeDownloads > 0) "$activeDownloads active · View download status" else "View download history and status",
            onDownloads)
        Text("Companion server", color = ink, modifier = Modifier.padding(top = 16.dp))
        Text("Your server for Jam and library downloads.", color = muted, fontSize = 13.sp)
        OutlinedTextField(companionAddress, { companionAddress = it; companionError = null },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp), singleLine = true,
            label = { Text("HTTPS address") }, isError = companionError != null)
        companionError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        Text("Storage", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp))
        if (settings.companionUrl.isBlank()) Text("Connect your companion server to view storage.", color = muted, fontSize = 13.sp)
        else if (storage == null) Text(storageError ?: "Checking storage…", color = muted, fontSize = 13.sp)
        storage?.let { available ->
            val disks = if (available.sharedDisk) listOf("Music library and downloads" to available.library)
                else listOf("Music library" to available.library, "Download staging" to available.downloads)
            disks.forEach { (label, space) ->
                Text(label, color = ink, modifier = Modifier.padding(top = 8.dp))
                Text(space?.let { "${formatStorageBytes(it.availableBytes)} available of ${formatStorageBytes(it.totalBytes)}" }
                    ?: "Unavailable", color = muted, fontSize = 13.sp)
                if (space != null && space.totalBytes > 0) LinearProgressIndicator(
                    progress = { (1f - space.availableBytes.toFloat() / space.totalBytes).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), color = ink)
            }
            storageError?.let { Text(it, color = muted, fontSize = 13.sp) }
        }
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
                        onClick = { onChange { it.copy(themePreference = mode) } })
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
            onChange { it.copy(miniPlayerSize = MiniPlayerSize.entries[index]) }
        }
        Spacer(Modifier.height(20.dp))
        Text("Navigation style", color = ink, fontSize = 16.sp)
        Text("Glaze’s glass pill or a clean, minimal bar.", color = muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        SettingsChoices(listOf("Glaze", "Minimal"), settings.navigationStyle.ordinal) { index ->
            onChange { it.copy(navigationStyle = NavigationStyle.entries[index]) }
        }
        Spacer(Modifier.height(20.dp))
        Text("Navigation size", color = ink, fontSize = 16.sp)
        Text("Adjust the height and icon size of the bottom bar.", color = muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        SettingsChoices(NavigationSize.entries.map { it.name }, settings.navigationSize.ordinal) { index ->
            onChange { it.copy(navigationSize = NavigationSize.entries[index]) }
        }
        if (settings.navigationStyle == NavigationStyle.Glaze) {
            SettingsToggle("Search in navigation", "Turn off for a separate search button",
                settings.searchInNavigation) {
                onChange { current -> current.copy(searchInNavigation = it) }
            }
        }
        SettingsToggle("Navigation labels", "Show text below the navigation icons",
            settings.navigationLabels) {
            onChange { current -> current.copy(navigationLabels = it) }
        }
        Spacer(Modifier.height(12.dp))
        Text("Glass intensity", color = ink, fontSize = 16.sp)
        Text("Adjust the translucency of player controls and navigation.", color = muted, fontSize = 13.sp)
        Slider(value = settings.glassIntensity.coerceIn(0f, 1f),
            onValueChange = { value -> onChange { it.copy(glassIntensity = value) } })
        Text("Home", color = accent, style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
        Text("Choose your sections and drag them into the order you want.", color = muted,
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        SettingsLinkRow(MaterialSymbols.RoundedFilled.Settings, "Customize Home",
            "Reorder and show or hide sections", onCustomizeHome)
        Text("Gestures", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 18.dp, bottom = 10.dp))
        SettingsToggle("Swipe mini player", "Skip to the previous or next song",
            gestures.miniPlayerSwipe) {
            onChange { current -> current.copy(gestures = current.gestures.copy(miniPlayerSwipe = it)) }
        }
        SettingsToggle("Swipe player down", "Close the full-screen player",
            gestures.playerSwipeDown) {
            onChange { current -> current.copy(gestures = current.gestures.copy(playerSwipeDown = it)) }
        }
        SettingsToggle("Long-press mini player", "Open quick song actions",
            gestures.miniPlayerLongPress) {
            onChange { current -> current.copy(gestures = current.gestures.copy(miniPlayerLongPress = it)) }
        }
        Spacer(Modifier.height(18.dp))
        Text("Swipe distance · ${gestures.sensitivityDp.roundToInt()} dp", color = ink, fontSize = 16.sp)
        Text("Increase to make gestures less sensitive.", color = muted, fontSize = 13.sp)
        Slider(value = gestures.sensitivityDp.coerceIn(40f, 160f), valueRange = 40f..160f,
            onValueChange = { value -> onChange { it.copy(gestures = it.gestures.copy(sensitivityDp = value)) } })
        Text("Playback", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 18.dp, bottom = 10.dp))
        SettingsToggle("Smart shuffle", "Explore underplayed songs and avoid recent repeats",
            settings.smartShuffle) {
            onChange { current -> current.copy(smartShuffle = it) }
        }
        SettingsLinkRow(MaterialSymbols.RoundedFilled.Logout, "Log out",
            "Leave this music server", onDisconnect, danger = true)
        Spacer(Modifier.height(bottomPadding))
    }
}

@Composable
private fun SettingsLinkRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit,
                            danger: Boolean = false, enabled: Boolean = true,
                            progress: Float? = null, progressError: Boolean = false,
                            spinning: Boolean = false) {
    val ink = if (danger) favoriteRed else MaterialTheme.colorScheme.onBackground
    val shape = RoundedCornerShape(18.dp)
    val iconRotation = if (spinning) {
        val transition = rememberInfiniteTransition(label = "Library sync")
        val rotation by transition.animateFloat(0f, 360f,
            infiniteRepeatable(tween(900, easing = LinearEasing)), label = "Sync icon rotation")
        rotation
    } else 0f
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(shape)
        .background(ink.copy(alpha = 0.07f))
        .border(1.dp, ink.copy(alpha = 0.12f), shape)
        .clickable(enabled = enabled, onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(ink.copy(alpha = 0.09f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = ink,
                    modifier = Modifier.size(22.dp).rotate(iconRotation))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = ink, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
                Text(artistLabel(subtitle), color = if (danger) ink.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
            Icon(MaterialSymbols.RoundedFilled.Arrow_forward, contentDescription = null,
                tint = if (danger) ink else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp))
        }
        if (progress != null) LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .height(5.dp).clip(CircleShape),
            color = if (progressError) MaterialTheme.colorScheme.error else ink,
            trackColor = ink.copy(alpha = 0.10f))
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
private fun Artwork(client: SubsonicClient, id: String?, modifier: Modifier = Modifier) {
    val url = remember(client, id) { id?.let { client.coverArtUrl(it) } }
    Box(modifier.clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF383838), Color(0xFF141414))))) {
        if (url != null) AsyncImage(model = url, contentDescription = "Album artwork",
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

@Composable
internal fun Modifier.glass(shape: RoundedCornerShape, intensity: Float): Modifier = this.clip(shape)
    .background(ink.copy(alpha = 0.04f + intensity.coerceIn(0f, 1f) * 0.16f))
    .border(1.dp, ink.copy(alpha = 0.08f + intensity.coerceIn(0f, 1f) * 0.18f), shape)
