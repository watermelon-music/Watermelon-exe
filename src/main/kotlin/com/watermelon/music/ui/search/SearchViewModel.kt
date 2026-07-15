package com.watermelon.music.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.watermelon.music.data.remote.RadioBrowserApi
import com.watermelon.music.data.repository.MusicCatalogRepository
import com.watermelon.music.domain.model.Song
import kotlinx.coroutines.*

class SearchViewModel {
    private val musicRepository = MusicCatalogRepository()
    private var searchJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var isLoading by mutableStateOf(false)
        private set
    var musicResults by mutableStateOf<List<Song>>(emptyList())
        private set
    var radioResults by mutableStateOf<List<Song>>(emptyList())
        private set

    fun search(query: String) {
        if (query.isBlank()) {
            musicResults = emptyList()
            radioResults = emptyList()
            isLoading = false
            return
        }

        searchJob?.cancel()
        searchJob = scope.launch {
            isLoading = true
            delay(500) // Debounce typing

            // Search concurrently
            val musicDeferred = async {
                try {
                    musicRepository.search(query)
                } catch (e: Exception) {
                    emptyList()
                }
            }
            val radioDeferred = async {
                try {
                    RadioBrowserApi.getStationsByTag(query, limit = 15)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            musicResults = musicDeferred.await()
            radioResults = radioDeferred.await()
            isLoading = false
        }
    }
}
