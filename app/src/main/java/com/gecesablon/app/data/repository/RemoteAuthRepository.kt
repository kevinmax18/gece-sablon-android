package com.gecesablon.app.data.repository

import com.gecesablon.app.core.network.safeApiCall
import com.gecesablon.app.core.network.safeApiCallUnit
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.remote.GeceRemoteApi
import com.gecesablon.app.data.remote.dto.LoginRequestDto
import com.gecesablon.app.data.remote.dto.RegisterRequestDto

/**
 * Implementasi riil AuthRepository yang terhubung ke REST API Laravel via GeceRemoteApi.
 * Role akun ditentukan sepenuhnya oleh server.
 * Repository ini tidak menyimpan sesi secara mandiri karena penyimpanan sesi dilakukan oleh ViewModel pemanggil.
 */
class RemoteAuthRepository(
    private val api: GeceRemoteApi
) : AuthRepository {

    override suspend fun login(identifier: String, password: String): Resource<Session> {
        val request = LoginRequestDto(
            login = identifier,
            password = password
        )

        return when (val result = safeApiCall { api.login(request) }) {
            is Resource.Loading -> Resource.Loading
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> {
                val data = result.data
                val role = try {
                    Role.valueOf(data.user.role.uppercase())
                } catch (e: IllegalArgumentException) {
                    return Resource.Error("Peran akun tidak dikenal.")
                }

                Resource.Success(
                    Session(
                        token = data.token,
                        role = role,
                        name = data.user.name,
                        userId = data.user.id.toString()
                    )
                )
            }
        }
    }

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String
    ): Resource<Session> {
        val request = RegisterRequestDto(
            name = name,
            phone = phone,
            email = email,
            password = password
        )

        return when (val result = safeApiCall { api.register(request) }) {
            is Resource.Loading -> Resource.Loading
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> {
                val data = result.data
                // Registrasi via API selalu menghasilkan akun pelanggan (CUSTOMER)
                Resource.Success(
                    Session(
                        token = data.token,
                        role = Role.CUSTOMER,
                        name = data.user.name,
                        userId = data.user.id.toString()
                    )
                )
            }
        }
    }

    override suspend fun logout() {
        // Best-effort: kirim permohonan pembatalan token ke endpoint server,
        // error apa pun diabaikan agar tidak pernah melempar exception keluar repository
        try {
            safeApiCallUnit { api.logout() }
        } catch (_: Exception) {
            // Sengaja diabaikan agar proses pembersihan sesi lokal tetap berjalan tuntas
        }
    }

    override suspend fun changePassword(
        actor: Session,
        oldPassword: String,
        newPassword: String
    ): Resource<Unit> {
        return Resource.Error("Fitur ubah kata sandi belum tersedia di server.")
    }
}
