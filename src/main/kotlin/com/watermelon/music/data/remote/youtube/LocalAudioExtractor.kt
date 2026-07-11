package com.watermelon.music.data.remote.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import okhttp3.OkHttpClient

object LocalAudioExtractor {
    private var isNewPipeInitialized = false
    private val client = OkHttpClient()

    fun ensureNewPipeInitialized() {
        if (!isNewPipeInitialized) {
            val downloader = YouTubeDownloader(client)
            NewPipe.init(downloader, Localization.DEFAULT, ContentCountry("US"))
            isNewPipeInitialized = true
        }
    }

    suspend fun extractAudioUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            // Use yt-dlp to extract the direct audio stream URL
            val processBuilder = ProcessBuilder(
                "yt-dlp",
                "-g",
                "-f",
                "bestaudio[ext=m4a]",
                "--no-update",
                "--no-warnings",
                url
            )
            
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line)
            }
            process.waitFor()
            
            val streamUrl = output.toString().trim()
            if (streamUrl.isNotBlank() && streamUrl.startsWith("http")) {
                println("🍉 yt-dlp Extraction Success!")
                return@withContext streamUrl
            }
        } catch (e: Exception) {
            println("🍉 yt-dlp Extraction Failed: ${e.message}")
            e.printStackTrace()
        }
        
        println("🍉 All Extraction Algorithms Failed!")
        null
    }
}
