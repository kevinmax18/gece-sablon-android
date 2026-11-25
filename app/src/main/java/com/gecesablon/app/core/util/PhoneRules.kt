package com.gecesablon.app.core.util

/**
 * Aturan bisnis murni untuk nomor HP/WhatsApp akun GeCe Screen Printing sesuai Tahap 6D:
 *
 * 1. Normalisasi:
 *    - Buang spasi, tanda hubung (-), dan kurung ((, )).
 *    - Awalan +62 atau 62 diubah menjadi 0.
 *
 * 2. Validasi:
 *    - Wajib diisi (tidak boleh kosong/blank).
 *    - Hanya angka (setelah normalisasi karakter non-digit buangan).
 *    - Diawali angka '0'.
 *    - Panjang tepat 10-14 digit.
 *
 * 3. Pesan error per kasus yang jelas.
 */
object PhoneRules {

    /**
     * Menormalkan string nomor HP:
     * - Membuang spasi (termasuk spasi di dalam), tanda hubung (-), kurung buka '(', dan kurung tutup ')'.
     * - Mengganti awalan "+62" atau "62" dengan "0".
     *
     * Contoh:
     * "+62 812-3456-7890" -> "081234567890"
     * "(0812) 345-678" -> "0812345678"
     * "628123456789" -> "08123456789"
     */
    fun normalize(rawPhone: String): String {
        // Buang spasi, tanda hubung, kurung buka, kurung tutup
        val cleaned = rawPhone.filterNot { it == ' ' || it == '-' || it == '(' || it == ')' || it == '\t' }

        return when {
            cleaned.startsWith("+62") -> "0" + cleaned.removePrefix("+62")
            cleaned.startsWith("62") -> "0" + cleaned.removePrefix("62")
            else -> cleaned
        }
    }

    /**
     * Memvalidasi format nomor HP.
     * Menerima input mentah atau yang sudah dinormalisasi.
     *
     * @param phone Nomor HP yang diinputkan pengguna
     * @return Pesan error jika tidak valid, atau null jika valid
     */
    fun validate(phone: String): String? {
        if (phone.isBlank()) {
            return "Nomor HP wajib diisi."
        }

        val normalized = normalize(phone)

        if (!normalized.all { it.isDigit() }) {
            return "Nomor HP hanya boleh berisi angka."
        }

        if (!normalized.startsWith("0")) {
            return "Nomor HP harus diawali dengan angka 0."
        }

        if (normalized.length < 10) {
            return "Nomor HP terlalu pendek (minimal 10 digit)."
        }

        if (normalized.length > 14) {
            return "Nomor HP terlalu panjang (maksimal 14 digit)."
        }

        return null
    }

    /**
     * Memeriksa keunikan nomor HP terhadap semua akun yang terdaftar setelah dinormalisasi.
     *
     * @param phone Nomor HP yang diinputkan pengguna
     * @param existingPhones Daftar nomor HP akun yang sudah ada (bisa mentah atau normal)
     * @return Pesan error jika duplikat, atau null jika unik
     */
    fun validateUniqueness(
        phone: String,
        existingPhones: List<String>
    ): String? {
        val target = normalize(phone)
        val isDuplicate = existingPhones.any { normalize(it) == target }
        return if (isDuplicate) {
            "Nomor HP sudah digunakan oleh akun lain."
        } else {
            null
        }
    }
}
