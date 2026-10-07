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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cinetrack.app.ui.components.CineTrackLogo
import com.cinetrack.app.ui.theme.CineSuccessContent

@Composable
fun RegisterScreen(
    loading: Boolean,
    error: String?,
    onRegister: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit,
    onEdit: () -> Unit
) {
    var name by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var email by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var password by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var confirmation by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var showPassword by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var acceptedTerms by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val emailLooksValid = email.contains("@") && email.substringAfter("@", "").contains(".")
    val passwordStrength = when {
        password.length >= 10 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 1f
        password.length >= 8 -> 0.66f
        password.length >= 6 -> 0.33f
        else -> 0f
    }
    val strengthLabel = when {
        passwordStrength >= 1f -> "Alta"
        passwordStrength >= 0.66f -> "Media"
        passwordStrength > 0f -> "Básica"
        else -> "—"
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))
        CineTrackLogo(compact = true)
        Spacer(Modifier.height(22.dp))
        Text("Crear cuenta", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth())
        Text("Únete y empieza a organizar tus historias", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))

        AuthLabel("Nombre completo")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(40); onEdit() },
            leadingIcon = { Icon(Icons.Outlined.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !loading,
            shape = RoundedCornerShape(13.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(12.dp))

        AuthLabel("Correo electrónico")
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; onEdit() },
            leadingIcon = { Icon(Icons.Outlined.Email, null) },
            trailingIcon = if (emailLooksValid) {
                { Icon(Icons.Filled.CheckCircle, contentDescription = "Formato de correo válido", tint = CineSuccessContent) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !loading,
            shape = RoundedCornerShape(13.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        if (emailLooksValid) {
            Text("Formato válido", color = CineSuccessContent, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        }
        Spacer(Modifier.height(12.dp))

        AuthLabel("Contraseña")
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; onEdit() },
            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !loading,
            shape = RoundedCornerShape(13.dp),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
        )
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(progress = { passwordStrength }, modifier = Modifier.weight(1f).height(4.dp))
            Spacer(Modifier.width(10.dp))
            Text("Seguridad: $strengthLabel", style = MaterialTheme.typography.labelSmall, color = if (passwordStrength >= .66f) CineSuccessContent else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))

        AuthLabel("Confirmar contraseña")
        OutlinedTextField(
            value = confirmation,
            onValueChange = { confirmation = it; onEdit() },
            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
            trailingIcon = if (confirmation.isNotBlank() && confirmation == password) {
                { Icon(Icons.Filled.CheckCircle, contentDescription = "Contraseñas coinciden", tint = CineSuccessContent) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !loading,
            shape = RoundedCornerShape(13.dp),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focus.clearFocus()
                if (acceptedTerms) onRegister(name, email, password, confirmation)
            })
        )

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it }, enabled = !loading)
            Text(
                "Acepto los Términos y condiciones y la Política de privacidad.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { focus.clearFocus(); onRegister(name, email, password, confirmation) },
            enabled = !loading && acceptedTerms,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(13.dp)
        ) {
            if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(22.dp)) else Text("Crear cuenta  →", fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onBackToLogin, enabled = !loading) { Text("Inicia sesión") }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun AuthLabel(text: String) {
    Text(text, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
}

