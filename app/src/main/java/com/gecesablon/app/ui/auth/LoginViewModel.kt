package com.gecesablon.app.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.repository.AuthRepository
import com.gecesablon.app.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UiState untuk layar Login.
 * @param loginSuccess  Sesi hasil login; non-null memicu navigasi di UI.
 * @param redirect      Nilai redirect diteruskan ke callback navigasi (hanya nilai valid dari Routes.VALID_REDIRECTS).
 */
data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Session? = null,
    val redirect: String? = null
)

/**
 * ViewModel untuk LoginScreen.
 * Role sepenuhnya ditentukan oleh repository; tidak ada input role dari UI.
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Validasi redirect — hanya nilai dalam VALID_REDIRECTS yang diterima
        val raw = savedStateHandle.get<String>("redirect")
        val safeRedirect = if (raw != null && raw in Routes.VALID_REDIRECTS) raw else null
        _uiState.update { it.copy(redirect = safeRedirect) }
    }

    fun onIdentifierChange(value: String) {
        _uiState.update { it.copy(identifier = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = authRepository.login(
                identifier = state.identifier.trim(),
                password = state.password
            )) {
                is Resource.Loading -> { /* tidak mungkin dari repository sinkron */ }
                is Resource.Success -> {
                    sessionStore.saveSession(result.data)
                    _uiState.update { it.copy(isLoading = false, loginSuccess = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    /** Dipanggil setelah navigasi selesai agar event tidak diproses dua kali. */
    fun onNavigated() {
        _uiState.update { it.copy(loginSuccess = null) }
    }

    /** Redirect saat ini — diteruskan ke Register jika pengguna berpindah ke Register. */
    fun currentRedirect(): String? = _uiState.value.redirect

    companion object {
        fun factory(
            authRepository: AuthRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                LoginViewModel(
                    authRepository = authRepository,
                    sessionStore = sessionStore,
                    savedStateHandle = extras.createSavedStateHandle()
                ) as T
        }
    }
}
