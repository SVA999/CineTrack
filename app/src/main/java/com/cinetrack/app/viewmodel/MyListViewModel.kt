package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.data.repository.UserMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class MyListItem(val media: MediaTitle, val entry: UserMedia)

data class MyListUiState(
    val selectedFilter: String = "Todos",
    val items: List<MyListItem> = emptyList(),
    val pendingCount: Int = 0,
    val watchingCount: Int = 0,
    val watchedCount: Int = 0
)

class MyListViewModel(
    private val userId: String,
    private val mediaRepository: MediaRepository,
    private val repository: UserMediaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyListUiState())
    val uiState: StateFlow<MyListUiState> = _uiState.asStateFlow()
    private var latestEntries: List<UserMedia> = emptyList()

    init {
        viewModelScope.launch {
            repository.entries.collect { entries ->
                latestEntries = entries.filter { it.userId == userId }
                rebuild()
            }
        }
        viewModelScope.launch {
            mediaRepository.items.collect { rebuild() }
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        rebuild()
    }

    fun remove(mediaId: String) = repository.remove(userId, mediaId)

    fun toggleFavorite(mediaId: String) {
        repository.get(userId, mediaId)?.let { repository.upsert(it.copy(favorite = !it.favorite)) }
    }

    fun setStatus(mediaId: String, status: WatchStatus) {
        repository.get(userId, mediaId)?.let { repository.upsert(it.copy(status = status)) }
    }

    private fun rebuild() {
        val filter = _uiState.value.selectedFilter
        val filtered = latestEntries.filter { entry ->
            when (filter) {
                "Pendientes" -> entry.status == WatchStatus.PENDING
                "Viendo" -> entry.status == WatchStatus.WATCHING
                "Vistas" -> entry.status == WatchStatus.WATCHED
                else -> true
            }
        }
        _uiState.value = MyListUiState(
            selectedFilter = filter,
            items = filtered.mapNotNull { entry ->
                (mediaRepository.getById(entry.mediaId) ?: entry.mediaSnapshot)?.let { MyListItem(it, entry) }
            },
            pendingCount = latestEntries.count { it.status == WatchStatus.PENDING },
            watchingCount = latestEntries.count { it.status == WatchStatus.WATCHING },
            watchedCount = latestEntries.count { it.status == WatchStatus.WATCHED }
        )
    }

    companion object {
        val filters = listOf("Todos", "Pendientes", "Viendo", "Vistas")
        fun factory(userId: String, media: MediaRepository, list: UserMediaRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MyListViewModel(userId, media, list) as T
            }
    }
}
