package com.cinetrack.app.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DeleteAccountScreen(
    deleting: Boolean,
    error: String?,
    onBack: () -> Unit,
    onDelete: (String) -> Unit,
    onClearError: () -> Unit
) {
    var confirm by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { if (!deleting) confirm = false },
            title = { Text("Eliminar cuenta definitivamente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Se eliminarán tu cuenta de acceso, perfil público, reseñas públicas y datos privados guardados por CineTrack en este dispositivo. Esta acción no se puede deshacer."
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; onClearError() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Contraseña actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !deleting
                    )
                    Text(
                        "La contraseña se usa únicamente para volver a verificar tu identidad con Firebase antes de borrar datos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val value = password
                        confirm = false
                        onDelete(value)
                    },
                    enabled = !deleting && password.isNotBlank()
                ) { Text("Eliminar definitivamente", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }, enabled = !deleting) { Text("Cancelar") }
            }
        )
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !deleting) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Atrás")
            }
            Text("Eliminar cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(28.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = .10f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Esta acción es permanente", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Text(
                    "CineTrack verifica primero tu identidad y luego elimina los datos asociados antes de borrar el acceso de Firebase Authentication.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Se eliminará:", fontWeight = FontWeight.Bold)
                Text("• Tu perfil público y avatar asociado.")
                Text("• Tus reseñas públicas.")
                Text("• Tu colección privada local: estados, favoritos, puntuaciones y comentarios.")
                Text("• Tu cuenta de Firebase Authentication.")
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                "  Por seguridad, CineTrack pedirá tu contraseña actual antes de empezar. Así evitamos borrar datos públicos si Firebase rechazara después la eliminación de la cuenta por falta de autenticación reciente.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.error.copy(alpha = .10f)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onClearError) { Text("Cerrar mensaje") }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { confirm = true },
            enabled = !deleting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            if (deleting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Text("  Eliminando…")
            } else {
                Icon(Icons.Outlined.DeleteForever, contentDescription = null)
                Text("  Eliminar mi cuenta")
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
