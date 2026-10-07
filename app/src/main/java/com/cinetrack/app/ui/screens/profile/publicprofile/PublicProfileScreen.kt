package com.cinetrack.app.ui.screens.profile.publicprofile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.social.PublicReview
import com.cinetrack.app.data.model.social.UserProfile
import com.cinetrack.app.ui.components.MediaPosterCard
import com.cinetrack.app.ui.components.InlineMessageCard
import com.cinetrack.app.ui.components.LoadingStateCard
import com.cinetrack.app.ui.components.PublicReviewCard
import com.cinetrack.app.ui.components.ProfileAvatar
import com.cinetrack.app.ui.components.SectionTitle

@Composable
fun PublicProfileScreen(
    profile: UserProfile?,
    favoriteMedia: List<MediaTitle>,
    recentReviews: List<PublicReview>,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onOpenMedia: (String) -> Unit,
    onOpenReview: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás") }
        if (loading) {
            LoadingStateCard(
                title = "Cargando perfil",
                description = "Consultando sus películas favoritas y reseñas públicas.",
                modifier = Modifier.padding(top = 28.dp)
            )
            return@Column
        }
        if (profile == null) {
            InlineMessageCard(
                message = error ?: "No se encontró el perfil.",
                error = true,
                modifier = Modifier.padding(top = 28.dp)
            )
            return@Column
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ProfileAvatar(displayName = profile.displayName, avatarUrl = profile.avatarUrl, size = 104.dp)
        }
        Spacer(Modifier.height(14.dp))
        Text(profile.displayName, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        profile.gender.takeIf { it != "Sin especificar" }?.let {
            Text(it, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = onShare, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Outlined.Share, contentDescription = null)
            Text(" Compartir perfil")
        }
        Spacer(Modifier.height(28.dp))
        SectionTitle("Películas favoritas")
        Spacer(Modifier.height(10.dp))
        if (favoriteMedia.isEmpty()) {
            Text("Este usuario todavía no ha publicado películas favoritas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favoriteMedia, key = { it.id }) { media ->
                    MediaPosterCard(media = media, onClick = { onOpenMedia(media.id) })
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionTitle("Reseñas públicas")
        Spacer(Modifier.height(10.dp))
        if (recentReviews.isEmpty()) {
            Text("Este usuario todavía no ha publicado reseñas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            recentReviews.forEach { review ->
                PublicReviewCard(
                    review = review,
                    onClick = { onOpenReview(review.id) },
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
