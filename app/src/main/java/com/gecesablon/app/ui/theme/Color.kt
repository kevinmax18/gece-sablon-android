package com.gecesablon.app.ui.theme

import androidx.compose.ui.graphics.Color

// WARNA RESMI GECE SCREEN PRINTING (DESIGN.md)
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

/**
 * Objek warna tambahan untuk token desain spesifik GeCe:
 * - Aksen kuning emulsi
 * - Border hairline
 * - Status warna pesanan & tahapan produksi
 */
object GeceColors {
    val Accent = AccentYellow
    val OnAccent = InkBlack
    val PrimaryInk = InkBlack
    val Background = BackgroundCanvas
    val Surface = SurfaceCard
    val Border = BorderHairline
    val TextMain = TextPrimary
    val TextMuted = TextSecondary

    // Warna Status Pesanan (OrderStatus)
    val StatusPendingBg = AccentYellow
    val StatusPendingText = InkBlack

    val StatusProcessBg = InkBlack
    val StatusProcessText = BackgroundCanvas

    val StatusReadyBg = SurfaceVariantColor
    val StatusReadyText = TextPrimary

    val StatusSuccessBg = Color(0xFFE6F4EA)
    val StatusSuccessText = Color(0xFF137333)

    val StatusCancelledBg = Color(0xFFFCE8E6)
    val StatusCancelledText = Color(0xFFC5221F)
}
