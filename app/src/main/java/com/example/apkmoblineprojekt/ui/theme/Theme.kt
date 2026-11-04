package com.example.apkmoblineprojekt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = ForestGreenLight,
    onPrimaryContainer = ForestGreen,
    secondary = AccentCoral,
    onSecondary = Color.White,
    secondaryContainer = AccentCoralSoft,
    onSecondaryContainer = AccentCoral,
    background = BgWarmOffWhite,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfacePillGray,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF76B59C),
    onPrimary = Color(0xFF0A291F),
    primaryContainer = ForestGreen,
    onPrimaryContainer = Color(0xFFD6EBE2),
    secondary = AccentCoral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF5A271B),
    onSecondaryContainer = AccentCoralBorder,
    background = Color(0xFF121413),
    onBackground = Color(0xFFE2E3E0),
    surface = Color(0xFF1C1F1D),
    onSurface = Color(0xFFE2E3E0),
    surfaceVariant = Color(0xFF2B2F2C),
    onSurfaceVariant = Color(0xFFA5AAA6),
    outline = Color(0xFF383C39)
)

@Composable
fun GeoPlanerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}