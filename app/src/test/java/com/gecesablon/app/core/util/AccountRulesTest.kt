package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk [AccountRules].
 * Menguji:
 * - Validasi email valid dan tidak valid
 * - Keunikan email (case-insensitive: ADMIN@gece.test vs admin@gece.test)
 * - Validasi panjang nama
 * - Validasi password: minimal 8 karakter, wajib ada huruf dan angka
 * - Larangan penonaktifan akun OWNER
 * - Role akun admin baru selalu ADMIN
 */
class AccountRulesTest {

    @Test
    fun `format email valid diterima`() {
        assertNull(AccountRules.validasiFormatEmail("admin2@gece.test"))
        assertNull(AccountRules.validasiFormatEmail("staff.sablon+01@workshop.co.id"))
    }

    @Test
    fun `format email kosong atau salah format menghasilkan error`() {
        assertEquals("Email wajib diisi.", AccountRules.validasiFormatEmail(""))
        assertEquals("Email wajib diisi.", AccountRules.validasiFormatEmail("   "))
        assertEquals("Format email tidak valid.", AccountRules.validasiFormatEmail("admin@gece"))
        assertEquals("Format email tidak valid.", AccountRules.validasiFormatEmail("admingece.test"))
        assertEquals("Format email tidak valid.", AccountRules.validasiFormatEmail("@gece.test"))
    }

    @Test
    fun `keunikan email case-insensitive mendeteksi duplikat terhadap semua akun`() {
        val existing = listOf("pelanggan@gece.test", "admin@gece.test", "owner@gece.test")

        val resultExact = AccountRules.validasiKeunikanEmail("admin@gece.test", existing)
        assertEquals("Email sudah digunakan oleh akun lain.", resultExact)

        val resultUpperCase = AccountRules.validasiKeunikanEmail("ADMIN@GECE.TEST", existing)
        assertEquals("Email sudah digunakan oleh akun lain.", resultUpperCase)

        val resultMixedCaseOwner = AccountRules.validasiKeunikanEmail("Owner@Gece.Test", existing)
        assertEquals("Email sudah digunakan oleh akun lain.", resultMixedCaseOwner)

        val resultUnique = AccountRules.validasiKeunikanEmail("admin2@gece.test", existing)
        assertNull(resultUnique)
    }

    @Test
    fun `validasi nama minimal 3 karakter dan maksimal 60 karakter`() {
        assertEquals("Nama lengkap wajib diisi.", AccountRules.validasiNama(""))
        assertEquals("Nama minimal 3 karakter.", AccountRules.validasiNama("AB"))
        assertNull(AccountRules.validasiNama("Budi Sablon"))

        val namaTerlaluPanjang = "A".repeat(61)
        assertEquals("Nama maksimal 60 karakter.", AccountRules.validasiNama(namaTerlaluPanjang))
    }

    @Test
    fun `password awal dan atur ulang harus minimal 8 karakter dengan huruf dan angka`() {
        assertEquals("Kata sandi wajib diisi.", AccountRules.validasiPassword(""))
        assertEquals("Kata sandi minimal 8 karakter.", AccountRules.validasiPassword("Abc1"))
        assertEquals("Kata sandi harus mengandung huruf.", AccountRules.validasiPassword("12345678"))
        assertEquals("Kata sandi harus mengandung angka.", AccountRules.validasiPassword("abcdefgh"))
        assertNull(AccountRules.validasiPassword("Admin123"))
        assertNull(AccountRules.validasiPassword("RahasiaWorkshop99"))
    }

    @Test
    fun `akun owner dilarang untuk dinonaktifkan`() {
        assertFalse(AccountRules.bolehDinonaktifkan(Role.OWNER))
        assertTrue(AccountRules.bolehDinonaktifkan(Role.ADMIN))
        assertTrue(AccountRules.bolehDinonaktifkan(Role.CUSTOMER))
    }

    @Test
    fun `role admin baru selalu bernilai ADMIN`() {
        assertEquals(Role.ADMIN, AccountRules.resolveRoleUntukAdminBaru())
    }
}
