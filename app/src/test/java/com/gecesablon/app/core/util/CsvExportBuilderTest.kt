package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class CsvExportBuilderTest {

    private val testTimeZone = TimeZone.getTimeZone("Asia/Jakarta")

    @Test
    fun `buildCsv includes UTF-8 BOM at the start`() {
        val csv = CsvExportBuilder.buildCsv(
            rangeStart = 1700000000000L,
            rangeEnd = 1702592000000L,
            totalIncome = 500000L,
            totalExpense = 200000L,
            netProfit = 300000L,
            categoryTotals = emptyMap(),
            transactions = emptyList(),
            timeZone = testTimeZone
        )

        assertTrue(csv.startsWith("\uFEFF"))
    }

    @Test
    fun `buildCsv formats empty report gracefully`() {
        val csv = CsvExportBuilder.buildCsv(
            rangeStart = 1700000000000L,
            rangeEnd = 1702592000000L,
            totalIncome = 0L,
            totalExpense = 0L,
            netProfit = 0L,
            categoryTotals = emptyMap(),
            transactions = emptyList(),
            timeZone = testTimeZone
        )

        assertTrue(csv.contains("Laporan Keuangan GeCe Screen Printing"))
        assertTrue(csv.contains("Total Pemasukan;0"))
        assertTrue(csv.contains("Total Pengeluaran;0"))
        assertTrue(csv.contains("Laba Bersih;0"))
        assertTrue(csv.contains("Tidak ada transaksi pada periode ini."))
    }

    @Test
    fun `buildCsv includes headers and pure numbers for nominal`() {
        val tx = Transaction(
            id = "tx-1",
            tipe = TransactionType.PEMASUKAN,
            kategori = TransactionCategory.PESANAN,
            nominal = 150000L,
            tanggal = 1700000000000L,
            keterangan = "Pesanan Kaos Sablon DTF",
            dibuatOleh = "Owner"
        )

        val csv = CsvExportBuilder.buildCsv(
            rangeStart = 1700000000000L,
            rangeEnd = 1702592000000L,
            totalIncome = 150000L,
            totalExpense = 0L,
            netProfit = 150000L,
            categoryTotals = mapOf(TransactionCategory.PESANAN to 150000L),
            transactions = listOf(tx),
            timeZone = testTimeZone
        )

        // Verifikasi header
        assertTrue(csv.contains("No;Tanggal;Tipe;Kategori;Keterangan;Nominal (Rp)"))
        // Verifikasi nominal murni tanpa pemisah ribuan
        assertTrue(csv.contains(";150000"))
        assertFalse(csv.contains(";\"150000\""))
        assertFalse(csv.contains("150.000"))
    }

    @Test
    fun `escapeValue handles semicolons quotes and line breaks`() {
        assertEquals("Biasa", CsvExportBuilder.escapeValue("Biasa"))
        assertEquals("\"Toko; Sablon\"", CsvExportBuilder.escapeValue("Toko; Sablon"))
        assertEquals("\"Kaos \"\"Combed\"\"\"", CsvExportBuilder.escapeValue("Kaos \"Combed\""))
        assertEquals("\"Baris1\nBaris2\"", CsvExportBuilder.escapeValue("Baris1\nBaris2"))
    }

    @Test
    fun `sanitizeFormulaInjection prefixes dangerous characters with single quote`() {
        assertEquals("'=SUM(A1:A10)", CsvExportBuilder.sanitizeFormulaInjection("=SUM(A1:A10)"))
        assertEquals("'+628123456", CsvExportBuilder.sanitizeFormulaInjection("+628123456"))
        assertEquals("'-Diskon 10%", CsvExportBuilder.sanitizeFormulaInjection("-Diskon 10%"))
        assertEquals("'@cmd.exe", CsvExportBuilder.sanitizeFormulaInjection("@cmd.exe"))
        assertEquals("'\tTabbed", CsvExportBuilder.sanitizeFormulaInjection("\tTabbed"))
        assertEquals("'\rReturn", CsvExportBuilder.sanitizeFormulaInjection("\rReturn"))

        // Aman
        assertEquals("Pembelian Cat DTF", CsvExportBuilder.sanitizeFormulaInjection("Pembelian Cat DTF"))
        assertEquals("", CsvExportBuilder.sanitizeFormulaInjection(""))
    }

    @Test
    fun `buildCsv prevents formula injection in user remarks`() {
        val dangerousTx = Transaction(
            id = "tx-vuln",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.BAHAN_BAKU,
            nominal = 75000L,
            tanggal = 1700000000000L,
            keterangan = "=2+5; malicious command",
            dibuatOleh = "Owner"
        )

        val csv = CsvExportBuilder.buildCsv(
            rangeStart = 1700000000000L,
            rangeEnd = 1702592000000L,
            totalIncome = 0L,
            totalExpense = 75000L,
            netProfit = -75000L,
            categoryTotals = mapOf(TransactionCategory.BAHAN_BAKU to 75000L),
            transactions = listOf(dangerousTx),
            timeZone = testTimeZone
        )

        // Karena mengandung ';' dan diawali '=', harus dinetralkan menjadi "'=2+5; malicious command" dan di-quote
        assertTrue(csv.contains("\"'=2+5; malicious command\";75000"))
        // Rugi bersih label
        assertTrue(csv.contains("Rugi Bersih;-75000"))
    }
}
