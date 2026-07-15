package com.watermelon.music.ui.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.watermelon.music.data.AuthRepository
import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    // Raw Supabase data — no processing from my side
    var profile by remember { mutableStateOf<AuthRepository.ProfileRow?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Dialog state
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var editDisplayName by remember { mutableStateOf("") }
    var editUsername by remember { mutableStateOf("") }
    var editAvatarUrl by remember { mutableStateOf("") }

    // Fetch profile from Supabase
    LaunchedEffect(Unit) {
        isLoading = true
        loadError = null
        try {
            email = authRepository.getCurrentUserEmail()
            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                profile = authRepository.fetchProfile(uid)
                if (profile == null) loadError = "Could not load profile (uid=$uid)"
            } else {
                loadError = "Not logged in"
            }
        } catch (e: Exception) {
            loadError = e.message
        }
        isLoading = false
    }

    val reloadProfile = {
        scope.launch {
            try {
                val uid = authRepository.getCurrentUserId()
                if (uid != null) profile = authRepository.fetchProfile(uid)
            } catch (_: Exception) {}
        }
    }

    // ── Raw values straight from profiles table ────────────────────────────
    val p = profile

    // profiles.display_name
    val displayName = p?.display_name?.takeIf { it.isNotBlank() }
        ?: email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Guest"

    // profiles.username
    val username = p?.username?.takeIf { it.isNotBlank() }
        ?: email?.substringBefore("@") ?: ""

    // profiles.avatar_url (PNG fallback — SVG does not render on desktop JVM)
    val avatarUrl = p?.avatar_url?.takeIf { it.isNotBlank() }
        ?: "https://api.dicebear.com/9.x/thumbs/png?seed=$username&size=200"

    // profiles.plan → display label
    val planLabel = when (p?.plan?.uppercase()) {
        "PREMIUM_INDIVIDUAL" -> "Premium"
        "PREMIUM_FAMILY"     -> "Family"
        "STUDENT"            -> "Student"
        else                 -> "FREE"
    }
    val isPremium = p?.is_premium == true

    // profiles.xp_total  (raw number from DB)
    val xpTotal = p?.xp_total ?: 0

    // profiles.xp_level  (raw number from DB)
    val xpLevel = p?.xp_level ?: 1

    fun getRankForLevel(level: Int): Pair<String, String> {
        val fullRank = when {
            level <= 10 -> "🌱 Seed Listener"
            level <= 20 -> "🌿 Sprout Wave"
            level <= 30 -> "🌊 Pulse Rider"
            level <= 40 -> "🌀 Echo Drift"
            level <= 50 -> "⚡ Resonance"
            level <= 60 -> "💽 Vinyl Hunter"
            level <= 70 -> "🌌 Frequency Soul"
            level <= 80 -> "🔥 NovaBeat"
            level <= 90 -> "🎶 Harmonic Flow"
            level <= 99 -> "👑 Reverb X"
            else -> "🌟 Spectrum Lord"
        }
        val emoji = fullRank.substring(0, fullRank.indexOf(" "))
        val name = fullRank.substring(fullRank.indexOf(" ") + 1)
        return Pair(emoji, name)
    }

    val (rankEmoji, rankName) = getRankForLevel(xpLevel)

    // profiles.songs_played  (raw number from DB)
    val songsPlayed = p?.songs_played ?: 0

    // Exact math from Android APK
    val xpLevelLong = xpLevel.toLong()
    val base = if (xpLevel <= 1) 0L else (xpLevelLong * xpLevelLong * 100L)
    val next = ((xpLevelLong + 1) * (xpLevelLong + 1) * 100L)
    val into = (xpTotal.toLong() - base).coerceAtLeast(0L)
    val need = (next - base).coerceAtLeast(1L)
    
    val xpProgress = (into.toFloat() / need.toFloat()).coerceIn(0f, 1f)
    val xpPct = (xpProgress * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = xpProgress,
        animationSpec = tween(800)
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFDC2626)
            )
            loadError != null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Failed to load profile", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(loadError ?: "", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { scope.launch { isLoading = true; email = authRepository.getCurrentUserEmail(); val uid = authRepository.getCurrentUserId(); if (uid != null) profile = authRepository.fetchProfile(uid); isLoading = false } },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFDC2626))) {
                    Text("Retry", color = Color.White)
                }
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ── TOP BAR ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(32.dp))
                    Text("Profile", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White,
                        modifier = Modifier.size(22.dp).clickable {
                            editDisplayName = p?.display_name ?: ""
                            editUsername = p?.username ?: ""
                            editAvatarUrl = p?.avatar_url ?: ""
                            showEditDialog = true
                        })
                }

                // ── AVATAR (left) + INFO (right) ──────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with red ring + camera badge
                    Box(modifier = Modifier.size(110.dp)) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                .border(3.5.dp, Color(0xFFC0392B), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(30.dp).clip(CircleShape).background(Color.White)
                                .clickable {
                                    editDisplayName = p?.display_name ?: ""
                                    editUsername = p?.username ?: ""
                                    editAvatarUrl = p?.avatar_url ?: ""
                                    showEditDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null,
                                tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Right column — all raw DB values
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // profiles.display_name
                        Text(displayName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        // profiles.username
                        if (username.isNotBlank())
                            Text("@$username", color = Color(0xFF9E9E9E), fontSize = 15.sp)
                        // profiles.plan
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPremium) Icons.Default.Star else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isPremium) Color(0xFFDC2626) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(planLabel, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                        // profiles.rank_tier — raw from DB
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .background(Color(0xFFDC2626).copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(rankEmoji, fontSize = 13.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(rankName, color = Color(0xFFDC2626), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // ── LEVEL / XP ─────────────────────────────────────────────
                // ALL values from Supabase: profiles.xp_level, profiles.xp_total, profiles.songs_played
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {

                    // "Level N"   ──────────   "xp / needed XP"
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Level $xpLevel", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("$into / $need XP", color = Color(0xFF9E9E9E), fontSize = 14.sp)
                    }

                    Spacer(Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF888888),
                        backgroundColor = Color(0xFF2A2A2A)
                    )

                    Spacer(Modifier.height(10.dp))

                    // "$xpPct% to Level N+1"   ────   "Total song played 🎵 N"
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("$xpPct% to Level ${xpLevel + 1}", color = Color(0xFF9E9E9E), fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total song played", color = Color(0xFF9E9E9E), fontSize = 13.sp)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.MusicNote, contentDescription = null,
                                tint = Color(0xFF9E9E9E), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            // profiles.songs_played — raw from DB
                            Text("$songsPlayed", color = Color(0xFF9E9E9E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))

                // ── ACTIONS ───────────────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    ProfileActionRow(Icons.Default.Delete, "Delete Account", Color(0xFFDC2626)) { showDeleteDialog = true }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1C1C1C)))
                    ProfileActionRow(Icons.AutoMirrored.Filled.Logout, "Log Out") { showLogoutDialog = true }
                }

                if (authError != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(authError!!, color = Color.Red, fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
                }
                Spacer(Modifier.height(40.dp))
            }
        }

        // ── EDIT DIALOG ───────────────────────────────────────────────────
        if (showEditDialog) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f))
                .clickable(enabled = !isProcessing) { showEditDialog = false },
                contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.width(440.dp).clickable(enabled = false) {},
                    backgroundColor = Color(0xFF161616), shape = RoundedCornerShape(20.dp), elevation = 12.dp) {
                    Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Edit Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = editDisplayName, onValueChange = { editDisplayName = it },
                            label = { Text("Display Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White,
                                focusedBorderColor = Color(0xFFDC2626), focusedLabelColor = Color(0xFFDC2626), cursorColor = Color(0xFFDC2626)))
                        OutlinedTextField(value = editUsername, onValueChange = { editUsername = it },
                            label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Text("@", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White,
                                focusedBorderColor = Color(0xFFDC2626), focusedLabelColor = Color(0xFFDC2626), cursorColor = Color(0xFFDC2626)))
                        OutlinedTextField(value = editAvatarUrl, onValueChange = { editAvatarUrl = it },
                            label = { Text("Avatar URL (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White,
                                focusedBorderColor = Color(0xFFDC2626), focusedLabelColor = Color(0xFFDC2626), cursorColor = Color(0xFFDC2626)))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { showEditDialog = false }, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent, contentColor = Color.White), enabled = !isProcessing) { Text("Cancel") }
                            Button(onClick = {
                                isProcessing = true; authError = null
                                scope.launch {
                                    val result = authRepository.updateProfile(editDisplayName.trim(), editUsername.trim(), "", editAvatarUrl.trim())
                                    isProcessing = false
                                    if (result.isSuccess) { showEditDialog = false; reloadProfile() }
                                    else authError = result.exceptionOrNull()?.message ?: "Failed to save"
                                }
                            }, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFDC2626)), enabled = !isProcessing) {
                                if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                else Text("Save", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ── LOGOUT DIALOG ─────────────────────────────────────────────────
        if (showLogoutDialog) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f))
                .clickable(enabled = !isProcessing) { showLogoutDialog = false },
                contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.width(360.dp).clickable(enabled = false) {},
                    backgroundColor = Color(0xFF161616), shape = RoundedCornerShape(16.dp), elevation = 8.dp) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Log Out", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Are you sure you want to log out?", color = Color.Gray, fontSize = 14.sp)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { showLogoutDialog = false }, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent, contentColor = Color.White), enabled = !isProcessing) { Text("Cancel") }
                            Button(onClick = { isProcessing = true
                                scope.launch { authRepository.signOut(); isProcessing = false; showLogoutDialog = false; navController.navigate(Screen.Login) }
                            }, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFDC2626)), enabled = !isProcessing) {
                                if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                else Text("Log Out", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ── DELETE ACCOUNT DIALOG ─────────────────────────────────────────
        if (showDeleteDialog) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f))
                .clickable(enabled = !isProcessing) { showDeleteDialog = false },
                contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.width(400.dp).clickable(enabled = false) {},
                    backgroundColor = Color(0xFF161616), shape = RoundedCornerShape(16.dp), elevation = 8.dp) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
                        Text("Delete Account", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("This will permanently delete your account and all data. Cannot be undone.",
                            color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { showDeleteDialog = false }, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent, contentColor = Color.White), enabled = !isProcessing) { Text("Cancel") }
                            Button(onClick = { isProcessing = true; authError = null
                                scope.launch {
                                    val result = authRepository.deleteAccount()
                                    isProcessing = false
                                    if (result.isSuccess) { showDeleteDialog = false; navController.navigate(Screen.Login) }
                                    else { authError = result.exceptionOrNull()?.message ?: "Failed"; showDeleteDialog = false }
                                }
                            }, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFDC2626)), enabled = !isProcessing) {
                                if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                else Text("Delete Forever", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111111)).clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}
