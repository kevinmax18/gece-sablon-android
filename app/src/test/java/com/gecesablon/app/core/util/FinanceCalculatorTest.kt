package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionSource
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.model.toVirtualTransactionOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Unit test untuk [FinanceCalculator] yang memverifikasi kalkulasi keuangan murni.
 */
class FinanceCalculatorTest {

    private val baseRange = DateRange(100_000L, 200_000L)

    @Test
    fun testHitungTotalPemasukan_gabunganManualDanPesananSelesai() {
        val manualTx = listOf(
            Transaction(
                id = "tx-1",
                tipe = TransactionType.PEMASUKAN,
                kategori = TransactionCategory.PENJUALAN_LAIN,
                nominal = 500_000L,
                tanggal = 150_000L,
                keterangan = "Jual sisa kain",
                dibuatOleh = "Owner"
            )
        )

        val orders = listOf(
            Order(
                id = "ord-1",
                kode = "GEC-001",
                namaPelanggan = "Budi",
                status = OrderStatus.SELESAI,
                total = 1_200_000L,
                selesaiPada = 160_000L
            )
        )

        val combined = FinanceCalculator.buildCombinedTransactions(manualTx, orders)
        val filtered = FinanceCalculator.filterTransactionsByPeriod(combined, baseRange)
        val totalIncome = FinanceCalculator.calculateTotalIncome(filtered)

        assertEquals(1_700_000L, totalIncome)
    }

    @Test
    fun testPesananDibatalkanDanStatusLainTidakDihitung() {
        val orders = listOf(
            Order(id = "o1", kode = "GEC-1", namaPelanggan = "A", status = OrderStatus.DIBATALKAN, total = 500_000L, selesaiPada = 150_000L),
            Order(id = "o2", kode = "GEC-2", namaPelanggan = "B", status = OrderStatus.DITERIMA, total = 600_000L, selesaiPada = 150_000L),
            Order(id = "o3", kode = "GEC-3", namaPelanggan = "C", status = OrderStatus.SEDANG_DIPROSES, total = 700_000L, selesaiPada = 150_000L),
            Order(id = "o4", kode = "GEC-4", namaPelanggan = "D", status = OrderStatus.CETAK_DTF, total = 800_000L, selesaiPada = 150_000L),
            Order(id = "o5", kode = "GEC-5", namaPelanggan = "E", status = OrderStatus.PRESSING, total = 900_000L, selesaiPada = 150_000L),
            Order(id = "o6", kode = "GEC-6", namaPelanggan = "F", status = OrderStatus.SIAP_DIAMBIL, total = 1_000_000L, selesaiPada = 150_000L)
        )

        val combined = FinanceCalculator.buildCombinedTransactions(emptyList(), orders)
        val income = FinanceCalculator.calculateTotalIncome(combined)
        assertEquals("Seluruh pesanan yang belum/tidak SELESAI tidak boleh dihitung", 0L, income)
    }

    @Test
    fun testSelesaiTanpaSelesaiPadaTidakDihitung() {
        val orders = listOf(
            Order(
                id = "ord-no-finish",
                kode = "GEC-999",
                namaPelanggan = "Tanpa SelesaiPada",
                status = OrderStatus.SELESAI,
                total = 750_000L,
                selesaiPada = null // Tanpa tanggal selesai
            )
        )

        val virtual = orders[0].toVirtualTransactionOrNull()
        assertNull("Pesanan SELESAI tanpa selesaiPada harus menghasilkan virtual transaction null", virtual)

        val combined = FinanceCalculator.buildCombinedTransactions(emptyList(), orders)
        val income = FinanceCalculator.calculateTotalIncome(combined)
        assertEquals(0L, income)
    }

    @Test
    fun testLainnyaPemasukanDanPengeluaranTidakTercampur() {
        val transactions = listOf(
            Transaction(
                id = "tx-in-other",
                tipe = TransactionType.PEMASUKAN,
                kategori = TransactionCategory.PEMASUKAN_LAINNYA,
                nominal = 300_000L,
                tanggal = 150_000L,
                keterangan = "Bonus vendor",
                dibuatOleh = "Owner"
            ),
            Transaction(
                id = "tx-out-other",
                tipe = TransactionType.PENGELUARAN,
                kategori = TransactionCategory.PENGELUARAN_LAINNYA,
                nominal = 150_000L,
                tanggal = 155_000L,
                keterangan = "Beli snack workshop",
                dibuatOleh = "Owner"
            )
        )

        val byCategory = FinanceCalculator.calculateTotalsByCategory(transactions)
        assertEquals(300_000L, byCategory[TransactionCategory.PEMASUKAN_LAINNYA])
        assertEquals(150_000L, byCategory[TransactionCategory.PENGELUARAN_LAINNYA])

        val totalIncome = FinanceCalculator.calculateTotalIncome(transactions)
        val totalExpense = FinanceCalculator.calculateTotalExpense(transactions)
        assertEquals(300_000L, totalIncome)
        assertEquals(150_000L, totalExpense)
    }

