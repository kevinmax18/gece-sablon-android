package com.gecesablon.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "gece_session_prefs")

/**
 * SessionStore mengelola persistensi data sesi lokal (token, role, nama) dan penanda onboarding
 * via DataStore Preferences. Tidak menyimpan password, email, atau nomor HP.
 * Sesi dianggap valid hanya jika token DAN role keduanya tersedia.
 */
class SessionStore(private val context: Context) {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_TOKEN = stringPreferencesKey("user_token")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_NAME = stringPreferencesKey("user_name")
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

    /**
     * Flow sesi aktif. Emit Session jika token DAN role ada; emit null selain itu.
     * Digunakan oleh SplashScreen dan AccountViewModel untuk membaca status login.
     */
    val session: Flow<Session?> = context.sessionDataStore.data
        .map { preferences ->
            val token = preferences[PreferencesKeys.USER_TOKEN]
            val roleStr = preferences[PreferencesKeys.USER_ROLE]
            val name = preferences[PreferencesKeys.USER_NAME] ?: ""
            if (token.isNullOrBlank() || roleStr.isNullOrBlank()) return@map null
            val role = try {
                Role.valueOf(roleStr)
            } catch (e: IllegalArgumentException) {
                return@map null
            }
            Session(token = token, role = role, name = name)
        }

    /**
     * Simpan sesi baru secara atomik (token + role + nama dalam satu transaksi).
     * Dipanggil oleh AuthRepository setelah login atau register berhasil.
     */
    suspend fun saveSession(session: Session) {
        context.sessionDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_TOKEN] = session.token
            preferences[PreferencesKeys.USER_ROLE] = session.role.name
            preferences[PreferencesKeys.USER_NAME] = session.name
        }
    }

    /**
     * Hapus sesi (token, role, nama) tanpa menyentuh penanda onboarding.
     * Dipanggil saat pengguna logout.
     */
    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_TOKEN)
            preferences.remove(PreferencesKeys.USER_ROLE)
            preferences.remove(PreferencesKeys.USER_NAME)
        }
    }
}
