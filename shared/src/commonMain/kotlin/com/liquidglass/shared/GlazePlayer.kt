package com.liquidglass.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Favorite as FavoriteOutline
import com.composables.icons.materialsymbols.roundedfilled.Album
import com.composables.icons.materialsymbols.roundedfilled.Arrow_back
import com.composables.icons.materialsymbols.roundedfilled.Delete
import com.composables.icons.materialsymbols.roundedfilled.Drag_handle
import com.composables.icons.materialsymbols.roundedfilled.Expand_more
import com.composables.icons.materialsymbols.roundedfilled.Info
import com.composables.icons.materialsymbols.roundedfilled.Lyrics
import com.composables.icons.materialsymbols.roundedfilled.More_horiz
import com.composables.icons.materialsymbols.roundedfilled.Pause
import com.composables.icons.materialsymbols.roundedfilled.Person
import com.composables.icons.materialsymbols.roundedfilled.Play_arrow
import com.composables.icons.materialsymbols.roundedfilled.Playlist_add
import com.composables.icons.materialsymbols.roundedfilled.Queue_music
import com.composables.icons.materialsymbols.roundedfilled.Repeat
import com.composables.icons.materialsymbols.roundedfilled.Repeat_one
import com.composables.icons.materialsymbols.roundedfilled.Search
import com.composables.icons.materialsymbols.roundedfilled.Share
import com.composables.icons.materialsymbols.roundedfilled.Shuffle
import com.composables.icons.materialsymbols.roundedfilled.Skip_next
import com.composables.icons.materialsymbols.roundedfilled.Skip_previous
import com.composables.icons.materialsymbols.roundedfilled.Speed
import com.composables.icons.materialsymbols.roundedfilled.Favorite
import com.composables.icons.materialsymbols.roundedfilled.Add
import com.composables.icons.materialsymbols.roundedfilled.Settings
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class PlayerView { Artwork, Lyrics }
private enum class SongOptionsView { Actions, Playlists, Speeds, Info }
private val playerWhite = Color.White
private val playerSecondary = Color.White.copy(alpha = 0.68f)

