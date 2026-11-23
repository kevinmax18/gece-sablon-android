package com.gecesablon.app.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Formatter menyediakan fungsi utilitas formatting seperti mata uang Rupiah dan tanggal/waktu.
 */
object Formatter {

    private val localeId = Locale("id", "ID")

    private val rupiahSymbols = DecimalFormatSymbols(localeId).apply {
        currencySymbol = "Rp"
        groupingSeparator = '.'
        monetaryDecimalSeparator = ','
    }

    private val indoDateFormat = SimpleDateFormat("d MMMM yyyy, HH.mm", localeId).apply {
        timeZone = TimeZone.getTimeZone("Asia/Jakarta")
    }

    /**
     * Memformat nominal angka ke dalam bentuk Rupiah tanpa spasi dan desimal.
     * Contoh: 65000L -> "Rp65.000", 0L -> "Rp0".
     */
    fun formatRupiah(amount: Long): String {
        if (amount == 0L) return "Rp0"
        val df = DecimalFormat("Rp#,###", rupiahSymbols)
        return df.format(amount)
    }

    /**
     * Memformat persentase dengan jumlah desimal yang ditentukan (Locale id-ID).
     * Contoh: formatPercent(48.8, 1) -> "48,8%".
     */
    fun formatPercent(value: Double, decimals: Int = 1): String {
        val pattern = if (decimals <= 0) "0'%' " else "0." + "0".repeat(decimals) + "'%'"
        val df = DecimalFormat(pattern, rupiahSymbols)
        return df.format(value).trim()
    }

    /**
     * Memformat waktu epoch timestamp (milidetik) ke dalam representasi format tanggal Indonesia.
     * Contoh: "3 November 2026, 14.30"
     */
    fun formatDateIndo(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        return indoDateFormat.format(Date(timestamp))
    }
}
