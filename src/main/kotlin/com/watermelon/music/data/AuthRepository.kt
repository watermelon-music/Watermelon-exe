package com.watermelon.music.data

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepository {
    private val client = SupabaseModule.client

    suspend fun signIn(emailParam: String, passwordParam: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.auth.signInWith(Email) {
                email = emailParam
                password = passwordParam
            }
            Unit
        }
    }

    suspend fun signUp(username: String, emailParam: String, passwordParam: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.auth.signUpWith(Email) {
                email = emailParam
                password = passwordParam
                data = buildJsonObject {
                    put("username", username)
                    put("display_name", username)
                }
            }
            Unit
        }
    }

    suspend fun resetPassword(emailParam: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.auth.resetPasswordForEmail(emailParam)
            Unit
        }
    }

    suspend fun resendVerificationEmail(emailParam: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val httpClient = okhttp3.OkHttpClient()
            val json = "{\"type\":\"signup\",\"email\":\"$emailParam\"}"
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val jsonBody = json.toRequestBody(mediaType)
            val request = okhttp3.Request.Builder()
                .url("${client.supabaseUrl}/auth/v1/resend")
                .addHeader("apikey", client.supabaseKey)
                .post(jsonBody)
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException("Resend failed: ${response.code}")
            }
            Unit
        }
    }

    suspend fun isEmailVerified(): Boolean = withContext(Dispatchers.IO) {
        val user = client.auth.currentUserOrNull()
        user?.emailConfirmedAt != null
    }

    suspend fun getCurrentUserEmail(): String? = withContext(Dispatchers.IO) {
        client.auth.currentUserOrNull()?.email
    }

    suspend fun getCurrentUserPhone(): String? = withContext(Dispatchers.IO) {
        client.auth.currentUserOrNull()?.phone
    }

    suspend fun getCurrentUserId(): String? = withContext(Dispatchers.IO) {
        client.auth.currentUserOrNull()?.id
    }

    @Serializable
    data class ProfileRow(
        val id: String? = null,
        val email: String? = null,
        val username: String? = null,
        val display_name: String? = null,
        val bio: String? = null,
        val avatar_url: String? = null,
        val banner_url: String? = null,
        val plan: String? = "FREE",
        val is_banned: Boolean? = false,
        val is_premium: Boolean? = false,
        val is_admin: Boolean? = false,
        val is_verified: Boolean? = false,
        // Gamification fields — all stored directly in profiles table
        val xp_total: Int? = 0,
        val xp_level: Int? = 1,
        val rank_tier: String? = null,
        val creator_level: Int? = 1,
        val points: Int? = 0,
        // Listening stats — stored directly in profiles table
        val songs_played: Int? = 0,
        val songs_completed: Int? = 0,
        val hours_listened: Double? = 0.0,
        val minutes_listened: Double? = 0.0,
        // Social counts
        val follower_count: Int? = 0,
        val following_count: Int? = 0,
        val total_plays: Int? = 0,
        val like_count: Int? = 0,
        // Streak
        val streak_days: Int? = 0,
        val longest_streak: Int? = 0,
        // Discovery
        val artists_discovered: Int? = 0,
        val playlists_created: Int? = 0,
        val liked_songs_count: Int? = 0
    )

    suspend fun fetchProfile(userId: String): ProfileRow? = withContext(Dispatchers.IO) {
        runCatching {
            var profile: ProfileRow? = null
            for (i in 0..5) {
                profile = client.postgrest.from("profiles")
                    .select {
                        filter { eq("id", userId) }
                    }
                    .decodeSingleOrNull<ProfileRow>()
                if (profile != null) break
                kotlinx.coroutines.delay(1000)
            }
            if (profile == null) {
                // If it STILL doesn't exist, try to create it manually from Auth session
                val session = client.auth.currentSessionOrNull()
                if (session != null && session.user?.id == userId) {
                    val user = session.user
                    val email = user?.email ?: ""
                    val username = user?.userMetadata?.get("username")?.let { 
                        if (it is kotlinx.serialization.json.JsonPrimitive) it.content else "User" 
                    } ?: "User"
                    
                    val newProfile = ProfileRow(
                        id = userId,
                        email = email,
                        username = username,
                        display_name = username,
                        plan = "FREE"
                    )
                    try {
                        client.postgrest.from("profiles").upsert(newProfile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // Always return the fallback profile even if upsert fails
                    profile = newProfile
                }
            }
            profile
        }.getOrNull()
    }

    @Serializable
    data class HistoryRowSelect(
        val id: String
    )

    @Serializable
    data class PlaylistRow(
        val id: String,
        val user_id: String,
        val name: String,
        val description: String? = null,
        val cover_url: String? = null,
        val share_code: String? = null,
        val is_public: Boolean = false
    )

    @Serializable
    data class PlaylistSongRow(
        val id: String? = null,
        val playlist_id: String,
        val song_id: String,
        val title: String,
        val artist: String? = null,
        val cover_url: String? = null,
        val audio_url: String? = null,
        val position: Int = 0
    )

    @Serializable
    data class FavoriteRow(
        val user_id: String,
        val song_id: String,
        val title: String,
        val artist: String? = null,
        val cover_url: String? = null,
        val audio_url: String? = null
        // NOTE: No 'duration' field - must match Android's FavoriteRow exactly
    )

    @Serializable
    data class RadioFavoriteRow(
        val user_id: String,
        val station_uuid: String,
        val name: String,
        val url: String? = null,
        val favicon: String? = null,
        val country: String? = null,
        val tags: String? = null
    )

    suspend fun getSongsPlayedCount(userId: String): Int = withContext(Dispatchers.IO) {
        runCatching {
            val list = client.postgrest.from("listening_history")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("id")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<HistoryRowSelect>()
            list.size
        }.getOrDefault(0)
    }

    @Serializable
    data class HistoryRow(
        val user_id: String,
        val song_id: String,
        val title: String,
        val artist: String,
        val cover_url: String? = null,
        val audio_url: String? = null,
        val duration_ms: Long = 0,
        val played_at: String
    )

    suspend fun recordRecentlyPlayed(song: com.watermelon.music.domain.model.Song, durationMs: Long = 10000L): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = getCurrentUserId() ?: return@runCatching
            val row = HistoryRow(
                user_id = uid,
                song_id = song.id,
                title = song.title,
                artist = song.artist,
                cover_url = song.thumbnail,
                audio_url = song.streamUrl,
                duration_ms = durationMs,
                played_at = java.time.Instant.now().toString()
            )
            client.postgrest.from("listening_history").insert(row)
        }
    }

    suspend fun updateProfile(displayNameParam: String, usernameParam: String, phoneParam: String, avatarUrlParam: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Update auth phone
            client.auth.modifyUser {
                phone = phoneParam.takeIf { it.isNotBlank() }
            }
            // Update profiles table
            val uid = client.auth.currentUserOrNull()?.id ?: throw IllegalStateException("Not logged in")
            client.postgrest.from("profiles").update({
                set("display_name", displayNameParam)
                set("username", usernameParam)
                set("avatar_url", avatarUrlParam)
            }) {
                filter { eq("id", uid) }
            }
            Unit
        }
    }

    suspend fun fetchPlaylists(): List<PlaylistRow> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext emptyList()
        runCatching {
            client.postgrest.from("playlists")
                .select { filter { eq("user_id", uid) } }
                .decodeList<PlaylistRow>()
        }.getOrDefault(emptyList())
    }

    suspend fun fetchPlaylistSongs(playlistId: String): List<PlaylistSongRow> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest.from("playlist_songs")
                .select { filter { eq("playlist_id", playlistId) } }
                .decodeList<PlaylistSongRow>()
        }.getOrDefault(emptyList())
    }

    suspend fun fetchFavorites(): List<FavoriteRow> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext emptyList()
        fetchFavoritesForUser(uid)
    }

    suspend fun fetchFavoritesForUser(uid: String): List<FavoriteRow> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest.from("favorites")
                .select { filter { eq("user_id", uid) } }
                .decodeList<FavoriteRow>()
        }.getOrDefault(emptyList())
    }

    suspend fun addFavorite(song: com.watermelon.music.domain.model.Song): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext Result.failure(Exception("Not logged in"))
        runCatching {
            // Use upsert exactly like Android - handles duplicates automatically
            client.postgrest.from("favorites").upsert(
                FavoriteRow(
                    user_id = uid,
                    song_id = song.id,
                    title = song.title,
                    artist = song.artist,
                    cover_url = song.thumbnail,
                    audio_url = song.streamUrl
                )
            )
            Unit
        }.onFailure { e ->
            try {
                java.io.File(System.getProperty("user.home"), ".watermelon_sync_error.log")
                    .appendText("addFavorite ERROR: ${e.message}\n${e.stackTraceToString()}\n\n")
            } catch (ex: Exception) {}
            println("addFavorite ERROR: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun removeFavorite(songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext Result.failure(Exception("Not logged in"))
        removeFavoriteForUser(uid, songId)
    }

    suspend fun removeFavoriteForUser(uid: String, songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest.from("favorites").delete {
                filter {
                    eq("user_id", uid)
                    eq("song_id", songId)
                }
            }
            Unit
        }.onFailure { e ->
            try {
                java.io.File(System.getProperty("user.home"), ".watermelon_sync_error.log").appendText("addFavorite ERROR: ${e.message}\n${e.stackTraceToString()}\n\n")
            } catch (ex: Exception) {}
            println("dY?% removeFavorite ERROR: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun fetchFavoriteRadios(): List<RadioFavoriteRow> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext emptyList()
        fetchFavoriteRadiosForUser(uid)
    }

    suspend fun fetchFavoriteRadiosForUser(uid: String): List<RadioFavoriteRow> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest.from("radio_favorites")
                .select { filter { eq("user_id", uid) } }
                .decodeList<RadioFavoriteRow>()
        }.getOrDefault(emptyList())
    }

    suspend fun addFavoriteRadio(song: com.watermelon.music.domain.model.Song): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext Result.failure(Exception("Not logged in"))
        runCatching {
            // Use upsert exactly like Android - handles duplicates automatically
            client.postgrest.from("radio_favorites").upsert(
                RadioFavoriteRow(
                    user_id = uid,
                    station_uuid = song.id,
                    name = song.title,
                    url = song.streamUrl,
                    favicon = song.thumbnail
                )
            )
            Unit
        }.onFailure { e ->
            try {
                java.io.File(System.getProperty("user.home"), ".watermelon_sync_error.log")
                    .appendText("addFavoriteRadio ERROR: ${e.message}\n${e.stackTraceToString()}\n\n")
            } catch (ex: Exception) {}
            println("addFavoriteRadio ERROR: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun removeFavoriteRadio(stationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext Result.failure(Exception("Not logged in"))
        removeFavoriteRadioForUser(uid, stationId)
    }

    suspend fun removeFavoriteRadioForUser(uid: String, stationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                client.postgrest.from("radio_favorites").delete {
                    filter {
                        eq("user_id", uid)
                        eq("station_uuid", stationId)
                    }
                }
            } catch (e: Exception) {}
            
            try {
                client.postgrest.from("radio_favorites").delete {
                    filter {
                        eq("user_id", uid)
                        eq("url", stationId)
                    }
                }
            } catch (e: Exception) {}
            Unit
        }
    }

    private val broadcastsFile = java.io.File(System.getProperty("user.home"), ".watermelon/broadcast_favorites.json")
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    fun fetchFavoriteBroadcasts(): List<com.watermelon.music.domain.model.Song> {
        return try {
            if (broadcastsFile.exists()) {
                val text = broadcastsFile.readText()
                if (text.isNotBlank()) {
                    json.decodeFromString<List<com.watermelon.music.domain.model.Song>>(text)
                } else emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFavoriteBroadcasts(broadcasts: List<com.watermelon.music.domain.model.Song>) {
        try {
            if (!broadcastsFile.parentFile.exists()) {
                broadcastsFile.parentFile.mkdirs()
            }
            broadcastsFile.writeText(json.encodeToString(kotlinx.serialization.builtins.ListSerializer(com.watermelon.music.domain.model.Song.serializer()), broadcasts))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addSongToPlaylist(playlistId: String, song: com.watermelon.music.domain.model.Song): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val existing = client.postgrest.from("playlist_songs")
                .select { filter { eq("playlist_id", playlistId); eq("song_id", song.id) } }
                .decodeList<PlaylistSongRow>()
            if (existing.isNotEmpty()) return@runCatching Unit

            val count = client.postgrest.from("playlist_songs")
                .select { filter { eq("playlist_id", playlistId) } }
                .decodeList<PlaylistSongRow>().size

            client.postgrest.from("playlist_songs").insert(
                PlaylistSongRow(
                    id = java.util.UUID.randomUUID().toString(),
                    playlist_id = playlistId,
                    song_id = song.id,
                    title = song.title,
                    artist = song.artist,
                    cover_url = song.thumbnail,
                    audio_url = song.streamUrl,
                    position = count
                )
            )
            Unit
        }
    }

    suspend fun createPlaylist(name: String): Result<String> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext Result.failure(Exception("Not logged in"))
        runCatching {
            val newId = java.util.UUID.randomUUID().toString()
            client.postgrest.from("playlists").insert(
                PlaylistRow(
                    id = newId,
                    user_id = uid,
                    name = name
                )
            )
            newId
        }
    }

    suspend fun deletePlaylist(playlistId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext Result.failure(Exception("Not logged in"))
        runCatching {
            // First delete songs in the playlist
            client.postgrest.from("playlist_songs").delete {
                filter { eq("playlist_id", playlistId) }
            }
            // Then delete the playlist itself
            client.postgrest.from("playlists").delete {
                filter {
                    eq("id", playlistId)
                    eq("user_id", uid)
                }
            }
            Unit
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.postgrest.from("playlist_songs").delete {
                filter {
                    eq("playlist_id", playlistId)
                    eq("song_id", songId)
                }
            }
            Unit
        }
    }

    suspend fun getCurrentAccessToken(): String? = withContext(Dispatchers.IO) {
        client.auth.currentSessionOrNull()?.accessToken
    }

    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val token = getCurrentAccessToken() ?: throw IllegalStateException("No active session found")
            val httpClient = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url("https://watermelon-api-oxx2.onrender.com/auth/delete-user")
                .addHeader("Authorization", "Bearer $token")
                .delete()
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val bodyString = response.body?.string() ?: ""
                throw IllegalStateException("Delete failed: ${response.code} - $bodyString")
            }
            response.body?.close()
            
            // Sign out locally to clear session cache
            try {
                client.auth.signOut()
            } catch (e: Exception) {
                client.auth.clearSession()
            }
            Unit
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                client.auth.signOut()
            } catch (e: Exception) {
                client.auth.clearSession()
            }
            Unit
        }
    }

    fun isAuthenticated(): Flow<Boolean> {
        return client.auth.sessionStatus.map { status ->
            status is SessionStatus.Authenticated
        }
    }

    suspend fun checkSession(): Boolean {
        // Very basic session check for now
        return client.auth.currentSessionOrNull() != null
    }
}
