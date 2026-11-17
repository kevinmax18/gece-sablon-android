package com.gecesablon.app.ui.customer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gecesablon.app.BuildConfig
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.core.util.WhatsAppIntent
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorView
import com.gecesablon.app.ui.components.LoadingView
import com.gecesablon.app.ui.components.OrderCard
import com.gecesablon.app.ui.components.TopBarStandardActions
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeControl
import com.gecesablon.app.ui.theme.ShapePill

/**
 * Layar Tab Pesan (Daftar Pesanan Pelanggan).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    viewModel: OrderListViewModel,
    onOrderClick: (orderId: String) -> Unit,
    onCreateNewOrder: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeceColors.Background,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.title_order),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        TopBarStandardActions(
                            onChatClick = {
                                WhatsAppIntent.openChat(
                                    context = context,
                                    phoneNumber = BuildConfig.NOMOR_WA_WORKSHOP,
                                    message = context.getString(R.string.whatsapp_msg_consultation)
                                )
                            },
                            onProfileClick = onProfileClick
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GeceColors.Background
                    )
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Filter: Semua / Aktif / Selesai
            val tabs = listOf(
                OrderFilterTab.SEMUA to stringResource(R.string.order_tab_all),
                OrderFilterTab.AKTIF to stringResource(R.string.order_tab_active),
                OrderFilterTab.SELESAI to stringResource(R.string.order_tab_completed)
            )

            val selectedTabIndex = tabs.indexOfFirst { it.first == uiState.activeFilter }.let { if (it == -1) 0 else it }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = GeceColors.Background,
                contentColor = GeceColors.PrimaryInk,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = GeceColors.PrimaryInk,
                        height = 3.dp
                    )
                },
                divider = {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                }
            ) {
                tabs.forEachIndexed { index, (tab, title) ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.onFilterSelected(tab) },
                        text = {
                            Text(
                                text = title,
                                style = if (isSelected) {
                                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                } else {
                                    MaterialTheme.typography.bodyMedium
                                },
                                color = if (isSelected) GeceColors.PrimaryInk else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Konten: Loading, Error, Empty State, atau Daftar Kartu Pesanan
            when {
                uiState.isLoading -> {
                    LoadingView(modifier = Modifier.fillMaxSize())
                }
                uiState.errorMessage != null -> {
                    ErrorView(
                        message = uiState.errorMessage ?: stringResource(R.string.state_error_desc),
                        onRetry = { /* Re-fetch orders */ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.filteredOrders.isEmpty() -> {
                    val (emptyTitle, emptyDesc) = when (uiState.activeFilter) {
                        OrderFilterTab.SEMUA -> Pair(
                            stringResource(R.string.order_empty_all_title),
                            stringResource(R.string.order_empty_all_desc)
                        )
                        OrderFilterTab.AKTIF -> Pair(
                            stringResource(R.string.order_empty_active_title),
                            stringResource(R.string.order_empty_active_desc)
                        )
                        OrderFilterTab.SELESAI -> Pair(
                            stringResource(R.string.order_empty_completed_title),
                            stringResource(R.string.order_empty_completed_desc)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyState(
                            title = emptyTitle,
                            description = emptyDesc
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onCreateNewOrder,
                            shape = ShapeControl,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeceColors.Accent,
                                contentColor = GeceColors.PrimaryInk
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.order_btn_create_new),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Tombol Buat Pesanan Baru di bagian paling atas
                        item {
                            Button(
                                onClick = onCreateNewOrder,
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeceColors.PrimaryInk,
                                    contentColor = GeceColors.Background
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.order_btn_create_new),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Daftar Kartu OrderCard
                        items(
                            items = uiState.filteredOrders,
                            key = { it.id }
                        ) { order ->
                            val firstItem = order.items.firstOrNull()
                            val itemName = firstItem?.namaProduk ?: "Pesanan Sablon DTF"
                            val itemVariant = if (firstItem != null) {
                                "${firstItem.warnaNama} • Ukuran ${firstItem.ukuranLabel}"
                            } else {
                                ""
                            }
                            val quantityPcs = firstItem?.jumlah ?: 1

                            OrderCard(
                                orderCode = order.kode,
                                date = Formatter.formatDateIndo(order.dibuatPada),
                                status = order.status,
                                title = itemName,
                                itemName = itemName,
                                itemVariant = itemVariant,
                                quantityPcs = quantityPcs,
                                footerInfo = "Total: ${Formatter.formatRupiah(order.total)}",
                                onClick = { onOrderClick(order.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
