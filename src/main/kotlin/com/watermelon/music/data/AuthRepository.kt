package com.watermelon.music.data

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val client = SupabaseModule.client

    suspend fun signIn(emailParam: String, passwordParam: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.auth.signInWith(Email) {
                email = emailParam
                password = passwordParam
            }
            Unit
        }
    }
    
    suspend fun checkSession(): Boolean {
        // Very basic session check for now
        return client.auth.currentSessionOrNull() != null
    }
}
