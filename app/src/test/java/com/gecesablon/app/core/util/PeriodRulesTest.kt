package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.FinancePeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Unit test untuk [PeriodRules] dengan zona waktu dan waktu acuan deterministik.
 */
class PeriodRulesTest {

    private val testZone = TimeZone.getTimeZone("GMT+07:00")

    private fun createTimestamp(
        year: Int,
        month: Int, // 1-12
        day: Int,
        hour: Int = 12,
        minute: Int = 0,
        second: Int = 0,
        milli: Int = 0
    ): Long {
        val cal = Calendar.getInstance(testZone).apply {
            set(year, month - 1, day, hour, minute, second)
            set(Calendar.MILLISECOND, milli)
        }
        return cal.timeInMillis
    }

    @Test
    fun testBatasHari_awalDanAkhir() {
        val ts = createTimestamp(2026, 11, 15, 14, 30, 45, 500)
        val start = PeriodRules.startOfDay(ts, testZone)
        val end = PeriodRules.endOfDay(ts, testZone)

        val calStart = Calendar.getInstance(testZone).apply { timeInMillis = start }
        val calEnd = Calendar.getInstance(testZone).apply { timeInMillis = end }

        assertEquals(0, calStart.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calStart.get(Calendar.MINUTE))
        assertEquals(0, calStart.get(Calendar.SECOND))
        assertEquals(0, calStart.get(Calendar.MILLISECOND))
        assertEquals(15, calStart.get(Calendar.DAY_OF_MONTH))

        assertEquals(23, calEnd.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calEnd.get(Calendar.MINUTE))
        assertEquals(59, calEnd.get(Calendar.SECOND))
        assertEquals(999, calEnd.get(Calendar.MILLISECOND))
        assertEquals(15, calEnd.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testBulanIni_batasAwalDanAkhir() {
        // Tanggal 18 November 2026 (November ada 30 hari)
        val now = createTimestamp(2026, 11, 18, 10, 0, 0)
        val range = PeriodRules.getPeriodRange(FinancePeriod.BULAN_INI, now = now, timeZone = testZone)

        val calStart = Calendar.getInstance(testZone).apply { timeInMillis = range.startMillis }
        val calEnd = Calendar.getInstance(testZone).apply { timeInMillis = range.endMillis }

        assertEquals(1, calStart.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.NOVEMBER, calStart.get(Calendar.MONTH))
        assertEquals(0, calStart.get(Calendar.HOUR_OF_DAY))

        assertEquals(30, calEnd.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.NOVEMBER, calEnd.get(Calendar.MONTH))
        assertEquals(23, calEnd.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun testBulanLalu_batasAwalDanAkhir() {
        // Dari 18 November 2026 -> Bulan lalu adalah Oktober 2026 (Oktober ada 31 hari)
        val now = createTimestamp(2026, 11, 18, 10, 0, 0)
        val range = PeriodRules.getPeriodRange(FinancePeriod.BULAN_LALU, now = now, timeZone = testZone)

        val calStart = Calendar.getInstance(testZone).apply { timeInMillis = range.startMillis }
        val calEnd = Calendar.getInstance(testZone).apply { timeInMillis = range.endMillis }

        assertEquals(1, calStart.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, calStart.get(Calendar.MONTH))
        assertEquals(0, calStart.get(Calendar.HOUR_OF_DAY))

        assertEquals(31, calEnd.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, calEnd.get(Calendar.MONTH))
        assertEquals(23, calEnd.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun testTujuhHariTerakhir() {
        // Dari 18 November 2026 15:00 -> 7 hari terakhir: 12 November 00:00 s/d 18 November 23:59:59.999
        val now = createTimestamp(2026, 11, 18, 15, 0, 0)
        val range = PeriodRules.getPeriodRange(FinancePeriod.HARI_7, now = now, timeZone = testZone)

        val calStart = Calendar.getInstance(testZone).apply { timeInMillis = range.startMillis }
        val calEnd = Calendar.getInstance(testZone).apply { timeInMillis = range.endMillis }

        assertEquals(12, calStart.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, calStart.get(Calendar.HOUR_OF_DAY))
        assertEquals(18, calEnd.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, calEnd.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun testTigaPuluhHariTerakhir() {
        val now = createTimestamp(2026, 11, 18, 15, 0, 0)
        val range = PeriodRules.getPeriodRange(FinancePeriod.HARI_30, now = now, timeZone = testZone)

        val calStart = Calendar.getInstance(testZone).apply { timeInMillis = range.startMillis }
        val calEnd = Calendar.getInstance(testZone).apply { timeInMillis = range.endMillis }

        // 18 November mundur 29 hari = 20 Oktober
        assertEquals(20, calStart.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, calStart.get(Calendar.MONTH))
        assertEquals(18, calEnd.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.NOVEMBER, calEnd.get(Calendar.MONTH))
    }

    @Test
    fun testTahunKabisat_februari() {
        // Tahun 2024 kabisat: Februari ada 29 hari
        val leapYearNow = createTimestamp(2024, 2, 10, 10, 0, 0)
        val leapRange = PeriodRules.getPeriodRange(FinancePeriod.BULAN_INI, now = leapYearNow, timeZone = testZone)
        val leapEnd = Calendar.getInstance(testZone).apply { timeInMillis = leapRange.endMillis }
        assertEquals(29, leapEnd.get(Calendar.DAY_OF_MONTH))

        // Tahun 2025 non-kabisat: Februari ada 28 hari
        val nonLeapYearNow = createTimestamp(2025, 2, 10, 10, 0, 0)
        val nonLeapRange = PeriodRules.getPeriodRange(FinancePeriod.BULAN_INI, now = nonLeapYearNow, timeZone = testZone)
        val nonLeapEnd = Calendar.getInstance(testZone).apply { timeInMillis = nonLeapRange.endMillis }
        assertEquals(28, nonLeapEnd.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testRentangTerbalikDitolak() {
        val start = createTimestamp(2026, 11, 20)
        val end = createTimestamp(2026, 11, 10)

        try {
            PeriodRules.getPeriodRange(
                period = FinancePeriod.CUSTOM,
                customStart = start,
                customEnd = end,
                timeZone = testZone
            )
            fail("Seharusnya melempar IllegalArgumentException ketika tanggal mulai melebihi tanggal akhir")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("tidak boleh melebihi") == true)
        }
    }

    @Test
    fun testNilaiTepatDiBatasPeriodeTerhitung() {
        val start = createTimestamp(2026, 11, 1, 0, 0, 0, 0)
        val end = createTimestamp(2026, 11, 30, 23, 59, 59, 999)
        val range = DateRange(start, end)

        // Tepat di milidetik awal dan akhir
        assertTrue("Milidetik start wajib terhitung", range.contains(start))
        assertTrue("Milidetik end wajib terhitung", range.contains(end))

        // Di luar batas 1 milidetik
        assertFalse("1 ms sebelum start harus di luar", range.contains(start - 1))
        assertFalse("1 ms setelah end harus di luar", range.contains(end + 1))
    }

    @Test
    fun testGetLastMonthSamePeriodRange_akhirBulanPendekDanBiasa() {
        // Kasus 1: Tanggal 15 Mei 2026 -> Bulan lalu: 1 s.d. 15 April 2026
        val nowMay15 = createTimestamp(2026, 5, 15, 14, 0, 0)
        val rangeMay15 = PeriodRules.getLastMonthSamePeriodRange(nowMay15, testZone)
        val calStartMay = Calendar.getInstance(testZone).apply { timeInMillis = rangeMay15.startMillis }
        val calEndMay = Calendar.getInstance(testZone).apply { timeInMillis = rangeMay15.endMillis }
        assertEquals(1, calStartMay.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.APRIL, calStartMay.get(Calendar.MONTH))
        assertEquals(15, calEndMay.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.APRIL, calEndMay.get(Calendar.MONTH))
        assertEquals(23, calEndMay.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calEndMay.get(Calendar.MINUTE))
        assertEquals(59, calEndMay.get(Calendar.SECOND))

        // Kasus 2: Tanggal 31 Mei 2026 -> Bulan lalu (April) hanya 30 hari -> 1 s.d. 30 April 2026
        val nowMay31 = createTimestamp(2026, 5, 31, 10, 0, 0)
        val rangeMay31 = PeriodRules.getLastMonthSamePeriodRange(nowMay31, testZone)
        val calEndApril = Calendar.getInstance(testZone).apply { timeInMillis = rangeMay31.endMillis }
        assertEquals(30, calEndApril.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.APRIL, calEndApril.get(Calendar.MONTH))

        // Kasus 3: Tanggal 31 Maret 2024 (Tahun Kabisat) -> Bulan lalu (Februari) 29 hari -> 1 s.d. 29 Feb 2024
        val nowMar31Leap = createTimestamp(2024, 3, 31, 12, 0, 0)
        val rangeMar31Leap = PeriodRules.getLastMonthSamePeriodRange(nowMar31Leap, testZone)
        val calEndFebLeap = Calendar.getInstance(testZone).apply { timeInMillis = rangeMar31Leap.endMillis }
        assertEquals(29, calEndFebLeap.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FEBRUARY, calEndFebLeap.get(Calendar.MONTH))

        // Kasus 4: Tanggal 31 Maret 2025 (Bukan Kabisat) -> Bulan lalu (Februari) 28 hari -> 1 s.d. 28 Feb 2025
        val nowMar31NonLeap = createTimestamp(2025, 3, 31, 12, 0, 0)
        val rangeMar31NonLeap = PeriodRules.getLastMonthSamePeriodRange(nowMar31NonLeap, testZone)
        val calEndFebNonLeap = Calendar.getInstance(testZone).apply { timeInMillis = rangeMar31NonLeap.endMillis }
        assertEquals(28, calEndFebNonLeap.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FEBRUARY, calEndFebNonLeap.get(Calendar.MONTH))
    }
}
