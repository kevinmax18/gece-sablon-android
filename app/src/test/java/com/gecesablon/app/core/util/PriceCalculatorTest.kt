package com.gecesablon.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pengujian unit untuk fungsi murni [PriceCalculator].
 * Menguji kombinasi bahan dan ukuran, serta batas jumlah (1-999).
 */
class PriceCalculatorTest {

    @Test
    fun testUkuranStandar_tanpaTambahanHarga() {
        val basePrice = 55_000L
        val sizeExtra = 0L
        val quantity = 1

        val unitPrice = PriceCalculator.calculateUnitPrice(basePrice, sizeExtra)
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, quantity)

        assertEquals(55_000L, unitPrice)
        assertEquals(55_000L, totalPrice)
    }

    @Test
    fun testUkuranXXL_denganTambahanLimaRibu() {
        val basePrice = 55_000L
        val sizeExtra = 5_000L
        val quantity = 1

        val unitPrice = PriceCalculator.calculateUnitPrice(basePrice, sizeExtra)
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, quantity)

        assertEquals(60_000L, unitPrice)
        assertEquals(60_000L, totalPrice)
    }

    @Test
    fun testUkuranXXXL_denganTambahanSepuluhRibu() {
        val basePrice = 55_000L
        val sizeExtra = 10_000L
        val quantity = 1

        val unitPrice = PriceCalculator.calculateUnitPrice(basePrice, sizeExtra)
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, quantity)

        assertEquals(65_000L, unitPrice)
        assertEquals(65_000L, totalPrice)
    }

    @Test
    fun testKombinasiBahanDanUkuran() {
        val basePrice = 65_000L
        val materialExtra = 5_000L // Bahan Combed 20s Heavyweight (+5.000)
        val sizeExtra = 10_000L     // Ukuran XXXL (+10.000)
        val quantity = 10

        val unitPrice = PriceCalculator.calculateUnitPrice(
            basePrice = basePrice,
            sizeExtra = sizeExtra,
            materialExtra = materialExtra
        )
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, quantity)

        assertEquals(80_000L, unitPrice)
        assertEquals(800_000L, totalPrice)
    }

    @Test
    fun testBahanDenganTambahanHarga_ukuranStandar() {
        val basePrice = 70_000L
        val materialExtra = 7_500L
        val sizeExtra = 0L
        val quantity = 2

        val unitPrice = PriceCalculator.calculateUnitPrice(
            basePrice = basePrice,
            sizeExtra = sizeExtra,
            materialExtra = materialExtra
        )
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, quantity)

        assertEquals(77_500L, unitPrice)
        assertEquals(155_000L, totalPrice)
    }

    @Test
    fun testBatasJumlahPemesanan_satuPcs() {
        val basePrice = 60_000L
        val unitPrice = PriceCalculator.calculateUnitPrice(basePrice, 0L, 0L)
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, 1)

        assertEquals(60_000L, totalPrice)
    }

    @Test
    fun testBatasJumlahPemesanan_999Pcs() {
        val basePrice = 60_000L
        val materialExtra = 5_000L
        val sizeExtra = 5_000L
        val unitPrice = PriceCalculator.calculateUnitPrice(basePrice, sizeExtra, materialExtra) // 70.000
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, 999)

        assertEquals(70_000L, unitPrice)
        assertEquals(69_930_000L, totalPrice)
    }

    @Test
    fun testJumlahBanyak_duaLusin() {
        val basePrice = 65_000L
        val sizeExtra = 0L
        val quantity = 24 // 2 lusin

        val unitPrice = PriceCalculator.calculateUnitPrice(basePrice, sizeExtra)
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, quantity)

        assertEquals(65_000L, unitPrice)
        assertEquals(1_560_000L, totalPrice)
    }

    @Test
    fun testJumlahBanyak_denganTambahanUkuranXXL() {
        val basePrice = 60_000L
        val sizeExtra = 5_000L // 65.000 / pcs
        val quantity = 50

        val unitPrice = PriceCalculator.calculateUnitPrice(basePrice, sizeExtra)
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, quantity)

        assertEquals(65_000L, unitPrice)
        assertEquals(3_250_000L, totalPrice)
    }

    @Test
    fun testNilaiNegatifDitolakDanDianggapNol() {
        val unitPrice = PriceCalculator.calculateUnitPrice(
            basePrice = -10_000L,
            sizeExtra = -5_000L,
            materialExtra = -2_000L
        )
        assertEquals(0L, unitPrice)

        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice = 50_000L, quantity = -5)
        assertEquals(0L, totalPrice)
    }
}
