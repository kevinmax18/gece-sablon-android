package com.gecesablon.app.ui.owner.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.DateRange
import com.gecesablon.app.core.util.FinanceCalculator
import com.gecesablon.app.core.util.FinanceCalculator.ExpenseCategoryBreakdown
import com.gecesablon.app.core.util.FinanceCalculator.IncomeSourcesSummary
import com.gecesablon.app.core.util.PeriodRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.FinancePeriod
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.SixMonthTrend
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.repository.FinanceRepository
import com.gecesablon.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerReportUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: FinancePeriod = FinancePeriod.BULAN_INI,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val currentRange: DateRange? = null,
    val comparisonRange: DateRange? = null,

    // FinanceHeroCard
    val totalPemasukan: Long = 0L,
    val totalPengeluaran: Long = 0L,
    val labaRugi: Long = 0L,
    val marginPercentage: Double? = null,
    val diffProfitVsPrev: Long? = null,

    // FinanceSummaryCards
    val completedOrdersCount: Int = 0,
    val biggestExpenseCategory: TransactionCategory? = null,
    val incomeGrowthPercentage: Double? = null,
    val expenseRatioToIncome: Double? = null,

    // Kinerja Finansial (MonthlyTrendChart)
    val sixMonthTrend: SixMonthTrend? = null,

    // ExpenseBreakdownCard & IncomeSourceCard
    val expenseBreakdown: List<ExpenseCategoryBreakdown> = emptyList(),
    val incomeSources: IncomeSourcesSummary = IncomeSourcesSummary(0L, 0, null, 0L, null, 0L),

    val errorMessage: String? = null
)

class OwnerReportViewModel(
    private val financeRepository: FinanceRepository,
    private val orderRepository: OrderRepository,
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
            val currentRange: DateRange = try {
                PeriodRules.getPeriodRange(
                    period = state.selectedPeriod,
                    customStart = state.customStartDate,
                    customEnd = state.customEndDate
                )
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                return@launch
            }

            val compRange: DateRange = try {
                PeriodRules.getComparisonPeriodRange(
                    period = state.selectedPeriod,
                    customStart = state.customStartDate,
                    customEnd = state.customEndDate
                )
            } catch (e: Exception) {
                currentRange
            }

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    currentRange = currentRange,
                    comparisonRange = compRange
                )
            }

            combine(
                financeRepository.getTransactions(session, currentRange),
                financeRepository.getTransactions(session, compRange),
                financeRepository.getSixMonthTrend(session),
                orderRepository.getAllOrders(session)
            ) { currentTxRes, compTxRes, trendRes, ordersRes ->
                val orders = (ordersRes as? Resource.Success<List<Order>>)?.data ?: emptyList()
                val currentTxs = (currentTxRes as? Resource.Success<List<Transaction>>)?.data ?: emptyList()
                val compTxs = (compTxRes as? Resource.Success<List<Transaction>>)?.data ?: emptyList()
                val trend = (trendRes as? Resource.Success<SixMonthTrend>)?.data

                val totalIncome = FinanceCalculator.calculateTotalIncome(currentTxs)
                val totalExpense = FinanceCalculator.calculateTotalExpense(currentTxs)
                val netProfit = FinanceCalculator.calculateNetProfit(totalIncome, totalExpense)
                val marginPct = if (totalIncome > 0L) {
                    (netProfit.toDouble() / totalIncome.toDouble()) * 100.0
                } else null

                val compIncome = FinanceCalculator.calculateTotalIncome(compTxs)
                val compExpense = FinanceCalculator.calculateTotalExpense(compTxs)
                val compProfit = FinanceCalculator.calculateNetProfit(compIncome, compExpense)
                val hasCompData = compIncome > 0L || compExpense > 0L
                val diffProfitVsPrev = if (hasCompData) netProfit - compProfit else null

                val growthPct = if (compIncome > 0L) {
                    ((totalIncome - compIncome).toDouble() / compIncome.toDouble()) * 100.0
                } else null

                val expenseRatio = if (totalIncome > 0L) {
                    (totalExpense.toDouble() / totalIncome.toDouble()) * 100.0
                } else null

                val countOrders = FinanceCalculator.countCompletedOrders(orders, currentRange)

                val breakdown = FinanceCalculator.expenseBreakdown(
                    currentTxs,
                    currentRange.startMillis,
                    currentRange.endMillis
                )
                val biggestCat = breakdown.firstOrNull()?.category

                val sources = FinanceCalculator.incomeSources(
                    currentTxs,
                    orders,
                    currentRange.startMillis,
                    currentRange.endMillis
                )

                _uiState.update { st ->
                    st.copy(
                        isLoading = false,
                        totalPemasukan = totalIncome,
                        totalPengeluaran = totalExpense,
                        labaRugi = netProfit,
                        marginPercentage = marginPct,
                        diffProfitVsPrev = diffProfitVsPrev,
                        completedOrdersCount = countOrders,
                        biggestExpenseCategory = biggestCat,
                        incomeGrowthPercentage = growthPct,
                        expenseRatioToIncome = expenseRatio,
                        sixMonthTrend = trend,
                        expenseBreakdown = breakdown,
                        incomeSources = sources,
                        errorMessage = null
                    )
                }
            }.collect {}
        }
    }

    companion object {
        fun factory(
            financeRepository: FinanceRepository,
            orderRepository: OrderRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerReportViewModel(
                    financeRepository = financeRepository,
                    orderRepository = orderRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
