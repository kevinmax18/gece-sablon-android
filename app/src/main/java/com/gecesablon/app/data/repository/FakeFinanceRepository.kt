package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.DateRange
import com.gecesablon.app.core.util.FinanceCalculator
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.FinanceSummary
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionSource
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.model.toVirtualTransactionOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Implementasi palsu in-memory untuk [FinanceRepository] pada Tahap 6A.
 * Menggunakan [combine] antara aliran pesanan ([OrderRepository.getAllOrders]) dan
 * StateFlow transaksi manual sehingga layar keuangan ikut berubah secara reaktif
 * saat pesanan baru ditandai SELESAI.
 *
 * TODO: ganti ke Retrofit di Tahap 8
 */
class FakeFinanceRepository(
    private val orderRepository: OrderRepository
) : FinanceRepository {

    // =========================================================================
    // Konstanta validasi
    // =========================================================================

    private val NOMINAL_MIN = 1L
    private val NOMINAL_MAX = 1_000_000_000L
    private val KETERANGAN_MAX = 200

    // =========================================================================
    // Sesi owner palsu untuk membaca pesanan (sesuai pola Repository yang ada)
    // TODO: di API asli aktor dari token
    // =========================================================================

    private val ownerSession = Session(
        token = "fake-owner-token",
        role = Role.OWNER,
        name = "Owner GeCe",
        userId = "user-owner-001"
    )

    // =========================================================================
    // Seed transaksi manual ~12 entri selama dua bulan terakhir
    // Tanggal dihitung relatif dari hari ini agar periode selalu berisi data
    // =========================================================================

    private val DAY = 24L * 60 * 60 * 1000
    private val now = System.currentTimeMillis()

    private val seedTransactions: List<Transaction> = listOf(
        // --- Pengeluaran bulan ini ---
        Transaction(
            id = "fin-001",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.BAHAN_BAKU,
            nominal = 1_250_000L,
            tanggal = now - (3 * DAY),
            keterangan = "Pembelian kain Cotton Combed 24s hitam solid 50 yard",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-002",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.MATERIAL_DTF,
            nominal = 850_000L,
            tanggal = now - (5 * DAY),
            keterangan = "Tinta DTF CMYK + White 1 liter per warna & PET film 60cm x 100m",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-003",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.OPERASIONAL,
            nominal = 420_000L,
            tanggal = now - (7 * DAY),
            keterangan = "Tagihan listrik workshop bulan November",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-004",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.GAJI,
            nominal = 2_500_000L,
            tanggal = now - (10 * DAY),
            keterangan = "Gaji staf sablon November (1 orang)",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-005",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.BAHAN_BAKU,
            nominal = 650_000L,
            tanggal = now - (12 * DAY),
            keterangan = "Restock kain Polo TC putih dan baby blue 30 yard",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-006",
            tipe = TransactionType.PEMASUKAN,
            kategori = TransactionCategory.PENJUALAN_LAIN,
            nominal = 450_000L,
            tanggal = now - (8 * DAY),
            keterangan = "Jual sisa kaos combed putih reject layak pakai (20 pcs @ Rp22.500)",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),

        // --- Pengeluaran bulan lalu ---
        Transaction(
            id = "fin-007",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.GAJI,
            nominal = 2_500_000L,
            tanggal = now - (38 * DAY),
            keterangan = "Gaji staf sablon Oktober (1 orang)",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-008",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.OPERASIONAL,
            nominal = 390_000L,
            tanggal = now - (40 * DAY),
            keterangan = "Tagihan listrik workshop bulan Oktober",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-009",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.MATERIAL_DTF,
            nominal = 920_000L,
            tanggal = now - (42 * DAY),
            keterangan = "Restock PET film 60cm x 150m dan lem hot melt DTF powder",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-010",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.BAHAN_BAKU,
            nominal = 1_100_000L,
            tanggal = now - (45 * DAY),
            keterangan = "Pembelian kain Cotton Combed 30s putih bersih 40 yard",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-011",
            tipe = TransactionType.PEMASUKAN,
            kategori = TransactionCategory.PENJUALAN_LAIN,
            nominal = 280_000L,
            tanggal = now - (35 * DAY),
            keterangan = "Jual tinta DTF sisa warna merah & kuning ke workshop tetangga",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        ),
        Transaction(
            id = "fin-012",
            tipe = TransactionType.PENGELUARAN,
            kategori = TransactionCategory.PENGELUARAN_LAINNYA,
            nominal = 175_000L,
            tanggal = now - (33 * DAY),
            keterangan = "Pembelian masker, sarung tangan, dan cairan cuci head meja press",
            sumber = TransactionSource.MANUAL,
            dibuatOleh = "Owner GeCe (Owner)"
        )
    )

    // =========================================================================
    // StateFlow transaksi manual (in-memory)
    // =========================================================================

    private val manualTransactionsFlow =
        kotlinx.coroutines.flow.MutableStateFlow<List<Transaction>>(seedTransactions)

    // =========================================================================
    // Validasi helper
    // =========================================================================

    private fun validateOwner(actor: Session): String? {
        // TODO: di API asli aktor dari token
        return if (actor.role != Role.OWNER) {
            "Akses ditolak. Fitur keuangan hanya dapat diakses oleh Owner."
        } else null
    }

    private fun validateTransaction(
        tipe: TransactionType,
        kategori: TransactionCategory,
        nominal: Long,
        keterangan: String
    ): String? {
        if (nominal < NOMINAL_MIN || nominal > NOMINAL_MAX) {
            return "Nominal harus lebih dari Rp0 dan tidak melebihi Rp1.000.000.000."
        }
        if (keterangan.length > KETERANGAN_MAX) {
            return "Keterangan maksimal $KETERANGAN_MAX karakter."
        }
        val validKategoriPengeluaran = TransactionCategory.expenseCategories
        val validKategoriPemasukan = TransactionCategory.manualIncomeCategories

        return when (tipe) {
            TransactionType.PENGELUARAN -> {
                if (kategori !in validKategoriPengeluaran) {
                    "Kategori '$kategori' tidak valid untuk transaksi pengeluaran."
                } else null
            }
            TransactionType.PEMASUKAN -> {
                if (kategori !in validKategoriPemasukan) {
                    "Kategori '$kategori' tidak valid untuk transaksi pemasukan manual."
                } else null
            }
        }
    }

    // =========================================================================
    // Implementasi Interface
    // =========================================================================

    /**
     * Mengalirkan transaksi gabungan (manual + virtual pesanan) secara reaktif.
     * Menggunakan [combine] agar layar ikut berubah saat pesanan ditandai selesai.
     */
    override fun getTransactions(
        actor: Session,
        range: DateRange
    ): Flow<Resource<List<Transaction>>> {
        val ownerError = validateOwner(actor)
        if (ownerError != null) {
            return flow { emit(Resource.Error(ownerError)) }
        }

        return combine(
            manualTransactionsFlow,
            orderRepository.getAllOrders(ownerSession)
        ) { manualList, ordersResource ->
            when (ordersResource) {
                is Resource.Success -> {
                    val combined = FinanceCalculator.buildCombinedTransactions(
                        manualTransactions = manualList,
                        orders = ordersResource.data
                    )
                    val filtered = FinanceCalculator.filterTransactionsByPeriod(combined, range)
                    Resource.Success(filtered)
                }
                is Resource.Error -> Resource.Error(ordersResource.message ?: "Gagal memuat data pesanan.")
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    /**
     * Mengambil satu transaksi. Mengenali prefix "order-" sebagai transaksi virtual pesanan.
     */
    override suspend fun getTransactionById(id: String, actor: Session): Resource<Transaction> {
        delay(300)
        val ownerError = validateOwner(actor)
        if (ownerError != null) return Resource.Error(ownerError)

        // Transaksi virtual dari pesanan: ID berformat "order-<orderId>"
        if (id.startsWith("order-")) {
            val orderId = id.removePrefix("order-")
            val orderResult = orderRepository.getOrderById(orderId)
            return when (orderResult) {
                is Resource.Success -> {
                    val virtual = orderResult.data.toVirtualTransactionOrNull()
                        ?: return Resource.Error("Pesanan ini belum selesai atau tidak memiliki tanggal selesai.")
                    Resource.Success(virtual)
                }
                is Resource.Error -> Resource.Error(orderResult.message ?: "Pesanan tidak ditemukan.")
                is Resource.Loading -> Resource.Error("Memuat data pesanan...")
            }
        }

        // Transaksi manual
        val tx = manualTransactionsFlow.value.find { it.id == id }
            ?: return Resource.Error("Transaksi tidak ditemukan.")
        return Resource.Success(tx)
    }

    override suspend fun createTransaction(
        actor: Session,
        tipe: TransactionType,
        kategori: TransactionCategory,
        nominal: Long,
        tanggal: Long,
        keterangan: String
    ): Resource<Transaction> {
        delay(500)
        val ownerError = validateOwner(actor)
        if (ownerError != null) return Resource.Error(ownerError)

        val validationError = validateTransaction(tipe, kategori, nominal, keterangan)
        if (validationError != null) return Resource.Error(validationError)

        val roleLabel = "Owner"
        val newTx = Transaction(
            id = UUID.randomUUID().toString(),
            tipe = tipe,
            kategori = kategori,
            nominal = nominal,
            tanggal = tanggal,
            keterangan = keterangan.trim(),
            sumber = TransactionSource.MANUAL,
            orderId = null,
            dibuatOleh = "${actor.name} ($roleLabel)"
        )

        manualTransactionsFlow.value = listOf(newTx) + manualTransactionsFlow.value
        return Resource.Success(newTx)
    }

    override suspend fun updateTransaction(
        id: String,
        actor: Session,
        tipe: TransactionType,
        kategori: TransactionCategory,
        nominal: Long,
        tanggal: Long,
        keterangan: String
    ): Resource<Transaction> {
        delay(500)
        val ownerError = validateOwner(actor)
        if (ownerError != null) return Resource.Error(ownerError)

        // Transaksi virtual pesanan tidak bisa diubah
        if (id.startsWith("order-")) {
            return Resource.Error("Transaksi dari pesanan tidak dapat diubah secara manual.")
        }

        val currentList = manualTransactionsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index == -1) return Resource.Error("Transaksi tidak ditemukan.")

        val existing = currentList[index]
        if (existing.sumber == TransactionSource.PESANAN) {
            return Resource.Error("Transaksi dari pesanan tidak dapat diubah secara manual.")
        }

        val validationError = validateTransaction(tipe, kategori, nominal, keterangan)
        if (validationError != null) return Resource.Error(validationError)

        val updated = existing.copy(
            tipe = tipe,
            kategori = kategori,
            nominal = nominal,
            tanggal = tanggal,
            keterangan = keterangan.trim()
        )
        currentList[index] = updated
        manualTransactionsFlow.value = currentList
        return Resource.Success(updated)
    }

    override suspend fun deleteTransaction(id: String, actor: Session): Resource<Unit> {
        delay(400)
        val ownerError = validateOwner(actor)
        if (ownerError != null) return Resource.Error(ownerError)

        // Transaksi virtual pesanan tidak bisa dihapus
        if (id.startsWith("order-")) {
            return Resource.Error("Transaksi dari pesanan tidak dapat dihapus secara manual.")
        }

        val currentList = manualTransactionsFlow.value.toMutableList()
        val tx = currentList.find { it.id == id }
            ?: return Resource.Error("Transaksi tidak ditemukan.")

        if (tx.sumber == TransactionSource.PESANAN) {
            return Resource.Error("Transaksi dari pesanan tidak dapat dihapus secara manual.")
        }

        currentList.removeAll { it.id == id }
        manualTransactionsFlow.value = currentList
        return Resource.Success(Unit)
    }

    /**
     * Mengalirkan ringkasan keuangan secara reaktif menggunakan [combine].
     */
    override fun getFinanceSummary(
        actor: Session,
        range: DateRange
    ): Flow<Resource<FinanceSummary>> {
        val ownerError = validateOwner(actor)
        if (ownerError != null) {
            return flow { emit(Resource.Error(ownerError)) }
        }

        return combine(
            manualTransactionsFlow,
            orderRepository.getAllOrders(ownerSession)
        ) { manualList, ordersResource ->
            when (ordersResource) {
                is Resource.Success -> {
                    val orders = ordersResource.data
                    val combined = FinanceCalculator.buildCombinedTransactions(manualList, orders)
                    val filtered = FinanceCalculator.filterTransactionsByPeriod(combined, range)

                    val totalIncome = FinanceCalculator.calculateTotalIncome(filtered)
                    val totalExpense = FinanceCalculator.calculateTotalExpense(filtered)
                    val labaRugi = FinanceCalculator.calculateNetProfit(totalIncome, totalExpense)
                    val jumlahSelesai = FinanceCalculator.countCompletedOrders(orders, range)
                    val byCategory = FinanceCalculator.calculateTotalsByCategory(filtered)

                    Resource.Success(
                        FinanceSummary(
                            totalPemasukan = totalIncome,
                            totalPengeluaran = totalExpense,
                            labaRugi = labaRugi,
                            jumlahPesananSelesai = jumlahSelesai,
                            categoryBreakdown = byCategory
                        )
                    )
                }
                is Resource.Error -> Resource.Error(ordersResource.message ?: "Gagal memuat ringkasan keuangan.")
                is Resource.Loading -> Resource.Loading
            }
        }
    }
}


