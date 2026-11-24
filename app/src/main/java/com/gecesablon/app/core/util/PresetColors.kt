package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.ProductColor

/**
 * Daftar warna umum yang disediakan sebagai pintasan cepat pada form tambah/ubah produk.
 * Data ini adalah konstan murni; tidak ada dependensi UI.
 */
object PresetColors {
    val list: List<ProductColor> = listOf(
        ProductColor(nama = "Hitam",   hex = "#111111"),
        ProductColor(nama = "Putih",   hex = "#FFFFFF"),
        ProductColor(nama = "Abu-abu", hex = "#6B7280"),
        ProductColor(nama = "Navy",    hex = "#1B2A4A"),
        ProductColor(nama = "Merah",   hex = "#DC2626"),
        ProductColor(nama = "Biru",    hex = "#2563EB"),
        ProductColor(nama = "Hijau",   hex = "#16A34A"),
        ProductColor(nama = "Kuning",  hex = "#F5E85B")
    )
}
