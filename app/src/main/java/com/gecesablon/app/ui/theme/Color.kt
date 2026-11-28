package com.gecesablon.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// WARNA RESMI GECE SCREEN PRINTING (DESIGN.md - Tema Terang)
val InkBlack = Color(0xFF111111)
val BackgroundCanvas = Color(0xFFFAFAF7)
val SurfaceCard = Color(0xFFF1F1ED)
val SurfaceVariantColor = Color(0xFFE5E2E1)
val TextPrimary = Color(0xFF171717)
val TextSecondary = Color(0xFF737373)
val AccentYellow = Color(0xFFF5E85B)
val BorderHairline = Color(0xFFE5E5E0)
val ErrorRed = Color(0xFFBA1A1A)
val White = Color(0xFFFFFFFF)

// WARNA TURUNAN TEMA GELAP (diturunkan dari skema terang di DESIGN.md)
val DarkBackgroundCanvas = Color(0xFF121210)
val DarkSurfaceCard = Color(0xFF1D1D1A)
val DarkSurfaceVariantColor = Color(0xFF2A2A26)
val DarkInkPrimary = Color(0xFFF3F3EE)
val DarkTextPrimary = Color(0xFFF3F3EE)
val DarkTextSecondary = Color(0xFFA6A69E)
val DarkBorderHairline = Color(0xFF2E2E29)
val DarkErrorRed = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)

@Immutable
data class GeceColorPalette(
    val accent: Color,
    val onAccent: Color,
    val primaryInk: Color,
    val background: Color,
    val surface: Color,
    val border: Color,
    val textMain: Color,
    val textMuted: Color,
    val statusPendingBg: Color,
    val statusPendingText: Color,
    val statusProcessBg: Color,
    val statusProcessText: Color,
    val statusReadyBg: Color,
    val statusReadyText: Color,
    val statusSuccessBg: Color,
    val statusSuccessText: Color,
    val statusCancelledBg: Color,
    val statusCancelledText: Color,
    val highlightContainer: Color,
    val onHighlight: Color,
    val onHighlightMuted: Color,
    val highlightAccent: Color
)

val LightGeceColorPalette = GeceColorPalette(
    accent = AccentYellow,
    onAccent = InkBlack,
    primaryInk = InkBlack,
    background = BackgroundCanvas,
    surface = SurfaceCard,
    border = BorderHairline,
    textMain = TextPrimary,
    textMuted = TextSecondary,
    statusPendingBg = AccentYellow,
    statusPendingText = InkBlack,
    statusProcessBg = InkBlack,
    statusProcessText = BackgroundCanvas,
    statusReadyBg = SurfaceVariantColor,
    statusReadyText = TextPrimary,
    statusSuccessBg = Color(0xFFE6F4EA),
    statusSuccessText = Color(0xFF137333),
    statusCancelledBg = Color(0xFFFCE8E6),
    statusCancelledText = Color(0xFFC5221F),
    highlightContainer = InkBlack,
    onHighlight = BackgroundCanvas,
    onHighlightMuted = Color(0xFFD2D2CC),
    highlightAccent = AccentYellow
)

val DarkGeceColorPalette = GeceColorPalette(
    accent = AccentYellow,
    onAccent = InkBlack,
    primaryInk = DarkInkPrimary,
    background = DarkBackgroundCanvas,
    surface = DarkSurfaceCard,
    border = DarkBorderHairline,
    textMain = DarkTextPrimary,
    textMuted = DarkTextSecondary,
    statusPendingBg = AccentYellow,
    statusPendingText = InkBlack,
    statusProcessBg = DarkInkPrimary,
    statusProcessText = DarkBackgroundCanvas,
    statusReadyBg = DarkSurfaceVariantColor,
    statusReadyText = DarkTextPrimary,
    statusSuccessBg = Color(0xFF1B3A24),
    statusSuccessText = Color(0xFF81C995),
    statusCancelledBg = Color(0xFF421A18),
    statusCancelledText = Color(0xFFF28B82),
    highlightContainer = SurfaceCard,
    onHighlight = InkBlack,
    onHighlightMuted = Color(0xFF52524E),
    highlightAccent = InkBlack
)

val LocalGeceColorPalette = staticCompositionLocalOf { LightGeceColorPalette }

/**
 * Objek warna token desain spesifik GeCe yang otomatis mengikuti tema terang/gelap aktif.
 */
object GeceColors {
    val Accent: Color @Composable get() = LocalGeceColorPalette.current.accent
    val OnAccent: Color @Composable get() = LocalGeceColorPalette.current.onAccent
    val PrimaryInk: Color @Composable get() = LocalGeceColorPalette.current.primaryInk
    val Background: Color @Composable get() = LocalGeceColorPalette.current.background
    val Surface: Color @Composable get() = LocalGeceColorPalette.current.surface
    val Border: Color @Composable get() = LocalGeceColorPalette.current.border
    val TextMain: Color @Composable get() = LocalGeceColorPalette.current.textMain
    val TextMuted: Color @Composable get() = LocalGeceColorPalette.current.textMuted

    // Warna Status Pesanan (OrderStatus)
    val StatusPendingBg: Color @Composable get() = LocalGeceColorPalette.current.statusPendingBg
    val StatusPendingText: Color @Composable get() = LocalGeceColorPalette.current.statusPendingText

    val StatusProcessBg: Color @Composable get() = LocalGeceColorPalette.current.statusProcessBg
    val StatusProcessText: Color @Composable get() = LocalGeceColorPalette.current.statusProcessText

    val StatusReadyBg: Color @Composable get() = LocalGeceColorPalette.current.statusReadyBg
    val StatusReadyText: Color @Composable get() = LocalGeceColorPalette.current.statusReadyText

    val StatusSuccessBg: Color @Composable get() = LocalGeceColorPalette.current.statusSuccessBg
    val StatusSuccessText: Color @Composable get() = LocalGeceColorPalette.current.statusSuccessText

    val StatusCancelledBg: Color @Composable get() = LocalGeceColorPalette.current.statusCancelledBg
    val StatusCancelledText: Color @Composable get() = LocalGeceColorPalette.current.statusCancelledText

    // Kartu Sorotan (Highlight / Hero Banner)
    val HighlightContainer: Color @Composable get() = LocalGeceColorPalette.current.highlightContainer
    val OnHighlight: Color @Composable get() = LocalGeceColorPalette.current.onHighlight
    val OnHighlightMuted: Color @Composable get() = LocalGeceColorPalette.current.onHighlightMuted
    val HighlightAccent: Color @Composable get() = LocalGeceColorPalette.current.highlightAccent
}
