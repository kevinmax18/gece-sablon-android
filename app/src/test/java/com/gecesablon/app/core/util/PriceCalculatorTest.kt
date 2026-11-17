package com.gecesablon.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pengujian unit untuk fungsi murni [PriceCalculator].
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
}
