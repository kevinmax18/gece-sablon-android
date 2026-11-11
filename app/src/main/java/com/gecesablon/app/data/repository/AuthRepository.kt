package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Session

/**
 * Kontrak repository autentikasi. Implementasi nyata menggunakan API (Tahap 8);
 * implementasi palsu (FakeAuthRepository) dipakai di Tahap 3.
 * Role SELALU ditentukan oleh repository, tidak pernah diterima dari UI.
 */
interface AuthRepository {
    /**
     * Login dengan email atau nomor HP beserta password.
     * @return Resource.Success(Session) jika berhasil, Resource.Error(pesan) jika gagal.
     */
    suspend fun login(identifier: String, password: String): Resource<Session>

    /**
     * Daftarkan akun pelanggan baru. Role selalu CUSTOMER.
     * @return Resource.Success(Session) jika berhasil (auto-login), Resource.Error(pesan) jika gagal.
     */
    suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String
    ): Resource<Session>

    /** Logout: implementasi nyata akan membatalkan token di server (Tahap 8). */
    suspend fun logout()
}
