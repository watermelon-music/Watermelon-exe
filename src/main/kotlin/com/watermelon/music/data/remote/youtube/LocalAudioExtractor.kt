package com.watermelon.music.data.remote.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.schabi.newpipe.extractor.ServiceList
import java.util.regex.Pattern

object LocalAudioExtractor {
    private val client = OkHttpClient()

    suspend fun extractAudioUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        // Algorithm 1: Direct InnerTube API (Fastest, no HTML parsing)
        try {
            val innerTubeUrl = tryInnerTubeApi(videoId)
            if (innerTubeUrl != null) {
                println("🍉 Algorithm 1 (InnerTube) Success!")
                return@withContext innerTubeUrl
            }
        } catch (e: Exception) {
            println("🍉 Algorithm 1 Failed: ${e.message}")
        }

        // Algorithm 2: HTML Regex Scraper (Fast, bypasses NewPipe's heavy parsing)
        try {
            val htmlUrl = tryHtmlRegexScrape(videoId)
            if (htmlUrl != null) {
                println("🍉 Algorithm 2 (Regex) Success!")
                return@withContext htmlUrl
            }
        } catch (e: Exception) {
            println("🍉 Algorithm 2 Failed: ${e.message}")
        }

        // Algorithm 3: NewPipe Extractor (Thorough but sometimes slower/blocked)
        try {
            val newpipeUrl = tryNewPipeExtractor(videoId)
            if (newpipeUrl != null) {
                println("🍉 Algorithm 3 (NewPipe) Success!")
                return@withContext newpipeUrl
            }
        } catch (e: Exception) {
            println("🍉 Algorithm 3 Failed: ${e.message}")
        }

        null
    }

    private fun tryInnerTubeApi(videoId: String): String? {
        val jsonPayload = """
            {
                "context": {
                    "client": {
                        "hl": "en",
                        "clientName": "WEB",
                        "clientVersion": "2.20210721.00.00",
                        "clientFormFactor": "UNKNOWN_FORM_FACTOR",
                        "clientScreen": "WATCH"
                    }
                },
                "videoId": "$videoId",
                "playbackContext": {
                    "contentPlaybackContext": {
                        "signatureTimestamp": 19000
                    }
                }
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://www.youtube.com/youtubei/v1/player")
            .post(jsonPayload.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val jsonString = response.body?.string() ?: return null
        
        val json = JSONObject(jsonString)
        val streamingData = json.optJSONObject("streamingData") ?: return null
        val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats") ?: return null

        for (i in 0 until adaptiveFormats.length()) {
            val format = adaptiveFormats.getJSONObject(i)
            val mimeType = format.optString("mimeType", "")
            // We specifically want MP4/M4A audio for JavaFX
            if (mimeType.contains("audio/mp4")) {
                val url = format.optString("url")
                if (url.isNotEmpty()) return url
            }
        }
        return null
    }

    private fun tryHtmlRegexScrape(videoId: String): String? {
        val request = Request.Builder()
            .url("https://www.youtube.com/watch?v=$videoId")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build()

        val response = client.newCall(request).execute()
        val html = response.body?.string() ?: return null

        val pattern = Pattern.compile("ytInitialPlayerResponse\\s*=\\s*(\\{.+?\\});")
        val matcher = pattern.matcher(html)
        
        if (matcher.find()) {
            val jsonString = matcher.group(1)
            val json = JSONObject(jsonString)
            val streamingData = json.optJSONObject("streamingData") ?: return null
            val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats") ?: return null
            
            for (i in 0 until adaptiveFormats.length()) {
                val format = adaptiveFormats.getJSONObject(i)
                val mimeType = format.optString("mimeType", "")
                if (mimeType.contains("audio/mp4")) {
                    val url = format.optString("url")
                    if (url.isNotEmpty()) return url
                }
            }
        }
        return null
    }

    private fun tryNewPipeExtractor(videoId: String): String? {
        val extractor = ServiceList.YouTube.getStreamExtractor("https://www.youtube.com/watch?v=$videoId")
        extractor.fetchPage()
        
        val audioStreams = extractor.audioStreams
        // Must enforce M4A for JavaFX!
        val m4aStream = audioStreams.find { it.format?.name?.contains("M4A", ignoreCase = true) == true }
        return m4aStream?.content
    }
}
