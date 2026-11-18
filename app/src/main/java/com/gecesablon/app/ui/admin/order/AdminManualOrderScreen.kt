package com.gecesablon.app.ui.admin.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.PriceCalculator
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.ui.components.GeceButton
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.White
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminManualOrderScreen(
    viewModel: AdminManualOrderViewModel,
    onNavigateBack: () -> Unit,
    onOrderCreated: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.createdOrder) {
        uiState.createdOrder?.let {
            onOrderCreated(it)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    var productExpanded by remember { mutableStateOf(false) }
    val selectedProduct = uiState.availableProducts.find { it.id == uiState.selectedProductId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.admin_manual_order_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = stringResource(R.string.admin_manual_order_subtitle),
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GeceColors.Background)
            )
        },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bagian 1: Data Pelanggan Walk-In
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
                border = BorderStroke(1.dp, GeceColors.Border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. Data Pelanggan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )

                    OutlinedTextField(
                        value = uiState.namaPelanggan,
                        onValueChange = viewModel::onNamaChanged,
                        label = { Text(stringResource(R.string.admin_manual_order_cust_name_label)) },
                        placeholder = { Text(stringResource(R.string.admin_manual_order_cust_name_hint)) },
                        isError = uiState.namaError != null,
                        supportingText = uiState.namaError?.let { { Text(it) } },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.noHpPelanggan,
                        onValueChange = viewModel::onHpChanged,
                        label = { Text(stringResource(R.string.admin_manual_order_phone_label)) },
                        placeholder = { Text(stringResource(R.string.admin_manual_order_phone_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = uiState.hpError != null,
                        supportingText = uiState.hpError?.let { { Text(it) } },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Bagian 2: Spesifikasi Produk & DTF
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
                border = BorderStroke(1.dp, GeceColors.Border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "2. Spesifikasi Produk & Sablon",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )

                    // Dropdown Produk
                    ExposedDropdownMenuBox(
                        expanded = productExpanded,
                        onExpandedChange = { productExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.nama ?: "Pilih produk...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.admin_manual_order_product_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = productExpanded,
                            onDismissRequest = { productExpanded = false }
                        ) {
                            uiState.availableProducts.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.nama} (${rupiahFormat.format(prod.hargaDasar)})") },
                                    onClick = {
                                        viewModel.onProductSelected(prod.id)
                                        productExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Pilihan Warna
                    if (selectedProduct != null) {
                        Text(
                            text = stringResource(R.string.admin_manual_order_color_label),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = GeceColors.TextMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedProduct.daftarWarna.forEach { color ->
                                val isSelected = uiState.selectedColorName.equals(color.nama, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onColorSelected(color.nama) },
                                    label = { Text(color.nama) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = InkBlack,
                                        selectedLabelColor = White
                                    )
                                )
                            }
                        }

                        // Pilihan Ukuran
                        Text(
                            text = stringResource(R.string.admin_manual_order_size_label),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = GeceColors.TextMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedProduct.daftarUkuran.forEach { size ->
                                val isSelected = uiState.selectedSizeLabel.equals(size.label, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onSizeSelected(size.label) },
                                    label = {
                                        Text(
                                            if (size.tambahanHarga > 0L) {
                                                "${size.label} (+${rupiahFormat.format(size.tambahanHarga)})"
                                            } else {
                                                size.label
                                            }
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = InkBlack,
                                        selectedLabelColor = White
                                    )
                                )
                            }
                        }
                    }

                    // Jumlah Pcs Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.admin_manual_order_qty_label),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = InkBlack
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.onQuantityChanged(uiState.quantity - 1) },
                                enabled = uiState.quantity > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurang")
                            }
                            Text(
                                text = "${uiState.quantity}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(
                                onClick = { viewModel.onQuantityChanged(uiState.quantity + 1) },
                                enabled = uiState.quantity < 999
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah")
                            }
                        }
                    }
                }
            }

            // Bagian 3: Catatan & Desain
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
                border = BorderStroke(1.dp, GeceColors.Border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "3. Instruksi Cetak & Desain",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )

                    OutlinedTextField(
                        value = uiState.catatan,
                        onValueChange = viewModel::onCatatanChanged,
                        label = { Text(stringResource(R.string.admin_manual_order_notes_label)) },
                        placeholder = { Text(stringResource(R.string.admin_manual_order_notes_hint)) },
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.uriDesain ?: "",
                        onValueChange = viewModel::onUriDesainChanged,
                        label = { Text(stringResource(R.string.admin_manual_order_design_url_label)) },
                        placeholder = { Text(stringResource(R.string.admin_manual_order_design_url_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Ringkasan Estimasi Biaya
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GeceColors.StatusPendingBg.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, GeceColors.StatusPendingBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.admin_manual_order_summary_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                R.string.admin_manual_order_unit_price,
                                rupiahFormat.format(uiState.calculatedUnitPrice)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = GeceColors.TextMuted
                        )
                        Text(
                            text = "x ${uiState.quantity} pcs",
                            style = MaterialTheme.typography.bodySmall,
                            color = GeceColors.TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.admin_manual_order_total_price,
                            rupiahFormat.format(uiState.calculatedTotalPrice)
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            GeceButton(
                text = stringResource(R.string.admin_manual_order_btn_submit),
                onClick = viewModel::submitOrder,
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
