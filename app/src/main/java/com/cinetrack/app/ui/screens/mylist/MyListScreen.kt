package com.cinetrack.app.ui.screens.mylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.ui.components.CineTrackBottomBar
import com.cinetrack.app.ui.components.ConfirmActionDialog
import com.cinetrack.app.ui.components.EmptyState
import com.cinetrack.app.ui.components.MediaHorizontalCard
import com.cinetrack.app.ui.components.StatCard
import com.cinetrack.app.ui.components.StatusPill
import com.cinetrack.app.viewmodel.MyListItem

@Composable
fun MyListScreen(
    currentRoute: String,
    selectedFilter: String,
    filters: List<String>,
    items: List<MyListItem>,
    pendingCount: Int,
    watchingCount: Int,
    watchedCount: Int,
    showRatings: Boolean,
    onSetFilter: (String) -> Unit,
    onNavigateBottom: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSetStatus: (String, WatchStatus) -> Unit,
    onRemove: (String) -> Unit,
    onExplore: () -> Unit
) {
    var statusTarget by remember { mutableStateOf<MyListItem?>(null) }
    var removeTarget by remember { mutableStateOf<MyListItem?>(null) }

    statusTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { statusTarget = null },
            title = { Text("Cambiar estado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(target.media.title, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    WatchStatus.entries.forEach { status ->
                        TextButton(
                            onClick = {
                                onSetStatus(target.media.id, status)
                                statusTarget = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(status.label) }
                    }
                }
            },
            confirmButton = {}
        )
    }

    removeTarget?.let { target ->
        ConfirmActionDialog(
            title = "Quitar de Mi lista",
            message = "Se eliminará el registro local de “${target.media.title}”, incluida su valoración y comentario.",
            confirmLabel = "Quitar",
            destructive = true,
            onConfirm = {
                onRemove(target.media.id)
                removeTarget = null
            },
            onDismiss = { removeTarget = null }
        )
    }

    Scaffold(bottomBar = { CineTrackBottomBar(currentRoute, onNavigateBottom) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(start = 18.dp, end = 10.dp, top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Mi lista", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                        Text("Tu registro personal, en un solo lugar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        IconButton(onClick = onOpenProfile) { Icon(Icons.Outlined.AccountCircle, contentDescription = "Perfil") }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    StatCard("Pendientes", pendingCount, Modifier.weight(1f))
                    StatCard("Viendo", watchingCount, Modifier.weight(1f))
                    StatCard("Vistas", watchedCount, Modifier.weight(1f))
                }
            }
            item {
                LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters) { filter ->
                        FilterChip(selected = filter == selectedFilter, onClick = { onSetFilter(filter) }, label = { Text(filter) })
                    }
                }
            }
            if (items.isEmpty()) {
                item {
                    EmptyState(
                        title = if (selectedFilter == "Todos") "Tu lista está vacía" else "No hay títulos en este estado",
                        description = if (selectedFilter == "Todos") "Guarda películas o series desde Explorar y aparecerán aquí." else "Prueba otro filtro o cambia el estado de alguno de tus títulos.",
                        actionLabel = if (selectedFilter == "Todos") "Ir a Explorar" else "Ver todos",
                        onAction = if (selectedFilter == "Todos") onExplore else ({ onSetFilter("Todos") })
                    )
                }
            } else {
                items(items, key = { it.media.id }) { item ->
                    Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        MediaHorizontalCard(
                            media = item.media,
                            subtitle = "${item.media.year} · ${item.media.type.label}",
                            onClick = { onOpenDetail(item.media.id) },
                            showRating = showRatings,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onToggleFavorite(item.media.id) }) {
                                        Icon(
                                            if (item.entry.favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = if (item.entry.favorite) "Quitar favorito" else "Marcar favorito",
                                            tint = if (item.entry.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { removeTarget = item }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Quitar de Mi lista")
                                    }
                                }
                            }
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(onClick = { statusTarget = item }, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.padding(end = 5.dp))
                                    StatusPill(item.entry.status)
                                }
                            }
                            item.entry.personalRating?.let { Text("Tu nota: $it/5", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}
