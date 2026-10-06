package com.cinetrack.app.data.local.room

import com.cinetrack.app.data.local.dao.UserMediaDao
import com.cinetrack.app.data.local.database.toDomain
import com.cinetrack.app.data.local.database.toEntity
import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.repository.UserMediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Implementación persistente de la colección privada del usuario.
 *
 * Mantiene un StateFlow en memoria para que la UI existente sea reactiva y
 * ejecuta todas las escrituras de SQLite fuera del hilo principal.
 */
class RoomUserMediaRepository(
    private val dao: UserMediaDao,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : UserMediaRepository {
    private val _entries = MutableStateFlow<List<UserMedia>>(emptyList())
    override val entries: StateFlow<List<UserMedia>> = _entries.asStateFlow()

    init {
        scope.launch {
            dao.observeAll().collectLatest { rows ->
                _entries.value = rows.map { it.toDomain() }
            }
        }
    }

    override fun get(userId: String, mediaId: String): UserMedia? =
        _entries.value.firstOrNull { it.userId == userId && it.mediaId == mediaId }

    override fun upsert(entry: UserMedia) {
        val now = System.currentTimeMillis()
        val normalized = entry.copy(
            personalRating = entry.personalRating?.coerceIn(1, 5),
            comment = entry.comment.take(500),
            updatedAt = now
        )
        // Actualización optimista: la UI responde inmediatamente. Room vuelve a
        // emitir la fuente de verdad cuando termina la escritura.
        _entries.value = _entries.value.toMutableList().apply {
            val index = indexOfFirst { it.userId == normalized.userId && it.mediaId == normalized.mediaId }
            if (index >= 0) set(index, normalized) else add(0, normalized)
        }
        scope.launch { dao.upsert(normalized.toEntity()) }
    }

    override fun remove(userId: String, mediaId: String) {
        _entries.value = _entries.value.filterNot { it.userId == userId && it.mediaId == mediaId }
        scope.launch { dao.remove(userId, mediaId) }
    }

    override fun clearUser(userId: String) {
        _entries.value = _entries.value.filterNot { it.userId == userId }
        scope.launch { dao.clearUser(userId) }
    }
}
