package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.LanguageMode
import com.gecesablon.app.data.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Unit test murni untuk [AppPreferencesRules].
 */
class AppPreferencesRulesTest {

    @Test
    fun `toLanguageTag memetakan setiap LanguageMode dengan benar`() {
        assertNull(AppPreferencesRules.toLanguageTag(LanguageMode.SISTEM))
        assertEquals("id", AppPreferencesRules.toLanguageTag(LanguageMode.INDONESIA))
        assertEquals("en", AppPreferencesRules.toLanguageTag(LanguageMode.ENGLISH))
    }

    @Test
    fun `fromLanguageTag memetakan tag kosong id in dan en dengan benar`() {
        assertEquals(LanguageMode.SISTEM, AppPreferencesRules.fromLanguageTag(null))
        assertEquals(LanguageMode.SISTEM, AppPreferencesRules.fromLanguageTag(""))
        assertEquals(LanguageMode.SISTEM, AppPreferencesRules.fromLanguageTag("   "))
        assertEquals(LanguageMode.INDONESIA, AppPreferencesRules.fromLanguageTag("id"))
        assertEquals(LanguageMode.INDONESIA, AppPreferencesRules.fromLanguageTag("id-ID"))
        assertEquals(LanguageMode.INDONESIA, AppPreferencesRules.fromLanguageTag("in"))
        assertEquals(LanguageMode.INDONESIA, AppPreferencesRules.fromLanguageTag("in-ID"))
        assertEquals(LanguageMode.ENGLISH, AppPreferencesRules.fromLanguageTag("en"))
        assertEquals(LanguageMode.ENGLISH, AppPreferencesRules.fromLanguageTag("en-US"))
        assertEquals(LanguageMode.SISTEM, AppPreferencesRules.fromLanguageTag("fr"))
    }

    @Test
    fun `toLocale mengembalikan locale sesuai pilihan bahasa`() {
        val systemFr = Locale.FRANCE
        assertEquals(systemFr, AppPreferencesRules.toLocale(LanguageMode.SISTEM, systemFr))
        assertEquals("id", AppPreferencesRules.toLocale(LanguageMode.INDONESIA, systemFr).language.let {
            if (it == "in") "id" else it
        })
        assertEquals("en", AppPreferencesRules.toLocale(LanguageMode.ENGLISH, systemFr).language)
    }

    @Test
    fun `isDarkTheme memetakan ThemeMode dan status sistem dengan benar`() {
        assertTrue(AppPreferencesRules.isDarkTheme(ThemeMode.SISTEM, isSystemInDarkTheme = true))
        assertFalse(AppPreferencesRules.isDarkTheme(ThemeMode.SISTEM, isSystemInDarkTheme = false))
        assertFalse(AppPreferencesRules.isDarkTheme(ThemeMode.TERANG, isSystemInDarkTheme = true))
        assertFalse(AppPreferencesRules.isDarkTheme(ThemeMode.TERANG, isSystemInDarkTheme = false))
        assertTrue(AppPreferencesRules.isDarkTheme(ThemeMode.GELAP, isSystemInDarkTheme = true))
        assertTrue(AppPreferencesRules.isDarkTheme(ThemeMode.GELAP, isSystemInDarkTheme = false))
    }

    @Test
    fun `parseLanguageMode dan parseThemeMode aman terhadap nilai null atau tidak valid`() {
        assertEquals(LanguageMode.SISTEM, AppPreferencesRules.parseLanguageMode(null))
        assertEquals(LanguageMode.SISTEM, AppPreferencesRules.parseLanguageMode("UNKNOWN"))
        assertEquals(LanguageMode.ENGLISH, AppPreferencesRules.parseLanguageMode("ENGLISH"))

        assertEquals(ThemeMode.SISTEM, AppPreferencesRules.parseThemeMode(null))
        assertEquals(ThemeMode.SISTEM, AppPreferencesRules.parseThemeMode("INVALID"))
        assertEquals(ThemeMode.GELAP, AppPreferencesRules.parseThemeMode("GELAP"))
        assertEquals(ThemeMode.TERANG, AppPreferencesRules.parseThemeMode("TERANG"))
    }
}
