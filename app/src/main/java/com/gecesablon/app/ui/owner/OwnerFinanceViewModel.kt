package com.gecesablon.app.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.DateRange
import com.gecesablon.app.core.util.PeriodRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.FinancePeriod
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

data class OwnerFinanceUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: FinancePeriod = FinancePeriod.BULAN_INI,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val totalPemasukan: Long = 0L,
    val totalPengeluaran: Long = 0L,
    val labaRugi: Long = 0L,
    val recentTransactions: List<Transaction> = emptyList(),
    val currentRange: DateRange? = null,
    val errorMessage: String? = null
)

class OwnerFinanceViewModel(
    private val financeRepository: FinanceRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerFinanceUiState())
    val uiState: StateFlow<OwnerFinanceUiState> = _uiState.asStateFlow()

    private var currentSession: Session? = null

    init {
        viewModelScope.launch {
            currentSession = sessionStore.session.firstOrNull()
            loadFinance()
        }
    }

    fun onPeriodChange(
        period: FinancePeriod,
        customStart: Long? = null,
        customEnd: Long? = null
    ) {
        _uiState.update {
            it.copy(
                selectedPeriod = period,
                customStartDate = customStart,
                customEndDate = customEnd
            )
        }
        loadFinance()
    }

    fun loadFinance() {
        viewModelScope.launch {
            val session = currentSession ?: sessionStore.session.firstOrNull()
            currentSession = session

            if (session == null || session.role != Role.OWNER) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Sesi tidak valid atau bukan Owner.")
                }
                return@launch
            }

            val state = _uiState.value
            val range: DateRange = try {
                PeriodRules.getPeriodRange(
                    period = state.selectedPeriod,
                    customStart = state.customStartDate,
                    customEnd = state.customEndDate
                )
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null, currentRange = range) }

            financeRepository.getFinanceSummary(session, range).collect { summaryResource ->
                when (summaryResource) {
                    is Resource.Success -> {
                        val summary = summaryResource.data
                        // Ambil 5 transaksi terbaru untuk pratinjau
                        financeRepository.getTransactions(session, range).collect { txResource ->
                            when (txResource) {
                                is Resource.Success -> {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            totalPemasukan = summary.totalPemasukan,
                                            totalPengeluaran = summary.totalPengeluaran,
                                            labaRugi = summary.labaRugi,
                                            recentTransactions = txResource.data.take(5),
                                            errorMessage = null
                                        )
                                    }
                                }
                                is Resource.Error -> {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            totalPemasukan = summary.totalPemasukan,
                                            totalPengeluaran = summary.totalPengeluaran,
                                            labaRugi = summary.labaRugi,
                                            errorMessage = txResource.message
                                        )
                                    }
                                }
                                else -> {}
                            }
                            // Hanya ambil emisi pertama dari transaksi
                            return@collect
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = summaryResource.message)
                        }
                    }
                    else -> {}
                }
                // Hanya ambil emisi pertama dari summary
                return@collect
            }
        }
    }

    companion object {
        fun factory(
            financeRepository: FinanceRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerFinanceViewModel(
                    financeRepository = financeRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
