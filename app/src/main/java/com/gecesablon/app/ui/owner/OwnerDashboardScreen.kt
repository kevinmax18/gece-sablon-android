package com.gecesablon.app.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.SixMonthTrend
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.ui.components.EmptyState
import com.gecesablon.app.ui.components.OwnerHeader
import com.gecesablon.app.ui.owner.components.MonthlyTrendChart
import com.gecesablon.app.ui.components.StatusChip
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.BorderHairline
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeBadge
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import com.gecesablon.app.ui.theme.ShapePill
import com.gecesablon.app.ui.theme.SurfaceCard
import com.gecesablon.app.ui.theme.SurfaceVariantColor
import com.gecesablon.app.ui.theme.TextPrimary
import com.gecesablon.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max

/**
 * Layar Dashboard Utama Owner sesuai desain Figma (docs/figma/owner/30-dashboard.png).
 * Struktur urutan bagian dari atas ke bawah:
 * 1. Header bersama (OwnerHeader)
 * 2. Salam & identitas sesi
 * 3. Aksi cepat 4 ubin (ikon di atas label)
 * 4. Ringkasan Finansial (Laba Bersih besar, Pendapatan, Pengeluaran)
 * 5. Tren Pendapatan & Pengeluaran 6 bulan (grafik batang berpasangan Canvas Compose murni)
 * 6. Status Produksi Workshop (grid 2x2: Masuk, Diproses, Cetak & Press, Siap Diambil)
 * 7. Pesanan Terbaru (3-5 pesanan dengan ringkasan & StatusChip)
 * 8. Status Stok Material (Bahan Produk & Konsumabel DTF)
 */