@Composable
internal fun ExplicitBadge(color: Color, modifier: Modifier = Modifier, titleSized: Boolean = false) {
    if (titleSized) {
        Box(modifier.size(22.dp).semantics { contentDescription = "Explicit" }
            .border(1.dp, color.copy(alpha = 0.75f), RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center) {
            Text("E", color = color, fontSize = 18.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        Text("E", color = color, fontSize = 10.sp, lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier.semantics { contentDescription = "Explicit" }
                .border(1.dp, color.copy(alpha = 0.75f), RoundedCornerShape(3.dp))
                .padding(horizontal = 3.dp))
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ReferencePlayerScreen(
    client: SubsonicClient,
    song: Song,
    isPlaying: Boolean,
    isBuffering: Boolean,
    playerColor: Color,
    playerBackdropColor: Color,
    positionMs: Long,
    durationMs: Long,
    queue: List<Song>,
    currentIndex: Int,
    settings: AppSettings,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit,
    onPlayQueueIndex: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onRestoreQueueItem: (Song, Int) -> Unit,
    onMoveInQueue: (Int, Int) -> Unit,
    onViewAlbum: (Song) -> Unit,
    onViewArtist: (Artist) -> Unit,
    onShareSong: (Song) -> Unit,
    onReadPosition: () -> Pair<Long, Long>,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onClearUpcoming: () -> Unit,
    playbackSpeed: Float,
    onChangePlaybackSpeed: (Float) -> Unit,
    jam: JamViewState,
    jamActions: JamActions,
    onJam: () -> Unit,
    openQueueForJam: Int,
    onJamInvite: () -> Unit,
    onJamSettings: () -> Unit,
) {
    val sky = rememberSky()
    val artUrl = remember(client, song.coverArt) { song.coverArt?.let { client.coverArtUrl(it, 1024) } }
    val accent = remember(playerColor) { artworkAccent(playerColor) }
    val glassTint = Color.White.copy(alpha = 0.05f + 0.12f * settings.glassIntensity.coerceIn(0f, 1f))
    var view by remember { mutableStateOf(PlayerView.Artwork) }
    var queueOpen by remember { mutableStateOf(false) }
    LaunchedEffect(openQueueForJam) { if (openQueueForJam > 0) queueOpen = true }
    var optionsOpen by remember { mutableStateOf(false) }
    var optionsView by remember(song.id) { mutableStateOf(SongOptionsView.Actions) }
    var artistsOpen by remember { mutableStateOf(false) }
    var artistRefs by remember(song.id) { mutableStateOf<List<Artist>?>(null) }
    val scope = rememberCoroutineScope()
    val jamSongs = remember(client, jam.sessionId) { mutableStateMapOf<String, Song>() }
    LaunchedEffect(client, jam.sessionId, jam.queue.map { it.trackId }) {
        if (jam.sessionId.isNotEmpty()) jam.queue.map { it.trackId }.distinct()
            .filterNot(jamSongs::containsKey).forEach { id ->
                try { client.songById(id)?.let { jamSongs[id] = it } }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { /* Keep the queue visible when one song is unavailable. */ }
            }
    }
    val displayedQueue = if (jam.sessionId.isEmpty()) queue else listOf(song) + jam.queue.map { entry ->
        jamSongs[entry.trackId] ?: Song(entry.trackId, "Loading song…", "", "")
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val artistsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var livePosition by remember(song.id) { mutableLongStateOf(positionMs) }
    var liveDuration by remember(song.id) { mutableLongStateOf(durationMs) }
    var detailedSong by remember(song.id) { mutableStateOf<Song?>(null) }
    fun showSongArtists() {
        scope.launch {
            val choices = artistRefs ?: resolveTrackArtists(client, detailedSong ?: song)
                .also { artistRefs = it }
            when (choices.size) {
                1 -> onViewArtist(choices.single())
                in 2..Int.MAX_VALUE -> artistsOpen = true
            }
        }
    }
    var dragDown by remember(song.id) { mutableFloatStateOf(0f) }
    val dragOffset by animateFloatAsState(
        dragDown, spring(dampingRatio = Spring.DampingRatioNoBouncy), label = "Player drag",
    )
    val dismissThreshold = with(LocalDensity.current) { settings.gestures.sensitivityDp.dp.toPx() }

    LaunchedEffect(song.id, positionMs, durationMs) {
        livePosition = positionMs
        liveDuration = durationMs
    }
    LaunchedEffect(client, song.id) {
        if (!song.isExplicit || song.suffix == null || song.samplingRate == null || song.bitRate == null ||
            song.albumId == null || song.artistId == null) {
            try { detailedSong = client.songById(song.id) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { /* Keep playback controls available offline. */ }
        }
    }
    LaunchedEffect(client, song.id) {
        artistRefs = resolveTrackArtists(client, song)
    }
    LaunchedEffect(song.id, isPlaying) {
        while (true) {
            val (position, duration) = onReadPosition()
            livePosition = position
            liveDuration = duration
            delay(350)
        }
    }
    LaunchedEffect(artUrl, playerColor, playerBackdropColor, isPlaying) { sky.invalidate(350) }
    LaunchedEffect(artUrl, isPlaying) {
        while (artUrl != null && isPlaying) {
            sky.invalidate()
            delay(100)
        }
    }

    Box(Modifier.fillMaxSize().offset { IntOffset(0, dragOffset.roundToInt()) }) {
        Box(Modifier.fillMaxSize().sky(sky)) {
            FluidArtworkBackground(artUrl, playerBackdropColor, sky, isPlaying)
        }

        AnimatedContent(
            targetState = view,
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            transitionSpec = {
                if (targetState == PlayerView.Lyrics) {
                    slideInVertically(tween(320)) { it }
                        .togetherWith(fadeOut(tween(160)))
                } else {
                    fadeIn(tween(180))
                        .togetherWith(slideOutVertically(tween(300)) { it })
                }
            },
            label = "Player and lyrics",
        ) { target ->
            when (target) {
                PlayerView.Artwork -> Column(Modifier.fillMaxSize()
                    .then(if (settings.gestures.playerSwipeDown) Modifier.pointerInput(song.id, dismissThreshold) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, amount ->
                                dragDown = (dragDown + amount).coerceAtLeast(0f)
                                if (dragDown > 0f) change.consume()
                            },
                            onDragEnd = {
                                if (dragDown >= dismissThreshold) onDismiss()
                                dragDown = 0f
                            },
                            onDragCancel = { dragDown = 0f },
                        )
                    } else Modifier)) {
                    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                        val artArea = maxHeight * 0.57f
                        val artSize = minOf(maxWidth * 0.73f, artArea - 28.dp)
                        val coverScale by animateFloatAsState(
                            if (isPlaying) 1f else 0.84f,
                            animationSpec = tween(durationMillis = 190), label = "Pause artwork scale",
                        )
                        Column(Modifier.fillMaxSize()) {
                            Box(Modifier.fillMaxWidth().height(artArea), contentAlignment = Alignment.Center) {
                                CoverArt(client, song,
                                    Modifier.size(artSize).offset(y = (-12).dp).graphicsLayer {
                                        scaleX = coverScale; scaleY = coverScale
                                    }.dropShadow(RoundedCornerShape(12.dp), Shadow(
                                        radius = 28.dp, color = Color.Black.copy(alpha = 0.22f),
                                        offset = DpOffset(0.dp, 8.dp),
                                    )).dropShadow(RoundedCornerShape(12.dp), Shadow(
                                        radius = 6.dp, color = Color.Black.copy(alpha = 0.16f),
                                        offset = DpOffset(0.dp, 2.dp),
                                    ))
                                        .clip(RoundedCornerShape(12.dp)))
                            }
                            Column(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp)) {
                                SongHeading(client, detailedSong ?: song, sky, glassTint,
                                    onViewAlbum, ::showSongArtists) {
                                    optionsView = SongOptionsView.Actions; optionsOpen = true
                                }
                                Spacer(Modifier.height(12.dp))
                                PlayerProgress(detailedSong ?: song, livePosition, liveDuration, isPlaying, accent, onSeek)
                                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                    TransportControls(isPlaying, isBuffering, onToggle, onPrevious, onNext)
                                }
                            }
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlainPlayerIcon(MaterialSymbols.RoundedFilled.Shuffle, if (isShuffleEnabled) "Shuffle on" else "Shuffle off",
                            21.dp, if (isShuffleEnabled) accent else playerSecondary,
                            active = isShuffleEnabled, onClick = onToggleShuffle)
                        PlainPlayerIcon(MaterialSymbols.RoundedFilled.Lyrics, "Lyrics", 21.dp,
                            playerSecondary) { view = PlayerView.Lyrics }
                        Box {
                            PlainPlayerIcon(MaterialSymbols.RoundedFilled.Queue_music,
                                if (jam.sessionId.isNotEmpty()) "${jam.members.firstOrNull { it.id == jam.hostId }?.name ?: "Your"}’s Jam queue" else "Queue",
                                21.dp, playerSecondary) { queueOpen = true }
                            if (jam.sessionId.isNotEmpty()) {
                                Box(Modifier.align(Alignment.TopEnd)) {
                                    JamAvatar(jam.members.firstOrNull { it.id == jam.memberId }
                                        ?: JamMember(jam.memberId, jam.name.ifBlank { "You" }), 17)
                                }
                            }
                        }
                        PlainPlayerIcon(if (repeatMode == 1) MaterialSymbols.RoundedFilled.Repeat_one else MaterialSymbols.RoundedFilled.Repeat,
                            when (repeatMode) { 1 -> "Repeat one"; 2 -> "Repeat all"; else -> "Repeat off" },
                            21.dp, if (repeatMode == 0) playerSecondary else accent,
                            active = repeatMode != 0, onClick = onCycleRepeat)
                    }
                }
                PlayerView.Lyrics -> LyricsView(client, song, livePosition, onSeek) {
                    view = PlayerView.Artwork
                }
            }
        }

        PlatformBackHandler(enabled = view == PlayerView.Lyrics && !queueOpen && !optionsOpen && !artistsOpen) {
            view = PlayerView.Artwork
        }

        if (queueOpen) {
            ModalBottomSheet(
                onDismissRequest = { queueOpen = false },
                sheetState = sheetState,
                modifier = Modifier.fillMaxHeight().statusBarsPadding(),
                containerColor = Color.Transparent,
                contentColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.24f),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                dragHandle = null,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            ) {
                QueueContents(
                    client, displayedQueue, if (jam.sessionId.isEmpty()) currentIndex else 0,
                    isPlaying, artUrl, onClearUpcoming,
                    onPlay = { index ->
                        if (jam.sessionId.isEmpty()) {
                            onPlayQueueIndex(index)
                            queueOpen = false
                        } else if (index > 0 && (jam.isHost || jam.guestPlayback)) {
                            jam.queue.getOrNull(index - 1)?.let { entry ->
                                jamActions.move(entry.id, 0)
                                jamActions.next()
                                queueOpen = false
                            }
                        }
                    },
                    onRemove = { index -> if (jam.sessionId.isEmpty()) onRemoveFromQueue(index)
                        else jam.queue.getOrNull(index - 1)?.let { jamActions.remove(it.id) } },
                    onRestore = { restored, index -> if (jam.sessionId.isEmpty()) onRestoreQueueItem(restored, index)
                        else jamActions.add(restored.id) },
                    onMove = { from, to -> if (jam.sessionId.isEmpty()) onMoveInQueue(from, to)
                        else jam.queue.getOrNull(from - 1)?.let { jamActions.move(it.id, to - 1) } },
                    jam = jam.takeIf { it.sessionId.isNotEmpty() },
                    onClearJam = { if (jamActions.clear()) { onClearUpcoming(); true } else false },
                    onJamInvite = onJamInvite,
                    onJamSettings = onJamSettings,
                    onEndJam = { jamActions.leave(); queueOpen = false },
                )
            }
        }
        if (optionsOpen) {
            val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { optionsOpen = false },
                sheetState = optionsSheetState,
                sheetGesturesEnabled = false,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.28f),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                dragHandle = null,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            ) {
                SongOptionsSheet(client, detailedSong ?: song, artUrl, optionsView,
                    onViewChange = { optionsView = it },
                    onClose = { optionsOpen = false },
                    onShare = onShareSong,
                    onJam = { optionsOpen = false; onJam() },
                    jamActive = jam.sessionId.isNotEmpty(),
                    onViewAlbum = onViewAlbum, onGoToArtists = {
                        optionsOpen = false
                        showSongArtists()
                    },
                    playbackSpeed = playbackSpeed, onChangePlaybackSpeed = onChangePlaybackSpeed)
            }
        }
        if (artistsOpen) {
            ModalBottomSheet(
                onDismissRequest = { artistsOpen = false },
                sheetState = artistsSheetState,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.28f),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                dragHandle = null,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            ) {
                ArtistPickerSheet(client, detailedSong ?: song, artistRefs.orEmpty(), artUrl) { artist ->
                    artistsOpen = false
                    onViewArtist(artist)
                }
            }
        }
    }
}

