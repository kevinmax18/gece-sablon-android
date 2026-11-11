package com.gecesablon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.ui.navigation.AppNavHost
import com.gecesablon.app.ui.theme.GeceSablonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeceSablonTheme {
                val geceApp = application as? GeceApp
                val appContainer = remember {
                    geceApp?.container ?: AppContainer(applicationContext)
                }

                AppNavHost(
                    appContainer = appContainer,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
