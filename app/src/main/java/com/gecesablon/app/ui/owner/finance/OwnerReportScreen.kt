package com.gecesablon.app.ui.owner.finance

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.FinanceCalculator
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.data.model.FinancePeriod
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.displayName
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorState
import com.gecesablon.app.ui.components.LoadingState
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl

/**
 * Layar laporan keuangan: ringkasan laba/rugi, rincian per kategori, dan jumlah pesanan selesai.
 * Tombol unduh disembunyikan sampai Tahap 6C.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerReportScreen(
    viewModel: OwnerReportViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.owner_report_title),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(innerPadding))
            uiState.errorMessage != null -> ErrorState(
                description = uiState.errorMessage ?: "",
                onRetry = { viewModel.loadReport() },
                modifier = Modifier.padding(innerPadding)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filter periode
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(FinancePeriod.values().filter { it != FinancePeriod.CUSTOM }) { period ->
                                FilterChip(
                                    selected = uiState.selectedPeriod == period,
                                    onClick = { viewModel.onPeriodChange(period) },
                                    label = { Text(period.labelText()) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GeceColors.PrimaryInk,
                                        selectedLabelColor = GeceColors.Background
                                    )
                                )
                            }
                        }
                    }

                    // Kartu Ringkasan Laba/Rugi
                    item {
                        SectionLabel(stringResource(R.string.owner_report_summary))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryCard(
                                label = stringResource(R.string.owner_dashboard_income),
                                value = Formatter.formatRupiah(uiState.totalPemasukan),
                                valueColor = GeceColors.StatusSuccessText,
                                containerColor = GeceColors.StatusSuccessBg,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                label = stringResource(R.string.owner_dashboard_expense),
                                value = Formatter.formatRupiah(uiState.totalPengeluaran),
                                valueColor = GeceColors.StatusCancelledText,
                                containerColor = GeceColors.StatusCancelledBg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val labaPositif = uiState.labaRugi >= 0
                        SummaryCard(
                            label = if (labaPositif)
                                stringResource(R.string.owner_dashboard_profit)
                            else
                                stringResource(R.string.owner_dashboard_loss),
                            value = Formatter.formatRupiah(kotlin.math.abs(uiState.labaRugi)),
                            valueColor = if (labaPositif) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText,
                            containerColor = if (labaPositif) GeceColors.StatusSuccessBg else GeceColors.StatusCancelledBg,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Jumlah Pesanan Selesai
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ShapeCard,
                            colors = CardDefaults.cardColors(containerColor = GeceColors.Surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.owner_report_completed_orders),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${uiState.pesananSelesaiCount} pesanan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GeceColors.PrimaryInk
                                )
                            }
                        }
                    }

                    // Rincian Pemasukan per Kategori
                    item {
                        SectionLabel(stringResource(R.string.owner_report_income_by_category))
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.incomeByCategory.isEmpty()) {
                            Text(
                                text = stringResource(R.string.owner_report_no_income),
                                style = MaterialTheme.typography.bodySmall,
                                color = GeceColors.TextMuted
                            )
                        } else {
                            CategoryBarList(
                                breakdown = uiState.incomeByCategory,
                                totalForType = uiState.totalPemasukan,
                                barColor = GeceColors.StatusSuccessBg,
                                valueColor = GeceColors.StatusSuccessText
                            )
                        }
                    }

                    // Rincian Pengeluaran per Kategori
                    item {
                        SectionLabel(stringResource(R.string.owner_report_expense_by_category))
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.expenseByCategory.isEmpty()) {
                            Text(
                                text = stringResource(R.string.owner_report_no_expense),
                                style = MaterialTheme.typography.bodySmall,
                                color = GeceColors.TextMuted
                            )
                        } else {
                            CategoryBarList(
                                breakdown = uiState.expenseByCategory,
                                totalForType = uiState.totalPengeluaran,
                                barColor = GeceColors.StatusCancelledBg,
                                valueColor = GeceColors.StatusCancelledText
                            )
                        }
                    }

                    // Spacer bawah
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

/** Daftar batang per kategori. */
@Composable
private fun CategoryBarList(
    breakdown: Map<TransactionCategory, Long>,
    totalForType: Long,
    barColor: Color,
    valueColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = GeceColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            breakdown.entries.forEachIndexed { index, (kategori, amount) ->
                val ratio = FinanceCalculator.calculateCategoryRatio(amount, totalForType)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = kategori.displayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = GeceColors.TextMuted
                        )
                        Text(
                            text = Formatter.formatRupiah(amount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = valueColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Batang sederhana
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(GeceColors.Border, ShapeControl)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .height(8.dp)
                                .background(barColor, ShapeControl)
                        )
                    }
                }
                if (index < breakdown.size - 1) {
                    HorizontalDivider(color = GeceColors.Border, thickness = 0.5.dp)
                }
            }
        }
    }
}

/** Kartu ringkasan kecil dengan label, nilai, dan warna. */
@Composable
private fun SummaryCard(
    label: String,
    value: String,
    valueColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = containerColor)
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

/** Label bagian laporan. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = GeceColors.TextMain
    )
}

/** Label chip periode keuangan dalam Bahasa Indonesia. */
@Composable
private fun FinancePeriod.labelText(): String = when (this) {
    FinancePeriod.BULAN_INI -> stringResource(R.string.owner_finance_period_this_month)
    FinancePeriod.BULAN_LALU -> stringResource(R.string.owner_finance_period_last_month)
    FinancePeriod.HARI_7 -> stringResource(R.string.owner_finance_period_7days)
    FinancePeriod.HARI_30 -> stringResource(R.string.owner_finance_period_30days)
    FinancePeriod.CUSTOM -> stringResource(R.string.owner_finance_period_custom)
}
