package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductMaterial
import com.gecesablon.app.data.model.ProductSize

/**
 * Hasil validasi form dan aturan bisnis produk GeCe.
 * Mengembalikan error per kolom agar UI dapat menampilkannya di bawah kolom terkait.
 */
data class ProductValidationResult(
    val isValid: Boolean,
    val namaError: String? = null,
    val kategoriError: String? = null,
    val bahanError: String? = null,
    val daftarBahanError: String? = null,
    val deskripsiError: String? = null,
    val hargaDasarError: String? = null,
    val warnaError: String? = null,
    val ukuranError: String? = null,
    val generalError: String? = null
) {
    companion object {
        val Valid = ProductValidationResult(isValid = true)
    }
}

/**
 * Kumpulan fungsi murni (pure functions) untuk validasi produk dan variasinya.
 * Tanpa dependensi Android agar dapat diuji secara deterministik dengan JUnit.
 */
object ProductRules {

    const val MIN_NAMA_LENGTH = 3
    const val MAX_NAMA_LENGTH = 60
    const val MAX_DESKRIPSI_LENGTH = 500
    const val MIN_HARGA_DASAR = 1_000L
    const val MAX_HARGA_DASAR = 10_000_000L

    const val MIN_MATERIAL_NAMA_LENGTH = 2
    const val MAX_MATERIAL_NAMA_LENGTH = 80
    const val MIN_MATERIAL_EXTRA = 0L
    const val MAX_MATERIAL_EXTRA = 10_000_000L

    val ALLOWED_SIZE_LABELS = listOf("S", "M", "L", "XL", "XXL", "XXXL")

    private val HEX_COLOR_REGEX = "^#[0-9a-fA-F]{6}$".toRegex()

    /**
     * Memvalidasi nama produk (3-60 karakter).
     */
    fun validateNama(nama: String): String? {
        val trimmed = nama.trim()
        return when {
            trimmed.isEmpty() -> "Nama produk wajib diisi."
            trimmed.length < MIN_NAMA_LENGTH -> "Nama produk minimal $MIN_NAMA_LENGTH karakter."
            trimmed.length > MAX_NAMA_LENGTH -> "Nama produk maksimal $MAX_NAMA_LENGTH karakter."
            else -> null
        }
    }

    /**
     * Memvalidasi categoryId produk (wajib diisi / dipilih).
     */
    fun validateCategoryId(categoryId: String?): String? {
        return if (categoryId.isNullOrBlank()) {
            "Kategori produk wajib dipilih."
        } else {
            null
        }
    }

    /**
     * Memvalidasi bahan kain ringkasan produk (wajib diisi).
     */
    fun validateBahan(bahan: String): String? {
        return if (bahan.trim().isEmpty()) {
            "Bahan kain wajib diisi."
        } else {
            null
        }
    }

    /**
     * Memvalidasi deskripsi produk (maksimal 500 karakter).
     */
    fun validateDeskripsi(deskripsi: String): String? {
        return if (deskripsi.length > MAX_DESKRIPSI_LENGTH) {
            "Deskripsi produk maksimal $MAX_DESKRIPSI_LENGTH karakter."
        } else {
            null
        }
    }

    /**
     * Memvalidasi harga dasar produk (1.000 - 10.000.000).
     */
    fun validateHargaDasar(harga: Long): String? {
        return when {
            harga < MIN_HARGA_DASAR -> "Harga dasar minimal Rp1.000."
            harga > MAX_HARGA_DASAR -> "Harga dasar maksimal Rp10.000.000."
            else -> null
        }
    }

    /**
     * Memeriksa apakah string merupakan kode hex warna valid (#RRGGBB).
     */
    fun isValidHexColor(hex: String): Boolean {
        return hex.trim().matches(HEX_COLOR_REGEX)
    }

