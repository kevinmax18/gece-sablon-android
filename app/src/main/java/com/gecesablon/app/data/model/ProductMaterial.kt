package com.gecesablon.app.data.model

/**
 * Model data pilihan bahan kain per produk.
 *
 * @param id Identifier unik bahan (stabil saat diubah, misal "mat-1", "mat-2", atau UUID)
 * @param nama Nama jenis bahan kain (2-80 karakter, unik per produk tanpa membedakan huruf besar-kecil)
 * @param tambahanHarga Biaya tambahan per pcs untuk bahan tertentu (0 - 10.000.000)
 * @param isDefault Penanda apakah bahan ini terpilih secara default pada produk (tepat 1 per produk bernilai true)
 */
data class ProductMaterial(
    val id: String,
    val nama: String,
    val tambahanHarga: Long = 0L,
    val isDefault: Boolean = false
)
