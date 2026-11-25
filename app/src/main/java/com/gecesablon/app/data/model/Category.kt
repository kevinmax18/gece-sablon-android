package com.gecesablon.app.data.model

/**
 * Model data yang merepresentasikan kategori produk sablon GeCe.
 * @param id Identifier unik kategori (stabil saat nama diubah).
 * @param nama Nama kategori produk (misal: "Kaos Custom", "Kaos Oversize", "Baju Polo").
 */
data class Category(
    val id: String,
    val nama: String
)
