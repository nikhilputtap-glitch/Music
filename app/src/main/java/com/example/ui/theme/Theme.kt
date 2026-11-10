package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = AmoledBlack,
    primaryContainer = DeepPurple,
    onPrimaryContainer = TextPrimary,
    secondary = NeonPurple,
    onSecondary = TextPrimary,
    secondaryContainer = AmoledCard,
    onSecondaryContainer = NeonPurpleBright,
    tertiary = NeonMagenta,
    onTertiary = TextPrimary,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = AmoledDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = AmoledCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassBorderPurple,
    error = NeonRed,
    onError = TextPrimary
)

@Composable
fun UnlockTunesTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AmoledBlack.toArgb()
                window.navigationBarColor = AmoledBlack.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
