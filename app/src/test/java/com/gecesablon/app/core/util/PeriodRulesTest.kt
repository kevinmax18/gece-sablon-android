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

    @Test
    fun testGetComparisonPeriodRange_semuaKasus() {
        // 1. BULAN_INI: Hari ke-31 Mei vs bulan lalu April (30 hari)
        val nowMay31 = createTimestamp(2026, 5, 31, 10, 0, 0)
        val compMay31 = PeriodRules.getComparisonPeriodRange(FinancePeriod.BULAN_INI, now = nowMay31, timeZone = testZone)
        val calEndMayComp = Calendar.getInstance(testZone).apply { timeInMillis = compMay31.endMillis }
        assertEquals(30, calEndMayComp.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.APRIL, calEndMayComp.get(Calendar.MONTH))

        // 2. BULAN_INI: Maret -> Februari kabisat (2024: 29 hari)
        val nowMar31Leap = createTimestamp(2024, 3, 31, 12, 0, 0)
        val compMarLeap = PeriodRules.getComparisonPeriodRange(FinancePeriod.BULAN_INI, now = nowMar31Leap, timeZone = testZone)
        val calEndMarLeap = Calendar.getInstance(testZone).apply { timeInMillis = compMarLeap.endMillis }
        assertEquals(29, calEndMarLeap.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FEBRUARY, calEndMarLeap.get(Calendar.MONTH))

        // 3. BULAN_INI: Lintas tahun (Januari -> Desember tahun sebelumnya)
        val nowJan15 = createTimestamp(2027, 1, 15, 10, 0, 0)
        val compJan = PeriodRules.getComparisonPeriodRange(FinancePeriod.BULAN_INI, now = nowJan15, timeZone = testZone)
        val calStartJan = Calendar.getInstance(testZone).apply { timeInMillis = compJan.startMillis }
        val calEndJan = Calendar.getInstance(testZone).apply { timeInMillis = compJan.endMillis }
        assertEquals(2026, calStartJan.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calStartJan.get(Calendar.MONTH))
        assertEquals(1, calStartJan.get(Calendar.DAY_OF_MONTH))
        assertEquals(2026, calEndJan.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calEndJan.get(Calendar.MONTH))
        assertEquals(15, calEndJan.get(Calendar.DAY_OF_MONTH))

        // 4. BULAN_LALU: Bulan sebelumnya penuh (dari Nov 2026 -> 2 bulan lalu = Sept 2026)
        val nowNov = createTimestamp(2026, 11, 20, 10, 0, 0)
        val compBulanLalu = PeriodRules.getComparisonPeriodRange(FinancePeriod.BULAN_LALU, now = nowNov, timeZone = testZone)
        val calStartBL = Calendar.getInstance(testZone).apply { timeInMillis = compBulanLalu.startMillis }
        val calEndBL = Calendar.getInstance(testZone).apply { timeInMillis = compBulanLalu.endMillis }
        assertEquals(Calendar.SEPTEMBER, calStartBL.get(Calendar.MONTH))
        assertEquals(1, calStartBL.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.SEPTEMBER, calEndBL.get(Calendar.MONTH))
        assertEquals(30, calEndBL.get(Calendar.DAY_OF_MONTH))

        // 5. HARI_7: 7 hari sebelum rentang 7 hari aktif
        // Rentang aktif 14-20 Nov -> Rentang pembanding: 7-13 Nov
        val comp7 = PeriodRules.getComparisonPeriodRange(FinancePeriod.HARI_7, now = nowNov, timeZone = testZone)
        val calStart7 = Calendar.getInstance(testZone).apply { timeInMillis = comp7.startMillis }
        val calEnd7 = Calendar.getInstance(testZone).apply { timeInMillis = comp7.endMillis }
        assertEquals(7, calStart7.get(Calendar.DAY_OF_MONTH))
        assertEquals(13, calEnd7.get(Calendar.DAY_OF_MONTH))

        // 6. HARI_30: 30 hari sebelum rentang 30 hari aktif
        val comp30 = PeriodRules.getComparisonPeriodRange(FinancePeriod.HARI_30, now = nowNov, timeZone = testZone)
        val current30 = PeriodRules.getPeriodRange(FinancePeriod.HARI_30, now = nowNov, timeZone = testZone)
        assertEquals(PeriodRules.startOfDay(current30.startMillis, testZone) - 1, comp30.endMillis)

        // 7. CUSTOM 1 hari: 15 Nov -> pembanding: 14 Nov
        val customDay = createTimestamp(2026, 11, 15)
        val compCustom1 = PeriodRules.getComparisonPeriodRange(
            FinancePeriod.CUSTOM,
            customStart = customDay,
            customEnd = customDay,
            timeZone = testZone
        )
        val calStartCustom1 = Calendar.getInstance(testZone).apply { timeInMillis = compCustom1.startMillis }
        val calEndCustom1 = Calendar.getInstance(testZone).apply { timeInMillis = compCustom1.endMillis }
        assertEquals(14, calStartCustom1.get(Calendar.DAY_OF_MONTH))
        assertEquals(14, calEndCustom1.get(Calendar.DAY_OF_MONTH))
    }
}
