package com.gecesablon.app.ui.admin.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.PriceCalculator
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductMaterial
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.repository.OrderRepository
import com.gecesablon.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminManualOrderUiState(
    val namaPelanggan: String = "",
    val noHpPelanggan: String = "",
    val availableProducts: List<Product> = emptyList(),
    val selectedProductId: String? = null,
    val selectedMaterialName: String? = null,
    val selectedColorName: String? = null,
    val selectedSizeLabel: String? = null,
    val quantity: Int = 1,
    val catatan: String = "",
    val uriDesain: String? = null,
    val calculatedUnitPrice: Long = 0L,
    val calculatedTotalPrice: Long = 0L,
    val namaError: String? = null,
    val hpError: String? = null,
    val productError: String? = null,
    val materialError: String? = null,
    val colorError: String? = null,
    val sizeError: String? = null,
    val quantityError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdOrder: Order? = null
)

class AdminManualOrderViewModel(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminManualOrderUiState())
    val uiState: StateFlow<AdminManualOrderUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private suspend fun getSession(): Session? {
        return sessionStore.session.firstOrNull()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            when (val res = productRepository.getProducts()) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    val products = res.data
                    val firstProduct = products.firstOrNull()
                    val defaultMaterial = firstProduct?.daftarBahan?.find { it.isDefault } ?: firstProduct?.daftarBahan?.firstOrNull()
                    val firstColor = firstProduct?.daftarWarna?.firstOrNull()?.nama
                    val firstSize = firstProduct?.daftarUkuran?.firstOrNull()?.label

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            availableProducts = products,
                            selectedProductId = firstProduct?.id,
                            selectedMaterialName = defaultMaterial?.nama,
                            selectedColorName = firstColor,
                            selectedSizeLabel = firstSize
                        )
                    }
                    recalculatePrice()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun onNamaChanged(nama: String) {
        _uiState.update { it.copy(namaPelanggan = nama, namaError = null) }
    }

    fun onHpChanged(hp: String) {
        _uiState.update { it.copy(noHpPelanggan = hp, hpError = null) }
    }

    fun onProductSelected(productId: String) {
        val product = _uiState.value.availableProducts.find { it.id == productId }
        val defaultMaterial = product?.daftarBahan?.find { it.isDefault } ?: product?.daftarBahan?.firstOrNull()
        val newColor = product?.daftarWarna?.firstOrNull()?.nama
        val newSize = product?.daftarUkuran?.firstOrNull()?.label
        _uiState.update {
            it.copy(
                selectedProductId = productId,
                selectedMaterialName = defaultMaterial?.nama,
                selectedColorName = newColor,
                selectedSizeLabel = newSize,
                productError = null,
                materialError = null,
                colorError = null,
                sizeError = null
            )
        }
        recalculatePrice()
    }

    fun onMaterialSelected(materialName: String) {
        _uiState.update { it.copy(selectedMaterialName = materialName, materialError = null) }
        recalculatePrice()
    }

    fun onColorSelected(colorName: String) {
        _uiState.update { it.copy(selectedColorName = colorName, colorError = null) }
    }

    fun onSizeSelected(sizeLabel: String) {
        _uiState.update { it.copy(selectedSizeLabel = sizeLabel, sizeError = null) }
        recalculatePrice()
    }

    fun onQuantityChanged(qty: Int) {
        if (qty in 1..999) {
            _uiState.update { it.copy(quantity = qty, quantityError = null) }
            recalculatePrice()
        }
    }

    fun onCatatanChanged(catatan: String) {
        _uiState.update { it.copy(catatan = catatan) }
    }

    fun onUriDesainChanged(uri: String?) {
        _uiState.update { it.copy(uriDesain = uri) }
    }

    private fun recalculatePrice() {
        val current = _uiState.value
        val product = current.availableProducts.find { it.id == current.selectedProductId }
        val material = product?.daftarBahan?.find { it.nama.equals(current.selectedMaterialName, ignoreCase = true) }
        val size = product?.daftarUkuran?.find { it.label.equals(current.selectedSizeLabel, ignoreCase = true) }

        val unitPrice = if (product != null && size != null) {
            PriceCalculator.calculateUnitPrice(
                basePrice = product.hargaDasar,
                sizeExtra = size.tambahanHarga,
                materialExtra = material?.tambahanHarga ?: 0L
            )
        } else {
            0L
        }
        val total = PriceCalculator.calculateTotalPrice(unitPrice, current.quantity)

        _uiState.update {
            it.copy(
                calculatedUnitPrice = unitPrice,
                calculatedTotalPrice = total
            )
        }
    }

    fun submitOrder() {
        val current = _uiState.value
        var hasError = false

        if (current.namaPelanggan.isBlank()) {
            _uiState.update { it.copy(namaError = "Nama pelanggan wajib diisi.") }
            hasError = true
        }

        val cleanPhone = current.noHpPelanggan.trim()
        val isValidPhone = (cleanPhone.startsWith("08") || cleanPhone.startsWith("628")) &&
                cleanPhone.length in 10..14 && cleanPhone.all { it.isDigit() }
        if (!isValidPhone) {
            _uiState.update { it.copy(hpError = "Nomor HP harus format Indonesia (08/628, 10–14 digit).") }
            hasError = true
        }

        if (current.selectedProductId == null) {
            _uiState.update { it.copy(productError = "Pilih produk sablon.") }
            hasError = true
        }
        if (current.selectedMaterialName == null) {
            _uiState.update { it.copy(materialError = "Pilih varian bahan.") }
            hasError = true
        }
        if (current.selectedColorName == null) {
            _uiState.update { it.copy(colorError = "Pilih varian warna.") }
            hasError = true
        }
        if (current.selectedSizeLabel == null) {
            _uiState.update { it.copy(sizeError = "Pilih varian ukuran.") }
            hasError = true
        }
        if (current.quantity !in 1..999) {
            _uiState.update { it.copy(quantityError = "Jumlah harus 1–999 pcs.") }
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

            val result = orderRepository.createManualOrder(
                actor = session,
                namaPelanggan = current.namaPelanggan.trim(),
                noHpPelanggan = cleanPhone,
                productId = current.selectedProductId!!,
                materialName = current.selectedMaterialName,
                colorName = current.selectedColorName!!,
                sizeLabel = current.selectedSizeLabel!!,
                quantity = current.quantity,
                catatan = current.catatan.trim(),
                uriDesain = current.uriDesain?.takeIf { it.isNotBlank() }
            )

            when (result) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, createdOrder = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    companion object {
        fun factory(
            orderRepository: OrderRepository,
            productRepository: ProductRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminManualOrderViewModel(orderRepository, productRepository, sessionStore) as T
            }
        }
    }
}
