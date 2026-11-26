package com.gecesablon.app.data.remote

import com.gecesablon.app.data.remote.dto.ApiEnvelope
import com.gecesablon.app.data.remote.dto.AuthResponseDataDto
import com.gecesablon.app.data.remote.dto.LoginRequestDto
import com.gecesablon.app.data.remote.dto.ProductDto
import com.gecesablon.app.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface GeceRemoteApi mendefinisikan seluruh endpoint REST API backend.
 * Seluruh path Retrofit tidak diawali "/" dan selalu diawali "api/".
 */
interface GeceRemoteApi {

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
}
