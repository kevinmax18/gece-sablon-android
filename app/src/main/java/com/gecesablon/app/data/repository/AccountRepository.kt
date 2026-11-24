package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.AdminAccount
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository untuk manajemen akun staf admin oleh Owner.
 * Bebas dari tipe Android agar mudah diganti implementasi Retrofit nantinya.
 *
 * Seluruh fungsi sensitif menerima [actor: Session] dan hanya diizinkan untuk [Role.OWNER].
 * Akses oleh peran lain ditolak dengan Resource.Error.
 *
 * TODO: di API asli aktor dari token
 */
interface AccountRepository {

    /**
     * Mengalirkan daftar semua akun staf admin secara reaktif.
     * Hanya boleh diakses oleh [Role.OWNER].
     *
     * @param actor Sesi pengguna yang meminta data
     */
    fun daftarAdmin(actor: Session): Flow<Resource<List<AdminAccount>>>

    /**
     * Mengambil detail satu akun staf admin berdasarkan ID.
     *
     * @param accountId ID akun admin
     * @param actor Sesi pengguna yang meminta data
     */
    suspend fun getAdminById(accountId: String, actor: Session): Resource<AdminAccount>

    /**
     * Menambahkan akun admin baru.
     * Role selalu [Role.ADMIN]. Email harus valid dan unik terhadap semua akun (case-insensitive).
     * Password awal minimal 8 karakter, kombinasi huruf dan angka.
     *
     * @param actor Sesi pembuat akun (wajib OWNER)
     * @param name Nama lengkap staf
     * @param email Alamat email
     * @param phone Nomor telepon / HP (opsional)
     * @param initialPassword Kata sandi awal sementara
     */
    suspend fun tambahAdmin(
        actor: Session,
        name: String,
        email: String,
        phone: String,
        initialPassword: String
    ): Resource<AdminAccount>

    /**
     * Mengubah nama staf admin.
     *
     * @param actor Sesi pengguna (wajib OWNER)
     * @param accountId ID akun yang akan diubah
     * @param newName Nama baru
     */
    suspend fun ubahNama(
        actor: Session,
        accountId: String,
        newName: String
    ): Resource<Unit>

    /**
     * Mengaktifkan atau menonaktifkan akun staf.
     * Akun OWNER tidak pernah bisa dinonaktifkan.
     *
     * @param actor Sesi pengguna (wajib OWNER)
     * @param accountId ID akun yang akan diubah statusnya
     * @param isActive true jika aktif, false jika dinonaktifkan
     */
    suspend fun setAktif(
        actor: Session,
        accountId: String,
        isActive: Boolean
    ): Resource<Unit>

    /**
     * Mengatur ulang kata sandi staf oleh Owner.
     * Password baru harus memenuhi aturan yang sama (min 8 karakter, huruf + angka).
     * Password tidak pernah disimpan di log atau ditampilkan ulang.
     *
     * @param actor Sesi pengguna (wajib OWNER)
     * @param accountId ID akun yang disetel ulang
     * @param newPassword Kata sandi sementara baru
     */
    suspend fun aturUlangPassword(
        actor: Session,
        accountId: String,
        newPassword: String
    ): Resource<Unit>
}
