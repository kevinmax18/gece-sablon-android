package com.gecesablon.app.data.model

/**
 * Model data warna produk untuk tampilan swatch dan informasi visual kain.
 * @param nama Nama warna kain (contoh: Hitam Pekat, Broken White).
 * @param hex Kode hex warna kain (contoh: #111111).
 */
data class ProductColor(
    val nama: String,
    val hex: String
)
