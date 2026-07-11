package com.watermelon.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.watermelon.music.domain.model.Song
import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen
import com.watermelon.music.ui.player.PlayerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(navController: NavController, playerViewModel: PlayerViewModel? = null) {
    val viewModel = remember { HomeViewModel() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080808))) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFF4040)
            )
        } else if (viewModel.isError) {
            Text(
                text = "Failed to load home data. Please try again.",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // TOP BAR
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Bar
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Search artists, songs...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, tint = Color.Gray, contentDescription = null) },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFF1E1E1E),
                            textColor = Color.White,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.width(300.dp).height(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.loadData() }
                    )
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { navController.navigate(Screen.Profile) }
                    ) {
                        Text("Alex Rivera", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        ) {
                            // User Avatar Profile Picture (Placeholder)
                            AsyncImage(
                                model = "https://i.pravatar.cc/150?img=11",
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                ) {
                    // HERO BANNER
                    item {
                        HeroBanner(viewModel, playerViewModel)
                    }

                    // RECOMMENDED ROW
                    if (viewModel.topHits.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Recommended for You",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Based on your recent listening history",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(viewModel.topHits) { song ->
                                    ModernSongCard(song) {
                                        playerViewModel?.playSong(song)
                                    }
                                }
                            }
                        }
                    }

                    // Sequential Categories
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    items(com.watermelon.music.domain.model.HOME_CATEGORIES) { category ->
                        val songs = viewModel.categories[category.id]
                        if (!songs.isNullOrEmpty()) {
                            SongCategoryRow(
                                category = category.title,
                                songs = songs,
                                playerViewModel = playerViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBanner(viewModel: HomeViewModel, playerViewModel: PlayerViewModel?) {
    val carouselSongs = viewModel.topHits.take(5)
    if (carouselSongs.isEmpty()) {
        // Fallback static banner if no songs are loaded
        Box(
            modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(16.dp)).background(Color.DarkGray)
        )
        return
    }

    var currentIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    
    androidx.compose.runtime.LaunchedEffect(carouselSongs) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            if (carouselSongs.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % carouselSongs.size
            }
        }
    }

    val currentSong = carouselSongs.getOrNull(currentIndex) ?: return

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.DarkGray)
    ) {
        // Background Image with Cinematic Pan/Zoom (looks like a video)
        AsyncImage(
            model = currentSong.thumbnail,
            contentDescription = "Concert",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale)
        )
        // Cinematic Gradient Overlay
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY
            )
        ))
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("NEW RELEASE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = currentSong.title,
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Experience the newest from ${currentSong.artist}!",
                color = Color.LightGray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Play Now Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFF4040))
                        .clickable {
                            playerViewModel?.playSong(currentSong)
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Now", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Save Playlist Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2A2A2A))
                        .clickable { /* Save Playlist */ }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Save Playlist", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ModernSongCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(165.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = song.thumbnail,
            contentDescription = "Song Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E1E))
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = song.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = song.artist,
            color = Color(0xFFFF8A8A), // Light red/pinkish for artist name to match the aesthetic
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SongCategoryRow(category: String, songs: List<Song>, playerViewModel: PlayerViewModel?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = category,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(songs) { song ->
                ModernSongCard(song) {
                    playerViewModel?.playSong(song)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