    @Test
    fun testHitungLabaPositifDanNegatif() {
        val labaPositif = FinanceCalculator.calculateNetProfit(2_000_000L, 1_200_000L)
        assertEquals(800_000L, labaPositif)

        val labaNegatif = FinanceCalculator.calculateNetProfit(1_000_000L, 1_500_000L)
        assertEquals(-500_000L, labaNegatif)
    }

    @Test
    fun testHitungTotalPerKategori() {
        val transactions = listOf(
            Transaction(id = "1", tipe = TransactionType.PENGELUARAN, kategori = TransactionCategory.BAHAN_BAKU, nominal = 400_000L, tanggal = 120_000L, keterangan = "Kain", dibuatOleh = "Owner"),
            Transaction(id = "2", tipe = TransactionType.PENGELUARAN, kategori = TransactionCategory.BAHAN_BAKU, nominal = 200_000L, tanggal = 130_000L, keterangan = "Kain tambah", dibuatOleh = "Owner"),
            Transaction(id = "3", tipe = TransactionType.PENGELUARAN, kategori = TransactionCategory.GAJI, nominal = 1_000_000L, tanggal = 140_000L, keterangan = "Gaji staf", dibuatOleh = "Owner")
        )

        val byCategory = FinanceCalculator.calculateTotalsByCategory(transactions)
        assertEquals(600_000L, byCategory[TransactionCategory.BAHAN_BAKU])
        assertEquals(1_000_000L, byCategory[TransactionCategory.GAJI])
        assertNull(byCategory[TransactionCategory.MATERIAL_DTF])
    }

    @Test
    fun testJumlahPesananSelesaiDiRingkasanBenar() {
        val orders = listOf(
            Order(id = "1", kode = "G1", namaPelanggan = "A", status = OrderStatus.SELESAI, total = 100_000L, selesaiPada = 150_000L),
            Order(id = "2", kode = "G2", namaPelanggan = "B", status = OrderStatus.SELESAI, total = 200_000L, selesaiPada = 160_000L),
            Order(id = "3", kode = "G3", namaPelanggan = "C", status = OrderStatus.SELESAI, total = 300_000L, selesaiPada = 250_000L), // di luar range
            Order(id = "4", kode = "G4", namaPelanggan = "D", status = OrderStatus.DIBATALKAN, total = 400_000L, selesaiPada = 170_000L)
        )

        val count = FinanceCalculator.countCompletedOrders(orders, baseRange)
        assertEquals(2, count)
    }

    @Test
    fun testIdVirtualTerbacaDanTerurai() {
        val order = Order(
            id = "seed-99",
            kode = "GEC-20261118-099",
            namaPelanggan = "Ahmad Dani",
            status = OrderStatus.SELESAI,
            total = 850_000L,
            selesaiPada = 150_000L
        )

        val virtual = order.toVirtualTransactionOrNull()
        assertNotNull(virtual)
        assertEquals("order-seed-99", virtual?.id)
        assertEquals(TransactionSource.PESANAN, virtual?.sumber)
        assertEquals(TransactionCategory.PESANAN, virtual?.kategori)
        assertEquals("seed-99", virtual?.orderId)
    }

    @Test
    fun testCategoryRatio_amanDariPembagianNol() {
        val ratioNol = FinanceCalculator.calculateCategoryRatio(0L, 0L)
        assertEquals(0f, ratioNol, 0.001f)

        val ratioBiasa = FinanceCalculator.calculateCategoryRatio(500_000L, 2_000_000L)
        assertEquals(0.25f, ratioBiasa, 0.001f)

        val ratioPenuh = FinanceCalculator.calculateCategoryRatio(1_000_000L, 1_000_000L)
        assertEquals(1.0f, ratioPenuh, 0.001f)
    }

