package com.watermelon.music.ui.player

import com.watermelon.music.domain.model.Song
import com.watermelon.music.player.AudioPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
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
    val currentPositionMs = AudioPlayer.currentPositionMs
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
    
    // Type tracking for liking
    private val _currentType = MutableStateFlow("song") // "song", "radio", or "broadcast"
    val currentType: StateFlow<String> = _currentType.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private var currentIndex = -1
    private var shuffledIndices: List<Int> = emptyList()
    
    private var recordPlayJob: kotlinx.coroutines.Job? = null

    init {
        AudioPlayer.onSongEnd = {
            if (_isLooping.value) {
                // Play same song again
                val current = currentSong.value
                if (current != null) {
                    if (current.duration == "LIVE") playRadio(current, _currentQueue.value)
                    else playSong(current, _currentQueue.value)
                }
            } else {
                playNext()
            }
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        _currentType.value = "song"
        if (queue != _currentQueue.value) {
            _currentQueue.value = queue
            if (_isShuffle.value) {
                shuffledIndices = queue.indices.shuffled()
            }
        }
        currentIndex = queue.indexOf(song)
        
        recordPlayJob?.cancel()
        
        scope.launch {
            _isLoading.value = true
            // Record play history in Supabase every 10 seconds of playback
            recordPlayJob = launch {
                var lastDatabaseUpdatePositionMs = 0L
                currentPositionMs.collect { pos ->
                    val elapsed = pos - lastDatabaseUpdatePositionMs
                    if (elapsed >= 10000L) {
                        lastDatabaseUpdatePositionMs = pos
                        try {
                            withContext(Dispatchers.IO) {
                                com.watermelon.music.data.AuthRepository().recordRecentlyPlayed(song, 10000L)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (elapsed < 0L) {
                        // User seeked backwards, reset tracker
                        lastDatabaseUpdatePositionMs = pos
                    }
                }
            }
            
            // Fetch recommendations asynchronously
            launch(Dispatchers.IO) {
                try {
                    val results = repository.search("${song.artist} top songs").filter { it.id != song.id }
                    _recommendedSongs.value = results.take(7)
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

    fun playRadio(station: Song, queue: List<Song> = emptyList(), isBroadcast: Boolean = false) {
        _currentType.value = if (isBroadcast) "broadcast" else "radio"
        if (queue != _currentQueue.value) {
            _currentQueue.value = queue
            if (_isShuffle.value) {
                shuffledIndices = queue.indices.shuffled()
            }
        }
        currentIndex = queue.indexOf(station)
        
        recordPlayJob?.cancel()
        
        scope.launch {
            _isLoading.value = true
            
            // Record play history in Supabase every 10 seconds of playback
            recordPlayJob = launch {
                var lastDatabaseUpdatePositionMs = 0L
                currentPositionMs.collect { pos ->
                    val elapsed = pos - lastDatabaseUpdatePositionMs
                    if (elapsed >= 10000L) {
                        lastDatabaseUpdatePositionMs = pos
                        try {
                            withContext(Dispatchers.IO) {
                                com.watermelon.music.data.AuthRepository().recordRecentlyPlayed(station, elapsed)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (elapsed < 0L) {
                        // User seeked backwards, reset tracker
                        lastDatabaseUpdatePositionMs = pos
                    }
                }
            }
            
            val audioUrl = station.id
            _isLoading.value = false
            AudioPlayer.play(station, audioUrl)
        }
    }

    fun playNext() {
        val queue = _currentQueue.value
        if (queue.isEmpty() || currentIndex == -1) return
        
        val nextIndex = if (_isShuffle.value) {
            val sIdx = shuffledIndices.indexOf(currentIndex)
            if (sIdx != -1 && sIdx < shuffledIndices.size - 1) shuffledIndices[sIdx + 1] else return
        } else {
            if (currentIndex < queue.size - 1) currentIndex + 1 else return
        }
        
        val nextSong = queue[nextIndex]
        if (nextSong.duration == "LIVE") {
            playRadio(nextSong, queue)
        } else {
            playSong(nextSong, queue)
        }
    }

    fun playPrevious() {
        val queue = _currentQueue.value
        if (queue.isEmpty() || currentIndex == -1) return
        
        val prevIndex = if (_isShuffle.value) {
            val sIdx = shuffledIndices.indexOf(currentIndex)
            if (sIdx > 0) shuffledIndices[sIdx - 1] else return
        } else {
            if (currentIndex > 0) currentIndex - 1 else return
        }
        
        val prevSong = queue[prevIndex]
        if (prevSong.duration == "LIVE") {
            playRadio(prevSong, queue)
        } else {
            playSong(prevSong, queue)
        }
    }
    
    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
        if (_isShuffle.value) {
            shuffledIndices = _currentQueue.value.indices.shuffled()
        }
    }
    
    fun toggleLoop() {
        _isLooping.value = !_isLooping.value
    }

    fun setVolume(vol: Float) {
        AudioPlayer.setVolume(vol)
    }

    fun seek(fraction: Float) {
        AudioPlayer.seek(fraction)
    }

    fun toggleLike() {
        val song = currentSong.value
        if (song != null) {
            when (_currentType.value) {
                "radio" -> com.watermelon.music.data.LibraryEngine.toggleLikeRadio(song)
                "broadcast" -> com.watermelon.music.data.LibraryEngine.toggleLikeBroadcast(song)
                else -> com.watermelon.music.data.LibraryEngine.toggleLike(song)
            }
        }
    }

    fun togglePlayPause() {
        AudioPlayer.togglePlayPause()
    }
}
