package com.gecesablon.app.data.model

/**
 * Asal pembuat pesanan: dibuat langsung oleh pelanggan melalui aplikasi
 * atau dibuat oleh staf workshop (pesanan manual).
 */
enum class OrderCreator {
    CUSTOMER,
    STAFF
}
