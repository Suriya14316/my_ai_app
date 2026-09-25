package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SiriCyan,
    onPrimary = Color.Black,
    primaryContainer = SiriBlue.copy(alpha = 0.25f),
    onPrimaryContainer = Color.White,
    secondary = SiriPurple,
    onSecondary = Color.White,
    secondaryContainer = SiriPurple.copy(alpha = 0.2f),
    onSecondaryContainer = Color.White,
    tertiary = SiriPink,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // SAYA is strictly designed as an Apple Intelligence Dark-Mode experience
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
