package com.cinetrack.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cinetrack.app.ui.components.CineTrackLogo

@Composable
fun LoginScreen(
    loading: Boolean,
    error: String?,
    message: String?,
    firebaseConfigured: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    onEdit: () -> Unit
) {
    var email by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var password by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var showPassword by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val focus = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), Color.Transparent),
                radius = 800f
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            ) {
                Box(Modifier.padding(18.dp)) { CineTrackLogo(compact = true) }
            }
            Spacer(Modifier.height(22.dp))
            Text("CineTrack", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Text(
                "🎬 Tu próxima historia está aquí",
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(30.dp))

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Correo electrónico", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; onEdit() },
                    placeholder = { Text("ejemplo@correo.com") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !loading,
                    shape = RoundedCornerShape(13.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
            }
            Spacer(Modifier.height(14.dp))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Contraseña", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; onEdit() },
                    placeholder = { Text("••••••••") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !loading,
                    shape = RoundedCornerShape(13.dp),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); onLogin(email, password) })
                )
            }

            TextButton(onClick = onForgotPassword, enabled = !loading, modifier = Modifier.align(Alignment.End)) {
                Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.bodySmall)
            }

            if (!firebaseConfigured) {
                Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                    Text(
                        "Firebase pendiente: agrega app/google-services.json para activar el acceso real.",
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall) }
            message?.let { Text(it, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { focus.clearFocus(); onLogin(email, password) },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(13.dp)
            ) {
                if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(22.dp))
                else Text("Iniciar sesión  →", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta?", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onRegister, enabled = !loading) { Text("Regístrate") }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
