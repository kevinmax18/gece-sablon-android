package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Role

/**
 * Aturan bisnis murni untuk validasi akun, email, password awal / atur ulang,
 * dan perlindungan hak akses akun sesuai spesifikasi GeCe Screen Printing:
 *
 * 1. Role selalu [Role.ADMIN] untuk akun yang ditambahkan owner.
 * 2. Akun [Role.OWNER] tidak dapat dinonaktifkan atau dihapus lewat aplikasi.
 * 3. Email harus valid dan unik terhadap SEMUA akun tanpa membedakan huruf besar-kecil.
 * 4. Password awal & atur ulang minimal 8 karakter, mengandung huruf dan angka.
 */
object AccountRules {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

    /**
     * Memvalidasi format email.
     * @param email String alamat email
     * @return Pesan error jika tidak valid, atau null jika valid
     */
    fun validasiFormatEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isBlank() -> "Email wajib diisi."
            !EMAIL_REGEX.matches(trimmed) -> "Format email tidak valid."
            else -> null
        }
    }

    /**
     * Memeriksa apakah email sudah digunakan oleh akun lain (case-insensitive).
     * @param email Alamat email baru
     * @param existingEmails Daftar email semua akun yang sudah terdaftar
     * @param currentAccountId ID akun saat ini jika sedang mengedit (opsional)
     * @return Pesan error jika sudah digunakan, atau null jika unik
     */
    fun validasiKeunikanEmail(
        email: String,
        existingEmails: List<String>
    ): String? {
        val trimmed = email.trim()
        val isDuplicate = existingEmails.any { it.equals(trimmed, ignoreCase = true) }
        return if (isDuplicate) {
            "Email sudah digunakan oleh akun lain."
        } else {
            null
        }
    }

    /**
     * Memvalidasi nama admin.
     * @param name Nama lengkap staf admin
     * @return Pesan error jika tidak valid, atau null jika valid
     */
    fun validasiNama(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> "Nama lengkap wajib diisi."
            trimmed.length < 3 -> "Nama minimal 3 karakter."
            trimmed.length > 60 -> "Nama maksimal 60 karakter."
            else -> null
        }
    }

    /**
     * Memvalidasi password awal atau password baru untuk atur ulang kata sandi oleh Owner.
     * Aturan: minimal 8 karakter, mengandung huruf dan angka.
     *
     * @param password Password yang diinputkan
     * @return Pesan error jika tidak memenuhi syarat, atau null jika valid
     */
    fun validasiPassword(password: String): String? {
        return when {
            password.isBlank() -> "Kata sandi wajib diisi."
            password.length < 8 -> "Kata sandi minimal 8 karakter."
            !password.any { it.isLetter() } -> "Kata sandi harus mengandung huruf."
            !password.any { it.isDigit() } -> "Kata sandi harus mengandung angka."
            else -> null
        }
    }

    /**
     * Memeriksa apakah suatu akun boleh dinonaktifkan.
     * Akun Owner TIDAK PERNAH boleh dinonaktifkan lewat aplikasi.
     *
     * @param role Peran dari akun yang ingin diubah statusnya
     * @return true jika boleh dinonaktifkan, false jika dilarang (misal Owner)
     */
    fun bolehDinonaktifkan(role: Role): Boolean {
        return role != Role.OWNER
    }

    /**
     * Memastikan role yang dihasilkan selalu [Role.ADMIN].
     */
    fun resolveRoleUntukAdminBaru(): Role {
        return Role.ADMIN
    }
}
