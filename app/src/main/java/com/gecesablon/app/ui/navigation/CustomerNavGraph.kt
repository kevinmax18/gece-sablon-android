package com.gecesablon.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gecesablon.app.ui.account.AccountViewModel
import com.gecesablon.app.ui.customer.CatalogScreen
import com.gecesablon.app.ui.customer.ContactScreen
import com.gecesablon.app.ui.customer.CustomerHomeScreen
import com.gecesablon.app.ui.customer.OrderScreen
import com.gecesablon.app.ui.customer.ProfileScreen

/**
 * NavGraph untuk alur peran Pelanggan (dan Tamu):
 * - Tab Beranda, Katalog, Pesan, Kontak
 * - Layar Profil Pelanggan
 */
fun NavGraphBuilder.customerNavGraph(
    navController: NavHostController,
    isGuest: Boolean,
    accountViewModel: AccountViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    navigation(startDestination = Routes.CUSTOMER_HOME, route = Routes.CUSTOMER_ROOT) {
        composable(Routes.CUSTOMER_HOME) {
            CustomerHomeScreen(
                onProfileClick = {
                    if (isGuest) {
                        onNavigateToLogin()
                    } else {
                        navController.navigate(Routes.CUSTOMER_PROFILE)
                    }
                }
            )
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
        composable(Routes.CUSTOMER_PROFILE) {
            ProfileScreen(
                viewModel = accountViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAbout = onNavigateToAbout,
                onLogout = onLogout
            )
        }
    }
}
