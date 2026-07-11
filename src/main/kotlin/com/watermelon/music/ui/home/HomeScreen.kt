package com.watermelon.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.watermelon.music.domain.model.HOME_CATEGORIES
import com.watermelon.music.domain.model.Song

import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen
import com.watermelon.music.ui.player.PlayerViewModel

@Composable
fun HomeScreen(navController: NavController, playerViewModel: PlayerViewModel? = null) {
    val viewModel = remember { HomeViewModel() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080808))) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFF3B3B)
            )
        } else if (viewModel.isError) {
            Text(
                text = "Failed to load home data. Please try again.",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Watermelon",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material.Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = "Library",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { navController.navigate(Screen.Library) }
                        )
                        androidx.compose.material.Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = "Radio",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { navController.navigate(Screen.Radio) }
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.DarkGray)
                                .clickable { navController.navigate(Screen.Profile) },
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material.Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                ) {
                    // Top Hits Section
                    if (viewModel.topHits.isNotEmpty()) {
                        item {
                            SongCategoryRow(
                                category = "🔥 Trending Top Hits",
                                songs = viewModel.topHits,
                                playerViewModel = playerViewModel
                            )
                        }
                    }

                    // Sequential Categories
                    items(HOME_CATEGORIES) { category ->
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
fun SongCategoryRow(
    category: String,
    songs: List<Song>,
    playerViewModel: PlayerViewModel? = null
) {
    Column(
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Text(
            text = category,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(songs) { song ->
                SongCard(song, onClick = {
                    playerViewModel?.playSong(song)
                })
            }
        }
    }
}

@Composable
fun SongCard(song: Song, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = song.thumbnail,
            contentDescription = "Song Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = song.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = song.artist,
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
