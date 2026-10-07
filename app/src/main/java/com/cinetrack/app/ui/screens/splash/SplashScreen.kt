package com.cinetrack.app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cinetrack.app.ui.components.CineTrackLogo

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), Color.Transparent), radius = 800f)
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)) {
                CineTrackLogo(Modifier.background(Color.Transparent))
            }
            Spacer(Modifier.height(18.dp))
            Text("Tu próxima historia está aquí", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(strokeWidth = 2.dp)
        }
    }
}
