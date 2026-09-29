package com.liquidglass.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class JamQueueEntry(val id: String, val trackId: String, val addedBy: String,
                         val voters: Set<String>)
data class JamMember(val id: String, val name: String)
data class JamPlayback(val trackId: String, val playing: Boolean, val positionMs: Long,
                       val updatedAtMs: Long, val serverTimeMs: Long)
data class JamViewState(
    val url: String = "https://jam.andyhserver.duckdns.org",
    val name: String = "",
    val hasToken: Boolean = false,
    val sessionId: String = "",
    val inviteToken: String = "",
    val memberId: String = "",
    val hostId: String = "",
    val connection: String = "Disconnected",
    val members: List<JamMember> = emptyList(),
    val queue: List<JamQueueEntry> = emptyList(),
    val playback: JamPlayback? = null,
    val error: String? = null,
) {
    val isHost: Boolean get() = sessionId.isNotEmpty() && memberId == hostId
}

data class JamActions(
    val connect: (url: String, token: String, name: String, sessionId: String?) -> Unit,
    val leave: () -> Unit,
    val add: (trackId: String) -> Unit,
    val vote: (itemId: String, vote: Boolean) -> Unit,
    val remove: (itemId: String) -> Unit,
    val next: () -> Unit,
    val toggle: () -> Unit,
    val share: () -> Unit,
)

@Composable
internal fun JamScreen(client: SubsonicClient, state: JamViewState, actions: JamActions,
                       bottomPadding: Dp) {
    var url by remember(state.url) { mutableStateOf(state.url) }
    var name by remember(state.name) { mutableStateOf(state.name) }
    var token by remember { mutableStateOf("") }
    var invite by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Song>>(emptyList()) }
    val songs = remember { mutableStateMapOf<String, Song>() }
    val tracks = state.queue.map { it.trackId } + listOfNotNull(state.playback?.trackId?.takeIf { it.isNotEmpty() })
    LaunchedEffect(tracks) {
        tracks.distinct().filterNot(songs::containsKey).forEach { id ->
            runCatching { client.songById(id) }.getOrNull()?.let { songs[id] = it }
        }
    }
    LaunchedEffect(query) {
        results = emptyList()
        if (query.isNotBlank()) {
            delay(300)
            results = runCatching { client.search(query, songCount = 12).songs }.getOrDefault(emptyList())
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(horizontal = 22.dp)) {
        val hostName = state.members.firstOrNull { it.id == state.hostId }?.name ?: state.name
        Text(if (state.sessionId.isEmpty()) "Start a Jam" else "$hostName's Jam",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
        Text("Listen together from the same music library.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 12.dp)) }
        if (state.sessionId.isEmpty()) {
            Spacer(Modifier.height(22.dp))
            OutlinedTextField(url, { url = it }, label = { Text("Companion HTTPS address") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            if (invite.isBlank()) OutlinedTextField(token, { token = it },
                label = { Text(if (state.hasToken) "Host token (leave blank to keep saved)" else "Host token") },
                visualTransformation = PasswordVisualTransformation(), singleLine = true,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(name, { name = it }, label = { Text("Your name") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(onClick = { actions.connect(url, token, name, null); token = "" },
                modifier = Modifier.fillMaxWidth()) { Text("Start a Jam") }
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(invite, { invite = it }, label = { Text("Invite code") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            TextButton(onClick = { actions.connect(url, "", name, invite.trim()); token = "" },
                enabled = invite.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text("Join Jam")
            }
        } else {
            Spacer(Modifier.height(18.dp))
            Text(if (state.isHost) "Hosting · ${state.connection}" else "Listening · ${state.connection}",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!state.isHost) Text("The host controls playback on everyone’s device.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (state.isHost) Button(onClick = actions.share,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) { Text("Share invite") }
            Text("People in this Jam", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                state.members.forEach { member ->
                    Box(Modifier.size(42.dp).background(Color.White.copy(alpha = 0.13f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Text(member.name.take(1).uppercase(), fontWeight = FontWeight.Bold)
                    }
                }
            }
            val current = state.playback?.trackId.orEmpty()
            Text("Queue", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 10.dp))
            Row(Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(18.dp))
                .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                songs[current]?.let { CoverArt(client, it, Modifier.size(52.dp)) }
                if (current.isNotEmpty()) Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (current.isEmpty()) "Nothing playing" else songs[current]?.title ?: "Loading song…",
                        maxLines = 1, fontWeight = FontWeight.SemiBold)
                    Text(songs[current]?.artist ?: if (current.isEmpty()) "Add songs to get started" else "",
                        maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (state.isHost && current.isNotEmpty()) TextButton(actions.toggle) {
                    Text(if (state.playback?.playing == true) "Pause" else "Play")
                }
            }
            if (state.isHost && state.queue.isNotEmpty()) TextButton(actions.next) { Text("Play next") }
            Text("Up next", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            if (state.queue.isEmpty()) Text("The queue is empty.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.queue.forEach { entry ->
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(14.dp))
                    .padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(songs[entry.trackId]?.title ?: entry.trackId, maxLines = 1)
                        Text("${songs[entry.trackId]?.artist ?: "Loading…"} · ${entry.voters.size} votes",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (entry.voters.contains(state.memberId)) "▲" else "△",
                        modifier = Modifier.clickable {
                            actions.vote(entry.id, !entry.voters.contains(state.memberId))
                        }.padding(8.dp))
                    if (state.isHost || entry.addedBy == state.memberId)
                        Text("×", modifier = Modifier.clickable { actions.remove(entry.id) }
                            .padding(8.dp))
                }
            }
            Text("Add a song", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 22.dp))
            OutlinedTextField(query, { query = it }, label = { Text("Search your library") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            results.forEach { song ->
                Row(Modifier.fillMaxWidth().clickable { actions.add(song.id); query = "" }
                    .padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(song.title, maxLines = 1)
                        Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    Text("+", modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            TextButton(actions.leave) { Text(if (state.isHost) "End Jam" else "Leave Jam") }
        }
        Spacer(Modifier.height(bottomPadding))
    }
}
