package com.cinetrack.app.data.repository.social

import com.cinetrack.app.data.model.social.UserProfile

interface ProfileRepository {
    val isConfigured: Boolean
    suspend fun getProfile(uid: String): Result<UserProfile?>
    suspend fun saveProfile(profile: UserProfile): Result<Unit>
    suspend fun uploadAvatar(uid: String, sourceUri: String): Result<String>
    suspend fun deleteProfile(uid: String): Result<Unit>
}