    @Test
    fun testBuildMonthlyTrend_lintasTahunDesemberJanuari() {
        val tz = TimeZone.getTimeZone("Asia/Jakarta")
        // Misalkan waktu acuan adalah 15 Januari 2027
        val cal = Calendar.getInstance(tz).apply {
            clear()
            set(2027, Calendar.JANUARY, 15, 10, 0, 0)
        }
        val now = cal.timeInMillis

        val trend = FinanceCalculator.buildMonthlyTrend(now, tz, emptyList(), emptyList())
        assertEquals(6, trend.monthlyBars.size)

        // Urutan 6 bulan: Agu 2026, Sep 2026, Okt 2026, Nov 2026, Des 2026, Jan 2027
        val expectedMonths = listOf(
            Calendar.AUGUST to 2026,
            Calendar.SEPTEMBER to 2026,
            Calendar.OCTOBER to 2026,
            Calendar.NOVEMBER to 2026,
            Calendar.DECEMBER to 2026,
            Calendar.JANUARY to 2027
        )

        for (i in 0 until 6) {
            val bar = trend.monthlyBars[i]
            assertEquals(expectedMonths[i].first, bar.monthIndex)
            assertEquals(expectedMonths[i].second, bar.year)
        }
        assertTrue(trend.monthlyBars.last().isCurrentMonth)
        assertEquals(false, trend.monthlyBars.first().isCurrentMonth)
    }

    @Test
    fun testBuildMonthlyTrend_februariKabisatDanBiasa() {
        val tz = TimeZone.getTimeZone("Asia/Jakarta")
        // 2024 adalah tahun kabisat (Februari = 29 hari)
        val leapFebRange = FinanceCalculator.getMonthRange(2024, Calendar.FEBRUARY, tz)
        val cal = Calendar.getInstance(tz).apply { timeInMillis = leapFebRange.endMillis }
        assertEquals(29, cal.get(Calendar.DAY_OF_MONTH))

        // 2025 adalah tahun biasa (Februari = 28 hari)
        val nonLeapFebRange = FinanceCalculator.getMonthRange(2025, Calendar.FEBRUARY, tz)
        cal.timeInMillis = nonLeapFebRange.endMillis
        assertEquals(28, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testBuildMonthlyTrend_nilaiTepatDiBatasAwalDanAkhirBulan() {
        val tz = TimeZone.getTimeZone("Asia/Jakarta")
        val febRange = FinanceCalculator.getMonthRange(2026, Calendar.FEBRUARY, tz)

        val txStart = Transaction(
            id = "tx-start",
            tipe = TransactionType.PEMASUKAN,
            kategori = TransactionCategory.PENJUALAN_LAIN,
            nominal = 100_000L,
            tanggal = febRange.startMillis, // Tepat 00:00:00.000
            keterangan = "Awal bulan",
            dibuatOleh = "Owner"
        )
        val txEnd = Transaction(
            id = "tx-end",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.OPERASIONAL,
            nominal = 50_000L,
            tanggal = febRange.endMillis, // Tepat 23:59:59.999
            keterangan = "Akhir bulan",
            dibuatOleh = "Owner"
        )

        val cal = Calendar.getInstance(tz).apply {
            clear()
            set(2026, Calendar.FEBRUARY, 10, 12, 0, 0)
        }
        val now = cal.timeInMillis

        val trend = FinanceCalculator.buildMonthlyTrend(now, tz, listOf(txStart, txEnd), emptyList())
        val febBar = trend.monthlyBars.last()

        assertEquals(100_000L, febBar.totalIncome)
        assertEquals(50_000L, febBar.totalExpense)
        assertEquals(50_000L, febBar.netProfit)
        assertEquals(50.0, febBar.marginPercentage!!, 0.001)
    }

    @Test
    fun testBuildMonthlyTrend_bulanTanpaDataDanPendapatanBulanLaluNol() {
        val tz = TimeZone.getTimeZone("Asia/Jakarta")
        val cal = Calendar.getInstance(tz).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 15, 12, 0, 0)
        }
        val now = cal.timeInMillis

        // Tidak ada transaksi sama sekali
        val trend = FinanceCalculator.buildMonthlyTrend(now, tz, emptyList(), emptyList())
        assertEquals(6, trend.monthlyBars.size)

        for (bar in trend.monthlyBars) {
            assertEquals(0L, bar.totalIncome)
            assertEquals(0L, bar.totalExpense)
            assertEquals(0L, bar.netProfit)
            assertNull("Margin harus null jika pemasukan 0", bar.marginPercentage)
        }

        assertNull("Rata-rata margin harus null jika total pemasukan 6 bulan 0", trend.averageMarginPercentage)
        assertNull("Pertumbuhan pendapatan harus null jika pendapatan bulan lalu 0", trend.incomeGrowthPercentage)
    }

