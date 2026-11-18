package com.gecesablon.app.data.model

/**
 * Model enum 5 nilai berurutan yang merepresentasikan tahapan produksi sablon DTF GeCe.
 *
 * 1. DESAIN_MOCKUP: Desain & Mockup
 * 2. PENGADAAN_BAJU: Pengadaan Baju Polos
 * 3. CETAK_DTF_MATERIAL: Cetak DTF & Material
 * 4. PENYABLONAN: Penyablonan Workshop
 * 5. PEMERIKSAAN_QC: Pemeriksaan QC
 */
enum class ProductionStage(val nomor: Int) {
    DESAIN_MOCKUP(1),
    PENGADAAN_BAJU(2),
    CETAK_DTF_MATERIAL(3),
    PENYABLONAN(4),
    PEMERIKSAAN_QC(5);

    companion object {
        /**
         * Mengembalikan daftar tahapan dalam urutan pengerjaan standar (1 s.d. 5).
         */
        val orderedStages: List<ProductionStage> by lazy {
            entries.sortedBy { it.nomor }
        }
    }
}
