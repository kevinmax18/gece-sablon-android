package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository untuk mengelola pesanan (Order).
 * Didesain tanpa ketergantungan tipe Android agar mudah diganti implementasi Retrofit nantinya.
 *
 * TODO: ganti ke Retrofit di Tahap 8
 */
interface OrderRepository {

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
     * @param userId ID pembuat pesanan (TODO: di API asli diambil dari token)
     * @param product Produk yang dipesan
     * @param color Varian warna terpilih
     * @param size Varian ukuran terpilih
     * @param quantity Jumlah pesanan dalam pcs
     * @param catatan Catatan tambahan/posisi cetak dari pelanggan
     * @param uriDesain URI file desain pratinjau yang diunggah
     */
    suspend fun createOrder(
        userId: String,
        product: Product,
        color: ProductColor,
        size: ProductSize,
        quantity: Int,
        catatan: String,
        uriDesain: String?
    ): Resource<Order>

    /**
     * Membatalkan pesanan. Hanya diizinkan jika status pesanan adalah DITERIMA.
     *
     * @param orderId ID pesanan yang akan dibatalkan
     * @param userId ID pengguna pemohon pembatalan (TODO: di API asli diambil dari token)
     */
    suspend fun cancelOrder(orderId: String, userId: String): Resource<Unit>
}
