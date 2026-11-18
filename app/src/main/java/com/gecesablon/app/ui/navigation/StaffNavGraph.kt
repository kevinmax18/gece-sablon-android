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
import com.gecesablon.app.ui.staff.StaffOrderDetailScreen
import com.gecesablon.app.ui.staff.StaffOrderDetailViewModel

/**
 * Mendaftarkan rute detail pesanan staf di level AppNavHost yang netral peran (dipakai Admin dan Owner).
 * Memeriksa izin peran: hanya akun dengan peran ADMIN atau OWNER yang dapat mengakses layar ini.
 */
fun NavGraphBuilder.staffOrderRoutes(
    appContainer: AppContainer,
    navController: NavHostController,
    currentSession: Session?
) {
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
            // Bukan staf workshop: batalkan dan kembali ke layar asal
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
}
