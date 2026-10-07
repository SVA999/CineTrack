package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.model.MediaDetails
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.RelatedMedia
import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.data.repository.UserMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class DetailUiState(
    val media: MediaTitle? = null,
    val details: MediaDetails? = null,
    val related: List<RelatedMedia> = emptyList(),
    val saved: Boolean = false,
    val status: WatchStatus = WatchStatus.PENDING,
    val favorite: Boolean = false,
    val personalRating: Int? = null,
    val comment: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

class DetailViewModel(
    private val mediaId: String,
    private val userId: String,
    private val mediaRepository: MediaRepository,
    private val userMediaRepository: UserMediaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(readState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userMediaRepository.entries.collect { refreshLocalState() }
        }
        viewModelScope.launch {
            mediaRepository.items.collect { refreshLocalState() }
        }
        loadDetails()
    }

    fun retry() = loadDetails()

    fun addToList() {
        val existing = currentEntry()
        userMediaRepository.upsert(existing ?: newEntry())
        refreshLocalState()
    }

    fun removeFromList() {
        userMediaRepository.remove(userId, mediaId)
        refreshLocalState()
    }

    fun setStatus(status: WatchStatus) = updateEntry { it.copy(status = status) }
    fun toggleFavorite() = updateEntry { it.copy(favorite = !it.favorite) }
    fun setRating(rating: Int?) = updateEntry { it.copy(personalRating = rating?.coerceIn(1, 5)) }
    fun setComment(comment: String) { _uiState.value = _uiState.value.copy(comment = comment) }

    fun saveReview() = updateEntry {
        it.copy(
            status = _uiState.value.status,
            favorite = _uiState.value.favorite,
            personalRating = _uiState.value.personalRating,
            comment = _uiState.value.comment.trim()
        )
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val details = mediaRepository.getDetails(mediaId)
            _uiState.value = _uiState.value.copy(
                media = details?.media ?: mediaRepository.getById(mediaId) ?: _uiState.value.media,
                details = details,
                related = details?.related ?: mediaRepository.getRelated(mediaId),
                loading = false,
                error = mediaRepository.lastError.value
            )
        }
    }

    private fun updateEntry(change: (UserMedia) -> UserMedia) {
        val state = _uiState.value
        val base = (currentEntry() ?: newEntry()).copy(
            status = state.status,
            favorite = state.favorite,
            personalRating = state.personalRating,
            comment = state.comment,
            mediaSnapshot = state.media ?: currentEntry()?.mediaSnapshot
        )
        userMediaRepository.upsert(change(base))
        refreshLocalState()
    }

    private fun currentEntry() = userMediaRepository.get(userId, mediaId)

    private fun newEntry() = UserMedia(
        id = "$userId:$mediaId",
        userId = userId,
        mediaId = mediaId,
        mediaSnapshot = _uiState.value.media ?: mediaRepository.getById(mediaId)
    )

    private fun refreshLocalState() {
        val current = _uiState.value
        val entry = currentEntry()
        _uiState.value = current.copy(
            media = mediaRepository.getById(mediaId) ?: entry?.mediaSnapshot ?: current.media,
            saved = entry != null,
            status = entry?.status ?: WatchStatus.PENDING,
            favorite = entry?.favorite ?: false,
            personalRating = entry?.personalRating,
            comment = entry?.comment.orEmpty()
        )
    }

    private fun readState(): DetailUiState {
        val entry = currentEntry()
        return DetailUiState(
            media = mediaRepository.getById(mediaId) ?: entry?.mediaSnapshot,
            saved = entry != null,
            status = entry?.status ?: WatchStatus.PENDING,
            favorite = entry?.favorite ?: false,
            personalRating = entry?.personalRating,
            comment = entry?.comment.orEmpty()
        )
    }

    companion object {
        fun factory(
            mediaId: String,
            userId: String,
            mediaRepository: MediaRepository,
            userMediaRepository: UserMediaRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DetailViewModel(mediaId, userId, mediaRepository, userMediaRepository) as T
        }
    }
}