private suspend fun resolveTrackArtists(client: SubsonicClient, song: Song): List<Artist> {
    val fallback = listOfNotNull(song.artistId?.let { Artist(it, song.artist) })
    return try { client.songArtists(song.id).ifEmpty { fallback } }
    catch (cancelled: CancellationException) { throw cancelled }
    catch (_: Exception) { fallback }
}

@Composable
private fun FluidArtworkBackground(
    artUrl: String?, backdropColor: Color, sky: Sky, isPlaying: Boolean,
) {
    var seconds by remember(artUrl) { mutableFloatStateOf(0f) }
    LaunchedEffect(artUrl, isPlaying) {
        if (artUrl != null && isPlaying) {
            // Restart the frame baseline on resume, retaining the accumulated phase.
            var previousFrame = withFrameNanos { it }
            while (true) {
                withFrameNanos { frame ->
                    if (frame - previousFrame >= 33_000_000L) {
                        seconds += ((frame - previousFrame) / 1_000_000_000f).coerceIn(0f, 0.1f)
                        previousFrame = frame
                    }
                }
            }
        }
    }
    Box(Modifier.fillMaxSize().clipToBounds().background(backdropColor)) {
        if (artUrl != null) {
            FluidArtworkSurface(
                artUrl = artUrl,
                seconds = { seconds },
                onArtworkReady = { sky.invalidate() },
                modifier = Modifier.fillMaxSize(),
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                Color.Black.copy(alpha = 0.16f),
                Color.Black.copy(alpha = 0.24f),
                Color.Black.copy(alpha = 0.32f),
            ))))
        }
    }
}

internal fun artworkAccent(color: Color): Color {
    // Use the typical darkened control area, not a hypothetical pure-white frame.
    // The latter pushes nearly every artwork hue into white.
    val backgroundLuminance = Color(0.40f, 0.40f, 0.40f).luminance()
    for (step in 0..100) {
        val candidate = lerp(color.copy(alpha = 1f), Color.White, step / 100f)
        if ((candidate.luminance() + 0.05f) / (backgroundLuminance + 0.05f) >= 3f) return candidate
    }
    return Color.White
}

