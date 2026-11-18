package com.gecesablon.app.data.model

import java.util.UUID

/**
 * Model data yang merepresentasikan persediaan bahan baku / kaos polos (StockItem).
 *
 * @param id           Identifier unik item stok
 * @param nama         Nama material / bahan baku
 * @param kategori     Kategori persediaan [StockCategory]
 * @param satuan       Satuan stok (misal: "pcs", "lembar", "meter", "roll")
 * @param jumlah       Jumlah stok fisik saat ini (>= 0)
 * @param batasMinimum Batas ambang batas persediaan menipis (>= 0)
 */
data class StockItem(
    val id: String = UUID.randomUUID().toString(),
    val nama: String,
    val kategori: StockCategory,
    val satuan: String,
    val jumlah: Int,
    val batasMinimum: Int
) {
    /**
     * Menandakan apakah stok telah berada pada atau di bawah batas minimum persediaan.
     */
    val isMenipis: Boolean
        get() = jumlah <= batasMinimum
}
