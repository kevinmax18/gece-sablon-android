package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.ProductRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductMaterial
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementasi in-memory repository untuk katalog dan pengelolaan produk GeCe.
 * Menggunakan MutableStateFlow sebagai Single Source of Truth di memori.
 * Menjamin kestabilan ID produk, warna, dan ukuran saat penambahan maupun perubahan.
 *
 * Seed kategori memetakan ke:
 * - Kaos Custom -> FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM ("cat-kaos-custom")
 * - Kaos Oversize -> FakeCategoryRepository.SEED_CAT_KAOS_OVERSIZE ("cat-kaos-oversize")
 * - Baju Polo -> FakeCategoryRepository.SEED_CAT_BAJU_POLO ("cat-baju-polo")
 */
class FakeProductRepository : ProductRepository {

    private val standardColors = listOf(
        ProductColor(nama = "Hitam Solid", hex = "#111111"),
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

    private val initialDummyProducts = listOf(
        Product(
            id = "prod-kaos-custom",
            nama = "Kaos Custom Cotton Combed",
            categoryId = FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM,
            bahan = "Cotton Combed 24s/30s",
            deskripsi = "Kaos katun combed premium dengan rajutan halus, adem, dan menyerap keringat. Cocok untuk kaos komunitas, polo merchandise brand, atau seragam event dengan cetak DTF full color presisi dan tahan lama.",
            hargaDasar = 65000L,
            gambarUrl = null,
            daftarBahan = listOf(
                ProductMaterial(id = "mat-kaos-combed-30s", nama = "Cotton Combed 30s", tambahanHarga = 0L, isDefault = true),
                ProductMaterial(id = "mat-kaos-combed-24s", nama = "Cotton Combed 24s", tambahanHarga = 5000L, isDefault = false)
            ),
            daftarWarna = standardColors,
            daftarUkuran = standardSizes,
            aktif = true
        ),
        Product(
            id = "prod-kaos-raglan",
            nama = "Kaos Raglan Combed",
            categoryId = FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM,
            bahan = "Cotton Combed 24s Raglan",
            deskripsi = "Kaos kombinasi warna lengan raglan dengan bahan katun combed lembut dan sablon DTF presisi berdaya rekat tinggi.",
            hargaDasar = 70000L,
            gambarUrl = null,
            daftarBahan = listOf(
                ProductMaterial(id = "mat-raglan-combed-24s", nama = "Cotton Combed 24s", tambahanHarga = 0L, isDefault = true),
                ProductMaterial(id = "mat-raglan-combed-20s", nama = "Cotton Combed 20s", tambahanHarga = 5000L, isDefault = false)
            ),
            daftarWarna = listOf(
                ProductColor(nama = "Putih - Hitam", hex = "#FFFFFF"),
                ProductColor(nama = "Abu - Navy", hex = "#858383"),
                ProductColor(nama = "Hitam - Maroon", hex = "#111111")
            ),
            daftarUkuran = standardSizes,
            aktif = true
        ),
        Product(
            id = "prod-oversize-streetwear",
            nama = "Kaos Oversize Streetwear",
            categoryId = FakeCategoryRepository.SEED_CAT_KAOS_OVERSIZE,
            bahan = "Heavyweight Cotton 20s",
            deskripsi = "Kaos potongan loose-fit vintage streetwear dengan bahan katun tebal kaku berkualitas tinggi dan cetak grafis DTF tajam.",
            hargaDasar = 85000L,
            gambarUrl = null,
            daftarBahan = listOf(
                ProductMaterial(id = "mat-oversize-heavy-20s", nama = "Heavyweight Cotton 20s", tambahanHarga = 0L, isDefault = true),
                ProductMaterial(id = "mat-oversize-heavy-16s", nama = "Heavyweight Cotton 16s", tambahanHarga = 10000L, isDefault = false)
            ),
            daftarWarna = listOf(
                ProductColor(nama = "Vintage Black", hex = "#1C1B1B"),
                ProductColor(nama = "Broken White", hex = "#F5F5F0"),
                ProductColor(nama = "Washed Olive", hex = "#3E4A3E")
            ),
            daftarUkuran = standardSizes,
            aktif = true
        ),
        Product(
            id = "prod-oversize-boxy",
            nama = "Kaos Oversize Boxy Cut",
            categoryId = FakeCategoryRepository.SEED_CAT_KAOS_OVERSIZE,
            bahan = "Heavyweight Cotton 24s Boxy",
            deskripsi = "Potongan boxy cut modern dengan kerah tebal elastis dan cetak DTF full color presisi di area dada dan punggung.",
            hargaDasar = 80000L,
            gambarUrl = null,
            daftarBahan = listOf(
                ProductMaterial(id = "mat-boxy-cotton-24s", nama = "Heavyweight Cotton 24s", tambahanHarga = 0L, isDefault = true),
                ProductMaterial(id = "mat-boxy-cotton-20s", nama = "Heavyweight Cotton 20s", tambahanHarga = 5000L, isDefault = false)
            ),
            daftarWarna = standardColors,
            daftarUkuran = standardSizes,
            aktif = true
        ),
        Product(
            id = "prod-polo-classic",
            nama = "Polo Shirt Lacoste Classic",
            categoryId = FakeCategoryRepository.SEED_CAT_BAJU_POLO,
            bahan = "Lacoste CVC Pique Premium",
            deskripsi = "Baju polo berkerah rajut dengan tekstur pori pique mewah, adem dipakai untuk seragam kerja maupun pakaian formal komunitas.",
            hargaDasar = 95000L,
            gambarUrl = null,
            daftarBahan = listOf(
                ProductMaterial(id = "mat-polo-lacoste-cvc", nama = "Lacoste CVC Pique", tambahanHarga = 0L, isDefault = true),
                ProductMaterial(id = "mat-polo-lacoste-cotton", nama = "Lacoste 100% Cotton", tambahanHarga = 10000L, isDefault = false)
            ),
            daftarWarna = listOf(
                ProductColor(nama = "Hitam Jet", hex = "#0A0A0A"),
                ProductColor(nama = "Putih Salju", hex = "#FFFFFF"),
                ProductColor(nama = "Deep Navy", hex = "#0E1B33"),
                ProductColor(nama = "Dark Maroon", hex = "#4A0E17")
            ),
            daftarUkuran = standardSizes,
            aktif = true
        ),
        Product(
            id = "prod-polo-sport",
            nama = "Polo Shirt Kerah Sport",
            categoryId = FakeCategoryRepository.SEED_CAT_BAJU_POLO,
            bahan = "Polo CVC Double Knit",
            deskripsi = "Polo shirt kombinasi kerah sporty dengan daya serap keringat tinggi, ideal untuk seragam kantor dan apparel organisasi.",
            hargaDasar = 90000L,
            gambarUrl = null,
            daftarBahan = listOf(
                ProductMaterial(id = "mat-polo-cvc-double", nama = "Polo CVC Double Knit", tambahanHarga = 0L, isDefault = true)
            ),
            daftarWarna = listOf(
                ProductColor(nama = "Navy - Putih", hex = "#1B2A4A"),
                ProductColor(nama = "Hitam - Merah", hex = "#111111"),
                ProductColor(nama = "Abu - Hitam", hex = "#505050")
            ),
            daftarUkuran = standardSizes,
            aktif = true
        )
    )

    private val _productsFlow = MutableStateFlow<List<Product>>(initialDummyProducts)

    // =========================================================================
    // Fungsi Pelanggan, Tamu, dan Flow Pesanan (Hanya Produk Aktif)
    // =========================================================================

    override fun getActiveProductsStream(): Flow<Resource<List<Product>>> {
        return _productsFlow.asStateFlow().map { allList ->
            val activeOnly = allList.filter { it.aktif }
            Resource.Success(activeOnly)
        }
    }

    override suspend fun getProducts(): Resource<List<Product>> {
        delay(300)
        val activeProducts = _productsFlow.value.filter { it.aktif }
        return Resource.Success(activeProducts)
    }

    override suspend fun getProductById(id: String): Resource<Product> {
        delay(300)
        val product = _productsFlow.value.find { it.id == id }
        return if (product != null && product.aktif) {
            Resource.Success(product)
        } else {
            Resource.Error("Produk tidak ditemukan atau sedang tidak aktif.")
        }
    }

    override suspend fun searchProducts(
        query: String,
        categoryId: String?
    ): Resource<List<Product>> {
        delay(300)
        val trimmedQuery = query.trim()
        val filtered = _productsFlow.value.filter { prod ->
            val isAktif = prod.aktif
            val matchCategory = categoryId.isNullOrBlank() || prod.categoryId == categoryId
            val matchQuery = trimmedQuery.isEmpty() ||
                prod.nama.contains(trimmedQuery, ignoreCase = true) ||
                prod.bahan.contains(trimmedQuery, ignoreCase = true)
            isAktif && matchCategory && matchQuery
        }
        return Resource.Success(filtered)
    }

    // =========================================================================
    // Fungsi Khusus Owner (Semua Produk termasuk Nonaktif)
    // =========================================================================

    override fun semuaProdukFlow(actor: Session): Flow<Resource<List<Product>>> {
        return _productsFlow.asStateFlow().map { list ->
            // TODO: di API asli aktor dari token
            if (actor.role != Role.OWNER) {
                Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
            } else {
                Resource.Success(list)
            }
        }
    }

    override suspend fun semuaProduk(actor: Session): Resource<List<Product>> {
        delay(300)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
        }
        return Resource.Success(_productsFlow.value)
    }

