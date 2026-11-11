package com.gecesablon.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UiState bersama untuk layar profil/akun (Customer ProfileScreen,
 * AdminAccountScreen, OwnerAccountScreen).
 * @param logoutDone  true setelah logout selesai; memicu navigasi ke Welcome.
 */
data class AccountUiState(
    val name: String = "",
    val role: Role? = null,
    val showLogoutDialog: Boolean = false,
    val logoutDone: Boolean = false
)

/**
 * ViewModel bersama untuk ProfileScreen, AdminAccountScreen, dan OwnerAccountScreen.
 * Membaca sesi dari SessionStore dan menangani logout.
 */
class AccountViewModel(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.session.collect { session ->
                _uiState.update { state ->
                    state.copy(
                        name = session?.name ?: "",
                        role = session?.role
                    )
                }
            }
        }
    }

    fun showLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun dismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun logout() {
        _uiState.update { it.copy(showLogoutDialog = false) }
        viewModelScope.launch {
            authRepository.logout()
            sessionStore.clearSession()
            _uiState.update { it.copy(logoutDone = true) }
        }
    }

    /** Dipanggil setelah navigasi ke Welcome selesai. */
    fun onLogoutNavigated() {
        _uiState.update { it.copy(logoutDone = false) }
    }

    companion object {
        fun factory(
            authRepository: AuthRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                AccountViewModel(
                    authRepository = authRepository,
                    sessionStore = sessionStore
                ) as T
        }
    }
}
