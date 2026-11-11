package com.gecesablon.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import com.gecesablon.app.R
import com.gecesablon.app.ui.components.BottomBarItem

/**
 * Mendefinisikan item menu pada Bottom Navigation Bar untuk tiap peran pengguna.
 */
object BottomNavItems {

    val customerItems = listOf(
        BottomBarItem(
            route = Routes.CUSTOMER_HOME,
            labelResId = R.string.nav_home,
            icon = Icons.Default.GridView
        ),
        BottomBarItem(
            route = Routes.CUSTOMER_CATALOG,
            labelResId = R.string.nav_catalog,
            icon = Icons.Default.Layers
        ),
        BottomBarItem(
            route = Routes.CUSTOMER_ORDER,
            labelResId = R.string.nav_order,
            icon = Icons.Default.ShoppingBag
        ),
        BottomBarItem(
            route = Routes.CUSTOMER_CONTACT,
            labelResId = R.string.nav_contact,
            icon = Icons.Default.Phone
        )
    )

    val adminItems = listOf(
        BottomBarItem(
            route = Routes.ADMIN_HOME,
            labelResId = R.string.nav_home,
            icon = Icons.Default.GridView
        ),
        BottomBarItem(
            route = Routes.ADMIN_ORDERS,
            labelResId = R.string.nav_orders,
            icon = Icons.Default.ReceiptLong
        ),
        BottomBarItem(
            route = Routes.ADMIN_PROCESS,
            labelResId = R.string.nav_process,
            icon = Icons.Default.HourglassEmpty
        ),
        BottomBarItem(
            route = Routes.ADMIN_ACCOUNT,
            labelResId = R.string.nav_account,
            icon = Icons.Default.Person
        )
    )

    val ownerItems = listOf(
        BottomBarItem(
            route = Routes.OWNER_HOME,
            labelResId = R.string.nav_home,
            icon = Icons.Default.GridView
        ),
        BottomBarItem(
            route = Routes.OWNER_ORDERS,
            labelResId = R.string.nav_orders,
            icon = Icons.Default.ReceiptLong
        ),
        BottomBarItem(
            route = Routes.OWNER_PROCESS,
            labelResId = R.string.nav_process,
            icon = Icons.Default.HourglassEmpty
        ),
        BottomBarItem(
            route = Routes.OWNER_FINANCE,
            labelResId = R.string.nav_finance,
            icon = Icons.Default.AccountBalanceWallet
        ),
        BottomBarItem(
            route = Routes.OWNER_ACCOUNT,
            labelResId = R.string.nav_account,
            icon = Icons.Default.Person
        )
    )
}
