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
    const val CHANGE_PASSWORD = "change_password"

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

    // Owner – Sub-rute Keuangan (Tahap 6A)
    const val OWNER_TRANSACTION_FORM = "owner_transaction_form?transactionId={transactionId}&type={type}"
    const val OWNER_TRANSACTION_DETAIL = "owner_transaction_detail/{transactionId}"
    const val OWNER_REPORT = "owner_report"

    // Owner – Sub-rute Kelola Produk (Tahap 6B)
    const val OWNER_PRODUCTS = "owner_products"
    const val OWNER_PRODUCT_FORM = "owner_product_form?productId={productId}"

    // Owner – Sub-rute Kelola Akun Admin (Tahap 6B-2)
    const val OWNER_ADMIN_ACCOUNTS = "owner_admin_accounts"
    const val OWNER_ADMIN_FORM = "owner_admin_form"
    const val OWNER_ADMIN_DETAIL = "owner_admin_detail/{id}"

    // Owner – Sub-rute Kelola Kategori Produk (Tahap 6D-2)
    const val OWNER_CATEGORIES = "owner_categories"

    // Staf Workshop (Netral Peran - Admin & Owner)
    const val STAFF_ORDER_DETAIL = "staff_order_detail/{orderId}"

    // Tahap 5B: Admin Stock (tetap) – Pesanan Manual dipindah ke rute Staff Bersama (Tahap 6A)
    const val ADMIN_STOCK = "admin_stock"
    const val ADMIN_STOCK_FORM = "admin_stock_form?stockId={stockId}"

    // Rute Staf Bersama (Admin & Owner) – Tahap 6A
    // Menggantikan ADMIN_MANUAL_ORDER; didaftarkan di level AppNavHost melalui staffOrderRoutes
    const val STAFF_MANUAL_ORDER = "staff_manual_order"

    // Helper: bangun rute Detail Produk
    fun productDetailRoute(productId: String): String = "product_detail/$productId"

    // Helper: alur pemesanan
    fun orderFlowRoute(productId: String): String = "order_flow/$productId"
    fun orderSuccessRoute(orderId: String): String = "order_success/$orderId"
    fun orderDetailRoute(orderId: String): String = "order_detail/$orderId"

    // Helper: rute detail pesanan staf
    fun staffOrderDetailRoute(orderId: String): String = "staff_order_detail/$orderId"

    // Helper: rute form stok
    fun adminStockFormRoute(stockId: String? = null): String =
        if (!stockId.isNullOrBlank()) "admin_stock_form?stockId=$stockId" else "admin_stock_form"

    // Helper: rute form transaksi owner
    fun ownerTransactionFormRoute(transactionId: String? = null, type: String? = null): String {
        val params = buildString {
            if (!transactionId.isNullOrBlank()) append("transactionId=$transactionId&")
            if (!type.isNullOrBlank()) append("type=$type")
        }.trimEnd('&')
        return if (params.isNotEmpty()) "owner_transaction_form?$params" else "owner_transaction_form"
    }

    // Helper: rute detail transaksi owner
    fun ownerTransactionDetailRoute(transactionId: String): String =
        "owner_transaction_detail/$transactionId"

    // Helper: rute form produk owner (Tahap 6B)
    fun ownerProductFormRoute(productId: String? = null): String =
        if (!productId.isNullOrBlank()) "owner_product_form?productId=$productId" else "owner_product_form"

    // Helper: rute kelola akun admin owner (Tahap 6B-2)
    fun ownerAdminDetailRoute(id: String): String = "owner_admin_detail/$id"

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
