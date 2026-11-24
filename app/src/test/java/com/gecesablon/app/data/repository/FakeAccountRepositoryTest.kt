package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.local.AccountStore
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pengujian unit untuk [FakeAccountRepository] dan sinkronisasi dengan [FakeAuthRepository] melalui [AccountStore].
 */
class FakeAccountRepositoryTest {

    private lateinit var accountStore: AccountStore
    private lateinit var accountRepository: AccountRepository
    private lateinit var authRepository: AuthRepository

    private val ownerSession = Session(
        token = "token-owner",
        role = Role.OWNER,
        name = "Owner GeCe",
        userId = "user-owner-001"
    )

    private val adminSession = Session(
        token = "token-admin",
        role = Role.ADMIN,
        name = "Admin Workshop",
        userId = "user-admin-001"
    )

    private val customerSession = Session(
        token = "token-cust",
        role = Role.CUSTOMER,
        name = "Pelanggan Demo",
        userId = "user-cust-001"
    )

    @Before
    fun setUp() {
        accountStore = AccountStore()
        accountRepository = FakeAccountRepository(accountStore)
        authRepository = FakeAuthRepository(accountStore)
    }

    @Test
    fun `daftarAdmin hanya dapat diakses oleh Owner dan ditolak untuk peran lain`() = runBlocking {
        val ownerResult = accountRepository.daftarAdmin(ownerSession).first()
        assertTrue(ownerResult is Resource.Success)
        val initialAdmins = (ownerResult as Resource.Success).data
        assertEquals(1, initialAdmins.size)
        assertEquals("admin@gece.test", initialAdmins.first().email)

        val adminResult = accountRepository.daftarAdmin(adminSession).first()
        assertTrue(adminResult is Resource.Error)

        val customerResult = accountRepository.daftarAdmin(customerSession).first()
        assertTrue(customerResult is Resource.Error)
    }

    @Test
    fun `tambahAdmin berhasil menambahkan akun dengan role ADMIN dan sinkron ke login`() = runBlocking {
        val result = accountRepository.tambahAdmin(
            actor = ownerSession,
            name = "Staf Baru",
            email = "stafbaru@gece.test",
            phone = "081299998888",
            initialPassword = "Password123"
        )
        assertTrue(result is Resource.Success)
        val created = (result as Resource.Success).data
        assertEquals(Role.ADMIN, created.role)
        assertEquals("stafbaru@gece.test", created.email)
        assertTrue(created.isActive)

        // Login menggunakan akun baru di auth repository
        val loginResult = authRepository.login("stafbaru@gece.test", "Password123")
        assertTrue(loginResult is Resource.Success)
        val session = (loginResult as Resource.Success).data
        assertEquals(Role.ADMIN, session.role)
        assertEquals("Staf Baru", session.name)
    }

    @Test
    fun `tambahAdmin menolak email duplikat case-insensitive terhadap semua peran`() = runBlocking {
        // Coba duplikat email admin
        val dupeAdmin = accountRepository.tambahAdmin(
            actor = ownerSession,
            name = "Staf Kembar",
            email = "ADMIN@GECE.TEST",
            phone = "081299990001",
            initialPassword = "Password123"
        )
        assertTrue(dupeAdmin is Resource.Error)
        assertEquals("Email sudah digunakan oleh akun lain.", (dupeAdmin as Resource.Error).message)

        // Coba duplikat email pelanggan
        val dupeCust = accountRepository.tambahAdmin(
            actor = ownerSession,
            name = "Staf Pelanggan",
            email = "Pelanggan@Gece.Test",
            phone = "081299990002",
            initialPassword = "Password123"
        )
        assertTrue(dupeCust is Resource.Error)
        assertEquals("Email sudah digunakan oleh akun lain.", (dupeCust as Resource.Error).message)
    }

    @Test
    fun `penonaktifan akun menolak login dengan pesan tepat dan dapat diaktifkan kembali`() = runBlocking {
        // Nonaktifkan admin-001
        val deactResult = accountRepository.setAktif(ownerSession, "user-admin-001", false)
        assertTrue(deactResult is Resource.Success)

        // Login ditolak dengan pesan spesifikasi tepat
        val loginResult = authRepository.login("admin@gece.test", "Admin1234")
        assertTrue(loginResult is Resource.Error)
        assertEquals("Akun dinonaktifkan. Hubungi Owner.", (loginResult as Resource.Error).message)

        // Aktifkan kembali
        val reactResult = accountRepository.setAktif(ownerSession, "user-admin-001", true)
        assertTrue(reactResult is Resource.Success)

        val loginSuccess = authRepository.login("admin@gece.test", "Admin1234")
        assertTrue(loginSuccess is Resource.Success)
    }

    @Test
    fun `akun owner tidak dapat dinonaktifkan`() = runBlocking {
        val result = accountRepository.setAktif(ownerSession, "user-owner-001", false)
        assertTrue(result is Resource.Error)

        val owner = accountStore.findById("user-owner-001")
        assertNotNull(owner)
        assertTrue(owner!!.isActive)
    }

    @Test
    fun `aturUlangPassword oleh owner memperbarui kata sandi login staf`() = runBlocking {
        val resetResult = accountRepository.aturUlangPassword(
            actor = ownerSession,
            accountId = "user-admin-001",
            newPassword = "PasswordBaru99"
        )
        assertTrue(resetResult is Resource.Success)

        // Password lama gagal
        val oldLogin = authRepository.login("admin@gece.test", "Admin1234")
        assertTrue(oldLogin is Resource.Error)

        // Password baru berhasil
        val newLogin = authRepository.login("admin@gece.test", "PasswordBaru99")
        assertTrue(newLogin is Resource.Success)
    }

    @Test
    fun `ubahNama berhasil memperbarui nama akun staf admin`() = runBlocking {
        val res = accountRepository.ubahNama(ownerSession, "user-admin-001", "Admin Senior Workshop")
        assertTrue(res is Resource.Success)

        val getRes = accountRepository.getAdminById("user-admin-001", ownerSession)
        assertTrue(getRes is Resource.Success)
        assertEquals("Admin Senior Workshop", (getRes as Resource.Success).data.name)
    }
}
