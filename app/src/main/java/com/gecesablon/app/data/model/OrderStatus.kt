package com.gecesablon.app.data.model

/**
 * Model enum yang merepresentasikan status transaksi pesanan (OrderStatus).
 */
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    IN_PRODUCTION,
    COMPLETED,
    CANCELLED
}
