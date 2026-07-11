package com.watermelon.music.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Home : Screen()
    object Search : Screen()
    object Profile : Screen()
    object Library : Screen()
    object Radio : Screen()
}

class NavController(initialScreen: Screen) {
    var currentScreen: Screen by mutableStateOf(initialScreen)
        private set

    private val backStack = mutableListOf<Screen>()

    fun navigate(screen: Screen) {
        if (currentScreen != screen) {
            backStack.add(currentScreen)
            currentScreen = screen
        }
    }

    fun popBackStack(): Boolean {
        return if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeLast()
            true
        } else {
            false
        }
    }
}
