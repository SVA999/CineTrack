package com.cinetrack.app.ui.screens.review.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.ui.components.PosterArtwork
import com.cinetrack.app.ui.components.InlineMessageCard
import com.cinetrack.app.ui.components.LoadingStateCard
import com.cinetrack.app.ui.theme.CineRatingContent

@Composable
fun SharedReviewScreen(
    review: PublicReview?,
    media: MediaTitle?,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenMedia: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás") }
        if (loading) {
            LoadingStateCard(
                title = "Cargando reseña",
                description = "Recuperando la reseña pública y su película o serie.",
                modifier = Modifier.padding(top = 28.dp)
            )
            return@Column
        }
        if (review == null) {
            InlineMessageCard(
                message = error ?: "No se encontró la reseña.",
                error = true,
                modifier = Modifier.padding(top = 28.dp)
            )
            return@Column
        }
        Text("Reseña compartida", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(review.mediaTitle, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(18.dp))
        media?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PosterArtwork(it, Modifier.width(86.dp).height(122.dp), compact = true)
                Column(Modifier.padding(start = 14.dp)) {
                    Text(it.title, fontWeight = FontWeight.Bold)
                    Text("${it.year} · ${it.type.label}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Por ${review.userDisplayName}", fontWeight = FontWeight.Bold)
                review.rating?.let { rating ->
                    Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(Icons.Filled.Star, contentDescription = null, tint = if (index < rating) CineRatingContent else MaterialTheme.colorScheme.outline)
                        }
                        Text("  $rating/5", fontWeight = FontWeight.Bold)
                    }
                }
                if (review.comment.isNotBlank()) Text(review.comment, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(18.dp))
        OutlinedButton(onClick = { onOpenProfile(review.userId) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Person, contentDescription = null)
            Text(" Ver perfil")
        }
        OutlinedButton(onClick = { onOpenMedia(review.mediaId) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Movie, contentDescription = null)
            Text(" Ver título")
        }
        Spacer(Modifier.height(32.dp))
    }
}

