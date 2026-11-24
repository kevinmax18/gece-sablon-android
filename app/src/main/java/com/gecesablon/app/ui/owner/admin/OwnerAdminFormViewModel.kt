package com.gecesablon.app.ui.owner.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.AccountRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerAdminFormUiState(
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class OwnerAdminFormViewModel(
    private val accountRepository: AccountRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerAdminFormUiState())
    val uiState: StateFlow<OwnerAdminFormUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, phoneError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submit() {
        val state = _uiState.value
        val nameErr = AccountRules.validasiNama(state.name)
        val emailErr = AccountRules.validasiFormatEmail(state.email)
        val passErr = AccountRules.validasiPassword(state.password)

        if (nameErr != null || emailErr != null || passErr != null) {
            _uiState.update {
                it.copy(
                    nameError = nameErr,
                    emailError = emailErr,
                    passwordError = passErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val session = sessionStore.session.firstOrNull()
            if (session == null) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Sesi Owner tidak ditemukan.") }
                return@launch
            }

            val result = accountRepository.tambahAdmin(
                actor = session,
                name = state.name.trim(),
                email = state.email.trim(),
                phone = state.phone.trim(),
                initialPassword = state.password
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> { /* no-op */ }
            }
        }
    }

    companion object {
        fun factory(
            accountRepository: AccountRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerAdminFormViewModel(
                    accountRepository = accountRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
