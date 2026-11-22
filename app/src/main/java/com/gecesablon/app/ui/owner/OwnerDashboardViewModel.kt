package com.gecesablon.app.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.DateRange
import com.gecesablon.app.core.util.FinanceCalculator
import com.gecesablon.app.core.util.PeriodRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.FinancePeriod
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.repository.FinanceRepository
import com.gecesablon.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerDashboardUiState(
    val isLoading: Boolean = true,
    // Ringkasan keuangan bulan ini
    val pemasukanBulanIni: Long = 0L,
    val pengeluaranBulanIni: Long = 0L,
    val labaRugiBulanIni: Long = 0L,
    // Hitungan pesanan
    val countBaru: Int = 0,         // DITERIMA
    val countDiproses: Int = 0,     // SEDANG_DIPROSES, CETAK_DTF, PRESSING
    val countSiapDiambil: Int = 0,  // SIAP_DIAMBIL
    // Daftar perhatian (maks 5, diurutkan terlama dulu)
    val perluPerhatianOrders: List<Order> = emptyList(),
    // Pesanan terbaru (5 teratas)
    val recentOrders: List<Order> = emptyList(),
    val errorMessage: String? = null
)

class OwnerDashboardViewModel(
    private val orderRepository: OrderRepository,
    private val financeRepository: FinanceRepository,
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

            val bulanIniRange: DateRange = try {
                PeriodRules.getPeriodRange(FinancePeriod.BULAN_INI)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                return@launch
            }

            combine(
                orderRepository.getAllOrders(session),
                financeRepository.getFinanceSummary(session, bulanIniRange)
            ) { ordersResource, summaryResource ->
                Pair(ordersResource, summaryResource)
            }.collect { (ordersResource, summaryResource) ->
                when {
                    ordersResource is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = ordersResource.message)
                        }
                    }
                    ordersResource is Resource.Success -> {
                        val orders = ordersResource.data

                        // Statistik pesanan
                        val countBaru = orders.count { it.status == OrderStatus.DITERIMA }
                        val countDiproses = orders.count {
                            it.status in setOf(
                                OrderStatus.SEDANG_DIPROSES,
                                OrderStatus.CETAK_DTF,
                                OrderStatus.PRESSING
                            )
                        }
                        val countSiapDiambil = orders.count { it.status == OrderStatus.SIAP_DIAMBIL }

                        // "Perlu Perhatian" = DITERIMA dan SIAP_DIAMBIL, diurutkan terlama (ascending dibuatPada), maks 5
                        val perluPerhatian = orders
                            .filter {
                                it.status == OrderStatus.DITERIMA ||
                                        it.status == OrderStatus.SIAP_DIAMBIL
                            }
                            .sortedBy { it.dibuatPada }
                            .take(5)

                        // Pesanan terbaru 5
                        val recentOrders = orders
                            .sortedByDescending { it.dibuatPada }
                            .take(5)

                        // Ringkasan keuangan bulan ini
                        val pemasukan: Long
                        val pengeluaran: Long
                        val labaRugi: Long
                        if (summaryResource is Resource.Success) {
                            pemasukan = summaryResource.data.totalPemasukan
                            pengeluaran = summaryResource.data.totalPengeluaran
                            labaRugi = summaryResource.data.labaRugi
                        } else {
                            pemasukan = 0L
                            pengeluaran = 0L
                            labaRugi = 0L
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                pemasukanBulanIni = pemasukan,
                                pengeluaranBulanIni = pengeluaran,
                                labaRugiBulanIni = labaRugi,
                                countBaru = countBaru,
                                countDiproses = countDiproses,
                                countSiapDiambil = countSiapDiambil,
                                perluPerhatianOrders = perluPerhatian,
                                recentOrders = recentOrders,
                                errorMessage = null
                            )
                        }
                    }
                    else -> { /* Loading, tunggu */ }
                }
            }
        }
    }

    companion object {
        fun factory(
            orderRepository: OrderRepository,
            financeRepository: FinanceRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerDashboardViewModel(
                    orderRepository = orderRepository,
                    financeRepository = financeRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
