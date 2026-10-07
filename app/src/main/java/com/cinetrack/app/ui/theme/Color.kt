package com.cinetrack.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Palette aligned with the approved CineTrack Figma screens.
val CineBackground = Color(0xFF0F0F16)
val CineSurface = Color(0xFF15151F)
val CineSurfaceAlt = Color(0xFF1C1C28)
val CineSurfaceRaised = Color(0xFF232231)
val CinePrimary = Color(0xFF8B6CFF)
val CinePrimarySoft = Color(0xFFAC98FF)
// Backwards-compatible aliases used by older components.
val CineCoral = CinePrimary
val CineCoralSoft = CinePrimarySoft
val CineText = Color(0xFFF7F5FF)
val CineTextSecondary = Color(0xFF9996A8)
val CineBorder = Color(0xFF2A2938)
val CineRating = Color(0xFFFFB84D)
val CineSuccess = Color(0xFF22D7A3)
val CineInfo = Color(0xFF8C7CFF)
val CineDanger = Color(0xFFFF536A)

val CineLightBackground = Color(0xFFF7F7FB)
val CineLightSurface = Color(0xFFFFFFFF)
val CineLightSurfaceAlt = Color(0xFFF0EFF7)
val CineLightSurfaceRaised = Color(0xFFE9E7F2)
val CineLightText = Color(0xFF17151F)
val CineLightTextSecondary = Color(0xFF666272)
val CineLightBorder = Color(0xFFDCD9E6)

// Semantic accents on theme surfaces; artwork badges retain their fixed gold.
val CineRatingContent: Color
    @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) CineRating else Color(0xFF805000)
val CineSuccessContent: Color
    @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) CineSuccess else Color(0xFF006B50)
val CineInfoContent: Color
    @Composable get() = MaterialTheme.colorScheme.primary
