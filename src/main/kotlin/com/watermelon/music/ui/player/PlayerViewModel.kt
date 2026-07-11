package com.watermelon.music.ui.player

import com.watermelon.music.domain.model.Song
import com.watermelon.music.player.AudioPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    val currentSong = AudioPlayer.currentSong
    val isPlaying = AudioPlayer.isPlaying
    val progress = AudioPlayer.progress
    
    val library = com.watermelon.music.data.LibraryEngine.library

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun playSong(song: Song) {
        scope.launch {
            _isLoading.value = true
            
            val audioUrl = try {
                com.watermelon.music.data.remote.youtube.LocalAudioExtractor.extractAudioUrl(song.id)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            _isLoading.value = false
            
            if (audioUrl != null) {
                AudioPlayer.play(song, audioUrl)
            }
        }
    }

    fun toggleLike() {
        val song = currentSong.value
        if (song != null) {
            com.watermelon.music.data.LibraryEngine.toggleLike(song)
        }
    }

    fun togglePlayPause() {
        AudioPlayer.togglePlayPause()
    }
}
