package com.watermelon.music.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen

@Composable
fun Sidebar(navController: NavController) {
    val currentScreen = navController.currentScreen

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(220.dp)
            .background(Color(0xFF0F0F0F))
            .padding(vertical = 24.dp)
    ) {
        // Logo
        Box(
            modifier = Modifier
                .padding(start = 24.dp, bottom = 48.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource("watermelon_icon.png"), // Assuming this exists in resources
                contentDescription = "Logo",
                modifier = Modifier.size(24.dp)
            )
        }

        // Navigation Items
        SidebarItem(
            icon = Icons.Default.Home,
            title = "Home",
            isSelected = currentScreen == Screen.Home,
            onClick = { navController.navigate(Screen.Home) }
        )
        SidebarItem(
            icon = Icons.Default.Search,
            title = "Search",
            isSelected = currentScreen == Screen.Search,
            onClick = { navController.navigate(Screen.Search) }
        )
        SidebarItem(
            icon = Icons.Default.Radio,
            title = "Radio",
            isSelected = currentScreen == Screen.Radio,
            onClick = { navController.navigate(Screen.Radio) }
        )
        SidebarItem(
            icon = Icons.Default.LibraryMusic,
            title = "Playlist",
            isSelected = currentScreen == Screen.Library,
            onClick = { navController.navigate(Screen.Library) }
        )
    }
}

@Composable
fun SidebarItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF2A1515) // Soft dark red gradient approximation
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) Color(0xFFFF4040) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFFF4040))
            )
        }
    }
}
