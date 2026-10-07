package com.cinetrack.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cinetrack.app.data.local.mock.MockMediaRepository
import com.cinetrack.app.data.model.AppUser
import com.cinetrack.app.data.model.UserPreferences
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.model.social.UserProfile
import com.cinetrack.app.ui.screens.auth.ForgotPasswordScreen
import com.cinetrack.app.ui.screens.auth.LoginScreen
import com.cinetrack.app.ui.screens.auth.RegisterScreen
import com.cinetrack.app.ui.screens.credits.CreditsScreen
import com.cinetrack.app.ui.screens.profile.ProfileScreen
import com.cinetrack.app.ui.screens.profile.publicprofile.PublicProfileScreen
import com.cinetrack.app.ui.screens.review.ReviewScreen
import com.cinetrack.app.ui.screens.settings.SettingsScreen
import com.cinetrack.app.ui.screens.splash.SplashScreen
import com.cinetrack.app.ui.theme.CineTrackTheme

private val extendedPreviewMedia = MockMediaRepository().getAll()
private val previewProfile = UserProfile(
    uid = "preview-user",
    displayName = "Laura Gómez",
    gender = "Mujer",
    favoriteMediaIds = extendedPreviewMedia.take(3).map { it.id }
)
private val previewReview = PublicReview(
    id = "preview-review",
    userId = "preview-user",
    userDisplayName = "Laura Gómez",
    mediaId = extendedPreviewMedia.first().id,
    mediaTitle = extendedPreviewMedia.first().title,
    rating = 5,
    comment = "Una historia muy bien construida. La volvería a ver por la atmósfera y el ritmo."
)

@Preview(name = "Login", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun LoginPreview() = CineTrackTheme {
    LoginScreen(
        loading = false,
        error = null,
        message = null,
        firebaseConfigured = true,
        onLogin = { _, _ -> },
        onRegister = {},
        onForgotPassword = {},
        onEdit = {}
    )
}

@Preview(name = "Recuperar contraseña", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ForgotPasswordPreview() = CineTrackTheme {
    ForgotPasswordScreen(
        loading = false,
        error = null,
        message = null,
        firebaseConfigured = true,
        onBack = {},
        onSend = {},
        onEdit = {}
    )
}

@Preview(name = "Registro", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun RegisterPreview() = CineTrackTheme {
    RegisterScreen(
        loading = false,
        error = null,
        onRegister = { _, _, _, _ -> },
        onBackToLogin = {},
        onEdit = {}
    )
}

@Preview(name = "Perfil", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ProfilePreview() = CineTrackTheme {
    ProfileScreen(
        currentRoute = com.cinetrack.app.navigation.Routes.Profile,
        user = AppUser("preview-user", "Laura Gómez", "laura@example.com"),
        profile = previewProfile,
        watched = 28,
        pending = 11,
        favorites = 7,
        firebaseConfigured = true,
        loading = false,
        error = null,
        onRetry = {},
        onEdit = {},
        onShare = {},
        onSettings = {},
        onCredits = {},
        onNavigateBottom = {},
        onLogout = {}
    )
}

@Preview(name = "Perfil público", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun PublicProfilePreview() = CineTrackTheme {
    PublicProfileScreen(
        profile = previewProfile,
        favoriteMedia = extendedPreviewMedia.take(3),
        recentReviews = listOf(previewReview),
        loading = false,
        error = null,
        onBack = {},
        onShare = {},
        onOpenMedia = {},
        onOpenReview = {}
    )
}

@Preview(name = "Mi registro", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ReviewPreview() = CineTrackTheme {
    ReviewScreen(
        media = extendedPreviewMedia.first(),
        status = WatchStatus.WATCHED,
        favorite = true,
        rating = 5,
        comment = "Me gustó mucho la atmósfera y la fotografía.",
        hasChanges = true,
        publicReview = true,
        firebaseConfigured = true,
        loadingPublicState = false,
        publishing = false,
        publicReviewId = "preview-review",
        message = null,
        onBack = {},
        onStatus = {},
        onFavorite = {},
        onRating = {},
        onComment = {},
        onPublicReview = {},
        onSave = {},
        onShare = {}
    )
}

@Preview(name = "Configuración", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun SettingsPreview() = CineTrackTheme {
    SettingsScreen(
        preferences = UserPreferences(),
        displayName = "Laura Gómez",
        email = "laura@example.com",
        avatarUrl = null,
        onBack = {},
        onTheme = {},
        onEditProfile = {},
        onChangePassword = {},
        onCredits = {},
        onDeleteAccount = {},
        onLogout = {}
    )
}

@Preview(name = "Créditos", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun CreditsPreview() = CineTrackTheme { CreditsScreen(onBack = {}) }

@Preview(name = "Splash", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun SplashPreview() = CineTrackTheme { SplashScreen() }
