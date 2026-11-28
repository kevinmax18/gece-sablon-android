package com.gecesablon.app.ui.owner.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gecesablon.app.R
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.ui.theme.AccentYellow
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.InkBlack
import com.gecesablon.app.ui.theme.ShapeCard
import com.gecesablon.app.ui.theme.ShapePill
import com.gecesablon.app.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs

/**
 * Kartu hitam besar ringkasan laba/rugi bersih sesuai Figma:
 * - Baris atas: titik status + "LABA BERSIH" / "RUGI BERSIH" dan chip margin ("x,x% Margin" / "—")
 * - Nominal besar
 * - Garis pemisah
 * - Baris "Pemasukan Rp… - Pengeluaran Rp…"
 * - Chip perbandingan "vs periode sebelumnya +Rp…/-Rp…" (hanya bila pembanding punya pemasukan atau pengeluaran)
 */
@Composable
fun FinanceHeroCard(
    totalPemasukan: Long,
    totalPengeluaran: Long,
    labaRugi: Long,
    marginPercentage: Double?,
    diffProfitVsPrev: Long?, // null jika periode pembanding tidak punya pemasukan/pengeluaran
    modifier: Modifier = Modifier
) {
    val isProfit = labaRugi >= 0L
    val marginText = if (marginPercentage != null) {
        Formatter.formatPercent(marginPercentage, 1)
    } else {
        "\u2014"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = InkBlack)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Baris Atas: Titik status + Label & Chip Margin
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

                // Chip Margin di kanan
                Surface(
                    shape = ShapePill,
                    color = if (isProfit) AccentYellow else GeceColors.StatusCancelledBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (marginPercentage != null) {
                            Icon(
                                imageVector = if (isProfit) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isProfit) InkBlack else GeceColors.StatusCancelledText,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = if (marginPercentage != null) "$marginText Margin" else "\u2014 Margin",
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

            // Nominal Besar
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

            // Garis Pemisah
            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color.White.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Baris Formula & Chip Perbandingan vs Periode Sebelumnya
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.owner_finance_formula_row,
                        Formatter.formatRupiah(totalPemasukan),
                        Formatter.formatRupiah(totalPengeluaran)
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondary.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (diffProfitVsPrev != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val diffSign = if (diffProfitVsPrev >= 0) "+" else "-"
                    val diffFormatted = "$diffSign${Formatter.formatRupiah(abs(diffProfitVsPrev))}"
                    Text(
                        text = stringResource(R.string.owner_finance_vs_prev_diff, diffFormatted),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        ),
                        color = if (diffProfitVsPrev >= 0) AccentYellow else GeceColors.StatusCancelledText,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
