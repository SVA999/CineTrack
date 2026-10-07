package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.data.repository.social.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SharedReviewUiState(
    val review: PublicReview? = null,
    val media: MediaTitle? = null,
    val loading: Boolean = true,
    val error: String? = null
)

class SharedReviewViewModel(
    reviewId: String,
    private val reviewRepository: ReviewRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SharedReviewUiState())
    val uiState: StateFlow<SharedReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (!reviewRepository.isConfigured) {
                _uiState.value = SharedReviewUiState(loading = false, error = "Conecta Firebase para abrir reseñas compartidas.")
                return@launch
            }
            reviewRepository.getReview(reviewId)
                .onSuccess { review ->
                    if (review == null) {
                        _uiState.value = SharedReviewUiState(loading = false, error = "La reseña no existe.")
                    } else {
                        val media = mediaRepository.getById(review.mediaId) ?: mediaRepository.getDetails(review.mediaId)?.media
                        _uiState.value = SharedReviewUiState(review = review, media = media, loading = false)
                    }
                }
                .onFailure { error ->
                    _uiState.value = SharedReviewUiState(loading = false, error = error.message ?: "No se pudo cargar la reseña.")
                }
        }
    }

    companion object {
        fun factory(reviewId: String, reviews: ReviewRepository, media: MediaRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SharedReviewViewModel(reviewId, reviews, media) as T
        }
    }
}
