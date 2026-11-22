package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.model.toVirtualTransactionOrNull

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
}
