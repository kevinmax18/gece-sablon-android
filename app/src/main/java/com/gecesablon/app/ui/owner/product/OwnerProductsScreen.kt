package com.gecesablon.app.ui.owner.product

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductCategory
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.ErrorState
import com.gecesablon.app.ui.components.LoadingState
import com.gecesablon.app.ui.components.OwnerHeader
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.BorderHairline
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import com.gecesablon.app.ui.theme.SurfaceCard
import com.gecesablon.app.ui.theme.SurfaceVariantColor
import com.gecesablon.app.ui.theme.TextPrimary
import com.gecesablon.app.ui.theme.TextSecondary
import com.gecesablon.app.ui.theme.White

/**
 * Layar Kelola Katalog Produk untuk Owner:
 * - Menampilkan daftar produk dengan kartu, sakelar aktif/nonaktif, edit, dan hapus.
 * - Pencarian dan filter chip berdasarkan kategori.
 * - Tombol FAB tambah produk baru.
 * - Dialog konfirmasi hapus yang menyebut nama produk secara eksplisit.
 */
@Composable
fun OwnerProductsScreen(
    viewModel: OwnerProductsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Feedback pesan error atau sukses
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

    // Dialog Konfirmasi Hapus Produk yang menyebut nama produk secara eksplisit
    uiState.productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = {
                Text(
                    text = stringResource(R.string.owner_product_delete_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.owner_product_delete_dialog_msg, product.nama),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::executeDeleteProduct) {
                    Text(
                        text = stringResource(R.string.owner_product_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text(
                        text = stringResource(R.string.owner_product_delete_cancel),
                        color = TextSecondary
                    )
                }
            },
            containerColor = SurfaceCard,
            shape = ShapeCard
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeceColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            OwnerHeader(
                title = stringResource(R.string.owner_header_title),
                subtitle = stringResource(R.string.owner_products_header_subtitle),
                showBack = true,
                onBack = onNavigateBack,
                onProfileClick = onNavigateToAccount
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = AccentYellow,
                contentColor = InkBlack,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.owner_products_btn_add)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // =========================================================================
            // 1. Bar Pencarian
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.owner_products_search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.content_desc_clear_text),
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = ShapeControl,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = InkBlack,
                        unfocusedBorderColor = BorderHairline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // =========================================================================
            // 2. Chip Filter Kategori
            // =========================================================================
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Opsi "Semua Kategori"
                item {
                    val isSelected = uiState.selectedCategory == null
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = {
                            Text(
                                text = stringResource(R.string.owner_products_category_all),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InkBlack,
                            selectedLabelColor = White,
                            containerColor = SurfaceCard,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) InkBlack else BorderHairline
                        ),
                        shape = ShapeControl
                    )
                }

                items(ProductCategory.entries.toTypedArray()) { category ->
                    val isSelected = uiState.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = {
                            Text(
                                text = stringResource(category.titleRes),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InkBlack,
                            selectedLabelColor = White,
                            containerColor = SurfaceCard,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) InkBlack else BorderHairline
                        ),
                        shape = ShapeControl
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // =========================================================================
            // 3. Status Layar: Loading / Error / Empty / Daftar Produk
            // =========================================================================
            when {
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier.fillMaxSize(),
                        message = stringResource(R.string.state_loading)
                    )
                }

                uiState.errorMessage != null && uiState.allProducts.isEmpty() -> {
                    ErrorState(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.state_error_title),
                        description = uiState.errorMessage ?: stringResource(R.string.state_error_desc),
                        onRetry = viewModel::loadAndObserveProducts
                    )
                }

                uiState.filteredProducts.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        title = stringResource(R.string.owner_products_empty_title),
                        description = stringResource(R.string.owner_products_empty_desc),
                        actionText = stringResource(R.string.owner_products_btn_add),
                        onActionClick = onNavigateToAdd
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredProducts,
                            key = { it.id }
                        ) { product ->
                            OwnerProductItemCard(
                                product = product,
                                onEditClick = { onNavigateToEdit(product.id) },
                                onDeleteClick = { viewModel.requestDeleteProduct(product) },
                                onToggleActive = { newActive ->
                                    viewModel.toggleProductActive(product, newActive)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kartu baris produk pada daftar produk owner.
 * Responsif pada layar 320-360 dp dan memakai weight pada baris dengan banyak teks.
 */
@Composable
private fun OwnerProductItemCard(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Baris Atas: Kategori Badge & Status Sakelar Aktif
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge Kategori
                Box(
                    modifier = Modifier
                        .clip(ShapeBadge)
                        .background(AccentYellow)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(product.kategori.titleRes),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = InkBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Sakelar Aktif/Nonaktif
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (product.aktif) {
                            stringResource(R.string.owner_product_status_active)
                        } else {
                            stringResource(R.string.owner_product_status_inactive)
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = if (product.aktif) GeceColors.StatusSuccessText else TextSecondary,
                        maxLines = 1
                    )
                    Switch(
                        checked = product.aktif,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = InkBlack,
                            checkedTrackColor = AccentYellow,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceVariantColor
                        )
                    )
                }
            }

            // Baris Tengah: Nama Produk & Bahan
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = product.nama,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = product.bahan,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Pratinjau Swatch Warna Kecil & Pilihan Ukuran
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Swatch warna
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    product.daftarWarna.take(5).forEach { warna ->
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(warna.hex.trim()))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .clip(CircleShape)
                        )
                    }
                    if (product.daftarWarna.size > 5) {
                        Text(
                            text = "+${product.daftarWarna.size - 5}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "• ${product.daftarUkuran.size} ukuran",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Baris Bawah: Harga Dasar & Tombol Aksi (Ubah & Hapus)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.owner_product_base_price_label),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Text(
                        text = Formatter.formatRupiah(product.hargaDasar),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.owner_product_btn_edit),
                            tint = InkBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.owner_product_btn_delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
