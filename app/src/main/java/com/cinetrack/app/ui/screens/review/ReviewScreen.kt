package com.cinetrack.app.ui.screens.review

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.WatchStatus
import com.cinetrack.app.ui.components.ConfirmActionDialog
import com.cinetrack.app.ui.components.PosterArtwork
import com.cinetrack.app.ui.components.RatingStars
import com.cinetrack.app.ui.components.SectionTitle
import com.cinetrack.app.ui.components.StatusSelector

@Composable
fun ReviewScreen(
    media: MediaTitle?,
    status: WatchStatus,
    favorite: Boolean,
    rating: Int?,
    comment: String,
    hasChanges: Boolean,
    publicReview: Boolean,
    firebaseConfigured: Boolean,
    loadingPublicState: Boolean,
    publishing: Boolean,
    publicReviewId: String?,
    message: String?,
    onBack: () -> Unit,
    onStatus: (WatchStatus) -> Unit,
    onFavorite: (Boolean) -> Unit,
    onRating: (Int?) -> Unit,
    onComment: (String) -> Unit,
    onPublicReview: (Boolean) -> Unit,
    onSave: () -> Unit,
    onShare: (String) -> Unit
) {
    var confirmExit by remember { mutableStateOf(false) }

    fun requestBack() {
        if (hasChanges) confirmExit = true else onBack()
    }

    BackHandler { requestBack() }

    if (confirmExit) {
        ConfirmActionDialog(
            title = "Descartar cambios",
            message = "Tienes cambios sin guardar en este registro.",
            confirmLabel = "Salir sin guardar",
            destructive = true,
            onConfirm = { confirmExit = false; onBack() },
            onDismiss = { confirmExit = false }
        )
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        IconButton(onClick = ::requestBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás") }
        Spacer(Modifier.height(8.dp))

        if (media != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PosterArtwork(media, Modifier.padding(vertical = 2.dp).fillMaxWidth(0.25f).height(116.dp), compact = true)
                Column(Modifier.weight(1f)) {
                    Text("Mi registro", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(media.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Text("${media.year} · ${media.type.label}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Text("Mi registro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.height(26.dp))
        SectionTitle("Estado")
        Spacer(Modifier.height(10.dp))
        StatusSelector(status, onStatus)

        Spacer(Modifier.height(26.dp))
        SectionTitle("Valoración personal")
        Text("Tu registro es personal; si Firebase está conectado, la reseña podrá compartirse mediante enlace.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(5.dp))
        RatingStars(rating, { onRating(it) })
        if (rating != null) {
            OutlinedButton(onClick = { onRating(null) }, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                Text(" Quitar valoración")
            }
        }

        Spacer(Modifier.height(26.dp))
        SectionTitle("Comentario")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = comment,
            onValueChange = onComment,
            placeholder = { Text("¿Qué te pareció? Escribe una nota para recordarla después.") },
            minLines = 5,
            maxLines = 8,
            supportingText = { Text("${comment.length}/500") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(Modifier.height(18.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Favorito", fontWeight = FontWeight.Bold)
                    Text("Destácalo dentro de tu colección", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
                Switch(checked = favorite, onCheckedChange = onFavorite)
            }
        }

        Spacer(Modifier.height(14.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Reseña pública", fontWeight = FontWeight.Bold)
                    Text(
                        if (firebaseConfigured) "Permite verla desde tu perfil y compartir un enlace" else "Conecta Firebase para publicar reseñas",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(
                    checked = publicReview,
                    onCheckedChange = onPublicReview,
                    enabled = firebaseConfigured && !loadingPublicState && !publishing
                )
            }
        }

        Spacer(Modifier.height(26.dp))
        Button(onClick = onSave, enabled = hasChanges && !publishing, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Text(if (publishing) "Publicando…" else if (hasChanges) "Guardar registro" else "Sin cambios pendientes")
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp)) }
        publicReviewId?.let { reviewId ->
            OutlinedButton(onClick = { onShare(reviewId) }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Outlined.Share, contentDescription = null)
                Text(" Compartir mi reseña")
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
