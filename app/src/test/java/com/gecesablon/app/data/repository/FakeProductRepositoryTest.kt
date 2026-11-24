package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductCategory
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
 * Unit test untuk [FakeProductRepository] dan interaksinya dengan [FakeOrderRepository] (Tahap 6B-1):
 * - Memastikan fungsi baca pelanggan/tamu hanya mengembalikan produk aktif
 * - Memastikan fungsi owner (semuaProduk, tambah, ubah, setAktif, hapus) dilindungi otorisasi OWNER
 * - Memastikan perubahan owner mengalir reaktif via StateFlow
 * - Memastikan pembuatan pesanan (pelanggan & manual staff) menolak produk nonaktif atau terhapus
 */
class FakeProductRepositoryTest {

    private lateinit var productRepository: FakeProductRepository
    private lateinit var orderRepository: FakeOrderRepository

    private val ownerSession = Session(
        token = "token-owner",
        userId = "owner-1",
        name = "Owner GeCe",
        role = Role.OWNER
    )

    private val adminSession = Session(
        token = "token-admin",
        userId = "admin-1",
        name = "Admin Workshop",
        role = Role.ADMIN
    )

    private val customerSession = Session(
        token = "token-customer",
        userId = "cust-1",
        name = "Pelanggan Demo",
        role = Role.CUSTOMER
    )

    private val sampleColors = listOf(
        ProductColor(nama = "Hitam", hex = "#111111"),
        ProductColor(nama = "Putih", hex = "#FFFFFF")
    )

    private val sampleSizes = listOf(
        ProductSize(label = "S", tambahanHarga = 0L),
        ProductSize(label = "XL", tambahanHarga = 0L)
    )

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        orderRepository = FakeOrderRepository(productRepository = productRepository)
    }

    @Test
    fun testGetProductsHanyaMengembalikanProdukAktif() = runBlocking {
        // Awalnya seluruh 6 produk bawaan aktif
        val initialRes = productRepository.getProducts()
        assertTrue(initialRes is Resource.Success)
        val initialList = (initialRes as Resource.Success).data
        assertEquals(6, initialList.size)
        assertTrue(initialList.all { it.aktif })

        // Owner menonaktifkan produk pertama
        val targetId = initialList[0].id
        val setRes = productRepository.setAktif(ownerSession, targetId, false)
        assertTrue(setRes is Resource.Success)

        // getProducts sekarang hanya mengembalikan 5 produk
        val updatedRes = productRepository.getProducts()
        assertTrue(updatedRes is Resource.Success)
        val updatedList = (updatedRes as Resource.Success).data
        assertEquals(5, updatedList.size)
        assertFalse(updatedList.any { it.id == targetId })
    }

    @Test
    fun testGetProductByIdMenolakProdukNonaktif() = runBlocking {
        val initialRes = productRepository.getProducts() as Resource.Success
        val targetId = initialRes.data[0].id

        // Sebelum dinonaktifkan: berhasil
        val beforeRes = productRepository.getProductById(targetId)
        assertTrue(beforeRes is Resource.Success)

        // Nonaktifkan produk
        productRepository.setAktif(ownerSession, targetId, false)

        // Setelah dinonaktifkan: getProductById untuk pelanggan/tamu mengembalikan Resource.Error
        val afterRes = productRepository.getProductById(targetId)
        assertTrue("Produk nonaktif harus mengembalikan Resource.Error", afterRes is Resource.Error)

        // Namun getProductByIdForOwner tetap bisa mengambil produk tersebut
        val ownerRes = productRepository.getProductByIdForOwner(targetId, ownerSession)
        assertTrue(ownerRes is Resource.Success)
        assertFalse((ownerRes as Resource.Success).data.aktif)
    }

    @Test
    fun testSemuaProdukOwnerMengembalikanProdukAktifDanNonaktif() = runBlocking {
        val allInitial = productRepository.semuaProduk(ownerSession) as Resource.Success
        val targetId = allInitial.data[0].id

        // Nonaktifkan 1 produk
        productRepository.setAktif(ownerSession, targetId, false)

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
            kategori = ProductCategory.KAOS_CUSTOM,
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
            kategori = ProductCategory.KAOS_OVERSIZE,
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

        // Ubah produk: update harga dasar dan nama
        val updatedProduct = added.copy(
            nama = "Kaos Vintage Special Edition Updated",
            hargaDasar = 110000L
        )

        val ubahRes = productRepository.ubah(ownerSession, updatedProduct)
        assertTrue(ubahRes is Resource.Success)
        val modified = (ubahRes as Resource.Success).data
        assertEquals("prod-kustom-001", modified.id) // ID stabil
        assertEquals(110000L, modified.hargaDasar)
        assertEquals("Kaos Vintage Special Edition Updated", modified.nama)
    }

    @Test
    fun testHapusProdukOwner() = runBlocking {
        val initialList = (productRepository.getProducts() as Resource.Success).data
        val target = initialList[0]

        val hapusRes = productRepository.hapus(ownerSession, target.id)
        assertTrue(hapusRes is Resource.Success)

        // Tidak lagi ada di semuaProduk maupun getProducts
        val allAfter = (productRepository.semuaProduk(ownerSession) as Resource.Success).data
        assertFalse(allAfter.any { it.id == target.id })
    }

    @Test
    fun testOrderRepositoryMenolakPesananUntukProdukNonaktif() = runBlocking {
        val products = (productRepository.getProducts() as Resource.Success).data
        val targetProduct = products[0]

        // Owner menonaktifkan produk
        productRepository.setAktif(ownerSession, targetProduct.id, false)

        // 1. Pelanggan membuat pesanan lewat createOrder
        val createRes = orderRepository.createOrder(
            userId = "cust-1",
            product = targetProduct,
            color = targetProduct.daftarWarna.first(),
            size = targetProduct.daftarUkuran.first(),
            quantity = 10,
            catatan = "Sablon depan",
            uriDesain = null
        )
        assertTrue("Pesanan pelanggan untuk produk nonaktif harus ditolak", createRes is Resource.Error)

        // 2. Staff membuat pesanan manual lewat createManualOrder
        val manualRes = orderRepository.createManualOrder(
            actor = adminSession,
            namaPelanggan = "Walk-in Guest",
            noHpPelanggan = "081234567890",
            productId = targetProduct.id,
            colorName = targetProduct.daftarWarna.first().nama,
            sizeLabel = targetProduct.daftarUkuran.first().label,
            quantity = 5,
            catatan = "Pesanan walk-in",
            uriDesain = null
        )
        assertTrue("Pesanan manual staf untuk produk nonaktif harus ditolak", manualRes is Resource.Error)
    }

    @Test
    fun testOrderRepositoryMenolakPesananUntukProdukTerhapus() = runBlocking {
        val products = (productRepository.getProducts() as Resource.Success).data
        val targetProduct = products[0]

        // Owner menghapus produk
        productRepository.hapus(ownerSession, targetProduct.id)

        // Pelanggan membuat pesanan
        val createRes = orderRepository.createOrder(
            userId = "cust-1",
            product = targetProduct,
            color = targetProduct.daftarWarna.first(),
            size = targetProduct.daftarUkuran.first(),
            quantity = 10,
            catatan = "Sablon depan",
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
            kategori = ProductCategory.KAOS_CUSTOM,
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
