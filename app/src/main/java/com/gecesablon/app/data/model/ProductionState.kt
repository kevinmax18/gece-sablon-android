package com.gecesablon.app.data.model

/**
 * Model data yang merepresentasikan kondisi internal tahapan produksi sablon DTF.
 *
 * @param tahapanSelesai Himpunan tahapan yang telah selesai dikerjakan secara berurutan
 * @param bajuTiba       Penanda material: kain/baju polos telah tiba di workshop
 * @param dtfTiba        Penanda material: lembar film DTF & bubuk curing telah tiba di workshop
 * @param siapDisablon   Penanda kesiapan: bahan baju dan DTF lengkap dan siap naik meja press
 * @param updatedAt      Epoch timestamp terakhir kali status produksi diperbarui (0L bila belum ada)
 * @param updatedBy      Nama staf (Admin/Owner) yang terakhir kali memperbarui tahapan produksi
 */
data class ProductionState(
    val tahapanSelesai: Set<ProductionStage> = emptySet(),
    val bajuTiba: Boolean = false,
    val dtfTiba: Boolean = false,
    val siapDisablon: Boolean = false,
    val updatedAt: Long = 0L,
    val updatedBy: String = ""
)
