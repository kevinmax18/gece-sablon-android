package com.gecesablon.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gecesablon.app.ui.admin.AdminAccountScreen
import com.gecesablon.app.ui.admin.AdminDashboardScreen
import com.gecesablon.app.ui.staff.StaffOrdersScreen
import com.gecesablon.app.ui.staff.StaffProcessScreen

/**
 * NavGraph untuk alur peran Admin Workshop:
 * - Tab Beranda, Pesanan, Proses, Akun
 */
fun NavGraphBuilder.adminNavGraph(navController: NavHostController) {
    navigation(startDestination = Routes.ADMIN_HOME, route = Routes.ADMIN_ROOT) {
        composable(Routes.ADMIN_HOME) {
            AdminDashboardScreen()
        }
        composable(Routes.ADMIN_ORDERS) {
            StaffOrdersScreen()
        }
        composable(Routes.ADMIN_PROCESS) {
            StaffProcessScreen()
        }
        composable(Routes.ADMIN_ACCOUNT) {
            AdminAccountScreen()
        }
    }
}
