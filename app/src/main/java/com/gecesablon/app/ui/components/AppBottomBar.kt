package com.gecesablon.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gecesablon.app.R
import com.gecesablon.app.ui.theme.GeceColors
import com.gecesablon.app.ui.theme.GeceSablonTheme

/**
 * Model item pada AppBottomBar yang independen terhadap modul navigasi.
 */
data class BottomBarItem(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

/**
 * Komponen bottom navigation bar bersama GeCe Screen Printing:
 * - Menerima daftar item sebagai parameter sehingga adaptif untuk Pelanggan, Admin, dan Owner
 * - Item terpilih diberi aksen pill kuning emulsi sesuai Figma
 * - Divider atas hairline 1px
 */
@Composable
fun AppBottomBar(
    items: List<BottomBarItem>,
    currentRoute: String?,
    onItemSelected: (BottomBarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GeceColors.Background
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onItemSelected(item) }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Wadah Ikon dengan aksen kuning saat aktif
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) GeceColors.Accent else Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgedBox(
                                badge = {
                                    if (item.badgeCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
                                            Text(text = item.badgeCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(item.labelResId),
                                    tint = if (isSelected) {
                                        GeceColors.OnAccent
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Label teks navigasi
                        Text(
                            text = stringResource(item.labelResId),
                            style = if (isSelected) {
                                MaterialTheme.typography.labelSmall
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "AppBottomBar - Pelanggan", showBackground = true)
@Composable
private fun AppBottomBarCustomerPreview() {
    val customerItems = listOf(
        BottomBarItem("home", R.string.nav_home, Icons.Default.GridView),
        BottomBarItem("catalog", R.string.nav_catalog, Icons.Default.Layers),
        BottomBarItem("order", R.string.nav_order, Icons.Default.ShoppingBag),
        BottomBarItem("contact", R.string.nav_contact, Icons.Default.Phone)
    )

    GeceSablonTheme {
        AppBottomBar(
            items = customerItems,
            currentRoute = "home",
            onItemSelected = {}
        )
    }
}

@Preview(name = "AppBottomBar - Admin", showBackground = true)
@Composable
private fun AppBottomBarAdminPreview() {
    val adminItems = listOf(
        BottomBarItem("admin_home", R.string.nav_home, Icons.Default.GridView),
        BottomBarItem("admin_orders", R.string.nav_orders, Icons.Default.ReceiptLong, badgeCount = 5),
        BottomBarItem("admin_process", R.string.nav_process, Icons.Default.HourglassEmpty),
        BottomBarItem("admin_account", R.string.nav_account, Icons.Default.Person)
    )

    GeceSablonTheme {
        AppBottomBar(
            items = adminItems,
            currentRoute = "admin_orders",
            onItemSelected = {}
        )
    }
}

@Preview(name = "AppBottomBar - Owner", showBackground = true)
@Composable
private fun AppBottomBarOwnerPreview() {
    val ownerItems = listOf(
        BottomBarItem("owner_home", R.string.nav_home, Icons.Default.GridView),
        BottomBarItem("owner_orders", R.string.nav_orders, Icons.Default.ReceiptLong),
        BottomBarItem("owner_process", R.string.nav_process, Icons.Default.HourglassEmpty),
        BottomBarItem("owner_finance", R.string.nav_finance, Icons.Default.AccountBalanceWallet),
        BottomBarItem("owner_account", R.string.nav_account, Icons.Default.Person)
    )

    GeceSablonTheme {
        AppBottomBar(
            items = ownerItems,
            currentRoute = "owner_finance",
            onItemSelected = {}
        )
    }
}
