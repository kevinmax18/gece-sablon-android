package com.gecesablon.app.core.util

/**
 * Utilitas murni untuk perhitungan harga produk dan pesanan sablon DTF GeCe Screen Printing.
 * Menjamin rumus harga konsisten di satu tempat dan tidak diletakkan di UI.
 *
 * Rumus:
 *   hargaSatuan = hargaDasar + tambahanUkuran
 *   totalHarga  = hargaSatuan * jumlah
 */
object PriceCalculator {

    /**
     * Menghitung harga satuan per pcs dari harga dasar produk ditambah biaya variasi ukuran.
     *
     * @param basePrice Harga dasar produk (Rupiah)
     * @param sizeExtra Additional cost untuk ukuran khusus (misal XXL +Rp5.000)
     * @return Harga satuan per pcs
     */
    fun calculateUnitPrice(basePrice: Long, sizeExtra: Long): Long {
        val safeBase = if (basePrice < 0L) 0L else basePrice
        val safeExtra = if (sizeExtra < 0L) 0L else sizeExtra
        return safeBase + safeExtra
    }

    /**
     * Menghitung total harga pesanan dari harga satuan dikalikan dengan jumlah pcs.
     *
     * @param unitPrice Harga satuan per pcs
     * @param quantity Jumlah pesanan (pcs, 1-999)
     * @return Total harga keseluruhan dalam Rupiah
     */
    fun calculateTotalPrice(unitPrice: Long, quantity: Int): Long {
        val safeUnitPrice = if (unitPrice < 0L) 0L else unitPrice
        val safeQuantity = if (quantity < 0) 0 else quantity
        return safeUnitPrice * safeQuantity
    }
}
