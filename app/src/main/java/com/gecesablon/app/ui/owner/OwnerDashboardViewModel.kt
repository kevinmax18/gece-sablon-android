package com.gecesablon.app.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.DateRange
import com.gecesablon.app.core.util.PeriodRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.FinancePeriod
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.SixMonthTrend
import com.gecesablon.app.data.model.StockCategory
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.repository.FinanceRepository
import com.gecesablon.app.data.repository.OrderRepository
import com.gecesablon.app.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerDashboardUiState(
    val isLoading: Boolean = true,
    val ownerName: String = "Owner",
    val currentMonthRangeText: String = "",
    // Ringkasan keuangan bulan ini
    val pemasukanBulanIni: Long = 0L,
    val pengeluaranBulanIni: Long = 0L,
    val labaRugiBulanIni: Long = 0L,
    val marginBulanIni: Double? = null,
    val jumlahPesananSelesaiBulanIni: Int = 0,
    val biggestExpenseCategory: TransactionCategory? = null,
    // Tren 6 bulan
    val sixMonthTrend: SixMonthTrend? = null,
    // Hitungan status produksi workshop (2x2)
    val countPesananMasuk: Int = 0,      // DITERIMA
    val countSedangDiproses: Int = 0,    // SEDANG_DIPROSES
    val countCetakDanPress: Int = 0,     // CETAK_DTF + PRESSING
    val countSiapDiambil: Int = 0,       // SIAP_DIAMBIL
    val countPesananAktif: Int = 0,      // Total selain SELESAI dan DIBATALKAN
    // Pesanan terbaru (3-5 item)
    val recentOrders: List<Order> = emptyList(),
    // Status stok material (maks 2 per grup, diutamakan menipis)
    val stockBahanProduk: List<StockItem> = emptyList(),
    val stockKonsumabelDtf: List<StockItem> = emptyList(),
    val errorMessage: String? = null
)

