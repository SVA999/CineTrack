package com.cinetrack.app.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.ui.components.CineTrackBottomBar
import com.cinetrack.app.ui.components.CineTrackLogo
import com.cinetrack.app.ui.components.EmptyState
import com.cinetrack.app.ui.components.InlineMessageCard
import com.cinetrack.app.ui.components.LoadingStateCard
import com.cinetrack.app.ui.components.MediaGridCard
import com.cinetrack.app.ui.components.ProfileAvatar

@Composable
fun SearchScreen(
    currentRoute: String,
    displayName: String?,
    query: String,
    selectedGenre: String,
    genres: List<String>,
    results: List<MediaTitle>,
    showRatings: Boolean,
    loading: Boolean,
    error: String?,
    usingTmdb: Boolean,
    onQueryChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onClear: () -> Unit,
    onNavigateBottom: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenDetail: (String) -> Unit
) {
    Scaffold(bottomBar = { CineTrackBottomBar(currentRoute, onNavigateBottom) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                Text(
                    "Buscar",
                    modifier = Modifier.padding(horizontal = 18.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Buscar películas o series…") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (query.isNotBlank()) {
                                IconButton(onClick = onClear) { Icon(Icons.Outlined.Close, contentDescription = "Limpiar búsqueda") }
                            }
                            Icon(Icons.Outlined.Tune, contentDescription = "Filtros", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genres) { genre ->
                        FilterChip(
                            selected = genre == selectedGenre,
                            onClick = { onGenreChange(genre) },
                            label = { Text(genre) }
                        )
                    }
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (query.isBlank() && selectedGenre == "Todos") "Explorar catálogo" else "Resultados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${results.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }

            if (loading) {
                item {
                    LoadingStateCard(
                        title = "Buscando en TMDB",
                        description = "Consultando películas y series que coincidan con tu búsqueda.",
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }

            if (!error.isNullOrBlank()) {
                item {
                    InlineMessageCard(
                        message = "$error${if (usingTmdb) " Puedes seguir usando los resultados disponibles." else ""}",
                        error = true,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }

            if (!loading && results.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        EmptyState(
                            "No encontramos resultados",
                            "Intenta buscar con otros términos, directores o explora otros géneros en CineTrack.",
                            actionLabel = "Limpiar filtros",
                            onAction = onClear
                        )
                    }
                }
            } else if (results.isNotEmpty()) {
                results.chunked(2).forEach { rowItems ->
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

            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}
