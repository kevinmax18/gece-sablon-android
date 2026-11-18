package com.gecesablon.app.ui.admin.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.StockCategory
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AdminStockFormUiState(
    val isEditMode: Boolean = false,
    val stockId: String? = null,
    val nama: String = "",
    val kategori: StockCategory = StockCategory.BAJU_POLOS,
    val satuan: String = "pcs",
    val jumlahText: String = "",
    val batasMinText: String = "",
    val namaError: String? = null,
    val jumlahError: String? = null,
    val batasMinError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AdminStockFormViewModel(
    private val stockId: String?,
    private val stockRepository: StockRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminStockFormUiState(isEditMode = stockId != null, stockId = stockId))
    val uiState: StateFlow<AdminStockFormUiState> = _uiState.asStateFlow()

    init {
        if (stockId != null) {
            loadExistingItem(stockId)
        }
    }

    private suspend fun getSession(): Session? {
        return sessionStore.session.firstOrNull()
    }

    private fun loadExistingItem(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = getSession() ?: return@launch
            when (val res = stockRepository.getStockById(id, session)) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    val item = res.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nama = item.nama,
                            kategori = item.kategori,
                            satuan = item.satuan,
                            jumlahText = item.jumlah.toString(),
                            batasMinText = item.batasMinimum.toString()
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = res.message)
                    }
                }
            }
        }
    }

    fun onNamaChanged(value: String) {
        _uiState.update { it.copy(nama = value, namaError = null) }
    }

    fun onKategoriChanged(category: StockCategory) {
        _uiState.update { it.copy(kategori = category) }
    }

    fun onSatuanChanged(value: String) {
        _uiState.update { it.copy(satuan = value) }
    }

    fun onJumlahChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(jumlahText = filtered, jumlahError = null) }
    }

    fun onBatasMinChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(batasMinText = filtered, batasMinError = null) }
    }

    fun submit() {
        val current = _uiState.value
        var hasError = false

        if (current.nama.isBlank()) {
            _uiState.update { it.copy(namaError = "Nama item wajib diisi.") }
            hasError = true
        }

        val jumlahInt = current.jumlahText.toIntOrNull()
        if (jumlahInt == null || jumlahInt < 0) {
            _uiState.update { it.copy(jumlahError = "Jumlah harus berupa angka >= 0.") }
            hasError = true
        }

        val batasMinInt = current.batasMinText.toIntOrNull()
        if (batasMinInt == null || batasMinInt < 0) {
            _uiState.update { it.copy(batasMinError = "Batas minimum harus berupa angka >= 0.") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = getSession()
            if (session == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sesi telah berakhir.") }
                return@launch
            }

            val item = StockItem(
                id = current.stockId ?: UUID.randomUUID().toString(),
                nama = current.nama.trim(),
                kategori = current.kategori,
                satuan = current.satuan.trim(),
                jumlah = jumlahInt!!,
                batasMinimum = batasMinInt!!
            )

            val result = if (current.isEditMode) {
                stockRepository.updateStock(item, session)
            } else {
                stockRepository.addStock(item, session)
            }

            when (result) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    companion object {
        fun factory(
            stockId: String?,
            stockRepository: StockRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminStockFormViewModel(stockId, stockRepository, sessionStore) as T
            }
        }
    }
}
