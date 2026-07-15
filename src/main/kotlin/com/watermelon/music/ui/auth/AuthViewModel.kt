package com.watermelon.music.ui.auth

import com.watermelon.music.data.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false, needsEmailVerification = false) }
            val result = authRepository.signIn(email, password)
            if (result.isSuccess) {
                // Check if email verified is possible with supabase in desktop. Assume true or handle according to repository.
                val verified = authRepository.isEmailVerified()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = verified,
                        needsEmailVerification = !verified,
                        errorMessage = null
                    )
                }
            } else {
                val rawMessage = result.exceptionOrNull()?.message
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = cleanErrorMessage(rawMessage)
                    )
                }
            }
        }
    }

    fun signUp(username: String, email: String, password: String) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false, needsEmailVerification = false) }
            val result = authRepository.signUp(username, email, password)
            val rawMessage = result.exceptionOrNull()?.message
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = false,
                    needsEmailVerification = result.isSuccess,
                    errorMessage = if (result.isSuccess) null else cleanErrorMessage(rawMessage)
                )
            }
        }
    }

    fun resetPassword(email: String) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, resetSent = false) }
            val result = authRepository.resetPassword(email)
            val rawMessage = result.exceptionOrNull()?.message
            _uiState.update {
                it.copy(
                    isLoading = false,
                    resetSent = result.isSuccess,
                    errorMessage = if (result.isSuccess) null else cleanErrorMessage(rawMessage)
                )
            }
        }
    }

    fun resendVerificationEmail(email: String) {
        coroutineScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, resetSent = false) }
            val result = authRepository.resendVerificationEmail(email)
            val rawMessage = result.exceptionOrNull()?.message
            _uiState.update {
                it.copy(
                    isLoading = false,
                    resetSent = result.isSuccess,
                    errorMessage = if (result.isSuccess) null else cleanErrorMessage(rawMessage)
                )
            }
        }
    }

    suspend fun isEmailVerified(): Boolean {
        return authRepository.isEmailVerified()
    }

    suspend fun getCurrentEmail(): String? = authRepository.getCurrentUserEmail()

    fun signOut() {
        coroutineScope.launch {
            authRepository.signOut()
            _uiState.update { AuthUiState() }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, isSuccess = false, resetSent = false, needsEmailVerification = false) }
    }

    private fun cleanErrorMessage(message: String?): String {
        if (message == null) return "Something went wrong. Please try again."
        if (message.contains("Invalid login credentials", ignoreCase = true)) {
            return "Invalid email or password."
        }
        if (message.contains("User already exists", ignoreCase = true)) {
            return "An account with this email already exists."
        }
        if (message.contains("Email not confirmed", ignoreCase = true)) {
            return "Please verify your email address first."
        }
        if (message.contains("URL:", ignoreCase = true)) {
            return message.substringBefore("URL:").trim()
        }
        return message
    }

    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated()
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), false)
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val needsEmailVerification: Boolean = false,
    val resetSent: Boolean = false,
    val errorMessage: String? = null
)
