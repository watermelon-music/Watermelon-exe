package com.watermelon.music.data

import com.watermelon.music.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val songs: List<Song> = emptyList()
)

@Serializable
data class LibraryData(
    val likedSongs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

object LibraryEngine {
    private val dbFile = File("watermelon_library.json")
    
    private val _library = MutableStateFlow(loadLibrary())
    val library: StateFlow<LibraryData> = _library.asStateFlow()

    fun isSongLiked(songId: String): Boolean {
        return _library.value.likedSongs.any { it.id == songId }
    }

    fun toggleLike(song: Song) {
        val current = _library.value
        val isLiked = current.likedSongs.any { it.id == song.id }
        val updatedLikes = if (isLiked) {
            current.likedSongs.filter { it.id != song.id }
        } else {
            current.likedSongs + song
        }
        updateLibrary(current.copy(likedSongs = updatedLikes))
    }

    fun createPlaylist(name: String) {
        val current = _library.value
        val newPlaylist = Playlist(
            id = java.util.UUID.randomUUID().toString(),
            name = name
        )
        updateLibrary(current.copy(playlists = current.playlists + newPlaylist))
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        val current = _library.value
        val updatedPlaylists = current.playlists.map { playlist ->
            if (playlist.id == playlistId && !playlist.songs.any { it.id == song.id }) {
                playlist.copy(songs = playlist.songs + song)
            } else {
                playlist
            }
        }
        updateLibrary(current.copy(playlists = updatedPlaylists))
    }

    private fun updateLibrary(newData: LibraryData) {
        _library.value = newData
        saveLibrary(newData)
    }

    private fun loadLibrary(): LibraryData {
        return if (dbFile.exists()) {
            try {
                Json.decodeFromString<LibraryData>(dbFile.readText())
            } catch (e: Exception) {
                LibraryData()
            }
        } else {
            LibraryData()
        }
    }

    private fun saveLibrary(data: LibraryData) {
        try {
            dbFile.writeText(Json.encodeToString(data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
