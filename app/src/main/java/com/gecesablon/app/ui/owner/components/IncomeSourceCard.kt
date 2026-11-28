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
import com.gecesablon.app.core.util.FinanceCalculator.IncomeSourcesSummary
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeCard

/**
 * Kartu Sumber Pemasukan.
 * - Total di kanan
 * - Dua baris: "Pesanan Selesai" (n pesanan) dan "Pemasukan Lain",
 *   masing-masing nominal dan persen.
 */
@Composable
fun IncomeSourceCard(
    sources: IncomeSourcesSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
        border = BorderStroke(1.dp, GeceColors.Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Baris: "Sumber Pemasukan" di kiri, Total di kanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.owner_finance_income_source_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = GeceColors.TextMain,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = Formatter.formatRupiah(sources.totalIncome),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = GeceColors.TextMain,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Baris 1: Pesanan Selesai
            val ordersPctText = if (sources.ordersPercentage != null) {
                Formatter.formatPercent(sources.ordersPercentage, 1)
            } else {
                "\u2014"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = stringResource(R.string.owner_finance_source_orders),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = GeceColors.TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.owner_finance_source_orders_count, sources.completedOrdersCount),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = GeceColors.TextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatter.formatRupiah(sources.totalOrdersIncome),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GeceColors.TextMain,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ordersPctText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = GeceColors.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = GeceColors.Border)
            Spacer(modifier = Modifier.height(10.dp))

            // Baris 2: Pemasukan Lain (Manual)
            val manualPctText = if (sources.manualPercentage != null) {
                Formatter.formatPercent(sources.manualPercentage, 1)
            } else {
                "\u2014"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = stringResource(R.string.owner_finance_source_manual),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = GeceColors.TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatter.formatRupiah(sources.totalManualIncome),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GeceColors.TextMain,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = manualPctText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = GeceColors.TextMuted
                    )
                }
            }
        }
    }
}
