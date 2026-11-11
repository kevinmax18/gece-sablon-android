package com.gecesablon.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gecesablon.app.ui.owner.OwnerAccountScreen
import com.gecesablon.app.ui.owner.OwnerDashboardScreen
import com.gecesablon.app.ui.owner.OwnerFinanceScreen
import com.gecesablon.app.ui.staff.StaffOrdersScreen
import com.gecesablon.app.ui.staff.StaffProcessScreen

/**
 * NavGraph untuk alur peran Owner / Pemilik Workshop:
 * - Tab Beranda, Pesanan, Proses, Keuangan, Akun
 */
fun NavGraphBuilder.ownerNavGraph(navController: NavHostController) {
    navigation(startDestination = Routes.OWNER_HOME, route = Routes.OWNER_ROOT) {
        composable(Routes.OWNER_HOME) {
            OwnerDashboardScreen()
        }
        composable(Routes.OWNER_ORDERS) {
            StaffOrdersScreen()
        }
        composable(Routes.OWNER_PROCESS) {
            StaffProcessScreen()
        }
        composable(Routes.OWNER_FINANCE) {
            OwnerFinanceScreen()
        }
        composable(Routes.OWNER_ACCOUNT) {
            OwnerAccountScreen()
        }
    }
}
