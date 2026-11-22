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
import org.junit.Test

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
}
