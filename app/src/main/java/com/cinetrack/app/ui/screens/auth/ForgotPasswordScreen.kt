package com.cinetrack.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordScreen(
    loading: Boolean,
    error: String?,
    message: String?,
    firebaseConfigured: Boolean,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onEdit: () -> Unit
) {
    var email by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = .25f), Color.Transparent),
                radius = 760f
            )
        )
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver")
            }
            Spacer(Modifier.height(24.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Icon(Icons.Outlined.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(22.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text(
                "Ingresa tu correo electrónico asociado a tu cuenta de CineTrack y te enviaremos las instrucciones para restablecer tu contraseña.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; onEdit() },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                enabled = !loading && firebaseConfigured,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), style = MaterialTheme.typography.bodySmall) }
            message?.let { Text(it, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { onSend(email) },
                enabled = !loading && firebaseConfigured,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(13.dp)
            ) {
                if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(22.dp)) else Text("Enviar instrucciones  →")
            }
            TextButton(onClick = onBack) { Text("‹ Volver a Iniciar sesión") }
            if (!firebaseConfigured) {
                Text("Conecta Firebase para activar la recuperación de contraseña.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
