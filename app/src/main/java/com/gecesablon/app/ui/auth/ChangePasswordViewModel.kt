package com.gecesablon.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.PasswordRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UiState untuk formulir Ubah Kata Sandi.
 */
data class ChangePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val oldPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null
)

/**
 * Event satu kali untuk UI (misal: pesan sukses & navigasi kembali).
 */
sealed interface ChangePasswordEvent {
    data class Success(val message: String) : ChangePasswordEvent
}

/**
 * ViewModel untuk menangani perubahan kata sandi.
 */
class ChangePasswordViewModel(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChangePasswordEvent>()
    val events: SharedFlow<ChangePasswordEvent> = _events.asSharedFlow()

    fun onOldPasswordChange(value: String) {
        _uiState.update { it.copy(oldPassword = value, oldPasswordError = null, generalError = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, newPasswordError = null, generalError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val validation = PasswordRules.validasiBaru(
            lama = state.oldPassword,
            baru = state.newPassword,
            konfirmasi = state.confirmPassword
        )

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    oldPasswordError = validation.oldPasswordError,
                    newPasswordError = validation.newPasswordError,
                    confirmPasswordError = validation.confirmPasswordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, generalError = null) }

        viewModelScope.launch {
            val session = sessionStore.session.firstOrNull()
            if (session == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Sesi tidak ditemukan. Silakan login kembali."
                    )
                }
                return@launch
            }

            when (val result = authRepository.changePassword(
                actor = session,
                oldPassword = state.oldPassword,
                newPassword = state.newPassword
            )) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(ChangePasswordEvent.Success("Kata sandi diperbarui"))
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    // no-op
                }
            }
        }
    }

    companion object {
        fun factory(
            authRepository: AuthRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)) {
                    return ChangePasswordViewModel(authRepository, sessionStore) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
