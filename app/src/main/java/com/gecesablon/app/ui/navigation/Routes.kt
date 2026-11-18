package com.gecesablon.app.ui.navigation

/**
 * Mendefinisikan konstanta rute navigasi untuk seluruh layar di aplikasi.
 *
 * Argumen opsional redirect di Login/Register:
 *   "login?redirect={redirect}&productId={productId}" atau "register?redirect={redirect}&productId={productId}"
 * Nilai redirect yang valid saat ini: "order" (tab Pesan pelanggan) dan "product" (kembali ke Detail Produk).
 * Nilai lain diabaikan.
 */
object Routes {
    // Alur Awal & Auth
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val LOGIN_WITH_REDIRECT = "login?redirect={redirect}&productId={productId}"
    const val REGISTER = "register"
    const val REGISTER_WITH_REDIRECT = "register?redirect={redirect}&productId={productId}"
    const val ABOUT = "about"

    /** Daftar nilai redirect yang diizinkan. Nilai lain diabaikan. */
    val VALID_REDIRECTS = setOf("order", "product")

    // Pelanggan (dan Tamu)
    const val CUSTOMER_ROOT = "customer_root"
    const val CUSTOMER_HOME = "customer_home"
    const val CUSTOMER_CATALOG = "customer_catalog"
    const val CUSTOMER_ORDER = "customer_order"
    const val CUSTOMER_CONTACT = "customer_contact"
    const val CUSTOMER_PROFILE = "customer_profile"
    const val CUSTOMER_PRODUCT_DETAIL = "product_detail/{productId}"

    // Alur Pemesanan Pelanggan (Tahap 4B)
    const val ORDER_FLOW_GRAPH = "order_flow/{productId}"
    const val ORDER_FORM = "order_form"
    const val ORDER_CONFIRM = "order_confirm"
    const val ORDER_SUCCESS = "order_success/{orderId}"
    const val ORDER_DETAIL = "order_detail/{orderId}"

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

    // Staf Workshop (Netral Peran - Admin & Owner)
    const val STAFF_ORDER_DETAIL = "staff_order_detail/{orderId}"

    // Helper: bangun rute Detail Produk
    fun productDetailRoute(productId: String): String = "product_detail/$productId"

    // Helper: alur pemesanan
    fun orderFlowRoute(productId: String): String = "order_flow/$productId"
    fun orderSuccessRoute(orderId: String): String = "order_success/$orderId"
    fun orderDetailRoute(orderId: String): String = "order_detail/$orderId"

    // Helper: rute detail pesanan staf
    fun staffOrderDetailRoute(orderId: String): String = "staff_order_detail/$orderId"

    // Helper: bangun rute Login/Register dengan redirect dan productId opsional
    fun loginRoute(redirect: String? = null, productId: String? = null): String =
        if (redirect != null && redirect in VALID_REDIRECTS) {
            if (!productId.isNullOrBlank()) {
                "login?redirect=$redirect&productId=$productId"
            } else {
                "login?redirect=$redirect"
            }
        } else {
            LOGIN
        }

    fun registerRoute(redirect: String? = null, productId: String? = null): String =
        if (redirect != null && redirect in VALID_REDIRECTS) {
            if (!productId.isNullOrBlank()) {
                "register?redirect=$redirect&productId=$productId"
            } else {
                "register?redirect=$redirect"
            }
        } else {
            REGISTER
        }
}
