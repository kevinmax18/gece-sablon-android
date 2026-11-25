package com.gecesablon.app.ui.owner.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.ProductRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Category
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductMaterial
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.repository.CategoryRepository
import com.gecesablon.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import com.gecesablon.app.ui.theme.ShapePill

/**
 * Item baris variasi bahan pada formulir.
 */
data class MaterialRowItem(
    val id: String = UUID.randomUUID().toString(),
    val nama: String = "",
    val tambahanHargaInput: String = "0",
    val isDefault: Boolean = false
)

/**
 * Item baris variasi warna pada formulir.
 */
data class ColorRowItem(
    val tempId: String = UUID.randomUUID().toString(),
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
    val categoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val bahan: String = "",
    val deskripsi: String = "",
    val hargaDasarInput: String = "",
    val aktif: Boolean = true,
    val materialRows: List<MaterialRowItem> = emptyList(),
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
    val daftarBahanError: String? = null,
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
    private val categoryRepository: CategoryRepository,
    private val sessionStore: SessionStore,
    private val productId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OwnerProductFormUiState(
            isEditMode = !productId.isNullOrBlank(),
            productId = productId ?: "",
            isLoading = true,
            materialRows = listOf(MaterialRowItem(nama = "Cotton Combed 30s", tambahanHargaInput = "0", isDefault = true)),
            sizeSelections = defaultSizeSelections()
        )
    )
    val uiState: StateFlow<OwnerProductFormUiState> = _uiState.asStateFlow()

    init {
        loadCategoriesAndInitialData()
    }

    private suspend fun getOwnerSession(): Session? {
        val session = sessionStore.session.firstOrNull()
        return if (session != null && session.role == Role.OWNER) session else null
    }

    private fun loadCategoriesAndInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Muat kategori aktif
            val catRes = categoryRepository.getCategories()
            val loadedCats = if (catRes is Resource.Success) catRes.data else emptyList()

            _uiState.update { current ->
                current.copy(
                    categories = loadedCats,
                    categoryId = current.categoryId ?: loadedCats.firstOrNull()?.id
                )
            }

            // 2. Jika mode edit, muat data produk
            if (_uiState.value.isEditMode && !productId.isNullOrBlank()) {
                loadProductDetail(productId)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        colorRows = listOf(ColorRowItem(nama = "Hitam", hex = "#111111")),
                        materialRows = listOf(MaterialRowItem(nama = "Cotton Combed 30s", tambahanHargaInput = "0", isDefault = true))
                    )
                }
            }
        }
    }

    private fun loadProductDetail(id: String) {
        viewModelScope.launch {
            val session = getOwnerSession()
            if (session == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Akses ditolak. Fitur ini hanya untuk Owner.")
                }
                return@launch
            }

            when (val res = productRepository.getProductByIdForOwner(id, session)) {
                is Resource.Success -> {
                    val p = res.data
                    val loadedMaterials = p.daftarBahan.map {
                        MaterialRowItem(
                            id = it.id,
                            nama = it.nama,
                            tambahanHargaInput = it.tambahanHarga.toString(),
                            isDefault = it.isDefault
                        )
                    }
                    val loadedColors = p.daftarWarna.map { ColorRowItem(nama = it.nama, hex = it.hex) }
                    val loadedSizes = ProductRules.ALLOWED_SIZE_LABELS.map { label ->
                        val existing = p.daftarUkuran.find { it.label.equals(label, ignoreCase = true) }
                        SizeSelectionItem(
                            label = label,
                            isSelected = existing != null,
                            tambahanHargaInput = (existing?.tambahanHarga ?: 0L).toString()
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nama = p.nama,
                            categoryId = p.categoryId,
                            bahan = p.bahan,
                            deskripsi = p.deskripsi,
                            hargaDasarInput = p.hargaDasar.toString(),
                            aktif = p.aktif,
                            materialRows = if (loadedMaterials.isNotEmpty()) loadedMaterials else listOf(MaterialRowItem(isDefault = true)),
                            colorRows = if (loadedColors.isNotEmpty()) loadedColors else listOf(ColorRowItem()),
                            sizeSelections = loadedSizes
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

    fun onCategoryIdSelected(value: String) {
        _uiState.update { it.copy(categoryId = value, kategoriError = null) }
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

    // Pilihan Bahan
    fun addMaterialRow() {
        _uiState.update {
            val shouldBeDefault = it.materialRows.isEmpty() || it.materialRows.none { m -> m.isDefault }
            it.copy(
                materialRows = it.materialRows + MaterialRowItem(nama = "", tambahanHargaInput = "0", isDefault = shouldBeDefault),
                daftarBahanError = null
            )
        }
    }

    fun updateMaterialRow(index: Int, nama: String, tambahanHargaInput: String) {
        val cleanPrice = tambahanHargaInput.filter { it.isDigit() }
        _uiState.update { state ->
            val updated = state.materialRows.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(nama = nama, tambahanHargaInput = cleanPrice)
            }
            state.copy(materialRows = updated, daftarBahanError = null)
        }
    }

    fun setMaterialDefault(index: Int) {
        _uiState.update { state ->
            val updated = state.materialRows.mapIndexed { idx, item ->
                item.copy(isDefault = (idx == index))
            }
            state.copy(materialRows = updated, daftarBahanError = null)
        }
    }

    fun removeMaterialRow(index: Int) {
        _uiState.update { state ->
            if (state.materialRows.size <= 1) {
                state.copy(daftarBahanError = "Minimal 1 pilihan bahan harus dipertahankan.")
            } else {
                val updated = state.materialRows.toMutableList()
                val removedItem = if (index in updated.indices) updated.removeAt(index) else null
                // Jika yang dihapus adalah default, jadikan baris pertama sebagai default baru
                if (removedItem?.isDefault == true && updated.isNotEmpty()) {
                    updated[0] = updated[0].copy(isDefault = true)
                }
                state.copy(
                    materialRows = updated,
                    daftarBahanError = null
                )
            }
        }
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

    fun addPresetColor(preset: ProductColor, duplicateMsg: String) {
        val currentRows = _uiState.value.colorRows
        val isDuplicate = currentRows.any {
            it.nama.trim().equals(preset.nama.trim(), ignoreCase = true) ||
                it.hex.trim().equals(preset.hex.trim(), ignoreCase = true)
        }
        if (isDuplicate) {
            _uiState.update { it.copy(warnaError = duplicateMsg) }
        } else {
            _uiState.update {
                it.copy(
                    colorRows = it.colorRows + ColorRowItem(nama = preset.nama, hex = preset.hex),
                    warnaError = null
                )
            }
        }
    }

    fun updateColorRow(index: Int, nama: String, hex: String) {
        _uiState.update { state ->
            val updated = state.colorRows.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(nama = nama, hex = hex)
            }
            state.copy(colorRows = updated, warnaError = null)
        }
    }

    fun removeColorRow(index: Int) {
        _uiState.update { state ->
            if (state.colorRows.size <= 1) {
                state.copy(warnaError = "Minimal 1 varian warna harus dipertahankan.")
            } else {
                val updated = state.colorRows.toMutableList()
                if (index in updated.indices) {
                    updated.removeAt(index)
                }
                state.copy(
                    colorRows = updated,
                    warnaError = null
                )
            }
        }
    }

    // Variasi Ukuran
    fun toggleSizeSelected(label: String) {
        _uiState.update { state ->
            val updated = state.sizeSelections.map { sizeItem ->
                if (sizeItem.label == label) sizeItem.copy(isSelected = !sizeItem.isSelected) else sizeItem
            }
            state.copy(sizeSelections = updated, ukuranError = null)
        }
    }

    fun toggleSizeSelection(label: String, isChecked: Boolean) {
        _uiState.update { state ->
            val updated = state.sizeSelections.map { sizeItem ->
                if (sizeItem.label == label) sizeItem.copy(isSelected = isChecked) else sizeItem
            }
            state.copy(sizeSelections = updated, ukuranError = null)
        }
    }

    fun updateSizeExtraPrice(label: String, priceInput: String) {
        val clean = priceInput.filter { it.isDigit() }
        _uiState.update { state ->
            val updated = state.sizeSelections.map { sizeItem ->
                if (sizeItem.label == label) sizeItem.copy(tambahanHargaInput = clean) else sizeItem
            }
            state.copy(sizeSelections = updated, ukuranError = null)
        }
    }

    // Simpan Form (Tambah / Ubah)
    fun saveProduct(onSuccess: (() -> Unit)? = null) {
        val state = _uiState.value
        val parsedHargaDasar = state.hargaDasarInput.toLongOrNull() ?: 0L

        val productMaterials = state.materialRows.map {
            ProductMaterial(
                id = it.id,
                nama = it.nama.trim(),
                tambahanHarga = it.tambahanHargaInput.toLongOrNull() ?: 0L,
                isDefault = it.isDefault
            )
        }

        val productColors = state.colorRows.map {
            ProductColor(nama = it.nama.trim(), hex = it.hex.trim())
        }

        val productSizes = state.sizeSelections
            .filter { it.isSelected }
            .map {
                ProductSize(
                    label = it.label.trim(),
                    tambahanHarga = it.tambahanHargaInput.toLongOrNull() ?: 0L
                )
            }

        val validation = ProductRules.validateProductFields(
            nama = state.nama,
            categoryId = state.categoryId,
            bahan = state.bahan,
            deskripsi = state.deskripsi,
            hargaDasar = parsedHargaDasar,
            daftarBahan = productMaterials,
            daftarWarna = productColors,
            daftarUkuran = productSizes
        )

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    namaError = validation.namaError,
                    kategoriError = validation.kategoriError,
                    bahanError = validation.bahanError,
                    daftarBahanError = validation.daftarBahanError,
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
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val session = getOwnerSession()
            if (session == null) {
                _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = "Akses ditolak. Fitur ini hanya untuk Owner.")
                }
                return@launch
            }

            val productToSave = Product(
                id = if (state.isEditMode) state.productId else "",
                nama = state.nama.trim(),
                categoryId = state.categoryId!!,
                bahan = state.bahan.trim(),
                deskripsi = state.deskripsi.trim(),
                hargaDasar = parsedHargaDasar,
                gambarUrl = null,
                daftarBahan = productMaterials,
                daftarWarna = productColors,
                daftarUkuran = productSizes,
                aktif = state.aktif
            )

            val result = if (state.isEditMode) {
                productRepository.ubah(session, productToSave)
            } else {
                productRepository.tambah(session, productToSave)
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                    onSuccess?.invoke()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    // Dialog & Hapus Produk
    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete(onDeleted: (() -> Unit)? = null) {
        val state = _uiState.value
        if (!state.isEditMode || state.productId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, showDeleteDialog = false, errorMessage = null) }
            val session = getOwnerSession()
            if (session == null) {
                _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = "Akses ditolak. Fitur ini hanya untuk Owner.")
                }
                return@launch
            }

            when (val res = productRepository.hapus(session, state.productId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                    onDeleted?.invoke()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = res.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    companion object {
        fun defaultSizeSelections(): List<SizeSelectionItem> {
            return ProductRules.ALLOWED_SIZE_LABELS.map { label ->
                SizeSelectionItem(
                    label = label,
                    isSelected = label in listOf("S", "M", "L", "XL"),
                    tambahanHargaInput = if (label == "XXL") "5000" else if (label == "XXXL") "10000" else "0"
                )
            }
        }

        fun factory(
            productRepository: ProductRepository,
            categoryRepository: CategoryRepository,
            sessionStore: SessionStore,
            productId: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerProductFormViewModel(
                    productRepository = productRepository,
                    categoryRepository = categoryRepository,
                    sessionStore = sessionStore,
                    productId = productId
                ) as T
            }
        }
    }
}
