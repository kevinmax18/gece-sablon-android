package com.gecesablon.app.ui.navigation

/**
 * Mendefinisikan konstanta rute navigasi untuk seluruh layar di aplikasi.
 */
object Routes {
    // Alur Awal & Auth
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ABOUT = "about"

    // Pelanggan (dan Tamu)
    const val CUSTOMER_ROOT = "customer_root"
    const val CUSTOMER_HOME = "customer_home"
    const val CUSTOMER_CATALOG = "customer_catalog"
    const val CUSTOMER_ORDER = "customer_order"
    const val CUSTOMER_CONTACT = "customer_contact"

    // Admin Workshop
    const val ADMIN_ROOT = "admin_root"
    const val ADMIN_HOME = "admin_home"
    const val ADMIN_ORDERS = "admin_orders"
    const val ADMIN_PROCESS = "admin_process"
    const val ADMIN_ACCOUNT = "admin_account"

    // Owner
    const val OWNER_ROOT = "owner_root"
    const val OWNER_HOME = "owner_home"
    const val OWNER_ORDERS = "owner_orders"
    const val OWNER_PROCESS = "owner_process"
    const val OWNER_FINANCE = "owner_finance"
    const val OWNER_ACCOUNT = "owner_account"
}
