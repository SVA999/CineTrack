package com.cinetrack.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cinetrack.app.data.model.MediaDetails
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.RelatedMedia
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.ui.components.ConfirmActionDialog
import com.cinetrack.app.ui.components.MediaPosterCard
import com.cinetrack.app.ui.components.PersonCreditCard
import com.cinetrack.app.ui.components.PosterArtwork
import com.cinetrack.app.ui.components.RatingBadge
import com.cinetrack.app.ui.components.SectionTitle
import com.cinetrack.app.ui.theme.CineRatingContent
import com.cinetrack.app.ui.theme.CineSuccessContent

@Composable
fun DetailScreen(
    media: MediaTitle?,
    details: MediaDetails?,
    related: List<RelatedMedia>,
    saved: Boolean,
    status: WatchStatus,
    favorite: Boolean,
    personalRating: Int?,
    personalComment: String,
    showRatings: Boolean,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onStatus: (WatchStatus) -> Unit,
    onFavorite: () -> Unit,
    onSetRating: (Int?) -> Unit,
    onReview: () -> Unit,
    onShare: () -> Unit,
    onOpenRelated: (String) -> Unit,
    onRetry: () -> Unit
) {
    var confirmRemove by remember { mutableStateOf(false) }

    if (confirmRemove && media != null) {
        ConfirmActionDialog(
            title = "Quitar de Mi lista",
            message = "Se eliminará tu registro local de “${media.title}”.",
            confirmLabel = "Quitar",
            destructive = true,
            onConfirm = { confirmRemove = false; onRemove() },
            onDismiss = { confirmRemove = false }
        )
    }

    if (media == null) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (loading) "Cargando título…" else "No se encontró este título.", style = MaterialTheme.typography.titleLarge)
            error?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp)) }
            Spacer(Modifier.height(12.dp))
            if (!loading) OutlinedButton(onClick = onRetry) { Text("Reintentar") }
            OutlinedButton(onClick = onBack) { Text("Volver") }
        }
        return
    }

    // LazyColumn keeps only the visible vertical content active. Besides being
    // more efficient for this long screen, it avoids keeping one very tall
    // scrolling display list alive while posters and text are updated.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item(key = "hero") {
            Box(Modifier.fillMaxWidth().heightIn(min = 365.dp)) {
                PosterArtwork(media, Modifier.fillMaxWidth().height(270.dp), showPlaceholderText = false)
                Box(
                    Modifier.fillMaxWidth().height(270.dp).background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = .24f), Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = .95f))
                        )
                    )
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = .55f)
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás", tint = Color.White) }
                }
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Surface(shape = CircleShape, color = Color.Black.copy(alpha = .55f)) {
                        IconButton(onClick = { if (saved) confirmRemove = true else onAdd() }) {
                            Icon(if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Guardar título", tint = Color.White)
                        }
                    }
                    Surface(shape = CircleShape, color = Color.Black.copy(alpha = .55f)) {
                        IconButton(onClick = onShare) { Icon(Icons.Outlined.Share, contentDescription = "Compartir título", tint = Color.White) }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 203.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(Modifier.width(112.dp).height(162.dp)) {
                        PosterArtwork(media, Modifier.fillMaxSize(), compact = true)
                        if (showRatings) RatingBadge(media.generalRating, Modifier.align(Alignment.TopStart).padding(7.dp))
                    }
                    Column(Modifier.weight(1f).padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (showRatings) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = CineRatingContent, modifier = Modifier.size(17.dp))
                                Text(" ${media.generalRating?.let { "%.1f".format(it) } ?: "—"}", fontWeight = FontWeight.Bold)
                                if ((media.ratingCount ?: 0) > 0) {
                                    Text(" · ${(media.ratingCount ?: 0)} reseñas", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (media.year > 0) MetadataPill(media.year.toString())
                            media.durationMinutes?.let { MetadataPill("${it}m") }
                            media.ageRating?.let { MetadataPill(it) }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            media.genres.take(3).forEach { MetadataPill(it) }
                        }
                    }
                }
            }
        }

        item(key = "title") {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(media.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Text(
                    media.synopsis.take(115).let { if (media.synopsis.length > 115) "$it…" else it },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        error?.let { message ->
            item(key = "error") {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = .10f)
                ) {
                    Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
                }
            }
        }

        item(key = "actions") {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailAction(
                    label = "Favorito",
                    selected = favorite,
                    icon = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    modifier = Modifier.weight(1f),
                    onClick = onFavorite
                )
                DetailAction(
                    label = "Visto",
                    selected = saved && status == WatchStatus.WATCHED,
                    icon = if (saved && status == WatchStatus.WATCHED) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    modifier = Modifier.weight(1f),
                    onClick = { onStatus(WatchStatus.WATCHED) }
                )
                DetailAction(
                    label = "Pendiente",
                    selected = saved && status == WatchStatus.PENDING,
                    icon = Icons.Outlined.Schedule,
                    modifier = Modifier.weight(1f),
                    onClick = { onStatus(WatchStatus.PENDING) }
                )
            }
        }

        item(key = "synopsis") {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionTitle("Sinopsis")
                Text(media.synopsis, style = MaterialTheme.typography.bodyLarge)
            }
        }

        val cast = details?.cast.orEmpty()
        if (cast.isNotEmpty()) {
            item(key = "cast-title") {
                Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                    SectionTitle("Reparto principal")
                }
            }
            item(key = "cast") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 2.dp)
                ) {
                    items(cast.take(10), key = { it.id }) { person ->
                        PersonCreditCard(person = person)
                    }
                }
            }
        }

        item(key = "personal-rating") {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Tu valoración", fontWeight = FontWeight.Bold)
                            Text(
                                if (personalRating == null) "Toca una estrella para valorar" else "Tu puntuación está guardada en este dispositivo",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (personalRating != null) {
                            Surface(shape = RoundedCornerShape(999.dp), color = CineRatingContent.copy(alpha = .14f)) {
                                Text("★ $personalRating/5", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = CineRatingContent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            Surface(
                                onClick = { onSetRating(if (personalRating == star) null else star) },
                                color = Color.Transparent,
                                shape = CircleShape
                            ) {
                                Text(
                                    "★",
                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp),
                                    color = if ((personalRating ?: 0) >= star) CineRatingContent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .35f),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                    Surface(
                        onClick = onReview,
                        shape = RoundedCornerShape(11.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = personalComment.takeIf { it.isNotBlank() } ?: "Escribe tu reseña o notas personales…",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                            color = if (personalComment.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (personalRating == null && personalComment.isBlank()) "Sin registro todavía" else "Guardado localmente",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Button(onClick = onReview, shape = RoundedCornerShape(10.dp)) {
                            Text(if (personalComment.isBlank()) "Escribir reseña  ▷" else "Editar reseña  ▷")
                        }
                    }
                }
            }
        }

        if (related.isNotEmpty()) {
            item(key = "related-title") {
                Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp)) {
                    SectionTitle("Recomendados para ti")
                }
            }
            item(key = "related") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 2.dp)
                ) {
                    items(related, key = { "${it.media.id}:${it.relationType}" }) { item ->
                        Column(Modifier.width(150.dp)) {
                            MediaPosterCard(
                                media = item.media,
                                onClick = { onOpenRelated(item.media.id) },
                                modifier = Modifier.fillMaxWidth(),
                                showRating = showRatings
                            )
                            Text(
                                item.reason,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (saved) {
            item(key = "remove") {
                OutlinedButton(
                    onClick = { confirmRemove = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Quitar de Mi lista")
                }
            }
        }
    }
}

@Composable
private fun DetailAction(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .16f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = if (selected) CineSuccessContent else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(5.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MetadataPill(text: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .16f)) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}




