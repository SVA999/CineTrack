package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.model.ContentPreference
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.MediaType
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.data.repository.PreferencesRepository
import com.cinetrack.app.util.normalizedForSearch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val selectedGenre: String = "Todos",
    val results: List<MediaTitle> = emptyList(),
    val showRatings: Boolean = true,
    val contentPreference: ContentPreference = ContentPreference.ALL,
    val loading: Boolean = false,
    val usingTmdb: Boolean = false,
    val error: String? = null
)

class SearchViewModel(
    private val repository: MediaRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SearchUiState(results = repository.getAll(), usingTmdb = repository.remoteEnabled)
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    private var remoteResults: List<MediaTitle>? = null

    init {
        viewModelScope.launch {
            repository.items.collect {
                if (_uiState.value.query.isBlank()) rebuildLocal()
            }
        }
        viewModelScope.launch {
            preferencesRepository.preferences.collect {
                _uiState.value = _uiState.value.copy(
                    showRatings = true,
                    contentPreference = ContentPreference.ALL
                )
                applyCurrentFilters()
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(query = value, error = null)
        searchJob?.cancel()
        if (!repository.remoteEnabled || value.trim().length < 2) {
            remoteResults = null
            rebuildLocal()
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.value = _uiState.value.copy(loading = true)
            remoteResults = repository.search(value)
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = repository.lastError.value
            )
            applyCurrentFilters()
        }
    }

    fun onGenreChange(value: String) {
        _uiState.value = _uiState.value.copy(selectedGenre = value)
        applyCurrentFilters()
    }

    fun clearSearch() {
        searchJob?.cancel()
        remoteResults = null
        _uiState.value = _uiState.value.copy(query = "", selectedGenre = "Todos", loading = false, error = null)
        rebuildLocal()
    }

    private fun rebuildLocal() {
        remoteResults = null
        applyCurrentFilters(repository.getAll())
    }

    private fun applyCurrentFilters(sourceOverride: List<MediaTitle>? = null) {
        val state = _uiState.value
        val source = sourceOverride ?: remoteResults ?: repository.getAll()
        val needle = state.query.normalizedForSearch()
        val results = source.filter { media ->
            val searchable = listOf(media.title, media.director.orEmpty(), media.genres.joinToString(" "))
                .joinToString(" ").normalizedForSearch()
            val matchesQuery = repository.remoteEnabled && remoteResults != null || needle.isBlank() || searchable.contains(needle)
            val matchesGenre = when (state.selectedGenre) {
                "Todos" -> true
                "Películas" -> media.type == MediaType.MOVIE
                "Series" -> media.type == MediaType.SERIES
                else -> media.genres.any { it.normalizedForSearch().contains(state.selectedGenre.normalizedForSearch()) }
            }
            val matchesType = when (state.contentPreference) {
                ContentPreference.ALL -> true
                ContentPreference.MOVIES -> media.type == MediaType.MOVIE
                ContentPreference.SERIES -> media.type == MediaType.SERIES
            }
            matchesQuery && matchesGenre && matchesType
        }
        _uiState.value = state.copy(results = results, usingTmdb = repository.remoteEnabled)
    }

    companion object {
        val genres = listOf("Todos", "Películas", "Series", "Acción", "Drama", "Ciencia ficción", "Comedia", "Terror")
        fun factory(repository: MediaRepository, preferences: PreferencesRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(repository, preferences) as T
        }
    }
}
