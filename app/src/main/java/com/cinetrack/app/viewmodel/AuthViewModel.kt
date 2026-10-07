package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.repository.AuthActionResult
import com.cinetrack.app.data.repository.AuthRepository
import com.cinetrack.app.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val authenticated: Boolean? = null,
    val error: String? = null,
    val message: String? = null,
    val firebaseConfigured: Boolean = true
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init { checkSession() }

    fun checkSession() {
        _uiState.value = AuthUiState(
            authenticated = repository.currentUser != null,
            firebaseConfigured = repository.isFirebaseConfigured
        )
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(error = null, message = null)
    }

    fun signIn(email: String, password: String) {
        val validation = validateLogin(email, password)
        if (validation != null) {
            _uiState.value = _uiState.value.copy(error = validation, message = null)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null, message = null)
            when (val result = repository.signIn(email, password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(authenticated = true, firebaseConfigured = repository.isFirebaseConfigured)
                is AuthResult.Error -> _uiState.value = _uiState.value.copy(loading = false, authenticated = false, error = result.message)
            }
        }
    }

    fun signUp(name: String, email: String, password: String, confirmation: String) {
        val validation = validateRegistration(name, email, password, confirmation)
        if (validation != null) {
            _uiState.value = _uiState.value.copy(error = validation, message = null)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null, message = null)
            when (val result = repository.signUp(name, email, password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(authenticated = true, firebaseConfigured = repository.isFirebaseConfigured)
                is AuthResult.Error -> _uiState.value = _uiState.value.copy(loading = false, authenticated = false, error = result.message)
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !EMAIL_REGEX.matches(email.trim())) {
            _uiState.value = _uiState.value.copy(error = "Ingresa un correo válido para recuperar la contraseña.", message = null)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null, message = null)
            when (val result = repository.sendPasswordReset(email)) {
                AuthActionResult.Success -> _uiState.value = _uiState.value.copy(loading = false, message = "Si la cuenta existe, Firebase enviará instrucciones al correo indicado.")
                is AuthActionResult.Error -> _uiState.value = _uiState.value.copy(loading = false, error = result.message)
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState(authenticated = false, firebaseConfigured = repository.isFirebaseConfigured)
    }

    private fun validateLogin(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) return "Todos los campos son obligatorios."
        if (!EMAIL_REGEX.matches(email.trim())) return "Ingresa un correo válido."
        return null
    }

    private fun validateRegistration(name: String, email: String, password: String, confirmation: String): String? {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmation.isBlank()) return "Todos los campos son obligatorios."
        if (name.trim().length > 40) return "El nombre puede tener máximo 40 caracteres."
        if (!EMAIL_REGEX.matches(email.trim())) return "Ingresa un correo válido."
        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres."
        if (password != confirmation) return "Las contraseñas no coinciden."
        return null
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        fun factory(repository: AuthRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository) as T
        }
    }
}
