package com.gecesablon.app.ui.staff

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.core.util.ProductionRules
import com.gecesablon.app.core.util.WhatsAppIntent
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderCreator
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.ProductionStage
import com.gecesablon.app.data.model.ProductionState
import com.gecesablon.app.ui.components.ErrorView
import com.gecesablon.app.ui.components.LoadingView
import com.gecesablon.app.ui.components.StatusChip
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl

/**
 * Layar Detail Pesanan Staf Workshop (Admin & Owner).
 * Memuat panel produksi (centang tahapan berurutan, 3 aksi material, Terima & Mulai Proses, Simpan Perubahan, Tandai Selesai).
 * Memiliki peringatan keluar jika ada perubahan belum disimpan dan tombol WhatsApp pelanggan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOrderDetailScreen(
    viewModel: StaffOrderDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    // Tangani aksi kembali dengan peringatan perubahan belum disimpan
    val handleBackPress = {
        if (uiState.hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = true, onBack = handleBackPress)

    // Tampilkan snackbar jika ada pesan sukses atau error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // Dialog Peringatan Keluar Tanpa Menyimpan
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.staff_detail_dialog_unsaved_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.staff_detail_dialog_unsaved_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.staff_detail_dialog_unsaved_leave))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUnsavedDialog = false }) {
                    Text(stringResource(R.string.staff_detail_dialog_unsaved_stay))
                }
            }
        )
    }

    // Dialog Konfirmasi Pesanan Selesai
    if (showCompleteDialog) {
        val order = uiState.order
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.staff_detail_dialog_complete_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.staff_detail_dialog_complete_desc,
                        order?.kode ?: ""
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteDialog = false
                        viewModel.markOrderCompleted()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeceColors.Accent,
                        contentColor = GeceColors.PrimaryInk
                    )
                ) {
                    Text(
                        stringResource(R.string.staff_detail_dialog_complete_confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCompleteDialog = false }) {
                    Text(stringResource(R.string.staff_detail_dialog_complete_dismiss))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.staff_detail_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        uiState.order?.let { order ->
                            Box(
                                modifier = Modifier
                                    .clip(ShapeBadge)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = order.kode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = handleBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Floating Sticky Action Bar untuk Simpan / Selesai
            val order = uiState.order
            if (order != null && (uiState.hasUnsavedChanges || order.status == OrderStatus.SIAP_DIAMBIL)) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (order.status == OrderStatus.SIAP_DIAMBIL) {
                            Button(
                                onClick = { showCompleteDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeceColors.PrimaryInk,
                                    contentColor = GeceColors.Accent
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.staff_detail_btn_mark_completed),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        if (uiState.hasUnsavedChanges) {
                            Button(
                                onClick = viewModel::saveProductionState,
                                enabled = !uiState.isSaving,
                                modifier = Modifier.weight(1f),
                                shape = ShapeControl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GeceColors.Accent,
                                    contentColor = GeceColors.PrimaryInk
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.staff_detail_btn_save_changes),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.order == null -> {
                    ErrorView(
                        message = uiState.errorMessage ?: stringResource(R.string.state_error_desc),
                        onRetry = viewModel::loadOrderDetail,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    val order = uiState.order!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Kartu Informasi Pelanggan
                        CustomerInfoSection(
                            order = order,
                            onWhatsAppClick = {
                                val cleanPhone = order.noHpPelanggan ?: ""
                                val msg = context.getString(
                                    R.string.staff_detail_whatsapp_message,
                                    order.namaPelanggan,
                                    order.kode
                                )
                                WhatsAppIntent.openChat(context, cleanPhone, msg)
                            }
                        )

                        // 2. Rincian Item Apparel & Desain
                        OrderItemAndDesignSection(order = order)

                        // 3. Panel Produksi Internal
                        ProductionPanelSection(
                            order = order,
                            stagingState = uiState.stagingState,
                            isSaving = uiState.isSaving,
                            onToggleStage = viewModel::toggleStage,
                            onToggleBajuTiba = viewModel::toggleBajuTiba,
                            onToggleDtfTiba = viewModel::toggleDtfTiba,
                            onToggleSiapDisablon = viewModel::toggleSiapDisablon,
                            onAcceptProcess = viewModel::acceptAndStartProcess
                        )

                        // 4. Linimasa Riwayat Aktivitas
                        ActivityHistorySection(order = order)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerInfoSection(
    order: Order,
    onWhatsAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.staff_detail_customer_section),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .clip(ShapeBadge)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (order.dibuatOleh == OrderCreator.STAFF) {
                            stringResource(R.string.staff_detail_origin_manual)
                        } else {
                            stringResource(R.string.staff_detail_origin_customer)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = order.namaPelanggan,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!order.noHpPelanggan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = order.noHpPelanggan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onWhatsAppClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeControl,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeceColors.PrimaryInk,
                        contentColor = GeceColors.Accent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.staff_detail_btn_whatsapp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemAndDesignSection(
    order: Order,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spesifikasi Pesanan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            order.items.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeControl)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.namaProduk,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${item.jumlah} pcs",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = GeceColors.PrimaryInk
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val detailBahan = if (item.bahanNama.isNotBlank()) "Bahan: ${item.bahanNama} \u2022 " else ""
                    Text(
                        text = "${detailBahan}Varian: ${item.warnaNama} \u2022 Ukuran ${item.ukuranLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Harga Satuan: ${Formatter.formatRupiah(item.hargaSatuan)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Biaya",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = Formatter.formatRupiah(order.total),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GeceColors.PrimaryInk
                )
            }

            if (order.catatan.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Catatan Khusus Pelanggan:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = order.catatan,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!order.uriDesain.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeControl)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Berkas Artwork Desain Terlampir",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ProductionPanelSection(
    order: Order,
    stagingState: ProductionState,
    isSaving: Boolean,
    onToggleStage: (ProductionStage) -> Unit,
    onToggleBajuTiba: (Boolean) -> Unit,
    onToggleDtfTiba: (Boolean) -> Unit,
    onToggleSiapDisablon: (Boolean) -> Unit,
    onAcceptProcess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDiterima = order.status == OrderStatus.DITERIMA
    val isDibatalkanOrSelesai = order.status == OrderStatus.DIBATALKAN || order.status == OrderStatus.SELESAI

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.staff_detail_production_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .clip(ShapeBadge)
                        .background(GeceColors.Accent)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${stagingState.tahapanSelesai.size}/5 Selesai",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GeceColors.PrimaryInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kasus DITERIMA: Tampilkan banner aksi awal
            if (isDiterima) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeControl)
                        .background(GeceColors.StatusPendingBg.copy(alpha = 0.25f))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = GeceColors.PrimaryInk,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Menunggu Penerimaan Workshop",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = GeceColors.PrimaryInk
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.staff_detail_diterima_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onAcceptProcess,
                            enabled = !isSaving,
                            shape = ShapeControl,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeceColors.Accent,
                                contentColor = GeceColors.PrimaryInk
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.staff_detail_btn_accept_process),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Checklist 5 Tahapan Produksi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProductionStage.orderedStages.forEach { stage ->
                    val isChecked = stage in stagingState.tahapanSelesai
                    val checkResult = if (isChecked) {
                        ProductionRules.bolehBatal(stage, stagingState)
                    } else {
                        ProductionRules.bolehCentang(stage, stagingState)
                    }
                    val isActionAllowed = !isDiterima && !isDibatalkanOrSelesai && checkResult.diizinkan

                    StageChecklistItem(
                        stage = stage,
                        isChecked = isChecked,
                        isEnabled = isActionAllowed,
                        disabledReason = if (!isActionAllowed && !isDiterima && !isDibatalkanOrSelesai) {
                            checkResult.pesan
                        } else null,
                        onCheckedChange = { onToggleStage(stage) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Panel Aksi Material & Alat Workshop
            Text(
                text = stringResource(R.string.staff_detail_mat_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeControl)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Baju Tiba
                val izinBaju = ProductionRules.bolehUbahBajuTiba(!stagingState.bajuTiba, stagingState)
                MaterialToggleRow(
                    label = stringResource(R.string.staff_detail_action_shirt_arrived),
                    isChecked = stagingState.bajuTiba,
                    isEnabled = !isDiterima && !isDibatalkanOrSelesai && izinBaju.diizinkan,
                    disabledReason = if (!izinBaju.diizinkan) izinBaju.pesan else null,
                    onCheckedChange = { onToggleBajuTiba(it) }
                )

                // 2. DTF Tiba
                val izinDtf = ProductionRules.bolehUbahDtfTiba(!stagingState.dtfTiba, stagingState)
                MaterialToggleRow(
                    label = stringResource(R.string.staff_detail_action_dtf_arrived),
                    isChecked = stagingState.dtfTiba,
                    isEnabled = !isDiterima && !isDibatalkanOrSelesai && izinDtf.diizinkan,
                    disabledReason = if (!izinDtf.diizinkan) izinDtf.pesan else null,
                    onCheckedChange = { onToggleDtfTiba(it) }
                )

                // 3. Siap Disablon
                val izinSiap = ProductionRules.bolehUbahSiapDisablon(!stagingState.siapDisablon, stagingState)
                MaterialToggleRow(
                    label = stringResource(R.string.staff_detail_action_ready_press),
                    isChecked = stagingState.siapDisablon,
                    isEnabled = !isDiterima && !isDibatalkanOrSelesai && izinSiap.diizinkan,
                    disabledReason = if (!izinSiap.diizinkan) izinSiap.pesan else null,
                    onCheckedChange = { onToggleSiapDisablon(it) }
                )
            }
        }
    }
}

@Composable
fun StageChecklistItem(
    stage: ProductionStage,
    isChecked: Boolean,
    isEnabled: Boolean,
    disabledReason: String?,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeControl,
        color = if (isChecked) {
            GeceColors.StatusSuccessBg
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            1.dp,
            if (isChecked) GeceColors.StatusSuccessText.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { onCheckedChange() },
                    enabled = isEnabled,
                    colors = CheckboxDefaults.colors(
                        checkedColor = GeceColors.PrimaryInk,
                        checkmarkColor = GeceColors.Accent,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${stage.nomor}. ${stringResource(stage.titleRes)}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isChecked) {
                            Box(
                                modifier = Modifier
                                    .clip(ShapeBadge)
                                    .background(GeceColors.StatusSuccessBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.staff_detail_stage_status_done),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GeceColors.StatusSuccessText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(stage.descRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!disabledReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\u2022 $disabledReason",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }
    }
}

@Composable
fun MaterialToggleRow(
    label: String,
    isChecked: Boolean,
    isEnabled: Boolean,
    disabledReason: String?,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = isEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GeceColors.Accent,
                    checkedTrackColor = GeceColors.PrimaryInk,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        if (!disabledReason.isNullOrBlank()) {
            Text(
                text = "\u2022 $disabledReason",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ActivityHistorySection(
    order: Order,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.staff_detail_history_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (order.riwayat.isEmpty()) {
                Text(
                    text = stringResource(R.string.staff_detail_history_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    order.riwayat.reversed().forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ShapeControl)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GeceColors.PrimaryInk,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.aksi,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Oleh ${entry.aktor} \u2022 ${Formatter.formatDateIndo(entry.waktu)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
