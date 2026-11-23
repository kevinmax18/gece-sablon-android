package com.gecesablon.app.ui.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.ui.account.AccountViewModel
import com.gecesablon.app.ui.owner.OwnerAccountScreen
import com.gecesablon.app.ui.owner.OwnerDashboardScreen
import com.gecesablon.app.ui.owner.OwnerDashboardViewModel
import com.gecesablon.app.ui.owner.OwnerFinanceScreen
import com.gecesablon.app.ui.owner.OwnerFinanceViewModel
import com.gecesablon.app.ui.owner.finance.OwnerFinanceDetailScreen
import com.gecesablon.app.ui.owner.finance.OwnerFinanceDetailViewModel
import com.gecesablon.app.ui.owner.finance.OwnerReportScreen
import com.gecesablon.app.ui.owner.finance.OwnerReportViewModel
import com.gecesablon.app.ui.owner.finance.OwnerTransactionDetailScreen
import com.gecesablon.app.ui.owner.finance.OwnerTransactionDetailViewModel
import com.gecesablon.app.ui.owner.finance.OwnerTransactionFormScreen
import com.gecesablon.app.ui.owner.finance.OwnerTransactionFormViewModel
import com.gecesablon.app.ui.staff.StaffOrdersScreen
import com.gecesablon.app.ui.staff.StaffProcessScreen

/**
 * NavGraph untuk alur peran Owner / Pemilik Workshop.
 * Tab: Beranda, Pesanan, Proses, Keuangan, Akun.
 * Sub-rute keuangan: detail, form transaksi, detail transaksi, laporan.
 */
