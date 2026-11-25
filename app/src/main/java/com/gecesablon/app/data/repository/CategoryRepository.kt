package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Category
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository untuk data kategori produk GeCe Screen Printing.
 * Tanpa dependensi tipe Android agar mudah diganti implementasi Retrofit nantinya.
 */
interface CategoryRepository {

    // =========================================================================
    // Fungsi Baca (Publik)
    // =========================================================================

    /**
     * Mengalirkan daftar seluruh kategori secara reaktif (StateFlow di memori).
     */
    fun semuaKategoriFlow(): Flow<Resource<List<Category>>>

    /**
     * Mengambil daftar seluruh kategori produk.
     */
    suspend fun getCategories(): Resource<List<Category>>

    /**
     * Mengambil kategori berdasarkan ID.
     */
    suspend fun getCategoryById(id: String): Resource<Category>

    /**
     * Mengalirkan daftar kategori yang memiliki minimal satu produk aktif secara reaktif.
     * Digunakan untuk chip filter di katalog dan beranda pelanggan.
     */
    fun getCategoriesWithActiveProductsFlow(): Flow<Resource<List<Category>>>

    /**
     * Mengambil daftar kategori yang memiliki minimal satu produk aktif.
     */
    suspend fun getCategoriesWithActiveProducts(): Resource<List<Category>>

    // =========================================================================
    // Fungsi Tulis (Khusus OWNER)
    // =========================================================================

    /**
     * Menambahkan kategori baru.
     * // TODO: di API asli aktor dari token
     */
    suspend fun tambah(actor: Session, nama: String): Resource<Category>

    /**
     * Mengubah nama kategori. ID kategori tetap stabil.
     * // TODO: di API asli aktor dari token
     */
    suspend fun ubahNama(actor: Session, id: String, namaBaru: String): Resource<Category>

    /**
     * Menghapus kategori.
     * Ditolak bila kategori masih digunakan oleh produk (baik aktif maupun nonaktif),
     * dengan pesan yang menyebutkan jumlah produk yang memakainya.
     * // TODO: di API asli aktor dari token
     */
    suspend fun hapus(actor: Session, id: String): Resource<Unit>
}
