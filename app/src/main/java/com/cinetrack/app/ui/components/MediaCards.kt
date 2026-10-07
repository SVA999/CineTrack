package com.cinetrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.MediaType
import com.cinetrack.app.ui.theme.CineInfo
import com.cinetrack.app.ui.theme.CineRating
import com.cinetrack.app.ui.theme.CineRatingContent

private val posterPalettes = listOf(
    listOf(Color(0xFF171C2D), Color(0xFF69433E)),
    listOf(Color(0xFF101D20), Color(0xFF2E625A)),
    listOf(Color(0xFF17131F), Color(0xFF5A3B68)),
    listOf(Color(0xFF241818), Color(0xFF733E44)),
    listOf(Color(0xFF101827), Color(0xFF284E73)),
    listOf(Color(0xFF211B12), Color(0xFF74562D))
)

@Composable
fun PosterArtwork(media: MediaTitle, modifier: Modifier = Modifier, compact: Boolean = false, showPlaceholderText: Boolean = true) {
    val index = (media.id.hashCode() and Int.MAX_VALUE) % posterPalettes.size
    val palette = posterPalettes[index]
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 13.dp else 20.dp))
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.BottomStart
    ) {
        val networkImage = if (compact) media.posterUrl else (media.backdropUrl ?: media.posterUrl)
        if (!networkImage.isNullOrBlank()) {
            AsyncImage(
                model = networkImage,
                contentDescription = "Imagen de ${media.title}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.78f))))
        )
        if (showPlaceholderText && networkImage.isNullOrBlank()) {
            Column(Modifier.padding(if (compact) 10.dp else 14.dp)) {
                Text(
                    media.title.take(2).uppercase(),
                    fontSize = if (compact) 22.sp else 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.92f)
                )
                if (!compact) {
                    Text(
                        if (media.type == MediaType.MOVIE) "PELÍCULA" else "SERIE",
                        color = CineInfo,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RatingBadge(rating: Double?, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(999.dp), color = Color.Black.copy(alpha = 0.68f)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = CineRating, modifier = Modifier.width(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(rating?.let { "%.1f".format(it) } ?: "—", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MediaPosterCard(
    media: MediaTitle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showRating: Boolean = true
) {
    Column(modifier = modifier.width(154.dp).clickable(onClick = onClick)) {
        Box {
            PosterArtwork(media, Modifier.fillMaxWidth().aspectRatio(0.70f), compact = true)
            if (showRating) RatingBadge(media.generalRating, Modifier.align(Alignment.TopEnd).padding(8.dp))
        }
        Spacer(Modifier.height(9.dp))
        Text(media.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            "${media.year} · ${if (media.type == MediaType.MOVIE) "Película" else "Serie"}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

@Composable
fun MediaHorizontalCard(
    media: MediaTitle,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    showRating: Boolean = true,
    subtitle: String? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                PosterArtwork(media, Modifier.width(64.dp).height(88.dp), compact = true)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(media.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        subtitle ?: "${media.year} · ${media.genres.firstOrNull().orEmpty()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showRating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = CineRatingContent, modifier = Modifier.width(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(media.generalRating?.let { "%.1f".format(it) } ?: "—", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun MediaGridCard(
    media: MediaTitle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showRating: Boolean = true
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box {
                PosterArtwork(media, Modifier.fillMaxWidth().aspectRatio(0.69f), compact = true)
                if (showRating) RatingBadge(media.generalRating, Modifier.align(Alignment.TopEnd).padding(7.dp))
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            media.title,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            minLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            buildString {
                if (media.year > 0) append(media.year)
                media.genres.firstOrNull()?.let {
                    if (isNotBlank()) append(" · ")
                    append(it)
                }
            }.ifBlank { media.type.label },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}




