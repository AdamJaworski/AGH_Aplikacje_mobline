package com.example.apkmoblineprojekt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BliskoColorScheme = lightColorScheme(
    primary = BliskoColors.Forest,
    onPrimary = Color.White,
    secondary = BliskoColors.Coral,
    onSecondary = Color.White,
    background = BliskoColors.Surface,
    onBackground = BliskoColors.Ink,
    surface = BliskoColors.Surface,
    onSurface = BliskoColors.Ink,
    onSurfaceVariant = BliskoColors.Muted,
    surfaceContainerLowest = BliskoColors.Card,
    surfaceContainerLow = BliskoColors.Card,
    surfaceContainer = BliskoColors.Card,
    surfaceContainerHigh = BliskoColors.Card,
    surfaceContainerHighest = BliskoColors.Card,
    outline = BliskoColors.Line,
    outlineVariant = BliskoColors.Line,
)

/** Makieta ma tylko jasny motyw. */
@Composable
fun BliskoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BliskoColorScheme,
        typography = BliskoTypography,
        content = content,
    )
}
