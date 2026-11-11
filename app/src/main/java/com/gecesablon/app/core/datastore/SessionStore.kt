package com.gecesablon.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gecesablon.app.data.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "gece_session_prefs")

/**
 * SessionStore mengelola persistensi data sesi lokal seperti token otentikasi,
 * role pengguna, dan penanda onboarding via DataStore Preferences tanpa logika bisnis.
 */
class SessionStore(private val context: Context) {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_TOKEN = stringPreferencesKey("user_token")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.sessionDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.sessionDataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    val userRole: Flow<Role?> = context.sessionDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ROLE]?.let { roleName ->
                try {
                    Role.valueOf(roleName)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }

    val authToken: Flow<String?> = context.sessionDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_TOKEN]
        }

    suspend fun saveSession(token: String, role: Role) {
        context.sessionDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_TOKEN] = token
            preferences[PreferencesKeys.USER_ROLE] = role.name
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_TOKEN)
            preferences.remove(PreferencesKeys.USER_ROLE)
        }
    }
}
