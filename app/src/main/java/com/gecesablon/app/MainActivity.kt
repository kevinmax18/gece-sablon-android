package com.gecesablon.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.gecesablon.app.core.di.AppContainer
import com.gecesablon.app.core.util.AppPreferencesRules
import com.gecesablon.app.ui.navigation.AppNavHost
import com.gecesablon.app.ui.theme.GeceSablonTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val geceApp = application as? GeceApp
        val appContainer = geceApp?.container ?: AppContainer(applicationContext)

        lifecycleScope.launch {
            appContainer.sessionStore.languageMode
                .distinctUntilChanged()
                .collect { languageMode ->
                    val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                    if (AppPreferencesRules.shouldApplyLanguage(languageMode, currentTags)) {
                        val targetTag = AppPreferencesRules.toLanguageTag(languageMode)
                        val targetLocales = if (targetTag == null) {
                            LocaleListCompat.getEmptyLocaleList()
                        } else {
                            LocaleListCompat.forLanguageTags(targetTag)
                        }
                        AppCompatDelegate.setApplicationLocales(targetLocales)
                    }
                }
        }

        setContent {
            val container = remember { appContainer }
            val themeMode by container.sessionStore.themeMode
                .distinctUntilChanged()
                .collectAsState(initial = null)

            val resolvedThemeMode = themeMode ?: return@setContent

            GeceSablonTheme(themeMode = resolvedThemeMode) {
                AppNavHost(
                    appContainer = container,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
