package com.cinetrack.app.data.model

/**
 * Relación privada entre un usuario autenticado y un título.
 *
 * [mediaSnapshot] contiene únicamente los metadatos mínimos necesarios para
 * reconstruir Mi lista sin depender de red/TMDB. No sustituye al catálogo remoto.
 */
data class UserMedia(
    val id: String,
    val userId: String,
    val mediaId: String,
    val status: WatchStatus = WatchStatus.PENDING,
    val personalRating: Int? = null,
    val comment: String = "",
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val mediaSnapshot: MediaTitle? = null
)
