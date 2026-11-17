package com.gecesablon.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OrderFilterTab {
    SEMUA,
    AKTIF,
    SELESAI
}

data class OrderListUiState(
    val isLoading: Boolean = true,
    val allOrders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val activeFilter: OrderFilterTab = OrderFilterTab.SEMUA,
    val errorMessage: String? = null
)

class OrderListViewModel(
    private val userId: String,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderListUiState())
    val uiState: StateFlow<OrderListUiState> = _uiState.asStateFlow()

    init {
        observeOrders()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            orderRepository.getOrdersByUserId(userId).collectLatest { res ->
                when (res) {
                    is Resource.Success -> {
                        val orders = res.data
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                allOrders = orders,
                                filteredOrders = applyFilter(orders, current.activeFilter)
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = res.message)
                        }
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    fun onFilterSelected(tab: OrderFilterTab) {
        _uiState.update { current ->
            current.copy(
                activeFilter = tab,
                filteredOrders = applyFilter(current.allOrders, tab)
            )
        }
    }

    private fun applyFilter(orders: List<Order>, filter: OrderFilterTab): List<Order> {
        return when (filter) {
            OrderFilterTab.SEMUA -> orders
            OrderFilterTab.AKTIF -> orders.filter {
                it.status != OrderStatus.SELESAI && it.status != OrderStatus.DIBATALKAN
            }
            OrderFilterTab.SELESAI -> orders.filter {
                it.status == OrderStatus.SELESAI || it.status == OrderStatus.DIBATALKAN
            }
        }
    }

    companion object {
        fun factory(
            userId: String,
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OrderListViewModel(
                    userId = userId,
                    orderRepository = orderRepository
                ) as T
            }
        }
    }
}