@Composable
private fun SongHeading(
    client: SubsonicClient, song: Song, sky: Sky, glassTint: Color,
    onViewAlbum: (Song) -> Unit, onShowArtists: () -> Unit,
    onShowOptions: () -> Unit,
) {
    var starred by remember(client, song.id) { mutableStateOf(song.starred) }
    var favoritePending by remember(client, song.id) { mutableStateOf(true) }
    var favoriteError by remember(client, song.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(client, song.id) {
        try { client.songById(song.id)?.let { starred = it.starred } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { /* Keep the playback metadata's favorite state available offline. */ }
        finally { favoritePending = false }
    }
    Column {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(song.title, color = playerWhite, fontSize = 23.sp, lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false,
                    overflow = TextOverflow.Clip, modifier = Modifier.weight(1f, fill = false).basicMarquee(iterations = Int.MAX_VALUE))
                if (song.isExplicit) {
                    Spacer(Modifier.width(10.dp))
                    ExplicitBadge(playerSecondary, titleSized = true)
                }
            }
            Text(song.artist, color = playerSecondary, fontSize = 17.sp, lineHeight = 23.sp,
                fontWeight = FontWeight.Normal, maxLines = 1, softWrap = false,
                overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE)
                    .clickable(enabled = song.artist.isNotBlank(),
                        interactionSource = remember { MutableInteractionSource() }, indication = null,
                        role = Role.Button) { onShowArtists() })
            if (song.album.isNotBlank()) Text(song.album, color = playerSecondary.copy(alpha = 0.8f),
                fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal,
                maxLines = 1, softWrap = false,
                overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE)
                    .clickable(enabled = song.albumId != null,
                        interactionSource = remember { MutableInteractionSource() }, indication = null,
                        role = Role.Button) { onViewAlbum(song) })
        }
        GlassIconButton(
            if (starred) MaterialSymbols.RoundedFilled.Favorite else MaterialSymbols.Rounded.FavoriteOutline,
            if (starred) "Unfavorite song" else "Favorite song", sky, glassTint, 42.dp, 24.dp,
            iconTint = if (starred) favoriteRed else playerSecondary,
            enabled = !favoritePending,
        ) {
            if (favoritePending) return@GlassIconButton
            val next = !starred
            starred = next
            favoritePending = true
            favoriteError = null
            scope.launch {
                try { client.setSongStarred(song.id, next) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) {
                    starred = !next
                    favoriteError = "Could not update favorite. Try again."
                } finally { favoritePending = false }
            }
        }
        Spacer(Modifier.width(8.dp))
        GlassIconButton(MaterialSymbols.RoundedFilled.More_horiz, "More options", sky, glassTint,
            42.dp, 25.dp, onClick = onShowOptions)
    }
    favoriteError?.let { message ->
        Text(message, modifier = Modifier.padding(top = 6.dp), color = playerWhite,
            style = MaterialTheme.typography.bodySmall)
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerProgress(
    song: Song, positionMs: Long, durationMs: Long, isPlaying: Boolean,
    accent: Color, onSeek: (Long) -> Unit,
) {
    var scrub by remember(song.id) { mutableStateOf<Float?>(null) }
    val progress = scrub ?: if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val waveAmount by animateFloatAsState(if (isPlaying) 1f else 0f,
        animationSpec = tween(260), label = "Seek wave morph")
    var phase by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var previousFrame = withFrameNanos { it }
            while (true) {
                withFrameNanos { frame ->
                    phase = (phase + (frame - previousFrame) / 1_300_000_000f * (2 * PI).toFloat()) % (2 * PI).toFloat()
                    previousFrame = frame
                }
            }
        }
    }
    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = { scrub = it },
            onValueChangeFinished = {
                scrub?.let { onSeek((it * durationMs).toLong()) }
                scrub = null
            },
            enabled = durationMs > 0,
            modifier = Modifier.fillMaxWidth().height(38.dp),
            thumb = {
                Box(Modifier.size(width = 10.dp, height = 30.dp),
                    contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 5.dp, height = 27.dp)
                        .clip(CircleShape).background(accent))
                }
            },
            track = { slider ->
                Canvas(Modifier.fillMaxWidth().height(32.dp)) {
                    val y = size.height / 2f
                    val activeX = size.width * slider.value.coerceIn(0f, 1f)
                    val gap = 8.dp.toPx()
                    if (activeX + gap < size.width) {
                        drawLine(Color.White.copy(alpha = 0.28f),
                            Offset(activeX + gap, y), Offset(size.width, y),
                            strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
                    }
                    val activeEnd = activeX.coerceAtLeast(0f)
                    // The short segment meets the thumb, so a round cap stays attached rather than floating as a dot.
                    if (activeEnd in 0f..10.dp.toPx()) {
                        if (activeEnd > 0f) drawLine(accent, Offset(0f, y), Offset(activeEnd, y),
                            strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
                    } else {
                        val path = Path().apply { moveTo(0f, y) }
                        var x = 2.dp.toPx()
                        val wavelength = 28.dp.toPx()
                        val amplitude = 4.dp.toPx() * waveAmount
                        while (x < activeEnd) {
                            val taper = minOf(x / 10.dp.toPx(), (activeEnd - x) / 10.dp.toPx(), 1f)
                            path.lineTo(x, y + sin((x / wavelength) * (2.0 * PI) - phase).toFloat() * amplitude * taper)
                            x += 2.dp.toPx()
                        }
                        path.lineTo(activeEnd, y)
                        drawPath(path, accent, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatPlayerTime((progress * durationMs).toLong()), color = playerSecondary,
                fontSize = 13.sp, fontWeight = FontWeight.Normal)
            Text(formatPlayerTime(durationMs), color = playerSecondary,
                fontSize = 13.sp, fontWeight = FontWeight.Normal)
        }
        val quality = listOfNotNull(
            song.suffix?.uppercase(),
            song.samplingRate?.let { rate ->
                val whole = rate / 1000
                val decimal = (rate % 1000) / 100
                if (decimal == 0) "${whole}kHz" else "${whole}.${decimal}kHz"
            },
            song.bitRate?.let { "${it} kbps" },
        ).joinToString(" • ")
        if (quality.isNotBlank()) {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                Text(quality, color = playerSecondary, fontSize = 11.sp,
                    modifier = Modifier.clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.20f))
                        .padding(horizontal = 12.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun TransportControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onToggle: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(92.dp).padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransportSkipButton(previous = true, onClick = onPrevious)
        val playInteraction = remember { MutableInteractionSource() }
        val playPressed by playInteraction.collectIsPressedAsState()
        Box(Modifier.size(80.dp).graphicsLayer {
                val scale = if (playPressed) 0.94f else 1f
                scaleX = scale; scaleY = scale
            }.clip(CircleShape).clickable(
                interactionSource = playInteraction, indication = null, role = Role.Button,
                onClick = onToggle,
            ), contentAlignment = Alignment.Center) {
            if (isBuffering) CircularProgressIndicator(Modifier.size(48.dp)
                .semantics { contentDescription = "Loading track" },
                color = playerWhite, strokeWidth = 5.dp)
            else Icon(if (isPlaying) MaterialSymbols.RoundedFilled.Pause else MaterialSymbols.RoundedFilled.Play_arrow,
                contentDescription = if (isPlaying) "Pause" else "Play", tint = playerWhite,
                modifier = Modifier.size(64.dp))
        }
        TransportSkipButton(previous = false, onClick = onNext)
    }
}

@Composable
private fun TransportSkipButton(previous: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(Modifier.size(72.dp).graphicsLayer {
            val scale = if (pressed) 0.94f else 1f
            scaleX = scale; scaleY = scale
        }.clip(CircleShape).clickable(
            interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick,
        ), contentAlignment = Alignment.Center) {
        Icon(if (previous) MaterialSymbols.RoundedFilled.Skip_previous else MaterialSymbols.RoundedFilled.Skip_next,
            contentDescription = if (previous) "Previous track" else "Next track",
            tint = playerWhite, modifier = Modifier.size(50.dp))
    }
}

@Composable
private fun PlainPlayerIcon(
    icon: ImageVector, label: String, iconSize: androidx.compose.ui.unit.Dp,
    tint: Color = playerWhite, active: Boolean = false, onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(Modifier.size(48.dp).graphicsLayer {
            val scale = if (pressed) 0.92f else 1f
            scaleX = scale; scaleY = scale
        }.clip(CircleShape).clickable(
            interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick,
        ), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(iconSize))
        if (active) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
            .size(width = 10.dp, height = 3.dp).clip(CircleShape).background(tint))
    }
}

@Composable
private fun LyricsView(
    client: SubsonicClient, song: Song, positionMs: Long, onSeek: (Long) -> Unit, onClose: () -> Unit,
) {
    var lyrics by remember(song.id) { mutableStateOf<SongLyrics?>(null) }
    var loading by remember(song.id) { mutableStateOf(true) }
    var error by remember(song.id) { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(client, song.id) {
        loading = true
        error = false
        try { lyrics = client.lyricsBySongId(song.id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { error = true }
        finally { loading = false }
    }
    val lines = lyrics?.lines.orEmpty().filter { it.text.isNotBlank() }
    val synced = lyrics?.synced == true
    val active = activeLyricIndex(lines, synced, positionMs)
    val prelude = synced && active == -1 && lines.firstOrNull()?.startMs?.let { it > positionMs } == true
    val userDragging by listState.interactionSource.collectIsDraggedAsState()
    val dismissThreshold = with(LocalDensity.current) { 90.dp.toPx() }
    var dragDown by remember(song.id) { mutableFloatStateOf(0f) }
    val dragOffset by animateFloatAsState(dragDown, label = "Lyrics drag")
    var closing by remember(song.id) { mutableStateOf(false) }
    fun finishDrag() {
        if (dragDown >= dismissThreshold) {
            if (!closing) {
                closing = true
                onClose()
            }
        } else dragDown = 0f
    }
    val listDismiss = remember(listState, dismissThreshold) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || dragDown <= 0f || available.y >= 0f) return Offset.Zero
                val consumed = maxOf(available.y, -dragDown)
                dragDown += consumed
                return Offset(0f, consumed)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || available.y <= 0f || listState.canScrollBackward) return Offset.Zero
                dragDown += available.y
                return Offset(0f, available.y)
            }

            override suspend fun onPostFling(consumed: androidx.compose.ui.unit.Velocity,
                available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                finishDrag()
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }
    LaunchedEffect(song.id, active, lines.size) {
        if (active < 0 || userDragging) return@LaunchedEffect
        withFrameNanos { }
        val target = active + if (prelude) 1 else 0
        val viewport = listState.layoutInfo
        val midpoint = (viewport.viewportStartOffset + viewport.viewportEndOffset) / 2
        if (viewport.visibleItemsInfo.none { it.index == target }) {
            listState.scrollToItem(target, scrollOffset = -midpoint)
            withFrameNanos { }
        }
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == target }
        if (item != null) {
            val delta = item.offset + item.size / 2 - midpoint
            if (delta > 0 || (delta < 0 && listState.canScrollBackward)) {
                listState.animateScrollBy(delta.toFloat())
            }
        }
    }
    Column(Modifier.fillMaxSize().offset { IntOffset(0, dragOffset.roundToInt()) }) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 18.dp)
            .pointerInput(song.id, dismissThreshold) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, amount ->
                        dragDown = (dragDown + amount).coerceAtLeast(0f)
                        if (dragDown > 0f) change.consume()
                    },
                    onDragEnd = { finishDrag() },
                    onDragCancel = { dragDown = 0f },
                )
            },
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f))
                .clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                Icon(MaterialSymbols.RoundedFilled.Expand_more, contentDescription = "Back to player", tint = playerWhite)
            }
            Spacer(Modifier.weight(1f))
            Text("Lyrics", color = playerSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = playerWhite)
            }
            lines.isEmpty() -> Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
                Text(if (error) "Couldn’t load lyrics" else "Lyrics aren’t available for this song",
                    color = playerSecondary, fontSize = 18.sp)
            }
            else -> BoxWithConstraints(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().nestedScroll(listDismiss),
                    contentPadding = PaddingValues(top = 8.dp, bottom = maxHeight * 0.5f),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    if (prelude) item { WaitingDots(Modifier.padding(start = 32.dp, bottom = 14.dp)) }
                    items(lines.size) { index ->
                        val line = lines[index]
                        val highlighted = synced && index == active
                        val alpha = when {
                            !synced -> 0.9f
                            highlighted -> 1f
                            index > active -> 0.28f
                            else -> 0.38f
                        }
                        val animatedAlpha by animateFloatAsState(alpha,
                            animationSpec = spring(stiffness = Spring.StiffnessLow), label = "Lyric focus")
                        val blurRadius by animateDpAsState(
                            targetValue = when {
                                !synced || userDragging || highlighted -> 0.dp
                                active < 0 || abs(index - active) == 1 -> 1.dp
                                else -> 2.dp
                            },
                            animationSpec = tween(durationMillis = 260),
                            label = "Lyric depth",
                        )
                        Text(
                            line.text,
                            color = playerWhite.copy(alpha = animatedAlpha),
                            fontSize = 31.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                                .then(if (line.startMs != null) Modifier.clickable { onSeek(line.startMs) } else Modifier)
                                .blur(blurRadius, BlurredEdgeTreatment.Unbounded),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaitingDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "Waiting for lyrics")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(durationMillis = 580, delayMillis = index * 160), RepeatMode.Reverse,
                ), label = "Lyric dot $index",
            )
            Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White.copy(alpha = alpha)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueContents(
    client: SubsonicClient, queue: List<Song>, currentIndex: Int,
    isPlaying: Boolean, artUrl: String?, onClearUpcoming: () -> Unit,
    onPlay: (Int) -> Unit, onRemove: (Int) -> Unit,
    onRestore: (Song, Int) -> Unit, onMove: (Int, Int) -> Unit,
    jam: JamViewState?, onClearJam: () -> Boolean,
    onJamInvite: () -> Unit, onJamSettings: () -> Unit,
    onEndJam: () -> Unit,
) {
    var confirmEnd by remember { mutableStateOf(false) }
    val sky = rememberSky()
    LaunchedEffect(artUrl) { sky.invalidate(350) }
    if (confirmEnd && jam != null) ModalBottomSheet(
        onDismissRequest = { confirmEnd = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent, contentColor = playerWhite,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = null, contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        scrimColor = Color.Black.copy(alpha = 0.42f),
    ) {
        PlayerSheetSurface(artUrl, Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
                SheetHandle()
                Text(if (jam.isHost) "End this Jam?" else "Leave this Jam?",
                    color = playerWhite, fontSize = 23.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp))
                Text(if (jam.isHost) "Everyone will leave the shared queue. Your music keeps playing."
                    else "You’ll leave the shared queue. The others can keep listening.",
                    color = playerSecondary, fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 26.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QueueGlassPill("Keep listening", sky, Modifier.weight(1f)) { confirmEnd = false }
                    QueueGlassPill(if (jam.isHost) "End Jam" else "Leave Jam", sky,
                        Modifier.weight(1f), danger = true) {
                        confirmEnd = false
                        onEndJam()
                    }
                }
            }
        }
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentIndex.coerceIn(0, (queue.size - 1).coerceAtLeast(0)))
    var query by remember { mutableStateOf("") }
    val matchingRows = queue.withIndex().filter { (_, song) ->
        query.isBlank() || listOf(song.title, song.artist, song.album).any {
            it.contains(query.trim(), ignoreCase = true)
        }
    }
    var previewOrder by remember(queue.map { it.id }, query) { mutableStateOf<List<Int>?>(null) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var dragPointerY by remember { mutableFloatStateOf(0f) }
    val visibleRows = (previewOrder ?: matchingRows.map { it.index })
        .map { index -> IndexedValue(index, queue[index]) }
    LaunchedEffect(query) {
        listState.scrollToItem(if (query.isBlank()) currentIndex.coerceIn(0, (queue.size - 1).coerceAtLeast(0)) else 0)
    }
    val rowHeightPx = with(LocalDensity.current) { 84.dp.toPx() }
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
        while (dragY < -rowHeightPx / 2f && position > currentIndex + 1) {
            order = queuePreviewMoved(order, position, position - 1)
            position--
            dragY += rowHeightPx
        }
        previewOrder = order
    }
    LaunchedEffect(draggingIndex) {
        while (draggingIndex != null) {
            val layout = listState.layoutInfo
            val step = when {
                dragPointerY < layout.viewportStartOffset + edgePx -> -scrollStepPx
                dragPointerY > layout.viewportEndOffset - edgePx -> scrollStepPx
                else -> 0f
            }
            if (step != 0f) {
                val scrolled = listState.scrollBy(step)
                if (scrolled != 0f) {
                    dragY += scrolled
                    advanceDragPreview()
                }
            }
            delay(16)
        }
    }
    val remaining = queue.drop(currentIndex.coerceAtLeast(0))
    val duration = remaining.sumOf { it.durationSeconds.toLong() }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
    PlayerSheetSurface(artUrl, Modifier.matchParentSize().sky(sky)) {}
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        SheetHandle()
        if (jam != null) {
            val hostName = jam.members.firstOrNull { it.id == jam.hostId }?.name
                ?: jam.name.ifBlank { "Your" }
            Text("${hostName}’s Jam", color = playerWhite, fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 14.dp))
            Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                val self = jam.members.firstOrNull { it.id == jam.memberId }
                    ?: JamMember(jam.memberId, jam.name.ifBlank { "You" })
                val members = listOf(self) + jam.members.filterNot { it.id == jam.memberId }
                val shown = members.take(3)
                val hidden = (members.size - shown.size).coerceAtLeast(0)
                Box(Modifier.width((70 + (shown.size - 1) * 25 + if (hidden > 0) 25 else 0).dp)
                    .height(42.dp)) {
                    shown.forEachIndexed { index, member ->
                        Box(Modifier.offset(x = (30 + index * 25).dp)
                            .zIndex((shown.size - index).toFloat())
                            .border(2.dp, Color.Black.copy(alpha = 0.8f), CircleShape)) {
                            JamAvatar(member, 38)
                        }
                    }
                    if (hidden > 0) Box(Modifier.offset(x = (30 + shown.size * 25).dp)
                        .size(38.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.17f))
                        .border(1.dp, Color.White.copy(alpha = 0.26f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Text("+$hidden", color = playerWhite, fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Box(Modifier.zIndex(10f)) {
                        GlassIconButton(MaterialSymbols.RoundedFilled.Add, "Invite friends", sky,
                            Color.White.copy(alpha = 0.15f), 42.dp, 24.dp, onClick = onJamInvite)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (jam.isHost) GlassIconButton(MaterialSymbols.RoundedFilled.Settings,
                    "Guest controls", sky, Color.White.copy(alpha = 0.15f), 42.dp, 21.dp,
                    onClick = onJamSettings)
                Spacer(Modifier.width(10.dp))
                QueueGlassPill(if (jam.isHost) "End" else "Leave", sky,
                    danger = true) { confirmEnd = true }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${remaining.size} ${if (remaining.size == 1) "song" else "songs"} • ${formatQueueDuration(duration)}", color = playerWhite,
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            if (jam == null) Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onClearUpcoming).padding(horizontal = 14.dp, vertical = 9.dp)) {
                Text("Clear queue", color = playerSecondary, fontSize = 14.sp)
            } else if (jam.isHost) QueueGlassPill("Clear queue", sky) { onClearJam() }
        }
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = playerWhite),
            cursorBrush = SolidColor(playerWhite),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.12f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(MaterialSymbols.RoundedFilled.Search, contentDescription = null,
                        tint = playerSecondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) Text("Search queue", color = playerSecondary,
                            fontSize = 14.sp)
                        innerTextField()
                    }
                }
            },
        )
        Text("Swipe right to play next  ·  Swipe left to remove", color = playerSecondary,
            fontSize = 11.sp, modifier = Modifier.padding(start = 24.dp, top = 9.dp, bottom = 10.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                .pointerInput(queue, query, currentIndex) {
                    val handleWidth = 64.dp.toPx()
                    detectDragGesturesAfterLongPress(
                        onDragStart = { start ->
                            val item = listState.layoutInfo.visibleItemsInfo.firstOrNull {
                                start.y >= it.offset && start.y < it.offset + it.size
                            }
                            val index = item?.key?.toString()?.substringAfterLast(':')?.toIntOrNull()
                            if (query.isBlank() && start.x >= size.width - handleWidth &&
                                index != null && index > currentIndex) {
                                draggingIndex = index
                                dragY = 0f
                                dragPointerY = start.y
                                previewOrder = queue.indices.toList()
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
                            val index = draggingIndex
                            val target = index?.let { previewOrder?.indexOf(it) }
                            draggingIndex = null
                            dragY = 0f
                            if (index != null && target != null && target >= 0 && target != index)
                                onMove(index, target)
                            else previewOrder = null
                        },
                        onDragCancel = {
                            draggingIndex = null
                            dragY = 0f
                            previewOrder = null
                        },
                    )
                },
            state = listState,
            contentPadding = PaddingValues(bottom = 42.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (visibleRows.isEmpty()) item {
                Text(if (query.isBlank()) "Nothing is queued" else "No matching songs",
                    color = playerSecondary, modifier = Modifier.padding(22.dp))
            }
            items(visibleRows, key = { "${it.value.id}:${it.index}" }) { (index, rowSong) ->
                val dragging = draggingIndex == index
                val dismiss = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.82f })
                LaunchedEffect(dismiss.currentValue) {
                    when (dismiss.currentValue) {
                        SwipeToDismissBoxValue.StartToEnd -> if (index != currentIndex)
                            onMove(index, playNextQueueIndex(index, currentIndex))
                        SwipeToDismissBoxValue.EndToStart -> if (index != currentIndex) {
                            onRemove(index)
                            if (jam == null) scope.launch {
                                if (snackbar.showSnackbar("${rowSong.title} was deleted", "Restore",
                                        duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed)
                                    onRestore(rowSong, index)
                            }
                        }
                        SwipeToDismissBoxValue.Settled -> return@LaunchedEffect
                    }
                    dismiss.reset()
                }
                SwipeToDismissBox(
                    state = dismiss,
                    modifier = Modifier.animateItem(
                        fadeInSpec = null, fadeOutSpec = null,
                        placementSpec = if (dragging) null else spring(stiffness = Spring.StiffnessMediumLow),
                    ).zIndex(if (dragging) 1f else 0f)
                        .offset { IntOffset(0, if (dragging) dragY.roundToInt() else 0) },
                    enableDismissFromStartToEnd = index != currentIndex && index != currentIndex + 1,
                    enableDismissFromEndToStart = index != currentIndex &&
                        (jam == null || jam.isHost || jam.queue.getOrNull(index - 1)?.addedBy == jam.memberId),
                    backgroundContent = {
                        when (dismiss.dismissDirection) {
                            SwipeToDismissBoxValue.StartToEnd -> Row(
                                Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF6937B8))
                                    .padding(start = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(MaterialSymbols.RoundedFilled.Queue_music, contentDescription = null,
                                    tint = playerWhite, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Play next", color = playerWhite, fontSize = 13.sp)
                            }
                            SwipeToDismissBoxValue.EndToStart -> Row(
                                Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFB51529))
                                    .padding(end = 18.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Remove", color = playerWhite, fontSize = 13.sp)
                                Spacer(Modifier.width(6.dp))
                                Icon(MaterialSymbols.RoundedFilled.Delete, contentDescription = null,
                                    tint = playerWhite, modifier = Modifier.size(20.dp))
                            }
                            SwipeToDismissBoxValue.Settled -> Unit
                        }
                    },
                ) {
                    QueueRow(client, rowSong, index, dragging,
                        dismiss.dismissDirection != SwipeToDismissBoxValue.Settled,
                        query.isBlank() && index > currentIndex,
                        currentIndex, isPlaying, onPlay,
                        jam?.queue?.getOrNull(index - 1)?.let { entry ->
                            jam.members.firstOrNull { it.id == entry.addedBy }
                        })
                }
            }
        }
    }
    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter)
        .navigationBarsPadding().padding(bottom = 12.dp))
    }
    }
}

