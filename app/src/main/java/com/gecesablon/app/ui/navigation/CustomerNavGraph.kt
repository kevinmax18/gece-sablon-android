package com.gecesablon.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gecesablon.app.ui.customer.CatalogScreen
import com.gecesablon.app.ui.customer.ContactScreen
import com.gecesablon.app.ui.customer.CustomerHomeScreen
import com.gecesablon.app.ui.customer.OrderScreen

/**
 * NavGraph untuk alur peran Pelanggan (dan Tamu):
 * - Tab Beranda, Katalog, Pesan, Kontak
 */
fun NavGraphBuilder.customerNavGraph(navController: NavHostController) {
    navigation(startDestination = Routes.CUSTOMER_HOME, route = Routes.CUSTOMER_ROOT) {
        composable(Routes.CUSTOMER_HOME) {
            CustomerHomeScreen()
        }
        composable(Routes.CUSTOMER_CATALOG) {
            CatalogScreen()
        }
        composable(Routes.CUSTOMER_ORDER) {
            OrderScreen()
        }
        composable(Routes.CUSTOMER_CONTACT) {
            ContactScreen()
        }
    }
}
