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
     * Memformat waktu epoch timestamp (milidetik) ke dalam representasi format tanggal Indonesia.
     * Contoh: "3 November 2026, 14.30"
     */
    fun formatDateIndo(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        return indoDateFormat.format(Date(timestamp))
    }
}
