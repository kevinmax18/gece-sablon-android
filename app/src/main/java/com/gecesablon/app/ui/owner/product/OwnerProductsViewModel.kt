package com.gecesablon.app.ui.owner.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductCategory
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State untuk layar daftar produk Owner.
 */
data class OwnerProductsUiState(
    val isLoading: Boolean = true,
    val allProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ProductCategory? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val productToDelete: Product? = null,
    val isActionInProgress: Boolean = false
) {
    /**
     * Daftar produk yang telah disaring berdasarkan query pencarian dan kategori.
     */
    val filteredProducts: List<Product>
        get() {
            val trimmed = searchQuery.trim()
            return allProducts.filter { prod ->
                val matchCategory = selectedCategory == null || prod.kategori == selectedCategory
                val matchQuery = trimmed.isEmpty() ||
                    prod.nama.contains(trimmed, ignoreCase = true) ||
                    prod.bahan.contains(trimmed, ignoreCase = true)
                matchCategory && matchQuery
            }
        }
}

/**
 * ViewModel untuk mengelola katalog produk pada sisi Owner:
 * memantau daftar produk, pencarian, filter kategori, sakelar aktif/nonaktif, dan penghapusan produk.
 */
class OwnerProductsViewModel(
    private val productRepository: ProductRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerProductsUiState())
    val uiState: StateFlow<OwnerProductsUiState> = _uiState.asStateFlow()

    init {
        loadAndObserveProducts()
    }

    private suspend fun getOwnerSession(): Session? {
        val session = sessionStore.session.firstOrNull()
        return if (session != null && session.role == Role.OWNER) session else null
    }

    /**
     * Memuat dan mengamati seluruh produk secara reaktif.
     */
    fun loadAndObserveProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = getOwnerSession()
            if (session == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Akses ditolak. Fitur ini hanya untuk Owner."
                    )
                }
                return@launch
            }

            productRepository.semuaProdukFlow(session).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                allProducts = result.data,
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

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: ProductCategory?) {
        _uiState.update {
            it.copy(selectedCategory = if (it.selectedCategory == category) null else category)
        }
    }

    /**
     * Mengubah status aktif/nonaktif produk melalui sakelar.
     */
    fun toggleProductActive(product: Product, newActiveState: Boolean) {
        viewModelScope.launch {
            val session = getOwnerSession() ?: return@launch
            _uiState.update { it.copy(isActionInProgress = true) }
            when (val res = productRepository.setAktif(session, product.id, newActiveState)) {
                is Resource.Success -> {
                    val statusText = if (newActiveState) "diaktifkan" else "dinonaktifkan"
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            successMessage = "Produk '${product.nama}' berhasil $statusText."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            errorMessage = res.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    /**
     * Membuka dialog konfirmasi hapus dengan menyebut nama produk.
     */
    fun requestDeleteProduct(product: Product) {
        _uiState.update { it.copy(productToDelete = product) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(productToDelete = null) }
    }

    /**
     * Menjalankan penghapusan produk setelah dikonfirmasi owner.
     */
    fun executeDeleteProduct() {
        val target = _uiState.value.productToDelete ?: return
        viewModelScope.launch {
            val session = getOwnerSession() ?: return@launch
            _uiState.update { it.copy(isActionInProgress = true, productToDelete = null) }
            when (val res = productRepository.hapus(session, target.id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            successMessage = "Produk '${target.nama}' berhasil dihapus."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            errorMessage = res.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    companion object {
        fun factory(
            productRepository: ProductRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                OwnerProductsViewModel(productRepository, sessionStore) as T
        }
    }
}