fun NavGraphBuilder.ownerNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    accountViewModel: AccountViewModel,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    navigation(startDestination = Routes.OWNER_HOME, route = Routes.OWNER_ROOT) {

        // Tab Beranda (Dashboard Owner)
        composable(Routes.OWNER_HOME) {
            val dashboardViewModel: OwnerDashboardViewModel = viewModel(
                factory = OwnerDashboardViewModel.factory(
                    orderRepository = appContainer.orderRepository,
                    financeRepository = appContainer.financeRepository,
                    stockRepository = appContainer.stockRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            OwnerDashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToCreateManualOrder = {
                    navController.navigate(Routes.STAFF_MANUAL_ORDER)
                },
                onNavigateToRecordExpense = {
                    navController.navigate(
                        Routes.ownerTransactionFormRoute(type = TransactionType.PENGELUARAN.name)
                    )
                },
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate(Routes.staffOrderDetailRoute(orderId))
                },
                onNavigateToOrders = {
                    navController.navigate(Routes.OWNER_ORDERS) {
                        popUpTo(Routes.OWNER_HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProcess = {
                    navController.navigate(Routes.OWNER_PROCESS) {
                        popUpTo(Routes.OWNER_HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToAccount = {
                    navController.navigate(Routes.OWNER_ACCOUNT) {
                        popUpTo(Routes.OWNER_HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // Tab Pesanan (pakai StaffOrdersScreen yang sudah ada)
        composable(Routes.OWNER_ORDERS) {
            StaffOrdersScreen(
                onNavigateToDetail = { orderId ->
                    navController.navigate(Routes.staffOrderDetailRoute(orderId))
                }
            )
        }

        // Tab Proses (pakai StaffProcessScreen yang sudah ada)
        composable(Routes.OWNER_PROCESS) {
            StaffProcessScreen(
                onNavigateToDetail = { orderId ->
                    navController.navigate(Routes.staffOrderDetailRoute(orderId))
                }
            )
        }

        // Tab Keuangan
        composable(Routes.OWNER_FINANCE) {
            val financeViewModel: OwnerFinanceViewModel = viewModel(
                factory = OwnerFinanceViewModel.factory(
                    financeRepository = appContainer.financeRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            val uiState = financeViewModel.uiState
            OwnerFinanceScreen(
                viewModel = financeViewModel,
                onNavigateToRecordIncome = {
                    navController.navigate(
                        Routes.ownerTransactionFormRoute(type = TransactionType.PEMASUKAN.name)
                    )
                },
                onNavigateToRecordExpense = {
                    navController.navigate(
                        Routes.ownerTransactionFormRoute(type = TransactionType.PENGELUARAN.name)
                    )
                },
                onNavigateToFinanceDetail = { startMs, endMs ->
                    navController.navigate("owner_finance_detail?startMillis=$startMs&endMillis=$endMs")
                },
                onNavigateToReport = {
                    navController.navigate(Routes.OWNER_REPORT)
                },
                onNavigateToTransactionDetail = { txId ->
                    navController.navigate(Routes.ownerTransactionDetailRoute(txId))
                }
            )
        }

        // Tab Akun
        composable(Routes.OWNER_ACCOUNT) {
            OwnerAccountScreen(
                viewModel = accountViewModel,
                onNavigateToAbout = onNavigateToAbout,
                onLogout = onLogout
            )
        }

        // Sub-rute: Detail semua transaksi suatu periode
        // Rute: owner_finance_detail?startMillis={startMillis}&endMillis={endMillis}
        composable(
            route = "owner_finance_detail?startMillis={startMillis}&endMillis={endMillis}",
            arguments = listOf(
                navArgument("startMillis") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("endMillis") {
                    type = NavType.LongType
                    defaultValue = Long.MAX_VALUE
                }
            )
        ) { backStackEntry ->
            val startMillis = backStackEntry.arguments?.getLong("startMillis") ?: 0L
            val endMillis = backStackEntry.arguments?.getLong("endMillis") ?: Long.MAX_VALUE
            val detailViewModel: OwnerFinanceDetailViewModel = viewModel(
                factory = OwnerFinanceDetailViewModel.factory(
                    financeRepository = appContainer.financeRepository,
                    sessionStore = appContainer.sessionStore,
                    startMillis = startMillis,
                    endMillis = endMillis
                )
            )
            OwnerFinanceDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTransactionDetail = { txId ->
                    navController.navigate(Routes.ownerTransactionDetailRoute(txId))
                }
            )
        }

        // Sub-rute: Form tambah/ubah transaksi
        // Rute: owner_transaction_form?transactionId={transactionId}&type={type}
        composable(
            route = Routes.OWNER_TRANSACTION_FORM,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val txId = backStackEntry.arguments?.getString("transactionId")
            val typeStr = backStackEntry.arguments?.getString("type")
            // type selain PEMASUKAN/PENGELUARAN diabaikan
            val initialType = when (typeStr) {
                TransactionType.PEMASUKAN.name -> TransactionType.PEMASUKAN
                TransactionType.PENGELUARAN.name -> TransactionType.PENGELUARAN
                else -> null
            }
            val formViewModel: OwnerTransactionFormViewModel = viewModel(
                factory = OwnerTransactionFormViewModel.factory(
                    financeRepository = appContainer.financeRepository,
                    sessionStore = appContainer.sessionStore,
                    transactionId = txId,
                    initialType = initialType
                )
            )
            OwnerTransactionFormScreen(
                viewModel = formViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Sub-rute: Detail satu transaksi
        composable(
            route = Routes.OWNER_TRANSACTION_DETAIL,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val txId = backStackEntry.arguments?.getString("transactionId") ?: ""
            val txDetailViewModel: OwnerTransactionDetailViewModel = viewModel(
                factory = OwnerTransactionDetailViewModel.factory(
                    financeRepository = appContainer.financeRepository,
                    sessionStore = appContainer.sessionStore,
                    transactionId = txId
                )
            )
            OwnerTransactionDetailScreen(
                viewModel = txDetailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Routes.ownerTransactionFormRoute(transactionId = id))
                },
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate(Routes.staffOrderDetailRoute(orderId))
                }
            )
        }

        // Sub-rute: Laporan keuangan
        composable(Routes.OWNER_REPORT) {
            val reportViewModel: OwnerReportViewModel = viewModel(
                factory = OwnerReportViewModel.factory(
                    financeRepository = appContainer.financeRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            OwnerReportScreen(
                viewModel = reportViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

