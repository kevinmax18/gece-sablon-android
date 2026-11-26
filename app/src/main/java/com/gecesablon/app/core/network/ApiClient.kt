package com.gecesablon.app.core.network

import com.gecesablon.app.BuildConfig
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.data.remote.GeceRemoteApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiClient bertugas mengonfigurasi dan menyediakan instance Retrofit & OkHttpClient
 * untuk komunikasi jaringan dengan backend REST API Laravel GeCe Screen Printing.
 */
object ApiClient {

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = false
    }

    private val contentType = "application/json".toMediaType()

    /**
     * Membangun OkHttpClient dengan timeout 30 detik, header Accept: application/json,
     * bearer token auth interceptor, serta logging level BASIC untuk mode debug.
     */
    fun createOkHttpClient(sessionStore: SessionStore): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AcceptInterceptor())
            .addInterceptor(AuthInterceptor(sessionStore))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Membangun Retrofit instance menggunakan BuildConfig.BASE_URL
     * dan kotlinx.serialization converter factory.
     */
    fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = if (BuildConfig.BASE_URL.endsWith("/")) {
            BuildConfig.BASE_URL
        } else {
            "${BuildConfig.BASE_URL}/"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /**
     * Menyediakan instance GeceRemoteApi siap pakai.
     */
    fun createGeceRemoteApi(sessionStore: SessionStore): GeceRemoteApi {
        val client = createOkHttpClient(sessionStore)
        val retrofit = createRetrofit(client)
        return retrofit.create(GeceRemoteApi::class.java)
    }
}
