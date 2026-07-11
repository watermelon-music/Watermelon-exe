package com.watermelon.music.data.remote.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor

object LocalAudioExtractor {

    private val client = OkHttpClient()
    private var isNewPipeInitialized = false

    fun ensureNewPipeInitialized() {
        if (!isNewPipeInitialized) {
            val downloader = YouTubeDownloader(client)
            NewPipe.init(downloader, Localization.DEFAULT, ContentCountry("US"))
            isNewPipeInitialized = true
        }
    }

    suspend fun extractAudioUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            ensureNewPipeInitialized()
            val url = "https://www.youtube.com/watch?v=$videoId"
            val extractor = ServiceList.YouTube.getStreamExtractor(url)
            extractor.fetchPage()
            
            val audioStreams = extractor.audioStreams
            if (audioStreams.isNotEmpty()) {
                // Find highest quality M4A for JavaFX
                val bestM4a = audioStreams
                    .filter { it.format?.name?.contains("m4a", ignoreCase = true) == true || it.content.contains("audio/mp4") }
                    .maxByOrNull { it.averageBitrate }
                
                if (bestM4a != null && bestM4a.content.isNotBlank()) {
                    println("🍉 NewPipe Extractor (M4A) Success!")
                    return@withContext bestM4a.content
                }
                
                // Fallback to any audio stream if M4A not found
                val anyAudio = audioStreams.maxByOrNull { it.averageBitrate }
                if (anyAudio != null && anyAudio.content.isNotBlank()) {
                    println("🍉 NewPipe Extractor (Fallback) Success!")
                    return@withContext anyAudio.content
                }
            }
        } catch (e: Exception) {
            println("🍉 NewPipe Extractor Failed: ${e.message}")
            e.printStackTrace()
        }
        
        println("🍉 All Extraction Algorithms Failed!")
        null
    }
}
