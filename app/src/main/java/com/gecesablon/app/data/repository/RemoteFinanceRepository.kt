package com.gecesablon.app.data.repository

import com.gecesablon.app.core.network.fetchAllPages
import com.gecesablon.app.core.network.safeApiCall
import com.gecesablon.app.core.network.safeApiCallUnit
import com.gecesablon.app.core.util.DateRange
import com.gecesablon.app.core.util.FinanceCalculator
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.FinanceSummary
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.SixMonthTrend
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionSource
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.remote.GeceRemoteApi
import com.gecesablon.app.data.remote.dto.TransactionRequestDto
import com.gecesablon.app.data.remote.mapper.formatMillisToDateOnly
import com.gecesablon.app.data.remote.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementasi FinanceRepository yang terhubung ke REST API Laravel GeCe Sablon.
 * Hanya dapat diakses oleh Owner.
 *
 * Catatan Penting: Server sudah menyimpan transaksi otomatis (source = 'auto')
 * dari pesanan selesai ke dalam tabel `transactions`. Oleh karena itu, seluruh transaksi
 * dari server diperlakukan sebagai daftar final tanpa menggabungkan pesanan lagi
 * guna menghindari penghitungan ganda (double-counting).
 */
class RemoteFinanceRepository(
    private val api: GeceRemoteApi,
    private val orderRepository: OrderRepository
) : FinanceRepository {

    override fun getTransactions(
        actor: Session,
        range: DateRange
    ): Flow<Resource<List<Transaction>>> = flow {
        emit(Resource.Loading)
        if (actor.role != Role.OWNER) {
            emit(Resource.Error("Akses ditolak: Hanya Owner yang dapat mengakses data keuangan."))
            return@flow
        }

        val fromStr = formatMillisToDateOnly(range.startMillis)
        val toStr = formatMillisToDateOnly(range.endMillis)

        val txResult = fetchAllPages { page ->
            safeApiCall { api.getTransactions(page = page, from = fromStr, to = toStr) }
        }

        if (txResult !is Resource.Success) {
            emit(Resource.Error((txResult as? Resource.Error)?.message ?: "Gagal mengambil data transaksi."))
            return@flow
        }

        val transactions = txResult.data.map { it.toDomain() }
        val filtered = FinanceCalculator.filterTransactionsByPeriod(transactions, range)
        emit(Resource.Success(filtered.sortedByDescending { it.tanggal }))
    }

    override suspend fun getTransactionById(id: String, actor: Session): Resource<Transaction> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya Owner yang dapat melihat detail transaksi.")
        }

        // Cek jika ID berupa virtual ID dari pesanan
        if (id.startsWith("order-")) {
            val orderId = id.removePrefix("order-")
            val orderResult = orderRepository.getOrderById(orderId)
            return when (orderResult) {
                is Resource.Success -> {
                    val order = orderResult.data
                    if (order.status == OrderStatus.SELESAI && order.selesaiPada != null) {
                        Resource.Success(
                            Transaction(
                                id = id,
                                tipe = TransactionType.PEMASUKAN,
                                kategori = TransactionCategory.PESANAN,
                                nominal = order.total,
                                tanggal = order.selesaiPada,
                                keterangan = "Pesanan ${order.kode}",
                                sumber = TransactionSource.PESANAN,
                                orderId = order.id,
                                dibuatOleh = "Sistem (Pesanan)"
                            )
                        )
                    } else {
                        Resource.Error("Pesanan terkait belum selesai atau data tanggal selesai tidak tersedia.")
                    }
                }
                is Resource.Error -> Resource.Error(orderResult.message)
                is Resource.Loading -> Resource.Loading
            }
        }

        val intId = id.toIntOrNull()
            ?: return Resource.Error("ID transaksi tidak valid: $id")

        val txResult = fetchAllPages { page ->
            safeApiCall { api.getTransactions(page = page) }
        }

        return when (txResult) {
            is Resource.Success -> {
                val found = txResult.data.find { it.id == intId }
                if (found != null) {
                    Resource.Success(found.toDomain())
                } else {
                    Resource.Error("Transaksi dengan ID $id tidak ditemukan.")
                }
            }
            is Resource.Error -> Resource.Error(txResult.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun createTransaction(
        actor: Session,
        tipe: TransactionType,
        kategori: TransactionCategory,
        nominal: Long,
        tanggal: Long,
        keterangan: String
    ): Resource<Transaction> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya Owner yang dapat mencatat transaksi.")
        }
        if (nominal <= 0L || nominal > 1_000_000_000L) {
            return Resource.Error("Nominal transaksi harus lebih dari 0 dan maksimal Rp1.000.000.000.")
        }
        if (keterangan.length > 200) {
            return Resource.Error("Keterangan transaksi maksimal 200 karakter.")
        }

        val typeStr = if (tipe == TransactionType.PEMASUKAN) "income" else "expense"
        val catStr = mapCategoryToApiString(kategori)
        val dateStr = formatMillisToDateOnly(tanggal)

        val req = TransactionRequestDto(
            type = typeStr,
            category = catStr,
            amount = nominal,
            description = keterangan.ifBlank { null },
            trxDate = dateStr
        )

        val result = safeApiCall { api.createTransaction(req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
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
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya Owner yang dapat mengubah transaksi.")
        }
        if (id.startsWith("order-")) {
            return Resource.Error("Transaksi otomatis dari pesanan tidak dapat diubah.")
        }
        val intId = id.toIntOrNull()
            ?: return Resource.Error("ID transaksi tidak valid: $id")

        if (nominal <= 0L || nominal > 1_000_000_000L) {
            return Resource.Error("Nominal transaksi harus lebih dari 0 dan maksimal Rp1.000.000.000.")
        }
        if (keterangan.length > 200) {
            return Resource.Error("Keterangan transaksi maksimal 200 karakter.")
        }

        val typeStr = if (tipe == TransactionType.PEMASUKAN) "income" else "expense"
        val catStr = mapCategoryToApiString(kategori)
        val dateStr = formatMillisToDateOnly(tanggal)

        val req = TransactionRequestDto(
            type = typeStr,
            category = catStr,
            amount = nominal,
            description = keterangan.ifBlank { null },
            trxDate = dateStr
        )

        val result = safeApiCall { api.updateTransaction(intId, req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun deleteTransaction(id: String, actor: Session): Resource<Unit> {
        if (actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya Owner yang dapat menghapus transaksi.")
        }
        if (id.startsWith("order-")) {
            return Resource.Error("Transaksi otomatis dari pesanan tidak dapat dihapus.")
        }
        val intId = id.toIntOrNull()
            ?: return Resource.Error("ID transaksi tidak valid: $id")

        return safeApiCallUnit { api.deleteTransaction(intId) }
    }

    override fun getFinanceSummary(
        actor: Session,
        range: DateRange
    ): Flow<Resource<FinanceSummary>> = flow {
        emit(Resource.Loading)
        if (actor.role != Role.OWNER) {
            emit(Resource.Error("Akses ditolak: Hanya Owner yang dapat melihat ringkasan keuangan."))
            return@flow
        }

        val fromStr = formatMillisToDateOnly(range.startMillis)
        val toStr = formatMillisToDateOnly(range.endMillis)

        val txResult = fetchAllPages { page ->
            safeApiCall { api.getTransactions(page = page, from = fromStr, to = toStr) }
        }

        if (txResult !is Resource.Success) {
            emit(Resource.Error((txResult as? Resource.Error)?.message ?: "Gagal mengambil data transaksi."))
            return@flow
        }

        val transactions = txResult.data.map { it.toDomain() }
        val filtered = FinanceCalculator.filterTransactionsByPeriod(transactions, range)

        val totalIncome = FinanceCalculator.calculateTotalIncome(filtered)
        val totalExpense = FinanceCalculator.calculateTotalExpense(filtered)
        val labaRugi = FinanceCalculator.calculateNetProfit(totalIncome, totalExpense)
        val jumlahSelesai = filtered.count { it.sumber == TransactionSource.PESANAN }
        val byCategory = FinanceCalculator.calculateTotalsByCategory(filtered)

        emit(
            Resource.Success(
                FinanceSummary(
                    totalPemasukan = totalIncome,
                    totalPengeluaran = totalExpense,
                    labaRugi = labaRugi,
                    jumlahPesananSelesai = jumlahSelesai,
                    categoryBreakdown = byCategory
                )
            )
        )
    }

    override fun getSixMonthTrend(actor: Session): Flow<Resource<SixMonthTrend>> = flow {
        emit(Resource.Loading)
        if (actor.role != Role.OWNER) {
            emit(Resource.Error("Akses ditolak: Hanya Owner yang dapat melihat tren keuangan."))
            return@flow
        }

        val allTxResult = fetchAllPages { page ->
            safeApiCall { api.getTransactions(page = page) }
        }

        if (allTxResult !is Resource.Success) {
            emit(Resource.Error((allTxResult as? Resource.Error)?.message ?: "Gagal mengambil riwayat transaksi."))
            return@flow
        }

        val serverTxList = allTxResult.data.map { it.toDomain() }

        val trend = FinanceCalculator.buildMonthlyTrend(
            now = System.currentTimeMillis(),
            manualTransactions = serverTxList,
            orders = emptyList()
        )
        emit(Resource.Success(trend))
    }

    private fun mapCategoryToApiString(kategori: TransactionCategory): String = when (kategori) {
        TransactionCategory.BAHAN_BAKU -> "Bahan Baku"
        TransactionCategory.MATERIAL_DTF -> "Material DTF"
        TransactionCategory.OPERASIONAL -> "Operasional"
        TransactionCategory.GAJI -> "Gaji"
        TransactionCategory.PENGELUARAN_LAINNYA -> "Pengeluaran Lainnya"
        TransactionCategory.PENJUALAN_LAIN -> "Penjualan Lain"
        TransactionCategory.PEMASUKAN_LAINNYA -> "Pemasukan Lainnya"
        TransactionCategory.PESANAN -> "Penjualan"
    }
}
