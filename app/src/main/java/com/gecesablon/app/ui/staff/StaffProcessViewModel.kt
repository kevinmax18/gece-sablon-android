package com.gecesablon.app.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.ProductionRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.ProductionStage
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StaffProcessUiState(
    val isLoading: Boolean = true,
    val activeOrders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedStageFilter: ProductionStage? = null, // null = Semua Aktif
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class StaffProcessViewModel(
    private val orderRepository: OrderRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffProcessUiState())
    val uiState: StateFlow<StaffProcessUiState> = _uiState.asStateFlow()

    init {
        loadProcessOrders()
    }

    fun loadProcessOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val session = sessionStore.session.firstOrNull()
            if (session == null || (session.role != Role.ADMIN && session.role != Role.OWNER)) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Akses ditolak. Sesi staf tidak ditemukan."
                    )
                }
                return@launch
            }

            orderRepository.getAllOrders(session).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        // Hanya memuat pesanan dengan status SEDANG_DIPROSES, CETAK_DTF, PRESSING
                        val activeOrders = result.data.filter { order ->
                            order.status in setOf(
                                OrderStatus.SEDANG_DIPROSES,
                                OrderStatus.CETAK_DTF,
                                OrderStatus.PRESSING
                            )
                        }

                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                activeOrders = activeOrders,
                                filteredOrders = applyFilter(
                                    orders = activeOrders,
                                    stageFilter = current.selectedStageFilter,
                                    query = current.searchQuery
                                ),
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun onStageFilterSelected(stage: ProductionStage?) {
        _uiState.update { current ->
            current.copy(
                selectedStageFilter = stage,
                filteredOrders = applyFilter(
                    orders = current.activeOrders,
                    stageFilter = stage,
                    query = current.searchQuery
                )
            )
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = newQuery,
                filteredOrders = applyFilter(
                    orders = current.activeOrders,
                    stageFilter = current.selectedStageFilter,
                    query = newQuery
                )
            )
        }
    }

    private fun applyFilter(
        orders: List<Order>,
        stageFilter: ProductionStage?,
        query: String
    ): List<Order> {
        val trimmedQuery = query.trim().lowercase()

        return orders.filter { order ->
            // Filter pencarian
            val matchesSearch = if (trimmedQuery.isEmpty()) {
                true
            } else {
                order.kode.lowercase().contains(trimmedQuery) ||
                        order.namaPelanggan.lowercase().contains(trimmedQuery)
            }

            // Filter tahapan aktif
            val activeStage = ProductionRules.tahapanAktif(order.produksi)
            val matchesStage = stageFilter == null || activeStage == stageFilter

            matchesSearch && matchesStage
        }
    }

    companion object {
        fun factory(
            orderRepository: OrderRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StaffProcessViewModel(
                    orderRepository = orderRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
