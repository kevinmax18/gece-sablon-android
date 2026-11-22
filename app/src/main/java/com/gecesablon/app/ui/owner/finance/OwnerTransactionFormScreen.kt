package com.gecesablon.app.ui.owner.finance

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.core.util.PeriodRules
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.model.displayName
import com.gecesablon.app.ui.components.AppTopBar
import com.gecesablon.app.ui.components.GeceButton
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import com.gecesablon.app.ui.theme.White
import androidx.compose.material3.SelectableDates

/**
 * Layar form untuk mencatat pemasukan atau pengeluaran baru, atau mengubah transaksi yang ada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerTransactionFormScreen(
    viewModel: OwnerTransactionFormViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigasi balik saat simpan berhasil
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    // Tampilkan snackbar saat ada pesan error
    LaunchedEffect(uiState.errorMessage) {
        if (!uiState.errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(uiState.errorMessage!!)
        }
    }

    // State untuk date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }

    // Judul dinamis berdasarkan mode dan tipe
    val screenTitle = when {
        uiState.isEditMode -> stringResource(R.string.owner_tx_form_title_edit)
        uiState.tipe == TransactionType.PEMASUKAN -> stringResource(R.string.owner_tx_form_title_income)
        else -> stringResource(R.string.owner_tx_form_title_expense)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = screenTitle,
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toggle tipe: Pengeluaran / Pemasukan
            if (!uiState.isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipeToggleButton(
                        label = stringResource(R.string.owner_tx_form_type_expense),
                        isActive = uiState.tipe == TransactionType.PENGELUARAN,
                        onClick = { viewModel.onTipeChange(TransactionType.PENGELUARAN) },
                        modifier = Modifier.weight(1f)
                    )
                    TipeToggleButton(
                        label = stringResource(R.string.owner_tx_form_type_income),
                        isActive = uiState.tipe == TransactionType.PEMASUKAN,
                        onClick = { viewModel.onTipeChange(TransactionType.PEMASUKAN) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Dropdown kategori
            val availableKategori: List<TransactionCategory> = when (uiState.tipe) {
                TransactionType.PENGELUARAN -> TransactionCategory.expenseCategories
                TransactionType.PEMASUKAN -> TransactionCategory.manualIncomeCategories
            }
            KategoriDropdown(
                label = stringResource(R.string.owner_tx_form_category_label),
                selected = uiState.kategori,
                options = availableKategori,
                onSelect = { viewModel.onKategoriChange(it) },
                errorMessage = uiState.errorKategori
            )

            // Field nominal
            OutlinedTextField(
                value = uiState.nominalText,
                onValueChange = { viewModel.onNominalChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.owner_tx_form_nominal_label)) },
                prefix = { Text(stringResource(R.string.owner_tx_form_nominal_prefix)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.errorNominal != null,
                supportingText = {
                    if (uiState.errorNominal != null) {
                        Text(uiState.errorNominal!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = ShapeControl
            )

            // Field tanggal – tombol buka DatePickerDialog
            OutlinedTextField(
                value = Formatter.formatDateIndo(uiState.tanggal).substringBefore(","), // Hanya tanggal
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth(),
                label = { Text(stringResource(R.string.owner_tx_form_date_label)) },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                shape = ShapeControl,
                // Klik pada field membuka picker
                enabled = true
            )
            // Tombol pilih tanggal
            TextButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.owner_tx_form_date_label))
            }

            // Field keterangan
            OutlinedTextField(
                value = uiState.keterangan,
                onValueChange = { viewModel.onKeteranganChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.owner_tx_form_note_label)) },
                placeholder = { Text(stringResource(R.string.owner_tx_form_note_hint)) },
                isError = uiState.errorKeterangan != null,
                supportingText = {
                    Row {
                        if (uiState.errorKeterangan != null) {
                            Text(uiState.errorKeterangan!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text("${uiState.keterangan.length}/200", style = MaterialTheme.typography.bodySmall, color = GeceColors.TextMuted)
                    }
                },
                minLines = 2,
                maxLines = 4,
                shape = ShapeControl
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol simpan
            GeceButton(
                text = stringResource(R.string.owner_tx_form_btn_save),
                onClick = { viewModel.onSave() },
                loading = uiState.isLoading,
                enabled = !uiState.isLoading
            )
        }
    }

    // DatePickerDialog Material3
    if (showDatePicker) {
        val todayEndMillis = PeriodRules.endOfDay(System.currentTimeMillis())
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.tanggal,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= todayEndMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            // Konversi dari UTC ke lokal
                            val localMillis = PeriodRules.utcMillisToLocalDate(selected)
                            viewModel.onTanggalChange(localMillis)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/** Tombol toggle tipe transaksi (aktif: AccentYellow + InkBlack text, nonaktif: outline). */
@Composable
private fun TipeToggleButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = ShapeControl,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) AccentYellow else Color.Transparent,
            contentColor = if (isActive) InkBlack else GeceColors.TextMuted
        ),
        border = if (!isActive) androidx.compose.foundation.BorderStroke(1.dp, GeceColors.Border) else null
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

/** Dropdown kategori transaksi menggunakan ExposedDropdownMenuBox Material3. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KategoriDropdown(
    label: String,
    selected: TransactionCategory,
    options: List<TransactionCategory>,
    onSelect: (TransactionCategory) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected.displayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = errorMessage != null,
            supportingText = {
                if (errorMessage != null) Text(errorMessage, color = MaterialTheme.colorScheme.error)
            },
            shape = ShapeControl,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { kategori ->
                DropdownMenuItem(
                    text = { Text(kategori.displayName()) },
                    onClick = {
                        onSelect(kategori)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
