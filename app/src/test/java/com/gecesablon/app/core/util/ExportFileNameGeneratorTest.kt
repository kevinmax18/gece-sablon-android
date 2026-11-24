package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.FinancePeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ExportFileNameGeneratorTest {

    private val testTimeZone = TimeZone.getTimeZone("Asia/Jakarta")

    @Test
    fun `generateFileName for full month returns year and month format`() {
        // November 2026: 2026-11-01 to 2026-11-30
        val cal = Calendar.getInstance(testTimeZone).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 1, 0, 0, 0)
        }
        val start = cal.timeInMillis

        cal.set(2026, Calendar.NOVEMBER, 30, 23, 59, 59)
        val end = cal.timeInMillis

        val pdfName = ExportFileNameGenerator.generateFileName(
            period = FinancePeriod.BULAN_INI,
            startMillis = start,
            endMillis = end,
            extension = "pdf",
            timeZone = testTimeZone
        )
        assertEquals("laporan-keuangan-GeCe-2026-11.pdf", pdfName)

        val csvName = ExportFileNameGenerator.generateFileName(
            period = FinancePeriod.BULAN_LALU,
            startMillis = start,
            endMillis = end,
            extension = ".csv",
            timeZone = testTimeZone
        )
        assertEquals("laporan-keuangan-GeCe-2026-11.csv", csvName)
    }

    @Test
    fun `generateFileName for custom range returns start and end date format`() {
        val cal = Calendar.getInstance(testTimeZone).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 1, 0, 0, 0)
        }
        val start = cal.timeInMillis

        cal.set(2026, Calendar.NOVEMBER, 24, 23, 59, 59)
        val end = cal.timeInMillis

        val pdfName = ExportFileNameGenerator.generateFileName(
            period = FinancePeriod.CUSTOM,
            startMillis = start,
            endMillis = end,
            extension = "pdf",
            timeZone = testTimeZone
        )
        assertEquals("laporan-keuangan-GeCe-2026-11-01_2026-11-24.pdf", pdfName)

        val csvName = ExportFileNameGenerator.generateFileName(
            period = FinancePeriod.HARI_7,
            startMillis = start,
            endMillis = end,
            extension = "csv",
            timeZone = testTimeZone
        )
        assertEquals("laporan-keuangan-GeCe-2026-11-01_2026-11-24.csv", csvName)
    }
}
