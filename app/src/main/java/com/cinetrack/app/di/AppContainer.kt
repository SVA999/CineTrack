package com.cinetrack.app.di

import android.content.Context
import com.cinetrack.app.BuildConfig
import com.cinetrack.app.data.local.datastore.DataStorePreferencesRepository
import com.cinetrack.app.data.local.database.CineTrackDatabase
import com.cinetrack.app.data.local.room.RoomUserMediaRepository
import com.cinetrack.app.data.local.mock.MockMediaRepository
import com.cinetrack.app.data.remote.firebase.FirebaseAuthRepository
import com.cinetrack.app.data.remote.firebase.social.FirestoreProfileRepository
import com.cinetrack.app.data.remote.firebase.social.FirestoreReviewRepository
import com.cinetrack.app.data.remote.tmdb.TmdbClient
import com.cinetrack.app.data.remote.tmdb.TmdbMediaRepository
import com.cinetrack.app.data.repository.AuthRepository
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.data.repository.PreferencesRepository
import com.cinetrack.app.data.repository.UserMediaRepository
import com.cinetrack.app.data.repository.social.ProfileRepository
import com.cinetrack.app.data.repository.social.ReviewRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val mockMediaRepository = MockMediaRepository()

    val authRepository: AuthRepository = FirebaseAuthRepository(appContext)

    /**
     * TMDB activates automatically when TMDB_READ_TOKEN exists in local.properties.
     * Until then, all catalog screens continue to work with the local mock catalog.
     */
    val mediaRepository: MediaRepository = if (BuildConfig.TMDB_READ_TOKEN.isNotBlank()) {
        TmdbMediaRepository(
            api = TmdbClient.create(BuildConfig.TMDB_READ_TOKEN),
            fallback = mockMediaRepository
        )
    } else {
        mockMediaRepository
    }

    private val database = CineTrackDatabase.getInstance(appContext)
    val userMediaRepository: UserMediaRepository = RoomUserMediaRepository(database.userMediaDao())
    val preferencesRepository: PreferencesRepository = DataStorePreferencesRepository(appContext)

    val localAvatarStore = com.cinetrack.app.data.local.LocalAvatarStore(appContext)
    val profileRepository: ProfileRepository = FirestoreProfileRepository(appContext)
    val reviewRepository: ReviewRepository = FirestoreReviewRepository(appContext)
}

