package com.gecesablon.app.data.model

/**
 * Model data yang merepresentasikan rincian item dalam pesanan (OrderItem).
 * Tidak menyimpan objek Product utuh, hanya salinan data yang relevan saat pemesanan dibuat.
 *
 * @param id          ID item (opsional/palsu)
 * @param productId   ID produk yang dipesan
 * @param namaProduk  Nama produk saat pemesanan
 * @param warnaNama   Nama varian warna yang dipilih
 * @param ukuranLabel Label ukuran yang dipilih (S, M, L, XL, XXL, XXXL, dll)
 * @param jumlah      Jumlah pemesanan (pcs)
 * @param hargaSatuan Harga satuan (harga dasar + tambahan ukuran)
 */
data class OrderItem(
    val id: String = "",
    val productId: String,
    val namaProduk: String,
    val warnaNama: String,
    val ukuranLabel: String,
    val jumlah: Int,
    val hargaSatuan: Long
)
