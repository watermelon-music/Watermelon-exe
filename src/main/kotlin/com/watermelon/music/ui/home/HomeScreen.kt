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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
fun HomeScreen(playerViewModel: PlayerViewModel? = null) {
    val viewModel = remember { HomeViewModel() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000))) {
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Text
                    Text(
                        text = "Watermelon",
                        color = Color(0xFFF6070A), // Dark Red/Watermelon color
                        fontSize = 48.sp, // Much bigger as requested
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                ) {
                    // FILTER CHIPS
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(text = "All", isSelected = viewModel.currentFilter == HomeViewModel.Filter.ALL) {
                                viewModel.setFilter(HomeViewModel.Filter.ALL)
                            }
                            FilterChip(text = "Music", isSelected = viewModel.currentFilter == HomeViewModel.Filter.MUSIC) {
                                viewModel.setFilter(HomeViewModel.Filter.MUSIC)
                            }
                            FilterChip(text = "Broadcast", isSelected = viewModel.currentFilter == HomeViewModel.Filter.BROADCASTS) {
                                viewModel.setFilter(HomeViewModel.Filter.BROADCASTS)
                            }
                            FilterChip(text = "Radio", isSelected = viewModel.currentFilter == HomeViewModel.Filter.RADIO) {
                                viewModel.setFilter(HomeViewModel.Filter.RADIO)
                            }
                        }
                    }

                    // HERO BANNER
                    if (viewModel.currentFilter == HomeViewModel.Filter.ALL || viewModel.currentFilter == HomeViewModel.Filter.MUSIC) {
                        item {
                            HeroBanner(viewModel, playerViewModel)
                        }
                    }

                    // No Recommended Row anymore, jump straight to Categories
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (viewModel.currentFilter == HomeViewModel.Filter.RADIO) {
                        if (viewModel.selectedRadioCountry == null) {
                            if (viewModel.topGlobalRadios.isNotEmpty()) {
                                item {
                                    SongCategoryRow(
                                        category = "Top Global Radios",
                                        songs = viewModel.topGlobalRadios,
                                        onSongClick = { song -> playerViewModel?.playRadio(song) }
                                    )
                                }
                            }
                            item {
                                Text(
                                    text = "Browse by Country",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
                                )
                            }
                            
                            val chunkedCountries = viewModel.radioCountries.chunked(3)
                            items(chunkedCountries) { rowCountries ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    for (country in rowCountries) {
                                        CountryCard(
                                            country = country,
                                            modifier = Modifier.weight(1f),
                                            onClick = { viewModel.selectRadioCountry(country) }
                                        )
                                    }
                                    for (i in rowCountries.size until 3) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        } else {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .clickable { viewModel.selectRadioCountry(null) }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Back to Countries", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(
                                    text = "Top Stations in ${viewModel.selectedRadioCountry?.name}",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                            }
                            
                            val chunkedStations = viewModel.countryStations.chunked(4)
                            items(chunkedStations) { rowStations ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    for (station in rowStations) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            ModernSongCard(station) {
                                                playerViewModel?.playRadio(station, viewModel.countryStations)
                                            }
                                        }
                                    }
                                    for (i in rowStations.size until 4) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    } else if (viewModel.currentFilter == HomeViewModel.Filter.ALL) {
                        item { AdBannerPlaceholder() }
                        // Show Radio in ALL
                        if (viewModel.topGlobalRadios.isNotEmpty()) {
                            item {
                                SongCategoryRow(
                                    category = "Radio Stations",
                                    songs = viewModel.topGlobalRadios.take(6),
                                    onSongClick = { song -> playerViewModel?.playRadio(song) }
                                )
                            }
                        }
                        // Show Broadcasts in ALL
                        val broadcasts = viewModel.categories["broadcasts"] ?: emptyList()
                        if (broadcasts.isNotEmpty()) {
                            item {
                                SexyBroadcastSection(
                                    category = "Top Broadcasts",
                                    songs = broadcasts.take(5),
                                    onSongClick = { song -> playerViewModel?.playRadio(song, broadcasts) }
                                )
                            }
                        }
                        item { AdBannerPlaceholder() }
                        // Show standard categories
                        items(viewModel.currentCategories.filter { it.id != "broadcasts" }) { category ->
                            val songs = viewModel.categories[category.id]
                            if (!songs.isNullOrEmpty()) {
                                SongCategoryRow(
                                    category = category.title,
                                    songs = songs,
                                    onSongClick = { song -> 
                                        playerViewModel?.playSong(song, songs)
                                    }
                                )
                            }
                        }
                        item { AdBannerPlaceholder() }
                    } else {
                        items(viewModel.currentCategories) { category ->
                            val songs = viewModel.categories[category.id]
                            if (!songs.isNullOrEmpty()) {
                                if (viewModel.currentFilter == HomeViewModel.Filter.BROADCASTS) {
                                    SexyBroadcastSection(
                                        category = category.title,
                                        songs = songs,
                                        onSongClick = { song -> 
                                            playerViewModel?.playRadio(song, songs)
                                        }
                                    )
                                } else {
                                    SongCategoryRow(
                                        category = category.title,
                                        songs = songs,
                                        onSongClick = { song -> 
                                            playerViewModel?.playSong(song, songs)
                                        }
                                    )
                                }
                            }
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
            modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF111111))
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp) // Increased height from 300 to 400
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111))
    ) {
        // Background Image
        AsyncImage(
            model = currentSong.thumbnail,
            contentDescription = "Concert",
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter, // Crop from top instead of center
            modifier = Modifier.fillMaxSize()
        )
        // Dark Overlay for readability
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
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
            
            // Play Now Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF6070A))
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
        }
    }
}

@Composable
fun ModernSongCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(180.dp) // Make songs image bigger
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
            color = Color(0xFFF6070A), // Red for artist name to match the aesthetic
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun SongCategoryRow(category: String, songs: List<Song>, onSongClick: (Song) -> Unit) {
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
                    onSongClick(song)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SexyBroadcastSection(category: String, songs: List<Song>, onSongClick: (Song) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(
            text = category,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 5
        ) {
            for (song in songs.take(10)) { // Show up to 10 so we have 2 rows of 5
                BroadcastCard(
                    song = song,
                    modifier = Modifier.weight(1f),
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

@Composable
fun BroadcastCard(song: Song, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Make it perfectly square
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = song.thumbnail, // Revert to original thumbnail
            contentDescription = "Cover",
            contentScale = ContentScale.Fit, // Use Fit to not stretch tiny favicons
            modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))
        )
        
        // Dark gradient overlay from bottom up
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp, // Slightly larger text for big cards
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF6070A)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE NOW",
                    color = Color(0xFFF6070A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape) // Fully rounded like playlist section
            .background(if (isSelected) Color(0xFFFF4040) else Color(0xFF1A1A1A)) // Use theme red instead of white
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp), // Slightly larger
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray, // Better contrast
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun CountryCard(country: com.watermelon.music.domain.model.Country, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1A1A24), Color(0xFF101015))
                )
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag image
            AsyncImage(
                model = "https://flagcdn.com/w320/${country.isoCode.lowercase()}.png",
                contentDescription = "${country.name} flag",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = country.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Go",
                tint = Color(0xFFF6070A),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun AdBannerPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF222222)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AdMob Space",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
