package com.watermelon.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.watermelon.music.domain.model.Song

@Composable
fun SongActionDialog(
    song: Song,
    isRadioOrBroadcast: Boolean = false,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onLike: () -> Unit,
    onAddToPlaylist: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF161616))
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF222222)))

            // Play Option
            ActionRow("Play Now", Icons.Default.PlayArrow) {
                onPlay()
                onDismiss()
            }

            // Like Option
            ActionRow("Like", Icons.Default.Favorite) {
                onLike()
                onDismiss()
            }

            // Add to Playlist (Only for normal songs)
            if (!isRadioOrBroadcast) {
                ActionRow("Add to Playlist", Icons.AutoMirrored.Filled.PlaylistAdd) {
                    onAddToPlaylist()
                    onDismiss()
                }
            }
        }
    }
}

@Composable
fun ActionRow(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
