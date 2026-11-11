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
 * UiState untuk layar Register.
 * Error per-field ditampilkan di bawah masing-masing field saat Submit.
 * @param registerSuccess  Sesi hasil register+auto-login; non-null memicu navigasi.
 */
data class RegisterUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val agreedToTerms: Boolean = false,
    val isLoading: Boolean = false,

    // Error per-field (null = tidak ada error)
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,

    val globalError: String? = null,
    val registerSuccess: Session? = null,
    val redirect: String? = null
)

/**
 * ViewModel untuk RegisterScreen.
 * Validasi dilakukan di sini; role tidak diterima dari UI (selalu CUSTOMER).
 */
class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        val raw = savedStateHandle.get<String>("redirect")
        val safeRedirect = if (raw != null && raw in Routes.VALID_REDIRECTS) raw else null
        _uiState.update { it.copy(redirect = safeRedirect) }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value, phoneError = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    fun onAgreedToTermsChange(value: Boolean) = _uiState.update { it.copy(agreedToTerms = value, termsError = null) }

    fun register() {
        val state = _uiState.value
        if (state.isLoading) return

        // Validasi lokal
        val nameError = if (state.name.isBlank()) "Nama tidak boleh kosong." else null
        val phoneError = validatePhone(state.phone)
        val emailError = validateEmail(state.email)
        val passwordError = if (state.password.length < 8) "Kata sandi minimal 8 karakter." else null
        val confirmError = if (state.confirmPassword != state.password) "Konfirmasi kata sandi tidak cocok." else null
        val termsError = if (!state.agreedToTerms) "Anda harus menyetujui Syarat & Ketentuan." else null

        if (listOf(nameError, phoneError, emailError, passwordError, confirmError, termsError).any { it != null }) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    phoneError = phoneError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                    termsError = termsError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, globalError = null) }

        viewModelScope.launch {
            when (val result = authRepository.register(
                name = state.name.trim(),
                phone = state.phone.trim(),
                email = state.email.trim(),
                password = state.password
            )) {
                is Resource.Loading -> { /* tidak mungkin */ }
                is Resource.Success -> {
                    sessionStore.saveSession(result.data)
                    _uiState.update { it.copy(isLoading = false, registerSuccess = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, globalError = result.message) }
                }
            }
        }
    }

    /** Dipanggil setelah navigasi selesai. */
    fun onNavigated() {
        _uiState.update { it.copy(registerSuccess = null) }
    }

    fun currentRedirect(): String? = _uiState.value.redirect

    // ---------------------------------------------------------------------------
    // Validasi
    // ---------------------------------------------------------------------------

    /**
     * Validasi nomor HP format Indonesia:
     * Diawali 08 atau 628, total 10–14 digit, hanya angka.
     */
    private fun validatePhone(phone: String): String? {
        val trimmed = phone.trim()
        if (trimmed.isBlank()) return "Nomor HP tidak boleh kosong."
        val digits = trimmed.filter { it.isDigit() }
        val validPrefix = digits.startsWith("08") || digits.startsWith("628")
        val validLength = digits.length in 10..14
        return if (!validPrefix || !validLength) {
            "Format nomor HP tidak valid. Contoh: 081234567890"
        } else null
    }

    private fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return "Email tidak boleh kosong."
        val atIndex = trimmed.indexOf('@')
        if (atIndex < 1) return "Format email tidak valid."
        val domain = trimmed.substring(atIndex + 1)
        return if (!domain.contains('.') || domain.length < 3) {
            "Format email tidak valid."
        } else null
    }

    companion object {
        fun factory(
            authRepository: AuthRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                RegisterViewModel(
                    authRepository = authRepository,
                    sessionStore = sessionStore,
                    savedStateHandle = extras.createSavedStateHandle()
                ) as T
        }
    }
}
