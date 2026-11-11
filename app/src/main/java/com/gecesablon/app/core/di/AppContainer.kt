package com.gecesablon.app.core.di

import android.content.Context
import com.gecesablon.app.core.datastore.SessionStore

/**
 * AppContainer menyediakan dependency injection manual (service locator) untuk repository dan client.
 */
class AppContainer(private val context: Context) {
    val sessionStore: SessionStore by lazy {
        SessionStore(context.applicationContext)
    }
}
