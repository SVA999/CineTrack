package com.cinetrack.app.data.model.social

/** Public-facing profile data. Email is deliberately excluded. */
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val gender: String = "Sin especificar",
    val favoriteMediaIds: List<String> = emptyList(),
    val updatedAtEpochMillis: Long = 0L
)
