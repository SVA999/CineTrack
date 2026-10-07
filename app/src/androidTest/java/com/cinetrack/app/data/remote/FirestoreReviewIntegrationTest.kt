package com.cinetrack.app.data.remote

import androidx.test.platform.app.InstrumentationRegistry
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.remote.firebase.social.FirestoreReviewRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Opt-in live check: use an isolated test device. Deletes only resources it creates. */
class FirestoreReviewIntegrationTest {
    @Test fun deletingMissingOrAlreadyDeletedReviewSucceeds() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("firebaseLiveChecks") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)
        assertEquals("cinetrack-9df50", FirebaseApp.getInstance().options.projectId)
        val auth = FirebaseAuth.getInstance()

        val repository = FirestoreReviewRepository(context)
        auth.currentUser?.let { previous ->
            assumeTrue("Do not replace a real user's session", previous.email?.matches(
                Regex("cinetrack-owner-[a-f0-9-]+@example\\.com")) == true)
            repository.deleteReviewsForUser(previous.uid).getOrThrow()
            Tasks.await(previous.delete(), 30, TimeUnit.SECONDS)
            auth.signOut()
        }
        val unique = UUID.randomUUID().toString()
        val ownerEmail = "cinetrack-owner-$unique@example.com"
        val otherEmail = "cinetrack-other-$unique@example.com"
        val password = UUID.randomUUID().toString() + "Aa1!"
        var ownerCreated = false
        var otherCreated = false
        var reviewId: String? = null
        fun await(task: com.google.android.gms.tasks.Task<*>) { Tasks.await(task, 30, TimeUnit.SECONDS) }
        try {
            await(auth.createUserWithEmailAndPassword(ownerEmail, password))
            ownerCreated = true
            val owner = requireNotNull(auth.currentUser).uid
            val ownReviewId = "${owner}_integration_$unique"
            reviewId = ownReviewId
            assertTrue(repository.deleteReview(ownReviewId).isSuccess)
            val review = PublicReview(
                id = ownReviewId, userId = owner, userDisplayName = "Prueba temporal",
                mediaId = "audit:$unique", mediaTitle = "Integración temporal", rating = 4,
                comment = "Se elimina al finalizar la prueba"
            )
            assertTrue(repository.saveReview(review).isSuccess)
            assertTrue(repository.getReviewsForUser(owner).getOrThrow().any { it.id == ownReviewId })
            assertTrue(repository.getReviewsForMedia(review.mediaId).getOrThrow().any { it.id == ownReviewId })
            assertTrue(repository.deleteReview(ownReviewId).isSuccess)
            assertNull(repository.getReview(ownReviewId).getOrThrow())
            assertTrue(repository.deleteReview(ownReviewId).isSuccess)
            assertTrue(repository.saveReview(review).isSuccess)
            assertTrue(repository.getReviewsForUser(owner).getOrThrow().any { it.id == ownReviewId })
            assertTrue(repository.getReviewsForMedia(review.mediaId).getOrThrow().any { it.id == ownReviewId })
        } finally {
            if (ownerCreated) {
                if (auth.currentUser?.email != ownerEmail) await(auth.signInWithEmailAndPassword(ownerEmail, password))
                reviewId?.let { repository.deleteReview(it).getOrThrow() }
                await(requireNotNull(auth.currentUser).delete())
            }
            auth.signOut()
        }
    }
}



