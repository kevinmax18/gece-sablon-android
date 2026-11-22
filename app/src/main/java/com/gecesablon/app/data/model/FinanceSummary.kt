package com.gecesablon.app.data.model

/**
 * Ringkasan kalkulasi keuangan pada satu periode waktu.
 *
 * @param totalPemasukan       Total keseluruhan pemasukan (manual + pesanan selesai)
 * @param totalPengeluaran     Total keseluruhan pengeluaran manual
 * @param labaRugi             Total pemasukan dikurangi total pengeluaran (bisa bernilai negatif jika rugi)
 * @param jumlahPesananSelesai Jumlah pesanan berstatus SELESAI yang selesai pada periode tersebut
 * @param categoryBreakdown    Rincian akumulasi nominal per kategori transaksi
 */
data class FinanceSummary(
    val totalPemasukan: Long = 0L,
    val totalPengeluaran: Long = 0L,
    val labaRugi: Long = 0L,
    val jumlahPesananSelesai: Int = 0,
    val categoryBreakdown: Map<TransactionCategory, Long> = emptyMap()
)
