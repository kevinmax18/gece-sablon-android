package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.MonthlyFinanceBar
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.SixMonthTrend
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.model.toVirtualTransactionOrNull
import java.util.Calendar
import java.util.TimeZone

/**
 * Utilitas fungsi murni kalkulasi keuangan GeCe Sablon.
 * Memisahkan logika bisnis dari UI dan Repository, mudah diuji secara deterministik.
 */
object FinanceCalculator {

    /**
     * Membentuk daftar gabungan dari transaksi manual dan pesanan selesai.
     * Menggunakan fungsi tunggal [toVirtualTransactionOrNull].
     * Pesanan selain SELESAI atau pesanan SELESAI tanpa `selesaiPada` otomatis diabaikan.
     * Hasil diurutkan berdasarkan tanggal secara menurun (terbaru di atas).
     */
    fun buildCombinedTransactions(
        manualTransactions: List<Transaction>,
        orders: List<Order>
    ): List<Transaction> {
        val virtualTransactions = orders.mapNotNull { it.toVirtualTransactionOrNull() }
        return (manualTransactions + virtualTransactions).sortedByDescending { it.tanggal }
    }

    /**
     * Menyaring daftar transaksi yang jatuh di dalam rentang waktu [periodRange] (inklusif).
     */
    fun filterTransactionsByPeriod(
        transactions: List<Transaction>,
        periodRange: DateRange
    ): List<Transaction> {
        return transactions.filter { periodRange.contains(it.tanggal) }
    }

    /**
     * Menghitung total nominal seluruh transaksi bertipe [TransactionType.PEMASUKAN].
     */
    fun calculateTotalIncome(transactions: List<Transaction>): Long {
        return transactions
            .filter { it.tipe == TransactionType.PEMASUKAN }
            .sumOf { it.nominal }
    }

    /**
     * Menghitung total nominal seluruh transaksi bertipe [TransactionType.PENGELUARAN].
     */
    fun calculateTotalExpense(transactions: List<Transaction>): Long {
        return transactions
            .filter { it.tipe == TransactionType.PENGELUARAN }
            .sumOf { it.nominal }
    }

    /**
     * Menghitung laba/rugi bersih (Surplus/Defisit).
     * Laba/rugi = total pemasukan - total pengeluaran.
     * Nilai negatif merepresentasikan rugi.
     */
    fun calculateNetProfit(totalIncome: Long, totalExpense: Long): Long {
        return totalIncome - totalExpense
    }

    /**
     * Menghitung akumulasi total nominal per kategori transaksi.
     */
    fun calculateTotalsByCategory(transactions: List<Transaction>): Map<TransactionCategory, Long> {
        val result = mutableMapOf<TransactionCategory, Long>()
        for (tx in transactions) {
            val current = result.getOrDefault(tx.kategori, 0L)
            result[tx.kategori] = current + tx.nominal
        }
        return result
    }

    /**
     * Menghitung rasio proporsi nominal kategori terhadap total tipenya sendiri.
     * Dilengkapi pengaman pembagian nol.
     *
     * @param categoryTotal Total nominal kategori tertentu
     * @param totalForType  Total keseluruhan nominal untuk tipe yang sesuai (pemasukan atau pengeluaran)
     * @return Nilai float antara 0.0f sampai 1.0f
     */
    fun calculateCategoryRatio(categoryTotal: Long, totalForType: Long): Float {
        if (totalForType <= 0L || categoryTotal <= 0L) return 0f
        val ratio = categoryTotal.toDouble() / totalForType.toDouble()
        return ratio.toFloat().coerceIn(0f, 1f)
    }

    /**
     * Menghitung jumlah pesanan berstatus SELESAI yang memiliki tanggal selesaiPada
     * dan jatuh di dalam rentang periode yang ditentukan.
     */
    fun countCompletedOrders(orders: List<Order>, periodRange: DateRange): Int {
        return orders.count { order ->
            order.status == OrderStatus.SELESAI &&
                    order.selesaiPada != null &&
                    periodRange.contains(order.selesaiPada)
        }
    }

