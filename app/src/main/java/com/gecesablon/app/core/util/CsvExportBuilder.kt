package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.displayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utilitas fungsi murni pembuat isi file CSV untuk ekspor Laporan Keuangan GeCe Sablon.
 *
 * Spesifikasi Tugas 6C:
 * - File CSV dengan pemisah titik koma (;)
 * - Penanda UTF-8 (BOM: \uFEFF)
 * - Nilai di-escape benar (tanda kutip ganda dan titik koma)
 * - Nominal sebagai angka tanpa pemisah ribuan agar bisa dihitung di Excel
 * - Formula injection prevention: kolom teks isian pengguna yang diawali =, +, -, @, \t, atau \r
 *   dinetralkan dengan awalan tanda kutip tunggal (') sebelum di-escape. Nominal tetap angka murni.
 * - Tanggal berformat id-ID (misal: "d MMMM yyyy, HH.mm" atau "d MMMM yyyy")
 * - Menggunakan java.util.Calendar/SimpleDateFormat (Locale id-ID), bukan java.time.
 */
object CsvExportBuilder {

    private val localeId = Locale("id", "ID")

    /**
     * Karakter berbahaya untuk formula injection pada spreadsheet.
     */
    private val FORMULA_PREFIXES = charArrayOf('=', '+', '-', '@', '\t', '\r')

    /**
     * Membangun konten string CSV lengkap (termasuk BOM).
     *
     * @param rangeStart Waktu mulai periode (epoch ms)
     * @param rangeEnd Waktu akhir periode (epoch ms)
     * @param totalIncome Total nominal pemasukan
     * @param totalExpense Total nominal pengeluaran
     * @param netProfit Laba/rugi bersih (pemasukan - pengeluaran)
     * @param categoryTotals Peta total per kategori
     * @param transactions Daftar transaksi pada periode (sudah diurutkan)
     * @param timeZone Zona waktu perangkat/kalender
     */
    fun buildCsv(
        rangeStart: Long,
        rangeEnd: Long,
        totalIncome: Long,
        totalExpense: Long,
        netProfit: Long,
        categoryTotals: Map<TransactionCategory, Long>,
        transactions: List<Transaction>,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val dateFormat = SimpleDateFormat("d MMMM yyyy, HH.mm", localeId).apply {
            this.timeZone = timeZone
        }
        val dateOnlyFormat = SimpleDateFormat("d MMMM yyyy", localeId).apply {
            this.timeZone = timeZone
        }

        val sb = StringBuilder()
        // Penanda UTF-8 (Byte Order Mark)
        sb.append('\uFEFF')

        // 1. Header Dokumen
        sb.append(escapeValue("Laporan Keuangan GeCe Screen Printing")).append("\r\n")
        val periodText = "${dateOnlyFormat.format(Date(rangeStart))} - ${dateOnlyFormat.format(Date(rangeEnd))}"
        sb.append(escapeValue("Periode")).append(";").append(escapeValue(periodText)).append("\r\n")
        val generatedAtText = dateFormat.format(Date(System.currentTimeMillis()))
        sb.append(escapeValue("Waktu Unduh")).append(";").append(escapeValue(generatedAtText)).append("\r\n")
        sb.append("\r\n")

        // 2. Ringkasan Keuangan
        sb.append(escapeValue("Ringkasan Keuangan")).append("\r\n")
        sb.append(escapeValue("Indikator")).append(";").append(escapeValue("Nominal (Rp)")).append("\r\n")
        sb.append(escapeValue("Total Pemasukan")).append(";").append(totalIncome).append("\r\n")
        sb.append(escapeValue("Total Pengeluaran")).append(";").append(totalExpense).append("\r\n")
        val profitLabel = if (netProfit >= 0L) "Laba Bersih" else "Rugi Bersih"
        sb.append(escapeValue(profitLabel)).append(";").append(netProfit).append("\r\n")
        sb.append("\r\n")

        // 3. Ringkasan per Kategori
        sb.append(escapeValue("Rincian per Kategori")).append("\r\n")
        sb.append(escapeValue("Kategori")).append(";").append(escapeValue("Total (Rp)")).append("\r\n")
        val allCategories = TransactionCategory.values()
        for (cat in allCategories) {
            val catTotal = categoryTotals[cat] ?: 0L
            if (catTotal > 0L) {
                sb.append(escapeValue(cat.displayName())).append(";").append(catTotal).append("\r\n")
            }
        }
        sb.append("\r\n")

        // 4. Daftar Transaksi
        sb.append(escapeValue("Daftar Transaksi")).append("\r\n")
        val txHeaders = listOf("No", "Tanggal", "Tipe", "Kategori", "Keterangan", "Nominal (Rp)")
        sb.append(txHeaders.joinToString(";") { escapeValue(it) }).append("\r\n")

        if (transactions.isEmpty()) {
            sb.append(escapeValue("Tidak ada transaksi pada periode ini.")).append("\r\n")
        } else {
            transactions.forEachIndexed { index, tx ->
                val no = (index + 1).toString()
                val tgl = dateFormat.format(Date(tx.tanggal))
                val tipe = tx.tipe.name
                val kat = tx.kategori.displayName()
                val sanitizedKet = sanitizeFormulaInjection(tx.keterangan)
                val nominal = tx.nominal.toString()

                val row = listOf(
                    escapeValue(no),
                    escapeValue(tgl),
                    escapeValue(tipe),
                    escapeValue(kat),
                    escapeValue(sanitizedKet),
                    nominal // nominal tetap angka murni tanpa kutip/pemisah ribuan
                )
                sb.append(row.joinToString(";")).append("\r\n")
            }
        }

        return sb.toString()
    }

    /**
     * Mencegah formula injection (CSV/Excel Formula Injection).
     * Jika teks isian diawali karakter =, +, -, @, \t, atau \r,
     * netralkan dengan awalan tanda kutip tunggal (').
     */
    fun sanitizeFormulaInjection(value: String): String {
        if (value.isEmpty()) return value
        val firstChar = value[0]
        return if (FORMULA_PREFIXES.contains(firstChar)) {
            "'$value"
        } else {
            value
        }
    }

    /**
     * Meng-escape nilai CSV:
     * - Jika mengandung pemisah (;), tanda kutip ganda ("), baris baru (\r atau \n),
     *   maka nilai diapit tanda kutip ganda.
     * - Tanda kutip ganda di dalam nilai di-escape menjadi dua tanda kutip ganda ("").
     */
    fun escapeValue(value: String): String {
        val needsQuotes = value.contains(';') ||
                value.contains('"') ||
                value.contains('\n') ||
                value.contains('\r')

        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
