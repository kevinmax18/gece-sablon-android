package com.gecesablon.app.data.model

/**
 * Model data yang merepresentasikan entitas pesanan (Order).
 *
 * @param id         ID unik pesanan (misal UUID)
 * @param kode       Kode pesanan berformat GEC-YYYYMMDD-NNN
 * @param userId     ID pemilik akun pemesan (pelanggan)
 * @param status     Status tahapan pesanan resmi OrderStatus
 * @param total      Total biaya pesanan dalam Rupiah
 * @param catatan    Catatan instruksi pelanggan (posisi cetak, referensi, dll)
 * @param uriDesain  String URI atau path file desain yang diunggah
 * @param dibuatPada Timestamp epoch millisecond saat pesanan dibuat
 * @param items      Daftar item pesanan (OrderItem)
 */
data class Order(
    val id: String = "",
    val kode: String,
    val userId: String,
    val status: OrderStatus,
    val total: Long,
    val catatan: String = "",
    val uriDesain: String? = null,
    val dibuatPada: Long = System.currentTimeMillis(),
    val items: List<OrderItem> = emptyList()
)
