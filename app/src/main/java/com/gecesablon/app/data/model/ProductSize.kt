package com.gecesablon.app.data.model

/**
 * Model data ukuran produk beserta tambahan harga per ukuran.
 * @param label Label ukuran (S, M, L, XL, XXL, XXXL).
 * @param tambahanHarga Biaya tambahan untuk ukuran tertentu (misal XXL +5.000, XXXL +10.000).
 */
data class ProductSize(
    val label: String,
    val tambahanHarga: Long = 0L
)
