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
import com.watermelon.music.data.repository.MusicCatalogRepository

class PlayerViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    val currentSong = AudioPlayer.currentSong
    val isPlaying = AudioPlayer.isPlaying
    val progress = AudioPlayer.progress
    val volume = AudioPlayer.volume
    
    val library = com.watermelon.music.data.LibraryEngine.library

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val repository = MusicCatalogRepository()
    
    private val _recommendedSongs = MutableStateFlow<List<Song>>(emptyList())
    val recommendedSongs: StateFlow<List<Song>> = _recommendedSongs.asStateFlow()

    // Queue logic
    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()
    
    private var currentIndex = -1

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        _currentQueue.value = queue
        currentIndex = queue.indexOf(song)
        
        scope.launch {
            _isLoading.value = true
            
            // Fetch recommendations asynchronously
            launch(Dispatchers.IO) {
                try {
                    _recommendedSongs.value = repository.search("similar to ${song.artist} ${song.title}").take(7)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
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

    fun playRadio(station: Song, queue: List<Song> = emptyList()) {
        _currentQueue.value = queue
        currentIndex = queue.indexOf(station)
        
        scope.launch {
            _isLoading.value = true
            val audioUrl = station.id
            _isLoading.value = false
            AudioPlayer.play(station, audioUrl)
        }
    }

    fun playNext() {
        if (_currentQueue.value.isEmpty() || currentIndex == -1) return
        
        if (currentIndex < _currentQueue.value.size - 1) {
            currentIndex++
            val nextSong = _currentQueue.value[currentIndex]
            if (nextSong.duration == "LIVE") {
                playRadio(nextSong, _currentQueue.value)
            } else {
                playSong(nextSong, _currentQueue.value)
            }
        }
    }

    fun playPrevious() {
        if (_currentQueue.value.isEmpty() || currentIndex == -1) return
        
        if (currentIndex > 0) {
            currentIndex--
            val prevSong = _currentQueue.value[currentIndex]
            if (prevSong.duration == "LIVE") {
                playRadio(prevSong, _currentQueue.value)
            } else {
                playSong(prevSong, _currentQueue.value)
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
    
    fun setVolume(vol: Float) {
        AudioPlayer.setVolume(vol)
    }
}
