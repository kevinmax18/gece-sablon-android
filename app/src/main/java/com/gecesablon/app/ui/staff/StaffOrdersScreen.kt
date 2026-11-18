package com.gecesablon.app.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gecesablon.app.GeceApp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorView
import com.gecesablon.app.ui.components.LoadingView
import com.gecesablon.app.ui.components.OrderCard
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeControl

/**
 * Layar Daftar Manajemen Pesanan yang digunakan bersama oleh Admin Workshop dan Owner.
 * Netral peran tanpa menyebut kata "Admin".
 */
@Composable
fun StaffOrdersScreen(
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StaffOrdersViewModel = viewModel(
        factory = StaffOrdersViewModel.factory(
            orderRepository = GeceApp.instance.container.orderRepository,
            sessionStore = GeceApp.instance.container.sessionStore
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Top Bar Netral Peran
            StaffHeaderSection(
                title = stringResource(R.string.staff_orders_title),
                subtitle = stringResource(R.string.staff_orders_subtitle)
            )

            // Kolom Pencarian
            StaffSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Baris Chip Filter Kategori
            StaffFilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = viewModel::onFilterSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // Konten Daftar Pesanan / State Views
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        LoadingView(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.errorMessage != null -> {
                        ErrorView(
                            message = uiState.errorMessage ?: stringResource(R.string.state_error_desc),
                            onRetry = viewModel::loadOrders,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.filteredOrders.isEmpty() -> {
                        EmptyState(
                            title = stringResource(R.string.state_empty_title),
                            description = if (uiState.searchQuery.isNotBlank()) {
                                stringResource(R.string.staff_orders_empty_search)
                            } else {
                                stringResource(R.string.state_empty_desc)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.filteredOrders,
                                key = { it.id }
                            ) { order ->
                                val firstItem = order.items.firstOrNull()
                                val itemName = firstItem?.namaProduk ?: "Pesanan Sablon Custom"
                                val itemVariant = if (firstItem != null) {
                                    "${firstItem.warnaNama} • ${firstItem.ukuranLabel}"
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
                                    onClick = { onNavigateToDetail(order.id) }
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
fun StaffHeaderSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(ShapeBadge)
                    .background(GeceColors.Accent)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "WORKSHOP DTF",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = GeceColors.PrimaryInk
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StaffSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = stringResource(R.string.staff_orders_search_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.content_desc_clear_text),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = ShapeControl,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GeceColors.PrimaryInk,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun StaffFilterChips(
    selectedFilter: StaffOrderFilter,
    onFilterSelected: (StaffOrderFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StaffOrderFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = stringResource(filter.labelRes),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                shape = ShapeBadge,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GeceColors.PrimaryInk,
                    selectedLabelColor = GeceColors.Accent,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = GeceColors.PrimaryInk
                )
            )
        }
    }
}
