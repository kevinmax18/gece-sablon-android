package com.gecesablon.app.ui.owner.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.PeriodRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.FinancePeriod
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionSource
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerTransactionFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val tipe: TransactionType = TransactionType.PENGELUARAN,
    val kategori: TransactionCategory = TransactionCategory.BAHAN_BAKU,
    val nominalText: String = "",
    val tanggal: Long = System.currentTimeMillis(),
    val keterangan: String = "",
    // Error per field
    val errorNominal: String? = null,
    val errorKeterangan: String? = null,
    val errorKategori: String? = null,
    // State global
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class OwnerTransactionFormViewModel(
    private val financeRepository: FinanceRepository,
    private val sessionStore: SessionStore,
    private val transactionId: String?,
    private val initialType: TransactionType?
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerTransactionFormUiState())
    val uiState: StateFlow<OwnerTransactionFormUiState> = _uiState.asStateFlow()

    private var currentSession: Session? = null

    init {
        viewModelScope.launch {
            currentSession = sessionStore.session.firstOrNull()
            if (transactionId != null) {
                // Mode edit: muat data transaksi
                loadTransaction(transactionId)
            } else {
                // Mode tambah baru: atur tipe awal jika ada dan kategori sah
                val tipe = initialType ?: TransactionType.PENGELUARAN
                val kategori = defaultKategoriForTipe(tipe)
                _uiState.update { it.copy(tipe = tipe, kategori = kategori, isEditMode = false) }
            }
        }
    }

    private suspend fun loadTransaction(id: String) {
        val session = currentSession ?: return
        _uiState.update { it.copy(isLoading = true) }
        val result = financeRepository.getTransactionById(id, session)
        when (result) {
            is Resource.Success -> {
                val tx = result.data
                // Hanya transaksi MANUAL yang bisa diubah
                if (tx.sumber == TransactionSource.PESANAN) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Transaksi dari pesanan tidak dapat diubah."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            tipe = tx.tipe,
                            kategori = tx.kategori,
                            nominalText = tx.nominal.toString(),
                            tanggal = tx.tanggal,
                            keterangan = tx.keterangan
                        )
                    }
                }
            }
            is Resource.Error -> {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
            else -> {}
        }
    }

    fun onTipeChange(tipe: TransactionType) {
        // Ganti tipe → reset kategori ke nilai sah pertama
        val kategori = defaultKategoriForTipe(tipe)
        _uiState.update {
            it.copy(tipe = tipe, kategori = kategori, errorKategori = null)
        }
    }

    fun onKategoriChange(kategori: TransactionCategory) {
        _uiState.update { it.copy(kategori = kategori, errorKategori = null) }
    }

    fun onNominalChange(text: String) {
        // Hanya digit yang diterima; kosong = ""
        val digits = text.filter { it.isDigit() }
        _uiState.update { it.copy(nominalText = digits, errorNominal = null) }
    }

    fun onTanggalChange(millis: Long) {
        // Tanggal tidak boleh melewati hari ini
        val today = PeriodRules.endOfDay(System.currentTimeMillis())
        val safe = millis.coerceAtMost(today)
        _uiState.update { it.copy(tanggal = safe) }
    }

    fun onKeteranganChange(text: String) {
        // Maks 200 karakter
        if (text.length <= 200) {
            _uiState.update { it.copy(keterangan = text, errorKeterangan = null) }
        }
    }

    fun onSave() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = currentSession ?: sessionStore.session.firstOrNull()

            if (session == null || session.role != Role.OWNER) {
                _uiState.update { it.copy(errorMessage = "Sesi tidak valid atau bukan Owner.") }
                return@launch
            }

            // Validasi nominal
            val nominal = state.nominalText.toLongOrNull()
            if (nominal == null || nominal <= 0L || nominal > 1_000_000_000L) {
                _uiState.update {
                    it.copy(errorNominal = "Nominal harus lebih dari Rp0 dan tidak melebihi Rp1.000.000.000.")
                }
                return@launch
            }

            // Validasi keterangan
            if (state.keterangan.length > 200) {
                _uiState.update { it.copy(errorKeterangan = "Keterangan maksimal 200 karakter.") }
                return@launch
            }

            // Validasi kategori sesuai tipe
            val validKategori = kategoriValidUntukTipe(state.tipe)
            if (state.kategori !in validKategori) {
                _uiState.update { it.copy(errorKategori = "Kategori tidak sesuai dengan tipe transaksi.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = if (state.isEditMode && transactionId != null) {
                financeRepository.updateTransaction(
                    id = transactionId,
                    actor = session,
                    tipe = state.tipe,
                    kategori = state.kategori,
                    nominal = nominal,
                    tanggal = state.tanggal,
                    keterangan = state.keterangan
                )
            } else {
                financeRepository.createTransaction(
                    actor = session,
                    tipe = state.tipe,
                    kategori = state.kategori,
                    nominal = nominal,
                    tanggal = state.tanggal,
                    keterangan = state.keterangan
                )
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    // Kategori default untuk tipe tertentu
    private fun defaultKategoriForTipe(tipe: TransactionType): TransactionCategory =
        when (tipe) {
            TransactionType.PENGELUARAN -> TransactionCategory.BAHAN_BAKU
            TransactionType.PEMASUKAN -> TransactionCategory.PENJUALAN_LAIN
        }

    // Daftar kategori valid untuk tipe tertentu
    private fun kategoriValidUntukTipe(tipe: TransactionType): List<TransactionCategory> =
        when (tipe) {
            TransactionType.PENGELUARAN -> TransactionCategory.expenseCategories
            TransactionType.PEMASUKAN -> TransactionCategory.manualIncomeCategories
        }

    companion object {
        fun factory(
            financeRepository: FinanceRepository,
            sessionStore: SessionStore,
            transactionId: String?,
            initialType: TransactionType?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerTransactionFormViewModel(
                    financeRepository = financeRepository,
                    sessionStore = sessionStore,
                    transactionId = transactionId,
                    initialType = initialType
                ) as T
            }
        }
    }
}
