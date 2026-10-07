package com.cinetrack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cinetrack.app.data.model.ContentPreference
import com.cinetrack.app.data.model.ThemePreference
import com.cinetrack.app.data.model.UserPreferences
import com.cinetrack.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: PreferencesRepository) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = repository.preferences.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserPreferences()
    )

    fun setTheme(value: ThemePreference) = viewModelScope.launch { repository.setTheme(value) }
    fun setShowRatings(value: Boolean) = viewModelScope.launch { repository.setShowRatings(value) }
    fun setContentPreference(value: ContentPreference) = viewModelScope.launch { repository.setContentPreference(value) }
    fun reset() = viewModelScope.launch { repository.reset() }

    companion object {
        fun factory(repository: PreferencesRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(repository) as T
        }
    }
}
