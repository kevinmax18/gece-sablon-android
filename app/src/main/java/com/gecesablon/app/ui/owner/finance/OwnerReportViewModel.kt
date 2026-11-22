package com.gecesablon.app.ui.owner.finance

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
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerReportUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: FinancePeriod = FinancePeriod.BULAN_INI,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val totalPemasukan: Long = 0L,
    val totalPengeluaran: Long = 0L,
    val labaRugi: Long = 0L,
    val pesananSelesaiCount: Int = 0,
    // Rincian per kategori (pisah pemasukan dan pengeluaran)
    val incomeByCategory: Map<TransactionCategory, Long> = emptyMap(),
    val expenseByCategory: Map<TransactionCategory, Long> = emptyMap(),
    val currentRange: DateRange? = null,
    val errorMessage: String? = null
)

class OwnerReportViewModel(
    private val financeRepository: FinanceRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerReportUiState())
    val uiState: StateFlow<OwnerReportUiState> = _uiState.asStateFlow()

    private var currentSession: Session? = null

    init {
        viewModelScope.launch {
            currentSession = sessionStore.session.firstOrNull()
            loadReport()
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
        loadReport()
    }

    fun loadReport() {
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
                        // Pisahkan category breakdown berdasarkan tipe transaksi
                        val incomeCategories = setOf(
                            TransactionCategory.PENJUALAN_LAIN,
                            TransactionCategory.PEMASUKAN_LAINNYA,
                            TransactionCategory.PESANAN
                        )
                        val incomeByCategory = summary.categoryBreakdown
                            .filter { (k, _) -> k in incomeCategories }
                        val expenseByCategory = summary.categoryBreakdown
                            .filter { (k, _) -> k !in incomeCategories }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                totalPemasukan = summary.totalPemasukan,
                                totalPengeluaran = summary.totalPengeluaran,
                                labaRugi = summary.labaRugi,
                                pesananSelesaiCount = summary.jumlahPesananSelesai,
                                incomeByCategory = incomeByCategory,
                                expenseByCategory = expenseByCategory,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = summaryResource.message)
                        }
                    }
                    else -> {}
                }
                // Hanya ambil emisi pertama; laporan di-refresh oleh onPeriodChange
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
                return OwnerReportViewModel(
                    financeRepository = financeRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
