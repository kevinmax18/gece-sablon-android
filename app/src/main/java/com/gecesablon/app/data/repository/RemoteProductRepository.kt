package com.gecesablon.app.data.repository

import com.gecesablon.app.core.network.safeApiCall
import com.gecesablon.app.core.network.safeApiCallUnit
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.remote.GeceRemoteApi
import com.gecesablon.app.data.remote.dto.ProductColorRequestDto
import com.gecesablon.app.data.remote.dto.ProductMaterialRequestDto
import com.gecesablon.app.data.remote.dto.ProductRequestDto
import com.gecesablon.app.data.remote.dto.ProductSizeRequestDto
import com.gecesablon.app.data.remote.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementasi ProductRepository yang terhubung ke REST API Laravel GeCe Sablon.
 */
class RemoteProductRepository(
    private val api: GeceRemoteApi
) : ProductRepository {

    override fun getActiveProductsStream(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        emit(getProducts())
    }

    override suspend fun getProducts(): Resource<List<Product>> {
        val result = safeApiCall { api.getProducts() }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.map { it.toDomain() })
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun getProductById(id: String): Resource<Product> {
        val result = safeApiCall { api.getProductById(id) }
        return when (result) {
            is Resource.Success -> {
                val domain = result.data.toDomain()
                if (domain.aktif) {
                    Resource.Success(domain)
                } else {
                    Resource.Error("Produk tidak ditemukan atau sedang tidak aktif.")
                }
            }
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun searchProducts(query: String, categoryId: String?): Resource<List<Product>> {
        val searchQuery = query.trim().ifEmpty { null }
        val categoryFilter = categoryId?.trim()?.ifEmpty { null }
        val result = safeApiCall { api.getProducts(search = searchQuery, category = categoryFilter) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.map { it.toDomain() })
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override fun semuaProdukFlow(actor: Session): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        emit(semuaProduk(actor))
    }

    override suspend fun semuaProduk(actor: Session): Resource<List<Product>> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Hanya Owner yang dapat melihat seluruh daftar produk.")
        }
        val result = safeApiCall { api.getOwnerProducts() }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.map { it.toDomain() })
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun getProductByIdForOwner(id: String, actor: Session): Resource<Product> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Hanya Owner yang dapat mengakses detail produk ini.")
        }
        val result = safeApiCall { api.getProductById(id) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun tambah(actor: Session, product: Product): Resource<Product> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Hanya Owner yang dapat menambahkan produk.")
        }
        val req = ProductRequestDto(
            name = product.nama,
            category = product.categoryId,
            material = product.bahan.ifBlank { null },
            description = product.deskripsi.ifBlank { null },
            imagePath = product.gambarUrl,
            basePrice = product.hargaDasar,
            isActive = product.aktif,
            materials = product.daftarBahan.map {
                ProductMaterialRequestDto(
                    name = it.nama,
                    extraPrice = it.tambahanHarga,
                    isDefault = it.isDefault
                )
            },
            colors = product.daftarWarna.map {
                ProductColorRequestDto(
                    name = it.nama,
                    hex = it.hex
                )
            },
            sizes = product.daftarUkuran.map {
                ProductSizeRequestDto(
                    label = it.label,
                    extraPrice = it.tambahanHarga
                )
            }
        )
        val result = safeApiCall { api.createProduct(req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun ubah(actor: Session, product: Product): Resource<Product> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Hanya Owner yang dapat memperbarui produk.")
        }
        val req = ProductRequestDto(
            name = product.nama,
            category = product.categoryId,
            material = product.bahan.ifBlank { null },
            description = product.deskripsi.ifBlank { null },
            imagePath = product.gambarUrl,
            basePrice = product.hargaDasar,
            isActive = product.aktif,
            materials = product.daftarBahan.map {
                ProductMaterialRequestDto(
                    name = it.nama,
                    extraPrice = it.tambahanHarga,
                    isDefault = it.isDefault
                )
            },
            colors = product.daftarWarna.map {
                ProductColorRequestDto(
                    name = it.nama,
                    hex = it.hex
                )
            },
            sizes = product.daftarUkuran.map {
                ProductSizeRequestDto(
                    label = it.label,
                    extraPrice = it.tambahanHarga
                )
            }
        )
        val result = safeApiCall { api.updateProduct(product.id, req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun setAktif(actor: Session, productId: String, aktif: Boolean): Resource<Product> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Hanya Owner yang dapat mengubah status keaktifan produk.")
        }
        val current = getProductByIdForOwner(productId, actor)
        if (current !is Resource.Success) {
            return Resource.Error((current as? Resource.Error)?.message ?: "Produk tidak ditemukan.")
        }
        val p = current.data
        return ubah(actor, p.copy(aktif = aktif))
    }

    override suspend fun hapus(actor: Session, productId: String): Resource<Unit> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Hanya Owner yang dapat menghapus produk.")
        }
        return safeApiCallUnit { api.deleteProduct(productId) }
    }
}
