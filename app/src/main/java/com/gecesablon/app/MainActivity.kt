package com.gecesablon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.ui.navigation.AppNavHost
import com.gecesablon.app.ui.theme.GeceSablonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeceSablonTheme {
                val geceApp = application as? GeceApp
                val sessionStore = remember {
                    geceApp?.container?.sessionStore ?: SessionStore(applicationContext)
                }

                AppNavHost(
                    sessionStore = sessionStore,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
