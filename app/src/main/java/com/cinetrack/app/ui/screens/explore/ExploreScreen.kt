package com.cinetrack.app.ui.screens.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.ui.components.CineTrackBottomBar
import com.cinetrack.app.ui.components.CineTrackLogo
import com.cinetrack.app.ui.components.InlineMessageCard
import com.cinetrack.app.ui.components.LoadingStateCard
import com.cinetrack.app.ui.components.MediaGridCard
import com.cinetrack.app.ui.components.ProfileAvatar
import com.cinetrack.app.ui.components.MediaPosterCard
import com.cinetrack.app.ui.components.SectionTitle

@Composable
fun ExploreScreen(
    currentRoute: String,
    displayName: String?,
    trending: List<MediaTitle>,
    recentlyAdded: List<MediaTitle>,
    showRatings: Boolean,
    loading: Boolean,
    error: String?,
    usingTmdb: Boolean,
    onRetry: () -> Unit,
    onNavigateBottom: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDetail: (String) -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Acción", "Comedia", "Drama")
    val catalog = (trending + recentlyAdded)
        .distinctBy { it.id }
        .filter { media -> selectedCategory == "Todos" || media.genres.any { it.equals(selectedCategory, ignoreCase = true) } }
    val firstName = displayName?.trim()?.substringBefore(' ')?.takeIf { it.isNotBlank() }

    Scaffold(bottomBar = { CineTrackBottomBar(currentRoute, onNavigateBottom) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 10.dp, top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CineTrackLogo(compact = true)
                    Surface(onClick = onOpenProfile, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        ProfileAvatar(displayName = displayName ?: "Usuario", avatarUrl = null, size = 36.dp)
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (firstName != null) "¡Hola, $firstName!" else "¡Hola!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text("Encuentra tu próxima historia", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenSearch),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Text("Buscar películas o series", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Icon(Icons.Outlined.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (loading) {
                item {
                    LoadingStateCard(
                        title = if (usingTmdb) "Actualizando catálogo" else "Preparando CineTrack",
                        description = if (usingTmdb) "Consultando tendencias y estrenos en TMDB." else "Organizando el contenido disponible.",
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }

            if (!error.isNullOrBlank()) {
                item {
                    InlineMessageCard(
                        message = "$error Se mantiene el catálogo disponible para que puedas seguir usando CineTrack.",
                        actionLabel = if (usingTmdb) "Reintentar" else null,
                        onAction = if (usingTmdb) onRetry else null,
                        error = true,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }

            if (trending.isNotEmpty()) {
                item {
                    SectionTitle(
                        "🔥 Tendencias destacadas",
                        modifier = Modifier.padding(horizontal = 18.dp),
                        action = "Ver todo",
                        onAction = onOpenSearch
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(trending.take(8), key = { it.id }) { media ->
                            MediaPosterCard(media = media, onClick = { onOpenDetail(media.id) }, showRating = showRatings)
                        }
                    }
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }

            item {
                SectionTitle("▣ Catálogo de películas", modifier = Modifier.padding(horizontal = 18.dp))
            }

            if (catalog.isEmpty() && !loading) {
                item {
                    Text(
                        "No hay títulos para este filtro.",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                catalog.chunked(2).forEach { rowItems ->
                    item(key = rowItems.joinToString("|") { it.id }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { media ->
                                MediaGridCard(
                                    media = media,
                                    onClick = { onOpenDetail(media.id) },
                                    modifier = Modifier.weight(1f),
                                    showRating = showRatings
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
