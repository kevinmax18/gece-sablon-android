package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test untuk [FakeProductRepository] memverifikasi:
 * - Produk seed awal lengkap (6 produk)
 * - Pemisahan produk aktif vs nonaktif
 * - Otorisasi role: fungsi owner menolak non-owner
 * - Kestabilan ID saat tambah dan ubah produk
 * - Aliran reaktif getActiveProductsStream
 * - Penolakan pembuatan pesanan untuk produk nonaktif dan terhapus
 */
class FakeProductRepositoryTest {

    private lateinit var productRepository: FakeProductRepository
    private lateinit var orderRepository: FakeOrderRepository

    private val ownerSession = Session(
        token = "token-owner",
        userId = "owner-01",
        name = "Owner GeCe",
        role = Role.OWNER
    )

    private val adminSession = Session(
        token = "token-admin",
        userId = "admin-01",
        name = "Admin Staff",
        role = Role.ADMIN
    )

    private val customerSession = Session(
        token = "token-customer",
        userId = "cust-01",
        name = "Pelanggan",
        role = Role.CUSTOMER
    )

    private val sampleColors = listOf(
        ProductColor(nama = "Hitam", hex = "#111111"),
        ProductColor(nama = "Putih", hex = "#FFFFFF")
    )

    private val sampleSizes = listOf(
        ProductSize(label = "M", tambahanHarga = 0L),
        ProductSize(label = "XL", tambahanHarga = 0L)
    )

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        orderRepository = FakeOrderRepository(productRepository = productRepository)
    }

    @Test
    fun testSeedProdukAwalLengkapDanAktif() = runBlocking {
        val result = productRepository.getProducts()
        assertTrue(result is Resource.Success)
        val products = (result as Resource.Success).data
        assertEquals("Seed produk awal harus berjumlah 6", 6, products.size)
        assertTrue("Semua produk seed awal harus berstatus aktif", products.all { it.aktif })
        assertTrue("Semua produk seed awal harus memiliki categoryId valid", products.all { it.categoryId.isNotBlank() })
    }

    @Test
    fun testGetProductByIdMengembalikanHanyaYangAktif() = runBlocking {
        // Produk aktif
        val res = productRepository.getProductById("prod-kaos-custom")
        assertTrue(res is Resource.Success)
        assertEquals("Kaos Custom Cotton Combed", (res as Resource.Success).data.nama)

        // Nonaktifkan produk lewat owner
        productRepository.setAktif(ownerSession, "prod-kaos-custom", false)

        // getProductById untuk pelanggan sekarang harus gagal
        val resAfter = productRepository.getProductById("prod-kaos-custom")
        assertTrue("Produk nonaktif tidak boleh ditemukan lewat getProductById pelanggan", resAfter is Resource.Error)

        // Namun owner tetap bisa mengambilnya lewat getProductByIdForOwner
        val resOwner = productRepository.getProductByIdForOwner("prod-kaos-custom", ownerSession)
        assertTrue(resOwner is Resource.Success)
        assertFalse((resOwner as Resource.Success).data.aktif)
    }

    @Test
    fun testSearchProductsDenganQueryDanCategory() = runBlocking {
        // Cari berdasarkan kata kunci "Oversize"
        val resQuery = productRepository.searchProducts("Oversize", null)
        assertTrue(resQuery is Resource.Success)
        val dataQuery = (resQuery as Resource.Success).data
        assertEquals(2, dataQuery.size)

        // Filter berdasarkan kategori BAJU_POLO
        val resCategory = productRepository.searchProducts("", FakeCategoryRepository.SEED_CAT_BAJU_POLO)
        assertTrue(resCategory is Resource.Success)
        val dataCategory = (resCategory as Resource.Success).data
        assertEquals(2, dataCategory.size)
        assertTrue(dataCategory.all { it.categoryId == FakeCategoryRepository.SEED_CAT_BAJU_POLO })

        // Gabungan kata kunci dan kategori
        val resBoth = productRepository.searchProducts("Classic", FakeCategoryRepository.SEED_CAT_BAJU_POLO)
        assertTrue(resBoth is Resource.Success)
        val dataBoth = (resBoth as Resource.Success).data
        assertEquals(1, dataBoth.size)
        assertEquals("prod-polo-classic", dataBoth.first().id)
    }

    @Test
    fun testSetAktifMengubahKetersediaanDiKatalogPelanggan() = runBlocking {
        val targetId = "prod-polo-sport"

        // Pastikan awal aktif di katalog (6 produk)
        val initialActive = productRepository.getProducts() as Resource.Success
        assertEquals(6, initialActive.data.size)

        // Nonaktifkan oleh Owner
        val setRes = productRepository.setAktif(ownerSession, targetId, false)
        assertTrue(setRes is Resource.Success)
        assertFalse((setRes as Resource.Success).data.aktif)

        // Katalog pelanggan sekarang hanya 5 produk
        val afterActive = productRepository.getProducts() as Resource.Success
        assertEquals(5, afterActive.data.size)
        assertTrue(afterActive.data.none { it.id == targetId })

        // semuaProduk untuk owner tetap berisi 6 produk
        val allAfter = productRepository.semuaProduk(ownerSession) as Resource.Success
        assertEquals(6, allAfter.data.size)
        assertTrue(allAfter.data.any { it.id == targetId && !it.aktif })
    }

    @Test
    fun testOtorisasiRoleFungsiOwner() = runBlocking {
        // Admin mencoba memanggil fungsi owner -> harus ditolak
        val resAdminSemua = productRepository.semuaProduk(adminSession)
        assertTrue("Admin harus ditolak memanggil semuaProduk", resAdminSemua is Resource.Error)

        val resCustSemua = productRepository.semuaProduk(customerSession)
        assertTrue("Customer harus ditolak memanggil semuaProduk", resCustSemua is Resource.Error)

        val dummyProd = Product(
            id = "test-id",
            nama = "Kaos Baru",
            categoryId = FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM,
            bahan = "Combed 30s",
            deskripsi = "Deskripsi",
            hargaDasar = 60000L,
            daftarWarna = sampleColors,
            daftarUkuran = sampleSizes
        )

        // Admin mencoba tambah
        val tambahAdmin = productRepository.tambah(adminSession, dummyProd)
        assertTrue(tambahAdmin is Resource.Error)

        // Admin mencoba ubah
        val ubahAdmin = productRepository.ubah(adminSession, dummyProd)
        assertTrue(ubahAdmin is Resource.Error)

        // Admin mencoba setAktif
        val setAdmin = productRepository.setAktif(adminSession, "prod-kaos-custom", false)
        assertTrue(setAdmin is Resource.Error)

        // Admin mencoba hapus
        val hapusAdmin = productRepository.hapus(adminSession, "prod-kaos-custom")
        assertTrue(hapusAdmin is Resource.Error)
    }

    @Test
    fun testTambahDanUbahProdukOwnerMenjagaKestabilanId() = runBlocking {
        val newProduct = Product(
            id = "prod-kustom-001",
            nama = "Kaos Vintage Special Edition",
            categoryId = FakeCategoryRepository.SEED_CAT_KAOS_OVERSIZE,
            bahan = "Heavyweight 16s",
            deskripsi = "Kaos edisi khusus sablon DTF.",
            hargaDasar = 99000L,
            daftarWarna = sampleColors,
            daftarUkuran = sampleSizes,
            aktif = true
        )

        val tambahRes = productRepository.tambah(ownerSession, newProduct)
        assertTrue(tambahRes is Resource.Success)
        val added = (tambahRes as Resource.Success).data
        assertEquals("prod-kustom-001", added.id)

        // Ubah produk tanpa mengubah id
        val updatedProduct = added.copy(
            nama = "Kaos Vintage Special Edition V2",
            hargaDasar = 105000L
        )
        val ubahRes = productRepository.ubah(ownerSession, updatedProduct)
        assertTrue(ubahRes is Resource.Success)
        val saved = (ubahRes as Resource.Success).data
        assertEquals("prod-kustom-001", saved.id)
        assertEquals("Kaos Vintage Special Edition V2", saved.nama)
        assertEquals(105000L, saved.hargaDasar)
    }

    @Test
    fun testHapusProdukOwner() = runBlocking {
        val targetId = "prod-kaos-raglan"
        val hapusRes = productRepository.hapus(ownerSession, targetId)
        assertTrue(hapusRes is Resource.Success)

        // Pastikan tidak ada lagi di katalog
        val activeRes = productRepository.getProducts() as Resource.Success
        assertTrue(activeRes.data.none { it.id == targetId })

        // Pastikan tidak ada di daftar owner
        val allRes = productRepository.semuaProduk(ownerSession) as Resource.Success
        assertTrue(allRes.data.none { it.id == targetId })
    }

    @Test
    fun testPesananMenolakProdukNonaktif() = runBlocking {
        val targetId = "prod-kaos-custom"
        val product = (productRepository.getProductById(targetId) as Resource.Success).data

        // Nonaktifkan produk
        productRepository.setAktif(ownerSession, targetId, false)

        // Coba buat pesanan untuk produk nonaktif tersebut
        val createRes = orderRepository.createOrder(
            userId = "cust-01",
            namaPelanggan = "Pelanggan Tes",
            noHpPelanggan = "081234567890",
            product = product,
            color = product.daftarWarna.first(),
            size = product.daftarUkuran.first(),
            quantity = 10,
            catatan = "Uji tolak nonaktif",
            uriDesain = null
        )
        assertTrue("Pesanan untuk produk nonaktif harus ditolak", createRes is Resource.Error)
    }

    @Test
    fun testPesananMenolakProdukYangDihapus() = runBlocking {
        val targetId = "prod-oversize-boxy"
        val product = (productRepository.getProductById(targetId) as Resource.Success).data

        // Hapus produk
        productRepository.hapus(ownerSession, targetId)

        // Coba buat pesanan untuk produk yang sudah dihapus
        val createRes = orderRepository.createOrder(
            userId = "cust-01",
            namaPelanggan = "Pelanggan Tes",
            noHpPelanggan = "081234567890",
            product = product,
            color = product.daftarWarna.first(),
            size = product.daftarUkuran.first(),
            quantity = 5,
            catatan = "Uji tolak hapus",
            uriDesain = null
        )
        assertTrue("Pesanan untuk produk yang sudah dihapus harus ditolak", createRes is Resource.Error)
    }

    @Test
    fun testAliranReaktifStateFlow() = runBlocking {
        // Ambil emisi pertama dari getActiveProductsStream
        val streamInitial = productRepository.getActiveProductsStream().first()
        assertTrue(streamInitial is Resource.Success)
        assertEquals(6, (streamInitial as Resource.Success).data.size)

        // Tambah produk baru
        val newProduct = Product(
            id = "prod-new-stream",
            nama = "Kaos Stream Test",
            categoryId = FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM,
            bahan = "Cotton Combed 30s",
            deskripsi = "Deskripsi stream test",
            hargaDasar = 75000L,
            daftarWarna = sampleColors,
            daftarUkuran = sampleSizes,
            aktif = true
        )
        productRepository.tambah(ownerSession, newProduct)

        // Emisi berikutnya langsung mencerminkan produk baru (7 produk)
        val streamAfterAdd = productRepository.getActiveProductsStream().first()
        assertTrue(streamAfterAdd is Resource.Success)
        assertEquals(7, (streamAfterAdd as Resource.Success).data.size)
    }
}
