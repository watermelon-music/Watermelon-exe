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

    private val SUPABASE_URL = properties.getProperty("SUPABASE_URL") ?: "https://xljlceoircpibojirxob.supabase.co"
    private val SUPABASE_KEY = properties.getProperty("SUPABASE_KEY") ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhsamxjZW9pcmNwaWJvamlyeG9iIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODA2NzkzNzcsImV4cCI6MjA5NjI1NTM3N30.zmMpnCHT7_ZHQA9YvInLZL6V6xF9xNg1c2HHiub6TZE"

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
