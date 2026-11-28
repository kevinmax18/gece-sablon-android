package com.gecesablon.app.ui.admin.stock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.data.model.StockCategory
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorView
import com.gecesablon.app.ui.components.LoadingView
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStockScreen(
    viewModel: AdminStockViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.admin_stock_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = stringResource(R.string.admin_stock_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = GeceColors.TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = InkBlack
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.admin_stock_btn_add),
                            tint = InkBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GeceColors.Background
                )
            )
        },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Kolom Pencarian
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = {
                    Text(
                        text = stringResource(R.string.admin_stock_search_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GeceColors.TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GeceColors.TextMuted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Kategori Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text(stringResource(R.string.admin_stock_filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InkBlack,
                            selectedLabelColor = White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == StockCategory.BAJU_POLOS,
                        onClick = { viewModel.onCategorySelected(StockCategory.BAJU_POLOS) },
                        label = { Text(stringResource(R.string.admin_stock_filter_baju)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InkBlack,
                            selectedLabelColor = White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == StockCategory.MATERIAL_DTF,
                        onClick = { viewModel.onCategorySelected(StockCategory.MATERIAL_DTF) },
                        label = { Text(stringResource(R.string.admin_stock_filter_material)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InkBlack,
                            selectedLabelColor = White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == StockCategory.LAINNYA,
                        onClick = { viewModel.onCategorySelected(StockCategory.LAINNYA) },
                        label = { Text(stringResource(R.string.admin_stock_filter_lainnya)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InkBlack,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            // Konten Daftar Stok
            when {
                uiState.isLoading -> {
                    LoadingView(modifier = Modifier.fillMaxSize())
                }
                uiState.errorMessage != null && uiState.items.isEmpty() -> {
                    ErrorView(
                        message = uiState.errorMessage ?: "Terjadi kesalahan.",
                        onRetry = viewModel::loadStock,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.filteredItems.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.admin_stock_empty_title),
                        description = stringResource(R.string.admin_stock_empty_desc),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredItems, key = { it.id }) { item ->
                            StockCard(
                                item = item,
                                onEditClick = { onNavigateToEdit(item.id) },
                                onDeleteClick = { viewModel.onOpenDeleteDialog(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus
    uiState.itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::onCloseDeleteDialog,
            title = { Text(stringResource(R.string.admin_stock_dialog_delete_title)) },
            text = { Text(stringResource(R.string.admin_stock_dialog_delete_desc, item.nama)) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onConfirmDelete
                ) {
                    Text(
                        stringResource(R.string.admin_stock_dialog_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCloseDeleteDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun StockCard(
    item: StockItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
        border = BorderStroke(1.dp, GeceColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nama,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (item.kategori) {
                            StockCategory.BAJU_POLOS -> "Kategori: Baju Polos"
                            StockCategory.MATERIAL_DTF -> "Kategori: Material DTF"
                            StockCategory.LAINNYA -> "Kategori: Lainnya"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = GeceColors.TextMuted
                    )
                }

                if (item.isMenipis) {
                    Surface(
                        color = GeceColors.StatusCancelledBg,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.admin_stock_badge_low),
                            style = MaterialTheme.typography.labelSmall,
                            color = GeceColors.StatusCancelledText,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${item.jumlah}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isMenipis) GeceColors.StatusCancelledText else InkBlack
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.satuan,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GeceColors.TextMuted,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.admin_stock_min_threshold, item.batasMinimum, item.satuan),
                        style = MaterialTheme.typography.bodySmall,
                        color = GeceColors.TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = InkBlack
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
