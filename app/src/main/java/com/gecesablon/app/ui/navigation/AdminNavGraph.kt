package com.gecesablon.app.ui.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.ui.account.AccountViewModel
import com.gecesablon.app.ui.admin.AdminAccountScreen
import com.gecesablon.app.ui.admin.AdminDashboardScreen
import com.gecesablon.app.ui.staff.StaffOrdersScreen
import com.gecesablon.app.ui.staff.StaffOrdersViewModel
import com.gecesablon.app.ui.staff.StaffProcessScreen
import com.gecesablon.app.ui.staff.StaffProcessViewModel

/**
 * NavGraph untuk alur peran Admin Workshop:
 * - Tab Beranda, Pesanan, Proses, Akun
 */
fun NavGraphBuilder.adminNavGraph(
    appContainer: AppContainer,
    navController: NavHostController,
    accountViewModel: AccountViewModel,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    navigation(startDestination = Routes.ADMIN_HOME, route = Routes.ADMIN_ROOT) {
        composable(Routes.ADMIN_HOME) {
            AdminDashboardScreen()
        }
        composable(Routes.ADMIN_ORDERS) {
            val ordersViewModel: StaffOrdersViewModel = viewModel(
                factory = StaffOrdersViewModel.factory(
                    orderRepository = appContainer.orderRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            StaffOrdersScreen(
                viewModel = ordersViewModel,
                onNavigateToDetail = { orderId ->
                    navController.navigate(Routes.staffOrderDetailRoute(orderId))
                }
            )
        }
        composable(Routes.ADMIN_PROCESS) {
            val processViewModel: StaffProcessViewModel = viewModel(
                factory = StaffProcessViewModel.factory(
                    orderRepository = appContainer.orderRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            StaffProcessScreen(
                viewModel = processViewModel,
                onNavigateToDetail = { orderId ->
                    navController.navigate(Routes.staffOrderDetailRoute(orderId))
                }
            )
        }
        composable(Routes.ADMIN_ACCOUNT) {
            AdminAccountScreen(
                viewModel = accountViewModel,
                onNavigateToAbout = onNavigateToAbout,
                onLogout = onLogout
            )
        }
    }
}
