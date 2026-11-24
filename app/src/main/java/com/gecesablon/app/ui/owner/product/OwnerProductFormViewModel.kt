package com.gecesablon.app.ui.owner.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.ProductRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductCategory
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Item baris warna yang dapat diubah pada formulir.
 */
data class ColorRowItem(
    val rowId: String = UUID.randomUUID().toString(),
    val nama: String = "",
    val hex: String = "#111111"
)

/**
 * Item pilihan ukuran (S-XXXL) dengan tambahan harga pada formulir.
 */
data class SizeSelectionItem(
    val label: String,
    val isSelected: Boolean = false,
    val tambahanHargaInput: String = "0"
)

/**
 * State untuk layar Tambah / Ubah Produk Owner.
 */
data class OwnerProductFormUiState(
    val isEditMode: Boolean = false,
    val productId: String = "",
    val nama: String = "",
    val kategori: ProductCategory? = ProductCategory.KAOS_CUSTOM,
    val bahan: String = "",
    val deskripsi: String = "",
    val hargaDasarInput: String = "",
    val aktif: Boolean = true,
    val colorRows: List<ColorRowItem> = emptyList(),
    val sizeSelections: List<SizeSelectionItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false,
    val namaError: String? = null,
    val kategoriError: String? = null,
    val bahanError: String? = null,
    val deskripsiError: String? = null,
    val hargaDasarError: String? = null,
    val warnaError: String? = null,
    val ukuranError: String? = null
)

/**
 * ViewModel untuk formulir Tambah dan Ubah Produk Owner.
 */
