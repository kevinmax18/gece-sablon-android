package com.gecesablon.app.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Formatter menyediakan fungsi utilitas formatting seperti mata uang Rupiah, persentase, dan tanggal/waktu
 * yang mengikuti bahasa terpilih (mata uang tetap Rupiah).
 */
object Formatter {

    private fun symbolsFor(locale: Locale): DecimalFormatSymbols =
        DecimalFormatSymbols(locale).apply {
            currencySymbol = "Rp"
        }

    /**
     * Memformat nominal angka ke dalam bentuk Rupiah tanpa spasi dan desimal.
     * Pemisah ribuan mengikuti [locale] (titik untuk id, koma untuk en), mata uang tetap Rupiah.
     */
    fun formatRupiah(amount: Long, locale: Locale = Locale.getDefault()): String {
        if (amount == 0L) return "Rp0"
        val df = DecimalFormat("Rp#,###", symbolsFor(locale))
        return df.format(amount)
    }

    /**
     * Memformat persentase dengan jumlah desimal yang ditentukan mengikuti [locale].
     * Contoh: formatPercent(48.8, 1, Locale("id", "ID")) -> "48,8%", Locale.ENGLISH -> "48.8%".
     */
    fun formatPercent(
        value: Double,
        decimals: Int = 1,
        locale: Locale = Locale.getDefault()
    ): String {
        val pattern = if (decimals <= 0) "0'%'" else "0." + "0".repeat(decimals) + "'%'"
        val df = DecimalFormat(pattern, symbolsFor(locale))
        return df.format(value).trim()
    }

    /**
     * Memformat waktu epoch timestamp (milidetik) ke dalam representasi tanggal/waktu mengikuti [locale] dan [timeZone].
     * Contoh (id): "3 November 2026, 14.30"
     * Contoh (en): "3 November 2026, 14:30"
     */
    fun formatDateIndo(
        timestamp: Long,
        locale: Locale = Locale.getDefault(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        if (timestamp <= 0L) return "-"
        val lang = locale.language.lowercase(Locale.ROOT)
        val pattern = if (lang == "id" || lang == "in") {
            "d MMMM yyyy, HH.mm"
        } else {
            "d MMMM yyyy, HH:mm"
        }
        val sdf = SimpleDateFormat(pattern, locale).apply {
            this.timeZone = timeZone
        }
        return sdf.format(Date(timestamp))
    }
}
