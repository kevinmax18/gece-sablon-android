package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.FinancePeriod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utilitas pembuat nama file ekspor laporan keuangan GeCe Sablon.
 *
 * Aturan (Tugas 6C & AGENTS.md):
 * - Memakai Calendar/SimpleDateFormat (Locale id-ID), bukan java.time.
 * - Satu bulan penuh (misal FinancePeriod.BULAN_INI, BULAN_LALU, atau custom yang persis 1 bulan):
 *   memakai format: laporan-keuangan-GeCe-YYYY-MM (contoh: laporan-keuangan-GeCe-2026-11)
 * - Rentang lain (7 Hari Terakhir, 30 Hari Terakhir, atau Custom):
 *   memakai format: laporan-keuangan-GeCe-YYYY-MM-DD_YYYY-MM-DD (contoh: laporan-keuangan-GeCe-2026-11-01_2026-11-24)
 */
object ExportFileNameGenerator {

    private val localeId = Locale("id", "ID")

    fun generateFileName(
        period: FinancePeriod,
        startMillis: Long,
        endMillis: Long,
        extension: String,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val ext = if (extension.startsWith(".")) extension else ".$extension"

        val calStart = Calendar.getInstance(timeZone).apply { timeInMillis = startMillis }
        val calEnd = Calendar.getInstance(timeZone).apply { timeInMillis = endMillis }

        val isSingleFullMonth = (period == FinancePeriod.BULAN_INI || period == FinancePeriod.BULAN_LALU) ||
                (calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) &&
                        calStart.get(Calendar.MONTH) == calEnd.get(Calendar.MONTH) &&
                        calStart.get(Calendar.DAY_OF_MONTH) == 1 &&
                        calEnd.get(Calendar.DAY_OF_MONTH) == calEnd.getActualMaximum(Calendar.DAY_OF_MONTH))

        val baseName = if (isSingleFullMonth) {
            val monthFormat = SimpleDateFormat("yyyy-MM", localeId).apply {
                this.timeZone = timeZone
            }
            "laporan-keuangan-GeCe-${monthFormat.format(Date(startMillis))}"
        } else {
            val dayFormat = SimpleDateFormat("yyyy-MM-dd", localeId).apply {
                this.timeZone = timeZone
            }
            "laporan-keuangan-GeCe-${dayFormat.format(Date(startMillis))}_${dayFormat.format(Date(endMillis))}"
        }

        return "$baseName$ext"
    }
}
