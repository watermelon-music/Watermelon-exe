package com.watermelon.music

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowPlacement
import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen
import com.watermelon.music.ui.auth.*
import com.watermelon.music.ui.components.Sidebar
import com.watermelon.music.ui.components.CustomTitleBar
import com.watermelon.music.ui.home.HomeScreen
import com.watermelon.music.ui.player.BottomPlayer
import com.watermelon.music.ui.player.PlayerViewModel
import com.watermelon.music.ui.premium.PremiumScreen
import com.watermelon.music.ui.profile.ProfileScreen
import com.watermelon.music.ui.library.LibraryScreen
import com.watermelon.music.ui.radio.RadioScreen
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory

import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource

@Composable
@Preview
fun App(
    playerViewModel: PlayerViewModel, 
    authViewModel: AuthViewModel,
    navController: NavController, 
    searchQuery: String = "",
    isRightPanelVisible: Boolean, 
    onToggleRightPanel: () -> Unit
) {
    var isFullScreenMode by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
            // Main content area with Sidebar
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isFullScreenMode) {
                    com.watermelon.music.ui.player.FullScreenPlayerScreen(playerViewModel)
                } else {
                    val isAuthScreen = navController.currentScreen is Screen.Splash || 
                                       navController.currentScreen is Screen.Login ||
                                       navController.currentScreen is Screen.Register ||
                                       navController.currentScreen is Screen.ForgotPassword ||
                                       navController.currentScreen is Screen.EmailVerification

                    // Only show Sidebar if not on an auth screen
                    if (!isAuthScreen) {
                        Sidebar(navController)
                    }

                    // Dynamic Screen Content
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (navController.currentScreen) {
                        is Screen.Splash -> {
                            // Splash UI
                        }
                        is Screen.Login -> {
                            LoginScreen(
                                onNavigateToRegister = { navController.navigate(Screen.Register) },
                                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword) },
                                onAuthSuccess = { navController.navigate(Screen.Home) },
                                viewModel = authViewModel
                            )
                        }
                        is Screen.Register -> {
                            RegisterScreen(
                                onNavigateToLogin = { navController.navigate(Screen.Login) },
                                onAuthSuccess = { navController.navigate(Screen.EmailVerification) },
                                viewModel = authViewModel
                            )
                        }
                        is Screen.ForgotPassword -> {
                            ForgotPasswordScreen(
                                onNavigateToLogin = { navController.navigate(Screen.Login) },
                                viewModel = authViewModel
                            )
                        }
                        is Screen.EmailVerification -> {
                            EmailVerificationScreen(
                                onVerified = { navController.navigate(Screen.Home) },
                                onBackToLogin = { navController.navigate(Screen.Login) },
                                viewModel = authViewModel
                            )
                        }
                        is Screen.Home -> {
                            HomeScreen(playerViewModel)
                        }
                        is Screen.Search -> {
                            com.watermelon.music.ui.search.SearchScreen(playerViewModel, searchQuery)
                        }
                        is Screen.Profile -> {
                            ProfileScreen(navController)
                        }
                        is Screen.Library -> {
                            LibraryScreen(playerViewModel)
                        }
                        is Screen.Radio -> {
                            RadioScreen(playerViewModel)
                        }
                        is Screen.Premium -> {
                            PremiumScreen(navController)
                        }
                        else -> {}
                    }
                    }
                } // Closes else
                
                // Right Panel (Context Panel)
                val isAuthScreenForRightPanel = navController.currentScreen is Screen.Splash || 
                                                navController.currentScreen is Screen.Login ||
                                                navController.currentScreen is Screen.Register ||
                                                navController.currentScreen is Screen.ForgotPassword ||
                                                navController.currentScreen is Screen.EmailVerification
                if (!isFullScreenMode && isRightPanelVisible && !isAuthScreenForRightPanel) {
                    com.watermelon.music.ui.components.RightPanel(
                        playerViewModel = playerViewModel, 
                        onClose = onToggleRightPanel,
                        onLyricsClick = { isFullScreenMode = true }
                    )
                }
            } // Closes Row

            // Bottom Player (always visible if not in Splash/Login/Auth)
            val isAuthScreenForPlayer = navController.currentScreen is Screen.Splash || 
                                        navController.currentScreen is Screen.Login ||
                                        navController.currentScreen is Screen.Register ||
                                        navController.currentScreen is Screen.ForgotPassword ||
                                        navController.currentScreen is Screen.EmailVerification
            if (!isAuthScreenForPlayer) {
                BottomPlayer(playerViewModel, onFullScreenToggle = { isFullScreenMode = !isFullScreenMode })
            }
        }
    }
}

fun main() = application {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }
    
    val playerViewModel = remember { PlayerViewModel() }
    val authViewModel = remember { AuthViewModel() }
    val windowState = androidx.compose.ui.window.rememberWindowState(placement = WindowPlacement.Maximized)

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Watermelon",
        icon = painterResource("watermelon_icon.png"),
        undecorated = true,
        onPreviewKeyEvent = { event ->
            if (event.type == KeyEventType.KeyDown) {
                when (event.key) {
                    Key.Spacebar -> {
                        playerViewModel.togglePlayPause()
                        true
                    }
                    Key.DirectionUp -> {
                        playerViewModel.setVolume(playerViewModel.volume.value + 0.1f)
                        true
                    }
                    Key.DirectionDown -> {
                        playerViewModel.setVolume(playerViewModel.volume.value - 0.1f)
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    ) {
        var isRightPanelVisible by remember { mutableStateOf(false) } // Default to false until a song is played
        val navController = remember { NavController(Screen.Login) }
        var searchQuery by remember { mutableStateOf("") }
        
        val authRepository = remember { com.watermelon.music.data.AuthRepository() }
        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (authRepository.checkSession()) {
                navController.navigate(Screen.Home)
            } else {
                navController.navigate(Screen.Login)
            }
        }
        
        // Observe current song and show right panel when it changes
        val currentSong by playerViewModel.currentSong.collectAsState()
        androidx.compose.runtime.LaunchedEffect(currentSong) {
            if (currentSong != null) {
                isRightPanelVisible = true
            }
        }
        
        Column(modifier = Modifier.fillMaxSize()) {
            CustomTitleBar(
                state = windowState,
                onClose = ::exitApplication,
                navController = navController,
                searchQuery = searchQuery,
                onSearchQueryChange = { 
                    searchQuery = it
                    if (it.isNotEmpty() && navController.currentScreen !is Screen.Search) {
                        navController.navigate(Screen.Search)
                    }
                }
            )
            App(
                playerViewModel = playerViewModel,
                authViewModel = authViewModel,
                navController = navController,
                searchQuery = searchQuery,
                isRightPanelVisible = isRightPanelVisible,
                onToggleRightPanel = { isRightPanelVisible = false } // Only close it now
            )
        }
    }
}
