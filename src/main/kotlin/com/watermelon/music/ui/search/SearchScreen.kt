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

@Composable
fun SearchScreen(playerViewModel: PlayerViewModel?) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Musics", "Radios", "Broadcasts")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(24.dp)
    ) {
        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp)),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color(0xFF1E1E1E),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                textColor = Color.White,
                cursorColor = Color(0xFFFF4040)
            ),
            placeholder = {
                Text("What do you want to listen to?", color = Color.Gray, fontSize = 16.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(24.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

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
            // Browse All Sections
            Text(
                text = "Browse All",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(HOME_CATEGORIES) { category ->
                    BrowseCategoryCard(category)
                }
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
            
            // Dummy content for now, ideally hooks into a SearchViewModel
            Text("Search functionality coming soon. Try browsing categories!", color = Color.Gray)
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