    @Test
    fun testBuildMonthlyTrend_bulanRugiDanMarginRataRataBerbobot() {
        val tz = TimeZone.getTimeZone("Asia/Jakarta")
        // Acuan: Oktober 2026
        val cal = Calendar.getInstance(tz).apply {
            clear()
            set(2026, Calendar.OCTOBER, 20, 12, 0, 0)
        }
        val now = cal.timeInMillis

        val sepRange = FinanceCalculator.getMonthRange(2026, Calendar.SEPTEMBER, tz)
        val oktRange = FinanceCalculator.getMonthRange(2026, Calendar.OCTOBER, tz)

        // September: Pemasukan 1.000.000, Pengeluaran 1.500.000 -> Rugi -500.000 (margin = -50%)
        val txSepIn = Transaction(id = "s1", tipe = TransactionType.PEMASUKAN, kategori = TransactionCategory.PENJUALAN_LAIN, nominal = 1_000_000L, tanggal = sepRange.startMillis + 1000L, keterangan = "Sep", dibuatOleh = "Owner")
        val txSepOut = Transaction(id = "s2", tipe = TransactionType.PENGELUARAN, kategori = TransactionCategory.OPERASIONAL, nominal = 1_500_000L, tanggal = sepRange.startMillis + 2000L, keterangan = "Sep", dibuatOleh = "Owner")

        // Oktober: Pemasukan 3.000.000, Pengeluaran 1.000.000 -> Laba 2.000.000 (margin = 66.67%)
        val txOktIn = Transaction(id = "o1", tipe = TransactionType.PEMASUKAN, kategori = TransactionCategory.PENJUALAN_LAIN, nominal = 3_000_000L, tanggal = oktRange.startMillis + 1000L, keterangan = "Okt", dibuatOleh = "Owner")
        val txOktOut = Transaction(id = "o2", tipe = TransactionType.PENGELUARAN, kategori = TransactionCategory.OPERASIONAL, nominal = 1_000_000L, tanggal = oktRange.startMillis + 2000L, keterangan = "Okt", dibuatOleh = "Owner")

        val trend = FinanceCalculator.buildMonthlyTrend(now, tz, listOf(txSepIn, txSepOut, txOktIn, txOktOut), emptyList())

        val sepBar = trend.monthlyBars[trend.monthlyBars.size - 2]
        val oktBar = trend.monthlyBars.last()

        assertEquals(-500_000L, sepBar.netProfit)
        assertEquals(-50.0, sepBar.marginPercentage!!, 0.001)

        assertEquals(2_000_000L, oktBar.netProfit)
        assertEquals(66.666, oktBar.marginPercentage!!, 0.01)

        // Rata-rata margin berbobot:
        // Total Income = 1.000.000 + 3.000.000 = 4.000.000
        // Total Profit = -500.000 + 2.000.000 = 1.500.000
        // Avg Margin = 1.500.000 / 4.000.000 * 100 = 37.5%
        assertNotNull(trend.averageMarginPercentage)
        assertEquals(37.5, trend.averageMarginPercentage!!, 0.001)

        // Pertumbuhan: dari 1.000.000 ke 3.000.000 = +200%
        assertNotNull(trend.incomeGrowthPercentage)
        assertEquals(200.0, trend.incomeGrowthPercentage!!, 0.001)
    }

