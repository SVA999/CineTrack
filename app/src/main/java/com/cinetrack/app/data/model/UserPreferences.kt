package com.cinetrack.app.data.model

data class UserPreferences(
    val theme: ThemePreference = ThemePreference.DARK,
    val showRatings: Boolean = true,
    val contentPreference: ContentPreference = ContentPreference.ALL
)

enum class ThemePreference(val label: String) {
    DARK("Oscuro"),
    LIGHT("Claro"),
    SYSTEM("Sistema")
}

enum class ContentPreference(val label: String) {
    ALL("Películas y series"),
    MOVIES("Solo películas"),
    SERIES("Solo series")
}
