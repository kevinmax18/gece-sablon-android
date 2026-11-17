package com.gecesablon.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductCategory
import com.gecesablon.app.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State untuk layar Katalog Pelanggan.
 */
data class CatalogUiState(
    val searchQuery: String = "",
    val selectedCategory: ProductCategory? = null,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk layar Katalog (CatalogScreen).
 */
class CatalogViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState(isLoading = true))
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadProducts()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadProducts()
    }

    fun onCategorySelected(category: ProductCategory?) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        loadProducts()
    }

    fun loadProducts() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value
            when (val result = productRepository.searchProducts(state.searchQuery, state.selectedCategory)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = result.data,
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
        fun factory(productRepository: ProductRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CatalogViewModel(productRepository) as T
            }
    }
}
