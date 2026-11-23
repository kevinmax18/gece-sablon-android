package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.FinancePeriod
import java.util.Calendar
import java.util.TimeZone

/**
 * Representasi rentang waktu tertutup [startMillis, endMillis] (inklusif).
 */
data class DateRange(
    val startMillis: Long,
    val endMillis: Long
) {
    init {
        require(startMillis <= endMillis) {
            "Rentang tidak valid: waktu mulai ($startMillis) lebih besar dari waktu akhir ($endMillis)"
        }
    }

    /**
     * Memeriksa apakah suatu epoch timestamp berada di dalam rentang waktu (inklusif).
     */
    fun contains(timestamp: Long): Boolean {
        return timestamp in startMillis..endMillis
    }
}

/**
 * Aturan batas waktu dan kalender untuk periode keuangan sesuai spesifikasi.
 * Menggunakan java.util.Calendar tanpa java.time.
 */
object PeriodRules {

    /**
     * Menghitung batas awal hari (00:00:00.000) untuk timestamp yang diberikan pada zona waktu tertentu.
     */
    fun startOfDay(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Menghitung batas akhir hari (23:59:59.999) untuk timestamp yang diberikan pada zona waktu tertentu.
     */
    fun endOfDay(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    /**
     * Menghitung rentang tanggal [DateRange] untuk perbandingan Month-to-Date (MTD) bulan lalu:
     * Mulai dari tanggal 1 bulan lalu (00:00:00.000) sampai tanggal hari ini pada bulan lalu (23:59:59.999).
     * Jika tanggal hari ini melebihi jumlah hari maksimal di bulan lalu (misal tgl 31 pada bulan yg 30 hari),
     * maka batas akhir disesuaikan ke hari terakhir bulan lalu (misal tgl 30).
     */
    fun getLastMonthSamePeriodRange(
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): DateRange {
        val currentCal = Calendar.getInstance(timeZone).apply {
            timeInMillis = now
        }
        val currentDay = currentCal.get(Calendar.DAY_OF_MONTH)

        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = now
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis

        val maxDayLastMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val targetDay = currentDay.coerceAtMost(maxDayLastMonth)

        cal.set(Calendar.DAY_OF_MONTH, targetDay)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return DateRange(start, end)
    }

    /**
     * Menghitung rentang [DateRange] untuk [FinancePeriod].
     *
     * @param period       Periode yang dipilih
     * @param customStart  Timestamp mulai (wajib jika CUSTOM)
     * @param customEnd    Timestamp akhir (wajib jika CUSTOM)
     * @param now          Waktu acuan sekarang (default System.currentTimeMillis)
     * @param timeZone     Zona waktu kalender (default TimeZone.getDefault)
     */
    fun getPeriodRange(
        period: FinancePeriod,
        customStart: Long? = null,
        customEnd: Long? = null,
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): DateRange {
        return when (period) {
            FinancePeriod.BULAN_INI -> {
                val cal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = now
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

                DateRange(start, end)
            }

            FinancePeriod.BULAN_LALU -> {
                val cal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = now
                    add(Calendar.MONTH, -1)
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

                DateRange(start, end)
            }

            FinancePeriod.HARI_7 -> {
                val end = endOfDay(now, timeZone)
                val cal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, -6)
                }
                val start = startOfDay(cal.timeInMillis, timeZone)
                DateRange(start, end)
            }

            FinancePeriod.HARI_30 -> {
                val end = endOfDay(now, timeZone)
                val cal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, -29)
                }
                val start = startOfDay(cal.timeInMillis, timeZone)
                DateRange(start, end)
            }

