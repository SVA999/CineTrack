package com.cinetrack.app.data

import com.cinetrack.app.data.local.database.toDomain
import com.cinetrack.app.data.local.database.toEntity
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.MediaType
import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.model.WatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UserMediaEntityMappingTest {
    @Test
    fun roomMapping_preservesPrivateRecordAndOfflineSnapshot() {
        val media = MediaTitle(
            id = "movie:157336",
            title = "Interstellar",
            type = MediaType.MOVIE,
            year = 2014,
            genres = listOf("Ciencia ficción", "Drama"),
            durationMinutes = 169,
            synopsis = "Exploración espacial.",
            generalRating = 4.4,
            ratingCount = 100,
            director = "Christopher Nolan",
            posterUrl = "https://image.tmdb.org/poster.jpg",
            tmdbId = 157336
        )
        val entry = UserMedia(
            id = "user:movie:157336",
            userId = "user",
            mediaId = media.id,
            status = WatchStatus.WATCHED,
            personalRating = 5,
            comment = "Gran película",
            favorite = true,
            createdAt = 10L,
            updatedAt = 20L,
            mediaSnapshot = media
        )

        val restored = entry.toEntity().toDomain()

        assertEquals(WatchStatus.WATCHED, restored.status)
        assertEquals(5, restored.personalRating)
        assertEquals("Gran película", restored.comment)
        assertEquals(true, restored.favorite)
        assertNotNull(restored.mediaSnapshot)
        assertEquals("Interstellar", restored.mediaSnapshot?.title)
        assertEquals(listOf("Ciencia ficción", "Drama"), restored.mediaSnapshot?.genres)
        assertEquals(157336, restored.mediaSnapshot?.tmdbId)
    }
}
