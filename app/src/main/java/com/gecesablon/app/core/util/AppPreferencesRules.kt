package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.LanguageMode
import com.gecesablon.app.data.model.ThemeMode
import java.util.Locale

/**
 * Fungsi murni untuk pemetaan preferensi bahasa dan tema (Tahap 7B).
 */
object AppPreferencesRules {

    /**
     * Memetakan [LanguageMode] ke tag bahasa BCP-47 untuk per-app locales.
     * Mengembalikan null untuk [LanguageMode.SISTEM] (mengikuti pengaturan sistem).
     */
    fun toLanguageTag(mode: LanguageMode): String? = when (mode) {
        LanguageMode.SISTEM -> null
        LanguageMode.INDONESIA -> "id"
        LanguageMode.ENGLISH -> "en"
    }

    /**
     * Memetakan tag bahasa (mis. dari AppCompatDelegate) ke [LanguageMode].
     * Menangani kode lama "in" yang digunakan Java/Android untuk bahasa Indonesia.
     */
    fun fromLanguageTag(tag: String?): LanguageMode {
        val normalized = tag?.trim()?.lowercase(Locale.ROOT).orEmpty()
        return when {
            normalized.isEmpty() -> LanguageMode.SISTEM
            normalized == "id" || normalized.startsWith("id-") ||
                normalized == "in" || normalized.startsWith("in-") -> LanguageMode.INDONESIA
            normalized == "en" || normalized.startsWith("en-") -> LanguageMode.ENGLISH
            else -> LanguageMode.SISTEM
        }
    }

    /**
     * Memetakan [LanguageMode] ke [Locale] untuk pemformatan tanggal dan angka.
     */
    fun toLocale(mode: LanguageMode, systemLocale: Locale = Locale.getDefault()): Locale = when (mode) {
        LanguageMode.SISTEM -> systemLocale
        LanguageMode.INDONESIA -> Locale("id", "ID")
        LanguageMode.ENGLISH -> Locale("en", "US")
    }

    /**
     * Menentukan apakah tema gelap aktif berdasarkan [ThemeMode] dan status tema gelap sistem.
     */
    fun isDarkTheme(mode: ThemeMode, isSystemInDarkTheme: Boolean): Boolean = when (mode) {
        ThemeMode.SISTEM -> isSystemInDarkTheme
        ThemeMode.TERANG -> false
        ThemeMode.GELAP -> true
    }

    /**
     * Mengurai nilai string tersimpan ke [LanguageMode] dengan fallback aman ke [LanguageMode.SISTEM].
     */
    fun parseLanguageMode(raw: String?): LanguageMode {
        if (raw.isNullOrBlank()) return LanguageMode.SISTEM
        return try {
            LanguageMode.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            LanguageMode.SISTEM
        }
    }

    /**
     * Mengurai nilai string tersimpan ke [ThemeMode] dengan fallback aman ke [ThemeMode.SISTEM].
     */
    fun parseThemeMode(raw: String?): ThemeMode {
        if (raw.isNullOrBlank()) return ThemeMode.SISTEM
        return try {
            ThemeMode.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SISTEM
        }
    }
}