    override suspend fun getProductByIdForOwner(id: String, actor: Session): Resource<Product> {
        delay(300)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
        }
        val product = _productsFlow.value.find { it.id == id }
        return if (product != null) {
            Resource.Success(product)
        } else {
            Resource.Error("Produk tidak ditemukan.")
        }
    }

    override suspend fun tambah(actor: Session, product: Product): Resource<Product> {
        delay(400)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
        }

        val validation = ProductRules.validateProduct(product)
        if (!validation.isValid) {
            return Resource.Error(validation.generalError ?: "Validasi data produk gagal.")
        }

        val generatedId = if (product.id.isBlank()) "prod-${UUID.randomUUID().toString().take(8)}" else product.id
        val newProduct = product.copy(id = generatedId)

        val currentList = _productsFlow.value.toMutableList()
        currentList.add(0, newProduct)
        _productsFlow.value = currentList

        return Resource.Success(newProduct)
    }

    override suspend fun ubah(actor: Session, product: Product): Resource<Product> {
        delay(400)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
        }

        val validation = ProductRules.validateProduct(product)
        if (!validation.isValid) {
            return Resource.Error(validation.generalError ?: "Validasi data produk gagal.")
        }

        val currentList = _productsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == product.id }
        if (index == -1) {
            return Resource.Error("Produk tidak ditemukan.")
        }

        // Kestabilan ID produk, warna, dan ukuran dipertahankan
        currentList[index] = product
        _productsFlow.value = currentList

        return Resource.Success(product)
    }

    override suspend fun setAktif(actor: Session, productId: String, aktif: Boolean): Resource<Product> {
        delay(300)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
        }

        val currentList = _productsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == productId }
        if (index == -1) {
            return Resource.Error("Produk tidak ditemukan.")
        }

        val updated = currentList[index].copy(aktif = aktif)
        currentList[index] = updated
        _productsFlow.value = currentList

        return Resource.Success(updated)
    }

    override suspend fun hapus(actor: Session, productId: String): Resource<Unit> {
        delay(400)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk Owner.")
        }

        val currentList = _productsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == productId }
        if (index == -1) {
            return Resource.Error("Produk tidak ditemukan.")
        }

        currentList.removeAt(index)
        _productsFlow.value = currentList

        return Resource.Success(Unit)
    }
}
