package com.cinetrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cinetrack.app.data.local.database.UserMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMediaDao {
    @Query("SELECT * FROM user_media ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<UserMediaEntity>>

    @Query("SELECT * FROM user_media WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeForUser(userId: String): Flow<List<UserMediaEntity>>

    @Query("SELECT * FROM user_media WHERE userId = :userId AND mediaId = :mediaId LIMIT 1")
    suspend fun get(userId: String, mediaId: String): UserMediaEntity?

    @Upsert
    suspend fun upsert(entry: UserMediaEntity)

    @Query("DELETE FROM user_media WHERE userId = :userId AND mediaId = :mediaId")
    suspend fun remove(userId: String, mediaId: String)

    @Query("DELETE FROM user_media WHERE userId = :userId")
    suspend fun clearUser(userId: String)
}
