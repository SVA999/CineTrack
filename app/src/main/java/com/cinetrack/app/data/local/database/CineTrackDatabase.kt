package com.cinetrack.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cinetrack.app.data.local.dao.UserMediaDao

@Database(
    entities = [UserMediaEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CineTrackDatabase : RoomDatabase() {
    abstract fun userMediaDao(): UserMediaDao

    companion object {
        @Volatile
        private var instance: CineTrackDatabase? = null

        fun getInstance(context: Context): CineTrackDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CineTrackDatabase::class.java,
                    "cinetrack.db"
                ).build().also { instance = it }
            }
    }
}
