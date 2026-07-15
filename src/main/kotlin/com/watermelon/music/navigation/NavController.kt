package com.watermelon.music.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Register : Screen()
    object ForgotPassword : Screen()
    object EmailVerification : Screen()
    object Home : Screen()
    object Search : Screen()
    object Profile : Screen()
    object Library : Screen()
    object Radio : Screen()
    object Premium : Screen()
}

class NavController(initialScreen: Screen) {
    var currentScreen: Screen by mutableStateOf(initialScreen)
        private set

    private val backStack = mutableListOf<Screen>()
    private val forwardStack = mutableListOf<Screen>()

    fun navigate(screen: Screen) {
        if (currentScreen != screen) {
            backStack.add(currentScreen)
            forwardStack.clear()
            currentScreen = screen
        }
    }

    fun popBackStack(): Boolean {
        return if (backStack.isNotEmpty()) {
            forwardStack.add(currentScreen)
            currentScreen = backStack.removeLast()
            true
        } else {
            false
        }
    }
    
    fun goForward(): Boolean {
        return if (forwardStack.isNotEmpty()) {
            backStack.add(currentScreen)
            currentScreen = forwardStack.removeLast()
            true
        } else {
            false
        }
    }
}
