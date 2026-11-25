package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit test untuk [CategoryRules] memverifikasi:
 * - Panjang nama 2-40 karakter
 * - Spasi pinggir dibuang (cleanNama)
 * - Keunikan nama tanpa membedakan huruf besar-kecil (case-insensitive)
 * - Pengecualian ID sendiri saat edit/ubah nama
 */
class CategoryRulesTest {

    private val sampleCategories = listOf(
        Category(id = "cat-1", nama = "Kaos Custom"),
        Category(id = "cat-2", nama = "Kaos Oversize"),
        Category(id = "cat-3", nama = "Baju Polo")
    )

    @Test
    fun testCleanNamaMembuangSpasiPinggir() {
        assertEquals("Kaos Kustom", CategoryRules.cleanNama("  Kaos Kustom   "))
        assertEquals("Polo", CategoryRules.cleanNama("\tPolo\n"))
    }

    @Test
    fun testValidasiNamaPanjangKarakter() {
        // Kosong dan hanya spasi
        assertNotNull(CategoryRules.validateNama(""))
        assertNotNull(CategoryRules.validateNama("   "))

        // Kurang dari 2 karakter
        assertNotNull(CategoryRules.validateNama("A"))
        assertNotNull(CategoryRules.validateNama(" B "))

        // Pas 2 karakter
        assertNull(CategoryRules.validateNama("AB"))
        assertNull(CategoryRules.validateNama("  CD  "))

        // Pas 40 karakter
        val nama40 = "A".repeat(40)
        assertNull(CategoryRules.validateNama(nama40))

        // Lebih dari 40 karakter (41 karakter)
        val nama41 = "A".repeat(41)
        assertNotNull(CategoryRules.validateNama(nama41))
    }

    @Test
    fun testValidasiNamaUnikCaseInsensitive() {
        // Nama baru yang belum ada -> lolos
        assertNull(CategoryRules.validateNamaUnik("Jaket Hoodie", sampleCategories))

        // Nama duplikat persis
        val errorPersis = CategoryRules.validateNamaUnik("Kaos Custom", sampleCategories)
        assertNotNull(errorPersis)

        // Nama duplikat beda huruf besar-kecil dan dengan spasi pinggir
        val errorCase = CategoryRules.validateNamaUnik("  kaos custom  ", sampleCategories)
        assertNotNull(errorCase)

        val errorCasePolo = CategoryRules.validateNamaUnik("BAJU POLO", sampleCategories)
        assertNotNull(errorCasePolo)
    }

    @Test
    fun testValidasiNamaUnikDenganExcludeId() {
        // Mengubah nama kategori cat-1 tetap menjadi "Kaos Custom" (ID sama) -> harus diizinkan
        val resSame = CategoryRules.validateNamaUnik(
            nama = "Kaos Custom",
            existingCategories = sampleCategories,
            excludeId = "cat-1"
        )
        assertNull(resSame)

        // Mengubah nama kategori cat-1 menjadi nama yang dipakai cat-2 ("Kaos Oversize") -> harus ditolak
        val resDuplicate = CategoryRules.validateNamaUnik(
            nama = "kaos oversize",
            existingCategories = sampleCategories,
            excludeId = "cat-1"
        )
        assertNotNull(resDuplicate)
    }
}
