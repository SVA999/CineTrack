package com.cinetrack.app.data.remote.firebase.social

import android.content.Context
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.repository.social.ReviewRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirestoreReviewRepository(
    private val context: Context
) : ReviewRepository {
    override val isConfigured: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    private val firestore: FirebaseFirestore?
        get() = if (isConfigured) FirebaseFirestore.getInstance() else null

    override suspend fun getReview(reviewId: String): Result<PublicReview?> {
        val db = firestore ?: return Result.failure(configurationError())
        return suspendCancellableCoroutine { continuation ->
            db.collection("reviews").document(reviewId).get()
                .addOnSuccessListener { snapshot ->
                    if (continuation.isActive) continuation.resume(Result.success(snapshot.toObject(PublicReview::class.java)))
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(error))
                }
        }
    }

    override suspend fun saveReview(review: PublicReview): Result<Unit> {
        val db = firestore ?: return Result.failure(configurationError())
        val now = System.currentTimeMillis()
        val normalized = review.copy(
            createdAtEpochMillis = review.createdAtEpochMillis.takeIf { it > 0 } ?: now,
            updatedAtEpochMillis = now
        )
        return suspendCancellableCoroutine { continuation ->
            db.collection("reviews").document(review.id).set(normalized)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(error))
                }
        }
    }


    override suspend fun deleteReview(reviewId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(configurationError())
        return suspendCancellableCoroutine { continuation ->
            val reference = db.collection("reviews").document(reviewId)
            // Read and delete atomically: an absent public copy already satisfies
            // the requested private state. Never swallow permission/network errors.
            db.runTransaction { transaction ->
                if (transaction.get(reference).exists()) {
                    transaction.delete(reference)
                }
                Unit
            }
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(error))
                }
        }
    }

    override suspend fun getReviewsForUser(uid: String): Result<List<PublicReview>> =
        queryReviews("userId", uid)

    override suspend fun getReviewsForMedia(mediaId: String): Result<List<PublicReview>> =
        queryReviews("mediaId", mediaId)

    private suspend fun queryReviews(field: String, value: String): Result<List<PublicReview>> {
        val db = firestore ?: return Result.failure(configurationError())
        return suspendCancellableCoroutine { continuation ->
            db.collection("reviews")
                .whereEqualTo(field, value)
                .orderBy("updatedAtEpochMillis", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    continuation.resume(Result.success(snapshot.documents.mapNotNull { it.toObject(PublicReview::class.java) }))
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(error))
                }
        }
    }


    override suspend fun deleteReviewsForUser(uid: String): Result<Unit> {
        val db = firestore ?: return Result.failure(configurationError())
        return suspendCancellableCoroutine { continuation ->
            db.collection("reviews")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    val documents = snapshot.documents
                    if (documents.isEmpty()) {
                        continuation.resume(Result.success(Unit))
                        return@addOnSuccessListener
                    }
                    fun commitChunk(start: Int) {
                        if (!continuation.isActive) return
                        if (start >= documents.size) {
                            continuation.resume(Result.success(Unit))
                            return
                        }
                        val batch = db.batch()
                        documents.drop(start).take(450).forEach { batch.delete(it.reference) }
                        batch.commit()
                            .addOnSuccessListener { commitChunk(start + 450) }
                            .addOnFailureListener { error ->
                                if (continuation.isActive) continuation.resume(Result.failure(error))
                            }
                    }
                    commitChunk(0)
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(error))
                }
        }
    }

    private fun configurationError() = IllegalStateException(
        "Firebase no está conectado. Agrega google-services.json y habilita Firestore."
    )
}

