package com.watermelon.music.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watermelon.music.domain.model.Category
import com.watermelon.music.domain.model.HOME_CATEGORIES
import com.watermelon.music.ui.player.PlayerViewModel
import com.watermelon.music.ui.home.ModernSongCard
import com.watermelon.music.ui.home.BroadcastCard

@Composable
fun SearchScreen(playerViewModel: PlayerViewModel?, searchQuery: String = "") {
    val viewModel = remember { SearchViewModel() }
    var selectedFilter by remember { mutableStateOf("Music") }
    val filters = listOf("Music", "Radio & Broadcast")

    LaunchedEffect(searchQuery) {
        viewModel.search(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(24.dp)
    ) {

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFFFF4040) else Color(0xFF1E1E1E))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Type to search for music, radio, and more...",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // Search Results area
            Text(
                text = "Results for \"$searchQuery\"",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF4040))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val showMusic = selectedFilter == "Music"
                    val showRadio = selectedFilter == "Radio & Broadcast"

                    if (showMusic && viewModel.musicResults.isNotEmpty()) {
                        items(viewModel.musicResults) { song ->
                            ModernSongCard(
                                song = song,
                                onClick = { playerViewModel?.playSong(song, viewModel.musicResults) }
                            )
                        }
                    }

                    if (showRadio && viewModel.radioResults.isNotEmpty()) {
                        items(viewModel.radioResults) { song ->
                            BroadcastCard(
                                song = song,
                                onClick = { playerViewModel?.playRadio(song, viewModel.radioResults, isBroadcast = true) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrowseCategoryCard(category: Category) {
    // Generate a pseudo-random stable color based on category title
    val colorHash = category.title.hashCode()
    val color = Color(
        red = (colorHash and 0xFF) / 255f,
        green = ((colorHash shr 8) and 0xFF) / 255f,
        blue = ((colorHash shr 16) and 0xFF) / 255f
    )
    
    // Mix with some base color to ensure it's not too dark or light
    val blendedColor = Color(
        red = (color.red + 0.3f).coerceAtMost(1f),
        green = (color.green + 0.3f).coerceAtMost(1f),
        blue = (color.blue + 0.3f).coerceAtMost(1f)
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(blendedColor)
            .clickable { }
            .padding(16.dp)
    ) {
        Text(
            text = category.title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
