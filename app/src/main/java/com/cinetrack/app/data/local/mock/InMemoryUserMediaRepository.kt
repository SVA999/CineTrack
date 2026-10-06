package com.cinetrack.app.data.local.mock

import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.repository.UserMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryUserMediaRepository : UserMediaRepository {
    private val _entries = MutableStateFlow<List<UserMedia>>(emptyList())
    override val entries: StateFlow<List<UserMedia>> = _entries.asStateFlow()

    override fun get(userId: String, mediaId: String): UserMedia? =
        _entries.value.firstOrNull { it.userId == userId && it.mediaId == mediaId }

    override fun upsert(entry: UserMedia) {
        val current = _entries.value.toMutableList()
        val index = current.indexOfFirst { it.userId == entry.userId && it.mediaId == entry.mediaId }
        if (index >= 0) current[index] = entry.copy(updatedAt = System.currentTimeMillis())
        else current += entry
        _entries.value = current
    }

    override fun remove(userId: String, mediaId: String) {
        _entries.value = _entries.value.filterNot { it.userId == userId && it.mediaId == mediaId }
    }

    override fun clearUser(userId: String) {
        _entries.value = _entries.value.filterNot { it.userId == userId }
    }
}
