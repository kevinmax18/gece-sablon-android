package com.gecesablon.app.ui.owner.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.data.model.TransactionSource
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.model.displayName
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.components.ErrorState
import com.gecesablon.app.ui.components.LoadingState
import com.gecesablon.app.ui.theme.ErrorRed
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import com.gecesablon.app.ui.theme.White

/**
 * Layar detail satu transaksi keuangan.
 * Transaksi MANUAL: tampilkan tombol Ubah dan Hapus.
 * Transaksi PESANAN: tampilkan tombol Lihat Pesanan.
 */
@Composable
fun OwnerTransactionDetailScreen(
    viewModel: OwnerTransactionDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToOrderDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigasi balik saat transaksi berhasil dihapus
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onNavigateBack()
    }

    // Snackbar error
    LaunchedEffect(uiState.errorMessage) {
        if (!uiState.errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(uiState.errorMessage!!)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.owner_tx_detail_title),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(innerPadding))
            uiState.errorMessage != null && uiState.transaction == null -> ErrorState(
                description = uiState.errorMessage ?: "",
                onRetry = { viewModel.loadTransaction() },
                modifier = Modifier.padding(innerPadding)
            )
            uiState.transaction != null -> {
                val tx = uiState.transaction!!
                val isIncome = tx.tipe == TransactionType.PEMASUKAN
                val nominalColor = if (isIncome) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText
                val nominalBg = if (isIncome) GeceColors.StatusSuccessBg else GeceColors.StatusCancelledBg
                val nominalPrefix = if (isIncome) "+" else "-"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Kartu nominal utama
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = nominalBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Badge tipe
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isIncome) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText,
                                        shape = ShapeBadge
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isIncome)
                                        stringResource(R.string.owner_finance_detail_filter_income).uppercase()
                                    else
                                        stringResource(R.string.owner_finance_detail_filter_expense).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "$nominalPrefix${Formatter.formatRupiah(tx.nominal)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = nominalColor,
                                fontSize = 28.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatter.formatDateIndo(tx.tanggal),
                                style = MaterialTheme.typography.bodySmall,
                                color = GeceColors.TextMuted
                            )
                        }
                    }

                    // Kartu rincian
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ShapeCard,
                        colors = CardDefaults.cardColors(containerColor = GeceColors.Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow(
                                label = stringResource(R.string.owner_tx_detail_category),
                                value = tx.kategori.displayName()
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GeceColors.Border)
                            DetailRow(
                                label = stringResource(R.string.owner_tx_detail_note),
                                value = tx.keterangan.ifBlank { "-" }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GeceColors.Border)
                            DetailRow(
                                label = stringResource(R.string.owner_tx_detail_source),
                                value = if (tx.sumber == TransactionSource.MANUAL)
                                    stringResource(R.string.owner_tx_detail_source_manual)
                                else
                                    stringResource(R.string.owner_tx_detail_source_order)
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GeceColors.Border)
                            DetailRow(
                                label = stringResource(R.string.owner_tx_detail_created_by),
                                value = tx.dibuatOleh
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tombol aksi berdasarkan sumber
                    if (tx.sumber == TransactionSource.MANUAL) {
                        // Tombol Ubah (outlined) + Hapus (filled merah)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToEdit(tx.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = ShapeControl
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(stringResource(R.string.owner_tx_detail_btn_edit))
                            }
                            Button(
                                onClick = { viewModel.onShowDeleteDialog(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ErrorRed,
                                    contentColor = White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(stringResource(R.string.owner_tx_detail_btn_delete))
                            }
                        }
                    } else if (tx.sumber == TransactionSource.PESANAN && tx.orderId != null) {
                        // Transaksi dari pesanan: badge pesanan + tombol Lihat Pesanan
                        Box(
                            modifier = Modifier
                                .background(GeceColors.StatusPendingBg, ShapeBadge)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.owner_finance_badge_order),
                                style = MaterialTheme.typography.labelSmall,
                                color = GeceColors.StatusPendingText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = { onNavigateToOrderDetail(tx.orderId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = ShapeControl
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(stringResource(R.string.owner_tx_detail_btn_view_order))
                        }
                    }
                }

                // Dialog konfirmasi hapus
                if (uiState.showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.onShowDeleteDialog(false) },
                        title = { Text(stringResource(R.string.owner_tx_detail_delete_confirm_title)) },
                        text = { Text(stringResource(R.string.owner_tx_detail_delete_confirm_msg)) },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.onDeleteConfirm() },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                            ) {
                                Text(stringResource(R.string.owner_tx_detail_delete_btn_confirm), color = White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.onShowDeleteDialog(false) }) {
                                Text(stringResource(R.string.owner_tx_detail_delete_btn_cancel))
                            }
                        }
                    )
                }
            }
        }
    }
}

/** Baris label–nilai untuk kartu rincian. */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GeceColors.TextMuted,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}
