package com.gecesablon.app.core.di

import android.content.Context
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.network.ApiClient
import com.gecesablon.app.data.local.AccountStore
import com.gecesablon.app.data.remote.GeceRemoteApi
import com.gecesablon.app.data.repository.AccountRepository
import com.gecesablon.app.data.repository.AuthRepository
import com.gecesablon.app.data.repository.CategoryRepository
import com.gecesablon.app.data.repository.FakeAccountRepository
import com.gecesablon.app.data.repository.FakeAuthRepository
import com.gecesablon.app.data.repository.FakeCategoryRepository
import com.gecesablon.app.data.repository.FakeFinanceRepository
import com.gecesablon.app.data.repository.FakeOrderRepository
import com.gecesablon.app.data.repository.FakeProductRepository
import com.gecesablon.app.data.repository.FakeStockRepository
import com.gecesablon.app.data.repository.FinanceRepository
import com.gecesablon.app.data.repository.OrderRepository
import com.gecesablon.app.data.repository.ProductRepository
import com.gecesablon.app.data.repository.RemoteAuthRepository
import com.gecesablon.app.data.repository.StockRepository

/**
 * AppContainer menyediakan dependency injection manual (service locator) untuk
 * repository dan client yang dibutuhkan seluruh aplikasi.
 */
class AppContainer(private val context: Context) {

    /**
     * Saklar pengalih data palsu (in-memory) dan remote API.
     * Bernilai false untuk menghubungkan ke REST API Laravel nyata.
     */
    private val useFakeData = false

    val sessionStore: SessionStore by lazy {
        SessionStore(context.applicationContext)
    }

    /**
     * Instance GeceRemoteApi tunggal yang berbagi sessionStore yang sama untuk autentikasi token.
     */
    val remoteApi: GeceRemoteApi by lazy {
        ApiClient.createGeceRemoteApi(sessionStore)
    }

    /**
     * Penyimpanan akun bersama di memori (single source of truth) untuk
     * autentikasi dan manajemen akun staf admin tiruan.
     * Catatan: Akun yang dibuat di FakeAccountRepository tidak akan bisa login lewat server.
     */
    val accountStore: AccountStore by lazy {
        AccountStore()
    }

    /**
     * Repository autentikasi.
     * Menggunakan RemoteAuthRepository bila useFakeData == false.
     */
    val authRepository: AuthRepository by lazy {
        if (useFakeData) {
            FakeAuthRepository(accountStore = accountStore)
        } else {
            RemoteAuthRepository(api = remoteApi)
        }
    }

    /**
     * Repository akun dan staf admin (khusus Owner).
     * Menggunakan accountStore bersama.
     * TODO: ganti ke Retrofit di Tahap 8.
     */
    val accountRepository: AccountRepository by lazy {
        FakeAccountRepository(accountStore = accountStore)
    }

    /**
     * Repository produk.
     * TODO: ganti ke Retrofit di Tahap 8
     */
    val productRepository: ProductRepository by lazy {
        FakeProductRepository()
    }

    /**
     * Repository kategori produk.
     * TODO: ganti ke Retrofit di Tahap 8
     */
    val categoryRepository: CategoryRepository by lazy {
        FakeCategoryRepository(productRepository = productRepository)
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
