package com.gecesablon.app.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor menyisipkan token autentikasi Bearer ke header HTTP request.
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Stub interceptor autentikasi token
        return chain.proceed(chain.request())
    }
}
