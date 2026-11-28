package com.gecesablon.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.gecesablon.app.core.util.AppPreferencesRules
import com.gecesablon.app.data.model.ThemeMode

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

private val GeceDarkColorScheme = darkColorScheme(
    primary = DarkInkPrimary,
    onPrimary = InkBlack,
    primaryContainer = DarkSurfaceCard,
    onPrimaryContainer = DarkTextPrimary,
    secondary = AccentYellow,
    onSecondary = InkBlack,
    secondaryContainer = AccentYellow,
    onSecondaryContainer = InkBlack,
    tertiary = DarkInkPrimary,
    onTertiary = InkBlack,
    background = DarkBackgroundCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariantColor,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderHairline,
    outlineVariant = DarkBorderHairline,
    error = DarkErrorRed,
    onError = DarkOnError
)

@Composable
fun GeceSablonTheme(
    themeMode: ThemeMode = ThemeMode.SISTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = AppPreferencesRules.isDarkTheme(
        mode = themeMode,
        isSystemInDarkTheme = isSystemInDarkTheme()
    )
    val colorScheme = if (darkTheme) GeceDarkColorScheme else GeceLightColorScheme
    val gecePalette = if (darkTheme) DarkGeceColorPalette else LightGeceColorPalette

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalGeceColorPalette provides gecePalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
