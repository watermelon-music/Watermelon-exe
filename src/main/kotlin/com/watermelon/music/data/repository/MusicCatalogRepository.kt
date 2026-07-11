package com.watermelon.music.data.repository

import com.watermelon.music.data.remote.youtube.LocalAudioExtractor
import com.watermelon.music.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class MusicCatalogRepository {
    private val youtube by lazy { ServiceList.YouTube }

    suspend fun search(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            LocalAudioExtractor.ensureNewPipeInitialized()
            
            // Search specifically within YouTube Music (music_songs) to get official tracks
            val queryHandler = youtube.getSearchQHFactory().fromQuery(query, listOf("music_songs"), "")
            val extractor = youtube.getSearchExtractor(queryHandler)
            extractor.fetchPage()
            
            extractor.initialPage.items
                .filterIsInstance<StreamInfoItem>()
                .take(15) // Limit to save memory
                .map { it.toSong() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun upgradeImageUrl(url: String, videoId: String?): String {
        return when {
            url.contains("googleusercontent.com") ->
                url.replace(Regex("=[sw]\\d+.*"), "=s720")
            url.contains("ytimg.com") -> {
                if (videoId != null) {
                    "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                } else {
                    url.replace(Regex("(mqdefault|hqdefault|sddefault|default)"), "hqdefault")
                }
            }
            else -> url
        }
    }

    private fun extractVideoId(url: String): String? {
        val pattern = Regex("(?:v=|/v/|youtu\\.be/|/embed/|/vi/)([^&\\n?#]+)")
        val match = pattern.find(url)
        return match?.groupValues?.get(1)
    }

    private fun StreamInfoItem.toSong(): Song {
        val fullUrl = if (url.startsWith("http")) url else "https://www.youtube.com$url"
        val videoId = extractVideoId(fullUrl) ?: url
        val firstThumbnail = thumbnails.firstOrNull()?.url
        val baseThumbUrl = firstThumbnail?.takeIf { it.isNotBlank() } ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
        val thumbUrl = upgradeImageUrl(baseThumbUrl, videoId)
        
        // Convert duration seconds to formatted string (e.g. 3:45)
        val durationMins = duration / 60
        val durationSecs = duration % 60
        val formattedDuration = String.format("%d:%02d", durationMins, durationSecs)
        
        return Song(
            id = videoId,
            title = name ?: "Unknown Title",
            artist = uploaderName ?: "Unknown Artist",
            thumbnail = thumbUrl,
            duration = formattedDuration
        )
    }
}
