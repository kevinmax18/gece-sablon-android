package com.gecesablon.app.data.model

/**
 * Representasi akun staf/pengguna untuk pengelolaan akun admin oleh Owner.
 * Sesuai aturan: kata sandi tidak pernah disimpan dalam model yang dikirim ke UI.
 *
 * @param id          Identifikasi unik akun
 * @param name        Nama lengkap admin/staf
 * @param email       Alamat email akun
 * @param phone       Nomor HP / WhatsApp
 * @param role        Peran pengguna ([Role.ADMIN], [Role.OWNER], dsb.)
 * @param isActive    Status keaktifan akun (true: aktif, false: dinonaktifkan)
 * @param createdAt   Timestamp saat akun dibuat
 */
data class AdminAccount(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val role: Role = Role.ADMIN,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
