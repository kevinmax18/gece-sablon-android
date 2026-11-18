package com.gecesablon.app.ui.staff

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.R
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Filter kategori pesanan untuk tampilan staf workshop sesuai alur Figma.
 */
enum class StaffOrderFilter(@StringRes val labelRes: Int) {
    SEMUA(R.string.staff_orders_filter_all),
    BARU(R.string.staff_orders_filter_new),
    DIPROSES(R.string.staff_orders_filter_processing),
    SIAP_DIAMBIL(R.string.staff_orders_filter_ready),
    SELESAI(R.string.staff_orders_filter_done)
}

data class StaffOrdersUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: StaffOrderFilter = StaffOrderFilter.SEMUA,
    val errorMessage: String? = null
)

class StaffOrdersViewModel(
    private val orderRepository: OrderRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffOrdersUiState())
    val uiState: StateFlow<StaffOrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
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
                        val allOrders = result.data
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                orders = allOrders,
                                filteredOrders = applyFilterAndSearch(
                                    orders = allOrders,
                                    query = current.searchQuery,
                                    filter = current.selectedFilter
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

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = newQuery,
                filteredOrders = applyFilterAndSearch(
                    orders = current.orders,
                    query = newQuery,
                    filter = current.selectedFilter
                )
            )
        }
    }

    fun onFilterSelected(filter: StaffOrderFilter) {
        _uiState.update { current ->
            current.copy(
                selectedFilter = filter,
                filteredOrders = applyFilterAndSearch(
                    orders = current.orders,
                    query = current.searchQuery,
                    filter = filter
                )
            )
        }
    }

    private fun applyFilterAndSearch(
        orders: List<Order>,
        query: String,
        filter: StaffOrderFilter
    ): List<Order> {
        val trimmedQuery = query.trim().lowercase()

        return orders.filter { order ->
            // Filter query
            val matchesSearch = if (trimmedQuery.isEmpty()) {
                true
            } else {
                order.kode.lowercase().contains(trimmedQuery) ||
                        order.namaPelanggan.lowercase().contains(trimmedQuery)
            }

            // Filter status
            val matchesFilter = when (filter) {
                StaffOrderFilter.SEMUA -> true
                StaffOrderFilter.BARU -> order.status == OrderStatus.DITERIMA
                StaffOrderFilter.DIPROSES -> order.status in setOf(
                    OrderStatus.SEDANG_DIPROSES,
                    OrderStatus.CETAK_DTF,
                    OrderStatus.PRESSING
                )
                StaffOrderFilter.SIAP_DIAMBIL -> order.status == OrderStatus.SIAP_DIAMBIL
                StaffOrderFilter.SELESAI -> order.status in setOf(
                    OrderStatus.SELESAI,
                    OrderStatus.DIBATALKAN
                )
            }

            matchesSearch && matchesFilter
        }
    }

    companion object {
        fun factory(
            orderRepository: OrderRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StaffOrdersViewModel(
                    orderRepository = orderRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
