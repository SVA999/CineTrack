package com.cinetrack.app.data.model

data class MediaTitle(
    val id: String,
    val title: String,
    val type: MediaType,
    val year: Int,
    val genres: List<String>,
    val durationMinutes: Int? = null,
    val synopsis: String,
    /** CineTrack normalizes public ratings to a 0–5 scale. */
    val generalRating: Double? = null,
    val ratingCount: Int? = null,
    val ageRating: String? = null,
    val director: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val tmdbId: Int? = null
)
