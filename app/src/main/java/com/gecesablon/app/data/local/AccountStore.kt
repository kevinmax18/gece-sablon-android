package com.gecesablon.app.data.local

import com.gecesablon.app.data.model.AdminAccount
import com.gecesablon.app.data.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Penyimpanan akun bersama (shared in-memory) untuk [com.gecesablon.app.data.repository.FakeAuthRepository]
 * dan [com.gecesablon.app.data.repository.FakeAccountRepository].
 *
 * Menggunakan satu sumber kebenaran (single source of truth) di memori agar perubahan status akun,
 * penambahan admin, atau perubahan kata sandi langsung berefek pada autentikasi dan sebaliknya.
 *
 * TODO: server yang menyimpan hash password
 * TODO: di API asli aktor dari token
 */
class AccountStore {

    /**
     * Entitas akun internal yang menyimpan passwordHash dan data login.
     * Tidak diekspos ke UI atau keluar dari layer data.
     */
    data class StoredAccount(
        val id: String,
        val name: String,
        val email: String,
        val phone: String,
        val passwordHash: String, // TODO: server yang menyimpan hash password
        val role: Role,
        val isActive: Boolean = true,
        val createdAt: Long = System.currentTimeMillis()
    ) {
        fun toAdminAccount(): AdminAccount {
            return AdminAccount(
                id = id,
                name = name,
                email = email,
                phone = phone,
                role = role,
                isActive = isActive,
                createdAt = createdAt
            )
        }
    }

    // Tiga akun contoh awal — TIDAK BOLEH BERUBAH dan TIDAK BOLEH ADA AKUN DEMO BARU DI SEED
    private val _accounts = MutableStateFlow<List<StoredAccount>>(
        listOf(
            StoredAccount(
                id = "user-cust-001",
                name = "Pelanggan Demo",
                email = "pelanggan@gece.test",
                phone = "081234567890",
                passwordHash = hash("Pelanggan123"),
                role = Role.CUSTOMER,
                isActive = true
            ),
            StoredAccount(
                id = "user-admin-001",
                name = "Admin Workshop",
                email = "admin@gece.test",
                phone = "082000000001",
                passwordHash = hash("Admin1234"),
                role = Role.ADMIN,
                isActive = true
            ),
            StoredAccount(
                id = "user-owner-001",
                name = "Owner GeCe",
                email = "owner@gece.test",
                phone = "082000000002",
                passwordHash = hash("Owner1234"),
                role = Role.OWNER,
                isActive = true
            )
        )
    )
    val accounts: StateFlow<List<StoredAccount>> = _accounts.asStateFlow()

    fun getAllAccounts(): List<StoredAccount> = _accounts.value

    fun findByIdentifier(identifier: String): StoredAccount? {
        val trimmed = identifier.trim()
        return _accounts.value.find { acc ->
            acc.email.equals(trimmed, ignoreCase = true) || acc.phone == trimmed
        }
    }

    fun findById(id: String): StoredAccount? {
        return _accounts.value.find { it.id == id }
    }

    fun addCustomerAccount(name: String, email: String, phone: String, password: String): StoredAccount {
        val newAccount = StoredAccount(
            id = "user-cust-${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim(),
            passwordHash = hash(password),
            role = Role.CUSTOMER,
            isActive = true
        )
        _accounts.update { it + newAccount }
        return newAccount
    }

    fun addAdminAccount(name: String, email: String, phone: String, password: String): AdminAccount {
        val newAccount = StoredAccount(
            id = "user-admin-${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim(),
            passwordHash = hash(password),
            role = Role.ADMIN,
            isActive = true
        )
        _accounts.update { it + newAccount }
        return newAccount.toAdminAccount()
    }

    fun updateName(accountId: String, newName: String): Boolean {
        var updated = false
        _accounts.update { list ->
            list.map { acc ->
                if (acc.id == accountId) {
                    updated = true
                    acc.copy(name = newName.trim())
                } else {
                    acc
                }
            }
        }
        return updated
    }

    fun setAktif(accountId: String, isActive: Boolean): Boolean {
        var updated = false
        _accounts.update { list ->
            list.map { acc ->
                if (acc.id == accountId) {
                    // Akun Owner dilarang dinonaktifkan
                    if (acc.role == Role.OWNER && !isActive) {
                        acc
                    } else {
                        updated = true
                        acc.copy(isActive = isActive)
                    }
                } else {
                    acc
                }
            }
        }
        return updated
    }

    fun updatePassword(accountId: String, newPassword: String): Boolean {
        var updated = false
        _accounts.update { list ->
            list.map { acc ->
                if (acc.id == accountId) {
                    updated = true
                    acc.copy(passwordHash = hash(newPassword))
                } else {
                    acc
                }
            }
        }
        return updated
    }

    fun verifyPassword(account: StoredAccount, password: String): Boolean {
        return account.passwordHash == hash(password)
    }

    companion object {
        fun hash(input: String): String = input.hashCode().toString()
    }
}
