package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductCategory

/**
 * Kontrak repository untuk katalog dan data produk GeCe Screen Printing.
 */
interface ProductRepository {
    /**
     * Mengambil seluruh daftar produk sablon.
     */
    suspend fun getProducts(): Resource<List<Product>>

    /**
     * Mengambil detail produk berdasarkan identifier produk.
     */
    suspend fun getProductById(id: String): Resource<Product>

    /**
     * Mencari produk berdasarkan kata kunci nama produk dan filter kategori opsional.
     */
    suspend fun searchProducts(query: String, category: ProductCategory?): Resource<List<Product>>
}
