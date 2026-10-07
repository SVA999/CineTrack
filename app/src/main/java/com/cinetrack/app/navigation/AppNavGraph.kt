package com.cinetrack.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.cinetrack.app.BuildConfig
import com.cinetrack.app.data.model.UserPreferences
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.di.AppContainer
import com.cinetrack.app.share.ShareLinks
import com.cinetrack.app.ui.screens.auth.ForgotPasswordScreen
import com.cinetrack.app.ui.screens.auth.LoginScreen
import com.cinetrack.app.ui.screens.auth.RegisterScreen
import com.cinetrack.app.ui.screens.account.DeleteAccountScreen
import com.cinetrack.app.ui.screens.credits.CreditsScreen
import com.cinetrack.app.ui.screens.detail.DetailScreen
import com.cinetrack.app.ui.screens.explore.ExploreScreen
import com.cinetrack.app.ui.screens.mylist.MyListScreen
import com.cinetrack.app.ui.screens.profile.ProfileScreen
import com.cinetrack.app.ui.screens.profile.edit.EditProfileScreen
import com.cinetrack.app.ui.screens.profile.publicprofile.PublicProfileScreen
import com.cinetrack.app.ui.screens.review.ReviewScreen
import com.cinetrack.app.ui.screens.review.shared.SharedReviewScreen
import com.cinetrack.app.ui.screens.search.SearchScreen
import com.cinetrack.app.ui.screens.settings.SettingsScreen
import com.cinetrack.app.ui.screens.splash.SplashScreen
import com.cinetrack.app.viewmodel.AccountDeletionViewModel
import com.cinetrack.app.viewmodel.AuthViewModel
import com.cinetrack.app.viewmodel.DetailViewModel
import com.cinetrack.app.viewmodel.ExploreViewModel
import com.cinetrack.app.viewmodel.MyListViewModel
import com.cinetrack.app.viewmodel.ProfileViewModel
import com.cinetrack.app.viewmodel.PublicProfileViewModel
import com.cinetrack.app.viewmodel.ReviewViewModel
import com.cinetrack.app.viewmodel.SearchViewModel
import com.cinetrack.app.viewmodel.SettingsViewModel
import com.cinetrack.app.viewmodel.SharedReviewViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    container: AppContainer,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    preferences: UserPreferences
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingPrivateRoute by rememberSaveable { mutableStateOf<String?>(null) }

    fun goToLoginClearingPrivateStack() {
        navController.navigate(Routes.Login) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun navigateBack() {
        if (!navController.navigateUp()) {
            navController.navigate(if (container.authRepository.currentUser != null) Routes.Explore else Routes.Login) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun navigateBottom(route: String) {
        if (navController.currentDestination?.route == route) return
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Routes.Explore) { saveState = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable(Routes.Splash) {
            SplashScreen()
            LaunchedEffect(authState.authenticated) {
                when (authState.authenticated) {
                    true -> navController.navigate(Routes.Explore) { popUpTo(Routes.Splash) { inclusive = true } }
                    false -> navController.navigate(Routes.Login) { popUpTo(Routes.Splash) { inclusive = true } }
                    null -> Unit
                }
            }
        }

        composable(Routes.Login) {
            LaunchedEffect(authState.authenticated) {
                if (authState.authenticated == true) {
                    val target = pendingPrivateRoute ?: Routes.Explore
                    pendingPrivateRoute = null
                    navController.navigate(target) { popUpTo(Routes.Login) { inclusive = true } }
                }
            }
            LoginScreen(
                loading = authState.loading,
                error = authState.error,
                message = authState.message,
                firebaseConfigured = authState.firebaseConfigured,
                onLogin = authViewModel::signIn,
                onRegister = { authViewModel.clearFeedback(); navController.navigate(Routes.Register) },
                onForgotPassword = { authViewModel.clearFeedback(); navController.navigate(Routes.ForgotPassword) },
                onEdit = authViewModel::clearFeedback
            )
        }

        composable(Routes.ForgotPassword) {
            ForgotPasswordScreen(
                loading = authState.loading,
                error = authState.error,
                message = authState.message,
                firebaseConfigured = authState.firebaseConfigured,
                onBack = { authViewModel.clearFeedback(); navigateBack() },
                onSend = authViewModel::resetPassword,
                onEdit = authViewModel::clearFeedback
            )
        }

        composable(Routes.Register) {
            LaunchedEffect(authState.authenticated) {
                if (authState.authenticated == true) {
                    navController.navigate(Routes.Explore) { popUpTo(Routes.Login) { inclusive = true } }
                }
            }
            RegisterScreen(
                loading = authState.loading,
                error = authState.error,
                onRegister = authViewModel::signUp,
                onBackToLogin = { authViewModel.clearFeedback(); navigateBack() },
                onEdit = authViewModel::clearFeedback
            )
        }

        composable(Routes.Explore) {
            val user = container.authRepository.currentUser
            if (user == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else {
                val vm: ExploreViewModel = viewModel(factory = ExploreViewModel.factory(container.mediaRepository, user.uid, container.userMediaRepository, container.preferencesRepository))
                val state by vm.uiState.collectAsStateWithLifecycle()
                ExploreScreen(
                    currentRoute = Routes.Explore,
                    displayName = user.displayName,
                    trending = state.trending,
                    recentlyAdded = state.recentlyAdded,
                    showRatings = state.showRatings,
                    loading = state.loading,
                    error = state.error,
                    usingTmdb = state.usingTmdb,
                    onRetry = vm::refresh,
                    onNavigateBottom = ::navigateBottom,
                    onOpenProfile = { navigateBottom(Routes.Profile) },
                    onOpenSearch = { navigateBottom(Routes.Search) },
                    onOpenDetail = { navController.navigate(Routes.detail(it)) }
                )
            }
        }

        composable(Routes.Search) {
            if (container.authRepository.currentUser == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else {
                val vm: SearchViewModel = viewModel(factory = SearchViewModel.factory(container.mediaRepository, container.preferencesRepository))
                val state by vm.uiState.collectAsStateWithLifecycle()
                SearchScreen(
                    currentRoute = Routes.Search,
                    displayName = container.authRepository.currentUser?.displayName,
                    query = state.query,
                    selectedGenre = state.selectedGenre,
                    genres = SearchViewModel.genres,
                    results = state.results,
                    showRatings = state.showRatings,
                    loading = state.loading,
                    error = state.error,
                    usingTmdb = state.usingTmdb,
                    onQueryChange = vm::onQueryChange,
                    onGenreChange = vm::onGenreChange,
                    onClear = vm::clearSearch,
                    onNavigateBottom = ::navigateBottom,
                    onOpenProfile = { navigateBottom(Routes.Profile) },
                    onOpenDetail = { navController.navigate(Routes.detail(it)) }
                )
            }
        }

        composable(Routes.MyList) {
            val user = container.authRepository.currentUser
            if (user == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else {
                val vm: MyListViewModel = viewModel(factory = MyListViewModel.factory(user.uid, container.mediaRepository, container.userMediaRepository))
                val state by vm.uiState.collectAsStateWithLifecycle()
                MyListScreen(
                    currentRoute = Routes.MyList,
                    selectedFilter = state.selectedFilter,
                    filters = MyListViewModel.filters,
                    items = state.items,
                    pendingCount = state.pendingCount,
                    watchingCount = state.watchingCount,
                    watchedCount = state.watchedCount,
                    showRatings = true,
                    onSetFilter = vm::setFilter,
                    onNavigateBottom = ::navigateBottom,
                    onOpenProfile = { navigateBottom(Routes.Profile) },
                    onOpenDetail = { navController.navigate(Routes.detail(it)) },
                    onToggleFavorite = vm::toggleFavorite,
                    onSetStatus = vm::setStatus,
                    onRemove = vm::remove,
                    onExplore = { navigateBottom(Routes.Explore) }
                )
            }
        }

        composable(route = Routes.Detail, arguments = listOf(navArgument("mediaId") { type = NavType.StringType })) { backStackEntry ->
            val user = container.authRepository.currentUser
            if (user == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else {
                val mediaId = backStackEntry.arguments?.getString("mediaId").orEmpty()
                val vm: DetailViewModel = viewModel(factory = DetailViewModel.factory(mediaId, user.uid, container.mediaRepository, container.userMediaRepository))
                val state by vm.uiState.collectAsStateWithLifecycle()
                DetailScreen(
                    media = state.media,
                    details = state.details,
                    related = state.related,
                    saved = state.saved,
                    status = state.status,
                    favorite = state.favorite,
                    personalRating = state.personalRating,
                    personalComment = state.comment,
                    showRatings = true,
                    loading = state.loading,
                    error = state.error,
                    onBack = { navigateBack() },
                    onAdd = vm::addToList,
                    onRemove = vm::removeFromList,
                    onStatus = vm::setStatus,
                    onFavorite = vm::toggleFavorite,
                    onSetRating = vm::setRating,
                    onReview = { navController.navigate(Routes.review(mediaId)) },
                    onShare = {
                        state.media?.let { media ->
                            ShareLinks.share(context, media.title, "Mira ${media.title} en CineTrack", ShareLinks.media(media))
                        }
                    },
                    onOpenRelated = { navController.navigate(Routes.detail(it)) },
                    onRetry = vm::retry
                )
            }
        }

        composable(route = Routes.Review, arguments = listOf(navArgument("mediaId") { type = NavType.StringType })) { backStackEntry ->
            val user = container.authRepository.currentUser
            if (user == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else {
                val mediaId = backStackEntry.arguments?.getString("mediaId").orEmpty()
                val vm: ReviewViewModel = viewModel(
                    factory = ReviewViewModel.factory(
                        mediaId,
                        user.uid,
                        container.mediaRepository,
                        container.userMediaRepository,
                        container.reviewRepository,
                        container.profileRepository
                    )
                )
                val state by vm.uiState.collectAsStateWithLifecycle()
                ReviewScreen(
                    media = state.media,
                    status = state.status,
                    favorite = state.favorite,
                    rating = state.rating,
                    comment = state.comment,
                    hasChanges = state.hasChanges,
                    publicReview = state.publicReview,
                    firebaseConfigured = container.reviewRepository.isConfigured,
                    loadingPublicState = state.loadingPublicState,
                    publishing = state.publishing,
                    publicReviewId = state.publicReviewId,
                    message = state.message,
                    onBack = { navigateBack() },
                    onStatus = vm::setStatus,
                    onFavorite = vm::setFavorite,
                    onRating = vm::setRating,
                    onComment = vm::setComment,
                    onPublicReview = vm::setPublicReview,
                    onSave = vm::save,
                    onShare = { reviewId ->
                        ShareLinks.share(context, "Reseña en CineTrack", "Mira mi reseña en CineTrack", ShareLinks.review(reviewId))
                    }
                )
            }
        }

        composable(Routes.Profile) {
            val user = container.authRepository.currentUser
            if (user == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else {
                val entries by container.userMediaRepository.entries.collectAsStateWithLifecycle()
                val mine = entries.filter { it.userId == user.uid }
                val vm: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.factory(user, container.profileRepository, container.reviewRepository, container.mediaRepository, container.userMediaRepository, container.localAvatarStore)
                )
                val profileState by vm.uiState.collectAsStateWithLifecycle()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.load() }
                ProfileScreen(
                    currentRoute = Routes.Profile,
                    user = user,
                    profile = profileState.profile?.let { it.copy(avatarUrl = profileState.localAvatarUrl ?: it.avatarUrl) },
                    watched = mine.count { it.status == WatchStatus.WATCHED },
                    pending = mine.count { it.status == WatchStatus.PENDING },
                    favorites = mine.count { it.favorite },
                    firebaseConfigured = profileState.firebaseConfigured,
                    loading = profileState.loading,
                    error = profileState.error,
                    onRetry = vm::load,
                    onEdit = { navController.navigate(Routes.EditProfile) },
                    onShare = {
                        ShareLinks.share(context, "Perfil de ${profileState.profile?.displayName ?: user.displayName ?: "CineTrack"}", "Mira mi perfil en CineTrack", ShareLinks.profile(user.uid))
                    },
                    onSettings = { navController.navigate(Routes.Settings) },
                    onCredits = { navController.navigate(Routes.Credits) },
                    onNavigateBottom = ::navigateBottom,
                    onLogout = { authViewModel.signOut(); goToLoginClearingPrivateStack() }
                )
            }
        }

        composable(Routes.EditProfile) {
            val user = container.authRepository.currentUser
            if (user == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else {
                val vm: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.factory(user, container.profileRepository, container.reviewRepository, container.mediaRepository, container.userMediaRepository, container.localAvatarStore)
                )
                val state by vm.uiState.collectAsStateWithLifecycle()
                EditProfileScreen(
                    profile = state.profile?.let { it.copy(avatarUrl = state.localAvatarUrl ?: it.avatarUrl) },
                    availableFavorites = state.availableFavorites,
                    saving = state.saving,
                    firebaseConfigured = state.firebaseConfigured,
                    message = state.message,
                    error = state.error,
                    onBack = { navigateBack() },
                    avatarUploadEnabled = true,
                    onSave = vm::save
                )
            }
        }

        composable(
            route = Routes.PublicProfile,
            arguments = listOf(navArgument("userId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "${BuildConfig.CINETRACK_WEB_BASE_URL}/profile/{userId}" })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            val vm: PublicProfileViewModel = viewModel(factory = PublicProfileViewModel.factory(userId, container.profileRepository, container.reviewRepository, container.mediaRepository))
            val state by vm.uiState.collectAsStateWithLifecycle()
            PublicProfileScreen(
                profile = state.profile,
                favoriteMedia = state.favoriteMedia,
                recentReviews = state.recentReviews,
                loading = state.loading,
                error = state.error,
                onBack = { navigateBack() },
                onShare = { ShareLinks.share(context, "Perfil en CineTrack", "Mira este perfil en CineTrack", ShareLinks.profile(userId)) },
                onOpenMedia = { mediaId ->
                    if (container.authRepository.currentUser != null) {
                        navController.navigate(Routes.detail(mediaId))
                    } else {
                        pendingPrivateRoute = Routes.detail(mediaId)
                        navController.navigate(Routes.Login)
                    }
                },
                onOpenReview = { navController.navigate(Routes.sharedReview(it)) }
            )
        }

        composable(
            route = Routes.SharedReview,
            arguments = listOf(navArgument("reviewId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "${BuildConfig.CINETRACK_WEB_BASE_URL}/review/{reviewId}" })
        ) { backStackEntry ->
            val reviewId = backStackEntry.arguments?.getString("reviewId").orEmpty()
            val vm: SharedReviewViewModel = viewModel(factory = SharedReviewViewModel.factory(reviewId, container.reviewRepository, container.mediaRepository))
            val state by vm.uiState.collectAsStateWithLifecycle()
            SharedReviewScreen(
                review = state.review,
                media = state.media,
                loading = state.loading,
                error = state.error,
                onBack = { navigateBack() },
                onOpenProfile = { navController.navigate(Routes.publicProfile(it)) },
                onOpenMedia = { mediaId ->
                    if (container.authRepository.currentUser != null) {
                        navController.navigate(Routes.detail(mediaId))
                    } else {
                        pendingPrivateRoute = Routes.detail(mediaId)
                        navController.navigate(Routes.Login)
                    }
                }
            )
        }

        composable(
            route = Routes.SharedMedia,
            arguments = listOf(
                navArgument("mediaType") { type = NavType.StringType },
                navArgument("tmdbId") { type = NavType.IntType }
            ),
            deepLinks = listOf(navDeepLink { uriPattern = "${BuildConfig.CINETRACK_WEB_BASE_URL}/media/{mediaType}/{tmdbId}" })
        ) { backStackEntry ->
            val mediaType = backStackEntry.arguments?.getString("mediaType").orEmpty()
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
            val mediaId = "$mediaType:$tmdbId"
            LaunchedEffect(mediaId, authState.authenticated) {
                if (authState.authenticated == true) {
                    navController.navigate(Routes.detail(mediaId)) { popUpTo(Routes.SharedMedia) { inclusive = true } }
                } else if (authState.authenticated == false) {
                    pendingPrivateRoute = Routes.detail(mediaId)
                    navController.navigate(Routes.Login) { popUpTo(Routes.SharedMedia) { inclusive = true } }
                }
            }
            SplashScreen()
        }

        composable(Routes.Settings) {
            if (container.authRepository.currentUser == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else {
                val user = container.authRepository.currentUser!!
                val profileVm: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.factory(user, container.profileRepository, container.reviewRepository, container.mediaRepository, container.userMediaRepository, container.localAvatarStore)
                )
                val profileState by profileVm.uiState.collectAsStateWithLifecycle()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { profileVm.load() }
                SettingsScreen(
                    preferences = preferences,
                    displayName = profileState.profile?.displayName ?: user.displayName ?: "Usuario CineTrack",
                    email = user.email.orEmpty(),
                    avatarUrl = profileState.localAvatarUrl ?: profileState.profile?.avatarUrl,
                    onBack = { navigateBack() },
                    onTheme = settingsViewModel::setTheme,
                    onEditProfile = { navController.navigate(Routes.EditProfile) },
                    onChangePassword = { authViewModel.clearFeedback(); navController.navigate(Routes.ForgotPassword) },
                    onCredits = { navController.navigate(Routes.Credits) },
                    onDeleteAccount = { navController.navigate(Routes.DeleteAccount) },
                    onLogout = { authViewModel.signOut(); goToLoginClearingPrivateStack() }
                )
            }
        }

        composable(Routes.DeleteAccount) {
            if (container.authRepository.currentUser == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else {
                val vm: AccountDeletionViewModel = viewModel(
                    factory = AccountDeletionViewModel.factory(
                        container.authRepository,
                        container.profileRepository,
                        container.reviewRepository,
                        container.userMediaRepository,
                        container.localAvatarStore
                    )
                )
                val state by vm.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.deleted) {
                    if (state.deleted) {
                        authViewModel.checkSession()
                        goToLoginClearingPrivateStack()
                    }
                }
                DeleteAccountScreen(
                    deleting = state.deleting,
                    error = state.error,
                    onBack = { navigateBack() },
                    onDelete = vm::deleteAccount,
                    onClearError = vm::clearError
                )
            }
        }

        composable(Routes.Credits) {
            if (container.authRepository.currentUser == null) {
                LaunchedEffect(Unit) { goToLoginClearingPrivateStack() }
            } else CreditsScreen(onBack = { navigateBack() })
        }
    }
}





