package com.gecesablon.app.data.model

import java.util.UUID

/**
 * Entri catatan aktivitas / linimasa produksi pesanan.
 *
 * @param id     ID unik entri log
 * @param waktu  Timestamp waktu kejadian (epoch millis)
 * @param aktor  Nama pembuat aksi (pelanggan, nama staf admin, atau owner)
 * @param aksi   Deskripsi tindakan yang dilakukan
 */
data class ActivityLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val waktu: Long = System.currentTimeMillis(),
    val aktor: String,
    val aksi: String
)
