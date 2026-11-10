package com.gecesablon.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Bentuk (Shapes) resmi GeCe Screen Printing sesuai DESIGN.md:
 * - sm: 4.dp (checkbox, indikator kecil)
 * - DEFAULT: 8.dp (badge, chip teknis, selector)
 * - md: 12.dp (tombol, text field, kontrol interaktif)
 * - lg: 16.dp (kartu pesanan & katalog)
 * - xl: 24.dp (bottom sheet container)
 * - full: 9999.dp (pill chips)
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val ShapeControl = RoundedCornerShape(12.dp)
val ShapeCard = RoundedCornerShape(16.dp)
val ShapeBadge = RoundedCornerShape(8.dp)
val ShapePill = RoundedCornerShape(9999.dp)
