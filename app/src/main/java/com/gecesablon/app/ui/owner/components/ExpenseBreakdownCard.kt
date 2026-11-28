package com.gecesablon.app.ui.owner.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.FinanceCalculator.ExpenseCategoryBreakdown
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.displayName
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl

/**
 * Kartu Rincian Pengeluaran / Struktur Beban.
 * - Judul dengan total di kanan
 * - Satu baris per kategori > 0, urut terbesar:
 *   nomor + nama (dari strings.xml), nominal, persen porsi (format id, mis. 48,8%),
 *   batang proporsi (terbesar hitam, kedua kuning, sisanya abu-abu gelap #737373).
 */
@Composable
fun ExpenseBreakdownCard(
    title: String,
    breakdown: List<ExpenseCategoryBreakdown>,
    totalExpense: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
        border = BorderStroke(1.dp, GeceColors.Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Baris: Judul di kiri, Total di kanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = GeceColors.TextMain,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = Formatter.formatRupiah(totalExpense),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = GeceColors.TextMain,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (breakdown.isEmpty() || totalExpense <= 0L) {
                Text(
                    text = stringResource(R.string.owner_report_no_expense),
                    style = MaterialTheme.typography.bodySmall,
                    color = GeceColors.TextMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    breakdown.forEachIndexed { index, item ->
                        val barColor = when (index) {
                            0 -> GeceColors.PrimaryInk
                            1 -> GeceColors.Accent
                            else -> GeceColors.TextMuted
                        }

                        val categoryName = when (item.category) {
                            TransactionCategory.BAHAN_BAKU -> stringResource(R.string.tx_category_bahan_baku)
                            TransactionCategory.MATERIAL_DTF -> stringResource(R.string.tx_category_material_dtf)
                            TransactionCategory.OPERASIONAL -> stringResource(R.string.tx_category_operasional)
                            TransactionCategory.GAJI -> stringResource(R.string.tx_category_gaji)
                            TransactionCategory.PENGELUARAN_LAINNYA -> stringResource(R.string.tx_category_pengeluaran_lainnya)
                            else -> item.category.displayName()
                        }

                        Column {
                            // Baris informasi: Nomor + Nama di kiri, Nominal di kanan
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. $categoryName",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = GeceColors.TextMain,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Text(
                                    text = Formatter.formatRupiah(item.total),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = GeceColors.TextMain,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Persentase porsi
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = stringResource(R.string.owner_finance_portion_format, Formatter.formatPercent(item.percentage, 1)),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = GeceColors.TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Batang proporsi
                            val ratio = (item.percentage / 100.0).toFloat().coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(GeceColors.Border, ShapeControl)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .height(8.dp)
                                        .background(barColor, ShapeControl)
                                )
                            }
                        }

                        if (index < breakdown.size - 1) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = GeceColors.Border,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
