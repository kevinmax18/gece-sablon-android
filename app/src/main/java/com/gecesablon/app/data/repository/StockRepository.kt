package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.StockItem
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository untuk mengelola inventaris persediaan stok kaos polos dan material workshop.
 * Didesain tanpa dependensi framework Android agar mudah diganti Retrofit di Tahap 8.
 *
 * TODO: ganti ke Retrofit di Tahap 8
 */
interface StockRepository {

    /**
     * Mengalirkan seluruh daftar persediaan stok secara reaktif.
     * Hanya dapat diakses oleh staf workshop (ADMIN atau OWNER).
     *
     * @param actor Sesi pengguna yang sedang aktif
     * // TODO: di API asli aktor dari token
     */
    fun getAllStock(actor: Session): Flow<Resource<List<StockItem>>>

    /**
     * Mengambil detail item stok berdasarkan ID item.
     *
     * @param id    ID item persediaan
     * @param actor Sesi pengguna yang sedang aktif
     * // TODO: di API asli aktor dari token
     */
    suspend fun getStockById(id: String, actor: Session): Resource<StockItem>

    /**
     * Menambahkan item inventaris stok baru.
     *
     * @param item  Data item stok baru
     * @param actor Sesi pengguna yang sedang aktif
     * // TODO: di API asli aktor dari token
     */
    suspend fun addStock(item: StockItem, actor: Session): Resource<StockItem>

    /**
     * Memperbarui informasi data item stok yang sudah ada.
     *
     * @param item  Data item stok yang telah diperbarui
     * @param actor Sesi pengguna yang sedang aktif
     * // TODO: di API asli aktor dari token
     */
    suspend fun updateStock(item: StockItem, actor: Session): Resource<StockItem>

    /**
     * Menghapus item stok dari sistem persediaan.
     *
     * @param id    ID item yang akan dihapus
     * @param actor Sesi pengguna yang sedang aktif
     * // TODO: di API asli aktor dari token
     */
    suspend fun deleteStock(id: String, actor: Session): Resource<Unit>

    /**
     * Menyesuaikan (tambah / kurangi) jumlah stok dengan selisih [delta].
     *
     * @param id         ID item persediaan
     * @param delta      Nilai perubahan (+ untuk penambahan, - untuk pengurangan, tidak boleh 0, |delta| <= 100.000)
     * @param keterangan Catatan atau alasan penyesuaian (opsional)
     * @param actor      Sesi pengguna yang sedang aktif
     * // TODO: di API asli aktor dari token
     */
    suspend fun updateQuantity(id: String, delta: Int, keterangan: String?, actor: Session): Resource<StockItem>
}
