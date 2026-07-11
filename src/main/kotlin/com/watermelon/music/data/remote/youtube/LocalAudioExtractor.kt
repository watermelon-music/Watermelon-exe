package com.watermelon.music.data.remote.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object LocalAudioExtractor {

    private val client = OkHttpClient()

    suspend fun extractAudioUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        // Algorithm 1: Piped API (Fastest proxy, bypasses all local YouTube blocks)
        try {
            val pipedUrl = fetchPipedAudioUrl(videoId)
            if (pipedUrl != null) {
                println("🍉 Algorithm 1 (Piped) Success!")
                return@withContext pipedUrl
            }
        } catch (e: Exception) {
            println("🍉 Algorithm 1 Failed: ${e.message}")
        }

        // Algorithm 2: Cobalt API (Ultra reliable fallback)
        try {
            val cobaltUrl = fetchCobaltAudioUrl("https://www.youtube.com/watch?v=$videoId")
            if (cobaltUrl != null) {
                println("🍉 Algorithm 2 (Cobalt) Success!")
                return@withContext cobaltUrl
            }
        } catch (e: Exception) {
            println("🍉 Algorithm 2 Failed: ${e.message}")
        }

        println("🍉 All Extraction Algorithms Failed!")
        null
    }

    private fun fetchPipedAudioUrl(videoId: String): String? {
        val pipedInstances = listOf(
            "https://pipedapi.kavin.rocks",
            "https://api.piped.projectmainstreet.org",
            "https://pipedapi.adminforge.de"
        )
        for (baseUrl in pipedInstances) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/streams/$videoId")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val parsed = JSONObject(body)
                    val audioStreams = parsed.optJSONArray("audioStreams")
                    if (audioStreams != null && audioStreams.length() > 0) {
                        for (i in 0 until audioStreams.length()) {
                            val stream = audioStreams.getJSONObject(i)
                            val mimeType = stream.optString("mimeType", "")
                            if (mimeType.contains("audio/mp4")) {
                                val url = stream.optString("url")
                                if (url.isNotEmpty()) return url
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                continue
            }
        }
        return null
    }

    private fun fetchCobaltAudioUrl(sourceUrl: String): String? {
        try {
            val bodyJson = """{"url":"$sourceUrl","downloadMode":"audio","audioFormat":"best"}"""
            val requestBody = bodyJson.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.cobalt.tools/api/json")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val parsed = JSONObject(body)
                val status = parsed.optString("status")
                if (status == "stream" || status == "tunnel") {
                    return parsed.optString("url")
                } else if (status == "picker") {
                    val picker = parsed.optJSONArray("picker")
                    if (picker != null && picker.length() > 0) {
                        return picker.getJSONObject(0).optString("url")
                    }
                }
            }
        } catch (_: Exception) {
            return null
        }
        return null
    }
}
