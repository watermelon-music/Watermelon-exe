package com.watermelon.music.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.watermelon.music.data.GamificationEngine
import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen
import com.watermelon.music.ui.home.AdBannerPlaceholder

@Composable
fun ProfileScreen(navController: NavController) {
    val userStats by GamificationEngine.userStats.collectAsState()
    val currentLevel = GamificationEngine.getLevel(userStats.totalXp)
    val progress = GamificationEngine.getXpProgress(userStats.totalXp)
    val totalSongs = userStats.songsPlayed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)) // Deep dark theme background
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { navController.popBackStack() }) {
                Text("Back", color = Color(0xFFFF4040), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text("Profile", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(24.dp))
        }

        // AVATAR & INFO SECTION
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AVATAR
            Box(modifier = Modifier.size(120.dp)) {
                AsyncImage(
                    model = "https://i.pravatar.cc/150?img=11",
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(4.dp, Color(0xFF7D1B1B), CircleShape) // Dark red border
                )
                // Camera icon badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4040)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // INFO
            Column {
                Text("satu", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Text("@Satyam Pote", color = Color.Gray, fontSize = 16.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // PREMIUM BADGE
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = "Premium", tint = Color(0xFFFF4040), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PREMIUM INDIVIDUAL", color = Color(0xFFFF4040), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                // SEED LISTENER BADGE
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E0C0C)) // Very dark red/brown
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Eco, contentDescription = "Seed", tint = Color(0xFF8BC34A), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Seed Listener", color = Color(0xFFFF2222), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // PROGRESS SECTION (Match design)
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("Level $currentLevel", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Text("${userStats.totalXp} / ${(currentLevel * 1000)} XP", color = Color.LightGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(16.dp).clip(CircleShape),
                color = Color(0xFFFF8A8A), // Light pinkish red
                backgroundColor = Color(0xFF2A2A2A)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(progress * 100).toInt()}% to Level ${currentLevel + 1}", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Total song played ", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(" $totalSongs", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF161616))
                .border(1.dp, Color(0xFF222222), RoundedCornerShape(24.dp))
        ) {
            ProfileOptionRow(Icons.Default.Share, "Share Application")
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF222222)))
            ProfileOptionRow(Icons.Default.Info, "About")
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF222222)))
            ProfileOptionRow(Icons.Default.Delete, "Delete Account", Color(0xFFFF4040))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF222222)))
            ProfileOptionRow(Icons.AutoMirrored.Filled.Logout, "Log Out")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileOptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, tint: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = tint, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.Gray, modifier = Modifier.size(24.dp))
    }
}
