package com.gecesablon.app.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gecesablon.app.core.datastore.SessionStore
import com.gecesablon.app.core.util.ProductionRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.ProductionStage
import com.gecesablon.app.data.model.ProductionState
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StaffOrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val stagingState: ProductionState = ProductionState(),
    val hasUnsavedChanges: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class StaffOrderDetailViewModel(
    private val orderId: String,
    private val orderRepository: OrderRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffOrderDetailUiState())
    val uiState: StateFlow<StaffOrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadOrderDetail()
    }

    fun loadOrderDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val session = sessionStore.session.firstOrNull()
            if (session == null || (session.role != Role.ADMIN && session.role != Role.OWNER)) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Akses ditolak. Sesi staf tidak ditemukan."
                    )
                }
                return@launch
            }

            when (val result = orderRepository.getStaffOrderDetail(orderId, session)) {
                is Resource.Success -> {
                    val order = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            order = order,
                            stagingState = order.produksi,
                            hasUnsavedChanges = false,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun toggleStage(stage: ProductionStage) {
        val currentOrder = _uiState.value.order ?: return
        if (currentOrder.status == OrderStatus.DITERIMA) return

        val currentStaging = _uiState.value.stagingState
        val isFinished = stage in currentStaging.tahapanSelesai

        if (isFinished) {
            // Coba batalkan
            val izinBatal = ProductionRules.bolehBatal(stage, currentStaging)
            if (!izinBatal.diizinkan) {
                _uiState.update { it.copy(errorMessage = izinBatal.pesan) }
                return
            }
            val newTahapan = currentStaging.tahapanSelesai - stage
            updateStaging(currentStaging.copy(tahapanSelesai = newTahapan))
        } else {
            // Coba centang
            val izinCentang = ProductionRules.bolehCentang(stage, currentStaging)
            if (!izinCentang.diizinkan) {
                _uiState.update { it.copy(errorMessage = izinCentang.pesan) }
                return
            }
            val newTahapan = currentStaging.tahapanSelesai + stage
            updateStaging(currentStaging.copy(tahapanSelesai = newTahapan))
        }
    }

    fun toggleBajuTiba(target: Boolean) {
        val currentOrder = _uiState.value.order ?: return
        if (currentOrder.status == OrderStatus.DITERIMA) return

        val currentStaging = _uiState.value.stagingState
        val izin = ProductionRules.bolehUbahBajuTiba(target, currentStaging)
        if (!izin.diizinkan) {
            _uiState.update { it.copy(errorMessage = izin.pesan) }
            return
        }
        updateStaging(currentStaging.copy(bajuTiba = target))
    }

    fun toggleDtfTiba(target: Boolean) {
        val currentOrder = _uiState.value.order ?: return
        if (currentOrder.status == OrderStatus.DITERIMA) return

        val currentStaging = _uiState.value.stagingState
        val izin = ProductionRules.bolehUbahDtfTiba(target, currentStaging)
        if (!izin.diizinkan) {
            _uiState.update { it.copy(errorMessage = izin.pesan) }
            return
        }
        updateStaging(currentStaging.copy(dtfTiba = target))
    }

    fun toggleSiapDisablon(target: Boolean) {
        val currentOrder = _uiState.value.order ?: return
        if (currentOrder.status == OrderStatus.DITERIMA) return

        val currentStaging = _uiState.value.stagingState
        val izin = ProductionRules.bolehUbahSiapDisablon(target, currentStaging)
        if (!izin.diizinkan) {
            _uiState.update { it.copy(errorMessage = izin.pesan) }
            return
        }
        updateStaging(currentStaging.copy(siapDisablon = target))
    }

    private fun updateStaging(newStaging: ProductionState) {
        val original = _uiState.value.order?.produksi ?: ProductionState()
        val hasChanges = newStaging != original
        _uiState.update {
            it.copy(
                stagingState = newStaging,
                hasUnsavedChanges = hasChanges,
                errorMessage = null
            )
        }
    }

    fun acceptAndStartProcess() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val session = sessionStore.session.firstOrNull() ?: return@launch

            when (val result = orderRepository.acceptAndStartProcess(orderId, session)) {
                is Resource.Success -> {
                    val updatedOrder = result.data
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            order = updatedOrder,
                            stagingState = updatedOrder.produksi,
                            hasUnsavedChanges = false,
                            successMessage = "Pesanan berhasil diterima dan masuk antrean produksi."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                    loadOrderDetail()
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveProductionState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val session = sessionStore.session.firstOrNull() ?: return@launch
            val stateToSave = _uiState.value.stagingState

            when (val result = orderRepository.updateProductionState(orderId, stateToSave, session)) {
                is Resource.Success -> {
                    val updatedOrder = result.data
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            order = updatedOrder,
                            stagingState = updatedOrder.produksi,
                            hasUnsavedChanges = false,
                            successMessage = "Perubahan tahapan produksi berhasil disimpan."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                    loadOrderDetail()
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun markOrderCompleted() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val session = sessionStore.session.firstOrNull() ?: return@launch

            when (val result = orderRepository.markOrderCompleted(orderId, session)) {
                is Resource.Success -> {
                    val updatedOrder = result.data
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            order = updatedOrder,
                            stagingState = updatedOrder.produksi,
                            hasUnsavedChanges = false,
                            successMessage = "Pesanan telah ditandai selesai."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                    loadOrderDetail()
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    companion object {
        fun factory(
            orderId: String,
            orderRepository: OrderRepository,
            sessionStore: SessionStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StaffOrderDetailViewModel(
                    orderId = orderId,
                    orderRepository = orderRepository,
                    sessionStore = sessionStore
                ) as T
            }
        }
    }
}
