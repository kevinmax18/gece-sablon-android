package com.gecesablon.app

import android.app.Application
import com.gecesablon.app.core.di.AppContainer

/**
 * GeceApp adalah Application class utama untuk inisialisasi dependensi tingkat aplikasi.
 */
class GeceApp : Application() {

    companion object {
        lateinit var instance: GeceApp
            private set
    }

    val container: AppContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
