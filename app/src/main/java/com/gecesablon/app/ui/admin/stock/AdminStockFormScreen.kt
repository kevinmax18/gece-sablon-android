package com.gecesablon.app.ui.admin.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gecesablon.app.R
import com.gecesablon.app.data.model.StockCategory
import com.gecesablon.app.ui.components.GeceButton
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStockFormScreen(
    viewModel: AdminStockFormViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val satuanOptions = listOf("pcs", "lembar", "meter", "roll")
    var satuanExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) {
                            stringResource(R.string.admin_stock_form_title_edit)
                        } else {
                            stringResource(R.string.admin_stock_form_title_add)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
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
            // Nama Barang
            OutlinedTextField(
                value = uiState.nama,
                onValueChange = viewModel::onNamaChanged,
                label = { Text(stringResource(R.string.admin_stock_form_name_label)) },
                placeholder = { Text(stringResource(R.string.admin_stock_form_name_hint)) },
                isError = uiState.namaError != null,
                supportingText = uiState.namaError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Kategori Barang
            Column {
                Text(
                    text = stringResource(R.string.admin_stock_form_category_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                listOf(
                    StockCategory.BAJU_POLOS to stringResource(R.string.admin_stock_filter_baju),
                    StockCategory.MATERIAL_DTF to stringResource(R.string.admin_stock_filter_material),
                    StockCategory.LAINNYA to stringResource(R.string.admin_stock_filter_lainnya)
                ).forEach { (cat, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onKategoriChanged(cat) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = uiState.kategori == cat,
                            onClick = { viewModel.onKategoriChanged(cat) }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkBlack
                        )
                    }
                }
            }

            // Satuan Dropdown
            ExposedDropdownMenuBox(
                expanded = satuanExpanded,
                onExpandedChange = { satuanExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.satuan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.admin_stock_form_unit_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = satuanExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = satuanExpanded,
                    onDismissRequest = { satuanExpanded = false }
                ) {
                    satuanOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                viewModel.onSatuanChanged(opt)
                                satuanExpanded = false
                            }
                        )
                    }
                }
            }

            // Jumlah Stok Fisik
            OutlinedTextField(
                value = uiState.jumlahText,
                onValueChange = viewModel::onJumlahChanged,
                label = { Text(stringResource(R.string.admin_stock_form_qty_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.jumlahError != null,
                supportingText = uiState.jumlahError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Batas Minimum
            OutlinedTextField(
                value = uiState.batasMinText,
                onValueChange = viewModel::onBatasMinChanged,
                label = { Text(stringResource(R.string.admin_stock_form_min_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.batasMinError != null,
                supportingText = uiState.batasMinError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            GeceButton(
                text = stringResource(R.string.admin_stock_form_btn_save),
                onClick = viewModel::submit,
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
