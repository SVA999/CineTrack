package com.cinetrack.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// Figma is inconsistent in one older profile mockup (it shows "Actividad"),
// but the Home/Search screens and the actual implemented information architecture
// consistently support these four primary destinations.
val bottomNavItems = listOf(
    BottomNavItem(Routes.Explore, "Inicio", Icons.Outlined.Home),
    BottomNavItem(Routes.Search, "Buscar", Icons.Outlined.Search),
    BottomNavItem(Routes.MyList, "Mi lista", Icons.Outlined.VideoLibrary),
    BottomNavItem(Routes.Profile, "Perfil", Icons.Outlined.Person)
)
