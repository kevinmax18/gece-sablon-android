package com.gecesablon.app.data.model

import com.gecesablon.app.core.util.DateRange

/**
 * Data ringkasan keuangan per bulan untuk tren grafik.
 *
 * @param monthIndex 0..11 (Calendar.MONTH)
 * @param year Tahun kalender
 * @param range Rentang waktu [DateRange] awal bulan sampai akhir bulan
 * @param totalIncome Total pemasukan bulan tersebut (manual + pesanan selesai)
 * @param totalExpense Total pengeluaran bulan tersebut
 * @param netProfit Laba/rugi bersih (totalIncome - totalExpense)
 * @param marginPercentage Margin laba (netProfit / totalIncome * 100), null jika totalIncome == 0
 * @param isCurrentMonth Menandakan apakah bulan ini adalah bulan berjalan
 */
data class MonthlyFinanceBar(
    val monthIndex: Int,
    val year: Int,
    val range: DateRange,
    val totalIncome: Long,
    val totalExpense: Long,
    val netProfit: Long,
    val marginPercentage: Double?,
    val isCurrentMonth: Boolean
)

/**
 * Agregasi tren 6 bulan terakhir untuk layar dashboard dan laporan owner.
 *
 * @param monthlyBars 6 item bulanan berurutan dari 5 bulan lalu hingga bulan berjalan
 * @param averageMarginPercentage Rata-rata margin berbobot (totalLaba6Bulan / totalPemasukan6Bulan * 100), null jika totalPemasukan6Bulan == 0
 * @param currentMonthIncome Pemasukan bulan berjalan
 * @param lastMonthIncome Pemasukan bulan lalu
 * @param incomeGrowthPercentage Pertumbuhan pendapatan dibanding bulan lalu ((current - last) / last * 100), null jika lastMonthIncome == 0
 */
data class SixMonthTrend(
    val monthlyBars: List<MonthlyFinanceBar>,
    val averageMarginPercentage: Double?,
    val currentMonthIncome: Long,
    val lastMonthIncome: Long,
    val incomeGrowthPercentage: Double?
)