            FinancePeriod.CUSTOM -> {
                requireNotNull(customStart) { "Tanggal mulai wajib diisi untuk rentang kustom." }
                requireNotNull(customEnd) { "Tanggal akhir wajib diisi untuk rentang kustom." }

                val start = startOfDay(customStart, timeZone)
                val end = endOfDay(customEnd, timeZone)
                require(start <= end) { "Tanggal mulai tidak boleh melebihi tanggal akhir." }

                DateRange(start, end)
            }
        }
    }

    /**
     * Mengonversi epoch UTC dari Material 3 DatePicker / DateRangePicker ke [DateRange] lokal.
     * DatePicker Material 3 mengembalikan epoch milliseconds jam 00:00:00 UTC.
     * Fungsi ini mengekstrak Year, Month, dan Day dari kalender UTC,
     * kemudian menyusun awal hari (00:00:00.000) dan akhir hari (23:59:59.999) pada zona waktu lokal.
     */
    fun utcRangeToLocalDateRange(
        utcStartMillis: Long,
        utcEndMillis: Long,
        localTimeZone: TimeZone = TimeZone.getDefault()
    ): DateRange {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        utcCal.timeInMillis = utcStartMillis
        val startYear = utcCal.get(Calendar.YEAR)
        val startMonth = utcCal.get(Calendar.MONTH)
        val startDay = utcCal.get(Calendar.DAY_OF_MONTH)

        utcCal.timeInMillis = utcEndMillis
        val endYear = utcCal.get(Calendar.YEAR)
        val endMonth = utcCal.get(Calendar.MONTH)
        val endDay = utcCal.get(Calendar.DAY_OF_MONTH)

        val localCal = Calendar.getInstance(localTimeZone)

        localCal.set(startYear, startMonth, startDay, 0, 0, 0)
        localCal.set(Calendar.MILLISECOND, 0)
        val localStart = localCal.timeInMillis

        localCal.set(endYear, endMonth, endDay, 23, 59, 59)
        localCal.set(Calendar.MILLISECOND, 999)
        val localEnd = localCal.timeInMillis

        return DateRange(localStart, localEnd)
    }

    /**
     * Mengonversi satu tanggal epoch UTC dari Material 3 DatePicker ke timestamp lokal.
     */
    fun utcMillisToLocalDate(
        utcMillis: Long,
        localTimeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMillis
        }
        val year = utcCal.get(Calendar.YEAR)
        val month = utcCal.get(Calendar.MONTH)
        val day = utcCal.get(Calendar.DAY_OF_MONTH)

        val localCal = Calendar.getInstance(localTimeZone).apply {
            set(year, month, day, 12, 0, 0) // Tengah hari agar aman dari DST dan batas awal/akhir
            set(Calendar.MILLISECOND, 0)
        }
        return localCal.timeInMillis
    }

    /**
     * Menghitung rentang [DateRange] periode pembanding untuk [FinancePeriod].
     * - BULAN_INI: tanggal 1 sampai tanggal hari ini di bulan lalu (dipotong bila bulan lalu lebih pendek)
     * - BULAN_LALU: bulan sebelumnya penuh (2 bulan lalu dari now)
     * - HARI_7: 7 hari tepat sebelum rentang HARI_7 (yaitu rentang sama panjang tepat sebelumnya)
     * - HARI_30: 30 hari tepat sebelum rentang HARI_30
     * - CUSTOM: rentang sama panjang (jumlah hari) tepat sebelumnya
     */
    fun getComparisonPeriodRange(
        period: FinancePeriod,
        customStart: Long? = null,
        customEnd: Long? = null,
        now: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): DateRange {
        return when (period) {
            FinancePeriod.BULAN_INI -> {
                getLastMonthSamePeriodRange(now, timeZone)
            }

            FinancePeriod.BULAN_LALU -> {
                val cal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = now
                    add(Calendar.MONTH, -2)
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

                DateRange(start, end)
            }

            FinancePeriod.HARI_7 -> {
                val currentRange = getPeriodRange(FinancePeriod.HARI_7, now = now, timeZone = timeZone)
                val prevEnd = currentRange.startMillis - 1
                val cal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = currentRange.startMillis
                    add(Calendar.DAY_OF_YEAR, -7)
                }
                val prevStart = startOfDay(cal.timeInMillis, timeZone)
                DateRange(prevStart, endOfDay(prevEnd, timeZone))
            }

            FinancePeriod.HARI_30 -> {
                val currentRange = getPeriodRange(FinancePeriod.HARI_30, now = now, timeZone = timeZone)
                val prevEnd = currentRange.startMillis - 1
                val cal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = currentRange.startMillis
                    add(Calendar.DAY_OF_YEAR, -30)
                }
                val prevStart = startOfDay(cal.timeInMillis, timeZone)
                DateRange(prevStart, endOfDay(prevEnd, timeZone))
            }

            FinancePeriod.CUSTOM -> {
                requireNotNull(customStart) { "Tanggal mulai wajib diisi untuk rentang kustom." }
                requireNotNull(customEnd) { "Tanggal akhir wajib diisi untuk rentang kustom." }
                val currentStartDay = startOfDay(customStart, timeZone)
                val currentEndDay = endOfDay(customEnd, timeZone)
                require(currentStartDay <= currentEndDay) { "Tanggal mulai tidak boleh melebihi tanggal akhir." }

                // Hitung jumlah hari inklusif
                val startCal = Calendar.getInstance(timeZone).apply { timeInMillis = currentStartDay }
                val endCal = Calendar.getInstance(timeZone).apply { timeInMillis = currentEndDay }
                val dayDiff = ((endCal.timeInMillis - startCal.timeInMillis) / (24L * 60 * 60 * 1000)).toInt() + 1

                val prevEnd = currentStartDay - 1
                val prevStartCal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = currentStartDay
                    add(Calendar.DAY_OF_YEAR, -dayDiff)
                }
                val prevStart = startOfDay(prevStartCal.timeInMillis, timeZone)
                DateRange(prevStart, endOfDay(prevEnd, timeZone))
            }
        }
    }
}
