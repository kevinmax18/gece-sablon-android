package com.gecesablon.app.ui.customer.order

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.R
import com.gecesablon.app.core.util.PriceCalculator
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.repository.OrderRepository
import com.gecesablon.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDraftUiState(
    val isLoadingProduct: Boolean = true,
    val product: Product? = null,
    val selectedColor: ProductColor? = null,
    val selectedSize: ProductSize? = null,
    val quantity: Int = 1,
    val quantityInput: String = "1",
    val catatan: String = "",
    val designUri: Uri? = null,
    val designFileName: String? = null,
    val designFileSize: Long = 0L,

    // Perhitungan harga pratinjau (rumus PriceCalculator)
    val hargaSatuan: Long = 0L,
    val totalHarga: Long = 0L,

    // Error validasi per field
    @StringRes val colorErrorRes: Int? = null,
    @StringRes val sizeErrorRes: Int? = null,
    @StringRes val quantityErrorRes: Int? = null,
    @StringRes val designOrNoteErrorRes: Int? = null,
    val generalError: String? = null,

    // Status aksi submit
    val isSubmitting: Boolean = false,
    val createdOrder: Order? = null
)

/**
 * ViewModel bersama untuk formulir pemesanan dan review konfirmasi pesanan (scoped ke nested graph order_flow).
 */
class OrderDraftViewModel(
    private val productId: String,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDraftUiState())
    val uiState: StateFlow<OrderDraftUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProduct = true, generalError = null) }
            when (val result = productRepository.getProductById(productId)) {
                is Resource.Success -> {
                    val product = result.data
                    val defaultColor = product.daftarWarna.firstOrNull()
                    val defaultSize = product.daftarUkuran.firstOrNull()
                    val unitPrice = PriceCalculator.calculateUnitPrice(
                        basePrice = product.hargaDasar,
                        sizeExtra = defaultSize?.tambahanHarga ?: 0L
                    )
                    val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, 1)

                    _uiState.update {
                        it.copy(
                            isLoadingProduct = false,
                            product = product,
                            selectedColor = defaultColor,
                            selectedSize = defaultSize,
                            hargaSatuan = unitPrice,
                            totalHarga = totalPrice
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingProduct = false,
                            generalError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onColorSelected(color: ProductColor) {
        _uiState.update {
            it.copy(selectedColor = color, colorErrorRes = null)
        }
    }

    fun onSizeSelected(size: ProductSize) {
        _uiState.update { current ->
            val product = current.product
            val basePrice = product?.hargaDasar ?: 0L
            val unitPrice = PriceCalculator.calculateUnitPrice(basePrice, size.tambahanHarga)
            val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, current.quantity)
            current.copy(
                selectedSize = size,
                sizeErrorRes = null,
                hargaSatuan = unitPrice,
                totalHarga = totalPrice
            )
        }
    }

    fun onQuantityInputChanged(input: String) {
        // Hanya izinkan angka, maks 3 karakter (1-999)
        val filtered = input.filter { it.isDigit() }.take(3)
        val qty = filtered.toIntOrNull() ?: 0

        _uiState.update { current ->
            val product = current.product
            val basePrice = product?.hargaDasar ?: 0L
            val sizeExtra = current.selectedSize?.tambahanHarga ?: 0L
            val unitPrice = PriceCalculator.calculateUnitPrice(basePrice, sizeExtra)
            val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, qty)

            val errorRes = if (qty !in 1..999 && filtered.isNotBlank()) {
                R.string.order_error_quantity_invalid
            } else {
                null
            }

            current.copy(
                quantityInput = filtered,
                quantity = qty,
                hargaSatuan = unitPrice,
                totalHarga = totalPrice,
                quantityErrorRes = errorRes
            )
        }
    }

    fun onNotesChanged(notes: String) {
        val trimmedOrCapped = notes.take(500)
        _uiState.update { current ->
            val clearsError = current.designUri != null || trimmedOrCapped.isNotBlank()
            current.copy(
                catatan = trimmedOrCapped,
                designOrNoteErrorRes = if (clearsError) null else current.designOrNoteErrorRes
            )
        }
    }

    fun onDesignSelected(uri: Uri, fileName: String, fileSize: Long) {
        _uiState.update { current ->
            current.copy(
                designUri = uri,
                designFileName = fileName,
                designFileSize = fileSize,
                designOrNoteErrorRes = null
            )
        }
    }

    fun onDesignRemoved() {
        _uiState.update { current ->
            current.copy(
                designUri = null,
                designFileName = null,
                designFileSize = 0L
            )
        }
    }

    fun setFileValidationError(@StringRes errorRes: Int) {
        _uiState.update { it.copy(designOrNoteErrorRes = errorRes) }
    }

    /**
     * Memvalidasi form sebelum beralih ke layar review konfirmasi.
     * Mengembalikan true jika valid, false jika ada error.
     */
    fun validateForm(): Boolean {
        val current = _uiState.value
        var isValid = true

        var colorErr: Int? = null
        if (current.selectedColor == null) {
            colorErr = R.string.order_error_color_required
            isValid = false
        }

        var sizeErr: Int? = null
        if (current.selectedSize == null) {
            sizeErr = R.string.order_error_size_required
            isValid = false
        }

        var qtyErr: Int? = null
        if (current.quantity !in 1..999) {
            qtyErr = R.string.order_error_quantity_invalid
            isValid = false
        }

        var designNoteErr: Int? = null
        val hasDesign = current.designUri != null
        val hasNotes = current.catatan.trim().isNotBlank()
        if (!hasDesign && !hasNotes) {
            designNoteErr = R.string.order_error_design_or_note_required
            isValid = false
        }

        _uiState.update {
            it.copy(
                colorErrorRes = colorErr,
                sizeErrorRes = sizeErr,
                quantityErrorRes = qtyErr,
                designOrNoteErrorRes = designNoteErr
            )
        }

        return isValid
    }

    /**
     * Mengirim pesanan akhir ke repository.
     */
    fun submitOrder(userId: String, onSuccess: (Order) -> Unit) {
        val current = _uiState.value
        if (current.isSubmitting) return

        if (!validateForm()) return

        val product = current.product ?: return
        val color = current.selectedColor ?: return
        val size = current.selectedSize ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, generalError = null) }

            val result = orderRepository.createOrder(
                userId = userId,
                product = product,
                color = color,
                size = size,
                quantity = current.quantity,
                catatan = current.catatan,
                uriDesain = current.designUri?.toString()
            )

            when (result) {
                is Resource.Success -> {
                    val order = result.data
                    _uiState.update { it.copy(isSubmitting = false, createdOrder = order) }
                    onSuccess(order)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            generalError = result.message
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    companion object {
        fun factory(
            productId: String,
            productRepository: ProductRepository,
            orderRepository: OrderRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OrderDraftViewModel(
                    productId = productId,
                    productRepository = productRepository,
                    orderRepository = orderRepository
                ) as T
            }
        }
    }
}
