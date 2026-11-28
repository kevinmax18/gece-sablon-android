package com.gecesablon.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.data.model.LanguageMode
import com.gecesablon.app.data.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel bersama untuk layar Pengaturan (bahasa & tema).
 */
class SettingsViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        sessionStore.languageMode,
        sessionStore.themeMode
    ) { languageMode, themeMode ->
        SettingsUiState(
            languageMode = languageMode,
            themeMode = themeMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setLanguageMode(mode: LanguageMode) {
        viewModelScope.launch {
            sessionStore.setLanguageMode(mode)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            sessionStore.setThemeMode(mode)
        }
    }

    companion object {
        fun factory(sessionStore: SessionStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(sessionStore = sessionStore) as T
                }
            }
    }
}