@Composable
private fun QueueRow(
    client: SubsonicClient, song: Song, index: Int, dragging: Boolean, swiping: Boolean, canMove: Boolean,
    currentIndex: Int, isPlaying: Boolean, onPlay: (Int) -> Unit, addedBy: JamMember? = null,
) {
    val rowColor by animateColorAsState(when {
        dragging || swiping -> Color.Black
        index == currentIndex -> Color.White.copy(alpha = 0.20f)
        else -> Color.White.copy(alpha = 0.10f)
    }, animationSpec = tween(90), label = "Queue gesture surface")
    Row(
        Modifier.fillMaxWidth().height(81.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rowColor)
            .clickable { onPlay(index) }.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverArt(client, song, Modifier.size(51.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(end = 12.dp).clipToBounds()) {
            Text(song.title, color = if (index == currentIndex) Color.White else playerWhite,
                fontSize = 16.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isExplicit) {
                    ExplicitBadge(playerSecondary)
                    Spacer(Modifier.width(5.dp))
                }
                Text(song.artist, color = playerSecondary,
                    fontSize = 13.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f, fill = false).basicMarquee(iterations = Int.MAX_VALUE))
            }
            if (song.album.isNotBlank()) Text(song.album, color = playerSecondary.copy(alpha = 0.82f),
                fontSize = 11.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
        }
        addedBy?.let { JamAvatar(it, 25) }
        if (index == currentIndex) PlayingWaveform(isPlaying)
        if (canMove) {
            Icon(MaterialSymbols.RoundedFilled.Drag_handle, contentDescription = "Reorder ${song.title}",
                tint = playerSecondary, modifier = Modifier.size(32.dp))
        }
    }
}

