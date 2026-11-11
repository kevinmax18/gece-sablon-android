package com.gecesablon.app.core.util

/**
 * Resource membungkus status data dari proses async atau remote (Loading, Success, Error).
 * Pesan error menggunakan Bahasa Indonesia agar bisa langsung ditampilkan ke pengguna.
 */
sealed class Resource<out T> {
    /** Proses sedang berjalan. */
    data object Loading : Resource<Nothing>()

    /** Proses selesai dengan data. */
    data class Success<T>(val data: T) : Resource<T>()

    /** Proses gagal dengan pesan error berbahasa Indonesia. */
    data class Error(val message: String) : Resource<Nothing>()
}
