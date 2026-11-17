package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductCategory
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
import kotlinx.coroutines.delay

/**
 * FakeProductRepository menyediakan data produk contoh di memori untuk pengujian offline.
 * Meniru latensi jaringan dengan delay ±500 ms.
 *
 * TODO: ganti ke Retrofit di Tahap 8
 */
class FakeProductRepository : ProductRepository {

    private val standardColors = listOf(
        ProductColor(nama = "Hitam Pekat", hex = "#111111"),
        ProductColor(nama = "Putih Bersih", hex = "#FFFFFF"),
        ProductColor(nama = "Navy Blue", hex = "#1B2A4A"),
        ProductColor(nama = "Washed Olive", hex = "#3E4A3E"),
        ProductColor(nama = "Maroon", hex = "#5B1924")
    )

    private val standardSizes = listOf(
        ProductSize(label = "S", tambahanHarga = 0L),
        ProductSize(label = "M", tambahanHarga = 0L),
        ProductSize(label = "L", tambahanHarga = 0L),
        ProductSize(label = "XL", tambahanHarga = 0L),
        ProductSize(label = "XXL", tambahanHarga = 5000L),
        ProductSize(label = "XXXL", tambahanHarga = 10000L)
    )

    private val dummyProducts = listOf(
        Product(
            id = "prod-kaos-custom",
            nama = "Kaos Custom Cotton Combed",
            kategori = ProductCategory.KAOS_CUSTOM,
            bahan = "Cotton Combed 24s/30s",
            deskripsi = "Kaos katun combed premium dengan rajutan halus, adem, dan menyerap keringat. Cocok untuk kaos komunitas, polo merchandise brand, atau seragam event dengan cetak DTF full color presisi dan tahan lama.",
            hargaDasar = 65000L,
            gambarUrl = null,
            daftarWarna = standardColors,
            daftarUkuran = standardSizes
        ),
        Product(
            id = "prod-kaos-raglan",
            nama = "Kaos Raglan Combed",
            kategori = ProductCategory.KAOS_CUSTOM,
            bahan = "Cotton Combed 24s Raglan",
            deskripsi = "Kaos kombinasi warna lengan raglan dengan bahan katun combed lembut dan sablon DTF presisi berdaya rekat tinggi.",
            hargaDasar = 70000L,
            gambarUrl = null,
            daftarWarna = listOf(
                ProductColor(nama = "Putih - Hitam", hex = "#FFFFFF"),
                ProductColor(nama = "Abu - Navy", hex = "#858383"),
                ProductColor(nama = "Hitam - Maroon", hex = "#111111")
            ),
            daftarUkuran = standardSizes
        ),
        Product(
            id = "prod-oversize-streetwear",
            nama = "Kaos Oversize Streetwear",
            kategori = ProductCategory.KAOS_OVERSIZE,
            bahan = "Heavyweight Cotton 20s",
            deskripsi = "Kaos potongan loose-fit vintage streetwear dengan bahan katun tebal kaku berkualitas tinggi dan cetak grafis DTF tajam.",
            hargaDasar = 85000L,
            gambarUrl = null,
            daftarWarna = listOf(
                ProductColor(nama = "Vintage Black", hex = "#1C1B1B"),
                ProductColor(nama = "Broken White", hex = "#F5F5F0"),
                ProductColor(nama = "Washed Olive", hex = "#3E4A3E")
            ),
            daftarUkuran = standardSizes
        ),
        Product(
            id = "prod-oversize-boxy",
            nama = "Kaos Oversize Boxy Cut",
            kategori = ProductCategory.KAOS_OVERSIZE,
            bahan = "Heavyweight Cotton 24s Boxy",
            deskripsi = "Potongan boxy cut modern dengan kerah tebal elastis dan cetak DTF full color presisi di area dada dan punggung.",
            hargaDasar = 80000L,
            gambarUrl = null,
            daftarWarna = standardColors,
            daftarUkuran = standardSizes
        ),
        Product(
            id = "prod-polo-classic",
            nama = "Polo Shirt Lacoste Classic",
            kategori = ProductCategory.BAJU_POLO,
            bahan = "Lacoste CVC Pique Premium",
            deskripsi = "Baju polo berkerah rajut dengan tekstur pori pique mewah, adem dipakai untuk seragam kerja maupun pakaian formal komunitas.",
            hargaDasar = 95000L,
            gambarUrl = null,
            daftarWarna = listOf(
                ProductColor(nama = "Hitam Jet", hex = "#0A0A0A"),
                ProductColor(nama = "Putih Salju", hex = "#FFFFFF"),
                ProductColor(nama = "Deep Navy", hex = "#0E1B33"),
                ProductColor(nama = "Dark Maroon", hex = "#4A0E17")
            ),
            daftarUkuran = standardSizes
        ),
        Product(
            id = "prod-polo-sport",
            nama = "Polo Shirt Kerah Sport",
            kategori = ProductCategory.BAJU_POLO,
            bahan = "Polo CVC Double Knit",
            deskripsi = "Polo shirt kombinasi kerah sporty dengan daya serap keringat tinggi, ideal untuk seragam kantor dan apparel organisasi.",
            hargaDasar = 90000L,
            gambarUrl = null,
            daftarWarna = listOf(
                ProductColor(nama = "Navy - Putih", hex = "#1B2A4A"),
                ProductColor(nama = "Hitam - Merah", hex = "#111111"),
                ProductColor(nama = "Abu - Hitam", hex = "#505050")
            ),
            daftarUkuran = standardSizes
        )
    )

    override suspend fun getProducts(): Resource<List<Product>> {
        delay(500)
        return Resource.Success(dummyProducts)
    }

    override suspend fun getProductById(id: String): Resource<Product> {
        delay(500)
        val product = dummyProducts.find { it.id == id }
        return if (product != null) {
            Resource.Success(product)
        } else {
            Resource.Error("Produk tidak ditemukan")
        }
    }

    override suspend fun searchProducts(
        query: String,
        category: ProductCategory?
    ): Resource<List<Product>> {
        delay(500)
        val trimmedQuery = query.trim()
        val filtered = dummyProducts.filter { prod ->
            val matchCategory = category == null || prod.kategori == category
            val matchQuery = trimmedQuery.isEmpty() ||
                prod.nama.contains(trimmedQuery, ignoreCase = true) ||
                prod.bahan.contains(trimmedQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
        return Resource.Success(filtered)
    }
}