internal fun queuePreviewMoved(order: List<Int>, from: Int, to: Int): List<Int> =
    order.toMutableList().apply { add(to, removeAt(from)) }

@Composable
private fun PlayingWaveform(isPlaying: Boolean) {
    val transition = if (isPlaying) rememberInfiniteTransition(label = "Playing queue waveform") else null
    Row(Modifier.padding(end = 10.dp).height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            val level = if (transition != null) {
                val animated by transition.animateFloat(
                    initialValue = 0.30f, targetValue = 0.95f,
                    animationSpec = infiniteRepeatable(
                        tween(400 + index * 120), RepeatMode.Reverse),
                    label = "Queue bar $index",
                )
                animated
            } else 0.30f
            Box(Modifier.width(3.dp).fillMaxHeight(level).clip(CircleShape)
                .background(playerWhite))
        }
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector, label: String, sky: Sky, tint: Color,
    buttonSize: androidx.compose.ui.unit.Dp, iconSize: androidx.compose.ui.unit.Dp,
    iconTint: Color = playerWhite,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        Modifier.size(buttonSize).graphicsLayer { alpha = if (enabled) 1f else 0.5f }.clip(CircleShape)
            .cloudy(sky = sky, radius = 24, tint = tint, shape = CircleShape)
            .border(1.dp, Brush.verticalGradient(listOf(
                Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.07f),
            )), CircleShape)
            .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() },
                indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun QueueGlassPill(
    text: String, sky: Sky, modifier: Modifier = Modifier,
    danger: Boolean = false, onClick: () -> Unit,
) {
    Box(modifier.height(42.dp).clip(CircleShape)
        .cloudy(sky = sky, radius = 24, tint = Color.White.copy(alpha = 0.15f), shape = CircleShape)
        .border(1.dp, Brush.verticalGradient(listOf(
            Color.White.copy(alpha = 0.34f), Color.White.copy(alpha = 0.07f))), CircleShape)
        .clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (danger) Color(0xFFFF8996) else playerWhite,
            fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun ArtistPickerSheet(
    client: SubsonicClient, song: Song, references: List<Artist>, artUrl: String?,
    onSelect: (Artist) -> Unit,
) {
    var artists by remember(song.id, references) { mutableStateOf(references) }
    LaunchedEffect(client, song.id, references) {
        val libraryArtists = try { client.artists().associateBy { it.id } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { emptyMap() }
        artists = references.map { reference ->
            libraryArtists[reference.id] ?: reference
        }
        artists.filter { it.coverArt == null && it.imageUrl == null }.forEach { missing ->
            val imageUrl = try { client.artistImageUrl(missing.id) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { null }
            if (imageUrl != null) artists = artists.map {
                if (it.id == missing.id) it.copy(imageUrl = imageUrl) else it
            }
        }
    }
    PlayerSheetSurface(artUrl, Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 20.dp)) {
            SheetHandle()
            Row(Modifier.fillMaxWidth().padding(start = 26.dp, end = 26.dp, top = 14.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(MaterialSymbols.RoundedFilled.Person, contentDescription = null,
                    tint = playerSecondary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(15.dp))
                Text("Go to artist", color = playerWhite, fontSize = 22.sp,
                    fontWeight = FontWeight.Medium)
            }
            if (artists.isEmpty()) Text("Artist unavailable", color = playerSecondary,
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 14.dp))
            artists.forEach { artist ->
                val imageUrl = remember(client, artist.coverArt, artist.imageUrl) {
                    artist.coverArt?.let { client.coverArtUrl(it, 256) } ?: artist.imageUrl
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(role = Role.Button) { onSelect(artist) }
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    if (imageUrl != null) {
                        AsyncImage(imageUrl, contentDescription = "${artist.name} portrait",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(48.dp).clip(CircleShape))
                    } else {
                        Box(Modifier.size(48.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center) {
                            Icon(MaterialSymbols.RoundedFilled.Person, contentDescription = null,
                                tint = playerSecondary, modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(artist.name, color = playerWhite, fontSize = 17.sp,
                        fontWeight = FontWeight.Normal, maxLines = 1,
                        modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE))
                }
            }
        }
    }
}

@Composable
private fun SongOptionsSheet(
    client: SubsonicClient, song: Song, artUrl: String?, view: SongOptionsView,
    onViewChange: (SongOptionsView) -> Unit,
    onClose: () -> Unit, onShare: (Song) -> Unit, onJam: () -> Unit,
    jamActive: Boolean,
    onViewAlbum: (Song) -> Unit, onGoToArtists: () -> Unit,
    playbackSpeed: Float, onChangePlaybackSpeed: (Float) -> Unit,
) {
    PlatformBackHandler(enabled = view != SongOptionsView.Actions) {
        onViewChange(SongOptionsView.Actions)
    }
    var playlists by remember(song.id) { mutableStateOf<List<Playlist>>(emptyList()) }
    var loading by remember(song.id) { mutableStateOf(false) }
    var message by remember(song.id) { mutableStateOf<String?>(null) }
    var pullDown by remember { mutableFloatStateOf(0f) }
    var draggingHandle by remember { mutableStateOf(false) }
    val sheetPull by animateFloatAsState(pullDown,
        animationSpec = if (draggingHandle) snap() else spring(stiffness = Spring.StiffnessMedium),
        label = "More sheet pull")
    val dismissThreshold = with(LocalDensity.current) { 48.dp.toPx() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(client, view) {
        if (view == SongOptionsView.Playlists) {
            loading = true
            message = null
            try { playlists = client.playlists() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { message = "Couldn’t load playlists" }
            finally { loading = false }
        }
    }
    PlayerSheetSurface(artUrl, Modifier.fillMaxWidth()
        .offset { IntOffset(0, sheetPull.roundToInt()) }) {
    Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp)) {
        Box(Modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = { draggingHandle = true },
                onVerticalDrag = { change, amount ->
                    pullDown = (pullDown + amount).coerceAtLeast(0f)
                    if (pullDown > 0f) change.consume()
                },
                onDragEnd = {
                    draggingHandle = false
                    if (pullDown >= dismissThreshold) onClose() else pullDown = 0f
                },
                onDragCancel = { draggingHandle = false; pullDown = 0f },
            )
        }) { SheetHandle() }
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically) {
            CoverArt(client, song, Modifier.size(62.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, color = playerWhite, fontSize = 19.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false,
                    overflow = TextOverflow.Clip, modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (song.isExplicit) {
                        ExplicitBadge(playerSecondary)
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(song.artist, color = playerSecondary, fontSize = 13.sp,
                        maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                        modifier = Modifier.weight(1f, fill = false).basicMarquee(iterations = Int.MAX_VALUE))
                }
                if (song.album.isNotBlank()) Text(song.album, color = playerSecondary.copy(alpha = 0.82f),
                    fontSize = 12.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                    modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE))
            }
        }
        if (view == SongOptionsView.Actions) {
            Text("SONG ACTIONS", color = playerSecondary, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 10.dp))
        } else {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 24.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.11f))
                    .clickable(role = Role.Button) { onViewChange(SongOptionsView.Actions) },
                    contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbols.RoundedFilled.Arrow_back, "Back to song actions",
                        tint = playerWhite, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.width(14.dp))
                Text(when (view) {
                    SongOptionsView.Playlists -> "Add to playlist"
                    SongOptionsView.Speeds -> "Playback speed"
                    SongOptionsView.Info -> "Track details"
                    SongOptionsView.Actions -> ""
                }, color = playerWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        when (view) {
            SongOptionsView.Actions -> {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.09f))) {
                SongOptionRow(MaterialSymbols.RoundedFilled.Share, "Share") { onClose(); onShare(song) }
                SongOptionRow(MaterialSymbols.RoundedFilled.Person,
                    if (jamActive) "Jam already in progress" else "Start a Jam", enabled = !jamActive) {
                    onJam()
                }
                SongOptionRow(MaterialSymbols.RoundedFilled.Playlist_add, "Add to playlist") {
                    onViewChange(SongOptionsView.Playlists)
                }
                if (song.albumId != null) SongOptionRow(MaterialSymbols.RoundedFilled.Album, "View album") {
                    onClose(); onViewAlbum(song)
                }
                if (song.artist.isNotBlank()) SongOptionRow(MaterialSymbols.RoundedFilled.Person, "View artists") {
                    onGoToArtists()
                }
                SongOptionRow(MaterialSymbols.RoundedFilled.Speed, "Playback speed · ${playbackSpeed}×") {
                    onViewChange(SongOptionsView.Speeds)
                }
                SongOptionRow(MaterialSymbols.RoundedFilled.Info, "View track info") {
                    onViewChange(SongOptionsView.Info)
                }
                }
            }
            SongOptionsView.Playlists -> {
                when {
                    loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally)
                        .padding(24.dp), color = playerWhite)
                    message != null -> Text(message!!, color = playerSecondary,
                        modifier = Modifier.padding(24.dp))
                    playlists.isEmpty() -> Text("No playlists available", color = playerSecondary,
                        modifier = Modifier.padding(24.dp))
                    else -> LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .heightIn(max = 420.dp).clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.09f))) {
                        items(playlists.size) { index ->
                            val playlist = playlists[index]
                            SongOptionRow(MaterialSymbols.RoundedFilled.Playlist_add, playlist.name) {
                                if (!loading) scope.launch {
                                    loading = true
                                    try { client.addSongToPlaylist(playlist.id, song.id); onClose() }
                                    catch (cancelled: CancellationException) { throw cancelled }
                                    catch (_: Exception) { message = "Couldn’t add to ${playlist.name}" }
                                    finally { loading = false }
                                }
                            }
                        }
                    }
                }
            }
            SongOptionsView.Speeds -> {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.09f))) {
                listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                    SongOptionRow(MaterialSymbols.RoundedFilled.Speed,
                        "${speed}×${if (speed == playbackSpeed) " · Current" else ""}") {
                        onChangePlaybackSpeed(speed)
                        onClose()
                    }
                }
                }
            }
            SongOptionsView.Info -> {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.09f))) {
                listOfNotNull(
                    "Duration" to formatPlayerTime(song.durationSeconds * 1000L),
                    song.suffix?.let { "Format" to it.uppercase() },
                    song.samplingRate?.let { "Sample rate" to "${it / 1000.0} kHz" },
                    song.bitRate?.let { "Bit rate" to "$it kbps" },
                    song.genre?.let { "Genre" to it },
                    song.track?.let { "Track" to it.toString() },
                ).forEachIndexed { index, (label, value) ->
                    if (index > 0) Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp)
                        .height(1.dp).background(Color.White.copy(alpha = 0.09f)))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = playerSecondary, fontSize = 14.sp)
                        Text(value, color = playerWhite, fontSize = 14.sp,
                            fontWeight = FontWeight.Medium)
                    }
                }
                }
            }
        }
    }
    }
}