    /**
     * Menghitung batas rentang tanggal satu bulan penuh [startMillis, endMillis]
     * untuk tahun dan bulan tertentu (0-indexed Calendar.MONTH).
     */
    fun getMonthRange(year: Int, monthIndex: Int, timeZone: TimeZone = TimeZone.getDefault()): DateRange {
        val cal = Calendar.getInstance(timeZone).apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis

        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, maxDay)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return DateRange(start, end)
    }

    /**
     * Membangun agregasi tren keuangan 6 bulan terakhir secara deterministik (fungsi murni).
     *
     * @param now Waktu acuan saat ini (epoch ms)
     * @param timeZone Zona waktu perangkat/kalender
     * @param manualTransactions Daftar transaksi manual
     * @param orders Daftar pesanan
     * @return [SixMonthTrend] memuat 6 [MonthlyFinanceBar] dari 5 bulan lalu hingga bulan berjalan
     */
    fun buildMonthlyTrend(
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
        manualTransactions: List<Transaction>,
        orders: List<Order>
    ): SixMonthTrend {
        val combinedTx = buildCombinedTransactions(manualTransactions, orders)

        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = now
        }
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)

        val monthlyBars = mutableListOf<MonthlyFinanceBar>()

        // 6 bulan: dari i = 5 turun ke 0 (5 bulan lalu sampai bulan ini)
        for (offset in 5 downTo 0) {
            val monthCal = Calendar.getInstance(timeZone).apply {
                clear()
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -offset)
            }
            val y = monthCal.get(Calendar.YEAR)
            val m = monthCal.get(Calendar.MONTH)
            val range = getMonthRange(y, m, timeZone)

            val monthTx = filterTransactionsByPeriod(combinedTx, range)
            val income = calculateTotalIncome(monthTx)
            val expense = calculateTotalExpense(monthTx)
            val profit = calculateNetProfit(income, expense)
            val margin = if (income > 0L) {
                (profit.toDouble() / income.toDouble()) * 100.0
            } else {
                null
            }

            monthlyBars.add(
                MonthlyFinanceBar(
                    monthIndex = m,
                    year = y,
                    range = range,
                    totalIncome = income,
                    totalExpense = expense,
                    netProfit = profit,
                    marginPercentage = margin,
                    isCurrentMonth = (offset == 0)
                )
            )
        }

        val totalIncome6 = monthlyBars.sumOf { it.totalIncome }
        val totalProfit6 = monthlyBars.sumOf { it.netProfit }
        val avgMargin = if (totalIncome6 > 0L) {
            (totalProfit6.toDouble() / totalIncome6.toDouble()) * 100.0
        } else {
            null
        }

        val currentMonthBar = monthlyBars.last()
        val lastMonthBar = monthlyBars[monthlyBars.size - 2]

        // Perbandingan pertumbuhan pendapatan: Month-to-Date (MTD)
        // Membandingkan tanggal 1 s.d. hari ini bulan berjalan vs tanggal 1 s.d. hari ini bulan lalu
        val lastMonthMtdRange = PeriodRules.getLastMonthSamePeriodRange(now, timeZone)
        val lastMonthMtdTx = filterTransactionsByPeriod(combinedTx, lastMonthMtdRange)
        val lastMonthMtdIncome = calculateTotalIncome(lastMonthMtdTx)

        val growth = if (lastMonthMtdIncome > 0L) {
            ((currentMonthBar.totalIncome - lastMonthMtdIncome).toDouble() / lastMonthMtdIncome.toDouble()) * 100.0
        } else {
            null
        }

        return SixMonthTrend(
            monthlyBars = monthlyBars,
            averageMarginPercentage = avgMargin,
            currentMonthIncome = currentMonthBar.totalIncome,
            lastMonthIncome = lastMonthBar.totalIncome,
            incomeGrowthPercentage = growth
        )
    }
}
