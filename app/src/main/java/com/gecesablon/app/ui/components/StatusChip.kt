package com.gecesablon.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.GeceSablonTheme
import com.gecesablon.app.ui.theme.ShapePill

/**
 * Komponen chip untuk menampilkan status pesanan (OrderStatus) resmi GeCe Screen Printing:
 * Diterima, Sedang Diproses, Cetak DTF, Pressing, Siap Diambil, Selesai, Dibatalkan.
 */
@Composable
fun StatusChip(
    status: OrderStatus,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val (containerColor, contentColor, icon) = when (status) {
        OrderStatus.DITERIMA -> Triple(
            GeceColors.StatusPendingBg,
            GeceColors.StatusPendingText,
            Icons.Default.AccessTime
        )
        OrderStatus.SEDANG_DIPROSES -> Triple(
            GeceColors.StatusProcessBg,
            GeceColors.StatusProcessText,
            Icons.Default.Sync
        )
        OrderStatus.CETAK_DTF -> Triple(
            GeceColors.StatusProcessBg,
            GeceColors.StatusProcessText,
            Icons.Default.Print
        )
        OrderStatus.PRESSING -> Triple(
            GeceColors.StatusProcessBg,
            GeceColors.StatusProcessText,
            Icons.Default.Sync
        )
        OrderStatus.SIAP_DIAMBIL -> Triple(
            GeceColors.StatusReadyBg,
            GeceColors.StatusReadyText,
            Icons.Default.LocalShipping
        )
        OrderStatus.SELESAI -> Triple(
            GeceColors.StatusSuccessBg,
            GeceColors.StatusSuccessText,
            Icons.Default.CheckCircle
        )
        OrderStatus.DIBATALKAN -> Triple(
            GeceColors.StatusCancelledBg,
            GeceColors.StatusCancelledText,
            Icons.Default.Close
        )
    }

    Box(
        modifier = modifier
            .clip(ShapePill)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showIcon) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = stringResource(status.labelResId),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "StatusChip - Seluruh Status", showBackground = true)
@Composable
private fun StatusChipPreview() {
    GeceSablonTheme {
        FlowRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrderStatus.entries.forEach { status ->
                StatusChip(status = status)
            }
        }
    }
}
