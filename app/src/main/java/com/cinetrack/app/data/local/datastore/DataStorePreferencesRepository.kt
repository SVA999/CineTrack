package com.cinetrack.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cinetrack.app.data.model.ContentPreference
import com.cinetrack.app.data.model.ThemePreference
import com.cinetrack.app.data.model.UserPreferences
import com.cinetrack.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.cineTrackDataStore by preferencesDataStore(name = "cinetrack_preferences")

class DataStorePreferencesRepository(private val context: Context) : PreferencesRepository {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val showRatings = booleanPreferencesKey("show_ratings")
        val contentPreference = stringPreferencesKey("content_preference")
    }

    override val preferences: Flow<UserPreferences> = context.cineTrackDataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw throwable
        }
        .map { prefs ->
            UserPreferences(
                theme = prefs[Keys.theme]?.let { value ->
                    ThemePreference.entries.firstOrNull { it.name == value }
                } ?: ThemePreference.DARK,
                showRatings = prefs[Keys.showRatings] ?: true,
                contentPreference = prefs[Keys.contentPreference]?.let { value ->
                    ContentPreference.entries.firstOrNull { it.name == value }
                } ?: ContentPreference.ALL
            )
        }

    override suspend fun setTheme(value: ThemePreference) {
        context.cineTrackDataStore.edit { it[Keys.theme] = value.name }
    }

    override suspend fun setShowRatings(value: Boolean) {
        context.cineTrackDataStore.edit { it[Keys.showRatings] = value }
    }

    override suspend fun setContentPreference(value: ContentPreference) {
        context.cineTrackDataStore.edit { it[Keys.contentPreference] = value.name }
    }

    override suspend fun reset() {
        context.cineTrackDataStore.edit { it.clear() }
    }
}
