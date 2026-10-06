package com.cinetrack.app.data.repository.social

import com.cinetrack.app.data.model.social.PublicReview

interface ReviewRepository {
    val isConfigured: Boolean
    suspend fun getReview(reviewId: String): Result<PublicReview?>
    suspend fun saveReview(review: PublicReview): Result<Unit>
    suspend fun deleteReview(reviewId: String): Result<Unit>
    suspend fun getReviewsForUser(uid: String): Result<List<PublicReview>>
    suspend fun getReviewsForMedia(mediaId: String): Result<List<PublicReview>>
    suspend fun deleteReviewsForUser(uid: String): Result<Unit>
}
