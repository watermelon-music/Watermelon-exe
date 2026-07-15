package com.watermelon.music.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import com.watermelon.music.data.LibraryEngine
import com.watermelon.music.domain.model.Song
import com.watermelon.music.ui.player.PlayerViewModel
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton

@Composable
fun LibraryScreen(playerViewModel: PlayerViewModel?) {
    val library by LibraryEngine.library.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Liked Songs, 1 = Playlists, 2 = Radios, 3 = Broadcasts
    var playlistToDelete by remember { mutableStateOf<String?>(null) }

    if (playlistToDelete != null) {
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Delete Playlist", color = Color.White) },
            text = { Text("Are you sure you want to delete this playlist?", color = Color.Gray) },
            backgroundColor = Color(0xFF1E1E1E),
            confirmButton = {
                TextButton(onClick = {
                    LibraryEngine.deletePlaylist(playlistToDelete!!)
                    playlistToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFFF3B3B))
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        LibraryEngine.syncWithCloud()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080808))
            .padding(16.dp)
    ) {
        Text(
            text = "Your Library",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TabButton("Music", isSelected = selectedTab == 0) { selectedTab = 0 }
            TabButton("Radios & Broadcasts", isSelected = selectedTab == 1) { selectedTab = 1 }
        }

        if (selectedTab == 0) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    // Liked Songs Item
                    var isExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF121212))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE53935)), // Red background for liked songs
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Liked Songs", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Liked Songs", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("${library.likedSongs.size} songs", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                        if (isExpanded) {
                            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                if (library.likedSongs.isEmpty()) {
                                    Text("No liked songs yet.", color = Color.Gray, fontSize = 14.sp)
                                } else {
                                    Button(
                                        onClick = { playerViewModel?.playSong(library.likedSongs.first(), library.likedSongs) },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1DB954)),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play All",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Play All", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    library.likedSongs.forEach { song ->
                                        LibrarySongRow(
                                            song = song,
                                            icon = Icons.Default.Favorite,
                                            onIconClick = { LibraryEngine.toggleLike(song) }
                                        ) {
                                            playerViewModel?.playSong(song, library.likedSongs)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                items(library.playlists) { playlist ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF121212))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PlaylistCoverGrid(songs = playlist.songs, size = 64.dp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = playlist.name,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${playlist.songs.size} songs",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            IconButton(onClick = { playlistToDelete = playlist.id }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = Color.Gray)
                            }
                        }
                        if (isExpanded) {
                            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                if (playlist.songs.isEmpty()) {
                                    Text("No songs in this playlist.", color = Color.Gray, fontSize = 14.sp)
                                } else {
                                    Button(
                                        onClick = { playerViewModel?.playSong(playlist.songs.first(), playlist.songs) },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1DB954)),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play All",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Play All", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    playlist.songs.forEach { song ->
                                        LibrarySongRow(
                                            song = song,
                                            icon = Icons.Default.Delete,
                                            onIconClick = { LibraryEngine.removeSongFromPlaylist(playlist.id, song.id) }
                                        ) {
                                            playerViewModel?.playSong(song, playlist.songs)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Text("Liked Radios & Broadcasts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        if (library.likedRadios.isEmpty() && library.likedBroadcasts.isEmpty()) {
                            Text("No liked stations yet.", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                val combinedStations = library.likedRadios + library.likedBroadcasts
                items(combinedStations) { station ->
                    val isRadio = library.likedRadios.any { it.id == station.id }
                    LibrarySongRow(
                        song = station,
                        icon = Icons.Default.Favorite,
                        onIconClick = {
                            if (isRadio) {
                                LibraryEngine.toggleLikeRadio(station)
                            } else {
                                LibraryEngine.toggleLikeBroadcast(station)
                            }
                        }
                    ) {
                        playerViewModel?.playRadio(station, isBroadcast = !isRadio)
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) Color(0xFFFF3B3B) else Color.DarkGray)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun LibrarySongRow(song: Song, icon: ImageVector? = null, onIconClick: (() -> Unit)? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.thumbnail,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (icon != null && onIconClick != null) {
            IconButton(onClick = onIconClick) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (icon == Icons.Default.Favorite) Color(0xFFFF3B3B) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PlaylistCoverGrid(songs: List<Song>, size: androidx.compose.ui.unit.Dp = 64.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF282828)),
        contentAlignment = Alignment.Center
    ) {
        val images = songs.map { it.thumbnail }.filter { it.isNotBlank() }.take(4)
        if (images.isEmpty()) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(size / 2)
            )
        } else if (images.size < 4) {
            AsyncImage(
                model = images.first(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AsyncImage(
                        model = images[0],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    AsyncImage(
                        model = images[1],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AsyncImage(
                        model = images[2],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    AsyncImage(
                        model = images[3],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }
    }
}
