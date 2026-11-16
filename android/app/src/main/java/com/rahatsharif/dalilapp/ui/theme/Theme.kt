package com.rahatsharif.dalilapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rahatsharif.dalilapp.model.ThemePreference

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = PrimaryDarkBlue,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = CardBackground,
    onSurface = TextDark,
    surfaceVariant = CardSubtleLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = RedDelete
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDarkMode,
    onPrimary = Color(0xFF0B192C),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = SecondaryTealDarkMode,
    onSecondary = Color(0xFF042F2E),
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = CardSubtleDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderColorDark,
    error = RedDeleteDarkMode
)

@Composable
fun DalilFigureTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val barColor = colorScheme.surface.toArgb()
                window.statusBarColor = barColor
                window.navigationBarColor = barColor
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    insetsController.isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
