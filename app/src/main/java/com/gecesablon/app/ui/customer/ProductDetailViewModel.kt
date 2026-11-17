package com.gecesablon.app.ui.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State untuk layar Detail Produk.
 */
data class ProductDetailUiState(
    val productId: String = "",
    val product: Product? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk ProductDetailScreen.
 * Membaca productId dari SavedStateHandle.
 */
class ProductDetailViewModel(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle.get<String>("productId"))

    private val _uiState = MutableStateFlow(ProductDetailUiState(productId = productId, isLoading = true))
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        loadProductDetail()
    }

    fun loadProductDetail() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = productRepository.getProductById(productId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            product = result.data,
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

    companion object {
        fun factory(
            productRepository: ProductRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                ProductDetailViewModel(
                    productRepository = productRepository,
                    savedStateHandle = extras.createSavedStateHandle()
                ) as T
        }
    }
}
