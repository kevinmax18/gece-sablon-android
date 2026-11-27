package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Category
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementasi CategoryRepository untuk GeCe Sablon.
 * Catatan Arsitektur: Backend Laravel tidak menyediakan tabel atau endpoint /api/categories tersendiri,
 * melainkan kategori didefinisikan sebagai string validasi tetap ('kaos_custom', 'kaos_oversize', 'baju_polo').
 * Repository ini menyediakan daftar 3 kategori baku tersebut dan mengecek keaktifan via ProductRepository.
 */
class RemoteCategoryRepository(
    private val productRepository: ProductRepository
) : CategoryRepository {

    private val staticCategories = listOf(
        Category(id = "kaos_custom", nama = "Kaos Custom"),
        Category(id = "kaos_oversize", nama = "Kaos Oversize"),
        Category(id = "baju_polo", nama = "Baju Polo")
    )

    override fun semuaKategoriFlow(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        emit(getCategories())
    }

    override suspend fun getCategories(): Resource<List<Category>> {
        return Resource.Success(staticCategories)
    }

    override suspend fun getCategoryById(id: String): Resource<Category> {
        val cat = staticCategories.find { it.id == id }
        return if (cat != null) {
            Resource.Success(cat)
        } else {
            Resource.Error("Kategori tidak ditemukan.")
        }
    }

    override fun getCategoriesWithActiveProductsFlow(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        emit(getCategoriesWithActiveProducts())
    }

    override suspend fun getCategoriesWithActiveProducts(): Resource<List<Category>> {
        return when (val prodResult = productRepository.getProducts()) {
            is Resource.Success -> {
                val activeCategoryIds = prodResult.data.map { it.categoryId }.toSet()
                val filtered = staticCategories.filter { it.id in activeCategoryIds }
                Resource.Success(filtered.ifEmpty { staticCategories })
            }
            is Resource.Error -> Resource.Error(prodResult.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun tambah(actor: Session, nama: String): Resource<Category> {
        return Resource.Error("Penambahan kategori dinamis belum didukung oleh server backend.")
    }

    override suspend fun ubahNama(actor: Session, id: String, namaBaru: String): Resource<Category> {
        return Resource.Error("Pengubahan nama kategori belum didukung oleh server backend.")
    }

    override suspend fun hapus(actor: Session, id: String): Resource<Unit> {
        return Resource.Error("Penghapusan kategori belum didukung oleh server backend.")
    }
}
