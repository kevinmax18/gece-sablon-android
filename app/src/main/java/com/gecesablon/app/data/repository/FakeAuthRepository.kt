package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Implementasi palsu AuthRepository untuk Tahap 3 (tanpa API).
 * Meniru latensi jaringan ±800 ms, menyimpan akun baru di memori.
 *
 * TODO: Hapus di Tahap 8 — ganti dengan implementasi yang memanggil API Laravel.
 */
class FakeAuthRepository : AuthRepository {

    // ---------------------------------------------------------------------------
    // Akun contoh — HANYA ada di file ini, bukan di UI atau konfigurasi lain.
    // TODO: Hapus di Tahap 8 saat integrasi API.
    // ---------------------------------------------------------------------------
    private data class FakeAccount(
        val name: String,
        val email: String,
        val phone: String,
        val passwordHash: String, // disimpan dalam hash agar tidak ada teks asli di memori
        val role: Role
    )

    private val accounts: MutableList<FakeAccount> = mutableListOf(
        FakeAccount(
            name = "Pelanggan Demo",
            email = "pelanggan@gece.test",
            phone = "081234567890",
            passwordHash = hash("Pelanggan123"),
            role = Role.CUSTOMER
        ),
        FakeAccount(
            name = "Admin Workshop",
            email = "admin@gece.test",
            phone = "082000000001",
            passwordHash = hash("Admin1234"),
            role = Role.ADMIN
        ),
        FakeAccount(
            name = "Owner GeCe",
            email = "owner@gece.test",
            phone = "082000000002",
            passwordHash = hash("Owner1234"),
            role = Role.OWNER
        )
    )

    // ---------------------------------------------------------------------------
    // Implementasi
    // ---------------------------------------------------------------------------

    override suspend fun login(identifier: String, password: String): Resource<Session> {
        delay(800) // meniru latensi jaringan

        val identifierTrim = identifier.trim()
        val account = accounts.find { acc ->
            acc.email.equals(identifierTrim, ignoreCase = true) ||
                    acc.phone == identifierTrim
        } ?: return Resource.Error("Email atau nomor HP tidak ditemukan.")

        if (account.passwordHash != hash(password)) {
            return Resource.Error("Kata sandi salah. Silakan coba lagi.")
        }

        return Resource.Success(
            Session(
                token = UUID.randomUUID().toString(),
                role = account.role,
                name = account.name
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

        // Tolak duplikat email (case-insensitive)
        if (accounts.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return Resource.Error("Email sudah digunakan oleh akun lain.")
        }

        // Tolak duplikat nomor HP
        if (accounts.any { it.phone == phone.trim() }) {
            return Resource.Error("Nomor HP sudah digunakan oleh akun lain.")
        }

        // Tambahkan akun baru — role SELALU CUSTOMER, tidak menerima role dari UI
        val newAccount = FakeAccount(
            name = name.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim(),
            passwordHash = hash(password),
            role = Role.CUSTOMER
        )
        accounts.add(newAccount)

        return Resource.Success(
            Session(
                token = UUID.randomUUID().toString(),
                role = Role.CUSTOMER,
                name = newAccount.name
            )
        )
    }

    override suspend fun logout() {
        // Fake: tidak ada token di server yang perlu dibatalkan.
        // Tahap 8: panggil API untuk invalidate token.
    }

    // ---------------------------------------------------------------------------
    // Utilitas internal — hash sederhana agar password tidak tersimpan sebagai teks
    // ---------------------------------------------------------------------------
    private fun hash(input: String): String = input.hashCode().toString()
}
