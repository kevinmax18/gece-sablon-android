package com.gecesablon.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pengujian unit murni untuk [PhoneRules].
 * Menguji:
 * - Normalisasi: pembuangan spasi, strip, tanda kurung
 * - Penggantian awalan +62 dan 62 menjadi 0
 * - Validasi: kosong/blank, hanya angka, awalan 0, panjang 10-14 digit
 * - Validasi keunikan lintas format (+62 vs 08)
 */
class PhoneRulesTest {

    @Test
    fun `normalisasi membuang spasi strip kurung dan mengubah awalan +62 atau 62 menjadi 0`() {
        assertEquals("081234567890", PhoneRules.normalize("081234567890"))
        assertEquals("081234567890", PhoneRules.normalize("0812-3456-7890"))
        assertEquals("081234567890", PhoneRules.normalize("(0812) 3456 7890"))
        assertEquals("081234567890", PhoneRules.normalize("+62 812-3456-7890"))
        assertEquals("081234567890", PhoneRules.normalize("+6281234567890"))
        assertEquals("081234567890", PhoneRules.normalize("6281234567890"))
        assertEquals("081234567890", PhoneRules.normalize("62-812-3456-7890"))
        assertEquals("08123456789", PhoneRules.normalize("(0812) 345-6789"))
    }

    @Test
    fun `validasi nomor HP kosong atau blank mengembalikan error wajib diisi`() {
        assertEquals("Nomor HP wajib diisi.", PhoneRules.validate(""))
        assertEquals("Nomor HP wajib diisi.", PhoneRules.validate("    "))
    }

    @Test
    fun `validasi nomor HP mengandung karakter non-angka setelah normalisasi mengembalikan error`() {
        assertEquals("Nomor HP hanya boleh berisi angka.", PhoneRules.validate("081234567abc"))
        assertEquals("Nomor HP hanya boleh berisi angka.", PhoneRules.validate("0812#3456*789"))
    }

    @Test
    fun `validasi nomor HP tidak diawali 0 mengembalikan error`() {
        assertEquals("Nomor HP harus diawali dengan angka 0.", PhoneRules.validate("18123456789"))
        assertEquals("Nomor HP harus diawali dengan angka 0.", PhoneRules.validate("981234567890"))
    }

    @Test
    fun `validasi nomor HP kurang dari 10 digit mengembalikan error terlalu pendek`() {
        assertEquals("Nomor HP terlalu pendek (minimal 10 digit).", PhoneRules.validate("081234567"))
        assertEquals("Nomor HP terlalu pendek (minimal 10 digit).", PhoneRules.validate("+62 812-345"))
    }

    @Test
    fun `validasi nomor HP lebih dari 14 digit mengembalikan error terlalu panjang`() {
        assertEquals("Nomor HP terlalu panjang (maksimal 14 digit).", PhoneRules.validate("081234567890123"))
    }

    @Test
    fun `validasi nomor HP yang valid mengembalikan null`() {
        assertNull(PhoneRules.validate("081234567890")) // 12 digit
        assertNull(PhoneRules.validate("0812345678")) // 10 digit
        assertNull(PhoneRules.validate("08123456789012")) // 14 digit
        assertNull(PhoneRules.validate("+62 812-3456-7890"))
        assertNull(PhoneRules.validate("6281234567890"))
        assertNull(PhoneRules.validate("(0812) 3456 789"))
    }

    @Test
    fun `validasi keunikan mendeteksi nomor HP yang sama meskipun format penulisan berbeda`() {
        val existing = listOf("081200000001", "081200000002", "081200000003")

        // Format persis sama
        assertEquals("Nomor HP sudah digunakan oleh akun lain.",
            PhoneRules.validateUniqueness("081200000001", existing))

        // Format dengan +62
        assertEquals("Nomor HP sudah digunakan oleh akun lain.",
            PhoneRules.validateUniqueness("+62 812-0000-0001", existing))

        // Format dengan 62
        assertEquals("Nomor HP sudah digunakan oleh akun lain.",
            PhoneRules.validateUniqueness("6281200000002", existing))

        // Nomor baru yang unik
        assertNull(PhoneRules.validateUniqueness("081299998888", existing))
        assertNull(PhoneRules.validateUniqueness("+62 812-9999-8888", existing))
    }
}
