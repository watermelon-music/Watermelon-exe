package com.watermelon.music.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Placeholder lyrics
                val lyrics = listOf(
                    "This is a beautiful song",
                    "Playing just for you",
                    "We don't have real lyrics yet",
                    "But it sure looks great",
                    "In this full screen view",
                    "Watermelon music is awesome",
                    "Feel the beat, feel the rhythm",
                    "Keep on listening",
                    "All night long"
                )
                
                items(lyrics.size) { index ->
                    val isActive = index == 3 // Highlight a random line for effect
                    Text(
                        text = lyrics[index],
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = if (isActive) 36.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 44.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
