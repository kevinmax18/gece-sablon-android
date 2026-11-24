package com.gecesablon.app.ui.owner.finance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.core.util.PeriodRules
import com.gecesablon.app.data.model.FinancePeriod
import com.gecesablon.app.ui.components.AppButtonVariant
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorState
import com.gecesablon.app.ui.components.GeceButton
import com.gecesablon.app.ui.components.LoadingState
import com.gecesablon.app.ui.components.OwnerHeader
import com.gecesablon.app.ui.owner.components.ExpenseBreakdownCard
import com.gecesablon.app.ui.owner.components.FinanceHeroCard
import com.gecesablon.app.ui.owner.components.FinanceSummaryCards
import com.gecesablon.app.ui.owner.components.IncomeSourceCard
import com.gecesablon.app.ui.owner.components.MonthlyTrendChart
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.BorderHairline
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import com.gecesablon.app.ui.theme.SurfaceCard
import com.gecesablon.app.ui.theme.TextPrimary
import com.gecesablon.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Layar Laporan Keuangan Owner sesuai desain Figma (docs/figma/owner/31-laporan-keuangan.png).
 *
 * Urutan dari atas:
 * 1. OwnerHeader(subtitle "Laporan", showBack = true)
 * 2. Judul "Laporan Keuangan" dengan subjudul "Analisis pemasukan, pengeluaran, dan laba GeCe"
 * 3. Kartu info: "Khusus Owner. Laporan ini tidak dapat diakses Admin maupun tim produksi."
 * 4. Chip periode (state sendiri) dan bilah "Periode Laporan: <tanggal mulai> - <tanggal akhir>"
 * 5. FinanceHeroCard, FinanceSummaryCards, kartu "Kinerja Finansial" (MonthlyTrendChart),
 *    ExpenseBreakdownCard("Rincian Pengeluaran"), IncomeSourceCard
 * 6. Bagian Ekspor Laporan: Tombol "Unduh PDF" dan "Unduh Excel (CSV)" (Tahap 6C).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerReportScreen(
    viewModel: OwnerReportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val snackbarHostState = remember { SnackbarHostState() }

    var showDateRangePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // ActivityResultContracts.CreateDocument untuk PDF dan CSV
    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        viewModel.exportToPdf(uri, contentResolver)
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        viewModel.exportToCsv(uri, contentResolver)
    }

    // Feedback pesan berhasil / gagal via Snackbar
    LaunchedEffect(uiState.exportSuccessMessage) {
        uiState.exportSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessages()
        }
    }

    LaunchedEffect(uiState.exportErrorMessage) {
        uiState.exportErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // 1. OwnerHeader dengan subtitle "Laporan" dan showBack = true
            OwnerHeader(
                subtitle = stringResource(R.string.owner_report_header_subtitle),
                showBack = true,
                onBack = onNavigateBack,
                onProfileClick = onNavigateToAccount
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. Judul "Laporan Keuangan" & Subjudul
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.owner_report_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.owner_report_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // 3. Kartu Info: Khusus Owner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(0.5.dp, BorderHairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(InkBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AccentYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.owner_report_info_banner),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Chip periode dan bilah "Periode Laporan: <tanggal mulai> - <tanggal akhir>"
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                    selectedContainerColor = InkBlack,
                                    selectedLabelColor = AccentYellow,
                                    containerColor = GeceColors.Surface,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }

                    // Bilah Periode Laporan
                    if (uiState.currentRange != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ShapeControl,
                            color = SurfaceCard,
                            border = BorderStroke(0.5.dp, BorderHairline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.owner_report_period_bar,
                                        Formatter.formatDateIndo(uiState.currentRange!!.startMillis),
                                        Formatter.formatDateIndo(uiState.currentRange!!.endMillis)
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Konten Laporan
            if (uiState.isLoading) {
                item { LoadingState() }
            } else if (uiState.errorMessage != null) {
                item {
                    ErrorState(
                        description = uiState.errorMessage ?: "",
                        onRetry = { viewModel.loadReport() }
                    )
                }
            } else {
                // 5. FinanceHeroCard, FinanceSummaryCards, Kinerja Finansial, ExpenseBreakdownCard, IncomeSourceCard
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

                item {
                    uiState.sixMonthTrend?.let { trend ->
                        MonthlyTrendChart(
                            trend = trend,
                            title = stringResource(R.string.owner_report_financial_perf_title)
                        )
                    }
                }

                item {
                    ExpenseBreakdownCard(
                        title = stringResource(R.string.owner_report_expense_breakdown_title),
                        breakdown = uiState.expenseBreakdown,
                        totalExpense = uiState.totalPengeluaran
                    )
                }

                item {
                    IncomeSourceCard(sources = uiState.incomeSources)
                }

                // 6. Bagian Ekspor Laporan (Tahap 6C)
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.owner_report_export_section_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isAnyExporting = uiState.isExportingPdf || uiState.isExportingCsv

                            // Tombol Unduh PDF
                            GeceButton(
                                text = stringResource(R.string.owner_report_btn_download_pdf),
                                onClick = {
                                    val suggestedName = viewModel.getSuggestedFileName("pdf")
                                    pdfExportLauncher.launch(suggestedName)
                                },
                                modifier = Modifier.weight(1f),
                                variant = AppButtonVariant.Primary,
                                loading = uiState.isExportingPdf,
                                enabled = !isAnyExporting,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                fullWidth = false
                            )

                            // Tombol Unduh Excel (CSV)
                            GeceButton(
                                text = stringResource(R.string.owner_report_btn_download_csv),
                                onClick = {
                                    val suggestedName = viewModel.getSuggestedFileName("csv")
                                    csvExportLauncher.launch(suggestedName)
                                },
                                modifier = Modifier.weight(1f),
                                variant = AppButtonVariant.Accent,
                                loading = uiState.isExportingCsv,
                                enabled = !isAnyExporting,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                fullWidth = false
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
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
