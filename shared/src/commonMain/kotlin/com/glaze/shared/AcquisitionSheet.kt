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
            try { job = api.create(artist, title, kind).also(onJobCreated) }
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
                } else if (job == null) Text("$artist — $title", color = Color.White,
                    fontWeight = FontWeight.SemiBold)
                if (request == null || error != null || displayedJob?.status == "failed")
                JamButton(if (busy) "Searching…" else if (request != null) "Retry request" else "Add to Library", MaterialSymbols.RoundedFilled.Add,
                    enabled = artist.isNotBlank() && title.isNotBlank() && !busy) {
                    scope.launch {
                        busy = true
                        error = null
                        try { job = api.create(artist, title, kind).also(onJobCreated) }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { error = "Could not start acquisition. Check your connection or account." }
                        finally { busy = false }
                    }
                }
                if (request != null && busy) Text("Starting your request…", color = Color.White)
                displayedJob?.let { current ->
                    AcquisitionJobCard(current)
                }
                error?.let { Text(it, color = Color(0xFFFFB4AB), fontSize = 13.sp) }
            }
        }
    }
}

@Composable
internal fun AcquisitionStatusSheet(jobs: List<AcquisitionJob>, error: String? = null) {
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
                items(jobs.take(20), key = { it.id }) { AcquisitionJobCard(it) }
            }
        }
    }
}

@Composable
private fun AcquisitionJobCard(job: AcquisitionJob) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
        .background(Color.White.copy(alpha = 0.09f)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("${job.artist} — ${job.title}", color = Color.White,
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text((if (job.status == "rescanned") "Added to library" else
                job.status.replaceFirstChar { it.uppercase() }) +
                if (job.source.isNotBlank()) " · ${job.source}" else "",
                color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp)
            if (job.error.isNotBlank()) Text(job.error.replace('_', ' '),
                color = Color(0xFFFFB4AB), fontSize = 13.sp)
        }
        if (job.pending) CircularProgressIndicator(Modifier.padding(start = 14.dp).size(22.dp),
            color = Color.White, strokeWidth = 2.dp)
    }
}
