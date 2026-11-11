package com.gecesablon.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.ui.about.AboutScreen
import com.gecesablon.app.ui.account.AccountViewModel
import com.gecesablon.app.ui.auth.LoginScreen
import com.gecesablon.app.ui.auth.LoginViewModel
import com.gecesablon.app.ui.auth.RegisterScreen
import com.gecesablon.app.ui.auth.RegisterViewModel
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
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Pantau sesi saat ini untuk menentukan apakah pengguna dalam mode tamu
    val currentSession by appContainer.sessionStore.session.collectAsState(initial = null)
    val isGuest = currentSession == null

    // ViewModel bersama untuk pengelolaan profil/akun dan logout
    val accountViewModel: AccountViewModel = viewModel(
        factory = AccountViewModel.factory(
            authRepository = appContainer.authRepository,
            sessionStore = appContainer.sessionStore
        )
    )

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

    /**
     * Menangani navigasi setelah login atau register berhasil:
     * - Role CUSTOMER: jika ada redirect "order", kembali ke tab Pesan di CustomerNavGraph.
     *   Jika tidak, masuk CustomerNavGraph dari awal (bersihkan Welcome).
     * - Role ADMIN / OWNER: abaikan redirect dan bersihkan seluruh back stack.
     */
    fun handleAuthSuccess(session: Session, redirect: String?) {
        val safeRedirect = if (redirect != null && redirect in Routes.VALID_REDIRECTS) redirect else null
        when (session.role) {
            Role.CUSTOMER -> {
                if (safeRedirect == "order") {
                    navController.navigate(Routes.CUSTOMER_ORDER) {
                        popUpTo(Routes.CUSTOMER_HOME) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate(Routes.CUSTOMER_ROOT) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            }
            Role.ADMIN -> {
                navController.navigate(Routes.ADMIN_ROOT) {
                    popUpTo(0) { inclusive = true }
                }
            }
            Role.OWNER -> {
                navController.navigate(Routes.OWNER_ROOT) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val handleLogout: () -> Unit = {
        navController.navigate(Routes.WELCOME) {
            popUpTo(0) { inclusive = true }
        }
    }

    val handleNavigateToAbout: () -> Unit = {
        navController.navigate(Routes.ABOUT)
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
                            if (item.route == Routes.CUSTOMER_ORDER && isGuest) {
                                // Tamu klik tab Pesan -> Buka Login, tab tidak berpindah
                                navController.navigate(Routes.loginRoute("order"))
                            } else {
                                navigateTab(item.route, Routes.CUSTOMER_HOME)
                            }
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
                    // AppBottomBar tidak ditampilkan di Splash, Onboarding, Welcome, Login, Register, Profile, dan About
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
                    sessionStore = appContainer.sessionStore,
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
                    sessionStore = appContainer.sessionStore,
                    onFinishOnboarding = {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                        navController.navigate(Routes.loginRoute())
                    }
                )
            }

            // 3. Welcome Screen
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onNavigateToLogin = {
                        navController.navigate(Routes.loginRoute())
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.registerRoute())
                    },
                    onExploreAsGuest = {
                        navController.navigate(Routes.CUSTOMER_ROOT) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onNavigateToAbout = handleNavigateToAbout
                )
            }

            // 4. Layar Login & Register
            composable(
                route = Routes.LOGIN_WITH_REDIRECT,
                arguments = listOf(
                    navArgument("redirect") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModel.factory(
                        authRepository = appContainer.authRepository,
                        sessionStore = appContainer.sessionStore
                    )
                )
                LoginScreen(
                    viewModel = loginViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToRegister = { redirect ->
                        navController.navigate(Routes.registerRoute(redirect)) {
                            popUpTo(Routes.LOGIN_WITH_REDIRECT) { inclusive = true }
                        }
                    },
                    onExploreAsGuest = {
                        navController.navigate(Routes.CUSTOMER_ROOT) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onLoginSuccess = { session, redirect ->
                        handleAuthSuccess(session, redirect)
                    }
                )
            }

            composable(
                route = Routes.REGISTER_WITH_REDIRECT,
                arguments = listOf(
                    navArgument("redirect") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = RegisterViewModel.factory(
                        authRepository = appContainer.authRepository,
                        sessionStore = appContainer.sessionStore
                    )
                )
                RegisterScreen(
                    viewModel = registerViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToLogin = { redirect ->
                        navController.navigate(Routes.loginRoute(redirect)) {
                            popUpTo(Routes.REGISTER_WITH_REDIRECT) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = { session, redirect ->
                        handleAuthSuccess(session, redirect)
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
            customerNavGraph(
                navController = navController,
                isGuest = isGuest,
                accountViewModel = accountViewModel,
                onNavigateToLogin = {
                    navController.navigate(Routes.loginRoute())
                },
                onNavigateToAbout = handleNavigateToAbout,
                onLogout = handleLogout
            )

            adminNavGraph(
                navController = navController,
                accountViewModel = accountViewModel,
                onNavigateToAbout = handleNavigateToAbout,
                onLogout = handleLogout
            )

            ownerNavGraph(
                navController = navController,
                accountViewModel = accountViewModel,
                onNavigateToAbout = handleNavigateToAbout,
                onLogout = handleLogout
            )
        }
    }
}
