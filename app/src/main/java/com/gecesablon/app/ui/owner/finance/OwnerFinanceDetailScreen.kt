package com.gecesablon.app.ui.owner.finance

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionSource
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.model.displayName
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorState
import com.gecesablon.app.ui.components.LoadingState
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard

/**
 * Layar daftar semua transaksi pada periode tertentu.
 * Mendukung filter tipe (Semua/Pemasukan/Pengeluaran) dan pencarian keterangan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerFinanceDetailScreen(
    viewModel: OwnerFinanceDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.owner_finance_detail_title),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter tipe + search
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Chip filter: Semua / Pemasukan / Pengeluaran
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.filterType == null,
                            onClick = { viewModel.onFilterTypeChange(null) },
                            label = { Text(stringResource(R.string.owner_finance_detail_filter_all)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GeceColors.PrimaryInk,
                                selectedLabelColor = GeceColors.Background
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
                                selectedLabelColor = GeceColors.StatusSuccessText
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
                                selectedLabelColor = GeceColors.StatusCancelledText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Field pencarian keterangan
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.owner_finance_detail_search_hint)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            // Konten daftar
            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> ErrorState(
                    description = uiState.errorMessage ?: "",
                    onRetry = { viewModel.loadTransactions() }
                )
                uiState.filteredTransactions.isEmpty() -> EmptyState(
                    title = stringResource(R.string.owner_finance_detail_empty),
                    description = ""
                )
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredTransactions, key = { it.id }) { tx ->
                            TransactionListItem(
                                transaction = tx,
                                onClick = { onNavigateToTransactionDetail(tx.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item baris transaksi dalam daftar.
 */
@Composable
fun TransactionListItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.tipe == TransactionType.PEMASUKAN
    val nominalColor = if (isIncome) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText
    val nominalPrefix = if (isIncome) "+" else "-"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon kategori
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isIncome) GeceColors.StatusSuccessBg else GeceColors.StatusCancelledBg,
                        shape = ShapeCard
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = nominalColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info keterangan dan kategori
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (transaction.keterangan.isBlank()) transaction.kategori.displayName()
                               else transaction.keterangan,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Badge PESANAN
                    if (transaction.sumber == TransactionSource.PESANAN) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = GeceColors.StatusPendingBg,
                                    shape = ShapeBadge
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.owner_finance_badge_order),
                                style = MaterialTheme.typography.labelSmall,
                                color = GeceColors.StatusPendingText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${transaction.kategori.displayName()} · ${Formatter.formatDateIndo(transaction.tanggal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GeceColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Nominal
            Text(
                text = "$nominalPrefix${Formatter.formatRupiah(transaction.nominal)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = nominalColor
            )
        }
    }
}
