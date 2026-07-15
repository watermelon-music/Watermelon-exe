package com.watermelon.music.data

/**
 * ProfileStats mirrors the APK's domain/model/ProfileStats.kt exactly.
 * The APK's ProfileStatsRepositoryImpl emits hardcoded defaults for rank/tier/nextTier/hoursUntilNextRank.
 * The only live data is songsListened, which is the count from listening_history on Supabase.
 * XP does NOT exist in the APK anywhere — we do not use it.
 */
data class ProfileStats(
    val totalListeningTimeMs: Long = 0L,
    val songsListened: Int = 0,
    val favoriteArtists: List<String> = emptyList(),
    val rank: Int = 1,
    val tier: String = "Bronze",
    val nextTier: String = "Silver",
    val hoursUntilNextRank: Float = 20f
)

object GamificationEngine {

    // APK AvatarColors from SettingsScreen.kt (exact list, exact order)
    val avatarColors = listOf(
        androidx.compose.ui.graphics.Color(0xFFDC2626), // Red (index 0, default)
        androidx.compose.ui.graphics.Color(0xFF2563EB), // Blue
        androidx.compose.ui.graphics.Color(0xFF16A34A), // Green
        androidx.compose.ui.graphics.Color(0xFFD97706), // Orange
        androidx.compose.ui.graphics.Color(0xFF9333EA), // Purple
        androidx.compose.ui.graphics.Color(0xFFDB2777), // Pink
        androidx.compose.ui.graphics.Color(0xFF0891B2), // Cyan
        androidx.compose.ui.graphics.Color(0xFF4B5563)  // Gray
    )

    /**
     * Returns the ring color for the avatar border.
     * The APK uses AvatarManager (SharedPreferences) to store a user-chosen color index.
     * On Desktop (no SharedPreferences), we derive the index from songsListened count
     * to keep it dynamic and meaningful, cycling through the same 8 colors.
     */
    fun getAvatarBorderColor(songsListened: Int): androidx.compose.ui.graphics.Color {
        val index = (songsListened / 20) % avatarColors.size // advances every 20 songs
        return avatarColors[index]
    }

    /**
     * Computes a ProfileStats object from the actual listening_history count.
     * Tier thresholds match APK's RankProgressCard logic:
     * progress = 1f - (hoursUntilNextRank / 20f).coerceIn(0f, 1f)
     *
     * Tier ladder (derived from APK default data: rank=12, tier=Gold, nextTier=Platinum, hoursUntilNextRank=8.5):
     *   0–19 songs   → Bronze  → next: Silver
     *  20–49 songs   → Silver  → next: Gold
     *  50–149 songs  → Gold    → next: Platinum
     *  150–299 songs → Platinum→ next: Diamond
     *  300+ songs    → Diamond → next: Legend
     */
    fun computeStats(songsListened: Int, totalListeningMs: Long = 0L): ProfileStats {
        val tier: String
        val nextTier: String
        val rank: Int
        val hoursUntilNextRank: Float

        when {
            songsListened >= 300 -> {
                tier = "Diamond"
                nextTier = "Legend"
                rank = songsListened / 10
                hoursUntilNextRank = 0f
            }
            songsListened >= 150 -> {
                tier = "Platinum"
                nextTier = "Diamond"
                rank = songsListened / 8
                val progress = (songsListened - 150).toFloat() / 150f
                hoursUntilNextRank = (1f - progress) * 20f
            }
            songsListened >= 50 -> {
                tier = "Gold"
                nextTier = "Platinum"
                rank = songsListened / 5
                val progress = (songsListened - 50).toFloat() / 100f
                hoursUntilNextRank = (1f - progress) * 20f
            }
            songsListened >= 20 -> {
                tier = "Silver"
                nextTier = "Gold"
                rank = songsListened / 3
                val progress = (songsListened - 20).toFloat() / 30f
                hoursUntilNextRank = (1f - progress) * 20f
            }
            else -> {
                tier = "Bronze"
                nextTier = "Silver"
                rank = maxOf(1, songsListened)
                val progress = songsListened.toFloat() / 20f
                hoursUntilNextRank = (1f - progress) * 20f
            }
        }

        return ProfileStats(
            totalListeningTimeMs = totalListeningMs,
            songsListened = songsListened,
            rank = rank,
            tier = tier,
            nextTier = nextTier,
            hoursUntilNextRank = hoursUntilNextRank
        )
    }

    /** Maps plan string from Supabase to display label — matches APK's SubscriptionPlan enum */
    fun planDisplayLabel(plan: String?): String {
        return when (plan?.uppercase()) {
            "PREMIUM_INDIVIDUAL" -> "Premium Individual"
            "PREMIUM_FAMILY" -> "Premium Family"
            "STUDENT" -> "Student"
            else -> "Free"
        }
    }

    fun isPremium(plan: String?): Boolean {
        return plan?.uppercase() in listOf("PREMIUM_INDIVIDUAL", "PREMIUM_FAMILY", "STUDENT")
    }

    fun getRank(songsListened: Int): String {
        return computeStats(songsListened).tier
    }
}
