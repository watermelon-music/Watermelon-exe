package com.watermelon.music.data.remote

import com.watermelon.music.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object RadioBrowserApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "WatermelonMusicApp/1.0")
                .build()
            chain.proceed(request)
        }
        .build()

    // Radio Browser public endpoints usually have multiple mirrors, de1 is generally stable.
    private const val BASE_URL = "https://de1.api.radio-browser.info/json"

    suspend fun getTopCountries(limit: Int = 15): List<String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/countries?limit=$limit&order=stationcount&reverse=true")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext emptyList()
                val jsonArray = Json.parseToJsonElement(body).jsonArray
                val countries = mutableListOf<String>()
                for (item in jsonArray) {
                    val obj = item.jsonObject
                    countries.add(obj["name"]?.jsonPrimitive?.content ?: "")
                }
                countries
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getStationsByCountry(country: String, limit: Int = 10): List<Song> = withContext(Dispatchers.IO) {
        // order by votes to get the best/most popular stations in that country
        val encodedCountry = java.net.URLEncoder.encode(country, "UTF-8")
        val request = Request.Builder()
            .url("$BASE_URL/stations/bycountry/exact/$encodedCountry?limit=$limit&order=votes&reverse=true")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext emptyList()
                val jsonArray = Json.parseToJsonElement(body).jsonArray
                val stations = mutableListOf<Song>()
                for (item in jsonArray) {
                    val obj = item.jsonObject
                    var streamUrl = obj["url_resolved"]?.jsonPrimitive?.content ?: ""
                    if (streamUrl.isBlank()) streamUrl = obj["url"]?.jsonPrimitive?.content ?: ""
                    
                    if (streamUrl.isNotBlank()) {
                        stations.add(
                            Song(
                                id = streamUrl, // We store the stream URL as the ID so we can play it directly!
                                title = obj["name"]?.jsonPrimitive?.content?.trim() ?: "Unknown Station",
                                artist = country,
                                thumbnail = obj["favicon"]?.jsonPrimitive?.content ?: "",
                                duration = "LIVE"
                            )
                        )
                    }
                }
                stations
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
