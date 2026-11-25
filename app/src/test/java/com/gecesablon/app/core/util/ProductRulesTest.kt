package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductMaterial
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.repository.FakeCategoryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk [ProductRules] memverifikasi seluruh aturan bisnis produk owner:
 * - Nama 3-60 karakter
 * - Kategori (categoryId) dan bahan wajib
 * - Deskripsi maksimal 500 karakter
 * - Harga dasar 1.000 - 10.000.000
 * - Minimal 1 bahan, tepat 1 bahan default, nama 2-80 karakter unik tanpa membedakan huruf besar-kecil, tambahan harga 0-10.000.000
 * - Minimal 1 warna (nama + kode #RRGGBB valid) tanpa duplikat
 * - Minimal 1 ukuran (S-XXXL) dengan tambahan harga >= 0 tanpa duplikat
 * - Pengembalian error per kolom
 */
class ProductRulesTest {

    private val sampleValidMaterials = listOf(
        ProductMaterial(id = "mat-1", nama = "Cotton Combed 30s", tambahanHarga = 0L, isDefault = true),
        ProductMaterial(id = "mat-2", nama = "Cotton Combed 24s", tambahanHarga = 5000L, isDefault = false)
    )

    private val sampleValidColors = listOf(
        ProductColor(nama = "Hitam Pekat", hex = "#111111"),
        ProductColor(nama = "Putih Bersih", hex = "#FFFFFF")
    )

    private val sampleValidSizes = listOf(
        ProductSize(label = "S", tambahanHarga = 0L),
        ProductSize(label = "M", tambahanHarga = 0L),
        ProductSize(label = "XXL", tambahanHarga = 5000L)
    )

    private val sampleValidProduct = Product(
        id = "prod-test-01",
        nama = "Kaos Polos Combed 24s",
        categoryId = FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM,
        bahan = "Cotton Combed 24s",
        deskripsi = "Kaos polos berkualitas untuk sablon DTF.",
        hargaDasar = 65000L,
        daftarBahan = sampleValidMaterials,
        daftarWarna = sampleValidColors,
        daftarUkuran = sampleValidSizes,
        aktif = true
    )

    @Test
    fun testValidProductLolosValidasi() {
        val result = ProductRules.validateProduct(sampleValidProduct)
        assertTrue("Produk valid harus lolos validasi", result.isValid)
        assertNull(result.namaError)
        assertNull(result.kategoriError)
        assertNull(result.bahanError)
        assertNull(result.daftarBahanError)
        assertNull(result.deskripsiError)
        assertNull(result.hargaDasarError)
        assertNull(result.warnaError)
        assertNull(result.ukuranError)
        assertNull(result.generalError)
    }

    @Test
    fun testValidasiNamaProduk() {
        // Kurang dari 3 karakter
        assertNotNull(ProductRules.validateNama(""))
        assertNotNull(ProductRules.validateNama("  "))
        assertNotNull(ProductRules.validateNama("AB"))

        // Pas 3 karakter dan pas 60 karakter
        assertNull(ProductRules.validateNama("ABC"))
        val nama60 = "A".repeat(60)
        assertNull(ProductRules.validateNama(nama60))

        // Lebih dari 60 karakter
        val nama61 = "A".repeat(61)
        assertNotNull(ProductRules.validateNama(nama61))
    }

    @Test
    fun testValidasiKategoriDanBahan() {
        assertNotNull(ProductRules.validateCategoryId(null))
        assertNotNull(ProductRules.validateCategoryId(""))
        assertNotNull(ProductRules.validateCategoryId("   "))
        assertNull(ProductRules.validateCategoryId(FakeCategoryRepository.SEED_CAT_KAOS_CUSTOM))
        assertNull(ProductRules.validateCategoryId(FakeCategoryRepository.SEED_CAT_KAOS_OVERSIZE))
        assertNull(ProductRules.validateCategoryId(FakeCategoryRepository.SEED_CAT_BAJU_POLO))

        assertNotNull(ProductRules.validateBahan(""))
        assertNotNull(ProductRules.validateBahan("   "))
        assertNull(ProductRules.validateBahan("Cotton Combed 30s"))
    }

    @Test
    fun testValidasiDaftarBahan_minimalSatuDanTepatSatuDefault() {
        // Kosong
        assertNotNull(ProductRules.validateDaftarBahan(emptyList()))

        // Tanpa bahan default
        val noDefault = listOf(
            ProductMaterial(id = "1", nama = "Combed 30s", tambahanHarga = 0L, isDefault = false)
        )
        assertNotNull(ProductRules.validateDaftarBahan(noDefault))

        // Lebih dari 1 default
        val twoDefaults = listOf(
            ProductMaterial(id = "1", nama = "Combed 30s", tambahanHarga = 0L, isDefault = true),
            ProductMaterial(id = "2", nama = "Combed 24s", tambahanHarga = 5000L, isDefault = true)
        )
        assertNotNull(ProductRules.validateDaftarBahan(twoDefaults))

        // Valid: tepat 1 default
        val validOneDefault = listOf(
            ProductMaterial(id = "1", nama = "Combed 30s", tambahanHarga = 0L, isDefault = true)
        )
        assertNull(ProductRules.validateDaftarBahan(validOneDefault))
    }

    @Test
    fun testValidasiDaftarBahan_namaDanPanjang() {
        // Nama kosong
        val emptyName = listOf(
            ProductMaterial(id = "1", nama = "   ", tambahanHarga = 0L, isDefault = true)
        )
        assertNotNull(ProductRules.validateDaftarBahan(emptyName))

        // Nama kurang dari 2 karakter
        val shortName = listOf(
            ProductMaterial(id = "1", nama = "C", tambahanHarga = 0L, isDefault = true)
        )
        assertNotNull(ProductRules.validateDaftarBahan(shortName))

        // Nama pas 2 karakter dan pas 80 karakter
        val name2 = listOf(
            ProductMaterial(id = "1", nama = "TC", tambahanHarga = 0L, isDefault = true)
        )
        assertNull(ProductRules.validateDaftarBahan(name2))

        val name80 = listOf(
            ProductMaterial(id = "1", nama = "A".repeat(80), tambahanHarga = 0L, isDefault = true)
        )
        assertNull(ProductRules.validateDaftarBahan(name80))

        // Nama lebih dari 80 karakter
        val name81 = listOf(
            ProductMaterial(id = "1", nama = "A".repeat(81), tambahanHarga = 0L, isDefault = true)
        )
        assertNotNull(ProductRules.validateDaftarBahan(name81))
    }

    @Test
    fun testValidasiDaftarBahan_keunikanNamaCaseInsensitive() {
        val duplicateNames = listOf(
            ProductMaterial(id = "1", nama = "Cotton Combed 24s", tambahanHarga = 0L, isDefault = true),
            ProductMaterial(id = "2", nama = "cotton combed 24s", tambahanHarga = 3000L, isDefault = false)
        )
        assertNotNull(ProductRules.validateDaftarBahan(duplicateNames))
    }

    @Test
    fun testValidasiDaftarBahan_tambahanHargaRentang() {
        // Negatif
        val negativeExtra = listOf(
            ProductMaterial(id = "1", nama = "Combed 30s", tambahanHarga = -1000L, isDefault = true)
        )
        assertNotNull(ProductRules.validateDaftarBahan(negativeExtra))

        // Melebihi 10.000.000
        val overMaxExtra = listOf(
            ProductMaterial(id = "1", nama = "Combed 30s", tambahanHarga = 10_000_001L, isDefault = true)
        )
        assertNotNull(ProductRules.validateDaftarBahan(overMaxExtra))

        // Pas 0 dan pas 10.000.000
        val validBoundary = listOf(
            ProductMaterial(id = "1", nama = "Combed 30s", tambahanHarga = 0L, isDefault = true),
            ProductMaterial(id = "2", nama = "Combed Premium", tambahanHarga = 10_000_000L, isDefault = false)
        )
        assertNull(ProductRules.validateDaftarBahan(validBoundary))
    }

    @Test
    fun testValidasiDeskripsi() {
        // Boleh kosong
        assertNull(ProductRules.validateDeskripsi(""))
        // Maksimal 500 karakter
        val deskripsi500 = "B".repeat(500)
        assertNull(ProductRules.validateDeskripsi(deskripsi500))

        // 501 karakter ditolak
        val deskripsi501 = "B".repeat(501)
        assertNotNull(ProductRules.validateDeskripsi(deskripsi501))
    }

    @Test
    fun testValidasiHargaDasar() {
        // Di bawah 1.000
        assertNotNull(ProductRules.validateHargaDasar(0L))
        assertNotNull(ProductRules.validateHargaDasar(999L))

        // Pas 1.000 dan pas 10.000.000
        assertNull(ProductRules.validateHargaDasar(1_000L))
        assertNull(ProductRules.validateHargaDasar(65_000L))
        assertNull(ProductRules.validateHargaDasar(10_000_000L))

        // Di atas 10.000.000
        assertNotNull(ProductRules.validateHargaDasar(10_000_001L))
    }

    @Test
    fun testValidasiWarnaMinimalSatuDanTanpaDuplikat() {
        // Kosong
        assertNotNull(ProductRules.validateWarna(emptyList()))

        // Format hex salah
        val invalidHex = listOf(
            ProductColor(nama = "Merah", hex = "112233") // Tanpa '#'
        )
        assertNotNull(ProductRules.validateWarna(invalidHex))

        val invalidHexLength = listOf(
            ProductColor(nama = "Merah", hex = "#1122") // Kurang dari 6 digit
        )
        assertNotNull(ProductRules.validateWarna(invalidHexLength))

        // Nama warna kosong
        val emptyName = listOf(
            ProductColor(nama = "  ", hex = "#112233")
        )
        assertNotNull(ProductRules.validateWarna(emptyName))

        // Duplikat nama warna
        val duplicateName = listOf(
            ProductColor(nama = "Hitam", hex = "#000000"),
            ProductColor(nama = "hitam", hex = "#111111")
        )
        assertNotNull(ProductRules.validateWarna(duplicateName))

        // Duplikat hex warna
        val duplicateHex = listOf(
            ProductColor(nama = "Hitam Solid", hex = "#000000"),
            ProductColor(nama = "Hitam Pekat", hex = "#000000")
        )
        assertNotNull(ProductRules.validateWarna(duplicateHex))
    }

    @Test
    fun testValidasiUkuranRentangDanTanpaDuplikat() {
        // Kosong
        assertNotNull(ProductRules.validateUkuran(emptyList()))

        // Ukuran di luar S-XXXL
        val invalidSize = listOf(
            ProductSize(label = "XS", tambahanHarga = 0L)
        )
        assertNotNull(ProductRules.validateUkuran(invalidSize))

        val invalidSize4XL = listOf(
            ProductSize(label = "XXXXL", tambahanHarga = 0L)
        )
        assertNotNull(ProductRules.validateUkuran(invalidSize4XL))

        // Tambahan harga negatif
        val negativePrice = listOf(
            ProductSize(label = "M", tambahanHarga = -5000L)
        )
        assertNotNull(ProductRules.validateUkuran(negativePrice))

        // Duplikat ukuran
        val duplicateSize = listOf(
            ProductSize(label = "L", tambahanHarga = 0L),
            ProductSize(label = "l", tambahanHarga = 5000L)
        )
        assertNotNull(ProductRules.validateUkuran(duplicateSize))
    }

    @Test
    fun testNormalisasiHexInput() {
        assertEquals("#112233", ProductRules.normalizeHexInput("112233"))
        assertEquals("#AABBCC", ProductRules.normalizeHexInput("#aabbcc"))
        assertEquals("#1F2937", ProductRules.normalizeHexInput("  #1f2937  "))
        assertEquals("#FFFFFF", ProductRules.normalizeHexInput("ffffffextra"))
    }
}
