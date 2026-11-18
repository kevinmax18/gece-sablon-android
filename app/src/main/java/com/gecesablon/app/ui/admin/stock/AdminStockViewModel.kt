package com.gecesablon.app.ui.admin.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.StockCategory
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminStockUiState(
    val isLoading: Boolean = true,
    val items: List<StockItem> = emptyList(),
    val filteredItems: List<StockItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: StockCategory? = null,
    val adjustingItem: StockItem? = null,
    val itemToDelete: StockItem? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminStockViewModel(
    private val stockRepository: StockRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminStockUiState())
    val uiState: StateFlow<AdminStockUiState> = _uiState.asStateFlow()

    init {
        loadStock()
    }

    private suspend fun getSession(): Session? {
        return sessionStore.session.firstOrNull()
    }

    fun loadStock() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = getSession()
            if (session == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sesi telah berakhir.") }
                return@launch
            }

            stockRepository.getAllStock(session).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val items = resource.data
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                items = items,
                                filteredItems = filterItems(items, current.searchQuery, current.selectedCategory),
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredItems = filterItems(current.items, query, current.selectedCategory)
            )
        }
    }

    fun onCategorySelected(category: StockCategory?) {
        _uiState.update { current ->
            current.copy(
                selectedCategory = category,
                filteredItems = filterItems(current.items, current.searchQuery, category)
            )
        }
    }

    fun onOpenAdjustDialog(item: StockItem) {
        _uiState.update { it.copy(adjustingItem = item) }
    }

    fun onCloseAdjustDialog() {
        _uiState.update { it.copy(adjustingItem = null) }
    }

    fun onConfirmAdjust(delta: Int, keterangan: String?) {
        val target = _uiState.value.adjustingItem ?: return
        viewModelScope.launch {
            val session = getSession() ?: return@launch
            when (val res = stockRepository.updateQuantity(target.id, delta, keterangan, session)) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            adjustingItem = null,
                            successMessage = "Stok \"${target.nama}\" berhasil disesuaikan."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = res.message)
                    }
                }
            }
        }
    }

    fun onOpenDeleteDialog(item: StockItem) {
        _uiState.update { it.copy(itemToDelete = item) }
    }

    fun onCloseDeleteDialog() {
        _uiState.update { it.copy(itemToDelete = null) }
    }

    fun onConfirmDelete() {
        val target = _uiState.value.itemToDelete ?: return
        viewModelScope.launch {
            val session = getSession() ?: return@launch
            when (val res = stockRepository.deleteStock(target.id, session)) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            itemToDelete = null,
                            successMessage = "Item \"${target.nama}\" berhasil dihapus."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = res.message)
                    }
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun filterItems(
        items: List<StockItem>,
        query: String,
        category: StockCategory?
    ): List<StockItem> {
        val q = query.trim().lowercase()
        return items.filter { item ->
            val matchQuery = q.isEmpty() || item.nama.lowercase().contains(q) || item.satuan.lowercase().contains(q)
            val matchCategory = category == null || item.kategori == category
            matchQuery && matchCategory
        }
    }

    companion object {
        fun factory(
            stockRepository: StockRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminStockViewModel(stockRepository, sessionStore) as T
            }
        }
    }
}
