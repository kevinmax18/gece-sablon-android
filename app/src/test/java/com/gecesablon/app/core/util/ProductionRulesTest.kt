package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.ProductionStage
import com.gecesablon.app.data.model.ProductionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk [ProductionRules] yang memverifikasi aturan bisnis produksi sablon DTF.
 */
class ProductionRulesTest {

    @Test
    fun testUrutanTahapanHarusBerurutan() {
        val stateAwal = ProductionState()

        // Tahap 1 boleh dicentang
        val izinTahap1 = ProductionRules.bolehCentang(ProductionStage.DESAIN_MOCKUP, stateAwal)
        assertTrue("Tahap 1 Desain & Mockup harus boleh dicentang awal", izinTahap1.diizinkan)

        // Tahap 2, 3, 4, 5 TIDAK boleh dicentang langsung
        val izinTahap2 = ProductionRules.bolehCentang(ProductionStage.PENGADAAN_BAJU, stateAwal)
        assertFalse("Tahap 2 tidak boleh dicentang jika tahap 1 belum selesai", izinTahap2.diizinkan)

        val izinTahap3 = ProductionRules.bolehCentang(ProductionStage.CETAK_DTF_MATERIAL, stateAwal)
        assertFalse("Tahap 3 tidak boleh dicentang jika tahap 1 & 2 belum selesai", izinTahap3.diizinkan)

        // Validasi state yang melompat harus ditolak
        val stateLompat = ProductionState(
            tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP, ProductionStage.CETAK_DTF_MATERIAL),
            bajuTiba = true,
            dtfTiba = true
        )
        assertFalse("State dengan tahapan melompat harus ditolak validasiKeadaan",
            ProductionRules.validasiKeadaan(stateLompat).diizinkan)
    }

    @Test
    fun testPrasyaratBajuTibaDanDtfTiba() {
        val stateTahap1Selesai = ProductionState(
            tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP),
            bajuTiba = false
        )

        // Tahap 2 butuh bajuTiba == true
        val centangTahap2TanpaBaju = ProductionRules.bolehCentang(ProductionStage.PENGADAAN_BAJU, stateTahap1Selesai)
        assertFalse("Tahap Pengadaan Baju harus ditolak jika baju belum tiba", centangTahap2TanpaBaju.diizinkan)

        val stateBajuTiba = stateTahap1Selesai.copy(bajuTiba = true)
        val centangTahap2DenganBaju = ProductionRules.bolehCentang(ProductionStage.PENGADAAN_BAJU, stateBajuTiba)
        assertTrue("Tahap Pengadaan Baju diizinkan jika baju tiba", centangTahap2DenganBaju.diizinkan)

        // Tahap 3 butuh dtfTiba == true
        val stateTahap2Selesai = stateBajuTiba.copy(
            tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP, ProductionStage.PENGADAAN_BAJU),
            dtfTiba = false
        )
        val centangTahap3TanpaDtf = ProductionRules.bolehCentang(ProductionStage.CETAK_DTF_MATERIAL, stateTahap2Selesai)
        assertFalse("Tahap Cetak DTF harus ditolak jika DTF belum tiba", centangTahap3TanpaDtf.diizinkan)

        val stateDtfTiba = stateTahap2Selesai.copy(dtfTiba = true)
        val centangTahap3DenganDtf = ProductionRules.bolehCentang(ProductionStage.CETAK_DTF_MATERIAL, stateDtfTiba)
        assertTrue("Tahap Cetak DTF diizinkan jika DTF tiba", centangTahap3DenganDtf.diizinkan)
    }

    @Test
    fun testPrasyaratSiapDisablon() {
        // Mengaktifkan siapDisablon butuh bajuTiba dan dtfTiba
        val stateTanpaMaterial = ProductionState(bajuTiba = false, dtfTiba = false)
        assertFalse("Siap disablon ditolak jika material belum tiba",
            ProductionRules.bolehUbahSiapDisablon(true, stateTanpaMaterial).diizinkan)

        val stateBajuSaja = ProductionState(bajuTiba = true, dtfTiba = false)
        assertFalse("Siap disablon ditolak jika DTF belum tiba",
            ProductionRules.bolehUbahSiapDisablon(true, stateBajuSaja).diizinkan)

        val stateLengkap = ProductionState(bajuTiba = true, dtfTiba = true)
        assertTrue("Siap disablon diizinkan jika baju dan DTF tiba",
            ProductionRules.bolehUbahSiapDisablon(true, stateLengkap).diizinkan)

        // Tahap 4 Penyablonan butuh siapDisablon == true
        val stateTahap3Selesai = ProductionState(
            tahapanSelesai = setOf(
                ProductionStage.DESAIN_MOCKUP,
                ProductionStage.PENGADAAN_BAJU,
                ProductionStage.CETAK_DTF_MATERIAL
            ),
            bajuTiba = true,
            dtfTiba = true,
            siapDisablon = false
        )
        val centangPenyablonanTanpaSiap = ProductionRules.bolehCentang(ProductionStage.PENYABLONAN, stateTahap3Selesai)
        assertFalse("Penyablonan ditolak jika siap disablon false", centangPenyablonanTanpaSiap.diizinkan)

        val stateSiapSablon = stateTahap3Selesai.copy(siapDisablon = true)
        val centangPenyablonanSiap = ProductionRules.bolehCentang(ProductionStage.PENYABLONAN, stateSiapSablon)
        assertTrue("Penyablonan diizinkan jika siap disablon true", centangPenyablonanSiap.diizinkan)
    }

    @Test
    fun testBatalHanyaTahapanTerakhir() {
        val stateTigaTahap = ProductionState(
            tahapanSelesai = setOf(
                ProductionStage.DESAIN_MOCKUP,
                ProductionStage.PENGADAAN_BAJU,
                ProductionStage.CETAK_DTF_MATERIAL
            ),
            bajuTiba = true,
            dtfTiba = true
        )

        // Tahap 1 & 2 tidak boleh dibatalkan karena tahap 3 masih selesai
        assertFalse("Tahap 1 tidak boleh dibatalkan sebelum tahap 3",
            ProductionRules.bolehBatal(ProductionStage.DESAIN_MOCKUP, stateTigaTahap).diizinkan)
        assertFalse("Tahap 2 tidak boleh dibatalkan sebelum tahap 3",
            ProductionRules.bolehBatal(ProductionStage.PENGADAAN_BAJU, stateTigaTahap).diizinkan)

        // Tahap 3 (terakhir) boleh dibatalkan
        assertTrue("Tahap 3 (terakhir) boleh dibatalkan",
            ProductionRules.bolehBatal(ProductionStage.CETAK_DTF_MATERIAL, stateTigaTahap).diizinkan)
    }

    @Test
    fun testPenandaMaterialTidakBisaDibatalkanBilaTahapanBergantungSelesai() {
        val statePengadaanSelesai = ProductionState(
            tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP, ProductionStage.PENGADAAN_BAJU),
            bajuTiba = true,
            dtfTiba = false,
            siapDisablon = false
        )
        assertFalse("Baju tiba tidak bisa dimatikan jika Pengadaan Baju sudah selesai",
            ProductionRules.bolehUbahBajuTiba(false, statePengadaanSelesai).diizinkan)

        val stateDtfSelesai = statePengadaanSelesai.copy(
            tahapanSelesai = setOf(
                ProductionStage.DESAIN_MOCKUP,
                ProductionStage.PENGADAAN_BAJU,
                ProductionStage.CETAK_DTF_MATERIAL
            ),
            dtfTiba = true
        )
        assertFalse("DTF tiba tidak bisa dimatikan jika Cetak DTF sudah selesai",
            ProductionRules.bolehUbahDtfTiba(false, stateDtfSelesai).diizinkan)

        val statePenyablonanSelesai = stateDtfSelesai.copy(
            tahapanSelesai = setOf(
                ProductionStage.DESAIN_MOCKUP,
                ProductionStage.PENGADAAN_BAJU,
                ProductionStage.CETAK_DTF_MATERIAL,
                ProductionStage.PENYABLONAN
            ),
            siapDisablon = true
        )
        assertFalse("Siap disablon tidak bisa dimatikan jika Penyablonan sudah selesai",
            ProductionRules.bolehUbahSiapDisablon(false, statePenyablonanSelesai).diizinkan)
    }

    @Test
    fun testStatusTiapTahapanAktif() {
        // Desain aktif -> SEDANG_DIPROSES
        val state1 = ProductionState(tahapanSelesai = emptySet())
        assertEquals(OrderStatus.SEDANG_DIPROSES,
            ProductionRules.turunkanStatus(OrderStatus.SEDANG_DIPROSES, state1))

        // Pengadaan aktif -> SEDANG_DIPROSES
        val state2 = ProductionState(tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP))
        assertEquals(OrderStatus.SEDANG_DIPROSES,
            ProductionRules.turunkanStatus(OrderStatus.SEDANG_DIPROSES, state2))

        // Cetak DTF aktif -> CETAK_DTF
        val state3 = ProductionState(
            tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP, ProductionStage.PENGADAAN_BAJU),
            bajuTiba = true
        )
        assertEquals(OrderStatus.CETAK_DTF,
            ProductionRules.turunkanStatus(OrderStatus.SEDANG_DIPROSES, state3))

        // Penyablonan aktif -> PRESSING
        val state4 = ProductionState(
            tahapanSelesai = setOf(
                ProductionStage.DESAIN_MOCKUP,
                ProductionStage.PENGADAAN_BAJU,
                ProductionStage.CETAK_DTF_MATERIAL
            ),
            bajuTiba = true,
            dtfTiba = true
        )
        assertEquals(OrderStatus.PRESSING,
            ProductionRules.turunkanStatus(OrderStatus.CETAK_DTF, state4))

        // QC aktif -> PRESSING
        val state5 = state4.copy(
            tahapanSelesai = setOf(
                ProductionStage.DESAIN_MOCKUP,
                ProductionStage.PENGADAAN_BAJU,
                ProductionStage.CETAK_DTF_MATERIAL,
                ProductionStage.PENYABLONAN
            ),
            siapDisablon = true
        )
        assertEquals(OrderStatus.PRESSING,
            ProductionRules.turunkanStatus(OrderStatus.PRESSING, state5))
    }

    @Test
    fun testSemuaSelesaiMenjadiSiapDiambil() {
        val stateSemuaSelesai = ProductionState(
            tahapanSelesai = ProductionStage.orderedStages.toSet(),
            bajuTiba = true,
            dtfTiba = true,
            siapDisablon = true
        )
        assertTrue("State semua tahapan selesai harus valid",
            ProductionRules.validasiKeadaan(stateSemuaSelesai).diizinkan)
        assertEquals(OrderStatus.SIAP_DIAMBIL,
            ProductionRules.turunkanStatus(OrderStatus.PRESSING, stateSemuaSelesai))
    }

    @Test
    fun testBatalCentangQcSaatSiapDiambilMenjadiPressing() {
        // Keadaan SIAP_DIAMBIL (5 tahapan selesai)
        val stateSiapDiambil = ProductionState(
            tahapanSelesai = ProductionStage.orderedStages.toSet(),
            bajuTiba = true,
            dtfTiba = true,
            siapDisablon = true
        )
        assertEquals(OrderStatus.SIAP_DIAMBIL,
            ProductionRules.turunkanStatus(OrderStatus.SIAP_DIAMBIL, stateSiapDiambil))

        // QC adalah tahapan terakhir, boleh dibatalkan
        val izinBatalQc = ProductionRules.bolehBatal(ProductionStage.PEMERIKSAAN_QC, stateSiapDiambil)
        assertTrue("QC boleh dibatalkan saat SIAP_DIAMBIL", izinBatalQc.diizinkan)

        // Setelah QC dibatalkan centangnya, tahapan selesai menjadi 4
        val stateSetelahBatalQc = stateSiapDiambil.copy(
            tahapanSelesai = setOf(
                ProductionStage.DESAIN_MOCKUP,
                ProductionStage.PENGADAAN_BAJU,
                ProductionStage.CETAK_DTF_MATERIAL,
                ProductionStage.PENYABLONAN
            )
        )
        // Status pelanggan diturunkan kembali menjadi PRESSING (karena tahapan aktif adalah QC)
        val statusBaru = ProductionRules.turunkanStatus(OrderStatus.SIAP_DIAMBIL, stateSetelahBatalQc)
        assertEquals(OrderStatus.PRESSING, statusBaru)
    }

    @Test
    fun testPesananDiterimaTidakBolehDiubahTahapannya() {
        val izinUbahDiterima = ProductionRules.bolehUbahProduksi(OrderStatus.DITERIMA)
        assertFalse("Pesanan berstatus DITERIMA tidak boleh diubah tahapannya sebelum diterima",
            izinUbahDiterima.diizinkan)

        val izinUbahProses = ProductionRules.bolehUbahProduksi(OrderStatus.SEDANG_DIPROSES)
        assertTrue("Pesanan berstatus SEDANG_DIPROSES boleh diubah tahapannya",
            izinUbahProses.diizinkan)

        val izinUbahBatal = ProductionRules.bolehUbahProduksi(OrderStatus.DIBATALKAN)
        assertFalse("Pesanan berstatus DIBATALKAN tidak boleh diubah",
            izinUbahBatal.diizinkan)

        val izinUbahSelesai = ProductionRules.bolehUbahProduksi(OrderStatus.SELESAI)
        assertFalse("Pesanan berstatus SELESAI tidak boleh diubah",
            izinUbahSelesai.diizinkan)
    }

    @Test
    fun testSemuaSeedLolosValidasiKeadaanDanKonsisten() {
        // Seed 001 (Budi - user-cust-001): SEDANG_DIPROSES, tahap 1 selesai, bajuTiba=true
        val seed001State = ProductionState(
            tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP),
            bajuTiba = true,
            dtfTiba = false,
            siapDisablon = false
        )
        assertTrue("Seed 001 valid", ProductionRules.validasiKeadaan(seed001State).diizinkan)
        assertEquals(OrderStatus.SEDANG_DIPROSES,
            ProductionRules.turunkanStatus(OrderStatus.SEDANG_DIPROSES, seed001State))

        // Seed 002 (Budi - user-cust-001): SIAP_DIAMBIL, 5 tahap selesai
        val seed002State = ProductionState(
            tahapanSelesai = ProductionStage.orderedStages.toSet(),
            bajuTiba = true,
            dtfTiba = true,
            siapDisablon = true
        )
        assertTrue("Seed 002 valid", ProductionRules.validasiKeadaan(seed002State).diizinkan)
        assertEquals(OrderStatus.SIAP_DIAMBIL,
            ProductionRules.turunkanStatus(OrderStatus.PRESSING, seed002State))

        // Seed 003 (Budi - user-cust-001): SELESAI
        assertTrue("Seed 003 valid", ProductionRules.validasiKeadaan(seed002State).diizinkan)
        assertEquals(OrderStatus.SELESAI,
            ProductionRules.turunkanStatus(OrderStatus.SELESAI, seed002State))

        // Seed 004 (Siti - user-cust-002): DITERIMA
        val seed004State = ProductionState()
        assertTrue("Seed 004 valid", ProductionRules.validasiKeadaan(seed004State).diizinkan)
        assertEquals(OrderStatus.DITERIMA,
            ProductionRules.turunkanStatus(OrderStatus.DITERIMA, seed004State))

        // Seed 005 (Komunitas - user-cust-003): SEDANG_DIPROSES, hanya DESAIN_MOCKUP selesai, bajuTiba=false
        val seed005State = ProductionState(
            tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP),
            bajuTiba = false,
            dtfTiba = false,
            siapDisablon = false
        )
        assertTrue("Seed 005 valid", ProductionRules.validasiKeadaan(seed005State).diizinkan)
        assertEquals(OrderStatus.SEDANG_DIPROSES,
            ProductionRules.turunkanStatus(OrderStatus.SEDANG_DIPROSES, seed005State))

        // Seed 006 (Komunitas - user-cust-003): CETAK_DTF, tahap 1 & 2 selesai, bajuTiba=true
        val seed006State = ProductionState(
            tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP, ProductionStage.PENGADAAN_BAJU),
            bajuTiba = true,
            dtfTiba = false,
            siapDisablon = false
        )
        assertTrue("Seed 006 valid", ProductionRules.validasiKeadaan(seed006State).diizinkan)
        assertEquals(OrderStatus.CETAK_DTF,
            ProductionRules.turunkanStatus(OrderStatus.SEDANG_DIPROSES, seed006State))

        // Seed 007 (Arya - user-cust-004): PRESSING, tahap 1, 2, 3 selesai, bajuTiba=true, dtfTiba=true, siapDisablon=false
        val seed007State = ProductionState(
            tahapanSelesai = setOf(
                ProductionStage.DESAIN_MOCKUP,
                ProductionStage.PENGADAAN_BAJU,
                ProductionStage.CETAK_DTF_MATERIAL
            ),
            bajuTiba = true,
            dtfTiba = true,
            siapDisablon = false
        )
        assertTrue("Seed 007 valid", ProductionRules.validasiKeadaan(seed007State).diizinkan)
        assertEquals(OrderStatus.PRESSING,
            ProductionRules.turunkanStatus(OrderStatus.CETAK_DTF, seed007State))

        // Seed 008 (Dewi - user-cust-005): PRESSING, tahap 1, 2, 3, 4 selesai, bajuTiba=true, dtfTiba=true, siapDisablon=true
        val seed008State = ProductionState(
            tahapanSelesai = setOf(
                ProductionStage.DESAIN_MOCKUP,
                ProductionStage.PENGADAAN_BAJU,
                ProductionStage.CETAK_DTF_MATERIAL,
                ProductionStage.PENYABLONAN
            ),
            bajuTiba = true,
            dtfTiba = true,
            siapDisablon = true
        )
        assertTrue("Seed 008 valid", ProductionRules.validasiKeadaan(seed008State).diizinkan)
        assertEquals(OrderStatus.PRESSING,
            ProductionRules.turunkanStatus(OrderStatus.PRESSING, seed008State))

        // Seed 009 (Dewi - user-cust-005): DIBATALKAN
        val seed009State = ProductionState()
        assertTrue("Seed 009 valid", ProductionRules.validasiKeadaan(seed009State).diizinkan)
        assertEquals(OrderStatus.DIBATALKAN,
            ProductionRules.turunkanStatus(OrderStatus.DIBATALKAN, seed009State))
    }
}
