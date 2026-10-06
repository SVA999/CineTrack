package com.cinetrack.app.data.repository

import com.cinetrack.app.data.model.UserMedia
import kotlinx.coroutines.flow.StateFlow

/**
 * Fuente privada de Mi lista. En la app real está respaldada por Room; la
 * implementación en memoria se conserva únicamente para pruebas/previews.
 */
interface UserMediaRepository {
    val entries: StateFlow<List<UserMedia>>
    fun get(userId: String, mediaId: String): UserMedia?
    fun upsert(entry: UserMedia)
    fun remove(userId: String, mediaId: String)
    fun clearUser(userId: String)
}