@Composable
internal fun SongOptionRow(icon: ImageVector, label: String,
    iconTint: Color = playerSecondary, enabled: Boolean = true, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(enabled = enabled,
            interactionSource = remember { MutableInteractionSource() }, indication = null,
            role = Role.Button, onClick = onClick,
        )
        .padding(horizontal = 26.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint.copy(alpha = if (enabled) 1f else 0.35f), modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(18.dp))
        Text(label, color = playerWhite.copy(alpha = if (enabled) 1f else 0.35f), fontSize = 16.sp, fontWeight = FontWeight.Normal,
            maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
            modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE))
    }
}

@Composable
internal fun SheetHandle() {
    Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center) {
        Box(Modifier.size(width = 38.dp, height = 4.dp).clip(CircleShape)
            .background(playerSecondary))
    }
}

@Composable
internal fun PlayerSheetSurface(artUrl: String?, modifier: Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    Box(modifier.clip(shape).background(Color.Black)) {
        if (artUrl != null) AsyncImage(
            model = artUrl, contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize().blur(56.dp),
        )
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.79f)))
        content()
    }
}

@Composable
internal fun CoverArt(client: SubsonicClient, song: Song, modifier: Modifier = Modifier) {
    val artUrl = remember(client, song.coverArt) { song.coverArt?.let { client.coverArtUrl(it, 768) } }
    if (artUrl == null) {
        Box(modifier.background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(MaterialSymbols.RoundedFilled.Album, contentDescription = null, tint = playerSecondary, modifier = Modifier.size(42.dp))
        }
    } else {
        AsyncImage(artUrl, contentDescription = "${song.album} cover",
            contentScale = ContentScale.Crop, modifier = modifier.background(Color.White.copy(alpha = 0.12f)))
    }
}

private fun formatPlayerTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000).coerceAtLeast(0)
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

internal fun activeLyricIndex(lines: List<LyricLine>, synced: Boolean, positionMs: Long): Int =
    if (synced) lines.indexOfLast {
        it.text.isNotBlank() && it.startMs != null && it.startMs <= positionMs
    } else -1

internal fun formatQueueDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "${hours}h ${minutes}m ${seconds}s" else "${minutes}m ${seconds}s"
}

internal fun playNextQueueIndex(sourceIndex: Int, currentIndex: Int): Int =
    if (sourceIndex < currentIndex) currentIndex else currentIndex + 1
