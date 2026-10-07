package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.model.AppUser
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.MediaType
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.model.social.UserProfile
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.data.repository.UserMediaRepository
import com.cinetrack.app.data.repository.social.ProfileRepository
import com.cinetrack.app.data.repository.social.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val localAvatarUrl: String? = null,
    val favoriteMedia: List<MediaTitle> = emptyList(),
    val availableFavorites: List<MediaTitle> = emptyList(),
    val recentReviews: List<PublicReview> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val firebaseConfigured: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ProfileViewModel(
    private val user: AppUser,
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val mediaRepository: MediaRepository,
    private val localAvatarStore: com.cinetrack.app.data.local.LocalAvatarStore,
    private val userMediaRepository: UserMediaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(firebaseConfigured = profileRepository.isConfigured))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            userMediaRepository.entries.collect {
                _uiState.value.profile?.let { profile -> resolve(profile) }
            }
        }
    }

    fun load() {
        if (_uiState.value.loading || _uiState.value.saving) return
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val localAvatar = localAvatarStore.get(user.uid)
            _uiState.value = _uiState.value.copy(localAvatarUrl = localAvatar)

            val defaultProfile = UserProfile(
                uid = user.uid,
                displayName = user.displayName ?: "Usuario CineTrack"
            )
            val profileResult = if (profileRepository.isConfigured) {
                profileRepository.getProfile(user.uid)
            } else {
                Result.success(null)
            }

            val remote = profileResult.getOrNull()
            val profile = remote ?: _uiState.value.profile ?: defaultProfile
            var loadWarning = profileResult.exceptionOrNull()?.let {
                "No se pudo sincronizar el perfil. Se muestran los datos disponibles en este dispositivo."
            }

            // Solo crear un perfil inicial cuando Firestore respondió correctamente y confirmó que no existe.
            if (profileRepository.isConfigured && profileResult.isSuccess && remote == null) {
                profileRepository.saveProfile(profile).onFailure {
                    loadWarning = "El perfil todavía no pudo publicarse en Firebase."
                }
            }

            resolve(profile)
            val reviewsResult = if (reviewRepository.isConfigured) {
                reviewRepository.getReviewsForUser(user.uid)
            } else {
                Result.success(emptyList())
            }
            if (reviewsResult.isFailure && loadWarning == null) {
                loadWarning = "No se pudieron actualizar tus reseñas públicas."
            }

            _uiState.value = _uiState.value.copy(
                loading = false,
                profile = profile,
                recentReviews = reviewsResult.getOrDefault(emptyList()).take(10),
                firebaseConfigured = profileRepository.isConfigured,
                error = loadWarning
            )
        }
    }

    fun save(displayName: String, gender: String, favoriteMediaIds: List<String>, avatarUri: String?) {
        if (_uiState.value.saving || _uiState.value.loading) return
        val current = _uiState.value.profile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true, error = null, message = null)
            var avatarWarning: String? = null
            if (!avatarUri.isNullOrBlank()) {
                localAvatarStore.save(user.uid, avatarUri)
                    .onSuccess { _uiState.value = _uiState.value.copy(localAvatarUrl = it) }
                    .onFailure { error ->
                        avatarWarning = "La foto no se pudo actualizar: ${error.message.orEmpty()}"
                    }
            }
            val onlyPhotoChanged = displayName.trim() == current.displayName &&
                gender == current.gender && favoriteMediaIds.take(5) == current.favoriteMediaIds
            if (onlyPhotoChanged && !avatarUri.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    saving = false, error = avatarWarning,
                    message = if (avatarWarning == null) "Foto guardada solo en este dispositivo." else null
                )
                return@launch
            }
            val updated = current.copy(
                displayName = displayName.trim().ifBlank { "Usuario CineTrack" },
                gender = gender,
                favoriteMediaIds = favoriteMediaIds.take(5),
                avatarUrl = current.avatarUrl,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
            if (profileRepository.isConfigured) {
                profileRepository.saveProfile(updated)
                    .onSuccess {
                        resolve(updated)
                        _uiState.value = _uiState.value.copy(
                            profile = updated,
                            saving = false,
                            error = null,
                            message = avatarWarning?.let { "Perfil actualizado. $it" } ?: "Perfil actualizado."
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(saving = false, error = error.message ?: "No se pudo guardar el perfil.")
                    }
            } else {
                resolve(updated)
                _uiState.value = _uiState.value.copy(
                    profile = updated,
                    saving = false,
                    message = "Vista previa local. Conecta Firebase para publicar el perfil."
                )
            }
        }
    }

    private suspend fun resolve(profile: UserProfile) {
        val favoriteEntries = userMediaRepository.entries.value.filter { it.userId == user.uid && it.favorite }
        val available = favoriteEntries.mapNotNull { entry ->
            mediaRepository.getById(entry.mediaId) ?: entry.mediaSnapshot ?: mediaRepository.getDetails(entry.mediaId)?.media
        }.filter { it.type == MediaType.MOVIE }
        val selected = profile.favoriteMediaIds.mapNotNull { id ->
            mediaRepository.getById(id) ?: mediaRepository.getDetails(id)?.media
        }.filter { it.type == MediaType.MOVIE }
        _uiState.value = _uiState.value.copy(
            favoriteMedia = selected,
            availableFavorites = available.distinctBy { it.id }
        )
    }

    companion object {
        fun factory(
            user: AppUser,
            profileRepository: ProfileRepository,
            reviewRepository: ReviewRepository,
            mediaRepository: MediaRepository,
            userMediaRepository: UserMediaRepository,
            localAvatarStore: com.cinetrack.app.data.local.LocalAvatarStore
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(user, profileRepository, reviewRepository, mediaRepository, localAvatarStore, userMediaRepository) as T
        }
    }
}



