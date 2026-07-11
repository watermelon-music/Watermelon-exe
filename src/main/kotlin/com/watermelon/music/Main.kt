package com.watermelon.music

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.watermelon.music.navigation.NavController
import com.watermelon.music.navigation.Screen
import com.watermelon.music.ui.auth.LoginScreen
import com.watermelon.music.ui.home.HomeScreen
import com.watermelon.music.ui.player.MiniPlayer
import com.watermelon.music.ui.player.PlayerViewModel
import com.watermelon.music.ui.profile.ProfileScreen
import com.watermelon.music.ui.library.LibraryScreen
import com.watermelon.music.ui.radio.RadioScreen
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory

@Composable
@Preview
fun App() {
    val navController = remember { NavController(Screen.Home) }
    val playerViewModel = remember { PlayerViewModel() }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF080808))) {
            Box(modifier = Modifier.weight(1f)) {
                when (navController.currentScreen) {
                    is Screen.Splash -> {
                        // Splash UI
                    }
                    is Screen.Login -> {
                        LoginScreen(navController)
                    }
                    is Screen.Home -> {
                        HomeScreen(navController, playerViewModel)
                    }
                    is Screen.Profile -> {
                        ProfileScreen()
                    }
                    is Screen.Library -> {
                        LibraryScreen(playerViewModel)
                    }
                    is Screen.Radio -> {
                        RadioScreen(playerViewModel)
                    }
                    else -> {}
                }
            }
            
            // Bottom Mini Player
            MiniPlayer(playerViewModel)
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

    Window(
        onCloseRequest = ::exitApplication,
        title = "Watermelon",
    ) {
        App()
    }
}
