package com.gecesablon.app.ui.owner

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import kotlinx.coroutines.launch
import androidx.compose.material3.HorizontalDivider

/**
 * Layar Dashboard Owner menampilkan ringkasan keuangan bulan ini,
 * statistik pesanan, daftar perlu perhatian, pesanan terbaru, dan aksi cepat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    viewModel: OwnerDashboardViewModel,
    onNavigateToCreateManualOrder: () -> Unit,
    onNavigateToRecordExpense: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoonMessage = "Segera hadir di versi berikutnya"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "OWNER",
                            style = MaterialTheme.typography.labelSmall,
                            color = GeceColors.TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = InkBlack)
                }
            }

            uiState.errorMessage != null && uiState.recentOrders.isEmpty() -> {
                EmptyState(
                    title = "Terjadi Kesalahan",
                    description = uiState.errorMessage ?: "Gagal memuat data dashboard.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // =========================================================
                    // Ringkasan Keuangan Bulan Ini
                    // =========================================================
                    OwnerFinanceSummarySection(
                        pemasukan = uiState.pemasukanBulanIni,
                        pengeluaran = uiState.pengeluaranBulanIni,
                        labaRugi = uiState.labaRugiBulanIni,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // =========================================================
                    // Statistik Pesanan
                    // =========================================================
                    OwnerOrderStatSection(
                        countBaru = uiState.countBaru,
                        countDiproses = uiState.countDiproses,
                        countSiapDiambil = uiState.countSiapDiambil,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // =========================================================
                    // Aksi Cepat
                    // =========================================================
                    OwnerQuickActionsSection(
                        onBuatPesanan = onNavigateToCreateManualOrder,
                        onCatatPengeluaran = onNavigateToRecordExpense,
                        onKatalogProduk = {
                            scope.launch { snackbarHostState.showSnackbar(comingSoonMessage) }
                        },
                        onTimSablon = {
                            scope.launch { snackbarHostState.showSnackbar(comingSoonMessage) }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // =========================================================
                    // Perlu Perhatian
                    // =========================================================
                    if (uiState.perluPerhatianOrders.isNotEmpty()) {
                        OwnerAttentionSection(
                            orders = uiState.perluPerhatianOrders,
                            onOrderClick = onNavigateToOrderDetail,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // =========================================================
                    // Pesanan Terbaru
                    // =========================================================
                    OwnerRecentOrdersSection(
                        orders = uiState.recentOrders,
                        onOrderClick = onNavigateToOrderDetail,
                        onSeeAll = onNavigateToOrders,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun OwnerFinanceSummarySection(
    pemasukan: Long,
    pengeluaran: Long,
    labaRugi: Long,
    modifier: Modifier = Modifier
) {
    val isProfit = labaRugi >= 0L
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = InkBlack)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Keuangan Bulan Ini",
                style = MaterialTheme.typography.labelMedium,
                color = GeceColors.TextMuted.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pemasukan",
                        style = MaterialTheme.typography.labelSmall,
                        color = GeceColors.StatusSuccessText.copy(alpha = 0.8f)
                    )
                    Text(
                        text = Formatter.formatRupiah(pemasukan),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GeceColors.StatusSuccessText
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Pengeluaran",
                        style = MaterialTheme.typography.labelSmall,
                        color = GeceColors.StatusCancelledText.copy(alpha = 0.8f)
                    )
                    Text(
                        text = Formatter.formatRupiah(pengeluaran),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GeceColors.StatusCancelledText
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = GeceColors.TextMuted.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isProfit) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = if (isProfit) "Laba Bersih" else "Rugi Bersih",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isProfit) GeceColors.StatusSuccessText.copy(alpha = 0.8f) else GeceColors.StatusCancelledText.copy(alpha = 0.8f)
                    )
                    Text(
                        text = Formatter.formatRupiah(if (isProfit) labaRugi else -labaRugi),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isProfit) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText
                    )
                }
            }
        }
    }
}

@Composable
private fun OwnerOrderStatSection(
    countBaru: Int,
    countDiproses: Int,
    countSiapDiambil: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GeceColors.Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Status Pesanan",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "Baru", value = countBaru.toString(), valueColor = MaterialTheme.colorScheme.onSurface)
                StatItem(label = "Diproses", value = countDiproses.toString(), valueColor = MaterialTheme.colorScheme.onSurface)
                StatItem(label = "Siap Diambil", value = countSiapDiambil.toString(), valueColor = GeceColors.StatusSuccessText)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GeceColors.TextMuted
        )
    }
}

@Composable
private fun OwnerQuickActionsSection(
    onBuatPesanan: () -> Unit,
    onCatatPengeluaran: () -> Unit,
    onKatalogProduk: () -> Unit,
    onTimSablon: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionCard(
                label = "Buat\nPesanan",
                icon = Icons.Default.AddShoppingCart,
                onClick = onBuatPesanan,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                label = "Catat\nPengeluaran",
                icon = Icons.Default.AttachMoney,
                onClick = onCatatPengeluaran,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                label = "Katalog\nProduk",
                icon = Icons.Default.Layers,
                onClick = onKatalogProduk,
                modifier = Modifier.weight(1f),
                isComingSoon = true
            )
            QuickActionCard(
                label = "Tim\nSablon",
                icon = Icons.Default.People,
                onClick = onTimSablon,
                modifier = Modifier.weight(1f),
                isComingSoon = true
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isComingSoon: Boolean = false
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = ShapeControl,
        colors = CardDefaults.cardColors(
            containerColor = if (isComingSoon) MaterialTheme.colorScheme.surfaceVariant
            else AccentYellow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isComingSoon) GeceColors.TextMuted else InkBlack,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isComingSoon) GeceColors.TextMuted else InkBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OwnerAttentionSection(
    orders: List<Order>,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Perlu Perhatian",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        orders.forEach { order ->
            AttentionOrderItem(
                order = order,
                onClick = { onOrderClick(order.id) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun AttentionOrderItem(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = ShapeControl,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GeceColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.kode,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = order.namaPelanggan,
                    style = MaterialTheme.typography.bodySmall,
                    color = GeceColors.TextMuted
                )
            }
            StatusChip(status = order.status)
        }
    }
}

@Composable
private fun OwnerRecentOrdersSection(
    orders: List<Order>,
    onOrderClick: (String) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pesanan Terbaru",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onSeeAll) {
                Text(
                    text = "Lihat Semua",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (orders.isEmpty()) {
            Text(
                text = "Belum ada pesanan.",
                style = MaterialTheme.typography.bodySmall,
                color = GeceColors.TextMuted,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            orders.forEach { order ->
                AttentionOrderItem(
                    order = order,
                    onClick = { onOrderClick(order.id) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun StatusChip(status: OrderStatus) {
    val (bgColor, textColor, label) = when (status) {
        OrderStatus.DITERIMA -> Triple(GeceColors.StatusPendingBg, GeceColors.StatusPendingText, "Baru")
        OrderStatus.SEDANG_DIPROSES, OrderStatus.CETAK_DTF, OrderStatus.PRESSING ->
            Triple(GeceColors.StatusProcessBg, GeceColors.StatusProcessText, "Diproses")
        OrderStatus.SIAP_DIAMBIL -> Triple(GeceColors.StatusReadyBg, GeceColors.StatusReadyText, "Siap Diambil")
        OrderStatus.SELESAI -> Triple(GeceColors.StatusSuccessBg, GeceColors.StatusSuccessText, "Selesai")
        OrderStatus.DIBATALKAN -> Triple(GeceColors.StatusCancelledBg, GeceColors.StatusCancelledText, "Dibatalkan")
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
