package com.gecesablon.app.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.PeriodRules
import com.gecesablon.app.data.model.FinancePeriod
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorState
import com.gecesablon.app.ui.components.LoadingState
import com.gecesablon.app.ui.components.OwnerHeader
import com.gecesablon.app.ui.owner.components.ExpenseBreakdownCard
import com.gecesablon.app.ui.owner.components.FinanceHeroCard
import com.gecesablon.app.ui.owner.components.FinanceSummaryCards
import com.gecesablon.app.ui.owner.components.IncomeSourceCard
import com.gecesablon.app.ui.owner.components.MonthlyTrendChart
import com.gecesablon.app.ui.owner.finance.TransactionListItem
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.BorderHairline
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import com.gecesablon.app.ui.theme.TextPrimary
import com.gecesablon.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Layar utama Tab Keuangan Owner sesuai desain Figma (docs/figma/owner/32-detail-keuangan.png).
 *
 * Urutan dari atas:
 * 1. OwnerHeader (subtitle "Keuangan")
 * 2. Judul "Detail Keuangan" dengan badge "RAHASIA" dan subjudul
 * 3. Chip periode yang bisa digulir
 * 4. FinanceHeroCard & FinanceSummaryCards
 * 5. Kartu tren 6 bulan (MonthlyTrendChart)
 * 6. ExpenseBreakdownCard("Struktur Beban") & IncomeSourceCard
 * 7. Bagian "Transaksi Keuangan": tombol catat, pencarian keterangan, filter tipe, 5 item, tombol muat lebih banyak
 * 8. Tombol "Lihat Laporan Keuangan"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerFinanceScreen(
    viewModel: OwnerFinanceViewModel,
    onNavigateToRecordIncome: () -> Unit,
    onNavigateToRecordExpense: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit,
    onNavigateToAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDateRangePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // 1. OwnerHeader dengan subtitle "Keuangan"
            OwnerHeader(
                subtitle = stringResource(R.string.owner_finance_header_subtitle),
                onProfileClick = onNavigateToAccount
            )
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
            // 2. Judul "Detail Keuangan" dengan badge "RAHASIA" dan subjudul
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.owner_finance_title_detail),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = GeceColors.TextMain
                        )

                        // Badge "RAHASIA"
                        Box(
                            modifier = Modifier
                                .background(
                                    color = GeceColors.StatusCancelledBg,
                                    shape = ShapeBadge
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.owner_finance_badge_rahasia),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = GeceColors.StatusCancelledText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.owner_finance_desc_detail),
                        style = MaterialTheme.typography.bodySmall,
                        color = GeceColors.TextMuted
                    )
                }
            }

            // 3. Chip periode (Bulan Ini, Bulan Lalu, 7 Hari, 30 Hari, Pilih Tanggal), aktif hitam
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
                                selectedLabelColor = GeceColors.Accent,
                                containerColor = GeceColors.Surface,
                                labelColor = GeceColors.TextMain
                            )
                        )
                    }
                }
            }

            // Loading / Error
            if (uiState.isLoading) {
                item { LoadingState() }
            } else if (uiState.errorMessage != null && uiState.allTransactions.isEmpty()) {
                item {
                    ErrorState(
                        description = uiState.errorMessage ?: "",
                        onRetry = { viewModel.loadFinance() }
                    )
                }
            } else {
                // 4. FinanceHeroCard lalu FinanceSummaryCards
                item {
                    FinanceHeroCard(
                        totalPemasukan = uiState.totalPemasukan,
                        totalPengeluaran = uiState.totalPengeluaran,
                        labaRugi = uiState.labaRugi,
                        marginPercentage = uiState.marginPercentage,
                        diffProfitVsPrev = uiState.diffProfitVsPrev
                    )
                }

                item {
                    FinanceSummaryCards(
                        totalPemasukan = uiState.totalPemasukan,
                        totalPengeluaran = uiState.totalPengeluaran,
                        completedOrdersCount = uiState.completedOrdersCount,
                        biggestExpenseCategory = uiState.biggestExpenseCategory,
                        incomeGrowthPercentage = uiState.incomeGrowthPercentage,
                        expenseRatioToIncome = uiState.expenseRatioToIncome
                    )
                }

                // 5. Kartu Tren 6 Bulan Terakhir
                item {
                    uiState.sixMonthTrend?.let { trend ->
                        MonthlyTrendChart(trend = trend)
                    }
                }

                // 6. ExpenseBreakdownCard("Struktur Beban") lalu IncomeSourceCard
                item {
                    ExpenseBreakdownCard(
                        title = stringResource(R.string.owner_finance_structure_title),
                        breakdown = uiState.expenseBreakdown,
                        totalExpense = uiState.totalPengeluaran
                    )
                }

                item {
                    IncomeSourceCard(sources = uiState.incomeSources)
                }

                // 7. Bagian "Transaksi Keuangan"
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = stringResource(R.string.owner_finance_tx_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.owner_finance_tx_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tombol Catat Pemasukan dan Catat Pengeluaran
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToRecordIncome,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 44.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeceColors.StatusSuccessBg,
                                    contentColor = GeceColors.StatusSuccessText
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.owner_finance_btn_income),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Button(
                                onClick = onNavigateToRecordExpense,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 44.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeceColors.StatusCancelledBg,
                                    contentColor = GeceColors.StatusCancelledText
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.owner_finance_btn_expense),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Kolom Cari Keterangan Transaksi
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.owner_finance_search_placeholder),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = ShapeControl,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InkBlack,
                                unfocusedBorderColor = BorderHairline,
                                focusedContainerColor = GeceColors.Background,
                                unfocusedContainerColor = GeceColors.Background
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Filter Chip: Semua / Pemasukan / Pengeluaran
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = uiState.filterType == null,
                                    onClick = { viewModel.onFilterTypeChange(null) },
                                    label = { Text(stringResource(R.string.owner_finance_detail_filter_all)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = InkBlack,
                                        selectedLabelColor = AccentYellow,
                                        containerColor = GeceColors.Surface,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = uiState.filterType == TransactionType.PEMASUKAN,
                                    onClick = { viewModel.onFilterTypeChange(TransactionType.PEMASUKAN) },
                                    label = { Text(stringResource(R.string.owner_finance_detail_filter_income)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GeceColors.StatusSuccessBg,
                                        selectedLabelColor = GeceColors.StatusSuccessText,
                                        containerColor = GeceColors.Surface,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = uiState.filterType == TransactionType.PENGELUARAN,
                                    onClick = { viewModel.onFilterTypeChange(TransactionType.PENGELUARAN) },
                                    label = { Text(stringResource(R.string.owner_finance_detail_filter_expense)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GeceColors.StatusCancelledBg,
                                        selectedLabelColor = GeceColors.StatusCancelledText,
                                        containerColor = GeceColors.Surface,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Daftar Transaksi (5 item, tombol muat lebih banyak)
                if (uiState.filteredTransactions.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.owner_finance_detail_empty),
                            description = stringResource(R.string.owner_finance_no_transactions)
                        )
                    }
                } else {
                    val displayedList = uiState.filteredTransactions.take(uiState.displayedTransactionsLimit)
                    items(displayedList, key = { it.id }) { tx ->
                        TransactionListItem(
                            transaction = tx,
                            onClick = { onNavigateToTransactionDetail(tx.id) }
                        )
                    }

                    if (uiState.filteredTransactions.size > uiState.displayedTransactionsLimit) {
                        item {
                            OutlinedButton(
                                onClick = { viewModel.onLoadMoreTransactions() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = ShapeControl
                            ) {
                                Text(
                                    text = stringResource(R.string.owner_finance_btn_load_more),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }

                // 8. Tombol "Lihat Laporan Keuangan"
                item {
                    Button(
                        onClick = onNavigateToReport,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = ShapeControl,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InkBlack,
                            contentColor = AccentYellow
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.owner_finance_btn_view_report),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Modal DateRangePicker
    if (showDateRangePicker) {
        val pickerState = rememberDateRangePickerState(initialDisplayMode = DisplayMode.Picker)
        ModalBottomSheet(
            onDismissRequest = { showDateRangePicker = false },
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
