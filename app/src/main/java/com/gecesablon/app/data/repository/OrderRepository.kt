package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.ProductionState
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository untuk mengelola pesanan (Order).
 * Didesain tanpa ketergantungan tipe Android agar mudah diganti implementasi Retrofit nantinya.
 *
 * TODO: ganti ke Retrofit di Tahap 8
 */
interface OrderRepository {

    // =========================================================================
    // Fungsi Pelanggan
    // =========================================================================

    /**
     * Mengalirkan daftar pesanan milik pengguna tertentu secara reaktif.
     *
     * @param userId ID pengguna pemilik pesanan
     */
    fun getOrdersByUserId(userId: String): Flow<Resource<List<Order>>>

    /**
     * Mengambil detail pesanan berdasarkan ID pesanan.
     *
     * @param orderId ID pesanan
     */
    suspend fun getOrderById(orderId: String): Resource<Order>

    /**
     * Membuat pesanan baru pelanggan.
     *
     * @param userId         ID pembuat pesanan (TODO: di API asli diambil dari token)
     * @param product        Produk yang dipesan
     * @param color          Varian warna terpilih
     * @param size           Varian ukuran terpilih
     * @param quantity       Jumlah pesanan dalam pcs
     * @param catatan        Catatan tambahan/posisi cetak dari pelanggan
     * @param uriDesain      URI file desain pratinjau yang diunggah
     * @param namaPelanggan  Nama pelanggan pemesan
     * @param noHpPelanggan  Nomor kontak WhatsApp/HP pelanggan (opsional)
     */
    suspend fun createOrder(
        userId: String,
        product: Product,
        color: ProductColor,
        size: ProductSize,
        quantity: Int,
        catatan: String,
        uriDesain: String?,
        namaPelanggan: String = "Pelanggan GeCe",
        noHpPelanggan: String? = null
    ): Resource<Order>

    /**
     * Membatalkan pesanan. Hanya diizinkan jika status pesanan adalah DITERIMA.
     *
     * @param orderId ID pesanan yang akan dibatalkan
     * @param userId  ID pengguna pemohon pembatalan (TODO: di API asli diambil dari token)
     */
    suspend fun cancelOrder(orderId: String, userId: String): Resource<Unit>

    // =========================================================================
    // Fungsi Staf Workshop (Admin & Owner)
    // =========================================================================

    /**
     * Mengalirkan seluruh pesanan workshop secara reaktif.
     *
     * @param actor Sesi pengguna yang sedang aktif (hanya ADMIN atau OWNER)
     * // TODO: di API asli aktor dari token
     */
    fun getAllOrders(actor: Session): Flow<Resource<List<Order>>>

    /**
     * Mengambil detail pesanan untuk tampilan staf.
     *
     * @param orderId ID pesanan
     * @param actor   Sesi pengguna yang sedang aktif (hanya ADMIN atau OWNER)
     * // TODO: di API asli aktor dari token
     */
    suspend fun getStaffOrderDetail(orderId: String, actor: Session): Resource<Order>

    /**
     * Menerima pesanan berstatus DITERIMA dan memulai proses produksi internal (menjadi SEDANG_DIPROSES).
     *
     * @param orderId ID pesanan
     * @param actor   Sesi staf yang menerima
     * // TODO: di API asli aktor dari token
     */
    suspend fun acceptAndStartProcess(orderId: String, actor: Session): Resource<Order>

    /**
     * Memperbarui kondisi tahapan produksi internal pesanan.
     * Menghitung penurunan status pelanggan secara otomatis dan memvalidasi aturan produksi.
     * Menolak pesanan yang masih berstatus DITERIMA, DIBATALKAN, atau SELESAI.
     *
     * @param orderId  ID pesanan
     * @param newState Kondisi produksi baru
     * @param actor    Sesi staf yang melakukan pembaruan
     * // TODO: di API asli aktor dari token
     */
    suspend fun updateProductionState(orderId: String, newState: ProductionState, actor: Session): Resource<Order>

    /**
     * Menandai pesanan telah diambil dan selesai secara tuntas (menjadi SELESAI).
     * Hanya diizinkan jika status pesanan saat ini adalah SIAP_DIAMBIL.
     *
     * @param orderId ID pesanan
     * @param actor   Sesi staf yang menyerahkan pesanan
     * // TODO: di API asli aktor dari token
     */
    suspend fun markOrderCompleted(orderId: String, actor: Session): Resource<Order>

    /**
     * Membuat pesanan manual baru oleh staf (Admin/Owner) untuk pelanggan tamu / walk-in.
     *
     * @param actor         Sesi staf pembuat pesanan
     * @param namaPelanggan Nama lengkap pelanggan
     * @param noHpPelanggan Nomor WhatsApp pelanggan (format 08... / 628...)
     * @param productId     ID produk tekstil yang dipesan
     * @param colorName     Nama warna yang dipilih
     * @param sizeLabel     Label ukuran yang dipilih
     * @param quantity      Jumlah pesanan (1..999 pcs)
     * @param catatan       Instruksi atau catatan khusus sablon
     * @param uriDesain     URI file artwork atau mockup terunggah (opsional)
     * // TODO: di API asli aktor dari token
     */
    suspend fun createManualOrder(
        actor: Session,
        namaPelanggan: String,
        noHpPelanggan: String,
        productId: String,
        colorName: String,
        sizeLabel: String,
        quantity: Int,
        catatan: String,
        uriDesain: String?
    ): Resource<Order>
}
