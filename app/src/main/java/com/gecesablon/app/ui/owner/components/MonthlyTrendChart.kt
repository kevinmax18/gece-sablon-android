package com.gecesablon.app.ui.owner.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.gecesablon.app.data.model.SixMonthTrend
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapeControl
import kotlin.math.max

/**
 * Komponen grafik batang berpasangan 6 bulan terakhir (pendapatan vs pengeluaran).
 * Dipindahkan dari OwnerDashboardScreen sehingga dapat digunakan bersama oleh Dashboard,
 * Tab Keuangan, dan Layar Laporan Keuangan Owner.
 */
@Composable
fun MonthlyTrendChart(
    trend: SixMonthTrend,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.owner_dashboard_trend_title),
    subtitle: String = stringResource(R.string.owner_dashboard_trend_sub)
) {
    val monthShortNames = stringArrayResource(R.array.month_short_names)
    val chartDescription = stringResource(R.string.owner_dashboard_trend_chart_desc)

    val incomeNormalColor = GeceColors.Accent
    val incomeCurrentColor = GeceColors.PrimaryInk
    val expenseNormalColor = MaterialTheme.colorScheme.surfaceVariant
    val expenseCurrentColor = GeceColors.TextMuted
    val gridLineColor = GeceColors.Border

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = GeceColors.Surface),
        border = BorderStroke(1.dp, GeceColors.Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Tren 6 Bulan: Judul di baris sendiri
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = GeceColors.TextMain,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subjudul di baris sendiri
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = GeceColors.TextMuted,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Legenda di baris sendiri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(incomeNormalColor)
                    )
                    Text(
                        text = stringResource(R.string.owner_dashboard_legend_income),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = GeceColors.TextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(expenseNormalColor)
                    )
                    Text(
                        text = stringResource(R.string.owner_dashboard_legend_expense),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = GeceColors.TextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Canvas Grafik Batang Berpasangan 6 Bulan
            val maxValue = max(
                1L,
                trend.monthlyBars.maxOfOrNull { max(it.totalIncome, it.totalExpense) } ?: 1L
            ).toFloat()

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .semantics { contentDescription = chartDescription }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = canvasHeight * (i.toFloat() / gridLines.toFloat())
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 0.8f
                    )
                }

                val groupCount = trend.monthlyBars.size.coerceAtLeast(1)
                val groupWidth = canvasWidth / groupCount.toFloat()
                val barWidth = (groupWidth * 0.28f).coerceAtMost(16.dp.toPx())
                val barSpacing = 3.dp.toPx()
                val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                trend.monthlyBars.forEachIndexed { index, bar ->
                    val groupCenterX = (index * groupWidth) + (groupWidth / 2f)

                    val incomeHeight = (bar.totalIncome.toFloat() / maxValue) * (canvasHeight - 6.dp.toPx())
                    val expenseHeight = (bar.totalExpense.toFloat() / maxValue) * (canvasHeight - 6.dp.toPx())

                    val incomeLeft = groupCenterX - barWidth - (barSpacing / 2f)
                    val expenseLeft = groupCenterX + (barSpacing / 2f)

                    // Batang Pendapatan (Kiri)
                    if (incomeHeight > 0f) {
                        drawRoundRect(
                            color = if (bar.isCurrentMonth) incomeCurrentColor else incomeNormalColor,
                            topLeft = Offset(incomeLeft, canvasHeight - incomeHeight),
                            size = Size(barWidth, incomeHeight),
                            cornerRadius = corner
                        )
                    }

                    // Batang Pengeluaran (Kanan)
                    if (expenseHeight > 0f) {
                        drawRoundRect(
                            color = if (bar.isCurrentMonth) expenseCurrentColor else expenseNormalColor,
                            topLeft = Offset(expenseLeft, canvasHeight - expenseHeight),
                            size = Size(barWidth, expenseHeight),
                            cornerRadius = corner
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Label Bulan (Row Text berbobot sama)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trend.monthlyBars.forEach { bar ->
                    val monthName = if (bar.monthIndex in monthShortNames.indices) {
                        monthShortNames[bar.monthIndex]
                    } else {
                        "Bln"
                    }
                    val label = if (bar.isCurrentMonth) "$monthName*" else monthName

                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (bar.isCurrentMonth) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (bar.isCurrentMonth) GeceColors.TextMain else GeceColors.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Baris Rata-rata Margin
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeControl,
                color = GeceColors.Background,
                border = BorderStroke(0.5.dp, GeceColors.Border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.owner_dashboard_avg_margin),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = GeceColors.TextMuted
                    )

                    val avgMarginText = if (trend.averageMarginPercentage != null) {
                        Formatter.formatPercent(trend.averageMarginPercentage, 1)
                    } else {
                        "—"
                    }

                    Text(
                        text = avgMarginText,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GeceColors.TextMain
                    )
                }
            }
        }
    }
}