    @Test
    fun testExpenseBreakdown_berbagaiKondisi() {
        val range = DateRange(100_000L, 200_000L)

        // 1. Total 0 -> kosong
        val emptyList = FinanceCalculator.expenseBreakdown(emptyList(), range.startMillis, range.endMillis)
        assertTrue(emptyList.isEmpty())

        // 2. Satu kategori saja -> 100%
        val singleTx = listOf(
            Transaction(
                id = "tx1",
                tipe = TransactionType.PENGELUARAN,
                kategori = TransactionCategory.BAHAN_BAKU,
                nominal = 500_000L,
                tanggal = 150_000L,
                keterangan = "Kain",
                dibuatOleh = "Owner"
            )
        )
        val singleBreakdown = FinanceCalculator.expenseBreakdown(singleTx, range.startMillis, range.endMillis)
        assertEquals(1, singleBreakdown.size)
        assertEquals(TransactionCategory.BAHAN_BAKU, singleBreakdown[0].category)
        assertEquals(500_000L, singleBreakdown[0].total)
        assertEquals(100.0, singleBreakdown[0].percentage, 0.001)

        // 3. Multi-kategori: urut terbesar, nominal > 0, jumlah persen ~100%
        val multiTx = listOf(
            Transaction(id = "1", tipe = TransactionType.PENGELUARAN, kategori = TransactionCategory.BAHAN_BAKU, nominal = 500_000L, tanggal = 150_000L, keterangan = "A", dibuatOleh = "Owner"),
            Transaction(id = "2", tipe = TransactionType.PENGELUARAN, kategori = TransactionCategory.MATERIAL_DTF, nominal = 300_000L, tanggal = 150_000L, keterangan = "B", dibuatOleh = "Owner"),
            Transaction(id = "3", tipe = TransactionType.PENGELUARAN, kategori = TransactionCategory.OPERASIONAL, nominal = 200_000L, tanggal = 150_000L, keterangan = "C", dibuatOleh = "Owner"),
            // Transaksi di luar rentang
            Transaction(id = "4", tipe = TransactionType.PENGELUARAN, kategori = TransactionCategory.GAJI, nominal = 1_000_000L, tanggal = 300_000L, keterangan = "D", dibuatOleh = "Owner")
        )
        val multiBreakdown = FinanceCalculator.expenseBreakdown(multiTx, range.startMillis, range.endMillis)
        assertEquals(3, multiBreakdown.size)
        assertEquals(TransactionCategory.BAHAN_BAKU, multiBreakdown[0].category)
        assertEquals(50.0, multiBreakdown[0].percentage, 0.001)
        assertEquals(TransactionCategory.MATERIAL_DTF, multiBreakdown[1].category)
        assertEquals(30.0, multiBreakdown[1].percentage, 0.001)
        assertEquals(TransactionCategory.OPERASIONAL, multiBreakdown[2].category)
        assertEquals(20.0, multiBreakdown[2].percentage, 0.001)

        val totalPercent = multiBreakdown.sumOf { it.percentage }
        assertEquals(100.0, totalPercent, 0.01)
    }

    @Test
    fun testIncomeSources_danFormatPercent() {
        val range = DateRange(100_000L, 200_000L)

        // 1. Total 0 -> null percentages
        val sourcesZero = FinanceCalculator.incomeSources(emptyList(), emptyList(), range.startMillis, range.endMillis)
        assertEquals(0L, sourcesZero.totalIncome)
        assertEquals(0, sourcesZero.completedOrdersCount)
        assertNull(sourcesZero.ordersPercentage)
        assertNull(sourcesZero.manualPercentage)

        // 2. Gabungan pesanan selesai dan manual
        val orders = listOf(
            Order(id = "o1", kode = "G-1", namaPelanggan = "A", status = OrderStatus.SELESAI, total = 800_000L, selesaiPada = 150_000L),
            Order(id = "o2", kode = "G-2", namaPelanggan = "B", status = OrderStatus.SELESAI, total = 200_000L, selesaiPada = 160_000L),
            Order(id = "o3", kode = "G-3", namaPelanggan = "C", status = OrderStatus.SEDANG_DIPROSES, total = 500_000L, selesaiPada = 170_000L)
        )
        val txs = listOf(
            Transaction(id = "t1", tipe = TransactionType.PEMASUKAN, kategori = TransactionCategory.PENJUALAN_LAIN, nominal = 1_000_000L, tanggal = 150_000L, keterangan = "Lain", dibuatOleh = "Owner")
        )
        val sources = FinanceCalculator.incomeSources(txs, orders, range.startMillis, range.endMillis)
        assertEquals(2, sources.completedOrdersCount)
        assertEquals(1_000_000L, sources.totalOrdersIncome)
        assertEquals(1_000_000L, sources.totalManualIncome)
        assertEquals(2_000_000L, sources.totalIncome)
        assertEquals(50.0, sources.ordersPercentage!!, 0.001)
        assertEquals(50.0, sources.manualPercentage!!, 0.001)

        // 3. Formatter.formatPercent (Locale id-ID dengan koma)
        assertEquals("48,8%", Formatter.formatPercent(48.8, 1))
        assertEquals("50,0%", Formatter.formatPercent(50.0, 1))
        assertEquals("100,0%", Formatter.formatPercent(100.0, 1))
        assertEquals("0,0%", Formatter.formatPercent(0.0, 1))
    }
}
