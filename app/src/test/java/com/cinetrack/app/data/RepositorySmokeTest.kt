package com.cinetrack.app.data

import com.cinetrack.app.data.local.mock.InMemoryUserMediaRepository
import com.cinetrack.app.data.local.mock.MockMediaRepository
import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.model.WatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RepositorySmokeTest {
    @Test
    fun mockMedia_containsExpectedFeaturedTitle() {
        val repository = MockMediaRepository()
        assertNotNull(repository.getById("orbita-cero"))
        assertEquals(10, repository.getAll().size)
    }

    @Test
    fun userMedia_upsertReplacesExistingEntry() {
        val repository = InMemoryUserMediaRepository()
        repository.upsert(UserMedia("u:m", "u", "m"))
        repository.upsert(UserMedia("u:m", "u", "m", status = WatchStatus.WATCHED, favorite = true))

        val saved = repository.get("u", "m")
        assertEquals(1, repository.entries.value.size)
        assertEquals(WatchStatus.WATCHED, saved?.status)
        assertEquals(true, saved?.favorite)
    }

    @Test
    fun userMedia_clearUserOnlyRemovesSelectedAccount() {
        val repository = InMemoryUserMediaRepository()
        repository.upsert(UserMedia("u1:m1", "u1", "m1"))
        repository.upsert(UserMedia("u2:m2", "u2", "m2"))

        repository.clearUser("u1")

        assertEquals(null, repository.get("u1", "m1"))
        assertNotNull(repository.get("u2", "m2"))
    }
}
