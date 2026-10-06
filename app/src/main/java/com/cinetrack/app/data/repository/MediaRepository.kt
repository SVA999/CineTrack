package com.cinetrack.app.data.repository

import com.cinetrack.app.data.model.MediaDetails
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.RelatedMedia
import kotlinx.coroutines.flow.StateFlow

/**
 * Catalog abstraction. The synchronous getters always return the current cache,
 * while the suspend functions may refresh/search TMDB when remote access is enabled.
 */
interface MediaRepository {
    val items: StateFlow<List<MediaTitle>>
    val remoteEnabled: Boolean
    val lastError: StateFlow<String?>

    fun getAll(): List<MediaTitle>
    fun getById(id: String): MediaTitle?

    suspend fun refresh()
    suspend fun search(query: String): List<MediaTitle>
    suspend fun getDetails(id: String): MediaDetails?
    suspend fun getRelated(id: String): List<RelatedMedia>
}
