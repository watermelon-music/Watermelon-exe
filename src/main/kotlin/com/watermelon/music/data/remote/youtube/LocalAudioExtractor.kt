package com.watermelon.music.data.remote.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object LocalAudioExtractor {

    suspend fun extractAudioUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            // Use yt-dlp to get the direct audio stream URL. 
            // We request the best audio format that is m4a or webm. 
            // JavaFX primarily supports m4a (AAC) or mp3, so we prioritize m4a.
            val process = ProcessBuilder(
                "yt-dlp",
                "-g",
                "-f",
                "bestaudio[ext=m4a]/bestaudio[ext=mp3]/bestaudio",
                "--no-check-certificate",
                "--no-warnings",
                url
            )
            .redirectErrorStream(true)
            .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var directUrl: String? = null
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                // The first valid https:// URL returned is the stream URL
                if (line?.startsWith("https://") == true) {
                    directUrl = line
                    break
                }
            }

            process.waitFor()
            
            if (directUrl != null) {
                println("🍉 yt-dlp Extraction Success!")
                return@withContext directUrl
            }
            
            println("🍉 yt-dlp Extraction Failed: No URL returned")
            null
        } catch (e: Exception) {
            println("🍉 yt-dlp Extraction Failed: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
