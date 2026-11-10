package com.gecesablon.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GeceLightColorScheme = lightColorScheme(
    primary = InkBlack,
    onPrimary = BackgroundCanvas,
    primaryContainer = SurfaceCard,
    onPrimaryContainer = TextPrimary,
    secondary = AccentYellow,
    onSecondary = InkBlack,
    secondaryContainer = AccentYellow,
    onSecondaryContainer = InkBlack,
    tertiary = InkBlack,
    onTertiary = BackgroundCanvas,
    background = BackgroundCanvas,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantColor,
    onSurfaceVariant = TextSecondary,
    outline = BorderHairline,
    outlineVariant = BorderHairline,
    error = ErrorRed,
    onError = White
)

@Composable
fun GeceSablonTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = GeceLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BackgroundCanvas.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
