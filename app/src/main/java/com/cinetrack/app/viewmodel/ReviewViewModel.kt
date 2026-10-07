package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.data.repository.UserMediaRepository
import com.cinetrack.app.data.repository.social.ProfileRepository
import com.cinetrack.app.data.repository.social.ReviewRepository
import com.cinetrack.app.share.ShareLinks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class ReviewUiState(
    val media: MediaTitle? = null,
    val status: WatchStatus = WatchStatus.PENDING,
    val favorite: Boolean = false,
    val rating: Int? = null,
    val comment: String = "",
    val publicReview: Boolean = false,
    val hasChanges: Boolean = false,
    val saved: Boolean = false,
    val publishing: Boolean = false,
    val loadingPublicState: Boolean = false,
    val publicReviewId: String? = null,
    val message: String? = null
)

class ReviewViewModel(
    private val mediaId: String,
    private val userId: String,
    private val mediaRepository: MediaRepository,
    private val userMediaRepository: UserMediaRepository,
    private val reviewRepository: ReviewRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private var original = userMediaRepository.get(userId, mediaId)
    private var recordCreatedAt = original?.createdAt ?: System.currentTimeMillis()
    private val reviewId = ShareLinks.reviewId(userId, mediaId)

    private val initial = ReviewUiState(
        media = mediaRepository.getById(mediaId) ?: original?.mediaSnapshot,
        status = original?.status ?: WatchStatus.PENDING,
        favorite = original?.favorite ?: false,
        rating = original?.personalRating,
        comment = original?.comment.orEmpty(),
        loadingPublicState = reviewRepository.isConfigured
    )

    private var baseline = initial
    private val _uiState = MutableStateFlow(initial)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        // Room puede terminar de hidratarse después de crear el ViewModel. Si existe
        // un registro persistido y el usuario aún no ha editado nada, sincronizamos
        // el borrador con esa fuente de verdad.
        viewModelScope.launch {
            userMediaRepository.entries.collect { entries ->
                val persisted = entries.firstOrNull { it.userId == userId && it.mediaId == mediaId }
                val currentOriginal = original
                if (persisted != null && (currentOriginal == null || persisted.updatedAt > currentOriginal.updatedAt) && !_uiState.value.hasChanges) {
                    original = persisted
                    recordCreatedAt = persisted.createdAt
                    val hydrated = _uiState.value.copy(
                        media = _uiState.value.media ?: persisted.mediaSnapshot,
                        status = persisted.status,
                        favorite = persisted.favorite,
                        rating = persisted.personalRating,
                        comment = persisted.comment,
                        saved = false
                    )
                    baseline = hydrated.copy(message = null)
                    _uiState.value = hydrated
                }
            }
        }

        if (initial.media == null) {
            viewModelScope.launch {
                mediaRepository.getDetails(mediaId)?.media?.let { media ->
                    _uiState.value = _uiState.value.copy(media = media)
                    baseline = baseline.copy(media = media)
                }
            }
        }
        if (reviewRepository.isConfigured) {
            viewModelScope.launch {
                val existingPublic = reviewRepository.getReview(reviewId).getOrNull()
                val isPublic = existingPublic != null
                baseline = baseline.copy(
                    publicReview = isPublic,
                    publicReviewId = existingPublic?.id,
                    loadingPublicState = false
                )
                _uiState.value = _uiState.value.copy(
                    publicReview = isPublic,
                    publicReviewId = existingPublic?.id,
                    loadingPublicState = false
                )
            }
        }
    }

    fun setStatus(value: WatchStatus) = mutate { copy(status = value) }
    fun setFavorite(value: Boolean) = mutate { copy(favorite = value) }
    fun setRating(value: Int?) = mutate { copy(rating = value?.coerceIn(1, 5)) }
    fun setComment(value: String) = mutate { copy(comment = value.take(500)) }
    fun setPublicReview(value: Boolean) = mutate { copy(publicReview = value) }

    fun save() {
        val state = _uiState.value
        val wasPublicBeforeSave = baseline.publicReview
        val previousPublicReviewId = baseline.publicReviewId
        val now = System.currentTimeMillis()
        val normalizedComment = state.comment.trim()

        val savedEntry = UserMedia(
            id = original?.id ?: "$userId:$mediaId",
            userId = userId,
            mediaId = mediaId,
            status = state.status,
            personalRating = state.rating,
            comment = normalizedComment,
            favorite = state.favorite,
            createdAt = recordCreatedAt,
            updatedAt = now,
            mediaSnapshot = state.media ?: original?.mediaSnapshot ?: mediaRepository.getById(mediaId)
        )
        original = savedEntry
        userMediaRepository.upsert(savedEntry)

        val hasReviewContent = state.rating != null || normalizedComment.isNotBlank()
        val wantsPublic = state.publicReview && hasReviewContent
        val normalized = state.copy(
            comment = normalizedComment,
            publicReview = wantsPublic,
            hasChanges = false,
            saved = true
        )

        if (!reviewRepository.isConfigured) {
            baseline = normalized.copy(publicReview = false, publicReviewId = null, publishing = false)
            _uiState.value = baseline.copy(
                message = if (state.publicReview) {
                    "Registro guardado. Conecta Firebase para poder publicar y compartir la reseña."
                } else {
                    "Registro guardado."
                }
            )
            return
        }

        if (!wantsPublic) {
            baseline = normalized.copy(publicReview = false, publicReviewId = null, publishing = true)
            _uiState.value = baseline.copy(
                message = if (state.publicReview && !hasReviewContent) {
                    "Registro guardado. Añade una valoración o comentario para poder hacer pública la reseña."
                } else {
                    "Registro guardado. Actualizando privacidad…"
                }
            )
            viewModelScope.launch {
                reviewRepository.deleteReview(reviewId)
                    .onSuccess {
                        baseline = baseline.copy(publishing = false, publicReview = false, publicReviewId = null)
                        _uiState.value = baseline.copy(message = "Registro guardado como privado.")
                    }
                    .onFailure {
                        baseline = baseline.copy(
                            publishing = false,
                            publicReview = wasPublicBeforeSave,
                            publicReviewId = if (wasPublicBeforeSave) previousPublicReviewId ?: reviewId else null
                        )
                        _uiState.value = baseline.copy(
                            message = if (wasPublicBeforeSave)
                                "El registro local se guardó, pero la reseña sigue pública porque no se pudo eliminar la copia de Firebase."
                            else
                                "Registro guardado, pero no se pudo confirmar la privacidad en Firebase."
                        )
                    }
            }
            return
        }

        baseline = normalized.copy(publicReviewId = null, publishing = true)
        _uiState.value = baseline.copy(message = "Registro guardado. Publicando reseña…")
        viewModelScope.launch {
            val media = _uiState.value.media ?: mediaRepository.getDetails(mediaId)?.media
            val profile = profileRepository.getProfile(userId).getOrNull()
            val review = PublicReview(
                id = reviewId,
                userId = userId,
                userDisplayName = profile?.displayName?.takeIf { it.isNotBlank() } ?: "Usuario CineTrack",
                userAvatarUrl = profile?.avatarUrl,
                mediaId = mediaId,
                mediaTitle = media?.title ?: "Título de CineTrack",
                rating = normalized.rating,
                comment = normalized.comment,
                createdAtEpochMillis = recordCreatedAt,
                updatedAtEpochMillis = now
            )
            reviewRepository.saveReview(review)
                .onSuccess {
                    baseline = baseline.copy(publishing = false, publicReview = true, publicReviewId = reviewId)
                    _uiState.value = baseline.copy(message = "Reseña pública y lista para compartir.")
                }
                .onFailure {
                    baseline = baseline.copy(
                        publishing = false,
                        publicReview = wasPublicBeforeSave,
                        publicReviewId = if (wasPublicBeforeSave) previousPublicReviewId ?: reviewId else null
                    )
                    _uiState.value = baseline.copy(
                        message = if (wasPublicBeforeSave)
                            "El registro local se guardó, pero Firebase no pudo actualizar la reseña pública anterior."
                        else
                            "El registro local se guardó, pero no se pudo publicar la reseña."
                    )
                }
        }
    }

    private fun mutate(change: ReviewUiState.() -> ReviewUiState) {
        val changed = _uiState.value.change().copy(saved = false, message = null)
        val hasChanges = changed.status != baseline.status ||
            changed.favorite != baseline.favorite ||
            changed.rating != baseline.rating ||
            changed.comment != baseline.comment ||
            changed.publicReview != baseline.publicReview
        _uiState.value = changed.copy(hasChanges = hasChanges)
    }

    companion object {
        fun factory(
            mediaId: String,
            userId: String,
            mediaRepository: MediaRepository,
            userMediaRepository: UserMediaRepository,
            reviewRepository: ReviewRepository,
            profileRepository: ProfileRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReviewViewModel(mediaId, userId, mediaRepository, userMediaRepository, reviewRepository, profileRepository) as T
        }
    }
}
