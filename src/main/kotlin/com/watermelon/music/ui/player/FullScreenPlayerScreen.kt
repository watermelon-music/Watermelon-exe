package com.watermelon.music.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.Text
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

@Composable
fun FullScreenPlayerScreen(viewModel: PlayerViewModel) {
    val currentSong by viewModel.currentSong.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val lyrics by viewModel.currentLyrics.collectAsState()

    if (currentSong == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF07304B)), contentAlignment = Alignment.Center) {
            Text("Nothing playing", color = Color.White)
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07304B)) // Deep blue gradient color similar to Spotify
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // LEFT SIDE: DP and Recommendations
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Album Artwork (DP)
            AsyncImage(
                model = currentSong?.thumbnail,
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f) // Square
                    .clip(RoundedCornerShape(16.dp))
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // "About the artist" / Recommendations section
            Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF888888).copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Text(
                        "About the artist",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF222222).copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Text(
                        "Credits",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        // RIGHT SIDE: Lyrics
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Lyrics",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            val listState = rememberLazyListState()
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (lyrics.isEmpty()) {
                    item {
                        Text(
                            text = "No lyrics found for this song...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        )
                    }
                } else {
                    val currentSecs = currentPositionMs / 1000f
                    val activeIndex = lyrics.indexOfLast { it.timeSeconds <= currentSecs }.coerceAtLeast(0)
                    
                    items(lyrics.size) { index ->
                        val isActive = index == activeIndex
                        Text(
                            text = lyrics[index].text,
                            color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
                            fontSize = if (isActive) 36.sp else 32.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 44.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
            
            // Auto scroll to active lyric line
            val activeIndex = if (lyrics.isNotEmpty()) lyrics.indexOfLast { it.timeSeconds <= (currentPositionMs / 1000f) }.coerceAtLeast(0) else -1
            LaunchedEffect(activeIndex) {
                if (activeIndex >= 0) {
                    // Try to scroll so the item is somewhat centered (e.g. subtracting an offset, or just animate to it)
                    // We'll scroll such that it's near the top for now
                    listState.animateScrollToItem(activeIndex)
                }
            }
        }
    }
}
