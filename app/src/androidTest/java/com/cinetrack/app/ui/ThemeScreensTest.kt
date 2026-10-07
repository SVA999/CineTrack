package com.cinetrack.app.ui

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.cinetrack.app.data.model.ThemePreference
import com.cinetrack.app.ui.theme.CineTrackTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

internal var testTheme = ThemePreference.DARK

class ThemeScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun detailBackdropDoesNotDuplicatePlaceholder() {
        var mode by mutableStateOf(ThemePreference.DARK)
        compose.setContent {
            testTheme = mode
            CineTrackTheme(themePreference = mode) {
                Box(Modifier.fillMaxSize().safeDrawingPadding()) { key(mode) { DetailPreview() } }
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(context.getExternalFilesDir(null), "detail-final").apply { mkdirs() }
        for (theme in listOf(ThemePreference.DARK, ThemePreference.LIGHT)) {
            compose.runOnIdle { mode = theme }
            compose.waitForIdle()
            // Only the foreground poster may show placeholder initials.
            compose.onAllNodesWithText("ÓR").assertCountEquals(1)
            compose.onNodeWithText("Órbita Cero").assertIsDisplayed()
            val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
            File(output, "${theme.name.lowercase()}-detail.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
    @Test fun allPreviewScreensRenderInBothThemes() {
        val scenes: List<Pair<String, @Composable () -> Unit>> = listOf(
            "home" to { ExplorePreview() }, "search" to { SearchPreview() },
            "detail" to { DetailPreview() }, "my-list" to { MyListPreview() },
            "login" to { LoginPreview() }, "password" to { ForgotPasswordPreview() },
            "register" to { RegisterPreview() }, "profile" to { ProfilePreview() },
            "public-profile" to { PublicProfilePreview() }, "review" to { ReviewPreview() },
            "settings" to { SettingsPreview() }, "credits" to { CreditsPreview() }, "edit-profile" to { EditProfilePreview() }, "delete-account" to { DeleteAccountPreview() }, "shared-review" to { SharedReviewPreview() }
        )
        var scene by mutableStateOf(0)
        var mode by mutableStateOf(ThemePreference.DARK)
        compose.setContent {
            testTheme = mode
            CineTrackTheme(themePreference = mode) { Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                key(scene, mode) { scenes[scene].second() } }
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(context.getExternalFilesDir(null), "theme-screens").apply { mkdirs() }
        for (theme in listOf(ThemePreference.DARK, ThemePreference.LIGHT)) {
            for (index in scenes.indices) {
                compose.runOnIdle { mode = theme; scene = index }
                compose.waitForIdle()
                compose.onRoot().assertIsDisplayed()
                val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
                File(output, "${theme.name.lowercase()}-${scenes[index].first}.png").outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                // These controls used to inherit black on a dark background.
                if (scenes[index].first == "detail") {
                    compose.onNodeWithContentDescription("Atrás").assertIsDisplayed()
                    compose.onNodeWithText("Sinopsis").performScrollTo().assertIsDisplayed()
                }
                if (scenes[index].first == "credits") {
                    compose.onNodeWithContentDescription("Atrás").assertIsDisplayed()
                }
            }
        }
    }

    @Test fun rootProvidesReadableContentAcrossThemeChanges() {
        var mode by mutableStateOf(ThemePreference.DARK)
        var checked = 0
        compose.setContent {
            CineTrackTheme(themePreference = mode) {
                val foreground = LocalContentColor.current
                val colors = MaterialTheme.colorScheme
                SideEffect {
                    assertEquals(colors.onBackground, foreground)
                    val light = maxOf(foreground.luminance(), colors.background.luminance())
                    val dark = minOf(foreground.luminance(), colors.background.luminance())
                    assertTrue("Root text contrast must be at least 4.5:1", (light + .05f) / (dark + .05f) >= 4.5f)
                    checked++
                }
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { mode = ThemePreference.LIGHT }
        compose.waitForIdle()
        compose.runOnIdle { mode = ThemePreference.SYSTEM }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(checked >= 3) }
    }
}