@Composable
fun OwnerDashboardScreen(
    viewModel: OwnerDashboardViewModel,
    onNavigateToCreateManualOrder: () -> Unit,
    onNavigateToRecordExpense: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToProcess: () -> Unit,
    onNavigateToAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoonMessage = stringResource(R.string.owner_dashboard_coming_soon)

        Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // =========================================================================
            // 1. Header Bersama Owner
            // =========================================================================
            OwnerHeader(
                tabName = stringResource(R.string.owner_header_tab_home),
                onProfileClick = onNavigateToAccount
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GeceColors.Background
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = InkBlack)
                }
            }

            uiState.errorMessage != null && uiState.recentOrders.isEmpty() -> {
                EmptyState(
                    title = "Terjadi Kesalahan",
                    description = uiState.errorMessage ?: "Gagal memuat data dashboard.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // =========================================================
                    // 2. Salam & Identitas Sesi
                    // =========================================================
                    OwnerGreetingSection(
                        ownerName = uiState.ownerName,
                        rangeText = uiState.currentMonthRangeText,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // =========================================================
                    // 3. Aksi Cepat (4 Ubin vertikal)
                    // =========================================================
                    OwnerQuickActionsSection(
                        onBuatPesanan = onNavigateToCreateManualOrder,
                        onCatatKeluar = onNavigateToRecordExpense,
                        onKatalogProduk = {
                            scope.launch { snackbarHostState.showSnackbar(comingSoonMessage) }
                        },
                        onTimSablon = {
                            scope.launch { snackbarHostState.showSnackbar(comingSoonMessage) }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // =========================================================
                    // 4. Ringkasan Finansial
                    // =========================================================
                    OwnerFinancialSummarySection(
                        pemasukan = uiState.pemasukanBulanIni,
                        pengeluaran = uiState.pengeluaranBulanIni,
                        labaRugi = uiState.labaRugiBulanIni,
                        margin = uiState.marginBulanIni,
                        jumlahPesananSelesai = uiState.jumlahPesananSelesaiBulanIni,
                        biggestExpense = uiState.biggestExpenseCategory,
                        trend = uiState.sixMonthTrend,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // =========================================================
                    // 5. Tren Pendapatan & Pengeluaran 6 Bulan (Canvas Compose)
                    // =========================================================
                    uiState.sixMonthTrend?.let { trend ->
                        MonthlyTrendChart(
                            trend = trend,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // =========================================================
                    // 6. Status Produksi Workshop (Grid 2x2)
                    // =========================================================
                    OwnerProductionStatusSection(
                        totalAktif = uiState.countPesananAktif,
                        countMasuk = uiState.countPesananMasuk,
                        countDiproses = uiState.countSedangDiproses,
                        countCetakPress = uiState.countCetakDanPress,
                        countSiapAmbil = uiState.countSiapDiambil,
                        onNavigateToOrders = onNavigateToOrders,
                        onNavigateToProcess = onNavigateToProcess,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // =========================================================
                    // 7. Pesanan Terbaru
                    // =========================================================
                    OwnerRecentOrdersSection(
                        orders = uiState.recentOrders,
                        onOrderClick = onNavigateToOrderDetail,
                        onSeeAll = onNavigateToOrders,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // =========================================================
                    // 8. Status Stok Material (Hanya Baca)
                    // =========================================================
                    OwnerStockMaterialSection(
                        bahanList = uiState.stockBahanProduk,
                        dtfList = uiState.stockKonsumabelDtf,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// =============================================================================
// 2. Bagian Salam & Identitas Sesi
// =============================================================================
@Composable
private fun OwnerGreetingSection(
    ownerName: String,
    rangeText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.owner_greeting_label),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = TextSecondary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Chip rentang bulan berjalan
            Surface(
                shape = ShapePill,
                color = SurfaceVariantColor,
                border = BorderStroke(0.5.dp, BorderHairline)
            ) {
                Text(
                    text = rangeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = ownerName,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(R.string.owner_greeting_sub),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =============================================================================
// 3. Aksi Cepat (4 Ubin vertikal: ikon di atas label)
// =============================================================================
@Composable
private fun OwnerQuickActionsSection(
    onBuatPesanan: () -> Unit,
    onCatatKeluar: () -> Unit,
    onKatalogProduk: () -> Unit,
    onTimSablon: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionTile(
            label = stringResource(R.string.owner_dashboard_action_create_order),
            icon = Icons.Default.AddShoppingCart,
            isPrimary = true,
            onClick = onBuatPesanan,
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            label = stringResource(R.string.owner_dashboard_action_record_expense),
            icon = Icons.Default.AttachMoney,
            isPrimary = false,
            onClick = onCatatKeluar,
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            label = stringResource(R.string.owner_dashboard_action_catalog),
            icon = Icons.Default.Layers,
            isPrimary = false,
            onClick = onKatalogProduk,
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            label = stringResource(R.string.owner_dashboard_action_team),
            icon = Icons.Default.People,
            isPrimary = false,
            onClick = onTimSablon,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(84.dp)
            .clickable(onClick = onClick),
        shape = ShapeControl,
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) InkBlack else SurfaceCard
        ),
        border = if (isPrimary) null else BorderStroke(1.dp, BorderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary) AccentYellow else TextPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                ),
                color = if (isPrimary) AccentYellow else TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// =============================================================================
// 4. Ringkasan Finansial (Laba Bersih besar + 2 kartu berdampingan)
// =============================================================================
@Composable
private fun OwnerFinancialSummarySection(
    pemasukan: Long,
    pengeluaran: Long,
    labaRugi: Long,
    margin: Double?,
    jumlahPesananSelesai: Int,
    biggestExpense: TransactionCategory?,
    trend: SixMonthTrend?,
    modifier: Modifier = Modifier
) {
    val isProfit = labaRugi >= 0L
    val marginText = if (margin != null) {
        String.format(Locale.US, "%.1f%%", margin)
    } else {
        "—"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.owner_dashboard_finance_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Kartu Hitam Besar "Laba Bersih"
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ShapeCard,
            colors = CardDefaults.cardColors(containerColor = InkBlack)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isProfit) AccentYellow else GeceColors.StatusCancelledText)
                        )
                        Text(
                            text = if (isProfit) {
                                stringResource(R.string.owner_dashboard_profit).uppercase(Locale.US)
                            } else {
                                stringResource(R.string.owner_dashboard_loss).uppercase(Locale.US)
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (isProfit) AccentYellow else GeceColors.StatusCancelledText,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Chip Margin
                    Surface(
                        shape = ShapePill,
                        color = if (isProfit) AccentYellow else GeceColors.StatusCancelledBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isProfit) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isProfit) InkBlack else GeceColors.StatusCancelledText,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "$marginText Margin",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isProfit) InkBlack else GeceColors.StatusCancelledText,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = Formatter.formatRupiah(if (isProfit) labaRugi else -labaRugi),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.owner_dashboard_finance_formula),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (isProfit) {
                            stringResource(R.string.owner_dashboard_margin_healthy)
                        } else {
                            stringResource(R.string.owner_dashboard_margin_defisit)
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isProfit) AccentYellow else GeceColors.StatusCancelledText,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dua Kartu Berdampingan: Total Pendapatan & Total Pengeluaran
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Kartu Total Pendapatan
            Card(
                modifier = Modifier.weight(1f),
                shape = ShapeCard,
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = stringResource(R.string.owner_dashboard_total_income),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = TextSecondary,
                            maxLines = 2,
                            softWrap = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Tampilkan perubahan jika pemasukan MTD bulan lalu > 0
                        if (trend?.incomeGrowthPercentage != null) {
                            val growth = trend.incomeGrowthPercentage
                            val sign = if (growth >= 0) "+" else ""
                            Text(
                                text = "$sign${String.format(Locale.US, "%.0f%%", growth)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (growth >= 0) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = Formatter.formatRupiah(pemasukan),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.owner_dashboard_orders_done_count, jumlahPesananSelesai),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }
            }

            // Kartu Total Pengeluaran
            Card(
                modifier = Modifier.weight(1f),
                shape = ShapeCard,
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderHairline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.owner_dashboard_total_expense),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = Formatter.formatRupiah(pengeluaran),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val catName = when (biggestExpense) {
                        TransactionCategory.BAHAN_BAKU -> stringResource(R.string.tx_category_bahan_baku)
                        TransactionCategory.MATERIAL_DTF -> stringResource(R.string.tx_category_material_dtf)
                        TransactionCategory.OPERASIONAL -> stringResource(R.string.tx_category_operasional)
                        TransactionCategory.GAJI -> stringResource(R.string.tx_category_gaji)
                        TransactionCategory.PENGELUARAN_LAINNYA -> stringResource(R.string.tx_category_pengeluaran_lainnya)
                        else -> "—"
                    }

                    Text(
                        text = stringResource(R.string.owner_dashboard_biggest_expense_prefix, catName),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// =============================================================================
// 6. Status Produksi Workshop (Grid 2x2)
// =============================================================================
@Composable
private fun OwnerProductionStatusSection(
    totalAktif: Int,
    countMasuk: Int,
    countDiproses: Int,
    countCetakPress: Int,
    countSiapAmbil: Int,
    onNavigateToOrders: () -> Unit,
    onNavigateToProcess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.owner_dashboard_production_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.owner_dashboard_production_active_count, totalAktif),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TextButton(
                onClick = onNavigateToProcess,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.owner_dashboard_queue_board),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid 2x2: Baris 1 (Pesanan Masuk & Sedang Diproses)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProductionStatusCard(
                count = countMasuk,
                label = stringResource(R.string.owner_dashboard_stat_diterima),
                icon = Icons.Default.ShoppingBag,
                isPriority = false,
                onClick = onNavigateToOrders,
                modifier = Modifier.weight(1f)
            )
            ProductionStatusCard(
                count = countDiproses,
                label = stringResource(R.string.owner_dashboard_stat_diproses),
                icon = Icons.Default.HourglassEmpty,
                isPriority = false,
                onClick = onNavigateToProcess,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid 2x2: Baris 2 (Cetak & Press [Prioritas/Hitam] & Siap Diambil)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProductionStatusCard(
                count = countCetakPress,
                label = stringResource(R.string.owner_dashboard_stat_cetak_press),
                icon = Icons.Default.Print,
                isPriority = true,
                onClick = onNavigateToProcess,
                modifier = Modifier.weight(1f)
            )
            ProductionStatusCard(
                count = countSiapAmbil,
                label = stringResource(R.string.owner_dashboard_stat_siap_ambil),
                icon = Icons.Default.Check,
                isPriority = false,
                onClick = onNavigateToOrders,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProductionStatusCard(
    count: Int,
    label: String,
    icon: ImageVector,
    isPriority: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = if (isPriority) InkBlack else SurfaceCard
        ),
        border = if (isPriority) null else BorderStroke(1.dp, BorderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPriority) AccentYellow else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )

                if (isPriority) {
                    Surface(
                        shape = ShapeBadge,
                        color = AccentYellow
                    ) {
                        Text(
                            text = stringResource(R.string.owner_dashboard_badge_priority).uppercase(Locale.US),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = InkBlack,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = if (isPriority) Color.White else TextPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = if (isPriority) Color.White.copy(alpha = 0.85f) else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// =============================================================================
// 7. Pesanan Terbaru (3-5 pesanan dengan ringkasan & StatusChip)
// =============================================================================
@Composable
private fun OwnerRecentOrdersSection(
    orders: List<Order>,
    onOrderClick: (String) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.owner_dashboard_recent_orders),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onSeeAll,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.owner_dashboard_see_all),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (orders.isEmpty()) {
            Text(
                text = "Belum ada pesanan terbaru.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            orders.forEach { order ->
                RecentOrderItemCard(
                    order = order,
                    onClick = { onOrderClick(order.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RecentOrderItemCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderHairline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.kode,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatusChip(status = order.status, showIcon = false)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = order.namaPelanggan,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Ringkasan produk
            val firstItem = order.items.firstOrNull()
            val productSummary = if (firstItem != null) {
                "${firstItem.namaProduk} (${firstItem.jumlah} pcs)"
            } else {
                "—"
            }

            Text(
                text = productSummary,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = Formatter.formatRupiah(order.total),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
    }
}

// =============================================================================
// 8. Status Stok Material (Hanya Baca, dari StockRepository)
// =============================================================================
@Composable
private fun OwnerStockMaterialSection(
    bahanList: List<StockItem>,
    dtfList: List<StockItem>,
    modifier: Modifier = Modifier
) {
    if (bahanList.isEmpty() && dtfList.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.owner_dashboard_stock_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Text(
            text = stringResource(R.string.owner_dashboard_stock_sub),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Kelompok 1: Bahan Produk (Tekstil)
        if (bahanList.isNotEmpty()) {
            StockGroupCard(
                groupTitle = stringResource(R.string.owner_dashboard_stock_group_baju),
                items = bahanList
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Kelompok 2: Konsumabel Cetak DTF
        if (dtfList.isNotEmpty()) {
            StockGroupCard(
                groupTitle = stringResource(R.string.owner_dashboard_stock_group_dtf),
                items = dtfList
            )
        }
    }
}

@Composable
private fun StockGroupCard(
    groupTitle: String,
    items: List<StockItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderHairline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = groupTitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.forEach { stock ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = ShapeControl,
                        color = if (stock.isMenipis) GeceColors.StatusPendingBg.copy(alpha = 0.25f) else GeceColors.Background,
                        border = BorderStroke(
                            0.5.dp,
                            if (stock.isMenipis) AccentYellow else BorderHairline
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = stock.nama,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${stock.jumlah} ${stock.satuan}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (stock.isMenipis) GeceColors.StatusCancelledText
                                            else GeceColors.StatusSuccessText
                                        )
                                )
                                Text(
                                    text = if (stock.isMenipis) {
                                        stringResource(R.string.owner_dashboard_stock_low)
                                    } else {
                                        stringResource(R.string.owner_dashboard_stock_available)
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (stock.isMenipis) GeceColors.StatusCancelledText
                                    else GeceColors.StatusSuccessText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
