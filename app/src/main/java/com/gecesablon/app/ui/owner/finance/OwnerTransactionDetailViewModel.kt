package com.gecesablon.app.ui.owner.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerTransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: Transaction? = null,
    val showDeleteDialog: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

class OwnerTransactionDetailViewModel(
    private val financeRepository: FinanceRepository,
    private val sessionStore: SessionStore,
    private val transactionId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerTransactionDetailUiState())
    val uiState: StateFlow<OwnerTransactionDetailUiState> = _uiState.asStateFlow()

    private var currentSession: Session? = null

    init {
        viewModelScope.launch {
            currentSession = sessionStore.session.firstOrNull()
            loadTransaction()
        }
    }

    fun loadTransaction() {
        viewModelScope.launch {
            val session = currentSession ?: sessionStore.session.firstOrNull()
            currentSession = session

            if (session == null || session.role != Role.OWNER) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Sesi tidak valid atau bukan Owner.")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = financeRepository.getTransactionById(transactionId, session)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, transaction = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun onShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun onDeleteConfirm() {
        viewModelScope.launch {
            val session = currentSession ?: return@launch
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }

            val result = financeRepository.deleteTransaction(transactionId, session)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    companion object {
        fun factory(
            financeRepository: FinanceRepository,
            sessionStore: SessionStore,
            transactionId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerTransactionDetailViewModel(
                    financeRepository = financeRepository,
                    sessionStore = sessionStore,
                    transactionId = transactionId
                ) as T
            }
        }
    }
}
