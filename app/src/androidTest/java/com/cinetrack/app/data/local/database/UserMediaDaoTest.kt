package com.cinetrack.app.data.local.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserMediaDaoTest {
    private lateinit var database: CineTrackDatabase

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, CineTrackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun upsertObserveAndRemove_persistPrivateEntry() = runBlocking {
        val dao = database.userMediaDao()
        val row = UserMediaEntity(
            id = "u:movie:1",
            userId = "u",
            mediaId = "movie:1",
            status = "WATCHED",
            personalRating = 4,
            comment = "Buena",
            favorite = true,
            createdAt = 1L,
            updatedAt = 2L,
            mediaTitle = "Prueba",
            mediaType = "MOVIE",
            mediaYear = 2026,
            mediaGenres = "Drama",
            mediaDurationMinutes = 100,
            mediaSynopsis = "Sinopsis",
            mediaGeneralRating = 4.0,
            mediaRatingCount = 5,
            mediaAgeRating = null,
            mediaDirector = "Directora",
            mediaPosterUrl = null,
            mediaBackdropUrl = null,
            mediaTmdbId = 1
        )

        dao.upsert(row)
        assertEquals(row, dao.get("u", "movie:1"))
        assertEquals(1, dao.observeForUser("u").first().size)

        dao.remove("u", "movie:1")
        assertNull(dao.get("u", "movie:1"))
    }

    @Test
    fun clearUser_onlyDeletesRowsForThatUser() = runBlocking {
        val dao = database.userMediaDao()
        fun row(id: String, userId: String, mediaId: String) = UserMediaEntity(
            id = id, userId = userId, mediaId = mediaId, status = "PENDING",
            personalRating = null, comment = "", favorite = false, createdAt = 1L, updatedAt = 1L,
            mediaTitle = mediaId, mediaType = "MOVIE", mediaYear = 2026, mediaGenres = "",
            mediaDurationMinutes = null, mediaSynopsis = "", mediaGeneralRating = null, mediaRatingCount = null,
            mediaAgeRating = null, mediaDirector = null, mediaPosterUrl = null, mediaBackdropUrl = null, mediaTmdbId = null
        )
        dao.upsert(row("u1:m1", "u1", "m1"))
        dao.upsert(row("u2:m2", "u2", "m2"))

        dao.clearUser("u1")

        assertNull(dao.get("u1", "m1"))
        assertEquals("m2", dao.get("u2", "m2")?.mediaId)
    }
}
