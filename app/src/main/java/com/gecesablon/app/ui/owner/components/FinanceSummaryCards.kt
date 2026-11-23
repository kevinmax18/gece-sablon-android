package com.gecesablon.app.ui.owner.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.displayName
import com.gecesablon.app.ui.theme.BorderHairline
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.SurfaceCard
import com.gecesablon.app.ui.theme.TextPrimary
import com.gecesablon.app.ui.theme.TextSecondary
import java.util.Locale

/**
 * Dua kartu ringkasan berdampingan: Pemasukan dan Pengeluaran.
 * Pemasukan: label, chip perubahan persen vs periode sebelumnya (hanya bila pembanding > 0), nominal, "n pesanan selesai".
 * Pengeluaran: label, chip "x% omzet" ("—" bila pemasukan 0), nominal, "Terbesar: <kategori>" (atau "Belum ada pengeluaran").
 */
@Composable
fun FinanceSummaryCards(
    totalPemasukan: Long,
    totalPengeluaran: Long,
    completedOrdersCount: Int,
    biggestExpenseCategory: TransactionCategory?,
    incomeGrowthPercentage: Double?, // null jika periode pembanding pemasukan <= 0
    expenseRatioToIncome: Double?,    // null jika pemasukan <= 0
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Kartu Total Pendapatan / Pemasukan
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
                            fontSize = 9.sp
                        ),
                        color = TextSecondary,
                        maxLines = 2,
                        softWrap = true,
                        modifier = Modifier.weight(1f)
                    )

                    if (incomeGrowthPercentage != null) {
                        val sign = if (incomeGrowthPercentage >= 0) "+" else ""
                        Text(
                            text = "$sign${String.format(Locale.US, "%.0f%%", incomeGrowthPercentage)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (incomeGrowthPercentage >= 0) GeceColors.StatusSuccessText else GeceColors.StatusCancelledText,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = Formatter.formatRupiah(totalPemasukan),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.owner_dashboard_orders_done_count, completedOrdersCount),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = stringResource(R.string.owner_dashboard_total_expense),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = TextSecondary,
                        maxLines = 2,
                        softWrap = true,
                        modifier = Modifier.weight(1f)
                    )

                    val ratioOmzetText = if (expenseRatioToIncome != null) {
                        stringResource(R.string.owner_finance_percent_omzet, Formatter.formatPercent(expenseRatioToIncome, 0))
                    } else {
                        stringResource(R.string.owner_finance_percent_omzet, "—")
                    }

                    Text(
                        text = ratioOmzetText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        ),
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = Formatter.formatRupiah(totalPengeluaran),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val catText = if (biggestExpenseCategory != null) {
                    val catName = when (biggestExpenseCategory) {
                        TransactionCategory.BAHAN_BAKU -> stringResource(R.string.tx_category_bahan_baku)
                        TransactionCategory.MATERIAL_DTF -> stringResource(R.string.tx_category_material_dtf)
                        TransactionCategory.OPERASIONAL -> stringResource(R.string.tx_category_operasional)
                        TransactionCategory.GAJI -> stringResource(R.string.tx_category_gaji)
                        TransactionCategory.PENGELUARAN_LAINNYA -> stringResource(R.string.tx_category_pengeluaran_lainnya)
                        else -> biggestExpenseCategory.displayName()
                    }
                    stringResource(R.string.owner_dashboard_biggest_expense_prefix, catName)
                } else {
                    stringResource(R.string.owner_finance_no_expense_yet)
                }

                Text(
                    text = catText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
