package com.gecesablon.app.data.model

/**
 * Tipe transaksi keuangan GeCe Sablon.
 */
enum class TransactionType {
    PEMASUKAN,
    PENGELUARAN
}

/**
 * Kategori transaksi keuangan GeCe Sablon.
 * Sesuai revisi: Kategori LAINNYA dipisah menjadi PENGELUARAN_LAINNYA dan PEMASUKAN_LAINNYA
 * agar tidak tercampur saat pencatatan dan pelaporan.
 */
enum class TransactionCategory {
    // Kategori Pengeluaran
    BAHAN_BAKU,
    MATERIAL_DTF,
    OPERASIONAL,
    GAJI,
    PENGELUARAN_LAINNYA,

    // Kategori Pemasukan Manual
    PENJUALAN_LAIN,
    PEMASUKAN_LAINNYA,

    // Kategori Pemasukan Otomatis dari Pesanan Selesai
    PESANAN;

    companion object {
        val expenseCategories = listOf(
            BAHAN_BAKU,
            MATERIAL_DTF,
            OPERASIONAL,
            GAJI,
            PENGELUARAN_LAINNYA
        )

        val manualIncomeCategories = listOf(
            PENJUALAN_LAIN,
            PEMASUKAN_LAINNYA
        )
    }
}

/**
 * Sumber pencatatan transaksi:
 * MANUAL: Dicatat secara manual oleh Owner workshop
 * PESANAN: Dihasilkan secara otomatis dari pesanan yang berstatus SELESAI
 */
enum class TransactionSource {
    MANUAL,
    PESANAN
}

/**
 * Entitas data transaksi keuangan.
 *
 * @param id          ID unik transaksi (UUID untuk manual, "order-<orderId>" untuk virtual pesanan)
 * @param tipe        Tipe transaksi (PEMASUKAN atau PENGELUARAN)
 * @param kategori    Kategori transaksi
 * @param nominal     Nominal rupiah (bulat, > 0, maks 1.000.000.000)
 * @param tanggal     Waktu transaksi (epoch millisecond)
 * @param keterangan  Deskripsi atau catatan transaksi (maks 200 karakter)
 * @param sumber      Asal transaksi (MANUAL atau PESANAN)
 * @param orderId     ID pesanan terkait (hanya jika sumber PESANAN)
 * @param dibuatOleh  Nama dan peran staf/owner pencatat
 */
data class Transaction(
    val id: String = "",
    val tipe: TransactionType,
    val kategori: TransactionCategory,
    val nominal: Long,
    val tanggal: Long,
    val keterangan: String,
    val sumber: TransactionSource = TransactionSource.MANUAL,
    val orderId: String? = null,
    val dibuatOleh: String
)

/**
 * Mengembalikan label tampilan (Bahasa Indonesia) dari [TransactionCategory].
 */
fun TransactionCategory.displayName(): String = when (this) {
    TransactionCategory.BAHAN_BAKU -> "Bahan Baku"
    TransactionCategory.MATERIAL_DTF -> "Material DTF"
    TransactionCategory.OPERASIONAL -> "Operasional"
    TransactionCategory.GAJI -> "Gaji"
    TransactionCategory.PENGELUARAN_LAINNYA -> "Pengeluaran Lainnya"
    TransactionCategory.PENJUALAN_LAIN -> "Penjualan Lain"
    TransactionCategory.PEMASUKAN_LAINNYA -> "Pemasukan Lainnya"
    TransactionCategory.PESANAN -> "Pesanan"
}
