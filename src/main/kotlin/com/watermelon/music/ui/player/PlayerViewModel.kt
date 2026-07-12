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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class LyricLine(
    val timeSeconds: Float,
    val text: String
)

class PlayerViewModel {
    private val client = OkHttpClient()
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
    
    private val _currentLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val currentLyrics: StateFlow<List<LyricLine>> = _currentLyrics.asStateFlow()

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
            
            // Fetch lyrics from LRCLIB
            launch(Dispatchers.IO) {
                try {
                    val url = "https://lrclib.net/api/get?track_name=${java.net.URLEncoder.encode(song.title, "UTF-8")}&artist_name=${java.net.URLEncoder.encode(song.artist, "UTF-8")}"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            val syncedLyrics = json.optString("syncedLyrics", "")
                            if (syncedLyrics.isNotBlank()) {
                                val lines = syncedLyrics.lines().mapNotNull { line ->
                                    // Parse format like [00:19.67] We're no strangers to love
                                    val match = Regex("\\[(\\d+):(\\d+\\.\\d+)\\](.*)").find(line)
                                    if (match != null) {
                                        val min = match.groupValues[1].toInt()
                                        val sec = match.groupValues[2].toFloat()
                                        val text = match.groupValues[3].trim()
                                        LyricLine((min * 60) + sec, text)
                                    } else null
                                }
                                _currentLyrics.value = lines
                            } else {
                                val plainLyrics = json.optString("plainLyrics", "")
                                if (plainLyrics.isNotBlank()) {
                                    val durationStr = song.duration ?: "0:0"
                                    val durParts = durationStr.split(":")
                                    val durSecs = if (durParts.size == 2) (durParts[0].toIntOrNull() ?: 0) * 60 + (durParts[1].toIntOrNull() ?: 0) else 180
                                    
                                    val lines = plainLyrics.lines().filter { it.isNotBlank() }
                                    val timePerLine = (durSecs.toFloat() / lines.size).coerceAtLeast(1f)
                                    _currentLyrics.value = lines.mapIndexed { index, text -> 
                                        LyricLine(index * timePerLine, text)
                                    }
                                } else {
                                    _currentLyrics.value = emptyList()
                                }
                            }
                        }
                    } else {
                        _currentLyrics.value = emptyList()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _currentLyrics.value = emptyList()
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
