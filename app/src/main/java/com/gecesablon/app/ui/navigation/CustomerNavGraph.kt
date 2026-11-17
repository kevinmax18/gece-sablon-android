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
import com.gecesablon.app.ui.customer.CatalogScreen
import com.gecesablon.app.ui.customer.CatalogViewModel
import com.gecesablon.app.ui.customer.ContactScreen
import com.gecesablon.app.ui.customer.CustomerHomeScreen
import com.gecesablon.app.ui.customer.HomeCustomerViewModel
import com.gecesablon.app.ui.customer.OrderScreen
import com.gecesablon.app.ui.customer.ProductDetailScreen
import com.gecesablon.app.ui.customer.ProductDetailViewModel
import com.gecesablon.app.ui.customer.ProfileScreen

/**
 * NavGraph untuk alur peran Pelanggan (dan Tamu):
 * - Tab Beranda, Katalog, Pesan, Kontak
 * - Layar Detail Produk
 * - Layar Profil Pelanggan
 */
fun NavGraphBuilder.customerNavGraph(
    appContainer: AppContainer,
    navController: NavHostController,
    isGuest: Boolean,
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
                }
            )
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
