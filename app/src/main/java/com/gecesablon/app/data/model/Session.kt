package com.gecesablon.app.data.model

/**
 * Model yang merepresentasikan sesi pengguna yang sedang aktif.
 * Disimpan di SessionStore; hanya berisi data yang diperlukan navigasi dan UI.
 * Email dan nomor HP tidak disimpan di sini (akan diambil dari API saat Tahap 8).
 *
 * @param token  Token autentikasi (string acak di Tahap 3; Bearer token di Tahap 8)
 * @param role   Peran pengguna, selalu berasal dari server / repository
 * @param name   Nama tampilan pengguna
 */
data class Session(
    val token: String,
    val role: Role,
    val name: String
)
