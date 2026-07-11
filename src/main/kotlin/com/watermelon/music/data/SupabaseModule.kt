package com.watermelon.music.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Properties

object SupabaseModule {
    private val properties = Properties().apply {
        val file = File("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

    private val SUPABASE_URL = properties.getProperty("SUPABASE_URL") ?: "https://your-supabase-url.supabase.co"
    private val SUPABASE_KEY = properties.getProperty("SUPABASE_KEY") ?: "your-supabase-anon-key"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
            install(Auth)
            install(Postgrest)
        }
    }
}
