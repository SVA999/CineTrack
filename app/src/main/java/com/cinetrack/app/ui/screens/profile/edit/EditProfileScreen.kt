package com.cinetrack.app.ui.screens.profile.edit

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.social.UserProfile
import com.cinetrack.app.ui.components.ConfirmActionDialog
import com.cinetrack.app.ui.components.PosterArtwork
import com.cinetrack.app.ui.components.ProfileAvatar
import com.cinetrack.app.ui.components.SectionTitle

@Composable
fun EditProfileScreen(
    profile: UserProfile?,
    availableFavorites: List<MediaTitle>,
    saving: Boolean,
    firebaseConfigured: Boolean,
    avatarUploadEnabled: Boolean,
    message: String?,
    error: String?,
    onBack: () -> Unit,
    onSave: (name: String, gender: String, favoriteIds: List<String>, avatarUri: String?) -> Unit
) {
    var name by remember(profile?.uid, profile?.updatedAtEpochMillis) { mutableStateOf(profile?.displayName.orEmpty()) }
    var gender by remember(profile?.uid, profile?.updatedAtEpochMillis) { mutableStateOf(profile?.gender ?: "Sin especificar") }
    val selectedFavorites = remember(profile?.uid, profile?.updatedAtEpochMillis) {
        mutableStateListOf<String>().apply { addAll(profile?.favoriteMediaIds.orEmpty().take(5)) }
    }
    var avatarUri by remember(profile?.uid, profile?.updatedAtEpochMillis, profile?.avatarUrl) { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) avatarUri = uri.toString()
    }

    val genders = listOf("Sin especificar", "Mujer", "Hombre", "No binario", "Otro")
    val avatarModel = avatarUri ?: profile?.avatarUrl
    val baselineFavorites = profile?.favoriteMediaIds.orEmpty().take(5)
    val hasChanges = name.trim() != profile?.displayName.orEmpty().trim() ||
        gender != (profile?.gender ?: "Sin especificar") ||
        selectedFavorites.toList() != baselineFavorites ||
        avatarUri != null
    var confirmExit by remember { mutableStateOf(false) }

    fun requestBack() {
        if (saving) return
        if (hasChanges && !saving) confirmExit = true else onBack()
    }

    BackHandler { requestBack() }

    if (confirmExit) {
        ConfirmActionDialog(
            title = "Descartar cambios",
            message = "Tienes cambios de perfil que todavía no has guardado.",
            confirmLabel = "Salir sin guardar",
            destructive = true,
            onConfirm = { confirmExit = false; onBack() },
            onDismiss = { confirmExit = false }
        )
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        IconButton(onClick = ::requestBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás") }
        Text("Editar perfil", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text("Tu nombre y preferencias son públicos. La foto se guarda solo en este dispositivo: no se sube ni se comparte. Sin foto se muestra tu inicial.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ProfileAvatar(displayName = name.ifBlank { "CineTrack" }, avatarUrl = avatarModel, size = 110.dp)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = !saving && avatarUploadEnabled,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
            Text(" Elegir foto")
        }
        if (!avatarUploadEnabled) {
            Text(
                "La carga de foto está deshabilitada en esta versión. Se usa tu inicial como avatar.",
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(40) },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(Modifier.height(22.dp))
        SectionTitle("Género")
        Text("Opcional. Si lo eliges, será visible para otros usuarios.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            genders.forEach { option ->
                FilterChip(selected = gender == option, onClick = { gender = option }, label = { Text(option) })
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle("Películas favoritas")
        Text("Elige hasta 5 títulos que ya hayas marcado como favoritos.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))
        if (availableFavorites.isEmpty()) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface) {
                Text("Marca títulos como favoritos desde Detalle o Mi lista para poder mostrarlos aquí.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            availableFavorites.forEach { media ->
                val selected = media.id in selectedFavorites
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        PosterArtwork(media, Modifier.width(54.dp).height(76.dp), compact = true)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(media.title, fontWeight = FontWeight.Bold)
                            Text(media.year.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) selectedFavorites.remove(media.id)
                                else if (selectedFavorites.size < 5) selectedFavorites.add(media.id)
                            },
                            label = { Text(if (selected) "Elegida" else "Elegir") },
                            enabled = selected || selectedFavorites.size < 5
                        )
                    }
                }
            }
        }

        message?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 14.dp)) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 14.dp)) }
        if (!firebaseConfigured) {
            AssistChip(onClick = {}, label = { Text("Modo local: conecta Firebase para publicar") }, modifier = Modifier.padding(top = 12.dp))
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onSave(name, gender, selectedFavorites.toList(), avatarUri) },
            enabled = !saving && name.isNotBlank() && hasChanges,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (saving) "Guardando…" else if (hasChanges) "Guardar cambios" else "Sin cambios pendientes")
        }
        Spacer(Modifier.height(32.dp))
    }
}



