package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.CategoryRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Category
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementasi in-memory repository kategori produk GeCe.
 * Menggunakan MutableStateFlow sebagai sumber kebenaran data di memori.
 * Memeriksa ketergantungan produk melalui ProductRepository sebelum menghapus kategori.
 */
class FakeCategoryRepository(
    private val productRepository: ProductRepository
) : CategoryRepository {

    // Seed kategori awal sesuai spesifikasi
    companion object {
        const val SEED_CAT_KAOS_CUSTOM = "cat-kaos-custom"
        const val SEED_CAT_KAOS_OVERSIZE = "cat-kaos-oversize"
        const val SEED_CAT_BAJU_POLO = "cat-baju-polo"

        val initialCategories = listOf(
            Category(id = SEED_CAT_KAOS_CUSTOM, nama = "Kaos Custom"),
            Category(id = SEED_CAT_KAOS_OVERSIZE, nama = "Kaos Oversize"),
            Category(id = SEED_CAT_BAJU_POLO, nama = "Baju Polo")
        )
    }

    private val _categoriesFlow = MutableStateFlow<List<Category>>(initialCategories)

    // =========================================================================
    // Fungsi Baca (Publik)
    // =========================================================================

    override fun semuaKategoriFlow(): Flow<Resource<List<Category>>> {
        return _categoriesFlow.asStateFlow().map { list ->
            Resource.Success(list)
        }
    }

    override suspend fun getCategories(): Resource<List<Category>> {
        delay(200)
        return Resource.Success(_categoriesFlow.value)
    }

    override suspend fun getCategoryById(id: String): Resource<Category> {
        delay(150)
        val cat = _categoriesFlow.value.find { it.id == id }
        return if (cat != null) {
            Resource.Success(cat)
        } else {
            Resource.Error("Kategori tidak ditemukan.")
        }
    }

    override fun getCategoriesWithActiveProductsFlow(): Flow<Resource<List<Category>>> {
        return combine(_categoriesFlow, productRepository.getActiveProductsStream()) { categories, productsResource ->
            when (productsResource) {
                is Resource.Success -> {
                    val activeProductCategoryIds = productsResource.data.map { it.categoryId }.toSet()
                    val filtered = categories.filter { it.id in activeProductCategoryIds }
                    Resource.Success(filtered)
                }
                is Resource.Error -> {
                    Resource.Error(productsResource.message)
                }
                is Resource.Loading -> {
                    Resource.Loading
                }
            }
        }
    }

    override suspend fun getCategoriesWithActiveProducts(): Resource<List<Category>> {
        delay(200)
        return when (val productsRes = productRepository.getProducts()) {
            is Resource.Success -> {
                val activeCategoryIds = productsRes.data.map { it.categoryId }.toSet()
                val filtered = _categoriesFlow.value.filter { it.id in activeCategoryIds }
                Resource.Success(filtered)
            }
            is Resource.Error -> Resource.Error(productsRes.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    // =========================================================================
    // Fungsi Tulis (Khusus OWNER)
    // =========================================================================

    override suspend fun tambah(actor: Session, nama: String): Resource<Category> {
        delay(300)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
        }

        val validationError = CategoryRules.validateNamaUnik(nama, _categoriesFlow.value)
        if (validationError != null) {
            return Resource.Error(validationError)
        }

        val cleaned = CategoryRules.cleanNama(nama)
        val newCategory = Category(
            id = "cat-${UUID.randomUUID().toString().take(8)}",
            nama = cleaned
        )

        val updated = _categoriesFlow.value.toMutableList()
        updated.add(newCategory)
        _categoriesFlow.value = updated

        return Resource.Success(newCategory)
    }

    override suspend fun ubahNama(actor: Session, id: String, namaBaru: String): Resource<Category> {
        delay(300)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
        }

        val currentList = _categoriesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index == -1) {
            return Resource.Error("Kategori tidak ditemukan.")
        }

        val validationError = CategoryRules.validateNamaUnik(namaBaru, currentList, excludeId = id)
        if (validationError != null) {
            return Resource.Error(validationError)
        }

        val cleaned = CategoryRules.cleanNama(namaBaru)
        val updatedCategory = currentList[index].copy(nama = cleaned)
        currentList[index] = updatedCategory
        _categoriesFlow.value = currentList

        return Resource.Success(updatedCategory)
    }

    override suspend fun hapus(actor: Session, id: String): Resource<Unit> {
        delay(350)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
        }

        val currentList = _categoriesFlow.value.toMutableList()
        val category = currentList.find { it.id == id }
            ?: return Resource.Error("Kategori tidak ditemukan.")

        // Periksa apakah kategori masih digunakan oleh produk (aktif maupun nonaktif)
        val allProductsRes = productRepository.semuaProduk(actor)
        if (allProductsRes is Resource.Success) {
            val usageCount = allProductsRes.data.count { it.categoryId == id }
            if (usageCount > 0) {
                return Resource.Error("Kategori '${category.nama}' tidak dapat dihapus karena masih digunakan oleh $usageCount produk.")
            }
        } else if (allProductsRes is Resource.Error) {
            return Resource.Error(allProductsRes.message)
        }

        currentList.removeAll { it.id == id }
        _categoriesFlow.value = currentList

        return Resource.Success(Unit)
    }
}
