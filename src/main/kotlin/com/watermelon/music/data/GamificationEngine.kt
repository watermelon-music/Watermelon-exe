package com.watermelon.music.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
data class UserStats(
    val username: String = "Guest User",
    val totalXp: Int = 0,
    val songsPlayed: Int = 0,
    val minutesListened: Int = 0
)

object GamificationEngine {
    private val statsFile = File("watermelon_stats.json")
    
    private val _userStats = MutableStateFlow(loadStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()
    
    fun getLevel(xp: Int): Int {
        return (xp / 1000) + 1
    }
    
    fun getRank(level: Int): String {
        return when (level) {
            in 1..5 -> "Rookie Listener"
            in 6..15 -> "Music Enthusiast"
            in 16..30 -> "Audiophile"
            in 31..50 -> "Melomaniac"
            else -> "Sonic Deity"
        }
    }
    
    fun getXpProgress(xp: Int): Float {
        val xpInCurrentLevel = xp % 1000
        return xpInCurrentLevel / 1000f
    }
    
    fun addSongPlay(durationMinutes: Int) {
        val current = _userStats.value
        val updated = current.copy(
            totalXp = current.totalXp + 50 + (durationMinutes * 10),
            songsPlayed = current.songsPlayed + 1,
            minutesListened = current.minutesListened + durationMinutes
        )
        _userStats.value = updated
        saveStats(updated)
    }

    private fun loadStats(): UserStats {
        return if (statsFile.exists()) {
            try {
                Json.decodeFromString<UserStats>(statsFile.readText())
            } catch (e: Exception) {
                UserStats()
            }
        } else {
            UserStats()
        }
    }

    private fun saveStats(stats: UserStats) {
        try {
            statsFile.writeText(Json.encodeToString(stats))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
