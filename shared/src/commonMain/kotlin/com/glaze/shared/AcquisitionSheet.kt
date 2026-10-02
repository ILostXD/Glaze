package com.glaze.shared

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun AcquisitionSheet(credentials: ServerCredentials, companionUrl: String,
    request: LibraryRequest? = null, onJobCreated: (AcquisitionJob) -> Unit = {},
    currentJob: (String) -> AcquisitionJob? = { null }) {
    val api = remember(credentials, companionUrl) { AcquisitionClient(companionUrl, credentials) }
    DisposableEffect(api) { onDispose { api.close() } }
    val scope = rememberCoroutineScope()
    var artist by remember(request) { mutableStateOf(request?.artist.orEmpty()) }
    var title by remember(request) { mutableStateOf(request?.title.orEmpty()) }
    var kind by remember(request) { mutableStateOf(request?.kind ?: "album") }
    var busy by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<AcquisitionJob?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val displayedJob = job?.let { currentJob(it.id) ?: it }
    LaunchedEffect(api) {
        if (request != null) {
            busy = true
            try { job = api.create(artist, title, kind, request?.catalogueAlbumId, request?.albumTitle).also(onJobCreated) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { error = "Could not start acquisition. Check your connection or account." }
            finally { busy = false }
            return@LaunchedEffect
        }
        try { job = api.latest() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { /* Keep the form usable if status is temporarily unavailable. */ }
    }
    PlayerSheetSurface(null, Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp)) {
            SheetHandle()
            Text(if (request == null) "Add to Library" else "Download", color = Color.White, fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 22.dp, top = 10.dp, bottom = 6.dp))
            Text("Find a track or album and add it to your server library.",
                color = Color.White.copy(alpha = 0.66f), fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp))
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (request == null) {
                JamField(artist, { artist = it }, "Artist")
                JamField(title, { title = it }, if (kind == "album") "Album title" else "Track title")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (choice in listOf("album", "track")) {
                        val selected = kind == choice
                        Text(choice.replaceFirstChar { it.uppercase() }, color = Color.White,
                            fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = if (selected) 0.20f else 0.08f))
                                .clickable { kind = choice }
                                .padding(horizontal = 20.dp, vertical = 11.dp))
                    }
                }
                } else if (job == null) Text("$artist â€” $title", color = Color.White,
                    fontWeight = FontWeight.SemiBold)
                if (request == null || error != null || displayedJob?.status == "failed")
                JamButton(if (busy) "Searchingâ€¦" else if (request != null) "Retry request" else "Add to Library",
                    if (request != null) MaterialSymbols.RoundedFilled.Refresh else MaterialSymbols.RoundedFilled.Add,
                    enabled = artist.isNotBlank() && title.isNotBlank() && !busy) {
                    scope.launch {
                        busy = true
                        error = null
                        try { job = (displayedJob?.takeIf { it.status == "failed" &&
                            it.artist == artist.trim() && it.title == title.trim() && it.kind == kind }?.let { api.retry(it.id) }
                            ?: api.create(artist, title, kind, request?.catalogueAlbumId, request?.albumTitle)).also(onJobCreated) }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { error = "Could not start acquisition. Check your connection or account." }
                        finally { busy = false }
                    }
                }
                if (request != null && busy) Text("Starting your requestâ€¦", color = Color.White)
                displayedJob?.let { current ->
                    AcquisitionJobCard(current)
                }
                error?.let { Text(it, color = Color(0xFFFFB4AB), fontSize = 13.sp) }
            }
        }
    }
}

@Composable
internal fun AcquisitionStatusSheet(jobs: List<AcquisitionJob>, error: String? = null,
    onRetry: suspend (AcquisitionJob) -> Unit) {
    val scope = rememberCoroutineScope()
    val retrying = remember { mutableStateMapOf<String, Boolean>() }
    val errors = remember { mutableStateMapOf<String, String>() }
    PlayerSheetSurface(null, Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp)) {
            SheetHandle()
            Text("Downloads", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 22.dp, top = 10.dp, bottom = 18.dp))
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp),
                contentPadding = PaddingValues(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (error != null) item { Text(error, color = Color.LightGray) }
                if (jobs.isEmpty()) item { Text("No downloads yet", color = Color.LightGray) }
                items(jobs.take(20), key = { it.id }) { job ->
                    AcquisitionJobCard(job, retrying[job.id] == true, errors[job.id]) {
                        scope.launch {
                            retrying[job.id] = true
                            errors.remove(job.id)
                            try { onRetry(job) }
                            catch (cancelled: CancellationException) { throw cancelled }
                            catch (failure: Exception) { errors[job.id] = failure.message ?: "Could not retry. Please try again." }
                            finally { retrying[job.id] = false }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AcquisitionJobCard(job: AcquisitionJob, retrying: Boolean = false, retryError: String? = null, onRetry: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
        .background(Color.White.copy(alpha = 0.09f)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("${job.artist} â€” ${job.title}", color = Color.White,
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text((if (job.status == "rescanned") "Added to library" else
                job.status.replaceFirstChar { it.uppercase() }) +
                if (job.source.isNotBlank()) " Â· ${job.source}" else "",
                color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp)
            if (job.error.isNotBlank()) Text(when {
                job.error.contains("download_release_mismatch") -> "The downloaded file was a different album or edition. It wasn’t added."
                job.error.contains("download_is_compilation") -> "Only a compilation copy was found. It wasn’t added."
                job.error.contains("download_track_mismatch") || job.error.contains("download_artist_mismatch") -> "The downloaded song didn’t match your request. It wasn’t added."
                job.error.contains("download_metadata_unreadable") -> "Couldn’t verify the downloaded file’s tags. It wasn’t added."
                else -> job.error.replace('_', ' ')
            },
                color = Color(0xFFFFB4AB), fontSize = 13.sp)
            retryError?.let { Text(it, color = Color(0xFFFFB4AB), fontSize = 13.sp) }
        }
        if (job.status == "failed" && onRetry != null) IconButton(onClick = onRetry, enabled = !retrying,
            modifier = Modifier.semantics { contentDescription = if (retrying) "Retrying download" else "Retry download" }) {
            if (retrying) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Icon(MaterialSymbols.RoundedFilled.Refresh, contentDescription = null, tint = Color.White)
        }
        if (job.pending) CircularProgressIndicator(Modifier.padding(start = 14.dp).size(22.dp),
            color = Color.White, strokeWidth = 2.dp)
    }
}
