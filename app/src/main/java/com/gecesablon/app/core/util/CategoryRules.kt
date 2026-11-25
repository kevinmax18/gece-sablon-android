package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Category

/**
 * Aturan bisnis dan validasi untuk kategori produk GeCe Screen Printing:
 * - Nama kategori 2-40 karakter setelah spasi pinggir dibuang
 * - Nama unik tanpa membedakan huruf besar-kecil (case-insensitive)
 * - Fungsi murni tanpa dependensi Android agar mudah diuji secara deterministik dengan JUnit.
 */
object CategoryRules {

    const val MIN_NAMA_LENGTH = 2
    const val MAX_NAMA_LENGTH = 40

    /**
     * Membersihkan nama kategori: membuang spasi pinggir (leading/trailing whitespace).
     */
    fun cleanNama(nama: String): String {
        return nama.trim()
    }

    /**
     * Memvalidasi nama kategori tunggal:
     * - Tidak boleh kosong
     * - Panjang 2-40 karakter
     */
    fun validateNama(nama: String): String? {
        val cleaned = cleanNama(nama)
        return when {
            cleaned.isEmpty() -> "Nama kategori wajib diisi."
            cleaned.length < MIN_NAMA_LENGTH -> "Nama kategori minimal $MIN_NAMA_LENGTH karakter."
            cleaned.length > MAX_NAMA_LENGTH -> "Nama kategori maksimal $MAX_NAMA_LENGTH karakter."
            else -> null
        }
    }

    /**
     * Memeriksa keunikan nama kategori (case-insensitive) terhadap daftar kategori yang ada.
     * @param nama Nama kategori yang akan dicek.
     * @param existingCategories Daftar kategori yang ada saat ini.
     * @param excludeId ID kategori yang dikecualikan (misal saat ubah nama kategori yang sama).
     * @return null jika unik, pesan kesalahan jika nama sudah digunakan.
     */
    fun validateNamaUnik(
        nama: String,
        existingCategories: List<Category>,
        excludeId: String? = null
    ): String? {
        val basicError = validateNama(nama)
        if (basicError != null) return basicError

        val cleaned = cleanNama(nama)
        val isDuplicate = existingCategories.any { cat ->
            (excludeId == null || cat.id != excludeId) &&
                cat.nama.trim().equals(cleaned, ignoreCase = true)
        }

        return if (isDuplicate) {
            "Kategori dengan nama '$cleaned' sudah ada."
        } else {
            null
        }
    }
}
