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
            val tmpDir = System.getProperty("java.io.tmpdir")
            val targetFile = java.io.File(tmpDir, "watermelon_$videoId.m4a")
            
            // If already downloaded, return immediately (Caching)
            if (targetFile.exists() && targetFile.length() > 0) {
                println("🍉 Found cached audio file: ${targetFile.absolutePath}")
                return@withContext targetFile.toURI().toString()
            }
            
            val url = "https://www.youtube.com/watch?v=$videoId"
            // Use yt-dlp to completely download the M4A file
            val processBuilder = ProcessBuilder(
                "yt-dlp",
                "-f",
                "bestaudio[ext=m4a]",
                "-o",
                targetFile.absolutePath,
                "--no-update",
                "--no-warnings",
                url
            )
            
            val process = processBuilder.start()
            // We must read output or it hangs on Windows
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println("yt-dlp: $line")
            }
            process.waitFor()
            
            if (targetFile.exists() && targetFile.length() > 0) {
                println("🍉 yt-dlp Download Success: ${targetFile.absolutePath}")
                return@withContext targetFile.toURI().toString()
            }
        } catch (e: Exception) {
            println("🍉 yt-dlp Download Failed: ${e.message}")
            e.printStackTrace()
        }
        
        println("🍉 All Extraction Algorithms Failed!")
        null
    }
}
