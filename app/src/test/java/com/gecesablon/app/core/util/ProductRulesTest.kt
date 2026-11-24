package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductCategory
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk [ProductRules] memverifikasi seluruh aturan bisnis produk owner:
 * - Nama 3-60 karakter
 * - Kategori dan bahan wajib
 * - Deskripsi maksimal 500 karakter
 * - Harga dasar 1.000 - 10.000.000
 * - Minimal 1 warna (nama + kode #RRGGBB valid) tanpa duplikat
 * - Minimal 1 ukuran (S-XXXL) dengan tambahan harga >= 0 tanpa duplikat
 * - Pengembalian error per kolom
 */
class ProductRulesTest {

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
        kategori = ProductCategory.KAOS_CUSTOM,
        bahan = "Cotton Combed 24s",
        deskripsi = "Kaos polos berkualitas untuk sablon DTF.",
        hargaDasar = 65000L,
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
        assertNotNull(ProductRules.validateKategori(null))
        assertNull(ProductRules.validateKategori(ProductCategory.KAOS_CUSTOM))
        assertNull(ProductRules.validateKategori(ProductCategory.KAOS_OVERSIZE))
        assertNull(ProductRules.validateKategori(ProductCategory.BAJU_POLO))

        assertNotNull(ProductRules.validateBahan(""))
        assertNotNull(ProductRules.validateBahan("   "))
        assertNull(ProductRules.validateBahan("Cotton Combed 30s"))
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
        // Di bawah 1.000 ditolak
        assertNotNull(ProductRules.validateHargaDasar(0L))
        assertNotNull(ProductRules.validateHargaDasar(999L))
        assertNotNull(ProductRules.validateHargaDasar(-5000L))

        // Batas bawah 1.000 diterima
        assertNull(ProductRules.validateHargaDasar(1000L))
        // Nominal normal diterima
        assertNull(ProductRules.validateHargaDasar(85000L))
        // Batas atas 10.000.000 diterima
        assertNull(ProductRules.validateHargaDasar(10000000L))

        // Di atas 10.000.000 ditolak
        assertNotNull(ProductRules.validateHargaDasar(10000001L))
    }

    @Test
    fun testValidasiHexColor() {
        assertTrue(ProductRules.isValidHexColor("#111111"))
        assertTrue(ProductRules.isValidHexColor("#FFFFFF"))
        assertTrue(ProductRules.isValidHexColor("#ffffff"))
        assertTrue(ProductRules.isValidHexColor("#AbCdEf"))

        assertFalse(ProductRules.isValidHexColor(""))
        assertFalse(ProductRules.isValidHexColor("111111")) // Tanpa '#'
        assertFalse(ProductRules.isValidHexColor("#FFF")) // Shorthand 3 digit ditolak
        assertFalse(ProductRules.isValidHexColor("#GGGGGG")) // Karakter non-hex
        assertFalse(ProductRules.isValidHexColor("#1234567")) // 7 digit
        assertFalse(ProductRules.isValidHexColor("merah"))
    }

    @Test
    fun testNormalisasiHexInput() {
        // Tanpa #
        assertEquals("#111111", ProductRules.normalizeHexInput("111111"))
        // Huruf kecil
        assertEquals("#ABCDEF", ProductRules.normalizeHexInput("abcdef"))
        // Karakter asing / non-hex dibuang
        assertEquals("#1A2B3C", ProductRules.normalizeHexInput("#1A2B3C!@_"))
        // Spasi diapit
        assertEquals("#FFFFFF", ProductRules.normalizeHexInput(" #ffffff "))
        // Kurang dari 6 digit
        assertEquals("#FFF", ProductRules.normalizeHexInput("fff"))
        assertEquals("#1A", ProductRules.normalizeHexInput("#1a"))
        // Lebih dari 6 digit: dipotong maksimal 6 karakter pertama
        assertEquals("#123456", ProductRules.normalizeHexInput("1234567890"))
        // Kosong
        assertEquals("#", ProductRules.normalizeHexInput(""))
        assertEquals("#", ProductRules.normalizeHexInput("   "))
        assertEquals("#", ProductRules.normalizeHexInput("#"))
        // Sudah berformat #RRGGBB
        assertEquals("#1F2937", ProductRules.normalizeHexInput("#1f2937"))
    }

    @Test
    fun testValidasiWarnaMinimalSatuDanFormat() {
        // Kosong ditolak
        assertNotNull(ProductRules.validateWarna(emptyList()))

        // Nama kosong ditolak
        val warnaNamaKosong = listOf(ProductColor(nama = "", hex = "#111111"))
        assertNotNull(ProductRules.validateWarna(warnaNamaKosong))

        // Hex tidak valid ditolak
        val warnaHexSalah = listOf(ProductColor(nama = "Hitam", hex = "bukanHex"))
        assertNotNull(ProductRules.validateWarna(warnaHexSalah))

        // Valid
        assertNull(ProductRules.validateWarna(sampleValidColors))
    }

    @Test
    fun testValidasiWarnaDuplikatDitolak() {
        // Duplikat nama warna (case-insensitive)
        val duplikatNama = listOf(
            ProductColor(nama = "Hitam", hex = "#111111"),
            ProductColor(nama = "hitam", hex = "#222222")
        )
        assertNotNull(ProductRules.validateWarna(duplikatNama))

        // Duplikat kode hex (case-insensitive)
        val duplikatHex = listOf(
            ProductColor(nama = "Hitam 1", hex = "#111111"),
            ProductColor(nama = "Hitam 2", hex = "#111111")
        )
        assertNotNull(ProductRules.validateWarna(duplikatHex))
    }

    @Test
    fun testValidasiUkuranRentangDanTambahanHarga() {
        // Kosong ditolak
        assertNotNull(ProductRules.validateUkuran(emptyList()))

        // Di luar S-XXXL ditolak
        val ukuranInvalid = listOf(ProductSize(label = "XS", tambahanHarga = 0L))
        assertNotNull(ProductRules.validateUkuran(ukuranInvalid))

        val ukuranAllSize = listOf(ProductSize(label = "All Size", tambahanHarga = 0L))
        assertNotNull(ProductRules.validateUkuran(ukuranAllSize))

        // Tambahan harga negatif ditolak
        val hargaNegatif = listOf(ProductSize(label = "L", tambahanHarga = -5000L))
        assertNotNull(ProductRules.validateUkuran(hargaNegatif))

        // Tambahan harga >= 0 diterima
        val hargaNol = listOf(ProductSize(label = "M", tambahanHarga = 0L))
        assertNull(ProductRules.validateUkuran(hargaNol))
        val hargaPositif = listOf(ProductSize(label = "XXL", tambahanHarga = 10000L))
        assertNull(ProductRules.validateUkuran(hargaPositif))
    }

    @Test
    fun testValidasiUkuranDuplikatDitolak() {
        val ukuranDuplikat = listOf(
            ProductSize(label = "M", tambahanHarga = 0L),
            ProductSize(label = "M", tambahanHarga = 5000L)
        )
        assertNotNull(ProductRules.validateUkuran(ukuranDuplikat))
    }

    @Test
    fun testErrorPerKolomSecaraTerisolasi() {
        // Buat produk dengan beberapa kesalahan berbeda
        val result = ProductRules.validateProductFields(
            nama = "X", // Error nama
            kategori = null, // Error kategori
            bahan = "", // Error bahan
            deskripsi = "A".repeat(505), // Error deskripsi
            hargaDasar = 500L, // Error harga
            daftarWarna = emptyList(), // Error warna
            daftarUkuran = emptyList() // Error ukuran
        )

        assertFalse(result.isValid)
        assertNotNull(result.namaError)
        assertNotNull(result.kategoriError)
        assertNotNull(result.bahanError)
        assertNotNull(result.deskripsiError)
        assertNotNull(result.hargaDasarError)
        assertNotNull(result.warnaError)
        assertNotNull(result.ukuranError)
        assertNotNull(result.generalError)
    }
}
