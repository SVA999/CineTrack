package com.cinetrack.app.ui.screens.credits

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DesignServices
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cinetrack.app.BuildConfig
import com.cinetrack.app.ui.components.CineTrackLogo
import com.cinetrack.app.ui.theme.CineRatingContent

@Composable
fun CreditsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás") }
            Text("Créditos", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(48.dp))
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            CineTrackLogo()
            Spacer(Modifier.height(6.dp))
            Text("TU PRÓXIMA HISTORIA ESTÁ AQUÍ", color = CineRatingContent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(28.dp))
        Text("Creado por", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("Equipo del proyecto CineTrack.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))

        CreditRoleCard(Icons.Outlined.Person, "Santiago Restrepo", "Integrante")
        CreditRoleCard(Icons.Outlined.Person, "Santiago Viana", "Integrante")
        CreditRoleCard(Icons.Outlined.Person, "Natalia Arce", "Integrante")

        Spacer(Modifier.height(18.dp))
        Text("Tecnologías", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        CreditRoleCard(Icons.Outlined.Code, "Android & Arquitectura", "Kotlin · Jetpack Compose · MVVM")
        CreditRoleCard(Icons.Outlined.DesignServices, "Diseño UX/UI", "Figma · sistema visual")
        CreditRoleCard(Icons.Outlined.Storage, "Datos & Integraciones", "TMDB · Firebase · Room · DataStore")

        Spacer(Modifier.height(18.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎞 CineTrack v${BuildConfig.VERSION_NAME}", fontWeight = FontWeight.Bold)
                Text("Proyecto académico · Aplicaciones Móviles · Ingeniería de Sistemas · 2026", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Text("Catálogo provisto por TMDB. This product uses the TMDB API but is not endorsed or certified by TMDB.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun CreditRoleCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .14f)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(11.dp))
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


