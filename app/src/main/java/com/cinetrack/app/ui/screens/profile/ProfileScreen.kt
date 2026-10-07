package com.cinetrack.app.ui.screens.profile

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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cinetrack.app.BuildConfig
import com.cinetrack.app.data.model.AppUser
import com.cinetrack.app.data.model.social.UserProfile
import com.cinetrack.app.ui.components.CineTrackBottomBar
import com.cinetrack.app.ui.components.ConfirmActionDialog
import com.cinetrack.app.ui.components.InlineMessageCard
import com.cinetrack.app.ui.components.LoadingStateCard
import com.cinetrack.app.ui.components.ProfileAvatar
import com.cinetrack.app.ui.components.StatCard
import com.cinetrack.app.ui.theme.CineSuccessContent

@Composable
fun ProfileScreen(
    currentRoute: String,
    user: AppUser?,
    profile: UserProfile?,
    watched: Int,
    pending: Int,
    favorites: Int,
    firebaseConfigured: Boolean,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onSettings: () -> Unit,
    onCredits: () -> Unit,
    onNavigateBottom: (String) -> Unit,
    onLogout: () -> Unit
) {
    var confirmLogout by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    if (confirmLogout) {
        ConfirmActionDialog(
            title = "Cerrar sesión",
            message = "Volverás a la pantalla de acceso. No podrás regresar al contenido privado con Atrás.",
            confirmLabel = "Cerrar sesión",
            destructive = true,
            onConfirm = { confirmLogout = false; onLogout() },
            onDismiss = { confirmLogout = false }
        )
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Ayuda y soporte") },
            text = {
                Text("Si tienes problemas, revisa primero tu conexión, la configuración de Firebase/TMDB y vuelve a intentarlo. Para la entrega académica, documenta cualquier incidencia encontrada durante la prueba en dispositivo físico.")
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Cerrar") } },
            dismissButton = {
                TextButton(onClick = { uriHandler.openUri("${BuildConfig.CINETRACK_WEB_BASE_URL}/support") }) {
                    Text("Abrir soporte")
                }
            }
        )
    }

    val displayName = profile?.displayName?.takeIf { it.isNotBlank() }
        ?: user?.displayName
        ?: "Usuario CineTrack"

    Scaffold(bottomBar = { CineTrackBottomBar(currentRoute, onNavigateBottom) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("CineTrack", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                IconButton(onClick = onShare, enabled = firebaseConfigured) {
                    Icon(Icons.Outlined.Share, contentDescription = "Compartir perfil", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(12.dp))
            ProfileAvatar(displayName = displayName, avatarUrl = profile?.avatarUrl, size = 94.dp)
            Spacer(Modifier.height(12.dp))
            Text(displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(user?.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(999.dp), color = CineSuccessContent.copy(alpha = .10f)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("●", color = CineSuccessContent)
                    Text(
                        if (firebaseConfigured) " Perfil público disponible" else " Perfil local · conecta Firebase para compartir",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Outlined.Edit, contentDescription = null)
                Text("  Editar perfil", fontWeight = FontWeight.Bold)
            }

            if (loading) {
                LoadingStateCard(
                    title = "Actualizando perfil",
                    description = "Sincronizando tus datos públicos.",
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
            if (!error.isNullOrBlank()) {
                InlineMessageCard(
                    message = error,
                    actionLabel = "Reintentar",
                    onAction = onRetry,
                    error = true,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Mi actividad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Este año", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                StatCard("Favoritos", favorites, Modifier.weight(1f))
                StatCard("Pendientes", pending, Modifier.weight(1f))
                StatCard("Vistos", watched, Modifier.weight(1f))
            }

            Spacer(Modifier.height(22.dp))
            ProfileMenuRow(Icons.Outlined.Settings, "Configuración", "Preferencias de cuenta y app", onSettings)
            ProfileMenuRow(Icons.Outlined.Info, "Créditos", "Equipo, versión y fuentes", onCredits)
            ProfileMenuRow(Icons.Outlined.HelpOutline, "Ayuda y soporte", "Preguntas frecuentes y contacto") { showHelp = true }

            Spacer(Modifier.height(22.dp))
            Surface(
                onClick = { confirmLogout = true },
                shape = RoundedCornerShape(13.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = .10f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("  Cerrar sesión", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

