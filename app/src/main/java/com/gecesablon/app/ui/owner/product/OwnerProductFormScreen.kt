package com.gecesablon.app.ui.owner.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.core.util.PresetColors
import com.gecesablon.app.core.util.ProductRules
import com.gecesablon.app.data.model.ProductCategory
import com.gecesablon.app.ui.components.LoadingState
import com.gecesablon.app.ui.components.OwnerHeader
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.BorderHairline
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import com.gecesablon.app.ui.theme.SurfaceCard
import com.gecesablon.app.ui.theme.SurfaceVariantColor
import com.gecesablon.app.ui.theme.TextPrimary
import com.gecesablon.app.ui.theme.TextSecondary
import com.gecesablon.app.ui.theme.White

/**
 * Layar Formulir Tambah / Ubah Produk Owner:
 * - Data umum: nama, kategori, bahan, deskripsi, harga dasar, sakelar aktif, dan placeholder gambar.
 * - Daftar warna: nama + hex dengan swatch pratinjau, tombol tambah baris, tombol hapus baris.
 * - Pilihan ukuran S-XXXL dengan checklist dan tambahan harga per ukuran.
 * - Dialog hapus produk yang menyebutkan nama produk pada mode ubah.
 * - Responsif untuk ukuran layar 320-360 dp dan penanganan error per kolom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerProductFormScreen(
    viewModel: OwnerProductFormViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Dialog Konfirmasi Hapus Produk (Mode Ubah)
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = {
                Text(
                    text = stringResource(R.string.owner_product_delete_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.owner_product_delete_dialog_msg, uiState.nama),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(onDeleted = onNavigateBack)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.owner_product_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) {
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
                subtitle = if (uiState.isEditMode) {
                    stringResource(R.string.owner_product_form_title_edit)
                } else {
                    stringResource(R.string.owner_product_form_title_add)
                },
                showBack = true,
                onBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                message = stringResource(R.string.state_loading)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // =====================================================================
                // 1. DATA UMUM PRODUK
                // =====================================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderHairline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.owner_product_section_general),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary
                        )

                        // Status Aktif/Nonaktif
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.owner_product_status_label),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.owner_product_status_hint),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                checked = uiState.aktif,
                                onCheckedChange = viewModel::onAktifChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = InkBlack,
                                    checkedTrackColor = AccentYellow,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = SurfaceVariantColor
                                )
                            )
                        }

                        HorizontalDivider(thickness = 0.8.dp, color = BorderHairline)

                        // Nama Produk
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.owner_product_name_label),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = uiState.nama,
                                onValueChange = viewModel::onNamaChanged,
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.owner_product_name_placeholder),
                                        color = TextSecondary
                                    )
                                },
                                isError = uiState.namaError != null,
                                singleLine = true,
                                shape = ShapeControl,
                                colors = formTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            uiState.namaError?.let { err ->
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                                )
                            }
                        }

                        // Kategori Produk (Dropdown)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.owner_product_category_label),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded,
                                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = uiState.kategori?.let { stringResource(it.titleRes) } ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = {
                                        Text(
                                            text = stringResource(R.string.owner_product_category_placeholder),
                                            color = TextSecondary
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                                    },
                                    shape = ShapeControl,
                                    isError = uiState.kategoriError != null,
                                    colors = formTextFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false },
                                    modifier = Modifier.background(SurfaceCard)
                                ) {
                                    ProductCategory.entries.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(text = stringResource(cat.titleRes), color = TextPrimary) },
                                            onClick = {
                                                viewModel.onKategoriSelected(cat)
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            uiState.kategoriError?.let { err ->
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                                )
                            }
                        }

                        // Bahan Kain
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.owner_product_fabric_label),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = uiState.bahan,
                                onValueChange = viewModel::onBahanChanged,
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.owner_product_fabric_placeholder),
                                        color = TextSecondary
                                    )
                                },
                                isError = uiState.bahanError != null,
                                singleLine = true,
                                shape = ShapeControl,
                                colors = formTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            uiState.bahanError?.let { err ->
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                                )
                            }
                        }

                        // Deskripsi Produk (Maks 500 karakter)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.owner_product_desc_label),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${uiState.deskripsi.length}/${ProductRules.MAX_DESKRIPSI_LENGTH}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (uiState.deskripsi.length > ProductRules.MAX_DESKRIPSI_LENGTH) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        TextSecondary
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = uiState.deskripsi,
                                onValueChange = viewModel::onDeskripsiChanged,
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.owner_product_desc_placeholder),
                                        color = TextSecondary
                                    )
                                },
                                minLines = 3,
                                maxLines = 5,
                                isError = uiState.deskripsiError != null,
                                shape = ShapeControl,
                                colors = formTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            uiState.deskripsiError?.let { err ->
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                                )
                            }
                        }

                        // Harga Dasar
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.owner_product_price_label),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = uiState.hargaDasarInput,
                                onValueChange = viewModel::onHargaDasarChanged,
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.owner_product_price_placeholder),
                                        color = TextSecondary
                                    )
                                },
                                prefix = {
                                    Text(
                                        text = "Rp ",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = uiState.hargaDasarError != null,
                                singleLine = true,
                                shape = ShapeControl,
                                colors = formTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            val parsedPrice = uiState.hargaDasarInput.toLongOrNull() ?: 0L
                            if (parsedPrice > 0) {
                                Text(
                                    text = Formatter.formatRupiah(parsedPrice) + " " + stringResource(R.string.detail_product_per_pcs),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                                )
                            }
                            uiState.hargaDasarError?.let { err ->
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                                )
                            }
                        }

                        // Informasi Gambar Produk (Placeholder)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ShapeControl)
                                .background(GeceColors.Background)
                                .border(1.dp, BorderHairline, ShapeControl)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.owner_product_image_title),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = stringResource(R.string.owner_product_image_note),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // =====================================================================
                // 2. DAFTAR VARIASI WARNA
                // =====================================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderHairline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.owner_product_color_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.owner_product_color_subtitle),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            TextButton(
                                onClick = viewModel::addColorRow,
                                colors = ButtonDefaults.textButtonColors(contentColor = InkBlack)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.owner_product_btn_add_color),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Deretan Chip Warna Preset (Dapat digulir ke samping)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.owner_product_color_preset_label),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = TextSecondary
                            )
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(PresetColors.list) { preset ->
                                    val chipColor = try {
                                        Color(android.graphics.Color.parseColor(preset.hex))
                                    } catch (e: Exception) {
                                        Color.LightGray
                                    }
                                    val duplicateMsg = stringResource(
                                        R.string.owner_product_color_duplicate_msg,
                                        preset.nama
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            viewModel.addPresetColor(preset, duplicateMsg)
                                        },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(chipColor)
                                                        .border(0.5.dp, BorderHairline, CircleShape)
                                                )
                                                Text(
                                                    text = preset.nama,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    maxLines = 1
                                                )
                                            }
                                        },
                                        shape = ShapeControl,
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = GeceColors.Background,
                                            labelColor = TextPrimary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = false,
                                            borderColor = BorderHairline
                                        )
                                    )
                                }
                            }
                        }

                        uiState.warnaError?.let { err ->
                            Text(
                                text = err,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        // Baris-baris variasi warna
                        uiState.colorRows.forEachIndexed { index, rowItem ->
                            ColorRowEditor(
                                item = rowItem,
                                onUpdate = { nama, hex ->
                                    viewModel.updateColorRow(index, nama, hex)
                                },
                                onRemove = {
                                    viewModel.removeColorRow(index)
                                },
                                canRemove = uiState.colorRows.size > 1
                            )
                        }
                    }
                }

                // =====================================================================
                // 3. PILIHAN UKURAN & TAMBAHAN HARGA
                // =====================================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderHairline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.owner_product_size_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(R.string.owner_product_size_subtitle),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }

                        uiState.ukuranError?.let { err ->
                            Text(
                                text = err,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Daftar Checklist Ukuran S-XXXL
                        uiState.sizeSelections.forEach { sizeItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ShapeControl)
                                    .background(if (sizeItem.isSelected) GeceColors.Background else Color.Transparent)
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.toggleSizeSelection(sizeItem.label, !sizeItem.isSelected)
                                        }
                                ) {
                                    Checkbox(
                                        checked = sizeItem.isSelected,
                                        onCheckedChange = { isChecked ->
                                            viewModel.toggleSizeSelection(sizeItem.label, isChecked)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = InkBlack,
                                            checkmarkColor = AccentYellow,
                                            uncheckedColor = TextSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ukuran ${sizeItem.label}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (sizeItem.isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (sizeItem.isSelected) {
                                    OutlinedTextField(
                                        value = sizeItem.tambahanHargaInput,
                                        onValueChange = { newVal ->
                                            viewModel.updateSizeExtraPrice(sizeItem.label, newVal)
                                        },
                                        prefix = {
                                            Text(
                                                text = "+Rp ",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = TextSecondary
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = ShapeControl,
                                        colors = formTextFieldColors(),
                                        modifier = Modifier.width(130.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // =====================================================================
                // 4. TOMBOL SIMPAN & HAPUS
                // =====================================================================
                Button(
                    onClick = {
                        viewModel.saveProduct(onSuccess = onNavigateBack)
                    },
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = ShapeControl,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentYellow,
                        contentColor = InkBlack
                    )
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            color = InkBlack,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isEditMode) {
                                    stringResource(R.string.owner_product_btn_save_edit)
                                } else {
                                    stringResource(R.string.owner_product_btn_save_add)
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }

                // Tombol Hapus pada Mode Ubah
                if (uiState.isEditMode) {
                    OutlinedButton(
                        onClick = viewModel::requestDelete,
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = ShapeControl,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.owner_product_btn_delete_product),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Komposabel satu baris editor warna (Swatch + Nama + Hex + Hapus).
 */
