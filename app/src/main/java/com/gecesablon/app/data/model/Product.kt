package com.gecesablon.app.data.model

/**
 * Model data yang merepresentasikan produk sablon atau pakaian.
 * @param id Identifier unik produk.
 * @param nama Nama produk (misal "Kaos Custom Cotton Combed").
 * @param kategori Kategori produk (Kaos Custom, Kaos Oversize, Baju Polo).
 * @param bahan Keterangan bahan kain (misal "Cotton Combed 24s/30s").
 * @param deskripsi Uraian deskripsi produk.
 * @param hargaDasar Harga dasar per pcs untuk ukuran standar.
 * @param gambarUrl URL atau path gambar produk (opsional/nullable).
 * @param daftarWarna Daftar pilihan warna produk.
 * @param daftarUkuran Daftar pilihan ukuran produk beserta selisih harga.
 * @param aktif Status ketersediaan produk (true = aktif di katalog pelanggan, false = nonaktif).
 */
data class Product(
    val id: String,
    val nama: String,
    val kategori: ProductCategory,
    val bahan: String,
    val deskripsi: String,
    val hargaDasar: Long,
    val gambarUrl: String? = null,
    val daftarWarna: List<ProductColor> = emptyList(),
    val daftarUkuran: List<ProductSize> = emptyList(),
    val aktif: Boolean = true
)
