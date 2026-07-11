package com.watermelon.music.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit

object RetrofitModule {
    private val properties = Properties().apply {
        val file = File("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

    private val WATERMELON_API_URL = properties.getProperty("WATERMELON_API_URL") ?: "https://watermelon-api-oxx2.onrender.com/"

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: WatermelonApi by lazy {
        Retrofit.Builder()
            .baseUrl(WATERMELON_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WatermelonApi::class.java)
    }
}
