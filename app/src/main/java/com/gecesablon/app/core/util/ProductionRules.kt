package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.ProductionStage
import com.gecesablon.app.data.model.ProductionState

/**
 * Hasil pengecekan aturan produksi.
 *
 * @param diizinkan True jika aksi diizinkan sesuai aturan bisnis
 * @param pesan     Alasan penolakan (bahasa Indonesia) bila [diizinkan] bernilai false
 */
data class RuleCheckResult(
    val diizinkan: Boolean,
    val pesan: String? = null
) {
    companion object {
        val Lolos = RuleCheckResult(diizinkan = true, pesan = null)
        fun Ditolak(pesan: String) = RuleCheckResult(diizinkan = false, pesan = pesan)
    }
}

/**
 * Kumpulan fungsi murni (pure functions) yang mengatur aturan produksi,
 * validasi ketergantungan material, dan penurunan status pesanan pelanggan.
 * Tidak memiliki dependensi Android maupun state internal.
 */
object ProductionRules {

    /**
     * Menemukan tahapan produksi yang sedang aktif (tahapan pertama berurutan yang belum selesai).
     * Mengembalikan null jika seluruh tahapan (1..5) telah selesai.
     */
    fun tahapanAktif(state: ProductionState): ProductionStage? {
        return ProductionStage.orderedStages.firstOrNull { it !in state.tahapanSelesai }
    }

    /**
     * Memeriksa apakah tahapan produksi dapat diubah pada status pesanan tertentu.
     * Pesanan DITERIMA, DIBATALKAN, atau SELESAI tidak boleh dimanipulasi tahapannya.
     */
    fun bolehUbahProduksi(status: OrderStatus): RuleCheckResult {
        return when (status) {
            OrderStatus.DITERIMA -> RuleCheckResult.Ditolak(
                "Pesanan masih berstatus Diterima. Silakan terima dan mulai proses terlebih dahulu."
            )
            OrderStatus.DIBATALKAN -> RuleCheckResult.Ditolak(
                "Pesanan telah dibatalkan dan tidak dapat diproses lagi."
            )
            OrderStatus.SELESAI -> RuleCheckResult.Ditolak(
                "Pesanan telah selesai dan tidak dapat diubah lagi."
            )
            else -> RuleCheckResult.Lolos
        }
    }

    /**
     * Memeriksa apakah suatu tahapan boleh dicentang (ditandai selesai).
     */
    fun bolehCentang(stage: ProductionStage, state: ProductionState): RuleCheckResult {
        if (stage in state.tahapanSelesai) {
            return RuleCheckResult.Ditolak("Tahapan ini sudah ditandai selesai.")
        }

        // Tahapan harus berurutan: semua tahapan sebelum `stage` harus sudah selesai
        val tahapanSebelumnya = ProductionStage.orderedStages.filter { it.nomor < stage.nomor }
        val belumSelesai = tahapanSebelumnya.firstOrNull { it !in state.tahapanSelesai }
        if (belumSelesai != null) {
            return RuleCheckResult.Ditolak("Selesaikan tahapan sebelumnya terlebih dahulu.")
        }

        // Prasyarat material per tahapan
        when (stage) {
            ProductionStage.PENGADAAN_BAJU -> {
                if (!state.bajuTiba) {
                    return RuleCheckResult.Ditolak("Baju polos harus tiba di workshop terlebih dahulu.")
                }
            }
            ProductionStage.CETAK_DTF_MATERIAL -> {
                if (!state.dtfTiba) {
                    return RuleCheckResult.Ditolak("Film DTF & material cetak harus tiba di workshop terlebih dahulu.")
                }
            }
            ProductionStage.PENYABLONAN -> {
                if (!state.siapDisablon) {
                    return RuleCheckResult.Ditolak("Material baju dan DTF harus ditandai siap disablon terlebih dahulu.")
                }
            }
            else -> Unit
        }

        return RuleCheckResult.Lolos
    }

    /**
     * Memeriksa apakah centang suatu tahapan boleh dibatalkan.
     * Aturan: Hanya tahapan terakhir yang selesai yang boleh dibatalkan.
     */
    fun bolehBatal(stage: ProductionStage, state: ProductionState): RuleCheckResult {
        if (stage !in state.tahapanSelesai) {
            return RuleCheckResult.Ditolak("Tahapan ini belum selesai.")
        }

        // Cek apakah ada tahapan setelahnya yang sudah selesai
        val tahapanSetelahnyaSelesai = ProductionStage.orderedStages
            .filter { it.nomor > stage.nomor }
            .any { it in state.tahapanSelesai }

        if (tahapanSetelahnyaSelesai) {
            return RuleCheckResult.Ditolak("Hanya tahapan terakhir yang selesai yang dapat dibatalkan centangnya.")
        }

        return RuleCheckResult.Lolos
    }