class OwnerProductFormViewModel(
    private val productRepository: ProductRepository,
    private val sessionStore: SessionStore,
    private val productId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OwnerProductFormUiState(
            isEditMode = !productId.isNullOrBlank(),
            productId = productId ?: "",
            isLoading = !productId.isNullOrBlank(),
            sizeSelections = defaultSizeSelections()
        )
    )
    val uiState: StateFlow<OwnerProductFormUiState> = _uiState.asStateFlow()

    init {
        if (!productId.isNullOrBlank()) {
            loadProductForEdit(productId)
        } else {
            initDefaultNewProduct()
        }
    }

    private suspend fun getOwnerSession(): Session? {
        val session = sessionStore.session.firstOrNull()
        return if (session != null && session.role == Role.OWNER) session else null
    }

    private fun defaultSizeSelections(): List<SizeSelectionItem> {
        return ProductRules.ALLOWED_SIZE_LABELS.map { label ->
            SizeSelectionItem(
                label = label,
                isSelected = label in listOf("S", "M", "L", "XL"),
                tambahanHargaInput = when (label) {
                    "XXL" -> "5000"
                    "XXXL" -> "10000"
                    else -> "0"
                }
            )
        }
    }

    private fun initDefaultNewProduct() {
        val initialColors = listOf(
            ColorRowItem(nama = "Hitam Pekat", hex = "#111111"),
            ColorRowItem(nama = "Putih Bersih", hex = "#FFFFFF")
        )
        _uiState.update {
            it.copy(
                colorRows = initialColors,
                sizeSelections = defaultSizeSelections(),
                hargaDasarInput = "65000"
            )
        }
    }

    private fun loadProductForEdit(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = getOwnerSession()
            if (session == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Akses ditolak. Fitur ini hanya untuk Owner.")
                }
                return@launch
            }

            when (val res = productRepository.getProductByIdForOwner(id, session)) {
                is Resource.Success -> {
                    val product = res.data
                    val colors = product.daftarWarna.map {
                        ColorRowItem(nama = it.nama, hex = it.hex)
                    }.ifEmpty {
                        listOf(ColorRowItem(nama = "Hitam", hex = "#111111"))
                    }

                    val sizes = ProductRules.ALLOWED_SIZE_LABELS.map { label ->
                        val existing = product.daftarUkuran.find { it.label.equals(label, ignoreCase = true) }
                        SizeSelectionItem(
                            label = label,
                            isSelected = existing != null,
                            tambahanHargaInput = existing?.tambahanHarga?.toString() ?: "0"
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nama = product.nama,
                            kategori = product.kategori,
                            bahan = product.bahan,
                            deskripsi = product.deskripsi,
                            hargaDasarInput = product.hargaDasar.toString(),
                            aktif = product.aktif,
                            colorRows = colors,
                            sizeSelections = sizes
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = res.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    // Handlers input form
    fun onNamaChanged(value: String) {
        _uiState.update { it.copy(nama = value, namaError = null) }
    }

    fun onKategoriSelected(value: ProductCategory) {
        _uiState.update { it.copy(kategori = value, kategoriError = null) }
    }

    fun onBahanChanged(value: String) {
        _uiState.update { it.copy(bahan = value, bahanError = null) }
    }

    fun onDeskripsiChanged(value: String) {
        _uiState.update { it.copy(deskripsi = value, deskripsiError = null) }
    }

    fun onHargaDasarChanged(value: String) {
        val clean = value.filter { it.isDigit() }
        _uiState.update { it.copy(hargaDasarInput = clean, hargaDasarError = null) }
    }

    fun onAktifChanged(value: Boolean) {
        _uiState.update { it.copy(aktif = value) }
    }

    // Variasi Warna
    fun addColorRow() {
        _uiState.update {
            it.copy(
                colorRows = it.colorRows + ColorRowItem(nama = "", hex = "#111111"),
                warnaError = null
            )
        }
    }

    fun addPresetColor(preset: ProductColor, duplicateMessage: String) {
        val current = _uiState.value.colorRows
        val isDuplicate = current.any {
            it.nama.trim().equals(preset.nama.trim(), ignoreCase = true) ||
            it.hex.trim().equals(preset.hex.trim(), ignoreCase = true)
        }
        if (isDuplicate) {
            _uiState.update {
                it.copy(warnaError = duplicateMessage)
            }
            return
        }
        _uiState.update {
            it.copy(
                colorRows = it.colorRows + ColorRowItem(nama = preset.nama, hex = preset.hex),
                warnaError = null
            )
        }
    }

    fun updateColorRow(index: Int, nama: String, hex: String) {
        val currentList = _uiState.value.colorRows.toMutableList()
        if (index in currentList.indices) {
            val normalizedHex = ProductRules.normalizeHexInput(hex)
            currentList[index] = currentList[index].copy(nama = nama, hex = normalizedHex)
            _uiState.update { it.copy(colorRows = currentList, warnaError = null) }
        }
    }

    fun removeColorRow(index: Int) {
        val currentList = _uiState.value.colorRows.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _uiState.update { it.copy(colorRows = currentList, warnaError = null) }
        }
    }

    // Variasi Ukuran
    fun toggleSizeSelection(label: String, selected: Boolean) {
        val updated = _uiState.value.sizeSelections.map { item ->
            if (item.label.equals(label, ignoreCase = true)) {
                item.copy(isSelected = selected)
            } else {
                item
            }
        }
        _uiState.update { it.copy(sizeSelections = updated, ukuranError = null) }
    }

    fun updateSizeExtraPrice(label: String, extraPrice: String) {
        val clean = extraPrice.filter { it.isDigit() }
        val updated = _uiState.value.sizeSelections.map { item ->
            if (item.label.equals(label, ignoreCase = true)) {
                item.copy(tambahanHargaInput = clean)
            } else {
                item
            }
        }
        _uiState.update { it.copy(sizeSelections = updated, ukuranError = null) }
    }

    // Dialog hapus pada mode edit
    fun requestDelete() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteProduct(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val session = getOwnerSession() ?: return@launch
            val id = _uiState.value.productId
            if (id.isBlank()) return@launch

            _uiState.update { it.copy(isSubmitting = true, showDeleteDialog = false) }
            when (val res = productRepository.hapus(session, id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                    onDeleted()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = res.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    /**
     * Menyimpan data produk (Tambah atau Ubah).
     */
    fun saveProduct(onSuccess: () -> Unit) {
        val current = _uiState.value
        val hargaDasar = current.hargaDasarInput.toLongOrNull() ?: 0L

        val productColors = current.colorRows.map {
            ProductColor(nama = it.nama.trim(), hex = it.hex.trim())
        }

        val selectedSizes = current.sizeSelections.filter { it.isSelected }.map {
            ProductSize(
                label = it.label,
                tambahanHarga = it.tambahanHargaInput.toLongOrNull() ?: 0L
            )
        }

        // Jalankan validasi fungsi murni ProductRules
        val validation = ProductRules.validateProductFields(
            nama = current.nama,
            kategori = current.kategori,
            bahan = current.bahan,
            deskripsi = current.deskripsi,
            hargaDasar = hargaDasar,
            daftarWarna = productColors,
            daftarUkuran = selectedSizes
        )

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    namaError = validation.namaError,
                    kategoriError = validation.kategoriError,
                    bahanError = validation.bahanError,
                    deskripsiError = validation.deskripsiError,
                    hargaDasarError = validation.hargaDasarError,
                    warnaError = validation.warnaError,
                    ukuranError = validation.ukuranError,
                    errorMessage = validation.generalError
                )
            }
            return
        }

        viewModelScope.launch {
            val session = getOwnerSession()
            if (session == null) {
                _uiState.update { it.copy(errorMessage = "Akses ditolak. Fitur ini hanya untuk Owner.") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val product = Product(
                id = current.productId,
                nama = current.nama.trim(),
                kategori = current.kategori!!,
                bahan = current.bahan.trim(),
                deskripsi = current.deskripsi.trim(),
                hargaDasar = hargaDasar,
                gambarUrl = null, // Placeholder gambar
                daftarWarna = productColors,
                daftarUkuran = selectedSizes,
                aktif = current.aktif
            )

            val result = if (current.isEditMode) {
                productRepository.ubah(session, product)
            } else {
                productRepository.tambah(session, product)
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    companion object {
        fun factory(
            productRepository: ProductRepository,
            sessionStore: SessionStore,
            productId: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                OwnerProductFormViewModel(productRepository, sessionStore, productId) as T
        }
    }
}
