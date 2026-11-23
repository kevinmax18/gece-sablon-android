package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.DateRange
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.FinanceSummary
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.SixMonthTrend
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository untuk modul keuangan GeCe Sablon.
 * Hanya dapat diakses oleh pengguna dengan peran OWNER.
 * Semua fungsi menerima [actor] dan menolak peran selain OWNER.
 *
 * TODO: ganti ke Retrofit di Tahap 8
 */
interface FinanceRepository {

    /**
     * Mengalirkan daftar gabungan transaksi (manual + virtual dari pesanan SELESAI)
     * yang jatuh dalam rentang periode [range] secara reaktif.
     *
     * Transaksi virtual pesanan memakai ID "order-<orderId>".
     * Pesanan SELESAI tanpa selesaiPada tidak dimasukkan.
     *
     * @param actor Sesi pengguna yang meminta (hanya OWNER)
     * @param range Rentang periode [DateRange]
     */
    fun getTransactions(
        actor: Session,
        range: DateRange
    ): Flow<Resource<List<Transaction>>>

    /**
     * Mengambil satu transaksi berdasarkan ID.
     * Mendukung ID virtual "order-<orderId>" dari pesanan selesai.
     *
     * @param id    ID transaksi (UUID untuk manual, "order-<orderId>" untuk virtual)
     * @param actor Sesi pengguna yang meminta (hanya OWNER)
     */
    suspend fun getTransactionById(id: String, actor: Session): Resource<Transaction>

    /**
     * Mencatat transaksi keuangan manual baru.
     * Validasi:
     * - actor harus OWNER
     * - nominal bulat > 0 dan ≤ 1.000.000.000
     * - keterangan maks 200 karakter
     * - kategori sesuai tipe (pengeluaran / pemasukan manual)
     *
     * @param actor      Sesi Owner pencatat
     * @param tipe       Tipe transaksi (PEMASUKAN atau PENGELUARAN)
     * @param kategori   Kategori transaksi
     * @param nominal    Jumlah rupiah
     * @param tanggal    Waktu transaksi (epoch ms)
     * @param keterangan Catatan atau deskripsi transaksi
     */
    suspend fun createTransaction(
        actor: Session,
        tipe: TransactionType,
        kategori: TransactionCategory,
        nominal: Long,
        tanggal: Long,
        keterangan: String
    ): Resource<Transaction>

    /**
     * Memperbarui transaksi manual yang sudah ada.
     * Transaksi sumber PESANAN tidak dapat diubah.
     *
     * @param id         ID transaksi yang akan diubah
     * @param actor      Sesi Owner yang meminta perubahan
     * @param tipe       Tipe transaksi baru
     * @param kategori   Kategori transaksi baru
     * @param nominal    Nominal baru
     * @param tanggal    Tanggal transaksi baru
     * @param keterangan Keterangan baru
     */
    suspend fun updateTransaction(
        id: String,
        actor: Session,
        tipe: TransactionType,
        kategori: TransactionCategory,
        nominal: Long,
        tanggal: Long,
        keterangan: String
    ): Resource<Transaction>

    /**
     * Menghapus transaksi manual.
     * Transaksi sumber PESANAN tidak dapat dihapus.
     *
     * @param id    ID transaksi yang akan dihapus
     * @param actor Sesi Owner yang meminta penghapusan
     */
    suspend fun deleteTransaction(id: String, actor: Session): Resource<Unit>

    /**
     * Mengalirkan ringkasan keuangan ([FinanceSummary]) untuk suatu periode secara reaktif.
     * Ikut berubah saat pesanan baru ditandai selesai.
     *
     * @param actor Sesi pengguna (hanya OWNER)
     * @param range Rentang periode
     */
    fun getFinanceSummary(
        actor: Session,
        range: DateRange
    ): Flow<Resource<FinanceSummary>>

    /**
     * Mengalirkan ringkasan dan tren keuangan 6 bulan terakhir ([SixMonthTrend])
     * secara reaktif menggunakan combine pada transaksi dan pesanan.
     *
     * @param actor Sesi pengguna (hanya OWNER)
     */
    fun getSixMonthTrend(
        actor: Session
    ): Flow<Resource<SixMonthTrend>>
}
