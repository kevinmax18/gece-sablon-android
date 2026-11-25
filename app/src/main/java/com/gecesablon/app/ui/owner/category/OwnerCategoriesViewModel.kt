package com.gecesablon.app.ui.owner.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.CategoryRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Category
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

/**
 * State untuk layar Kelola Kategori Owner.
 */
data class OwnerCategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isActionInProgress: Boolean = false,

    // Dialog Tambah / Ubah
    val showFormDialog: Boolean = false,
    val editingCategory: Category? = null,
    val formNamaInput: String = "",
    val formNamaError: String? = null,

    // Dialog Konfirmasi Hapus
    val categoryToDelete: Category? = null
)

/**
 * ViewModel untuk mengelola data kategori produk (CRUD oleh Owner).
 */
class OwnerCategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerCategoriesUiState())
    val uiState: StateFlow<OwnerCategoriesUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeCategories()
    }

    private suspend fun getOwnerSession(): Session? {
        val session = sessionStore.session.firstOrNull()
        return if (session != null && session.role == Role.OWNER) session else null
    }

    private fun observeCategories() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            categoryRepository.semuaKategoriFlow().collect { res ->
                when (res) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categories = res.data,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = res.message
                            )
                        }
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    fun openAddDialog() {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                editingCategory = null,
                formNamaInput = "",
                formNamaError = null
            )
        }
    }

    fun retryLoad() {
        observeCategories()
    }

    fun openEditDialog(category: Category) {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                editingCategory = category,
                formNamaInput = category.nama,
                formNamaError = null
            )
        }
    }

    fun dismissFormDialog() {
        _uiState.update {
            it.copy(
                showFormDialog = false,
                editingCategory = null,
                formNamaInput = "",
                formNamaError = null
            )
        }
    }

    fun onFormNamaChanged(value: String) {
        _uiState.update { it.copy(formNamaInput = value, formNamaError = null) }
    }

    fun submitCategoryForm() {
        val state = _uiState.value
        val rawInput = state.formNamaInput
        val editing = state.editingCategory

        val validationError = CategoryRules.validateNamaUnik(
            nama = rawInput,
            existingCategories = state.categories,
            excludeId = editing?.id
        )

        if (validationError != null) {
            _uiState.update { it.copy(formNamaError = validationError) }
            return
        }

        viewModelScope.launch {
            val session = getOwnerSession()
            if (session == null) {
                _uiState.update { it.copy(errorMessage = "Akses ditolak. Fitur ini hanya untuk Owner.") }
                return@launch
            }

            _uiState.update { it.copy(isActionInProgress = true) }

            val result = if (editing != null) {
                categoryRepository.ubahNama(session, editing.id, rawInput)
            } else {
                categoryRepository.tambah(session, rawInput)
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            showFormDialog = false,
                            editingCategory = null,
                            formNamaInput = "",
                            formNamaError = null,
                            successMessage = if (editing != null) "Nama kategori berhasil diperbarui." else "Kategori baru berhasil ditambahkan."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            formNamaError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun openDeleteDialog(category: Category) {
        _uiState.update { it.copy(categoryToDelete = category) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(categoryToDelete = null) }
    }

    fun confirmDeleteCategory() {
        val target = _uiState.value.categoryToDelete ?: return
        viewModelScope.launch {
            val session = getOwnerSession()
            if (session == null) {
                _uiState.update { it.copy(errorMessage = "Akses ditolak. Fitur ini hanya untuk Owner.") }
                return@launch
            }

            _uiState.update { it.copy(isActionInProgress = true, categoryToDelete = null) }
            when (val res = categoryRepository.hapus(session, target.id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            successMessage = "Kategori '${target.nama}' berhasil dihapus."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionInProgress = false,
                            errorMessage = res.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    companion object {
        fun factory(
            categoryRepository: CategoryRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                OwnerCategoriesViewModel(categoryRepository, sessionStore) as T
        }
    }
}
