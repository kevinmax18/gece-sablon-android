package com.gecesablon.app.ui.admin

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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.ui.components.AdminTopBar
import com.gecesablon.app.ui.components.ErrorView
import com.gecesablon.app.ui.components.LoadingView
import com.gecesablon.app.ui.components.OrderCard
import com.gecesablon.app.ui.components.StatusChip
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import java.text.NumberFormat
import java.util.Locale

/**
 * Layar Dashboard Utama Admin Workshop sesuai desain antarmuka Figma 20-dashboard-admin.png.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateToCreateManualOrder: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AdminTopBar(
                tabName = "Beranda",
                onProfileClick = onNavigateToAccount
            )
        },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                LoadingView(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }
            uiState.errorMessage != null && uiState.recentOrders.isEmpty() -> {
                ErrorView(
                    message = uiState.errorMessage ?: "Terjadi kesalahan.",
                    onRetry = viewModel::loadDashboardData,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Ringkasan Status Pesanan Hari Ini (Kartu Hitungan)
                    Text(
                        text = stringResource(R.string.admin_dashboard_summary_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = stringResource(R.string.admin_dashboard_stat_new),
                            count = uiState.countBaru,
                            colorBg = GeceColors.StatusPendingBg.copy(alpha = 0.25f),
                            colorText = InkBlack,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToOrders
                        )
                        StatCard(
                            label = stringResource(R.string.admin_dashboard_stat_process),
                            count = uiState.countDiproses,
                            colorBg = InkBlack,
                            colorText = GeceColors.Background,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToOrders
                        )
                        StatCard(
                            label = stringResource(R.string.admin_dashboard_stat_ready),
                            count = uiState.countSiapDiambil,
                            colorBg = GeceColors.StatusReadyBg,
                            colorText = InkBlack,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToOrders
                        )
                        StatCard(
                            label = stringResource(R.string.admin_dashboard_stat_stock_low),
                            count = uiState.countStokMenipis,
                            colorBg = if (uiState.countStokMenipis > 0) GeceColors.StatusCancelledBg else GeceColors.Surface,
                            colorText = if (uiState.countStokMenipis > 0) GeceColors.StatusCancelledText else InkBlack,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToStock
                        )
                    }

                    // 2. Aksi Cepat
                    Text(
                        text = stringResource(R.string.admin_dashboard_quick_actions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.AddShoppingCart,
                            title = stringResource(R.string.admin_dashboard_action_create_order),
                            onClick = onNavigateToCreateManualOrder,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.ListAlt,
                            title = stringResource(R.string.admin_dashboard_action_manage_orders),
                            onClick = onNavigateToOrders,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Inventory2,
                            title = stringResource(R.string.admin_dashboard_action_manage_stock),
                            onClick = onNavigateToStock,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 3. Perlu Tindak Lanjut Segera
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.admin_dashboard_need_action),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                            if (uiState.pendingActionOrders.isNotEmpty()) {
                                TextButton(onClick = onNavigateToOrders) {
                                    Text(
                                        stringResource(R.string.admin_dashboard_see_all),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = InkBlack
                                    )
                                }
                            }
                        }

                        if (uiState.pendingActionOrders.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
                                border = BorderStroke(1.dp, GeceColors.Border)
                            ) {
                                Text(
                                    text = stringResource(R.string.admin_dashboard_need_action_empty),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeceColors.TextMuted,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                uiState.pendingActionOrders.forEach { order ->
                                    val firstItem = order.items.firstOrNull()
                                    val itemName = firstItem?.namaProduk ?: "Pesanan Sablon Custom"
                                    val itemVariant = if (firstItem != null) {
                                        "${firstItem.warnaNama} \u2022 ${firstItem.ukuranLabel}"
                                    } else {
                                        ""
                                    }
                                    val quantityPcs = firstItem?.jumlah ?: 1

                                    OrderCard(
                                        orderCode = order.kode,
                                        date = Formatter.formatDateIndo(order.dibuatPada),
                                        status = order.status,
                                        title = order.namaPelanggan,
                                        itemName = itemName,
                                        itemVariant = itemVariant,
                                        quantityPcs = quantityPcs,
                                        footerInfo = "Total: ${Formatter.formatRupiah(order.total)}",
                                        customerName = order.namaPelanggan,
                                        onClick = { onNavigateToOrderDetail(order.id) }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Informasi Stok Workshop Menipis
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.admin_dashboard_stock_info),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                            TextButton(onClick = onNavigateToStock) {
                                Text(
                                    stringResource(R.string.admin_dashboard_see_all),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InkBlack
                                )
                            }
                        }

                        if (uiState.lowStockItems.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
                                border = BorderStroke(1.dp, GeceColors.Border)
                            ) {
                                Text(
                                    text = stringResource(R.string.admin_dashboard_stock_all_safe),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeceColors.TextMuted,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(uiState.lowStockItems, key = { it.id }) { item ->
                                    LowStockCard(item = item, onClick = onNavigateToStock)
                                }
                            }
                        }
                    }

                    // 5. Pesanan Terbaru
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.admin_dashboard_recent_orders),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                            TextButton(onClick = onNavigateToOrders) {
                                Text(
                                    stringResource(R.string.admin_dashboard_see_all),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InkBlack
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.recentOrders.forEach { order ->
                                val firstItem = order.items.firstOrNull()
                                val itemName = firstItem?.namaProduk ?: "Pesanan Sablon Custom"
                                val itemVariant = if (firstItem != null) {
                                    "${firstItem.warnaNama} \u2022 ${firstItem.ukuranLabel}"
                                } else {
                                    ""
                                }
                                val quantityPcs = firstItem?.jumlah ?: 1

                                OrderCard(
                                    orderCode = order.kode,
                                    date = Formatter.formatDateIndo(order.dibuatPada),
                                    status = order.status,
                                    title = order.namaPelanggan,
                                    itemName = itemName,
                                    itemVariant = itemVariant,
                                    quantityPcs = quantityPcs,
                                    footerInfo = "Total: ${Formatter.formatRupiah(order.total)}",
                                    customerName = order.namaPelanggan,
                                    onClick = { onNavigateToOrderDetail(order.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    count: Int,
    colorBg: androidx.compose.ui.graphics.Color,
    colorText: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = colorBg),
        border = BorderStroke(1.dp, GeceColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = colorText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
        border = BorderStroke(1.dp, GeceColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = InkBlack,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = InkBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LowStockCard(
    item: StockItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(170.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
        border = BorderStroke(1.dp, GeceColors.StatusCancelledBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = GeceColors.StatusCancelledBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.admin_stock_badge_low),
                        style = MaterialTheme.typography.labelSmall,
                        color = GeceColors.StatusCancelledText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = GeceColors.StatusCancelledText,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.nama,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = InkBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sisa: ${item.jumlah} ${item.satuan}",
                style = MaterialTheme.typography.bodySmall,
                color = GeceColors.StatusCancelledText,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Min: ${item.batasMinimum} ${item.satuan}",
                style = MaterialTheme.typography.bodySmall,
                color = GeceColors.TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
