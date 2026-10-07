package com.cinetrack.app.ui

import androidx.compose.runtime.Composable

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

@Composable
internal fun LoginPreview() = CineTrackTheme(themePreference = testTheme) {
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

@Composable
internal fun ForgotPasswordPreview() = CineTrackTheme(themePreference = testTheme) {
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

@Composable
internal fun RegisterPreview() = CineTrackTheme(themePreference = testTheme) {
    RegisterScreen(
        loading = false,
        error = null,
        onRegister = { _, _, _, _ -> },
        onBackToLogin = {},
        onEdit = {}
    )
}

@Composable
internal fun ProfilePreview() = CineTrackTheme(themePreference = testTheme) {
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

@Composable
internal fun PublicProfilePreview() = CineTrackTheme(themePreference = testTheme) {
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

@Composable
internal fun ReviewPreview() = CineTrackTheme(themePreference = testTheme) {
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

@Composable
internal fun SettingsPreview() = CineTrackTheme(themePreference = testTheme) {
    SettingsScreen(
        preferences = UserPreferences(theme = testTheme),
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

@Composable
internal fun CreditsPreview() = CineTrackTheme(themePreference = testTheme) { CreditsScreen(onBack = {}) }

@Composable
internal fun SplashPreview() = CineTrackTheme(themePreference = testTheme) { SplashScreen() }


@Composable
internal fun EditProfilePreview() = CineTrackTheme(themePreference = testTheme) {
    com.cinetrack.app.ui.screens.profile.edit.EditProfileScreen(
        profile = previewProfile, availableFavorites = extendedPreviewMedia.take(3),
        saving = false, firebaseConfigured = true, avatarUploadEnabled = true,
        message = null, error = null, onBack = {}, onSave = { _, _, _, _ -> }
    )
}

@Composable
internal fun DeleteAccountPreview() = CineTrackTheme(themePreference = testTheme) {
    com.cinetrack.app.ui.screens.account.DeleteAccountScreen(
        deleting = false, error = null, onBack = {}, onDelete = {}, onClearError = {}
    )
}

@Composable
internal fun SharedReviewPreview() = CineTrackTheme(themePreference = testTheme) {
    com.cinetrack.app.ui.screens.review.shared.SharedReviewScreen(
        review = previewReview, media = extendedPreviewMedia.first(), loading = false,
        error = null, onBack = {}, onOpenProfile = {}, onOpenMedia = {}
    )
}


