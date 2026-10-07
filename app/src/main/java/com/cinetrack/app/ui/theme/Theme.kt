package com.cinetrack.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.cinetrack.app.data.model.ThemePreference

internal val DarkColors = darkColorScheme(
    primary = CinePrimarySoft,
    onPrimary = Color(0xFF24134D),
    primaryContainer = Color(0xFF443071),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = CinePrimarySoft,
    onSecondary = Color(0xFF291C46),
    secondaryContainer = Color(0xFF39304D),
    onSecondaryContainer = Color(0xFFEADDFF),
    background = CineBackground,
    onBackground = CineText,
    surface = CineSurface,
    onSurface = CineText,
    surfaceVariant = CineSurfaceAlt,
    onSurfaceVariant = CineTextSecondary,
    surfaceDim = CineBackground,
    surfaceBright = CineSurfaceRaised,
    surfaceContainerLowest = CineBackground,
    surfaceContainerLow = CineSurface,
    surfaceContainer = CineSurfaceAlt,
    surfaceContainerHigh = CineSurfaceRaised,
    surfaceContainerHighest = Color(0xFF302E40),
    outline = Color(0xFF797586),
    outlineVariant = CineBorder,
    error = Color(0xFFFFB3BC),
    onError = Color(0xFF650020),
    errorContainer = Color(0xFF8D0032),
    onErrorContainer = Color(0xFFFFD9DF)
)

internal val LightColors = lightColorScheme(
    primary = Color(0xFF6344C5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF24134D),
    secondary = Color(0xFF62517F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADDFF),
    onSecondaryContainer = Color(0xFF291C46),
    background = CineLightBackground,
    onBackground = CineLightText,
    surface = CineLightSurface,
    onSurface = CineLightText,
    surfaceVariant = CineLightSurfaceAlt,
    onSurfaceVariant = CineLightTextSecondary,
    surfaceDim = CineLightSurfaceRaised,
    surfaceBright = CineLightSurface,
    surfaceContainerLowest = CineLightSurface,
    surfaceContainerLow = CineLightBackground,
    surfaceContainer = CineLightSurfaceAlt,
    surfaceContainerHigh = CineLightSurfaceRaised,
    surfaceContainerHighest = Color(0xFFE2DEEC),
    outline = Color(0xFF797384),
    outlineVariant = CineLightBorder,
    error = Color(0xFFB51E40),
    onError = Color.White,
    errorContainer = Color(0xFFFFD9DF),
    onErrorContainer = Color(0xFF400014)
)

@Composable
fun CineTrackTheme(
    themePreference: ThemePreference = ThemePreference.DARK,
    content: @Composable () -> Unit
) {
    val dark = when (themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
        }
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, typography = CineTypography) {
        // MaterialTheme alone does not provide a background or LocalContentColor.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            content = content
        )
    }
}
