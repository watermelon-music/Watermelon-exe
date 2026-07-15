package com.watermelon.music.data

import com.watermelon.music.domain.model.Song
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val songs: List<Song> = emptyList()
)

@Serializable
data class LibraryData(
    val likedSongs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val likedRadios: List<Song> = emptyList(),
    val likedBroadcasts: List<Song> = emptyList()
)

object LibraryEngine {
    private val dbFile = File("watermelon_library.json")
    private val authRepo = AuthRepository()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _library = MutableStateFlow(loadLibrary())
    val library: StateFlow<LibraryData> = _library.asStateFlow()

    init {
        // Observe session status reactively — sync the instant session becomes authenticated
        scope.launch {
            SupabaseModule.client.auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    // Small delay to ensure currentUserOrNull() is populated after auth event
                    kotlinx.coroutines.delay(300)
                    syncWithCloud()
                }
            }
        }

        // Background polling every 15 seconds to keep PC and phone in sync in real-time
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(15000)
                syncWithCloud()
            }
        }
    }

    // Internal suspend version — reads user ID once and passes it through everything
    private suspend fun doSync() {
        // Read user ID directly from the client — no extra withContext round-trip
        val uid = SupabaseModule.client.auth.currentUserOrNull()?.id
        if (uid == null) {
            logDebug("doSync: no user logged in, skipping")
            return
        }
        logDebug("doSync: starting for uid=$uid")

        val remoteFavorites  = authRepo.fetchFavoritesForUser(uid)
        val remotePlaylists  = authRepo.fetchPlaylists()
        val remoteRadios     = authRepo.fetchFavoriteRadiosForUser(uid)
        val localBroadcasts  = authRepo.fetchFavoriteBroadcasts()

        val mappedPlaylists = remotePlaylists.map { pr ->
            val songs = authRepo.fetchPlaylistSongs(pr.id)
            Playlist(
                id = pr.id,
                name = pr.name,
                songs = songs.sortedBy { it.position }.map { sr ->
                    Song(
                        id = sr.song_id,
                        title = sr.title,
                        artist = sr.artist ?: "",
                        thumbnail = sr.cover_url ?: "",
                        duration = "0:00",
                        streamUrl = sr.audio_url
                    )
                }
            )
        }

        logDebug("doSync: fetched ${remoteFavorites.size} songs, ${remoteRadios.size} radios")

        updateLibrary(_library.value.copy(
            likedSongs = remoteFavorites.map { fr ->
                Song(
                    id = fr.song_id,
                    title = fr.title,
                    artist = fr.artist ?: "",
                    thumbnail = fr.cover_url ?: "",
                    duration = "0:00",
                    streamUrl = fr.audio_url
                )
            },
            likedRadios = remoteRadios.map { rr ->
                Song(
                    id = rr.station_uuid,
                    title = rr.name ?: "",
                    artist = rr.country ?: "",
                    thumbnail = rr.favicon ?: "",
                    duration = "LIVE",
                    streamUrl = rr.url
                )
            },
            likedBroadcasts = localBroadcasts,
            playlists = mappedPlaylists
        ))
    }

    // Public entry point — launches doSync in the IO scope
    fun syncWithCloud() {
        scope.launch { doSync() }
    }

    fun isSongLiked(songId: String): Boolean {
        val current = _library.value
        return current.likedSongs.any { it.id == songId } ||
               current.likedRadios.any { it.id == songId } ||
               current.likedBroadcasts.any { it.id == songId }
    }

    fun toggleLike(song: Song) {
        val current = _library.value
        val isLiked = current.likedSongs.any {
            it.id == song.id || (it.streamUrl == song.streamUrl && !it.streamUrl.isNullOrEmpty())
        }
        val updatedLikes = if (isLiked) {
            current.likedSongs.filterNot {
                it.id == song.id || (it.streamUrl == song.streamUrl && !it.streamUrl.isNullOrEmpty())
            }
        } else {
            current.likedSongs + song
        }
        updateLibrary(current.copy(likedSongs = updatedLikes))

        // Capture user ID RIGHT NOW at call-time (user is definitely logged in here)
        val uid = SupabaseModule.client.auth.currentUserOrNull()?.id
        scope.launch {
            if (uid == null) {
                logDebug("toggleLike: uid null, cannot push to cloud")
                return@launch
            }
            if (isLiked) {
                logDebug("toggleLike: removing song ${song.id} for uid=$uid")
                authRepo.removeFavoriteForUser(uid, song.id)
            } else {
                logDebug("toggleLike: adding song ${song.id} for uid=$uid")
                authRepo.addFavorite(song)
            }
        }
    }

    fun toggleLikeRadio(station: Song) {
        val current = _library.value
        val isLiked = current.likedRadios.any {
            it.id == station.id || (it.streamUrl == station.streamUrl && !it.streamUrl.isNullOrEmpty())
        }
        val updatedLikes = if (isLiked) {
            current.likedRadios.filterNot {
                it.id == station.id || (it.streamUrl == station.streamUrl && !it.streamUrl.isNullOrEmpty())
            }
        } else {
            current.likedRadios + station
        }
        updateLibrary(current.copy(likedRadios = updatedLikes))

        // Capture user ID RIGHT NOW at call-time (user is definitely logged in here)
        val uid = SupabaseModule.client.auth.currentUserOrNull()?.id
        scope.launch {
            if (uid == null) {
                logDebug("toggleLikeRadio: uid null, cannot push to cloud")
                return@launch
            }
            if (isLiked) {
                logDebug("toggleLikeRadio: removing station ${station.id} for uid=$uid")
                authRepo.removeFavoriteRadioForUser(uid, station.id)
            } else {
                logDebug("toggleLikeRadio: adding station ${station.id} for uid=$uid")
                authRepo.addFavoriteRadio(station)
            }
        }
    }

    fun toggleLikeBroadcast(station: Song) {
        val current = _library.value
        val isLiked = current.likedBroadcasts.any { it.id == station.id || (it.streamUrl == station.streamUrl && !it.streamUrl.isNullOrEmpty()) }
        val updatedLikes = if (isLiked) {
            current.likedBroadcasts.filterNot { it.id == station.id || (it.streamUrl == station.streamUrl && !it.streamUrl.isNullOrEmpty()) }
        } else {
            current.likedBroadcasts + station
        }
        updateLibrary(current.copy(likedBroadcasts = updatedLikes))
        authRepo.saveFavoriteBroadcasts(updatedLikes)
    }

    fun createPlaylist(name: String) {
        val current = _library.value
        val newId = java.util.UUID.randomUUID().toString()
        val newPlaylist = Playlist(
            id = newId,
            name = name
        )
        updateLibrary(current.copy(playlists = current.playlists + newPlaylist))
        scope.launch { authRepo.createPlaylist(name) }
        syncWithCloud()
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        val current = _library.value
        val updatedPlaylists = current.playlists.map { playlist ->
            if (playlist.id == playlistId && !playlist.songs.any { it.id == song.id }) {
                scope.launch { authRepo.addSongToPlaylist(playlistId, song) }
                playlist.copy(songs = playlist.songs + song)
            } else {
                playlist
            }
        }
        updateLibrary(current.copy(playlists = updatedPlaylists))
    }

    fun deletePlaylist(playlistId: String) {
        val current = _library.value
        val updatedPlaylists = current.playlists.filter { it.id != playlistId }
        updateLibrary(current.copy(playlists = updatedPlaylists))
        scope.launch { authRepo.deletePlaylist(playlistId) }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val current = _library.value
        val updatedPlaylists = current.playlists.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(songs = playlist.songs.filter { it.id != songId })
            } else {
                playlist
            }
        }
        updateLibrary(current.copy(playlists = updatedPlaylists))
        scope.launch { authRepo.removeSongFromPlaylist(playlistId, songId) }
    }

    private fun updateLibrary(newData: LibraryData) {
        _library.value = newData
        saveLibrary(newData)
    }

    private fun logDebug(msg: String) {
        try {
            java.io.File(System.getProperty("user.home"), ".watermelon_debug.log")
                .appendText("[${java.time.Instant.now()}] $msg\n")
        } catch (_: Exception) {}
    }

    private fun loadLibrary(): LibraryData {
        return if (dbFile.exists()) {
            try {
                val format = Json { ignoreUnknownKeys = true; encodeDefaults = true }
                format.decodeFromString<LibraryData>(dbFile.readText())
            } catch (e: Exception) {
                LibraryData()
            }
        } else {
            LibraryData()
        }
    }

    private fun saveLibrary(data: LibraryData) {
        try {
            val format = Json { ignoreUnknownKeys = true; encodeDefaults = true }
            dbFile.writeText(format.encodeToString(data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