    /**
     * Memeriksa apakah penanda baju tiba boleh diubah.
     */
    fun bolehUbahBajuTiba(target: Boolean, state: ProductionState): RuleCheckResult {
        if (target) return RuleCheckResult.Lolos

        if (ProductionStage.PENGADAAN_BAJU in state.tahapanSelesai) {
            return RuleCheckResult.Ditolak("Tahap Pengadaan Baju sudah selesai, status baju tiba tidak dapat dibatalkan.")
        }
        if (state.siapDisablon) {
            return RuleCheckResult.Ditolak("Kesiapan sablon sudah aktif, status baju tiba tidak dapat dibatalkan.")
        }

        return RuleCheckResult.Lolos
    }

    /**
     * Memeriksa apakah penanda DTF tiba boleh diubah.
     */
    fun bolehUbahDtfTiba(target: Boolean, state: ProductionState): RuleCheckResult {
        if (target) return RuleCheckResult.Lolos

        if (ProductionStage.CETAK_DTF_MATERIAL in state.tahapanSelesai) {
            return RuleCheckResult.Ditolak("Tahap Cetak DTF sudah selesai, status DTF tiba tidak dapat dibatalkan.")
        }
        if (state.siapDisablon) {
            return RuleCheckResult.Ditolak("Kesiapan sablon sudah aktif, status DTF tiba tidak dapat dibatalkan.")
        }

        return RuleCheckResult.Lolos
    }

    /**
     * Memeriksa apakah penanda Siap Disablon boleh diubah.
     */
    fun bolehUbahSiapDisablon(target: Boolean, state: ProductionState): RuleCheckResult {
        if (target) {
            if (!state.bajuTiba || !state.dtfTiba) {
                return RuleCheckResult.Ditolak("Baju polos dan film DTF harus sudah tiba sebelum menandai siap disablon.")
            }
            return RuleCheckResult.Lolos
        }

        if (ProductionStage.PENYABLONAN in state.tahapanSelesai) {
            return RuleCheckResult.Ditolak("Tahap Penyablonan sudah selesai, status siap disablon tidak dapat dibatalkan.")
        }

        return RuleCheckResult.Lolos
    }

    /**
     * Validasi menyeluruh terhadap sebuah [ProductionState].
     * Memastikan konsistensi urutan tahapan selesai dan prasyarat penanda material.
     */
    fun validasiKeadaan(state: ProductionState): RuleCheckResult {
        val selesai = state.tahapanSelesai

        // Urutan tahapan harus merupakan prefix berurutan tanpa jeda
        var maxNomor = 0
        for (stage in ProductionStage.orderedStages) {
            if (stage in selesai) {
                if (stage.nomor != maxNomor + 1) {
                    return RuleCheckResult.Ditolak("Tahapan selesai harus berurutan tanpa jeda.")
                }
                maxNomor = stage.nomor
            }
        }

        if (ProductionStage.PENGADAAN_BAJU in selesai && !state.bajuTiba) {
            return RuleCheckResult.Ditolak("Tahap Pengadaan Baju selesai namun baju belum tiba.")
        }

        if (ProductionStage.CETAK_DTF_MATERIAL in selesai && !state.dtfTiba) {
            return RuleCheckResult.Ditolak("Tahap Cetak DTF selesai namun DTF belum tiba.")
        }

        if (ProductionStage.PENYABLONAN in selesai && !state.siapDisablon) {
            return RuleCheckResult.Ditolak("Tahap Penyablonan selesai namun belum ditandai siap disablon.")
        }

        if (state.siapDisablon && (!state.bajuTiba || !state.dtfTiba)) {
            return RuleCheckResult.Ditolak("Siap disablon aktif namun baju atau DTF belum tiba.")
        }

        return RuleCheckResult.Lolos
    }

    /**
     * Menurunkan status pelanggan berdasarkan tahapan aktif yang sedang berjalan.
     */
    fun turunkanStatus(currentStatus: OrderStatus, state: ProductionState): OrderStatus {
        if (currentStatus == OrderStatus.DIBATALKAN) return OrderStatus.DIBATALKAN
        if (currentStatus == OrderStatus.SELESAI) return OrderStatus.SELESAI
        if (currentStatus == OrderStatus.DITERIMA && state.tahapanSelesai.isEmpty()) {
            return OrderStatus.DITERIMA
        }

        val aktif = tahapanAktif(state) ?: return OrderStatus.SIAP_DIAMBIL

        return when (aktif) {
            ProductionStage.DESAIN_MOCKUP,
            ProductionStage.PENGADAAN_BAJU -> OrderStatus.SEDANG_DIPROSES
            ProductionStage.CETAK_DTF_MATERIAL -> OrderStatus.CETAK_DTF
            ProductionStage.PENYABLONAN,
            ProductionStage.PEMERIKSAAN_QC -> OrderStatus.PRESSING
        }
    }
}
