package com.gecesablon.app.ui.customer.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val isCancelling: Boolean = false,
    val cancelSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class OrderDetailViewModel(
    private val orderId: String,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = orderRepository.getOrderById(orderId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, order = res.data)
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

    fun cancelOrder(userId: String) {
        val currentOrder = _uiState.value.order ?: return
        if (_uiState.value.isCancelling) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, errorMessage = null) }
            when (val res = orderRepository.cancelOrder(currentOrder.id, userId)) {
                is Resource.Success -> {
                    // Refresh data pesanan setelah berhasil dibatalkan
                    val refreshed = orderRepository.getOrderById(currentOrder.id)
                    val updatedOrder = (refreshed as? Resource.Success)?.data
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            order = updatedOrder ?: currentOrder,
                            cancelSuccessMessage = "Pesanan berhasil dibatalkan."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isCancelling = false, errorMessage = res.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    companion object {
        fun factory(
            orderId: String,
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OrderDetailViewModel(
                    orderId = orderId,
                    orderRepository = orderRepository
                ) as T
            }
        }
    }
}
