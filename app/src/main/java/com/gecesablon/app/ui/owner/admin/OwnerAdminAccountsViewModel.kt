package com.gecesablon.app.ui.owner.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.AdminAccount
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerAdminAccountsUiState(
    val isLoading: Boolean = true,
    val admins: List<AdminAccount> = emptyList(),
    val errorMessage: String? = null
)

class OwnerAdminAccountsViewModel(
    private val accountRepository: AccountRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerAdminAccountsUiState())
    val uiState: StateFlow<OwnerAdminAccountsUiState> = _uiState.asStateFlow()

    init {
        loadAdmins()
    }

    fun loadAdmins() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = sessionStore.session.firstOrNull()
            if (session == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sesi Owner tidak ditemukan.") }
                return@launch
            }

            accountRepository.daftarAdmin(session).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                admins = resource.data,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
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
                return OwnerAdminAccountsViewModel(
                    accountRepository = accountRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
