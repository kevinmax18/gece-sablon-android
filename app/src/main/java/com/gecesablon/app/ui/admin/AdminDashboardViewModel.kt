package com.gecesablon.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.data.repository.OrderRepository
import com.gecesablon.app.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val countBaru: Int = 0,
    val countDiproses: Int = 0,
    val countSiapDiambil: Int = 0,
    val countStokMenipis: Int = 0,
    val pendingActionOrders: List<Order> = emptyList(),
    val recentOrders: List<Order> = emptyList(),
    val lowStockItems: List<StockItem> = emptyList(),
    val errorMessage: String? = null
)

class AdminDashboardViewModel(
    private val orderRepository: OrderRepository,
    private val stockRepository: StockRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private suspend fun getSession(): Session? {
        return sessionStore.session.firstOrNull()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = getSession()
            if (session == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sesi telah berakhir.") }
                return@launch
            }

            combine(
                orderRepository.getAllOrders(session),
                stockRepository.getAllStock(session)
            ) { ordersRes, stockRes ->
                Pair(ordersRes, stockRes)
            }.collect { (ordersRes, stockRes) ->
                when {
                    ordersRes is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = ordersRes.message) }
                    }
                    stockRes is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = stockRes.message) }
                    }
                    ordersRes is Resource.Success && stockRes is Resource.Success -> {
                        val allOrders = ordersRes.data
                        val allStock = stockRes.data

                        val countBaru = allOrders.count { it.status == OrderStatus.DITERIMA }
                        val countDiproses = allOrders.count {
                            it.status == OrderStatus.SEDANG_DIPROSES ||
                                    it.status == OrderStatus.CETAK_DTF ||
                                    it.status == OrderStatus.PRESSING
                        }
                        val countSiapDiambil = allOrders.count { it.status == OrderStatus.SIAP_DIAMBIL }
                        val countStokMenipis = allStock.count { it.isMenipis }

                        // Pesanan perlu tindak lanjut: DITERIMA atau SIAP_DIAMBIL (maks 5)
                        val pendingActions = allOrders.filter {
                            it.status == OrderStatus.DITERIMA || it.status == OrderStatus.SIAP_DIAMBIL
                        }.take(5)

                        // 5 Pesanan terbaru
                        val recent = allOrders.take(5)

                        // Item stok menipis
                        val lowStock = allStock.filter { it.isMenipis }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                countBaru = countBaru,
                                countDiproses = countDiproses,
                                countSiapDiambil = countSiapDiambil,
                                countStokMenipis = countStokMenipis,
                                pendingActionOrders = pendingActions,
                                recentOrders = recent,
                                lowStockItems = lowStock,
                                errorMessage = null
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun factory(
            orderRepository: OrderRepository,
            stockRepository: StockRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminDashboardViewModel(orderRepository, stockRepository, sessionStore) as T
            }
        }
    }
}
