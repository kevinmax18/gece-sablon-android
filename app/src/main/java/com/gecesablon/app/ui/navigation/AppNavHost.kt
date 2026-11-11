package com.gecesablon.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.ui.about.AboutScreen
import com.gecesablon.app.ui.auth.LoginScreen
import com.gecesablon.app.ui.auth.RegisterScreen
import com.gecesablon.app.ui.components.AppBottomBar
import com.gecesablon.app.ui.onboarding.OnboardingScreen
import com.gecesablon.app.ui.splash.SplashScreen
import com.gecesablon.app.ui.welcome.WelcomeScreen

/**
 * Host navigasi utama aplikasi yang mengatur navigasi berbasis autentikasi,
 * layar awal, dan NavGraph tiap peran (Pelanggan, Admin, Owner).
 */
@Composable
fun AppNavHost(
    sessionStore: SessionStore,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isCustomerTab = BottomNavItems.customerItems.any { it.route == currentRoute }
    val isAdminTab = BottomNavItems.adminItems.any { it.route == currentRoute }
    val isOwnerTab = BottomNavItems.ownerItems.any { it.route == currentRoute }

    fun navigateTab(targetRoute: String, startDestination: String) {
        if (currentRoute == targetRoute) return
        navController.navigate(targetRoute) {
            popUpTo(startDestination) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            when {
                isCustomerTab -> {
                    AppBottomBar(
                        items = BottomNavItems.customerItems,
                        currentRoute = currentRoute,
                        onItemSelected = { item ->
                            navigateTab(item.route, Routes.CUSTOMER_HOME)
                        }
                    )
                }
                isAdminTab -> {
                    AppBottomBar(
                        items = BottomNavItems.adminItems,
                        currentRoute = currentRoute,
                        onItemSelected = { item ->
                            navigateTab(item.route, Routes.ADMIN_HOME)
                        }
                    )
                }
                isOwnerTab -> {
                    AppBottomBar(
                        items = BottomNavItems.ownerItems,
                        currentRoute = currentRoute,
                        onItemSelected = { item ->
                            navigateTab(item.route, Routes.OWNER_HOME)
                        }
                    )
                }
                else -> {
                    // AppBottomBar tidak ditampilkan di Splash, Onboarding, Welcome, Login, Register, dan About
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Splash Screen
            composable(Routes.SPLASH) {
                SplashScreen(
                    sessionStore = sessionStore,
                    onNavigateToCustomer = {
                        navController.navigate(Routes.CUSTOMER_ROOT) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToAdmin = {
                        navController.navigate(Routes.ADMIN_ROOT) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToOwner = {
                        navController.navigate(Routes.OWNER_ROOT) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToWelcome = {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Onboarding Screen
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    sessionStore = sessionStore,
                    onFinishOnboarding = {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                        navController.navigate(Routes.LOGIN)
                    }
                )
            }

            // 3. Welcome Screen
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN)
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    },
                    onExploreAsGuest = {
                        navController.navigate(Routes.CUSTOMER_ROOT) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onDebugNavigateToCustomer = {
                        navController.navigate(Routes.CUSTOMER_ROOT) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onDebugNavigateToAdmin = {
                        navController.navigate(Routes.ADMIN_ROOT) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onDebugNavigateToOwner = {
                        navController.navigate(Routes.OWNER_ROOT) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                )
            }

            // 4. Layar Sementara Login & Register
            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 5. Layar Tentang Aplikasi
            composable(Routes.ABOUT) {
                AboutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 6. NavGraph masing-masing peran
            customerNavGraph(navController)
            adminNavGraph(navController)
            ownerNavGraph(navController)
        }
    }
}
