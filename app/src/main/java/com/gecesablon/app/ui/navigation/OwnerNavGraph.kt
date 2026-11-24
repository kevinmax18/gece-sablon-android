package com.gecesablon.app.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.ui.account.AccountViewModel
import com.gecesablon.app.ui.owner.OwnerAccountScreen
import com.gecesablon.app.ui.owner.OwnerAdminAccountsScreen
import com.gecesablon.app.ui.owner.OwnerAdminDetailScreen
import com.gecesablon.app.ui.owner.OwnerAdminFormScreen
import com.gecesablon.app.ui.owner.OwnerDashboardScreen
import com.gecesablon.app.ui.owner.OwnerDashboardViewModel
import com.gecesablon.app.ui.owner.OwnerFinanceScreen
import com.gecesablon.app.ui.owner.OwnerFinanceViewModel
import com.gecesablon.app.ui.owner.admin.OwnerAdminAccountsViewModel
import com.gecesablon.app.ui.owner.admin.OwnerAdminDetailViewModel
import com.gecesablon.app.ui.owner.admin.OwnerAdminFormViewModel
import com.gecesablon.app.ui.owner.finance.OwnerReportScreen
import com.gecesablon.app.ui.owner.finance.OwnerReportViewModel
import com.gecesablon.app.ui.owner.finance.OwnerTransactionDetailScreen
import com.gecesablon.app.ui.owner.finance.OwnerTransactionDetailViewModel
import com.gecesablon.app.ui.owner.finance.OwnerTransactionFormScreen
import com.gecesablon.app.ui.owner.finance.OwnerTransactionFormViewModel
import com.gecesablon.app.ui.owner.product.OwnerProductFormScreen
import com.gecesablon.app.ui.owner.product.OwnerProductFormViewModel
import com.gecesablon.app.ui.owner.product.OwnerProductsScreen
import com.gecesablon.app.ui.owner.product.OwnerProductsViewModel
import com.gecesablon.app.ui.staff.StaffOrdersScreen
import com.gecesablon.app.ui.staff.StaffProcessScreen

/**
 * NavGraph untuk alur peran Owner / Pemilik Workshop.
 * Tab: Beranda, Pesanan, Proses, Keuangan, Akun.
 * Sub-rute: Keuangan (detail, form, laporan) dan Kelola Produk (daftar, form).
 */
fun NavGraphBuilder.ownerNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    accountViewModel: AccountViewModel,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    currentSession: Session? = null
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
                },
                onNavigateToProducts = {
                    navController.navigate(Routes.OWNER_PRODUCTS)
                },
                onNavigateToAdminAccounts = {
                    navController.navigate(Routes.OWNER_ADMIN_ACCOUNTS)
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
                    orderRepository = appContainer.orderRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
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
                onNavigateToReport = {
                    navController.navigate(Routes.OWNER_REPORT)
                },
                onNavigateToTransactionDetail = { txId ->
                    navController.navigate(Routes.ownerTransactionDetailRoute(txId))
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

        // Tab Akun
        composable(Routes.OWNER_ACCOUNT) {
            OwnerAccountScreen(
                viewModel = accountViewModel,
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToManageProducts = {
                    navController.navigate(Routes.OWNER_PRODUCTS)
                },
                onNavigateToManageAdmins = {
                    navController.navigate(Routes.OWNER_ADMIN_ACCOUNTS)
                },
                onNavigateToAbout = onNavigateToAbout,
                onLogout = onLogout
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
                    orderRepository = appContainer.orderRepository,
                    sessionStore = appContainer.sessionStore
                )
            )
            OwnerReportScreen(
                viewModel = reportViewModel,
                onNavigateBack = { navController.popBackStack() },
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

        // Sub-rute: Kelola Produk (Tahap 6B) - Daftar Produk Owner
        composable(Routes.OWNER_PRODUCTS) {
            val role = currentSession?.role
            if (role != null && role != Role.OWNER) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                val productsViewModel: OwnerProductsViewModel = viewModel(
                    factory = OwnerProductsViewModel.factory(
                        productRepository = appContainer.productRepository,
                        sessionStore = appContainer.sessionStore
                    )
                )
                OwnerProductsScreen(
                    viewModel = productsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAdd = { navController.navigate(Routes.ownerProductFormRoute()) },
                    onNavigateToEdit = { id -> navController.navigate(Routes.ownerProductFormRoute(id)) },
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
        }

        // Sub-rute: Form Tambah/Ubah Produk Owner (Tahap 6B)
        composable(
            route = Routes.OWNER_PRODUCT_FORM,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val role = currentSession?.role
            if (role != null && role != Role.OWNER) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                val prodId = backStackEntry.arguments?.getString("productId")
                val formViewModel: OwnerProductFormViewModel = viewModel(
                    factory = OwnerProductFormViewModel.factory(
                        productRepository = appContainer.productRepository,
                        sessionStore = appContainer.sessionStore,
                        productId = prodId
                    )
                )
                OwnerProductFormScreen(
                    viewModel = formViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Sub-rute: Kelola Akun Admin Owner (Tahap 6B-2)
        composable(Routes.OWNER_ADMIN_ACCOUNTS) {
            val role = currentSession?.role
            if (role != null && role != Role.OWNER) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                val adminAccountsViewModel: OwnerAdminAccountsViewModel = viewModel(
                    factory = OwnerAdminAccountsViewModel.factory(
                        accountRepository = appContainer.accountRepository,
                        sessionStore = appContainer.sessionStore
                    )
                )
                OwnerAdminAccountsScreen(
                    viewModel = adminAccountsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAdd = { navController.navigate(Routes.OWNER_ADMIN_FORM) },
                    onNavigateToDetail = { id -> navController.navigate(Routes.ownerAdminDetailRoute(id)) }
                )
            }
        }

        // Sub-rute: Form Tambah Akun Admin
        composable(Routes.OWNER_ADMIN_FORM) {
            val role = currentSession?.role
            if (role != null && role != Role.OWNER) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                val adminFormViewModel: OwnerAdminFormViewModel = viewModel(
                    factory = OwnerAdminFormViewModel.factory(
                        accountRepository = appContainer.accountRepository,
                        sessionStore = appContainer.sessionStore
                    )
                )
                OwnerAdminFormScreen(
                    viewModel = adminFormViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Sub-rute: Detail Akun Admin & Ubah Status/Sandi
        composable(
            route = Routes.OWNER_ADMIN_DETAIL,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val role = currentSession?.role
            if (role != null && role != Role.OWNER) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                val adminId = backStackEntry.arguments?.getString("id") ?: ""
                val adminDetailViewModel: OwnerAdminDetailViewModel = viewModel(
                    factory = OwnerAdminDetailViewModel.factory(
                        accountRepository = appContainer.accountRepository,
                        sessionStore = appContainer.sessionStore,
                        accountId = adminId
                    )
                )
                OwnerAdminDetailScreen(
                    viewModel = adminDetailViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

