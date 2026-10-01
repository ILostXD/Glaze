package com.glaze.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock

@Composable
internal fun UpcomingAlbumScreen(album: UpcomingAlbum, api: ReleaseClient, client: SubsonicClient,
    darkMode: Boolean, onArtworkColor: suspend (String?) -> Color, onBack: () -> Unit,
    onShare: (String) -> Unit, onChanged: (UpcomingAlbum) -> Unit, onEnableNotifications: () -> Unit,
    onRequest: (LibraryRequest) -> Unit, onArtist: (Artist) -> Unit) {
    var detail by remember(album.id) { mutableStateOf(album) }
    var now by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    var pending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(album) { detail = album }
    LaunchedEffect(album.id, api) {
        try { detail = api.album(album.id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { error = "Couldn’t refresh this release. Showing the last details." }
        while (isActive) { now = Clock.System.now().toEpochMilliseconds(); delay(1_000) }
    }
    val future = detail.releaseAt > now
    CollectionSurface(detail.title, detail.cover, darkMode, onArtworkColor, onBack,
        onShare = { onShare("${detail.title} — ${detail.artist}\nReleases ${readableReleaseDate(detail.releaseDate)}") }, headerItemIndex = 3) {
        item { }
        item { Spacer(Modifier.height(32.dp)) }
        item {
            CollectionHeader(detail.title, detail.artist,
                "${if (future) "Releases" else "Released"} ${readableReleaseDate(detail.releaseDate)} · ${detail.trackCount} songs",
                null, client, artworkUrl = detail.cover,
                onSubtitleClick = { onArtist(Artist(detail.artistId, detail.artist)) })
        }
        item {
            if (future) ReleaseCountdown(detail.releaseAt, now)
            else Text("Out now", Modifier.fillMaxWidth().padding(20.dp), textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
        }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Button(enabled = !pending && (future || !detail.saved), onClick = {
                    if (!future) { onRequest(LibraryRequest(detail.artist, detail.title, "album")); return@Button }
                    pending = true; error = null
                    scope.launch {
                        try {
                            detail = api.save(detail.id, !detail.saved)
                            onChanged(detail)
                            if (detail.saved) onEnableNotifications()
                        } catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { error = "Couldn’t update Pre-Save. Please try again." }
                        finally { pending = false }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background), modifier = Modifier.height(52.dp).widthIn(min = 184.dp)) {
                    Text(if (pending) "Saving…" else if (!future && !detail.saved) "Add to library" else detail.saveLabel,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                Text(if (detail.saved) "Glaze will add this album to your library when it releases."
                    else "Pre-Save to add it to your library on release day.",
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                error?.let { Text(it, Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.error) }
                if (detail.status == "failed") Text("Acquisition will retry daily. You can check Downloads in Settings.",
                    Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        item { SectionTitle("Tracklist preview", 12.dp); CollectionDivider() }
        itemsIndexed(detail.tracks, key = { _, track -> track.id }) { index, track ->
            CollectionTrackRow(Song("upcoming:${track.id}", track.title, track.artist, detail.title,
                durationSeconds = track.duration, isExplicit = track.explicit), index + 1, false, client,
                onPlay = {}, dividerAbove = index > 0, downloadOnly = true,
                unavailable = future && !track.released, dimmed = future && !track.released) {
                onRequest(LibraryRequest(detail.artist, track.title, "track"))
            }
        }
        if (detail.tracks.isEmpty()) item {
            Text("${detail.trackCount} songs · Full tracklist hasn’t been announced yet.", Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { AlbumFooter(detail.trackCount, detail.tracks.sumOf { it.duration.toLong() }, detail.releaseDate) }
    }
}

@Composable
private fun ReleaseCountdown(at: Long, now: Long) {
    val counts = releaseCountdown(at, now)
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
        .clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f))
        .padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf("Days", "Hours", "Minutes", "Seconds").forEachIndexed { index, label ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(counts[index].toString().padStart(2, '0'), style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
