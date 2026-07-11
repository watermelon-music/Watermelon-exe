package com.watermelon.music.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WatermelonApi {
    @GET("search")
    suspend fun search(@Query("q") query: String): List<WatermelonSong>
}
