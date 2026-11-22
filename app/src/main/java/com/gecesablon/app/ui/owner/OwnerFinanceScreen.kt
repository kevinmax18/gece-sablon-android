package com.gecesablon.app.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.core.util.PeriodRules
import com.gecesablon.app.data.model.FinancePeriod
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.model.displayName
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorState
import com.gecesablon.app.ui.components.LoadingState
import com.gecesablon.app.ui.owner.finance.TransactionListItem
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import kotlinx.coroutines.launch

/**
 * Layar utama tab Keuangan Owner.
 * Menampilkan filter periode, ringkasan, tombol catat, dan transaksi terbaru.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerFinanceScreen(
    viewModel: OwnerFinanceViewModel,
    onNavigateToRecordIncome: () -> Unit,
    onNavigateToRecordExpense: () -> Unit,
    onNavigateToFinanceDetail: (Long, Long) -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // State untuk DateRangePicker bottom sheet
    var showDateRangePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(title = stringResource(R.string.title_owner_finance))
        },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter periode chips
            item {
                val periods = listOf(
                    FinancePeriod.BULAN_INI to stringResource(R.string.owner_finance_period_this_month),
                    FinancePeriod.BULAN_LALU to stringResource(R.string.owner_finance_period_last_month),
                    FinancePeriod.HARI_7 to stringResource(R.string.owner_finance_period_7days),
                    FinancePeriod.HARI_30 to stringResource(R.string.owner_finance_period_30days),
                    FinancePeriod.CUSTOM to stringResource(R.string.owner_finance_period_custom)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(periods) { (period, label) ->
                        FilterChip(
                            selected = uiState.selectedPeriod == period,
                            onClick = {
                                if (period == FinancePeriod.CUSTOM) {
                                    showDateRangePicker = true
                                } else {
                                    viewModel.onPeriodChange(period)
                                }
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GeceColors.PrimaryInk,
                                selectedLabelColor = GeceColors.Background
                            )
                        )
                    }
                }
            }

            // Kartu ringkasan
            item {
                when {
                    uiState.isLoading -> LoadingState()
                    uiState.errorMessage != null -> ErrorState(
                        description = uiState.errorMessage ?: "",
                        onRetry = { viewModel.loadFinance() }
                    )
                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.owner_finance_summary),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FinanceSummaryCard(
                                    label = stringResource(R.string.owner_dashboard_income),
                                    value = Formatter.formatRupiah(uiState.totalPemasukan),
                                    valueColor = GeceColors.StatusSuccessText,
                                    bgColor = GeceColors.StatusSuccessBg,
                                    modifier = Modifier.weight(1f)
                                )
                                FinanceSummaryCard(
                                    label = stringResource(R.string.owner_dashboard_expense),
                                    value = Formatter.formatRupiah(uiState.totalPengeluaran),
                                    valueColor = GeceColors.StatusCancelledText,
                                    bgColor = GeceColors.StatusCancelledBg,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            val labaPositif = uiState.labaRugi >= 0
                            FinanceSummaryCard(
                                label = if (labaPositif)
                                    stringResource(R.string.owner_dashboard_profit)
                                else
                                    stringResource(R.string.owner_dashboard_loss),
                                value = Formatter.formatRupiah(kotlin.math.abs(uiState.labaRugi)),
                                valueColor = if (labaPositif) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText,
                                bgColor = if (labaPositif) GeceColors.StatusSuccessBg else GeceColors.StatusCancelledBg,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Tombol Catat Pemasukan & Pengeluaran
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToRecordIncome,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = ShapeControl,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeceColors.StatusSuccessBg,
                            contentColor = GeceColors.StatusSuccessText
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(
                            text = stringResource(R.string.owner_finance_btn_income),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Button(
                        onClick = onNavigateToRecordExpense,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = ShapeControl,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeceColors.StatusCancelledBg,
                            contentColor = GeceColors.StatusCancelledText
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(
                            text = stringResource(R.string.owner_finance_btn_expense),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Transaksi terbaru
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.owner_finance_recent_tx),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = {
                            val range = uiState.currentRange
                            if (range != null) {
                                onNavigateToFinanceDetail(range.startMillis, range.endMillis)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.owner_finance_see_all))
                    }
                }
            }

            if (!uiState.isLoading) {
                if (uiState.recentTransactions.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.owner_finance_no_transactions),
                            style = MaterialTheme.typography.bodySmall,
                            color = GeceColors.TextMuted
                        )
                    }
                } else {
                    items(uiState.recentTransactions, key = { it.id }) { tx ->
                        TransactionListItem(
                            transaction = tx,
                            onClick = { onNavigateToTransactionDetail(tx.id) }
                        )
                    }
                }
            }

            // Tombol Laporan
            item {
                OutlinedButton(
                    onClick = onNavigateToReport,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = ShapeControl
                ) {
                    Text(stringResource(R.string.owner_finance_report))
                }
            }
        }
    }

    // DateRangePicker dalam ModalBottomSheet
    if (showDateRangePicker) {
        val pickerState = rememberDateRangePickerState(
            initialDisplayMode = DisplayMode.Picker
        )
        ModalBottomSheet(
            onDismissRequest = {
                // Dismiss tanpa memilih → pertahankan periode sebelumnya
                showDateRangePicker = false
            },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                DateRangePicker(
                    state = pickerState,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showDateRangePicker = false
                            }
                        }
                    ) { Text("Batal") }
                    TextButton(
                        onClick = {
                            val utcStart = pickerState.selectedStartDateMillis
                            val utcEnd = pickerState.selectedEndDateMillis
                            // Kedua tanggal wajib ada
                            if (utcStart != null && utcEnd != null) {
                                val localRange = PeriodRules.utcRangeToLocalDateRange(utcStart, utcEnd)
                                viewModel.onPeriodChange(
                                    period = FinancePeriod.CUSTOM,
                                    customStart = localRange.startMillis,
                                    customEnd = localRange.endMillis
                                )
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showDateRangePicker = false
                                }
                            }
                        },
                        enabled = pickerState.selectedStartDateMillis != null && pickerState.selectedEndDateMillis != null
                    ) { Text("OK") }
                }
            }
        }
    }
}

/** Kartu ringkasan nilai keuangan. */
@Composable
private fun FinanceSummaryCard(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    bgColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = GeceColors.TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}
