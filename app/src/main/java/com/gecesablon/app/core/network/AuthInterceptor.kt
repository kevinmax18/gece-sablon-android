package com.gecesablon.app.core.network

import com.gecesablon.app.core.datastore.SessionStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor menyisipkan token autentikasi Bearer ke header HTTP request
 * jika token tersedia di SessionStore.
 */
class AuthInterceptor(
    private val sessionStore: SessionStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking {
            try {
                sessionStore.session.firstOrNull()?.token
            } catch (e: Exception) {
                null
            }
        }

        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}

/**
 * AcceptInterceptor menambahkan header "Accept: application/json" ke semua request
 * agar server Laravel merespons dengan payload JSON (bukan HTML redirect) saat terjadi kegagalan/error.
 */
class AcceptInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
