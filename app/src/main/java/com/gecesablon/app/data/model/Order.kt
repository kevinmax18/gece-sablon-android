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
 * @param selesaiPada    Timestamp epoch millisecond saat pesanan ditandai selesai (nullable)
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
    val selesaiPada: Long? = null,
    val items: List<OrderItem> = emptyList(),
    val produksi: ProductionState = ProductionState(),
    val riwayat: List<ActivityLogEntry> = emptyList()
)

/**
 * Membentuk entitas [Transaction] virtual dari pesanan berstatus SELESAI.
 * Mengembalikan null jika pesanan bukan berstatus SELESAI atau [Order.selesaiPada] adalah null.
 * Memakai prefix ID "order-<orderId>".
 */
fun Order.toVirtualTransactionOrNull(): Transaction? {
    if (this.status != OrderStatus.SELESAI) return null
    val timestampSelesai = this.selesaiPada ?: return null

    return Transaction(
        id = "order-${this.id}",
        tipe = TransactionType.PEMASUKAN,
        kategori = TransactionCategory.PESANAN,
        nominal = this.total,
        tanggal = timestampSelesai,
        keterangan = "Pesanan ${this.kode} - ${this.namaPelanggan}",
        sumber = TransactionSource.PESANAN,
        orderId = this.id,
        dibuatOleh = "Sistem (Pesanan Selesai)"
    )
}
