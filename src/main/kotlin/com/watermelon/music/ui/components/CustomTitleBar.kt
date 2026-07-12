package com.watermelon.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import coil3.compose.AsyncImage

import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen

@Composable
fun WindowScope.CustomTitleBar(
    state: WindowState,
    onClose: () -> Unit,
    navController: NavController
) {
    WindowDraggableArea {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF000000)) // Pitch Black
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation Arrows (Start)
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp), // Space between < and >
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp).clickable { navController.popBackStack() }
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Forward",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp).clickable { navController.goForward() }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Center Controls: Home and Search Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Home Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E1E))
                        .clickable { navController.navigate(Screen.Home) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Search Bar
                Row(
                    modifier = Modifier
                        .width(420.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 16.dp)
                        .clickable { navController.navigate(Screen.Search) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "What do you want to play?",
                        color = Color.Gray,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // End Controls: Window Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Minimize
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Minimize",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp).clickable { 
                        state.isMinimized = true 
                    }
                )
                
                // Maximize
                Icon(
                    imageVector = Icons.Default.CropSquare,
                    contentDescription = "Maximize",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp).clickable {
                        if (state.placement == WindowPlacement.Maximized) {
                            state.placement = WindowPlacement.Floating
                        } else {
                            state.placement = WindowPlacement.Maximized
                        }
                    }
                )
                
                // Close
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp).clickable { onClose() }
                )
            }
        }
    }
}
