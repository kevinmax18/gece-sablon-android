package com.gecesablon.app.ui.navigation

/**
 * Mendefinisikan konstanta rute navigasi untuk seluruh layar di aplikasi.
 *
 * Argumen opsional redirect di Login/Register:
 *   "login?redirect={redirect}" atau "register?redirect={redirect}"
 * Nilai redirect yang valid saat ini hanya "order" (tab Pesan pelanggan).
 * Nilai lain diabaikan.
 */
object Routes {
    // Alur Awal & Auth
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val LOGIN_WITH_REDIRECT = "login?redirect={redirect}"
    const val REGISTER = "register"
    const val REGISTER_WITH_REDIRECT = "register?redirect={redirect}"
    const val ABOUT = "about"

    /** Daftar nilai redirect yang diizinkan. Nilai lain diabaikan. */
    val VALID_REDIRECTS = setOf("order")

    // Pelanggan (dan Tamu)
    const val CUSTOMER_ROOT = "customer_root"
    const val CUSTOMER_HOME = "customer_home"
    const val CUSTOMER_CATALOG = "customer_catalog"
    const val CUSTOMER_ORDER = "customer_order"
    const val CUSTOMER_CONTACT = "customer_contact"
    const val CUSTOMER_PROFILE = "customer_profile"

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

    // Helper: bangun rute Login/Register dengan redirect opsional
    fun loginRoute(redirect: String? = null): String =
        if (redirect != null && redirect in VALID_REDIRECTS) "login?redirect=$redirect" else LOGIN

    fun registerRoute(redirect: String? = null): String =
        if (redirect != null && redirect in VALID_REDIRECTS) "register?redirect=$redirect" else REGISTER
}
