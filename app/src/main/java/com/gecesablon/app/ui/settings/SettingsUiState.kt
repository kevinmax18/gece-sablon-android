package com.gecesablon.app.ui.settings

import com.gecesablon.app.data.model.LanguageMode
import com.gecesablon.app.data.model.ThemeMode

/**
 * State UI untuk layar Pengaturan (bahasa dan tema).
 */
data class SettingsUiState(
    val languageMode: LanguageMode = LanguageMode.SISTEM,
    val themeMode: ThemeMode = ThemeMode.SISTEM
)
