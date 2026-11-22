package com.gecesablon.app.core.di

import android.content.Context
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.data.repository.AuthRepository
import com.gecesablon.app.data.repository.FakeAuthRepository
import com.gecesablon.app.data.repository.FakeFinanceRepository
import com.gecesablon.app.data.repository.FakeOrderRepository
import com.gecesablon.app.data.repository.FakeProductRepository
import com.gecesablon.app.data.repository.FakeStockRepository
import com.gecesablon.app.data.repository.FinanceRepository
import com.gecesablon.app.data.repository.OrderRepository
import com.gecesablon.app.data.repository.ProductRepository
import com.gecesablon.app.data.repository.StockRepository

/**
 * AppContainer menyediakan dependency injection manual (service locator) untuk
 * repository dan client yang dibutuhkan seluruh aplikasi.
 */
class AppContainer(private val context: Context) {

    val sessionStore: SessionStore by lazy {
        SessionStore(context.applicationContext)
    }

    /**
     * Repository autentikasi.
     * TODO: Hapus FakeAuthRepository di Tahap 8 — ganti dengan RemoteAuthRepository.
     */
    val authRepository: AuthRepository by lazy {
        FakeAuthRepository()
    }

    /**
     * Repository produk.
     * TODO: ganti ke Retrofit di Tahap 8
     */
    val productRepository: ProductRepository by lazy {
        FakeProductRepository()
    }

    /**
     * Repository pesanan.
     * TODO: ganti ke Retrofit di Tahap 8
     */
    val orderRepository: OrderRepository by lazy {
        FakeOrderRepository(productRepository = productRepository)
    }

    /**
     * Repository stok persediaan dan bahan baku sablon.
     * TODO: ganti ke Retrofit di Tahap 8
     */
    val stockRepository: StockRepository by lazy {
        FakeStockRepository()
    }

    /**
     * Repository keuangan (hanya dapat diakses oleh Owner).
     * TODO: ganti ke Retrofit di Tahap 8
     */
    val financeRepository: FinanceRepository by lazy {
        FakeFinanceRepository(orderRepository = orderRepository)
    }
}
