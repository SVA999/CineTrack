package com.cinetrack.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cinetrack.app.BuildConfig
import com.cinetrack.app.data.model.ThemePreference
import com.cinetrack.app.data.model.UserPreferences
import com.cinetrack.app.ui.components.ConfirmActionDialog
import com.cinetrack.app.ui.components.ProfileAvatar

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    displayName: String,
    email: String,
    avatarUrl: String?,
    onBack: () -> Unit,
    onTheme: (ThemePreference) -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onCredits: () -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    var showPrivacy by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("Privacidad y datos") },
            text = {
                Text(
                    "Tu lista, estados, favoritos, valoraciones y comentarios privados se guardan localmente con Room. Firebase Authentication gestiona la sesión. Solo el perfil y las reseñas que marques como públicas se sincronizan con Firestore cuando Firebase esté conectado. TMDB provee la información del catálogo."
                )
            },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("Entendido") } },
            dismissButton = {
                TextButton(onClick = {
                    uriHandler.openUri("${BuildConfig.CINETRACK_WEB_BASE_URL}/privacy")
                }) { Text("Política completa") }
            }
        )
    }

    if (confirmLogout) {
        ConfirmActionDialog(
            title = "Cerrar sesión",
            message = "Volverás a la pantalla de acceso. Tus datos locales permanecen asociados a esta cuenta en el dispositivo.",
            confirmLabel = "Cerrar sesión",
            destructive = true,
            onConfirm = { confirmLogout = false; onLogout() },
            onDismiss = { confirmLogout = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 8.dp)
    ) {
        item(key = "header") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás") }
                Text("Configuración", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
        }

        item(key = "profile") {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(displayName = displayName, avatarUrl = avatarUrl, size = 54.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(displayName, fontWeight = FontWeight.Bold)
                        Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item(key = "account-title") { SettingsSectionTitle("CUENTA") }
        item(key = "account") {
            SettingsGroup {
                SettingsRow(Icons.Outlined.Edit, "Editar perfil", displayName, onEditProfile)
                SettingsRow(Icons.Outlined.LockReset, "Cambiar contraseña", "Enviar enlace de cambio", onChangePassword)
                SettingsRow(Icons.Outlined.DeleteForever, "Eliminar cuenta", "Borrar cuenta y datos asociados", onDeleteAccount, destructive = true)
            }
        }

        item(key = "preferences-title") { SettingsSectionTitle("PREFERENCIAS") }
        item(key = "preferences") {
            SettingsGroup {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text("Apariencia", fontWeight = FontWeight.SemiBold)
                            Text("Mismo diseño en oscuro, claro o según el sistema", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemePreference.entries.forEach { option ->
                            FilterChip(
                                selected = preferences.theme == option,
                                onClick = { onTheme(option) },
                                label = { Text(option.label) }
                            )
                        }
                    }
                }
                SettingsRow(Icons.Outlined.Language, "Idioma", "Español (Latam)", onClick = null)
            }
        }

        item(key = "legal-title") { SettingsSectionTitle("INFORMACIÓN Y LEGAL") }
        item(key = "legal") {
            SettingsGroup {
                SettingsRow(
                    Icons.Outlined.PrivacyTip,
                    "Privacidad y datos",
                    "Cómo usa CineTrack tus datos",
                    onClick = { showPrivacy = true }
                )
                SettingsRow(Icons.Outlined.Info, "Acerca de CineTrack", "Créditos, versión y fuentes", onCredits)
            }
        }

        item(key = "logout") {
            Spacer(Modifier.height(22.dp))
            Surface(
                onClick = { confirmLogout = true },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("  Cerrar sesión", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)?,
    destructive: Boolean = false
) {
    val content: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val accent = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(alpha = .12f)) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.padding(8.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (onClick != null) Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, color = androidx.compose.ui.graphics.Color.Transparent) { content() }
    } else {
        content()
    }
}


