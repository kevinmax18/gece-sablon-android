package com.gecesablon.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk fungsi validasi [PasswordRules].
 * Menguji skenario:
 * - lama kosong
 * - baru kosong / konfirmasi kosong
 * - terlalu pendek (< 8 karakter)
 * - tanpa huruf
 * - tanpa angka
 * - sama dengan lama
 * - konfirmasi beda
 * - valid
 */
class PasswordRulesTest {

    @Test
    fun `kata sandi lama kosong menghasilkan error`() {
        val result = PasswordRules.validasiBaru("", "Password123", "Password123")
        assertFalse(result.isValid)
        assertEquals("Kata sandi lama wajib diisi.", result.oldPasswordError)
        assertNull(result.newPasswordError)
        assertNull(result.confirmPasswordError)
    }

    @Test
    fun `kata sandi baru terlalu pendek menghasilkan error`() {
        val result = PasswordRules.validasiBaru("Lama1234", "Pass1", "Pass1")
        assertFalse(result.isValid)
        assertNull(result.oldPasswordError)
        assertEquals("Kata sandi baru minimal 8 karakter.", result.newPasswordError)
        assertNull(result.confirmPasswordError)
    }

    @Test
    fun `kata sandi baru tanpa huruf menghasilkan error`() {
        val result = PasswordRules.validasiBaru("Lama1234", "12345678", "12345678")
        assertFalse(result.isValid)
        assertNull(result.oldPasswordError)
        assertEquals("Kata sandi baru harus mengandung huruf.", result.newPasswordError)
        assertNull(result.confirmPasswordError)
    }

    @Test
    fun `kata sandi baru tanpa angka menghasilkan error`() {
        val result = PasswordRules.validasiBaru("Lama1234", "PasswordBaru", "PasswordBaru")
        assertFalse(result.isValid)
        assertNull(result.oldPasswordError)
        assertEquals("Kata sandi baru harus mengandung angka.", result.newPasswordError)
        assertNull(result.confirmPasswordError)
    }

    @Test
    fun `kata sandi baru sama dengan lama menghasilkan error`() {
        val result = PasswordRules.validasiBaru("Password123", "Password123", "Password123")
        assertFalse(result.isValid)
        assertNull(result.oldPasswordError)
        assertEquals("Kata sandi baru tidak boleh sama dengan kata sandi lama.", result.newPasswordError)
        assertNull(result.confirmPasswordError)
    }

    @Test
    fun `konfirmasi kata sandi beda menghasilkan error`() {
        val result = PasswordRules.validasiBaru("Lama1234", "Baru1234", "Beda1234")
        assertFalse(result.isValid)
        assertNull(result.oldPasswordError)
        assertNull(result.newPasswordError)
        assertEquals("Konfirmasi kata sandi tidak cocok.", result.confirmPasswordError)
    }

    @Test
    fun `semua input valid mengembalikan isValid true`() {
        val result = PasswordRules.validasiBaru("Lama1234", "Baru1234", "Baru1234")
        assertTrue(result.isValid)
        assertNull(result.oldPasswordError)
        assertNull(result.newPasswordError)
        assertNull(result.confirmPasswordError)
    }
}
