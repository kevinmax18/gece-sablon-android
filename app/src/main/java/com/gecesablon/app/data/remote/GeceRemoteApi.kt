package com.gecesablon.app.data.remote

import com.gecesablon.app.data.remote.dto.AdminAccountDto
import com.gecesablon.app.data.remote.dto.ApiEnvelope
import com.gecesablon.app.data.remote.dto.AuthResponseDataDto
import com.gecesablon.app.data.remote.dto.CreateAdminAccountRequestDto
import com.gecesablon.app.data.remote.dto.FinanceSummaryDto
import com.gecesablon.app.data.remote.dto.LoginRequestDto
import com.gecesablon.app.data.remote.dto.OrderDto
import com.gecesablon.app.data.remote.dto.PaginatedResponse
import com.gecesablon.app.data.remote.dto.ProductDto
import com.gecesablon.app.data.remote.dto.ProductRequestDto
import com.gecesablon.app.data.remote.dto.RegisterRequestDto
import com.gecesablon.app.data.remote.dto.StockDto
import com.gecesablon.app.data.remote.dto.StockRequestDto
import com.gecesablon.app.data.remote.dto.TransactionDto
import com.gecesablon.app.data.remote.dto.TransactionRequestDto
import com.gecesablon.app.data.remote.dto.UpdateAdminAccountRequestDto
import com.gecesablon.app.data.remote.dto.UpdateOrderStatusRequestDto
import com.gecesablon.app.data.remote.dto.UpdateProductionRequestDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface GeceRemoteApi mendefinisikan seluruh endpoint REST API backend.
 * Seluruh path Retrofit tidak diawali "/" dan selalu diawali "api/".
 */
interface GeceRemoteApi {

    // =========================================================================
    // 1. Autentikasi
    // =========================================================================

