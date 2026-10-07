package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.MediaType
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.model.social.UserProfile
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.data.repository.social.ProfileRepository
import com.cinetrack.app.data.repository.social.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PublicProfileUiState(
    val profile: UserProfile? = null,
    val favoriteMedia: List<MediaTitle> = emptyList(),
    val recentReviews: List<PublicReview> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class PublicProfileViewModel(
    uid: String,
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState: StateFlow<PublicProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (!profileRepository.isConfigured) {
                _uiState.value = PublicProfileUiState(loading = false, error = "Conecta Firebase para consultar perfiles públicos.")
                return@launch
            }
            profileRepository.getProfile(uid)
                .onSuccess { profile ->
                    if (profile == null) {
                        _uiState.value = PublicProfileUiState(loading = false, error = "Este perfil no existe o todavía no es público.")
                    } else {
                        val favorites = profile.favoriteMediaIds.mapNotNull { id ->
                            mediaRepository.getById(id) ?: mediaRepository.getDetails(id)?.media
                        }.filter { it.type == MediaType.MOVIE }
                        val reviews = if (reviewRepository.isConfigured) {
                            reviewRepository.getReviewsForUser(uid).getOrDefault(emptyList()).take(10)
                        } else emptyList()
                        _uiState.value = PublicProfileUiState(profile = profile, favoriteMedia = favorites, recentReviews = reviews, loading = false)
                    }
                }
                .onFailure { error ->
                    _uiState.value = PublicProfileUiState(loading = false, error = error.message ?: "No se pudo cargar el perfil.")
                }
        }
    }

    companion object {
        fun factory(uid: String, profiles: ProfileRepository, reviews: ReviewRepository, media: MediaRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PublicProfileViewModel(uid, profiles, reviews, media) as T
        }
    }
}
