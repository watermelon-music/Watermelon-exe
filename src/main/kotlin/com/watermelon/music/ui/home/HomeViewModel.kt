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

    // New states for Radio
    var topGlobalRadios by mutableStateOf<List<Song>>(emptyList())
        private set
    var radioCountries by mutableStateOf<List<com.watermelon.music.domain.model.Country>>(emptyList())
        private set
    var selectedRadioCountry by mutableStateOf<com.watermelon.music.domain.model.Country?>(null)
        private set
    var countryStations by mutableStateOf<List<Song>>(emptyList())
        private set

    enum class Filter { ALL, MUSIC, BROADCASTS, RADIO }
    var currentFilter by mutableStateOf(Filter.ALL)
        private set

    init {
        loadData()
    }

    fun setFilter(filter: Filter) {
        if (currentFilter != filter) {
            currentFilter = filter
            selectedRadioCountry = null
            loadData()
        }
    }

    fun selectRadioCountry(country: com.watermelon.music.domain.model.Country?) {
        selectedRadioCountry = country
        if (country != null) {
            coroutineScope.launch {
                isLoading = true
                countryStations = com.watermelon.music.data.remote.RadioBrowserApi.getStationsByCountry(country.name, 20)
                isLoading = false
            }
        }
    }

    fun loadData() {
        isLoading = true
        isError = false
        
        coroutineScope.launch {
            try {
                if (currentFilter == Filter.RADIO) {
                    val globalRadios = com.watermelon.music.data.remote.RadioBrowserApi.getTopGlobalStations(10)
                    val countries = com.watermelon.music.data.remote.RadioBrowserApi.getTopCountries(30)
                    topGlobalRadios = globalRadios
                    radioCountries = countries
                    isLoading = false
                } else {
                    // 1. Fetch Top Hits first based on filter
                    val query = when (currentFilter) {
                        Filter.ALL -> "top hits music trending"
                        Filter.MUSIC -> "latest hit songs music"
                        Filter.BROADCASTS -> "live broadcast radio podcast full episodes"
                        else -> ""
                    }
                    
                    val continueResult = repository.search(query).take(8)
                    topHits = continueResult
                    isLoading = false // Show UI immediately after top row loads

                    // 2. Fetch categories sequentially with delay to save server costs
                    val filteredCategories = if (currentFilter == Filter.BROADCASTS) {
                        listOf(
                            com.watermelon.music.domain.model.Category("comedy", "Comedy Podcasts", "comedy podcast full episode"),
                            com.watermelon.music.domain.model.Category("news", "News Broadcasts", "live news broadcast full"),
                            com.watermelon.music.domain.model.Category("sports", "Sports Radio", "sports radio live broadcast"),
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
                isError = true
            }
        }
    }
}
