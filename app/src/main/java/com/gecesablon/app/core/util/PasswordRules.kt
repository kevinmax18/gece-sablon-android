package com.gecesablon.app.core.util

/**
 * Validasi kata sandi murni untuk aturan bisnis GeCe Screen Printing.
 * Sesuai docs/spesifikasi.md:
 * - Wajib: kata sandi lama (harus diisi), kata sandi baru, konfirmasi.
 * - Kata sandi baru minimal 8 karakter.
 * - Mengandung huruf dan angka.
 * - Tidak boleh sama dengan kata sandi lama.
 * - Konfirmasi harus sama persis dengan kata sandi baru.
 */
object PasswordRules {

    data class ValidationResult(
        val oldPasswordError: String? = null,
        val newPasswordError: String? = null,
        val confirmPasswordError: String? = null
    ) {
        val isValid: Boolean
            get() = oldPasswordError == null && newPasswordError == null && confirmPasswordError == null
    }

    /**
     * Memvalidasi form ubah kata sandi baru.
     * @param lama Kata sandi lama yang dimasukkan pengguna
     * @param baru Kata sandi baru yang diinginkan
     * @param konfirmasi Konfirmasi pengulangan kata sandi baru
     * @return [ValidationResult] berisi pesan error per kolom (atau null jika valid)
     */
    fun validasiBaru(lama: String, baru: String, konfirmasi: String): ValidationResult {
        val oldError = if (lama.isBlank()) {
            "Kata sandi lama wajib diisi."
        } else {
            null
        }

        val newError = when {
            baru.isBlank() -> "Kata sandi baru wajib diisi."
            baru.length < 8 -> "Kata sandi baru minimal 8 karakter."
            !baru.any { it.isLetter() } -> "Kata sandi baru harus mengandung huruf."
            !baru.any { it.isDigit() } -> "Kata sandi baru harus mengandung angka."
            lama.isNotBlank() && baru == lama -> "Kata sandi baru tidak boleh sama dengan kata sandi lama."
            else -> null
        }

        val confirmError = when {
            konfirmasi.isBlank() -> "Konfirmasi kata sandi wajib diisi."
            konfirmasi != baru -> "Konfirmasi kata sandi tidak cocok."
            else -> null
        }

        return ValidationResult(
            oldPasswordError = oldError,
            newPasswordError = newError,
            confirmPasswordError = confirmError
        )
    }
}
