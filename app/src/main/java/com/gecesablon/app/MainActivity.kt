package com.gecesablon.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.core.util.AppPreferencesRules
import com.gecesablon.app.data.model.LanguageMode
import com.gecesablon.app.data.model.ThemeMode
import com.gecesablon.app.ui.navigation.AppNavHost
import com.gecesablon.app.ui.theme.GeceSablonTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val geceApp = application as? GeceApp
            val appContainer = remember {
                geceApp?.container ?: AppContainer(applicationContext)
            }
            val themeMode by appContainer.sessionStore.themeMode.collectAsState(initial = ThemeMode.SISTEM)
            val languageMode by appContainer.sessionStore.languageMode.collectAsState(initial = LanguageMode.SISTEM)

            LaunchedEffect(languageMode) {
                val targetTag = AppPreferencesRules.toLanguageTag(languageMode)
                val targetLocales = if (targetTag == null) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(targetTag)
                }
                if (AppCompatDelegate.getApplicationLocales() != targetLocales) {
                    AppCompatDelegate.setApplicationLocales(targetLocales)
                }
            }

            GeceSablonTheme(themeMode = themeMode) {
                AppNavHost(
                    appContainer = appContainer,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
