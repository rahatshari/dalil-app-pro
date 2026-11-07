package com.rahatsharif.dalilapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = CardBackground,
    secondary = SecondaryTeal,
    onSecondary = CardBackground,
    background = BackgroundLight,
    surface = CardBackground,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun DalilFigureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
