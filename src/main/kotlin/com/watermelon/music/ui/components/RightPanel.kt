package com.watermelon.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.watermelon.music.ui.player.PlayerViewModel
import com.watermelon.music.ui.components.SongActionDialog

@Composable
fun RightPanel(playerViewModel: PlayerViewModel, onClose: () -> Unit, onLyricsClick: () -> Unit = {}) {
    val currentSong by playerViewModel.currentSong.collectAsState()
    val recommendedSongs by playerViewModel.recommendedSongs.collectAsState()
    var showActionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Color(0xFF050505))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), // Increased padding to avoid collapse
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MUSIC",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "Options", tint = Color.Gray, modifier = Modifier.size(20.dp).clickable { showActionDialog = true })
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(20.dp).clickable { onClose() })
            }
        }

        if (currentSong != null) {
            val song = currentSong!!
            
            // Huge Cover Art
            AsyncImage(
                model = song.thumbnail,
                contentDescription = "Cover Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title and Artist
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                color = Color.Gray,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { /* Open artist page */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recommended
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up remaining space for scrolling
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF111111)) // Slightly lighter than pitch black for contrast
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = "Recommended",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Real recommended songs
                    if (recommendedSongs.isEmpty()) {
                        Text("Loading recommendations...", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        Column(modifier = Modifier.weight(1f).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                            recommendedSongs.forEach { recSong ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable { playerViewModel.playSong(recSong) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = recSong.thumbnail,
                                        contentDescription = "Song",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = recSong.title,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = recSong.artist,
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Empty State
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Play a song to see details", color = Color.Gray)
            }
        }
    }

    if (showActionDialog && currentSong != null) {
        val type by playerViewModel.currentType.collectAsState()
        SongActionDialog(
            song = currentSong!!,
            isRadioOrBroadcast = type == "radio" || type == "broadcast",
            onDismiss = { showActionDialog = false },
            onPlay = { playerViewModel.togglePlayPause() },
            onLike = { playerViewModel.toggleLike() },
            onAddToPlaylist = { /* Future */ },
            onLyrics = { 
                showActionDialog = false
                onLyricsClick() 
            }
        )
    }
}
