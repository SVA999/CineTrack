package com.cinetrack.app.data.model.social

data class PublicReview(
    val id: String = "",
    val userId: String = "",
    val userDisplayName: String = "",
    val userAvatarUrl: String? = null,
    val mediaId: String = "",
    val mediaTitle: String = "",
    val rating: Int? = null,
    val comment: String = "",
    val createdAtEpochMillis: Long = 0L,
    val updatedAtEpochMillis: Long = 0L
)
