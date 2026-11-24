package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.AccountRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.local.AccountStore
import com.gecesablon.app.data.model.AdminAccount
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementasi in-memory palsu untuk [AccountRepository].
 * Berbagi penyimpanan data [AccountStore] dengan [FakeAuthRepository].
 *
 * TODO: di API asli aktor dari token
 * TODO: server yang menyimpan hash password
 */
class FakeAccountRepository(
    private val accountStore: AccountStore
) : AccountRepository {

    private fun checkOwnerAccess(actor: Session): Resource.Error? {
        return if (actor.role != Role.OWNER) {
            Resource.Error("Akses ditolak: Hanya Owner yang dapat mengelola akun admin.")
        } else {
            null
        }
    }

    override fun daftarAdmin(actor: Session): Flow<Resource<List<AdminAccount>>> {
        return accountStore.accounts.map { list ->
            if (actor.role != Role.OWNER) {
                Resource.Error("Akses ditolak: Hanya Owner yang dapat mengelola akun admin.")
            } else {
                val adminList = list
                    .filter { it.role == Role.ADMIN }
                    .map { it.toAdminAccount() }
                    .sortedByDescending { it.createdAt }
                Resource.Success(adminList)
            }
        }
    }

    override suspend fun getAdminById(accountId: String, actor: Session): Resource<AdminAccount> {
        delay(300)
        checkOwnerAccess(actor)?.let { return it }

        val account = accountStore.findById(accountId)
            ?: return Resource.Error("Akun staf tidak ditemukan.")

        if (account.role != Role.ADMIN) {
            return Resource.Error("Akun yang dipilih bukan akun staf admin.")
        }

        return Resource.Success(account.toAdminAccount())
    }

    override suspend fun tambahAdmin(
        actor: Session,
        name: String,
        email: String,
        phone: String,
        initialPassword: String
    ): Resource<AdminAccount> {
        delay(600)
        checkOwnerAccess(actor)?.let { return it }

        // Validasi Nama
        AccountRules.validasiNama(name)?.let {
            return Resource.Error(it)
        }

        // Validasi Format Email
        AccountRules.validasiFormatEmail(email)?.let {
            return Resource.Error(it)
        }

        // Validasi Keunikan Email terhadap SEMUA akun (Customer, Admin, Owner)
        val allEmails = accountStore.getAllAccounts().map { it.email }
        AccountRules.validasiKeunikanEmail(email, allEmails)?.let {
            return Resource.Error(it)
        }

        // Validasi Password awal
        AccountRules.validasiPassword(initialPassword)?.let {
            return Resource.Error(it)
        }

        // Role selalu ADMIN (AccountRules.resolveRoleUntukAdminBaru())
        // TODO: server yang menyimpan hash password
        // TODO: di API asli aktor dari token
        val created = accountStore.addAdminAccount(
            name = name,
            email = email,
            phone = phone,
            password = initialPassword
        )

        return Resource.Success(created)
    }

    override suspend fun ubahNama(
        actor: Session,
        accountId: String,
        newName: String
    ): Resource<Unit> {
        delay(400)
        checkOwnerAccess(actor)?.let { return it }

        AccountRules.validasiNama(newName)?.let {
            return Resource.Error(it)
        }

        val account = accountStore.findById(accountId)
            ?: return Resource.Error("Akun staf tidak ditemukan.")

        if (account.role != Role.ADMIN) {
            return Resource.Error("Hanya akun staf admin yang dapat diubah.")
        }

        accountStore.updateName(accountId, newName)
        return Resource.Success(Unit)
    }

    override suspend fun setAktif(
        actor: Session,
        accountId: String,
        isActive: Boolean
    ): Resource<Unit> {
        delay(400)
        checkOwnerAccess(actor)?.let { return it }

        val account = accountStore.findById(accountId)
            ?: return Resource.Error("Akun staf tidak ditemukan.")

        // Akun Owner dilarang dinonaktifkan
        if (!AccountRules.bolehDinonaktifkan(account.role)) {
            return Resource.Error("Akun Owner tidak dapat dinonaktifkan.")
        }

        if (account.role != Role.ADMIN) {
            return Resource.Error("Hanya akun staf admin yang dapat diubah statusnya.")
        }

        accountStore.setAktif(accountId, isActive)
        return Resource.Success(Unit)
    }

    override suspend fun aturUlangPassword(
        actor: Session,
        accountId: String,
        newPassword: String
    ): Resource<Unit> {
        delay(600)
        checkOwnerAccess(actor)?.let { return it }

        val account = accountStore.findById(accountId)
            ?: return Resource.Error("Akun staf tidak ditemukan.")

        if (account.role != Role.ADMIN) {
            return Resource.Error("Hanya akun staf admin yang dapat diatur ulang kata sandinya.")
        }

        AccountRules.validasiPassword(newPassword)?.let {
            return Resource.Error(it)
        }

        // TODO: server yang menyimpan hash password
        // TODO: di API asli aktor dari token
        accountStore.updatePassword(accountId, newPassword)
        return Resource.Success(Unit)
    }
}
