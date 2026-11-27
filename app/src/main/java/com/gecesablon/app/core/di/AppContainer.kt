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
import com.gecesablon.app.data.repository.RemoteAccountRepository
import com.gecesablon.app.data.repository.RemoteAuthRepository
import com.gecesablon.app.data.repository.RemoteCategoryRepository
import com.gecesablon.app.data.repository.RemoteFinanceRepository
import com.gecesablon.app.data.repository.RemoteOrderRepository
import com.gecesablon.app.data.repository.RemoteProductRepository
import com.gecesablon.app.data.repository.RemoteStockRepository
import com.gecesablon.app.data.repository.StockRepository

/**
 * AppContainer menyediakan dependency injection manual (service locator) untuk
 * repository dan client yang dibutuhkan seluruh aplikasi.
 */
class AppContainer(private val context: Context) {

    /**
     * Saklar global pengalih data palsu (in-memory) dan remote API.
     * Bernilai false untuk menghubungkan ke REST API Laravel nyata.
     */
    private val useFakeData = false

    /**
     * Saklar modular per fitur (aktif saat useFakeData == false).
     */
    private val remoteProducts = true
    private val remoteOrders = true
    private val remoteStock = true
    private val remoteFinance = true
    private val remoteAccounts = true

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
     * Repository produk.
     */
    val productRepository: ProductRepository by lazy {
        if (useFakeData || !remoteProducts) {
            FakeProductRepository()
        } else {
            RemoteProductRepository(api = remoteApi)
        }
    }

    /**
     * Repository kategori produk.
     */
    val categoryRepository: CategoryRepository by lazy {
        if (useFakeData || !remoteProducts) {
            FakeCategoryRepository(productRepository = productRepository)
        } else {
            RemoteCategoryRepository(productRepository = productRepository)
        }
    }

    /**
     * Repository pesanan.
     */
    val orderRepository: OrderRepository by lazy {
        if (useFakeData || !remoteOrders) {
            FakeOrderRepository(productRepository = productRepository)
        } else {
            RemoteOrderRepository(
                api = remoteApi,
                context = context.applicationContext,
                productRepository = productRepository
            )
        }
    }

    /**
     * Repository stok persediaan dan bahan baku sablon.
     */
    val stockRepository: StockRepository by lazy {
        if (useFakeData || !remoteStock) {
            FakeStockRepository()
        } else {
            RemoteStockRepository(api = remoteApi)
        }
    }

    /**
     * Repository keuangan (hanya dapat diakses oleh Owner).
     */
    val financeRepository: FinanceRepository by lazy {
        if (useFakeData || !remoteFinance) {
            FakeFinanceRepository(orderRepository = orderRepository)
        } else {
            RemoteFinanceRepository(
                api = remoteApi,
                orderRepository = orderRepository
            )
        }
    }

    /**
     * Repository akun dan staf admin (khusus Owner).
     */
    val accountRepository: AccountRepository by lazy {
        if (useFakeData || !remoteAccounts) {
            FakeAccountRepository(accountStore = accountStore)
        } else {
            RemoteAccountRepository(api = remoteApi)
        }
    }
}
