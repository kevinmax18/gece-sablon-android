package com.gecesablon.app.ui.owner.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.DateRange
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerFinanceDetailUiState(
    val isLoading: Boolean = true,
    val filterType: TransactionType? = null,  // null = Semua
    val searchQuery: String = "",
    val allTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val errorMessage: String? = null
)

class OwnerFinanceDetailViewModel(
    private val financeRepository: FinanceRepository,
    private val sessionStore: SessionStore,
    private val startMillis: Long,
    private val endMillis: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerFinanceDetailUiState())
    val uiState: StateFlow<OwnerFinanceDetailUiState> = _uiState.asStateFlow()

    private var currentSession: Session? = null

    init {
        viewModelScope.launch {
            currentSession = sessionStore.session.firstOrNull()
            loadTransactions()
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            val session = currentSession ?: sessionStore.session.firstOrNull()
            currentSession = session

            if (session == null || session.role != Role.OWNER) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Sesi tidak valid atau bukan Owner.")
                }
                return@launch
            }

            val range = try {
                DateRange(startMillis, endMillis)
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Rentang periode tidak valid.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            financeRepository.getTransactions(session, range).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val all = resource.data
                        _uiState.update {
                            val filtered = applyFilter(all, it.filterType, it.searchQuery)
                            it.copy(
                                isLoading = false,
                                allTransactions = all,
                                filteredTransactions = filtered,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun onFilterTypeChange(type: TransactionType?) {
        _uiState.update {
            val filtered = applyFilter(it.allTransactions, type, it.searchQuery)
            it.copy(filterType = type, filteredTransactions = filtered)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            val filtered = applyFilter(it.allTransactions, it.filterType, query)
            it.copy(searchQuery = query, filteredTransactions = filtered)
        }
    }

    // Terapkan filter tipe dan pencarian keterangan secara bersamaan
    private fun applyFilter(
        all: List<Transaction>,
        filterType: TransactionType?,
        query: String
    ): List<Transaction> {
        return all.filter { tx ->
            val matchTipe = filterType == null || tx.tipe == filterType
            val matchQuery = query.isBlank() || tx.keterangan.contains(query, ignoreCase = true)
            matchTipe && matchQuery
        }
    }

    companion object {
        fun factory(
            financeRepository: FinanceRepository,
            sessionStore: SessionStore,
            startMillis: Long,
            endMillis: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerFinanceDetailViewModel(
                    financeRepository = financeRepository,
                    sessionStore = sessionStore,
                    startMillis = startMillis,
                    endMillis = endMillis
                ) as T
            }
        }
    }
}
