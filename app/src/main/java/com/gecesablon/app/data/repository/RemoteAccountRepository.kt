package com.gecesablon.app.data.repository

import com.gecesablon.app.core.network.safeApiCall
import com.gecesablon.app.core.network.safeApiCallUnit
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.AdminAccount
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.remote.GeceRemoteApi
import com.gecesablon.app.data.remote.dto.CreateAdminAccountRequestDto
import com.gecesablon.app.data.remote.dto.UpdateAdminAccountRequestDto
import com.gecesablon.app.data.remote.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementasi AccountRepository yang terhubung ke REST API Laravel GeCe Sablon.
 * Hanya dapat diakses oleh Owner untuk mengelola staf admin.
 */
class RemoteAccountRepository(
    private val api: GeceRemoteApi
) : AccountRepository {

    override fun daftarAdmin(actor: Session): Flow<Resource<List<AdminAccount>>> = flow {
        emit(Resource.Loading)
        if (actor.role != Role.OWNER) {
            emit(Resource.Error("Akses ditolak: Hanya Owner yang dapat mengelola akun admin."))
            return@flow
        }
        val result = safeApiCall { api.getAdminAccounts() }
        when (result) {
            is Resource.Success -> emit(Resource.Success(result.data.map { it.toDomain() }))
            is Resource.Error -> emit(Resource.Error(result.message))
            is Resource.Loading -> emit(Resource.Loading)
        }
    }

    override suspend fun getAdminById(accountId: String, actor: Session): Resource<AdminAccount> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya Owner yang dapat melihat detail akun admin.")
        }
        val listResult = safeApiCall { api.getAdminAccounts() }
        return when (listResult) {
            is Resource.Success -> {
                val found = listResult.data.find { it.id.toString() == accountId }
                if (found != null) {
                    Resource.Success(found.toDomain())
                } else {
                    Resource.Error("Akun staf admin dengan ID $accountId tidak ditemukan.")
                }
            }
            is Resource.Error -> Resource.Error(listResult.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun tambahAdmin(
        actor: Session,
        name: String,
        email: String,
        phone: String,
        initialPassword: String
    ): Resource<AdminAccount> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya Owner yang dapat menambahkan akun admin.")
        }
        if (initialPassword.length < 8) {
            return Resource.Error("Password awal minimal 8 karakter.")
        }
        val req = CreateAdminAccountRequestDto(
            name = name,
            email = email,
            phone = phone,
            password = initialPassword
        )
        val result = safeApiCall { api.createAdminAccount(req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun ubahNama(
        actor: Session,
        accountId: String,
        newName: String
    ): Resource<Unit> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya Owner yang dapat mengubah nama admin.")
        }
        val intId = accountId.toIntOrNull()
            ?: return Resource.Error("ID akun tidak valid: $accountId")

        val req = UpdateAdminAccountRequestDto(name = newName)
        return safeApiCallUnit { api.updateAdminAccount(intId, req) }
    }

    override suspend fun setAktif(
        actor: Session,
        accountId: String,
        isActive: Boolean
    ): Resource<Unit> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya Owner yang dapat mengubah status akun admin.")
        }
        val intId = accountId.toIntOrNull()
            ?: return Resource.Error("ID akun tidak valid: $accountId")

        val req = UpdateAdminAccountRequestDto(isActive = isActive)
        return safeApiCallUnit { api.updateAdminAccount(intId, req) }
    }

    override suspend fun aturUlangPassword(
        actor: Session,
        accountId: String,
        newPassword: String
    ): Resource<Unit> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya Owner yang dapat mengatur ulang password.")
        }
        if (newPassword.length < 8) {
            return Resource.Error("Password baru minimal 8 karakter.")
        }
        val intId = accountId.toIntOrNull()
            ?: return Resource.Error("ID akun tidak valid: $accountId")

        val req = UpdateAdminAccountRequestDto(password = newPassword)
        return safeApiCallUnit { api.updateAdminAccount(intId, req) }
    }
}