@Composable
private fun ColorRowEditor(
    item: ColorRowItem,
    onUpdate: (String, String) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean,
    modifier: Modifier = Modifier
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(item.hex.trim()))
    } catch (e: Exception) {
        Color.LightGray
    }

    val isHexValid = ProductRules.isValidHexColor(item.hex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeControl)
            .background(GeceColors.Background)
            .border(1.dp, BorderHairline, ShapeControl)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Swatch warna kotak/lingkaran pratinjau
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(parsedColor)
                    .border(1.dp, BorderHairline, CircleShape)
            )

            // Nama Warna
            OutlinedTextField(
                value = item.nama,
                onValueChange = { newNama -> onUpdate(newNama, item.hex) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.owner_product_color_name_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                shape = ShapeControl,
                colors = formTextFieldColors(),
                modifier = Modifier.weight(1.3f)
            )

            // Kode Hex (#RRGGBB)
            OutlinedTextField(
                value = item.hex,
                onValueChange = { newHex ->
                    val normalized = ProductRules.normalizeHexInput(newHex)
                    onUpdate(item.nama, normalized)
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.owner_product_color_hex_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                },
                isError = !isHexValid && item.hex.isNotEmpty(),
                singleLine = true,
                shape = ShapeControl,
                colors = formTextFieldColors(),
                modifier = Modifier.weight(1f)
            )

            // Tombol Hapus Baris
            IconButton(
                onClick = onRemove,
                enabled = canRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.owner_product_btn_remove_color_row),
                    tint = if (canRemove) MaterialTheme.colorScheme.error else TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Pesan bantu/error kode hex spesifik jika hex tidak valid
        if (!isHexValid && item.hex.isNotEmpty()) {
            Text(
                text = stringResource(R.string.owner_product_color_hex_error),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }
}

@Composable
private fun formTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = GeceColors.Background,
    unfocusedContainerColor = GeceColors.Background,
    focusedBorderColor = InkBlack,
    unfocusedBorderColor = BorderHairline,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
