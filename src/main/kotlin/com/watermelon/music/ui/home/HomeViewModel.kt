package com.watermelon.music.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.watermelon.music.data.repository.MusicCatalogRepository
import com.watermelon.music.domain.model.HOME_CATEGORIES
import com.watermelon.music.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: MusicCatalogRepository = MusicCatalogRepository(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    var isLoading by mutableStateOf(true)
        private set
    var isError by mutableStateOf(false)
        private set
    var categories by mutableStateOf<Map<String, List<Song>>>(emptyMap())
        private set
    var topHits by mutableStateOf<List<Song>>(emptyList())
        private set

    init {
        loadData()
    }

    fun loadData() {
        isLoading = true
        isError = false
        
        coroutineScope.launch {
            try {
                // 1. Fetch Top Hits first
                val continueResult = repository.search("top hits music trending").take(8)
                topHits = continueResult
                isLoading = false // Show UI immediately after top row loads

                // 2. Fetch categories sequentially with delay to save server costs
                for (cat in HOME_CATEGORIES) {
                    try {
                        val songs = repository.search(cat.query).take(8)
                        categories = categories + (cat.id to songs)
                        delay(1500) // 1.5s delay to space out API requests
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
                isError = true
            }
        }
    }
}
