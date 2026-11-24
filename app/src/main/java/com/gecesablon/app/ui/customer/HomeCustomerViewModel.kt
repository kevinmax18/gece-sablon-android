package com.gecesablon.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State untuk layar Beranda Pelanggan.
 */
data class HomeCustomerUiState(
    val isLoading: Boolean = false,
    val featuredProducts: List<Product> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ViewModel untuk layar Beranda Pelanggan (HomeCustomerScreen).
 */
class HomeCustomerViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeCustomerUiState(isLoading = true))
    val uiState: StateFlow<HomeCustomerUiState> = _uiState.asStateFlow()

    init {
        loadFeaturedProducts()
    }

    fun loadFeaturedProducts() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            productRepository.getActiveProductsStream().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                featuredProducts = result.data,
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
                    is Resource.Loading -> { /* no-op */ }
                }
            }
        }
    }

    companion object {
        fun factory(productRepository: ProductRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeCustomerViewModel(productRepository) as T
            }
    }
}
