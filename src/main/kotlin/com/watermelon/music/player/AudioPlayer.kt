package com.watermelon.music.player

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.watermelon.music.domain.model.Song

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    
    var onSongEnd: (() -> Unit)? = null
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _volume = MutableStateFlow(1f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // Initialize JavaFX toolkit without launching an application
    init {
        try {
            System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            com.sun.javafx.application.PlatformImpl.startup {}
        } catch (e: Exception) {
            // Already initialized or fails
        }
    }

    fun play(song: Song, audioUrl: String) {
        mediaPlayer?.stop()
        mediaPlayer?.dispose()

        try {
            println("🍉 Playing direct URL: $audioUrl")
            val media = Media(audioUrl)
            media.onError = Runnable {
                println("🍉 Media Error: ${media.error?.message}")
            }
            
            mediaPlayer = MediaPlayer(media).apply {
                volume = _volume.value.toDouble()
                setOnError {
                    println("🍉 MediaPlayer Error: ${error?.message}")
                }
                setOnReady {
                    play()
                    _isPlaying.value = true
                }
                setOnEndOfMedia {
                    _isPlaying.value = false
                    _progress.value = 1f
                    onSongEnd?.invoke()
                }
                currentTimeProperty().addListener { _, _, newValue ->
                    val total = totalDuration.toMillis()
                    if (total > 0) {
                        val currentMs = newValue.toMillis().toLong()
                        _currentPositionMs.value = currentMs
                        _progress.value = (currentMs.toFloat() / total.toFloat())
                    }
                }
            }
            _currentSong.value = song
        } catch (e: Exception) {
            println("🍉 AudioPlayer Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.status == MediaPlayer.Status.PLAYING) {
                player.pause()
                _isPlaying.value = false
            } else {
                player.play()
                _isPlaying.value = true
            }
        }
    }

    fun seek(fraction: Float) {
        mediaPlayer?.let { player ->
            val total = player.totalDuration.toMillis()
            if (total > 0) {
                player.seek(javafx.util.Duration(total * fraction))
            }
        }
    }
    
    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _volume.value = clamped
        mediaPlayer?.volume = clamped.toDouble()
    }
}