    /**
     * Normalisasi input hex dari pengguna:
     * - Menerima dengan atau tanpa "#"
     * - Mengubah huruf ke UPPERCASE
     * - Membuang karakter non-hex (non 0-9, A-F)
     * - Membatasi maksimal 6 digit
     * - Mengembalikan dalam bentuk "#XXXXXX" (panjang karakter setelah # bisa < 6 jika input kurang)
     * Aturan validasi (#RRGGBB tepat 6 digit) tidak berubah; normalisasi hanya membantu input UX.
     */
    fun normalizeHexInput(raw: String): String {
        val stripped = raw.trim().trimStart('#')
        val hexOnly = stripped.filter { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
            .uppercase()
        val limited = hexOnly.take(6)
        return "#$limited"
    }

    /**
     * Memvalidasi daftar bahan produk:
     * - Minimal 1 bahan
     * - Tepat 1 bahan bawaan (isDefault = true)
     * - Setiap bahan: nama 2-80 karakter, unik per produk tanpa membedakan huruf besar-kecil
     * - Tambahan harga 0 - 10.000.000
     */
    fun validateDaftarBahan(daftarBahan: List<ProductMaterial>): String? {
        if (daftarBahan.isEmpty()) {
            return "Minimal 1 pilihan bahan wajib ditambahkan."
        }

        val defaultCount = daftarBahan.count { it.isDefault }
        if (defaultCount == 0) {
            return "Tepat 1 bahan harus dipilih sebagai bahan bawaan (default)."
        }
        if (defaultCount > 1) {
            return "Hanya boleh ada 1 bahan bawaan (default) yang dipilih."
        }

        val seenNames = mutableSetOf<String>()

        for ((index, item) in daftarBahan.withIndex()) {
            val trimmedName = item.nama.trim()
            if (trimmedName.isEmpty()) {
                return "Nama bahan pada baris ke-${index + 1} tidak boleh kosong."
            }
            if (trimmedName.length < MIN_MATERIAL_NAMA_LENGTH) {
                return "Nama bahan minimal $MIN_MATERIAL_NAMA_LENGTH karakter."
            }
            if (trimmedName.length > MAX_MATERIAL_NAMA_LENGTH) {
                return "Nama bahan maksimal $MAX_MATERIAL_NAMA_LENGTH karakter."
            }

            val lowerName = trimmedName.lowercase()
            if (lowerName in seenNames) {
                return "Terdapat bahan duplikat: nama '$trimmedName' sudah digunakan."
            }
            seenNames.add(lowerName)

            if (item.tambahanHarga < MIN_MATERIAL_EXTRA) {
                return "Tambahan harga bahan '${item.nama}' tidak boleh bernilai negatif."
            }
            if (item.tambahanHarga > MAX_MATERIAL_EXTRA) {
                return "Tambahan harga bahan '${item.nama}' maksimal Rp10.000.000."
            }
        }

        return null
    }

    /**
     * Memvalidasi daftar warna produk:
     * - Minimal 1 warna
     * - Setiap warna: nama wajib dan kode hex #RRGGBB valid
     * - Tanpa warna duplikat (nama maupun hex)
     */
    fun validateWarna(daftarWarna: List<ProductColor>): String? {
        if (daftarWarna.isEmpty()) {
            return "Minimal 1 varian warna wajib ditambahkan."
        }

        val seenNames = mutableSetOf<String>()
        val seenHexes = mutableSetOf<String>()

        for ((index, warna) in daftarWarna.withIndex()) {
            val trimmedName = warna.nama.trim()
            val trimmedHex = warna.hex.trim()

            if (trimmedName.isEmpty()) {
                return "Nama warna pada baris ke-${index + 1} tidak boleh kosong."
            }

            if (!isValidHexColor(trimmedHex)) {
                return "Kode warna untuk '$trimmedName' harus 6 karakter 0-9 atau A-F, contoh #1F2937."
            }

            val lowerName = trimmedName.lowercase()
            if (lowerName in seenNames) {
                return "Terdapat warna duplikat: nama '$trimmedName' sudah digunakan."
            }
            seenNames.add(lowerName)

            val upperHex = trimmedHex.uppercase()
            if (upperHex in seenHexes) {
                return "Terdapat warna duplikat: kode hex '$trimmedHex' sudah digunakan."
            }
            seenHexes.add(upperHex)
        }

        return null
    }

    /**
     * Memvalidasi daftar ukuran produk:
     * - Minimal 1 ukuran
     * - Ukuran harus berada di rentang S-XXXL (S, M, L, XL, XXL, XXXL)
     * - Tambahan harga >= 0
     * - Tanpa ukuran duplikat
     */
    fun validateUkuran(daftarUkuran: List<ProductSize>): String? {
        if (daftarUkuran.isEmpty()) {
            return "Minimal 1 pilihan ukuran wajib dipilih."
        }

        val seenLabels = mutableSetOf<String>()

        for (ukuran in daftarUkuran) {
            val labelUpper = ukuran.label.trim().uppercase()
            if (labelUpper !in ALLOWED_SIZE_LABELS) {
                return "Ukuran '${ukuran.label}' tidak valid. Pilihan ukuran yang diizinkan hanya S sampai XXXL."
            }

            if (ukuran.tambahanHarga < 0L) {
                return "Tambahan harga untuk ukuran '${ukuran.label}' tidak boleh bernilai negatif."
            }

            if (labelUpper in seenLabels) {
                return "Terdapat ukuran duplikat: label '${ukuran.label}' sudah dipilih."
            }
            seenLabels.add(labelUpper)
        }

        return null
    }

    /**
     * Memvalidasi seluruh field input produk sekaligus dan mengembalikan error per kolom.
     */
    fun validateProductFields(
        nama: String,
        categoryId: String?,
        bahan: String,
        deskripsi: String,
        hargaDasar: Long,
        daftarBahan: List<ProductMaterial> = emptyList(),
        daftarWarna: List<ProductColor>,
        daftarUkuran: List<ProductSize>
    ): ProductValidationResult {
        val namaErr = validateNama(nama)
        val kategoriErr = validateCategoryId(categoryId)
        val bahanErr = validateBahan(bahan)
        val daftarBahanErr = validateDaftarBahan(daftarBahan)
        val deskripsiErr = validateDeskripsi(deskripsi)
        val hargaErr = validateHargaDasar(hargaDasar)
        val warnaErr = validateWarna(daftarWarna)
        val ukuranErr = validateUkuran(daftarUkuran)

        val hasError = namaErr != null || kategoriErr != null || bahanErr != null ||
            daftarBahanErr != null || deskripsiErr != null || hargaErr != null ||
            warnaErr != null || ukuranErr != null

        return ProductValidationResult(
            isValid = !hasError,
            namaError = namaErr,
            kategoriError = kategoriErr,
            bahanError = bahanErr,
            daftarBahanError = daftarBahanErr,
            deskripsiError = deskripsiErr,
            hargaDasarError = hargaErr,
            warnaError = warnaErr,
            ukuranError = ukuranErr,
            generalError = if (hasError) "Periksa kembali isian formulir produk." else null
        )
    }

    /**
     * Memvalidasi objek Product utuh.
     */
    fun validateProduct(product: Product): ProductValidationResult {
        return validateProductFields(
            nama = product.nama,
            categoryId = product.categoryId,
            bahan = product.bahan,
            deskripsi = product.deskripsi,
            hargaDasar = product.hargaDasar,
            daftarBahan = product.daftarBahan,
            daftarWarna = product.daftarWarna,
            daftarUkuran = product.daftarUkuran
        )
    }
}
