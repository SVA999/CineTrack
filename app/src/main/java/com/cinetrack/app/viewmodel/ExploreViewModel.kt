package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.model.ContentPreference
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.MediaType
import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.data.repository.PreferencesRepository
import com.cinetrack.app.data.repository.UserMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class ExploreUiState(
    val featured: MediaTitle? = null,
    val trending: List<MediaTitle> = emptyList(),
    val recentlyAdded: List<MediaTitle> = emptyList(),
    val featuredSaved: Boolean = false,
    val showRatings: Boolean = true,
    val loading: Boolean = false,
    val usingTmdb: Boolean = false,
    val error: String? = null
)

class ExploreViewModel(
    private val repository: MediaRepository,
    private val userId: String,
    private val userMediaRepository: UserMediaRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExploreUiState(usingTmdb = repository.remoteEnabled))
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var preference = ContentPreference.ALL
    private var showRatings = true

    init {
        rebuild()
        viewModelScope.launch {
            repository.items.collect { rebuild() }
        }
        viewModelScope.launch {
            repository.lastError.collect { message ->
                _uiState.value = _uiState.value.copy(error = message)
            }
        }
        viewModelScope.launch {
            preferencesRepository.preferences.collect {
                // El Figma definitivo siempre muestra ratings y filtra contenido desde Inicio/Buscar.
                preference = ContentPreference.ALL
                showRatings = true
                rebuild()
            }
        }
        viewModelScope.launch {
            userMediaRepository.entries.collect { rebuild() }
        }
        if (repository.remoteEnabled) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            repository.refresh()
            _uiState.value = _uiState.value.copy(loading = false, error = repository.lastError.value)
            rebuild()
        }
    }

    fun addToList(mediaId: String) {
        if (userMediaRepository.get(userId, mediaId) == null) {
            userMediaRepository.upsert(
                UserMedia(
                    id = "$userId:$mediaId",
                    userId = userId,
                    mediaId = mediaId,
                    mediaSnapshot = repository.getById(mediaId)
                )
            )
        }
    }

    private fun rebuild() {
        val all = repository.getAll().filter { media ->
            when (preference) {
                ContentPreference.ALL -> true
                ContentPreference.MOVIES -> media.type == MediaType.MOVIE
                ContentPreference.SERIES -> media.type == MediaType.SERIES
            }
        }
        val featured = all.firstOrNull()
        _uiState.value = _uiState.value.copy(
            featured = featured,
            trending = all.sortedByDescending { it.generalRating ?: 0.0 }.take(10),
            recentlyAdded = all.sortedByDescending { it.year }.take(8),
            featuredSaved = featured?.let { userMediaRepository.get(userId, it.id) != null } ?: false,
            showRatings = showRatings,
            usingTmdb = repository.remoteEnabled
        )
    }

    companion object {
        fun factory(
            repository: MediaRepository,
            userId: String,
            list: UserMediaRepository,
            preferences: PreferencesRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ExploreViewModel(repository, userId, list, preferences) as T
        }
    }
}
