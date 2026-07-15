package com.watermelon.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.focus.onFocusChanged
import coil3.compose.AsyncImage

import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import com.watermelon.music.data.AuthRepository

@Composable
fun WindowScope.CustomTitleBar(
    state: WindowState,
    onClose: () -> Unit,
    navController: NavController,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    val isAuthScreen = navController.currentScreen in listOf(
        Screen.Splash, Screen.Login, Screen.Register, Screen.ForgotPassword, Screen.EmailVerification
    )

    val authRepository = remember { AuthRepository() }
    var displayName by remember { mutableStateOf("Guest") }
    var avatarUrl by remember { mutableStateOf("https://api.dicebear.com/9.x/thumbs/png?seed=Guest&size=150") }

    LaunchedEffect(isAuthScreen) {
        if (!isAuthScreen) {
            try {
                val email = authRepository.getCurrentUserEmail()
                val uid = authRepository.getCurrentUserId()
                if (uid != null) {
                    val p = authRepository.fetchProfile(uid)
                    val baseUsername = p?.username?.takeIf { it.isNotBlank() } ?: email?.substringBefore("@") ?: "Guest"
                    displayName = p?.display_name?.takeIf { it.isNotBlank() } ?: baseUsername.replaceFirstChar { it.uppercase() }
                    avatarUrl = p?.avatar_url?.takeIf { it.isNotBlank() } ?: "https://api.dicebear.com/9.x/thumbs/png?seed=$baseUsername&size=150"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    WindowDraggableArea {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF000000)) // Pitch Black
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isAuthScreen) {
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
                    Box(
                        modifier = Modifier
                            .width(420.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1E1E1E)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && navController.currentScreen !is Screen.Search) {
                                        navController.navigate(Screen.Search)
                                    }
                                },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF4040)),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchQuery.isEmpty()) {
                                            Text("What do you want to play?", color = Color.Gray, fontSize = 15.sp)
                                        }
                                        innerTextField()
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.Gray,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { onSearchQueryChange("") }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // End Controls: Profile + Window Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isAuthScreen) {
                    // Profile
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { navController.navigate(Screen.Profile) }
                    ) {
                        Text(displayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1E1E))
                        ) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                }

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
