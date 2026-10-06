package com.cinetrack.app.data.local.database

import androidx.room.Entity
import androidx.room.Index
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.MediaType
import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.model.WatchStatus

/**
 * Tabla local privada. Se usa una PK compuesta para aislar correctamente el
 * registro de un mismo título entre diferentes cuentas que usen el dispositivo.
 */
@Entity(
    tableName = "user_media",
    primaryKeys = ["userId", "mediaId"],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "favorite"]),
        Index(value = ["userId", "status"])
    ]
)
data class UserMediaEntity(
    val id: String,
    val userId: String,
    val mediaId: String,
    val status: String,
    val personalRating: Int?,
    val comment: String,
    val favorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,

    // Snapshot mínimo del título para Mi lista/offline.
    val mediaTitle: String?,
    val mediaType: String?,
    val mediaYear: Int?,
    val mediaGenres: String?,
    val mediaDurationMinutes: Int?,
    val mediaSynopsis: String?,
    val mediaGeneralRating: Double?,
    val mediaRatingCount: Int?,
    val mediaAgeRating: String?,
    val mediaDirector: String?,
    val mediaPosterUrl: String?,
    val mediaBackdropUrl: String?,
    val mediaTmdbId: Int?
)

private const val GENRE_SEPARATOR = "\u001F"

fun UserMediaEntity.toDomain(): UserMedia {
    val snapshot = if (!mediaTitle.isNullOrBlank() && !mediaType.isNullOrBlank() && mediaYear != null) {
        val type = runCatching { MediaType.valueOf(mediaType!!) }.getOrNull()
        type?.let {
            MediaTitle(
                id = mediaId,
                title = mediaTitle,
                type = it,
                year = mediaYear,
                genres = mediaGenres.orEmpty().split(GENRE_SEPARATOR).filter(String::isNotBlank),
                durationMinutes = mediaDurationMinutes,
                synopsis = mediaSynopsis.orEmpty(),
                generalRating = mediaGeneralRating,
                ratingCount = mediaRatingCount,
                ageRating = mediaAgeRating,
                director = mediaDirector,
                posterUrl = mediaPosterUrl,
                backdropUrl = mediaBackdropUrl,
                tmdbId = mediaTmdbId
            )
        }
    } else null

    return UserMedia(
        id = id,
        userId = userId,
        mediaId = mediaId,
        status = runCatching { WatchStatus.valueOf(status) }.getOrDefault(WatchStatus.PENDING),
        personalRating = personalRating?.coerceIn(1, 5),
        comment = comment,
        favorite = favorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
        mediaSnapshot = snapshot
    )
}

fun UserMedia.toEntity(): UserMediaEntity {
    val media = mediaSnapshot
    return UserMediaEntity(
        id = id,
        userId = userId,
        mediaId = mediaId,
        status = status.name,
        personalRating = personalRating?.coerceIn(1, 5),
        comment = comment.take(500),
        favorite = favorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
        mediaTitle = media?.title,
        mediaType = media?.type?.name,
        mediaYear = media?.year,
        mediaGenres = media?.genres?.joinToString(GENRE_SEPARATOR),
        mediaDurationMinutes = media?.durationMinutes,
        mediaSynopsis = media?.synopsis,
        mediaGeneralRating = media?.generalRating,
        mediaRatingCount = media?.ratingCount,
        mediaAgeRating = media?.ageRating,
        mediaDirector = media?.director,
        mediaPosterUrl = media?.posterUrl,
        mediaBackdropUrl = media?.backdropUrl,
        mediaTmdbId = media?.tmdbId
    )
}
