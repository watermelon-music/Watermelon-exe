package com.watermelon.music.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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

@Composable
fun MiniPlayer(viewModel: PlayerViewModel) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val library by viewModel.library.collectAsState()

    val isShuffle by viewModel.isShuffle.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()
    val volume by viewModel.volume.collectAsState()

    if (currentSong == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF181818))
    ) {
        androidx.compose.material.Slider(
            value = progress,
            onValueChange = { viewModel.seek(it) },
            modifier = Modifier.fillMaxWidth().height(16.dp).padding(horizontal = 8.dp),
            colors = androidx.compose.material.SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFFFF3B3B),
                inactiveTrackColor = Color.DarkGray
            )
        )
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Song Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = currentSong?.thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong?.title ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong?.artist ?: "",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                val isLiked = library.likedSongs.any { it.id == currentSong?.id || (it.streamUrl == currentSong?.streamUrl && !it.streamUrl.isNullOrEmpty()) } ||
                              library.likedRadios.any { it.id == currentSong?.id || (it.streamUrl == currentSong?.streamUrl && !it.streamUrl.isNullOrEmpty()) } ||
                              library.likedBroadcasts.any { it.id == currentSong?.id || (it.streamUrl == currentSong?.streamUrl && !it.streamUrl.isNullOrEmpty()) }
                              
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color(0xFFFF3B3B) else Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.toggleLike() }
                        .padding(start = 8.dp)
                )
            }

            // Center: Controls
            Row(
                modifier = Modifier.weight(1.5f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) Color(0xFFFF3B3B) else Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { viewModel.toggleShuffle() }
                )
                Spacer(modifier = Modifier.width(24.dp))
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { viewModel.playPrevious() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { viewModel.playNext() }
                )
                Spacer(modifier = Modifier.width(24.dp))
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Loop",
                    tint = if (isLooping) Color(0xFFFF3B3B) else Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { viewModel.toggleLoop() }
                )
            }

            // Right: Extras
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1f)
            ) {
                var showPlaylistMenu by remember { mutableStateOf(false) }
                Box {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showPlaylistMenu = true }
                    )
                    DropdownMenu(
                        expanded = showPlaylistMenu,
                        onDismissRequest = { showPlaylistMenu = false }
                    ) {
                        library.playlists.forEach { playlist ->
                            DropdownMenuItem(onClick = {
                                com.watermelon.music.data.LibraryEngine.addSongToPlaylist(playlist.id, currentSong!!)
                                showPlaylistMenu = false
                            }) {
                                Text(playlist.name)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                androidx.compose.material.Slider(
                    value = volume,
                    onValueChange = { viewModel.setVolume(it) },
                    modifier = Modifier.width(80.dp).padding(start = 8.dp),
                    colors = androidx.compose.material.SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.DarkGray
                    )
                )
            }
        }
    }
}
