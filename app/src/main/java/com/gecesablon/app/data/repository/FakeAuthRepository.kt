package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.AccountRules
import com.gecesablon.app.core.util.PasswordRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.local.AccountStore
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Implementasi palsu AuthRepository untuk autentikasi in-memory.
 * Menggunakan shared [AccountStore] bersama [FakeAccountRepository].
 * Meniru latensi jaringan ±800 ms.
 *
 * TODO: Hapus di Tahap 8 — ganti dengan implementasi yang memanggil API Laravel.
 * TODO: server yang menyimpan hash password
 * TODO: di API asli aktor dari token
 */
class FakeAuthRepository(
    private val accountStore: AccountStore = AccountStore()
) : AuthRepository {

    override suspend fun login(identifier: String, password: String): Resource<Session> {
        delay(800) // meniru latensi jaringan

        val account = accountStore.findByIdentifier(identifier)
            ?: return Resource.Error("Email atau nomor HP tidak ditemukan.")

        // Periksa status keaktifan akun
        if (!account.isActive) {
            return Resource.Error("Akun dinonaktifkan. Hubungi Owner.")
        }

        if (!accountStore.verifyPassword(account, password)) {
            return Resource.Error("Kata sandi salah. Silakan coba lagi.")
        }

        return Resource.Success(
            Session(
                token = UUID.randomUUID().toString(),
                role = account.role,
                name = account.name,
                userId = account.id
            )
        )
    }

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String
    ): Resource<Session> {
        delay(800) // meniru latensi jaringan

        val allEmails = accountStore.getAllAccounts().map { it.email }
        AccountRules.validasiKeunikanEmail(email, allEmails)?.let {
            return Resource.Error("Email sudah digunakan oleh akun lain.")
        }

        // Tolak duplikat nomor HP
        if (accountStore.getAllAccounts().any { it.phone == phone.trim() }) {
            return Resource.Error("Nomor HP sudah digunakan oleh akun lain.")
        }

        // Tambahkan akun baru — role SELALU CUSTOMER, tidak menerima role dari UI
        // TODO: server yang menyimpan hash password
        val newAccount = accountStore.addCustomerAccount(
            name = name,
            email = email,
            phone = phone,
            password = password
        )

        return Resource.Success(
            Session(
                token = UUID.randomUUID().toString(),
                role = Role.CUSTOMER,
                name = newAccount.name,
                userId = newAccount.id
            )
        )
    }

    override suspend fun logout() {
        // Fake: tidak ada token di server yang perlu dibatalkan.
        // Tahap 8: panggil API untuk invalidate token.
    }

    override suspend fun changePassword(
        actor: Session,
        oldPassword: String,
        newPassword: String
    ): Resource<Unit> {
        delay(600) // meniru latensi jaringan

        val account = accountStore.findById(actor.userId)
            ?: accountStore.getAllAccounts().find { it.name.equals(actor.name, ignoreCase = true) }
            ?: return Resource.Error("Akun tidak ditemukan atau sesi tidak valid.")

        if (!account.isActive) {
            return Resource.Error("Akun dinonaktifkan. Hubungi Owner.")
        }

        if (!accountStore.verifyPassword(account, oldPassword)) {
            return Resource.Error("Kata sandi lama salah.")
        }

        val validation = PasswordRules.validasiBaru(oldPassword, newPassword, newPassword)
        if (!validation.isValid) {
            val errorMsg = validation.newPasswordError
                ?: validation.oldPasswordError
                ?: "Format kata sandi baru tidak memenuhi syarat."
            return Resource.Error(errorMsg)
        }

        // TODO: server yang menyimpan hash password
        // TODO: di API asli aktor dari token
        accountStore.updatePassword(account.id, newPassword)
        return Resource.Success(Unit)
    }
}
