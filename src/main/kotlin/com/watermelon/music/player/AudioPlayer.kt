package com.watermelon.music.player

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.watermelon.music.domain.model.Song

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    // Initialize JavaFX toolkit without launching an application
    init {
        try {
            com.sun.javafx.application.PlatformImpl.startup {}
        } catch (e: Exception) {
            // Already initialized or fails
        }
    }

    fun play(song: Song, audioUrl: String) {
        mediaPlayer?.stop()
        mediaPlayer?.dispose()

        try {
            val media = Media(audioUrl)
            mediaPlayer = MediaPlayer(media).apply {
                setOnReady {
                    play()
                    _isPlaying.value = true
                }
                setOnEndOfMedia {
                    _isPlaying.value = false
                    _progress.value = 1f
                    
                    val durationMins = totalDuration.toMinutes().toInt().coerceAtLeast(1)
                    com.watermelon.music.data.GamificationEngine.addSongPlay(durationMins)
                }
                currentTimeProperty().addListener { _, _, newValue ->
                    val total = totalDuration.toMillis()
                    if (total > 0) {
                        _progress.value = (newValue.toMillis() / total).toFloat()
                    }
                }
            }
            _currentSong.value = song
        } catch (e: Exception) {
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
}
