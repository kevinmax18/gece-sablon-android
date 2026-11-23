package com.gecesablon.app.ui.navigation

import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.ui.account.AccountViewModel
import com.gecesablon.app.ui.customer.CatalogScreen
import com.gecesablon.app.ui.customer.CatalogViewModel
import com.gecesablon.app.ui.customer.ContactScreen
import com.gecesablon.app.ui.customer.CustomerHomeScreen
import com.gecesablon.app.ui.customer.HomeCustomerViewModel
import com.gecesablon.app.ui.customer.OrderListViewModel
import com.gecesablon.app.ui.customer.OrderScreen
import com.gecesablon.app.ui.customer.ProductDetailScreen
import com.gecesablon.app.ui.customer.ProductDetailViewModel
import com.gecesablon.app.ui.customer.ProfileScreen
import com.gecesablon.app.ui.customer.order.OrderConfirmScreen
import com.gecesablon.app.ui.customer.order.OrderDetailScreen
import com.gecesablon.app.ui.customer.order.OrderDetailViewModel
import com.gecesablon.app.ui.customer.order.OrderDraftViewModel
import com.gecesablon.app.ui.customer.order.OrderFormScreen
import com.gecesablon.app.ui.customer.order.OrderSuccessScreen

/**
 * NavGraph untuk alur peran Pelanggan (dan Tamu):
 * - Tab Beranda, Katalog, Pesan, Kontak
 * - Layar Detail Produk
 * - Nested NavGraph Alur Pemesanan (Form -> Konfirmasi)
 * - Layar Pesanan Berhasil
 * - Layar Detail Pesanan Pelanggan
 * - Layar Profil Pelanggan
 */
fun NavGraphBuilder.customerNavGraph(
    appContainer: AppContainer,
    navController: NavHostController,
    isGuest: Boolean,
    currentUserId: String,
    accountViewModel: AccountViewModel,
    onNavigateToLogin: (redirect: String?, productId: String?) -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    navigation(startDestination = Routes.CUSTOMER_HOME, route = Routes.CUSTOMER_ROOT) {
        composable(Routes.CUSTOMER_HOME) {
            val homeViewModel: HomeCustomerViewModel = viewModel(
                factory = HomeCustomerViewModel.factory(appContainer.productRepository)
            )
            CustomerHomeScreen(
                viewModel = homeViewModel,
                onProfileClick = {
                    if (isGuest) {
                        onNavigateToLogin(null, null)
                    } else {
                        navController.navigate(Routes.CUSTOMER_PROFILE)
                    }
                },
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetailRoute(productId))
                },
                onNavigateToCatalog = { _ ->
                    navController.navigate(Routes.CUSTOMER_CATALOG)
                }
            )
        }

        composable(Routes.CUSTOMER_CATALOG) {
            val catalogViewModel: CatalogViewModel = viewModel(
                factory = CatalogViewModel.factory(appContainer.productRepository)
            )
            CatalogScreen(
                viewModel = catalogViewModel,
                onProfileClick = {
                    if (isGuest) {
                        onNavigateToLogin(null, null)
                    } else {
                        navController.navigate(Routes.CUSTOMER_PROFILE)
                    }
                },
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetailRoute(productId))
                }
            )
        }

        composable(
            route = Routes.CUSTOMER_PRODUCT_DETAIL,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val productDetailViewModel: ProductDetailViewModel = viewModel(
                factory = ProductDetailViewModel.factory(appContainer.productRepository)
            )
            val currentProdId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                viewModel = productDetailViewModel,
                isGuest = isGuest,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProfileClick = {
                    if (isGuest) {
                        onNavigateToLogin("product", currentProdId)
                    } else {
                        navController.navigate(Routes.CUSTOMER_PROFILE)
                    }
                },
                onRequireLogin = { productId ->
                    onNavigateToLogin("product", productId)
                },
                onNavigateToOrderFlow = { productId ->
                    navController.navigate(Routes.orderFlowRoute(productId))
                }
            )
        }

        // Tab Pesan Pelanggan
        composable(Routes.CUSTOMER_ORDER) {
            val orderListViewModel: OrderListViewModel = viewModel(
                factory = OrderListViewModel.factory(
                    userId = currentUserId,
                    orderRepository = appContainer.orderRepository
                )
            )
            OrderScreen(
                viewModel = orderListViewModel,
                onOrderClick = { orderId ->
                    navController.navigate(Routes.orderDetailRoute(orderId))
                },
                onCreateNewOrder = {
                    navController.navigate(Routes.CUSTOMER_CATALOG)
                },
                onProfileClick = {
                    if (isGuest) {
                        onNavigateToLogin(null, null)
                    } else {
                        navController.navigate(Routes.CUSTOMER_PROFILE)
                    }
                }
            )
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
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToAbout = onNavigateToAbout,
                onLogout = onLogout
            )
        }

        // Nested Route Pemesanan (OrderDraftViewModel bersama)
        navigation(
            startDestination = Routes.ORDER_FORM,
            route = Routes.ORDER_FLOW_GRAPH,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                }
            )
        ) {
            composable(Routes.ORDER_FORM) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.ORDER_FLOW_GRAPH)
                }
                val productId = parentEntry.arguments?.getString("productId") ?: ""
                val draftViewModel: OrderDraftViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = OrderDraftViewModel.factory(
                        productId = productId,
                        productRepository = appContainer.productRepository,
                        orderRepository = appContainer.orderRepository
                    )
                )

                OrderFormScreen(
                    viewModel = draftViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToConfirm = {
                        navController.navigate(Routes.ORDER_CONFIRM)
                    }
                )
            }

            composable(Routes.ORDER_CONFIRM) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.ORDER_FLOW_GRAPH)
                }
                val productId = parentEntry.arguments?.getString("productId") ?: ""
                val draftViewModel: OrderDraftViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = OrderDraftViewModel.factory(
                        productId = productId,
                        productRepository = appContainer.productRepository,
                        orderRepository = appContainer.orderRepository
                    )
                )

                OrderConfirmScreen(
                    viewModel = draftViewModel,
                    userId = currentUserId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSubmitSuccess = { createdOrder ->
                        navController.navigate(Routes.orderSuccessRoute(createdOrder.id)) {
                            popUpTo(Routes.ORDER_FLOW_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Layar Pesanan Berhasil
        composable(
            route = Routes.ORDER_SUCCESS,
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderSuccessScreen(
                orderId = orderId,
                orderRepository = appContainer.orderRepository,
                onSeeOrderStatus = { targetOrderId ->
                    // Bersihkan order_success, tampilkan tab Pesan (customer_order) sebagai dasar,
                    // lalu buka order_detail di atasnya, sehingga back dari Detail kembali ke tab Pesan.
                    navController.navigate(Routes.CUSTOMER_ORDER) {
                        popUpTo(Routes.CUSTOMER_HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                    navController.navigate(Routes.orderDetailRoute(targetOrderId))
                },
                onBackToHome = {
                    navController.navigate(Routes.CUSTOMER_HOME) {
                        popUpTo(Routes.CUSTOMER_HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Layar Detail Pesanan Pelanggan
        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val detailViewModel: OrderDetailViewModel = viewModel(
                factory = OrderDetailViewModel.factory(
                    orderId = orderId,
                    orderRepository = appContainer.orderRepository
                )
            )
            OrderDetailScreen(
                viewModel = detailViewModel,
                userId = currentUserId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
