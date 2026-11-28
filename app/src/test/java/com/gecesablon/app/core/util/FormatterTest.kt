package com.gecesablon.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Unit test murni untuk [Formatter] pada berbagai Locale dan TimeZone deterministik.
 */
class FormatterTest {

    private val localeId = Locale("id", "ID")
    private val localeEn = Locale.US
    private val testZone = TimeZone.getTimeZone("GMT+07:00")

    @Test
    fun `formatRupiah tetap memakai Rupiah dengan pemisah ribuan sesuai bahasa`() {
        assertEquals("Rp0", Formatter.formatRupiah(0L, localeId))
        assertEquals("Rp0", Formatter.formatRupiah(0L, localeEn))

        assertEquals("Rp65.000", Formatter.formatRupiah(65000L, localeId))
        assertEquals("Rp65,000", Formatter.formatRupiah(65000L, localeEn))

        assertEquals("Rp1.250.000", Formatter.formatRupiah(1250000L, localeId))
        assertEquals("Rp1,250,000", Formatter.formatRupiah(1250000L, localeEn))
    }

    @Test
    fun `formatPercent memakai pemisah desimal sesuai bahasa`() {
        assertEquals("48,8%", Formatter.formatPercent(48.8, 1, localeId))
        assertEquals("48.8%", Formatter.formatPercent(48.8, 1, localeEn))
    }

    @Test
    fun `formatDateIndo memformat tanggal sesuai bahasa dan zona waktu`() {
        val cal = Calendar.getInstance(testZone).apply {
            set(2026, Calendar.NOVEMBER, 3, 14, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val ts = cal.timeInMillis

        assertEquals("3 November 2026, 14.30", Formatter.formatDateIndo(ts, localeId, testZone))
        assertEquals("3 November 2026, 14:30", Formatter.formatDateIndo(ts, localeEn, testZone))
        assertEquals("-", Formatter.formatDateIndo(0L, localeId, testZone))
    }
}
