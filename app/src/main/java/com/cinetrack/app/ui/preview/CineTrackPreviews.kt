package com.cinetrack.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cinetrack.app.data.local.mock.MockMediaRepository
import com.cinetrack.app.data.model.UserMedia
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.navigation.Routes
import com.cinetrack.app.ui.screens.detail.DetailScreen
import com.cinetrack.app.ui.screens.explore.ExploreScreen
import com.cinetrack.app.ui.screens.mylist.MyListScreen
import com.cinetrack.app.ui.screens.search.SearchScreen
import com.cinetrack.app.ui.theme.CineTrackTheme
import com.cinetrack.app.viewmodel.MyListItem
import com.cinetrack.app.viewmodel.MyListViewModel
import com.cinetrack.app.viewmodel.SearchViewModel

private val previewMedia = MockMediaRepository().getAll()

@Preview(name = "Explorar", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ExplorePreview() {
    CineTrackTheme {
        ExploreScreen(
            currentRoute = Routes.Explore,
            displayName = "Laura Gómez",
            trending = previewMedia.take(5),
            recentlyAdded = previewMedia.drop(5).take(5),
            showRatings = true,
            loading = false,
            error = null,
            usingTmdb = true,
            onRetry = {},
            onNavigateBottom = {},
            onOpenProfile = {},
            onOpenSearch = {},
            onOpenDetail = {}
        )
    }
}

@Preview(name = "Buscar", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun SearchPreview() {
    CineTrackTheme {
        SearchScreen(
            currentRoute = Routes.Search,
            displayName = "Laura Gómez",
            query = "ciencia",
            selectedGenre = "Todos",
            genres = SearchViewModel.genres,
            results = previewMedia.take(6),
            showRatings = true,
            loading = false,
            error = null,
            usingTmdb = true,
            onQueryChange = {},
            onGenreChange = {},
            onClear = {},
            onNavigateBottom = {},
            onOpenProfile = {},
            onOpenDetail = {}
        )
    }
}

@Preview(name = "Detalle", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun DetailPreview() {
    CineTrackTheme {
        DetailScreen(
            media = previewMedia.first(),
            details = null,
            related = emptyList(),
            saved = true,
            status = WatchStatus.WATCHING,
            favorite = true,
            personalRating = 4,
            personalComment = "Visualmente increíble y con una atmósfera que vale la pena revisar.",
            showRatings = true,
            loading = false,
            error = null,
            onBack = {},
            onAdd = {},
            onRemove = {},
            onStatus = {},
            onFavorite = {},
            onSetRating = {},
            onReview = {},
            onShare = {},
            onOpenRelated = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Mi lista", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun MyListPreview() {
    val entries = previewMedia.take(4).mapIndexed { index, media ->
        MyListItem(
            media = media,
            entry = UserMedia(
                id = "preview-$index",
                userId = "preview-user",
                mediaId = media.id,
                status = WatchStatus.entries[index % WatchStatus.entries.size],
                personalRating = if (index % 2 == 0) 4 else null,
                favorite = index < 2
            )
        )
    }
    CineTrackTheme {
        MyListScreen(
            currentRoute = Routes.MyList,
            selectedFilter = "Todos",
            filters = MyListViewModel.filters,
            items = entries,
            pendingCount = entries.count { it.entry.status == WatchStatus.PENDING },
            watchingCount = entries.count { it.entry.status == WatchStatus.WATCHING },
            watchedCount = entries.count { it.entry.status == WatchStatus.WATCHED },
            showRatings = true,
            onSetFilter = {},
            onNavigateBottom = {},
            onOpenProfile = {},
            onOpenDetail = {},
            onToggleFavorite = {},
            onSetStatus = { _, _ -> },
            onRemove = {},
            onExplore = {}
        )
    }
}
