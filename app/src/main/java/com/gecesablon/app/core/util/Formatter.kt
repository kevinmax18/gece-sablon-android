package com.gecesablon.app.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formatter menyediakan fungsi utilitas formatting seperti mata uang Rupiah dan tanggal/waktu.
 */
object Formatter {

    private val rupiahSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        currencySymbol = "Rp"
        groupingSeparator = '.'
        monetaryDecimalSeparator = ','
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
}