    /**
     * Login pengguna dengan kredensial email/no HP (key: "login") dan password.
     */
    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): ApiEnvelope<AuthResponseDataDto>

    /**
     * Registrasi akun pelanggan baru.
     */
    @POST("api/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): ApiEnvelope<AuthResponseDataDto>

    /**
     * Logout untuk membatalkan token sesi saat ini di server.
     */
    @POST("api/logout")
    suspend fun logout(): ApiEnvelope<Unit>

    // =========================================================================
    // 2. Produk & Katalog
    // =========================================================================

    /**
     * Mengambil daftar produk aktif untuk katalog publik dengan filter opsional.
     */
    @GET("api/products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null
    ): ApiEnvelope<List<ProductDto>>

    /**
     * Mengambil detail produk berdasarkan ID produk.
     */
    @GET("api/products/{id}")
    suspend fun getProductById(
        @Path("id") id: String
    ): ApiEnvelope<ProductDto>

    /**
     * Mengambil seluruh produk (termasuk nonaktif) khusus Owner.
     */
    @GET("api/owner/products")
    suspend fun getOwnerProducts(): ApiEnvelope<List<ProductDto>>

    /**
     * Menambahkan produk baru oleh Owner.
     */
    @POST("api/products")
    suspend fun createProduct(
        @Body request: ProductRequestDto
    ): ApiEnvelope<ProductDto>

    /**
     * Memperbarui informasi produk oleh Owner.
     */
    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body request: ProductRequestDto
    ): ApiEnvelope<ProductDto>

    /**
     * Menghapus produk oleh Owner.
     */
    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: String
    ): ApiEnvelope<Unit>

    // =========================================================================
    // 3. Pesanan (Order)
    // =========================================================================

    /**
     * Mengambil daftar pesanan berpaginasi (pelanggan: miliknya, admin/owner: semua).
     */
    @GET("api/orders")
    suspend fun getOrders(
        @Query("page") page: Int = 1,
        @Query("status") status: String? = null
    ): ApiEnvelope<PaginatedResponse<OrderDto>>

    /**
     * Mengambil detail pesanan berdasarkan ID.
     */
    @GET("api/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String
    ): ApiEnvelope<OrderDto>

    /**
     * Membuat pesanan (customer / manual walk-in) via Multipart/form-data.
     */
    @Multipart
    @POST("api/orders")
    suspend fun createOrder(
        @Part("product_id") productId: RequestBody,
        @Part("color_id") colorId: RequestBody,
        @Part("size_id") sizeId: RequestBody,
        @Part("material_id") materialId: RequestBody? = null,
        @Part("quantity") quantity: RequestBody,
        @Part("notes") notes: RequestBody? = null,
        @Part design: MultipartBody.Part? = null,
        @Part("customer_name") customerName: RequestBody? = null,
        @Part("customer_phone") customerPhone: RequestBody? = null
    ): ApiEnvelope<OrderDto>

    /**
     * Membatalkan pesanan (hanya bisa jika status DITERIMA).
     */
    @POST("api/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") id: String
    ): ApiEnvelope<OrderDto>

    /**
     * Memperbarui status pesanan oleh staf workshop (Admin / Owner).
     */
    @PUT("api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateOrderStatusRequestDto
    ): ApiEnvelope<OrderDto>

    /**
     * Memperbarui tahapan produksi dan flag kesiapan oleh staf workshop.
     */
    @PUT("api/orders/{id}/production")
    suspend fun updateOrderProduction(
        @Path("id") id: String,
        @Body request: UpdateProductionRequestDto
    ): ApiEnvelope<OrderDto>

    // =========================================================================
    // 4. Inventaris Stok
    // =========================================================================

    /**
     * Mengambil seluruh daftar inventaris stok.
     */
    @GET("api/stock")
    suspend fun getStock(): ApiEnvelope<List<StockDto>>

    /**
     * Menambahkan item stok baru.
     */
    @POST("api/stock")
    suspend fun createStock(
        @Body request: StockRequestDto
    ): ApiEnvelope<StockDto>

    /**
     * Memperbarui data item stok.
     */
    @PUT("api/stock/{id}")
    suspend fun updateStock(
        @Path("id") id: Int,
        @Body request: StockRequestDto
    ): ApiEnvelope<StockDto>

    /**
     * Menghapus item stok dari sistem persediaan.
     */
    @DELETE("api/stock/{id}")
    suspend fun deleteStock(
        @Path("id") id: Int
    ): ApiEnvelope<Unit>

    // =========================================================================
    // 5. Keuangan (Owner)
    // =========================================================================

    /**
     * Mengambil daftar transaksi keuangan berpaginasi dengan filter opsional.
     */
    @GET("api/finance/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int = 1,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("type") type: String? = null
    ): ApiEnvelope<PaginatedResponse<TransactionDto>>

    /**
     * Mengambil ringkasan keuangan laba/rugi per periode dari server.
     */
    @GET("api/finance/summary")
    suspend fun getFinanceSummary(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): ApiEnvelope<FinanceSummaryDto>

    /**
     * Mencatat transaksi keuangan manual baru.
     */
    @POST("api/finance/transactions")
    suspend fun createTransaction(
        @Body request: TransactionRequestDto
    ): ApiEnvelope<TransactionDto>

    /**
     * Memperbarui transaksi keuangan manual yang sudah ada.
     */
    @PUT("api/finance/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: Int,
        @Body request: TransactionRequestDto
    ): ApiEnvelope<TransactionDto>

    /**
     * Menghapus transaksi keuangan manual.
     */
    @DELETE("api/finance/transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: Int
    ): ApiEnvelope<Unit>

    // =========================================================================
    // 6. Kelola Akun Admin (Owner)
    // =========================================================================

    /**
     * Mengambil daftar seluruh staf admin.
     */
    @GET("api/owner/admins")
    suspend fun getAdminAccounts(): ApiEnvelope<List<AdminAccountDto>>

    /**
     * Menambahkan akun staf admin baru.
     */
    @POST("api/owner/admins")
    suspend fun createAdminAccount(
        @Body request: CreateAdminAccountRequestDto
    ): ApiEnvelope<AdminAccountDto>

    /**
     * Memperbarui informasi akun admin (nama, no HP, password, status aktif).
     */
    @PUT("api/owner/admins/{id}")
    suspend fun updateAdminAccount(
        @Path("id") id: Int,
        @Body request: UpdateAdminAccountRequestDto
    ): ApiEnvelope<AdminAccountDto>

    /**
     * Menghapus akun admin.
     */
    @DELETE("api/owner/admins/{id}")
    suspend fun deleteAdminAccount(
        @Path("id") id: Int
    ): ApiEnvelope<Unit>
}
