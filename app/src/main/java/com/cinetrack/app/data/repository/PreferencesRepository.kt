package com.cinetrack.app.data.repository

import com.cinetrack.app.data.model.ContentPreference
import com.cinetrack.app.data.model.ThemePreference
import com.cinetrack.app.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setTheme(value: ThemePreference)
    suspend fun setShowRatings(value: Boolean)
    suspend fun setContentPreference(value: ContentPreference)
    suspend fun reset()
}
