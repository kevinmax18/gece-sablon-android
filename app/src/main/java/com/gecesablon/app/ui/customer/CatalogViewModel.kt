package com.gecesablon.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Category
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.repository.CategoryRepository
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
    val selectedCategoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk layar Katalog (CatalogScreen).
 * Chip filter kategori membaca dari CategoryRepository.getCategoriesWithActiveProductsFlow()
 */
class CatalogViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    initialCategoryId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CatalogUiState(
            isLoading = true,
            selectedCategoryId = initialCategoryId
        )
    )
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        observeCategories()
        observeCatalogProducts()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.getCategoriesWithActiveProductsFlow().collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { it.copy(categories = result.data) }
                }
            }
        }
    }

    private fun observeCatalogProducts() {
        viewModelScope.launch {
            productRepository.getActiveProductsStream().collect {
                loadProducts()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadProducts()
    }

    fun onCategorySelected(categoryId: String?) {
        if (_uiState.value.selectedCategoryId == categoryId) return
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadProducts()
    }

    fun loadProducts() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value
            when (val result = productRepository.searchProducts(state.searchQuery, state.selectedCategoryId)) {
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
        fun factory(
            productRepository: ProductRepository,
            categoryRepository: CategoryRepository,
            initialCategoryId: String? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CatalogViewModel(productRepository, categoryRepository, initialCategoryId) as T
                }
            }
    }
}
