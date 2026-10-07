package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.repository.AuthActionResult
import com.cinetrack.app.data.repository.AuthRepository
import com.cinetrack.app.data.repository.UserMediaRepository
import com.cinetrack.app.data.repository.social.ProfileRepository
import com.cinetrack.app.data.repository.social.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountDeletionUiState(
    val deleting: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null
)

/**
 * Orquesta la eliminación de los datos asociados a la cuenta.
 *
 * Firestore se limpia antes de Firebase Auth porque, una vez eliminada la
 * identidad, las reglas de seguridad ya no permitirían borrar documentos del
 * usuario desde el cliente.
 */
class AccountDeletionViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val localAvatarStore: com.cinetrack.app.data.local.LocalAvatarStore,
    private val userMediaRepository: UserMediaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountDeletionUiState())
    val uiState: StateFlow<AccountDeletionUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun deleteAccount(password: String) {
        if (_uiState.value.deleting) return
        val user = authRepository.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(error = "No hay una sesión activa.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AccountDeletionUiState(deleting = true)

            // Firebase Authentication exige una autenticación reciente para
            // operaciones sensibles como eliminar la cuenta. Verificamos la
            // contraseña ANTES de borrar Firestore para no dejar una cuenta
            // activa con el perfil/reseñas ya eliminados si Firebase rechaza
            // después la operación por ERROR_REQUIRES_RECENT_LOGIN.
            when (val reauth = authRepository.reauthenticateCurrentUser(password)) {
                AuthActionResult.Success -> Unit
                is AuthActionResult.Error -> {
                    _uiState.value = AccountDeletionUiState(error = reauth.message)
                    return@launch
                }
            }

            if (reviewRepository.isConfigured) {
                val reviews = reviewRepository.deleteReviewsForUser(user.uid)
                if (reviews.isFailure) {
                    _uiState.value = AccountDeletionUiState(error = "No se pudieron borrar tus reseñas públicas. Revisa la conexión e inténtalo de nuevo.")
                    return@launch
                }
            }

            if (profileRepository.isConfigured) {
                val profile = profileRepository.deleteProfile(user.uid)
                if (profile.isFailure) {
                    _uiState.value = AccountDeletionUiState(error = "No se pudo borrar tu perfil público. Revisa la conexión e inténtalo de nuevo.")
                    return@launch
                }
            }

            when (val result = authRepository.deleteCurrentAccount()) {
                AuthActionResult.Success -> {
                    userMediaRepository.clearUser(user.uid)
                    localAvatarStore.delete(user.uid)
                    _uiState.value = AccountDeletionUiState(deleted = true)
                }
                is AuthActionResult.Error -> {
                    _uiState.value = AccountDeletionUiState(error = result.message)
                }
            }
        }
    }

    companion object {
        fun factory(
            authRepository: AuthRepository,
            profileRepository: ProfileRepository,
            reviewRepository: ReviewRepository,
            userMediaRepository: UserMediaRepository,
            localAvatarStore: com.cinetrack.app.data.local.LocalAvatarStore
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AccountDeletionViewModel(authRepository, profileRepository, reviewRepository, localAvatarStore, userMediaRepository) as T
        }
    }
}

