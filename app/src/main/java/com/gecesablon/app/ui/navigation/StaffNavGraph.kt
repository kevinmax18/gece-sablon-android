package com.gecesablon.app.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.ui.admin.order.AdminManualOrderScreen
import com.gecesablon.app.ui.admin.order.AdminManualOrderViewModel
import com.gecesablon.app.ui.staff.StaffOrderDetailScreen
import com.gecesablon.app.ui.staff.StaffOrderDetailViewModel

/**
 * Mendaftarkan rute-rute staf bersama di level AppNavHost yang netral peran (dipakai Admin dan Owner).
 * Memeriksa izin peran: hanya akun dengan peran ADMIN atau OWNER yang dapat mengakses layar ini.
 */
fun NavGraphBuilder.staffOrderRoutes(
    appContainer: AppContainer,
    navController: NavHostController,
    currentSession: Session?
) {
    // =========================================================================
    // Rute: Detail Pesanan Staf (staff_order_detail/{orderId})
    // =========================================================================
    composable(
        route = Routes.STAFF_ORDER_DETAIL,
        arguments = listOf(
            navArgument("orderId") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val role = currentSession?.role
        if (role != Role.ADMIN && role != Role.OWNER) {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        } else {
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val detailViewModel: StaffOrderDetailViewModel = viewModel(
                factory = StaffOrderDetailViewModel.factory(
                    orderId = orderId,
                    orderRepository = appContainer.orderRepository,
                    sessionStore = appContainer.sessionStore
                )
            )

            StaffOrderDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }

    // =========================================================================
    // Rute: Pesanan Manual Staf Bersama (staff_manual_order)
    // Menggantikan admin_manual_order; dapat diakses ADMIN dan OWNER.
    // Setelah berhasil:
    //   1. Keluarkan form dari back stack (popUpTo STAFF_MANUAL_ORDER inclusive=true)
    //   2. Navigasikan ke tab Pesanan role aktif sebagai dasar
    //   3. Buka detail pesanan yang baru dibuat
    //   -> Back dari Detail Pesanan mendarat ke tab Pesanan role aktif
    // =========================================================================
    composable(Routes.STAFF_MANUAL_ORDER) {
        val role = currentSession?.role
        if (role != Role.ADMIN && role != Role.OWNER) {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        } else {
            val manualOrderViewModel: AdminManualOrderViewModel = viewModel(
                factory = AdminManualOrderViewModel.factory(
                    orderRepository = appContainer.orderRepository,
                    productRepository = appContainer.productRepository,
                    sessionStore = appContainer.sessionStore
                )
            )

            AdminManualOrderScreen(
                viewModel = manualOrderViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderCreated = { createdOrder ->
                    // Tab Pesanan role aktif sesuai peran
                    val ordersTab = if (role == Role.OWNER) Routes.OWNER_ORDERS else Routes.ADMIN_ORDERS
                    // Keluarkan form manual order dari back stack
                    navController.navigate(ordersTab) {
                        popUpTo(Routes.STAFF_MANUAL_ORDER) { inclusive = true }
                        launchSingleTop = true
                    }
                    // Buka detail pesanan baru di atas tab Pesanan
                    navController.navigate(Routes.staffOrderDetailRoute(createdOrder.id))
                }
            )
        }
    }
}
