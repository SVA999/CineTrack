package com.cinetrack.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.cinetrack.app.navigation.AppNavGraph
import com.cinetrack.app.ui.theme.CineTrackTheme
import com.cinetrack.app.viewmodel.AuthViewModel
import com.cinetrack.app.viewmodel.SettingsViewModel

@Composable
fun CineTrackApp() {
    val app = LocalContext.current.applicationContext as CineTrackApplication
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(app.container.authRepository))
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app.container.preferencesRepository))
    val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()

    CineTrackTheme(themePreference = preferences.theme) {
        // Consumes system/keyboard insets once; nested Scaffolds see only remaining insets.
        Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            AppNavGraph(
                navController = navController,
                container = app.container,
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
                preferences = preferences
            )
        }
    }
}
