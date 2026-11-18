package com.gecesablon.app.ui.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gecesablon.app.GeceApp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.ProductionRules
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.ProductionStage
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorView
import com.gecesablon.app.ui.components.LoadingView
import com.gecesablon.app.ui.components.StatusChip
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl

/**
 * Layar Proses Workshop untuk memantau antrean produksi pesanan aktif.
 * Hanya memuat pesanan dengan status SEDANG_DIPROSES, CETAK_DTF, dan PRESSING.
 * Netral peran tanpa kata "Admin".
 */
@Composable
fun StaffProcessScreen(
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StaffProcessViewModel = viewModel(
        factory = StaffProcessViewModel.factory(
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
            // Header Netral Peran
            StaffHeaderSection(
                title = stringResource(R.string.staff_process_title),
                subtitle = stringResource(R.string.staff_process_subtitle)
            )

            // Kolom Pencarian
            StaffSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Chip Berdasarkan Tahapan Aktif
            StaffStageFilterChips(
                selectedStage = uiState.selectedStageFilter,
                onStageSelected = viewModel::onStageFilterSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // Konten Antrean Proses
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
                            onRetry = viewModel::loadProcessOrders,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.filteredOrders.isEmpty() -> {
                        EmptyState(
                            title = stringResource(R.string.state_empty_title),
                            description = stringResource(R.string.staff_process_empty_desc),
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
                                ProcessOrderCard(
                                    order = order,
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
fun StaffStageFilterChips(
    selectedStage: ProductionStage?,
    onStageSelected: (ProductionStage?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip "Semua Aktif"
        val isAllSelected = selectedStage == null
        FilterChip(
            selected = isAllSelected,
            onClick = { onStageSelected(null) },
            label = {
                Text(
                    text = stringResource(R.string.staff_process_filter_all_active),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
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
                selected = isAllSelected,
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = GeceColors.PrimaryInk
            )
        )

        // Chip per 5 tahapan produksi
        ProductionStage.orderedStages.forEach { stage ->
            val isSelected = selectedStage == stage
            FilterChip(
                selected = isSelected,
                onClick = { onStageSelected(stage) },
                label = {
                    Text(
                        text = stringResource(stage.titleRes),
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

@Composable
fun ProcessOrderCard(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeStage = ProductionRules.tahapanAktif(order.produksi)
    val completedCount = order.produksi.tahapanSelesai.size
    val progressFraction = completedCount / 5f
    val firstItem = order.items.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeCard)
            .clickable(onClick = onClick),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Baris Atas: Kode Pesanan + StatusChip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(ShapeBadge)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = order.kode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nama Pemesan & Ringkasan Item
            Text(
                text = order.namaPelanggan,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (firstItem != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${firstItem.namaProduk} • ${firstItem.jumlah} pcs (${firstItem.warnaNama})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kotak Tahapan Aktif & Progres Produksi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeControl)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tahapan Aktif Saat Ini",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (activeStage != null) {
                                "${activeStage.nomor}. ${stringResource(activeStage.titleRes)}"
                            } else {
                                "Tahapan Selesai (Siap Diambil)"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(ShapeBadge)
                            .background(GeceColors.Accent)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.staff_process_progress_format, completedCount),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GeceColors.PrimaryInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(ShapeControl),
                    color = GeceColors.PrimaryInk,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Baris Penanda Kesiapan Material
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MaterialBadge(
                        label = stringResource(R.string.staff_process_mat_shirt),
                        isReady = order.produksi.bajuTiba
                    )
                    MaterialBadge(
                        label = stringResource(R.string.staff_process_mat_dtf),
                        isReady = order.produksi.dtfTiba
                    )
                    MaterialBadge(
                        label = stringResource(R.string.staff_process_mat_ready),
                        isReady = order.produksi.siapDisablon
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.order_action_detail),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MaterialBadge(
    label: String,
    isReady: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isReady) GeceColors.StatusSuccessBg else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isReady) GeceColors.StatusSuccessText else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(ShapeBadge)
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = "$label: ${if (isReady) stringResource(R.string.staff_process_mat_yes) else stringResource(R.string.staff_process_mat_no)}",
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
