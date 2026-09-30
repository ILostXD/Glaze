package com.liquidglass.shared

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.roundedfilled.*

data class JamQueueEntry(val id: String, val trackId: String, val addedBy: String,
                         val voters: Set<String>)
data class JamMember(val id: String, val name: String, val avatar: ImageBitmap? = null)
data class JamPlayback(val trackId: String, val playing: Boolean, val positionMs: Long,
                       val updatedAtMs: Long, val serverTimeMs: Long)
data class JamViewState(
    val url: String = "https://jam.andyhserver.duckdns.org",
    val name: String = "",
    val sessionId: String = "",
    val inviteToken: String = "",
    val memberId: String = "",
    val hostId: String = "",
    val connection: String = "Disconnected",
    val members: List<JamMember> = emptyList(),
    val queue: List<JamQueueEntry> = emptyList(),
    val playback: JamPlayback? = null,
    val error: String? = null,
    val pendingInvite: String = "",
    val inviteQr: ImageBitmap? = null,
    val guestPlayback: Boolean = true,
) {
    val isHost: Boolean get() = sessionId.isNotEmpty() && memberId == hostId
}

data class JamActions(
    val connect: (url: String, name: String, sessionId: String?) -> Unit,
    val leave: () -> Unit,
    val add: (trackId: String) -> Unit,
    val remove: (itemId: String) -> Unit,
    val clear: () -> Boolean,
    val next: () -> Unit,
    val share: () -> Unit,
    val move: (itemId: String, toIndex: Int) -> Unit,
    val setGuestPlayback: (Boolean) -> Unit,
)

private enum class JamPage { Welcome, Join, Invite, GuestControls }
private val jamSecondary = Color.White.copy(alpha = 0.66f)
private val jamGlass = Color.White.copy(alpha = 0.09f)

@Composable
internal fun JamScreen(
    client: SubsonicClient, state: JamViewState, actions: JamActions,
    nowPlaying: Song?, showGuestControls: Boolean,
) {
    val active = state.sessionId.isNotEmpty()
    val connecting = state.connection == "Connecting"
    var page by remember(state.sessionId, showGuestControls) {
        mutableStateOf(if (active) { if (showGuestControls) JamPage.GuestControls else JamPage.Invite }
            else if (state.pendingInvite.isNotBlank()) JamPage.Join else JamPage.Welcome)
    }
    var invite by remember { mutableStateOf(state.pendingInvite) }
    LaunchedEffect(state.pendingInvite) {
        if (state.pendingInvite.isNotBlank() && !active) {
            invite = state.pendingInvite
            page = JamPage.Join
        }
    }
    val hostName = state.members.firstOrNull { it.id == state.hostId }?.name?.ifBlank { null }
        ?: state.name.ifBlank { "Your" }
    val title = if (hostName == "Your") "Your Jam" else "$hostName’s Jam"
    val artUrl = nowPlaying?.coverArt?.let { client.coverArtUrl(it, 768) }
    PlayerSheetSurface(artUrl, Modifier.fillMaxWidth()) {
        Column(Modifier.heightIn(max = 620.dp)
            .fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp)) {
            SheetHandle()
            Text(when (page) {
                JamPage.Welcome -> "Listen together"
                JamPage.Join -> "Join a Jam"
                JamPage.Invite -> "Invite friends"
                JamPage.GuestControls -> "Guest controls"
            }, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp,
                    top = 10.dp, bottom = 16.dp))
            state.error?.let {
                Text(it, color = Color(0xFFFFB4AB), fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp))
            }
            when (page) {
                JamPage.Welcome, JamPage.Join -> Column(Modifier.weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (page == JamPage.Welcome) {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(jamGlass)
                            .padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(54.dp).background(jamGlass, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(MaterialSymbols.RoundedFilled.Groups, null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("One queue. Everyone’s taste.", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("Invite friends and pick what plays next.", color = jamSecondary, fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 5.dp))
                            }
                        }
                        JamButton(if (connecting) "Starting…" else "Start a Jam", MaterialSymbols.RoundedFilled.Groups,
                            enabled = !connecting) { actions.connect(state.url, state.name, null) }
                        JamButton("Join with an invite", MaterialSymbols.RoundedFilled.Link, primary = false,
                            enabled = !connecting) { page = JamPage.Join }
                    } else {
                        Text("Paste the invite link or code your host shared.",
                            color = jamSecondary, fontSize = 15.sp)
                        JamField(invite, { invite = it }, "Invite link or code", enabled = !connecting)
                        JamButton(if (connecting) "Joining…" else "Join Jam", MaterialSymbols.RoundedFilled.Groups,
                            enabled = invite.isNotBlank() && !connecting) {
                            actions.connect(state.url, state.name, invite.trim())
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                JamPage.Invite -> Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text("Send a link or let friends scan your code.", color = jamSecondary, fontSize = 15.sp)
                    state.inviteQr?.let { qr ->
                        Image(qr, "Scan to join $title", Modifier.size(212.dp).clip(RoundedCornerShape(22.dp))
                            .background(Color.White).padding(14.dp))
                    }
                    Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    JamButton("Share invite link", MaterialSymbols.RoundedFilled.Share, enabled = state.inviteToken.isNotEmpty(),
                        onClick = actions.share)
                    Text("Friends need Glaze and access to the same music library.", color = jamSecondary,
                        fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
                JamPage.GuestControls -> Column(Modifier.padding(horizontal = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Play, pause, and skip tracks", color = Color.White, fontSize = 16.sp,
                            modifier = Modifier.weight(1f))
                        Switch(checked = state.guestPlayback, onCheckedChange = actions.setGuestPlayback)
                    }
                    Text("Everyone in the Jam can add and reorder the queue.",
                        color = jamSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
internal fun JamAvatar(member: JamMember, size: Int) {
    Box(Modifier.size(size.dp).border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
        .clip(CircleShape).background(Color.White.copy(alpha = 0.13f), CircleShape), contentAlignment = Alignment.Center) {
        if (member.avatar != null) Image(member.avatar, "${member.name}'s picture", Modifier.fillMaxSize())
        else Text(member.name.take(1).uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun JamButton(text: String, icon: ImageVector, modifier: Modifier = Modifier.fillMaxWidth(),
                      primary: Boolean = true, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, enabled = enabled, modifier = modifier.heightIn(min = 50.dp), shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = if (primary) Color.White else jamGlass,
            contentColor = if (primary) Color.Black else Color.White,
            disabledContainerColor = jamGlass, disabledContentColor = jamSecondary),
        border = if (primary) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)) {
        Icon(icon, null, Modifier.size(20.dp))
        Spacer(Modifier.width(9.dp))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun JamField(value: String, onChange: (String) -> Unit, placeholder: String,
                     modifier: Modifier = Modifier, enabled: Boolean = true) {
    val keyboard = LocalSoftwareKeyboardController.current
    BasicTextField(value, onChange, enabled = enabled, singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp), cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(18.dp))
            .background(jamGlass).border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
            .semantics { contentDescription = placeholder }.padding(horizontal = 16.dp, vertical = 14.dp),
        decorationBox = { field ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(MaterialSymbols.RoundedFilled.Search, null, tint = jamSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, color = jamSecondary, fontSize = 16.sp)
                    field()
                }
            }
        })
}
