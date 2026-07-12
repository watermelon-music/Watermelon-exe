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
    var currentCategories by mutableStateOf<List<com.watermelon.music.domain.model.Category>>(emptyList())
        private set
    var topHits by mutableStateOf<List<Song>>(emptyList())
        private set

    enum class Filter { ALL, MUSIC, BROADCASTS }
    var currentFilter by mutableStateOf(Filter.ALL)
        private set

    init {
        loadData()
    }

    fun setFilter(filter: Filter) {
        if (currentFilter != filter) {
            currentFilter = filter
            loadData()
        }
    }

    fun loadData() {
        isLoading = true
        isError = false
        
        coroutineScope.launch {
            try {
                // 1. Fetch Top Hits first based on filter
                val query = when (currentFilter) {
                    Filter.ALL -> "top hits music trending"
                    Filter.MUSIC -> "latest hit songs music"
                    Filter.BROADCASTS -> "live broadcast radio podcast full episodes"
                }
                
                val continueResult = repository.search(query).take(8)
                topHits = continueResult
                isLoading = false // Show UI immediately after top row loads

                // 2. Fetch categories sequentially with delay to save server costs
                val filteredCategories = if (currentFilter == Filter.BROADCASTS) {
                    listOf(
                        com.watermelon.music.domain.model.Category("news", "News Broadcasts", "live news broadcast full"),
                        com.watermelon.music.domain.model.Category("sports", "Sports Radio", "sports radio live broadcast"),
                        com.watermelon.music.domain.model.Category("comedy", "Comedy Podcasts", "comedy podcast full episode"),
                        com.watermelon.music.domain.model.Category("tech", "Tech & Science", "technology podcast episode")
                    )
                } else {
                    HOME_CATEGORIES
                }
                
                currentCategories = filteredCategories
                categories = emptyMap() // Reset categories
                
                for (cat in filteredCategories) {
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
