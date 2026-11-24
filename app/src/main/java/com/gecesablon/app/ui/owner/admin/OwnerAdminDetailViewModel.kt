package com.gecesablon.app.ui.owner.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.AccountRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.AdminAccount
import com.gecesablon.app.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerAdminDetailUiState(
    val isLoading: Boolean = true,
    val admin: AdminAccount? = null,
    val editedName: String = "",
    val nameError: String? = null,
    val isSavingName: Boolean = false,
    val isTogglingStatus: Boolean = false,
    val showStatusDialog: Boolean = false,
    val newPasswordInput: String = "",
    val newPasswordError: String? = null,
    val isNewPasswordVisible: Boolean = false,
    val isResettingPassword: Boolean = false,
    val errorMessage: String? = null,
    val successSnackbarMessage: String? = null
)

class OwnerAdminDetailViewModel(
    private val accountRepository: AccountRepository,
    private val sessionStore: SessionStore,
    private val accountId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerAdminDetailUiState())
    val uiState: StateFlow<OwnerAdminDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = sessionStore.session.firstOrNull()
            if (session == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sesi Owner tidak ditemukan.") }
                return@launch
            }

            when (val result = accountRepository.getAdminById(accountId, session)) {
                is Resource.Success -> {
                    val acc = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            admin = acc,
                            editedName = acc.name,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> { /* no-op */ }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(editedName = value, nameError = null) }
    }

    fun saveName() {
        val nameErr = AccountRules.validasiNama(_uiState.value.editedName)
        if (nameErr != null) {
            _uiState.update { it.copy(nameError = nameErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingName = true, errorMessage = null) }
            val session = sessionStore.session.firstOrNull() ?: return@launch
            val res = accountRepository.ubahNama(session, accountId, _uiState.value.editedName.trim())
            when (res) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingName = false,
                            admin = it.admin?.copy(name = it.editedName.trim()),
                            successSnackbarMessage = "Nama berhasil diperbarui."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSavingName = false, errorMessage = res.message) }
                }
                is Resource.Loading -> { /* no-op */ }
            }
        }
    }

    fun showStatusDialog(show: Boolean) {
        _uiState.update { it.copy(showStatusDialog = show) }
    }

    fun toggleStatus() {
        val currentAdmin = _uiState.value.admin ?: return
        val targetStatus = !currentAdmin.isActive

        viewModelScope.launch {
            _uiState.update { it.copy(isTogglingStatus = true, showStatusDialog = false, errorMessage = null) }
            val session = sessionStore.session.firstOrNull() ?: return@launch
            val res = accountRepository.setAktif(session, accountId, targetStatus)
            when (res) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isTogglingStatus = false,
                            admin = currentAdmin.copy(isActive = targetStatus),
                            successSnackbarMessage = if (targetStatus) "Akun berhasil diaktifkan." else "Akun dinonaktifkan."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isTogglingStatus = false, errorMessage = res.message) }
                }
                is Resource.Loading -> { /* no-op */ }
            }
        }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPasswordInput = value, newPasswordError = null) }
    }

    fun toggleNewPasswordVisibility() {
        _uiState.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
    }

    fun resetPassword() {
        val passErr = AccountRules.validasiPassword(_uiState.value.newPasswordInput)
        if (passErr != null) {
            _uiState.update { it.copy(newPasswordError = passErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResettingPassword = true, errorMessage = null) }
            val session = sessionStore.session.firstOrNull() ?: return@launch
            val res = accountRepository.aturUlangPassword(
                actor = session,
                accountId = accountId,
                newPassword = _uiState.value.newPasswordInput
            )
            when (res) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isResettingPassword = false,
                            newPasswordInput = "",
                            successSnackbarMessage = "Kata sandi berhasil diatur ulang."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isResettingPassword = false, errorMessage = res.message) }
                }
                is Resource.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(successSnackbarMessage = null) }
    }

    companion object {
        fun factory(
            accountRepository: AccountRepository,
            sessionStore: SessionStore,
            accountId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerAdminDetailViewModel(
                    accountRepository = accountRepository,
                    sessionStore = sessionStore,
                    accountId = accountId
                ) as T
            }
        }
    }
}
