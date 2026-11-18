package com.gecesablon.app.data.model

/**
 * Model data yang merepresentasikan entitas pesanan (Order).
 *
 * @param id             ID unik pesanan (misal UUID)
 * @param kode           Kode pesanan berformat GEC-YYYYMMDD-NNN
 * @param namaPelanggan  Nama pemesan/pelanggan
 * @param noHpPelanggan  Nomor WhatsApp/HP pelanggan (opsional/nullable)
 * @param dibuatOleh     Asal pembuatan pesanan (CUSTOMER atau STAFF)
 * @param userId         ID pemilik akun pemesan (nullable untuk pesanan manual)
 * @param status         Status tahapan pesanan resmi OrderStatus
 * @param total          Total biaya pesanan dalam Rupiah
 * @param catatan        Catatan instruksi pelanggan (posisi cetak, referensi, dll)
 * @param uriDesain      String URI atau path file desain yang diunggah
 * @param dibuatPada     Timestamp epoch millisecond saat pesanan dibuat
 * @param items          Daftar item pesanan (OrderItem)
 * @param produksi       Kondisi tahapan produksi internal (ProductionState)
 * @param riwayat        Daftar linimasa aktivitas pesanan (ActivityLogEntry)
 */
data class Order(
    val id: String = "",
    val kode: String,
    val namaPelanggan: String,
    val noHpPelanggan: String? = null,
    val dibuatOleh: OrderCreator = OrderCreator.CUSTOMER,
    val userId: String? = null,
    val status: OrderStatus,
    val total: Long,
    val catatan: String = "",
    val uriDesain: String? = null,
    val dibuatPada: Long = System.currentTimeMillis(),
    val items: List<OrderItem> = emptyList(),
    val produksi: ProductionState = ProductionState(),
    val riwayat: List<ActivityLogEntry> = emptyList()
)
