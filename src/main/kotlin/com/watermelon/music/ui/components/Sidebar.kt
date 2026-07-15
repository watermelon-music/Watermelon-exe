package com.watermelon.music.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.watermelon.music.data.LibraryEngine
import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen

@Composable
fun Sidebar(navController: NavController) {
    val library by LibraryEngine.library.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(72.dp)
            .background(Color(0xFF050505)) // Pitch Black
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Plus Button
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E1E))
                .clickable { 
                    if (library.playlists.size < 2) showCreateDialog = true 
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Liked Songs (Heart)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFF6070A))
                .clickable { navController.navigate(Screen.Library) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Liked Songs",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Playlists
        library.playlists.forEach { playlist ->
            val coverUrl = playlist.songs.firstOrNull()?.thumbnail ?: "https://community.spotify.com/t5/image/serverpage/image-id/25294i2836BD1C1A31BDF2/image-dimensions/250x250"
            PlaylistIcon(
                url = coverUrl,
                onClick = { navController.navigate(Screen.Library) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            backgroundColor = Color(0xFF1E1E1E),
            title = { Text("Create Playlist", color = Color.White) },
            text = {
                TextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist Name", color = Color.Gray) },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.White,
                        backgroundColor = Color(0xFF282828),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            LibraryEngine.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreateDialog = false
                            navController.navigate(Screen.Library)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF3B3B))
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCreateDialog = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF282828))
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun PlaylistIcon(url: String, onClick: () -> Unit = {}) {
    AsyncImage(
        model = url,
        contentDescription = "Playlist",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    )
}

@Composable
fun AvatarIcon(url: String) {
    AsyncImage(
        model = url,
        contentDescription = "User",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable { }
    )
}
