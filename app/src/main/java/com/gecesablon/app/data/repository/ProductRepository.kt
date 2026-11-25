package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository untuk katalog dan data produk GeCe Screen Printing.
 * Tidak menggunakan tipe Android agar mudah diganti implementasi Retrofit nantinya.
 */
interface ProductRepository {

    // =========================================================================
    // Fungsi Pelanggan, Tamu, dan Flow Pesanan (Hanya Produk Aktif)
    // =========================================================================

    /**
     * Mengalirkan daftar produk aktif secara reaktif.
     */
    fun getActiveProductsStream(): Flow<Resource<List<Product>>>

    /**
     * Mengambil seluruh daftar produk aktif.
     */
    suspend fun getProducts(): Resource<List<Product>>

    /**
     * Mengambil detail produk berdasarkan identifier produk.
     * Hanya mengembalikan produk jika ditemukan dan berstatus aktif.
     */
    suspend fun getProductById(id: String): Resource<Product>

    /**
     * Mencari produk aktif berdasarkan kata kunci nama/bahan dan filter categoryId opsional.
     */
    suspend fun searchProducts(query: String, categoryId: String?): Resource<List<Product>>

    // =========================================================================
    // Fungsi Khusus Owner (Semua Produk termasuk Nonaktif)
    // =========================================================================

    /**
     * Mengalirkan seluruh produk (aktif maupun nonaktif) untuk owner secara reaktif.
     * // TODO: di API asli aktor dari token
     */
    fun semuaProdukFlow(actor: Session): Flow<Resource<List<Product>>>

    /**
     * Mengambil seluruh produk (termasuk produk nonaktif).
     * Hanya dapat diakses oleh peran OWNER.
     * // TODO: di API asli aktor dari token
     */
    suspend fun semuaProduk(actor: Session): Resource<List<Product>>

    /**
     * Mengambil detail produk untuk keperluan owner (termasuk produk nonaktif).
     * Hanya dapat diakses oleh peran OWNER.
     * // TODO: di API asli aktor dari token
     */
    suspend fun getProductByIdForOwner(id: String, actor: Session): Resource<Product>

    /**
     * Menambahkan produk baru. Hanya untuk peran OWNER.
     * Menjaga kestabilan ID produk.
     * // TODO: di API asli aktor dari token
     */
    suspend fun tambah(actor: Session, product: Product): Resource<Product>

    /**
     * Memperbarui informasi produk dan variasinya. Hanya untuk peran OWNER.
     * Menjaga kestabilan ID produk, warna, dan ukuran.
     * // TODO: di API asli aktor dari token
     */
    suspend fun ubah(actor: Session, product: Product): Resource<Product>

    /**
     * Mengubah status aktif/nonaktif produk. Hanya untuk peran OWNER.
     * // TODO: di API asli aktor dari token
     */
    suspend fun setAktif(actor: Session, productId: String, aktif: Boolean): Resource<Product>

    /**
     * Menghapus produk dari sistem. Hanya untuk peran OWNER.
     * // TODO: di API asli aktor dari token
     */
    suspend fun hapus(actor: Session, productId: String): Resource<Unit>
}
