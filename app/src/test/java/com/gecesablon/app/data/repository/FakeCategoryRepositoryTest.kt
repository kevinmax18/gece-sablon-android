package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Category
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test untuk [FakeCategoryRepository] memverifikasi:
 * - Kategori seed awal lengkap (Kaos Custom, Kaos Oversize, Baju Polo)
 * - Baca publik semua kategori dan kategori dengan produk aktif
 * - Otorisasi role: hanya OWNER yang boleh menambah, mengubah nama, dan menghapus
 * - Ubah nama menjaga kestabilan ID
 * - Hapus kategori yang masih dipakai produk (aktif maupun nonaktif) DITOLAK dengan pesan yang menyebut jumlah produk yang memakainya
 * - Hapus kategori yang tidak dipakai produk BERHASIL
 */
class FakeCategoryRepositoryTest {

    private lateinit var productRepository: FakeProductRepository
    private lateinit var categoryRepository: FakeCategoryRepository

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

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        categoryRepository = FakeCategoryRepository(productRepository = productRepository)
    }

    @Test
    fun testSeedAwalKategoriLengkap() = runBlocking {
        val result = categoryRepository.getCategories()
        assertTrue(result is Resource.Success)
        val list = (result as Resource.Success).data
        assertEquals(3, list.size)
        assertTrue(list.any { it.id == FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM && it.nama == "Kaos Custom" })
        assertTrue(list.any { it.id == FakeCategoryRepository.SEED_CAT_KAOS_OVERSIZE && it.nama == "Kaos Oversize" })
        assertTrue(list.any { it.id == FakeCategoryRepository.SEED_CAT_BAJU_POLO && it.nama == "Baju Polo" })
    }

    @Test
    fun testKategoriDenganProdukAktif() = runBlocking {
        val activeCatsRes = categoryRepository.getCategoriesWithActiveProducts()
        assertTrue(activeCatsRes is Resource.Success)
        val activeCats = (activeCatsRes as Resource.Success).data
        // Semua 3 seed memiliki produk aktif di FakeProductRepository
        assertEquals(3, activeCats.size)
    }

    @Test
    fun testOtorisasiRoleHanyaOwner() = runBlocking {
        // Admin mencoba tambah
        val resAdminTambah = categoryRepository.tambah(adminSession, "Kategori Baru")
        assertTrue(resAdminTambah is Resource.Error)

        // Customer mencoba ubah
        val resCustUbah = categoryRepository.ubahNama(customerSession, FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM, "Nama Lain")
        assertTrue(resCustUbah is Resource.Error)

        // Admin mencoba hapus
        val resAdminHapus = categoryRepository.hapus(adminSession, FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM)
        assertTrue(resAdminHapus is Resource.Error)
    }

    @Test
    fun testTambahDanUbahNamaMenjagaKestabilanId() = runBlocking {
        // Owner menambah kategori
        val tambahRes = categoryRepository.tambah(ownerSession, "Jaket Hoodie")
        assertTrue(tambahRes is Resource.Success)
        val newCat = (tambahRes as Resource.Success).data
        assertEquals("Jaket Hoodie", newCat.nama)
        assertNotNull(newCat.id)

        // Owner mengubah nama kategori tersebut
        val ubahRes = categoryRepository.ubahNama(ownerSession, newCat.id, "Hoodie Zipper")
        assertTrue(ubahRes is Resource.Success)
        val updatedCat = (ubahRes as Resource.Success).data
        // ID tetap sama
        assertEquals(newCat.id, updatedCat.id)
        assertEquals("Hoodie Zipper", updatedCat.nama)
    }

    @Test
    fun testHapusKategoriYangMasihDipakaiProdukDitolakDenganJumlahProduk() = runBlocking {
        // Kaos Custom dipakai oleh 2 produk seed (prod-kaos-custom dan prod-kaos-raglan)
        val hapusRes = categoryRepository.hapus(ownerSession, FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM)
        assertTrue("Hapus kategori yang masih dipakai harus ditolak", hapusRes is Resource.Error)
        val errMsg = (hapusRes as Resource.Error).message
        assertTrue("Pesan error harus menyebut nama kategori dan jumlah produk (2)", errMsg.contains("2 produk"))
    }

    @Test
    fun testHapusKategoriYangTidakDipakaiProdukBerhasil() = runBlocking {
        // Tambah kategori baru tanpa produk
        val tambahRes = categoryRepository.tambah(ownerSession, "Topi & Aksesoris")
        val newCat = (tambahRes as Resource.Success).data

        // Hapus kategori tersebut
        val hapusRes = categoryRepository.hapus(ownerSession, newCat.id)
        assertTrue("Hapus kategori tanpa produk harus berhasil", hapusRes is Resource.Success)

        // Pastikan sudah tidak ada di repository
        val afterList = (categoryRepository.getCategories() as Resource.Success).data
        assertTrue(afterList.none { it.id == newCat.id })
    }
}
