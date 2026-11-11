package com.gecesablon.app.core.di

import android.content.Context
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.data.repository.AuthRepository
import com.gecesablon.app.data.repository.FakeAuthRepository

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
}
