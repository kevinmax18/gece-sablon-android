package com.gecesablon.app.ui.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.ui.account.AccountViewModel
import com.gecesablon.app.ui.admin.AdminAccountScreen
import com.gecesablon.app.ui.admin.AdminDashboardScreen
import com.gecesablon.app.ui.admin.AdminDashboardViewModel
import com.gecesablon.app.ui.admin.order.AdminManualOrderScreen
import com.gecesablon.app.ui.admin.order.AdminManualOrderViewModel
import com.gecesablon.app.ui.admin.stock.AdminStockFormScreen
import com.gecesablon.app.ui.admin.stock.AdminStockFormViewModel
import com.gecesablon.app.ui.admin.stock.AdminStockScreen
import com.gecesablon.app.ui.admin.stock.AdminStockViewModel
import com.gecesablon.app.ui.staff.StaffOrdersScreen
import com.gecesablon.app.ui.staff.StaffOrdersViewModel
import com.gecesablon.app.ui.staff.StaffProcessScreen
import com.gecesablon.app.ui.staff.StaffProcessViewModel

/**
 * NavGraph untuk alur peran Admin Workshop:
 * - Tab Beranda, Pesanan, Proses, Akun
 * - Sub-rute: Stok (Daftar & Form), Pesanan Manual
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
            val dashboardViewModel: AdminDashboardViewModel = viewModel(
                factory = AdminDashboardViewModel.factory(
                    orderRepository = appContainer.orderRepository,
                    stockRepository = appContainer.stockRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            AdminDashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToCreateManualOrder = {
                    navController.navigate(Routes.ADMIN_MANUAL_ORDER)
                },
                onNavigateToOrders = {
                    navController.navigate(Routes.ADMIN_ORDERS)
                },
                onNavigateToStock = {
                    navController.navigate(Routes.ADMIN_STOCK)
                },
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate(Routes.staffOrderDetailRoute(orderId))
                },
                onNavigateToAccount = {
                    navController.navigate(Routes.ADMIN_ACCOUNT)
                }
            )
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
                },
                onNavigateToAccount = {
                    navController.navigate(Routes.ADMIN_ACCOUNT)
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
                },
                onNavigateToAccount = {
                    navController.navigate(Routes.ADMIN_ACCOUNT)
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

        // Sub-rute: Kelola Stok (Daftar)
        composable(Routes.ADMIN_STOCK) {
            val stockViewModel: AdminStockViewModel = viewModel(
                factory = AdminStockViewModel.factory(
                    stockRepository = appContainer.stockRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            AdminStockScreen(
                viewModel = stockViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdd = { navController.navigate(Routes.adminStockFormRoute()) },
                onNavigateToEdit = { stockId -> navController.navigate(Routes.adminStockFormRoute(stockId)) }
            )
        }

        // Sub-rute: Form Tambah / Ubah Stok
        composable(
            route = Routes.ADMIN_STOCK_FORM,
            arguments = listOf(
                navArgument("stockId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val stockId = backStackEntry.arguments?.getString("stockId")
            val formViewModel: AdminStockFormViewModel = viewModel(
                factory = AdminStockFormViewModel.factory(
                    stockId = stockId,
                    stockRepository = appContainer.stockRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            AdminStockFormScreen(
                viewModel = formViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        // Sub-rute: Pesanan Manual
        composable(Routes.ADMIN_MANUAL_ORDER) {
            val manualOrderViewModel: AdminManualOrderViewModel = viewModel(
                factory = AdminManualOrderViewModel.factory(
                    orderRepository = appContainer.orderRepository,
                    productRepository = appContainer.productRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            AdminManualOrderScreen(
                viewModel = manualOrderViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOrderCreated = { createdOrder ->
                    navController.navigate(Routes.staffOrderDetailRoute(createdOrder.id)) {
                        popUpTo(Routes.ADMIN_ORDERS) { inclusive = false }
                    }
                }
            )
        }
    }
}
