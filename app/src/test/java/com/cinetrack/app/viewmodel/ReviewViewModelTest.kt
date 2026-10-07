package com.cinetrack.app.viewmodel

import com.cinetrack.app.data.local.mock.InMemoryUserMediaRepository
import com.cinetrack.app.data.local.mock.MockMediaRepository
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.model.social.UserProfile
import com.cinetrack.app.data.repository.social.ProfileRepository
import com.cinetrack.app.data.repository.social.ReviewRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Before
import org.junit.After
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {
    @Before fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }
    @Test
    fun draftDoesNotPersistUntilSave() {
        val list = InMemoryUserMediaRepository()
        val vm = ReviewViewModel(
            mediaId = "orbita-cero",
            userId = "user",
            mediaRepository = MockMediaRepository(),
            userMediaRepository = list,
            reviewRepository = NoOpReviewRepository,
            profileRepository = NoOpProfileRepository
        )

        vm.setStatus(WatchStatus.WATCHED)
        vm.setRating(5)
        vm.setComment("Excelente")

        assertNull(list.get("user", "orbita-cero"))
        assertTrue(vm.uiState.value.hasChanges)

        vm.save()

        val saved = list.get("user", "orbita-cero")
        assertEquals(WatchStatus.WATCHED, saved?.status)
        assertEquals(5, saved?.personalRating)
        assertEquals("Excelente", saved?.comment)
    }

    private object NoOpProfileRepository : ProfileRepository {
        override val isConfigured = false
        override suspend fun getProfile(uid: String) = Result.success<UserProfile?>(null)
        override suspend fun saveProfile(profile: UserProfile) = Result.success(Unit)
        override suspend fun uploadAvatar(uid: String, sourceUri: String) = Result.failure<String>(IllegalStateException("disabled"))
        override suspend fun deleteProfile(uid: String) = Result.success(Unit)
    }

    private object NoOpReviewRepository : ReviewRepository {
        override val isConfigured = false
        override suspend fun getReview(reviewId: String) = Result.success<PublicReview?>(null)
        override suspend fun saveReview(review: PublicReview) = Result.success(Unit)
        override suspend fun deleteReview(reviewId: String) = Result.success(Unit)
        override suspend fun getReviewsForUser(uid: String) = Result.success(emptyList<PublicReview>())
        override suspend fun getReviewsForMedia(mediaId: String) = Result.success(emptyList<PublicReview>())
        override suspend fun deleteReviewsForUser(uid: String) = Result.success(Unit)
    }
}

