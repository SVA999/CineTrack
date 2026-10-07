package com.cinetrack.app.data.remote

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.cinetrack.app.data.local.LocalAvatarStore
import com.cinetrack.app.data.local.mock.InMemoryUserMediaRepository
import com.cinetrack.app.data.local.mock.MockMediaRepository
import com.cinetrack.app.data.model.AppUser
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.model.social.UserProfile
import com.cinetrack.app.data.repository.social.ProfileRepository
import com.cinetrack.app.data.repository.social.ReviewRepository
import com.cinetrack.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

class LocalAvatarProfileTest {
    @Test fun photoOnlySaveNeverPublishesAndProfileEditsNeverSendLocalPath() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uid = "avatar-vm-${UUID.randomUUID()}"
        val local = LocalAvatarStore(context)
        val file = File(context.cacheDir, "$uid.png")
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        var profileLoads = 0
        val published = mutableListOf<UserProfile>()
        val profiles = object : ProfileRepository {
            override val isConfigured = true
            override suspend fun getProfile(uid: String): Result<UserProfile?> {
                profileLoads++
                return Result.success(UserProfile(uid = uid, displayName = "Local"))
            }
            override suspend fun saveProfile(profile: UserProfile): Result<Unit> { published.add(profile); return Result.success(Unit) }
            override suspend fun uploadAvatar(uid: String, sourceUri: String): Result<String> = error("A local photo must never be uploaded")
            override suspend fun deleteProfile(uid: String) = Result.success(Unit)
        }
        val reviews = object : ReviewRepository {
            override val isConfigured = false
            override suspend fun getReview(reviewId: String) = Result.success<PublicReview?>(null)
            override suspend fun saveReview(review: PublicReview) = Result.success(Unit)
            override suspend fun deleteReview(reviewId: String) = Result.success(Unit)
            override suspend fun getReviewsForUser(uid: String) = Result.success(emptyList<PublicReview>())
            override suspend fun getReviewsForMedia(mediaId: String) = Result.success(emptyList<PublicReview>())
            override suspend fun deleteReviewsForUser(uid: String) = Result.success(Unit)
        }
        val holder = ViewModelStore()
        try {
            val vm = withContext(Dispatchers.Main) {
                ProfileViewModel(AppUser(uid = uid, displayName = "Local", email = null), profiles, reviews,
                    MockMediaRepository(), local, InMemoryUserMediaRepository()).also { holder.put("test", it); it.load(); it.load() }
            }
            withTimeout(10000) { vm.uiState.first { it.profile != null && !it.loading } }
            assertEquals("Concurrent reloads must be ignored", 1, profileLoads)
            val initial = vm.uiState.value.profile!!
            withContext(Dispatchers.Main) { vm.save(initial.displayName, initial.gender, initial.favoriteMediaIds, Uri.fromFile(file).toString()) }
            withTimeout(10000) { vm.uiState.first { !it.saving && it.message != null } }
            assertTrue(published.isEmpty())
            assertNotNull(vm.uiState.value.localAvatarUrl)
            assertNull(vm.uiState.value.profile!!.avatarUrl)
            withContext(Dispatchers.Main) { vm.save("Changed", initial.gender, initial.favoriteMediaIds, null) }
            withTimeout(10000) { vm.uiState.first { !it.saving && it.profile?.displayName == "Changed" } }
            assertEquals(1, published.size)
            assertNull(published.single().avatarUrl)
            assertNotNull(local.get(uid))
        } finally {
            withContext(Dispatchers.Main) { holder.clear() }
            local.delete(uid)
            file.delete()
        }
    }
}