class OwnerDashboardViewModel(
    private val orderRepository: OrderRepository,
    private val financeRepository: FinanceRepository,
    private val stockRepository: StockRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerDashboardUiState())
    val uiState: StateFlow<OwnerDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private suspend fun getSession(): Session? = sessionStore.session.firstOrNull()

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val session = getSession()
            if (session == null || session.role != Role.OWNER) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Sesi tidak valid atau bukan Owner.")
                }
                return@launch
            }

            val ownerName = session.name.ifBlank { "Owner" }

            val bulanIniRange: DateRange = try {
                PeriodRules.getPeriodRange(FinancePeriod.BULAN_INI)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                return@launch
            }

            val cal = java.util.Calendar.getInstance()
            val maxDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
            val monthStr = monthNames[cal.get(java.util.Calendar.MONTH)]
            val yearStr = cal.get(java.util.Calendar.YEAR)
            val rangeText = "1 - $maxDay $monthStr $yearStr"

            combine(
                orderRepository.getAllOrders(session),
                financeRepository.getFinanceSummary(session, bulanIniRange),
                financeRepository.getSixMonthTrend(session),
                stockRepository.getAllStock(session)
            ) { ordersRes, summaryRes, trendRes, stockRes ->
                DashboardData(ordersRes, summaryRes, trendRes, stockRes)
            }.collect { data ->
                val ordersRes = data.orders
                val summaryRes = data.summary
                val trendRes = data.trend
                val stockRes = data.stock

                if (ordersRes is Resource.Error) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = ordersRes.message) }
                    return@collect
                }

                if (ordersRes is Resource.Success) {
                    val orders = ordersRes.data

                    // Hitungan produksi 2x2
                    val countMasuk = orders.count { it.status == OrderStatus.DITERIMA }
                    val countDiproses = orders.count { it.status == OrderStatus.SEDANG_DIPROSES }
                    val countCetakPress = orders.count {
                        it.status == OrderStatus.CETAK_DTF || it.status == OrderStatus.PRESSING
                    }
                    val countSiapAmbil = orders.count { it.status == OrderStatus.SIAP_DIAMBIL }
                    val countAktif = orders.count {
                        it.status != OrderStatus.SELESAI && it.status != OrderStatus.DIBATALKAN
                    }

                    // Pesanan terbaru (3-5 entri)
                    val recent = orders.sortedByDescending { it.dibuatPada }.take(5)

                    // Finansial bulan ini
                    val pemasukan: Long
                    val pengeluaran: Long
                    val labaRugi: Long
                    val jumlahSelesai: Int
                    var biggestCat: TransactionCategory? = null

                    if (summaryRes is Resource.Success) {
                        pemasukan = summaryRes.data.totalPemasukan
                        pengeluaran = summaryRes.data.totalPengeluaran
                        labaRugi = summaryRes.data.labaRugi
                        jumlahSelesai = summaryRes.data.jumlahPesananSelesai

                        // Cari kategori pengeluaran terbesar
                        val expenseCategories = setOf(
                            TransactionCategory.BAHAN_BAKU,
                            TransactionCategory.MATERIAL_DTF,
                            TransactionCategory.OPERASIONAL,
                            TransactionCategory.GAJI,
                            TransactionCategory.PENGELUARAN_LAINNYA
                        )
                        biggestCat = summaryRes.data.categoryBreakdown
                            .filter { it.key in expenseCategories && it.value > 0L }
                            .maxByOrNull { it.value }
                            ?.key
                    } else {
                        pemasukan = 0L
                        pengeluaran = 0L
                        labaRugi = 0L
                        jumlahSelesai = 0
                    }

                    val marginBulanIni = if (pemasukan > 0L) {
                        (labaRugi.toDouble() / pemasukan.toDouble()) * 100.0
                    } else {
                        null
                    }

                    val sixMonthTrend = if (trendRes is Resource.Success) trendRes.data else null

                    // Status stok material (maks 2 per grup, diutamakan yang menipis)
                    val stockItems = if (stockRes is Resource.Success) stockRes.data else emptyList()
                    val bahanList = stockItems
                        .filter { it.kategori == StockCategory.BAJU_POLOS }
                        .sortedWith(compareByDescending<StockItem> { it.isMenipis }.thenBy { it.nama })
                        .take(2)
                    val dtfList = stockItems
                        .filter { it.kategori == StockCategory.MATERIAL_DTF }
                        .sortedWith(compareByDescending<StockItem> { it.isMenipis }.thenBy { it.nama })
                        .take(2)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            ownerName = ownerName,
                            currentMonthRangeText = rangeText,
                            pemasukanBulanIni = pemasukan,
                            pengeluaranBulanIni = pengeluaran,
                            labaRugiBulanIni = labaRugi,
                            marginBulanIni = marginBulanIni,
                            jumlahPesananSelesaiBulanIni = jumlahSelesai,
                            biggestExpenseCategory = biggestCat,
                            sixMonthTrend = sixMonthTrend,
                            countPesananMasuk = countMasuk,
                            countSedangDiproses = countDiproses,
                            countCetakDanPress = countCetakPress,
                            countSiapDiambil = countSiapAmbil,
                            countPesananAktif = countAktif,
                            recentOrders = recent,
                            stockBahanProduk = bahanList,
                            stockKonsumabelDtf = dtfList,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    private data class DashboardData(
        val orders: Resource<List<Order>>,
        val summary: Resource<com.gecesablon.app.data.model.FinanceSummary>,
        val trend: Resource<SixMonthTrend>,
        val stock: Resource<List<StockItem>>
    )

    companion object {
        fun factory(
            orderRepository: OrderRepository,
            financeRepository: FinanceRepository,
            stockRepository: StockRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerDashboardViewModel(
                    orderRepository = orderRepository,
                    financeRepository = financeRepository,
                    stockRepository = stockRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
