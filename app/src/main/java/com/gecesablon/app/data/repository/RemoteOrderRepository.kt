package com.gecesablon.app.data.repository

import android.content.Context
import android.net.Uri
import com.gecesablon.app.core.network.fetchAllPages
import com.gecesablon.app.core.network.safeApiCall
import com.gecesablon.app.core.network.safeApiCallUnit
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductMaterial
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.ProductionStage
import com.gecesablon.app.data.model.ProductionState
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.remote.GeceRemoteApi
import com.gecesablon.app.data.remote.dto.ProductionStageItemRequestDto
import com.gecesablon.app.data.remote.dto.UpdateOrderStatusRequestDto
import com.gecesablon.app.data.remote.dto.UpdateProductionRequestDto
import com.gecesablon.app.data.remote.mapper.mapStageEnumToString
import com.gecesablon.app.data.remote.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Implementasi OrderRepository yang terhubung ke REST API Laravel GeCe Sablon.
 */
class RemoteOrderRepository(
    private val api: GeceRemoteApi,
    private val context: Context,
    private val productRepository: ProductRepository
) : OrderRepository {

    override fun getOrdersByUserId(userId: String): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading)
        val result = fetchAllPages { page ->
            safeApiCall { api.getOrders(page = page) }
        }
        when (result) {
            is Resource.Success -> emit(Resource.Success(result.data.map { it.toDomain() }))
            is Resource.Error -> emit(Resource.Error(result.message))
            is Resource.Loading -> emit(Resource.Loading)
        }
    }

    override suspend fun getOrderById(orderId: String): Resource<Order> {
        val result = safeApiCall { api.getOrderById(orderId) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun createOrder(
        userId: String,
        product: Product,
        material: ProductMaterial?,
        color: ProductColor,
        size: ProductSize,
        quantity: Int,
        catatan: String,
        uriDesain: String?,
        namaPelanggan: String,
        noHpPelanggan: String?
    ): Resource<Order> {
        return sendOrderToServer(
            product = product,
            materialName = material?.nama,
            colorName = color.nama,
            sizeLabel = size.label,
            quantity = quantity,
            catatan = catatan,
            uriDesain = uriDesain,
            namaPelanggan = namaPelanggan,
            noHpPelanggan = noHpPelanggan,
            isManual = false
        )
    }

    override suspend fun cancelOrder(orderId: String, userId: String): Resource<Unit> {
        return safeApiCallUnit { api.cancelOrder(orderId) }
    }

    override fun getAllOrders(actor: Session): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading)
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            emit(Resource.Error("Akses ditolak: Hanya staf workshop yang dapat melihat seluruh pesanan."))
            return@flow
        }
        val result = fetchAllPages { page ->
            safeApiCall { api.getOrders(page = page) }
        }
        when (result) {
            is Resource.Success -> emit(Resource.Success(result.data.map { it.toDomain() }))
            is Resource.Error -> emit(Resource.Error(result.message))
            is Resource.Loading -> emit(Resource.Loading)
        }
    }

    override suspend fun getStaffOrderDetail(orderId: String, actor: Session): Resource<Order> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat melihat detail pesanan ini.")
        }
        return getOrderById(orderId)
    }

    override suspend fun acceptAndStartProcess(orderId: String, actor: Session): Resource<Order> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat memproses pesanan.")
        }
        val req = UpdateOrderStatusRequestDto(status = "SEDANG_DIPROSES")
        val result = safeApiCall { api.updateOrderStatus(orderId, req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun updateProductionState(
        orderId: String,
        newState: ProductionState,
        actor: Session
    ): Resource<Order> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat memperbarui tahapan produksi.")
        }

        val stageItems = ProductionStage.orderedStages.map { stageEnum ->
            ProductionStageItemRequestDto(
                stage = mapStageEnumToString(stageEnum),
                isDone = newState.tahapanSelesai.contains(stageEnum)
            )
        }

        val req = UpdateProductionRequestDto(
            stages = stageItems,
            shirtArrived = newState.bajuTiba,
            dtfArrived = newState.dtfTiba,
            readyToPress = newState.siapDisablon
        )

        val result = safeApiCall { api.updateOrderProduction(orderId, req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun markOrderCompleted(orderId: String, actor: Session): Resource<Order> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat menyelesaikan pesanan.")
        }
        val req = UpdateOrderStatusRequestDto(status = "SELESAI")
        val result = safeApiCall { api.updateOrderStatus(orderId, req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun createManualOrder(
        actor: Session,
        namaPelanggan: String,
        noHpPelanggan: String,
        productId: String,
        materialName: String?,
        colorName: String,
        sizeLabel: String,
        quantity: Int,
        catatan: String,
        uriDesain: String?
    ): Resource<Order> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat membuat pesanan manual.")
        }
        val prodResult = productRepository.getProductById(productId)
        if (prodResult !is Resource.Success) {
            return Resource.Error((prodResult as? Resource.Error)?.message ?: "Produk tidak ditemukan.")
        }

        return sendOrderToServer(
            product = prodResult.data,
            materialName = materialName,
            colorName = colorName,
            sizeLabel = sizeLabel,
            quantity = quantity,
            catatan = catatan,
            uriDesain = uriDesain,
            namaPelanggan = namaPelanggan,
            noHpPelanggan = noHpPelanggan,
            isManual = true
        )
    }

    // =========================================================================
    // Helper Penyusun Multipart Request
    // =========================================================================

    private suspend fun sendOrderToServer(
        product: Product,
        materialName: String?,
        colorName: String,
        sizeLabel: String,
        quantity: Int,
        catatan: String,
        uriDesain: String?,
        namaPelanggan: String?,
        noHpPelanggan: String?,
        isManual: Boolean
    ): Resource<Order> {
        // Ambil DTO asli dari server untuk mendapatkan integer ID variasi (color_id, size_id, material_id)
        val remoteProdResult = safeApiCall { api.getProductById(product.id) }
        if (remoteProdResult !is Resource.Success) {
            return Resource.Error((remoteProdResult as? Resource.Error)?.message ?: "Gagal memverifikasi variasi produk di server.")
        }
        val remoteProdDto = remoteProdResult.data

        val colorDto = remoteProdDto.colors.find { it.name.equals(colorName, ignoreCase = true) }
            ?: remoteProdDto.colors.firstOrNull()
        val sizeDto = remoteProdDto.sizes.find { it.label.equals(sizeLabel, ignoreCase = true) }
            ?: remoteProdDto.sizes.firstOrNull()
        val matDto = if (!materialName.isNullOrBlank()) {
            remoteProdDto.materials.find { it.name.equals(materialName, ignoreCase = true) }
        } else {
            remoteProdDto.materials.find { it.isDefault == true } ?: remoteProdDto.materials.firstOrNull()
        }

        val colorId = colorDto?.id
        val sizeId = sizeDto?.id
        val materialId = matDto?.id

        if (colorId == null || sizeId == null) {
            return Resource.Error("Varian warna atau ukuran yang dipilih tidak valid di server.")
        }

        val textPlain = "text/plain".toMediaTypeOrNull()

        val productIdPart = product.id.toRequestBody(textPlain)
        val colorIdPart = colorId.toString().toRequestBody(textPlain)
        val sizeIdPart = sizeId.toString().toRequestBody(textPlain)
        val materialIdPart = materialId?.toString()?.toRequestBody(textPlain)
        val quantityPart = quantity.toString().toRequestBody(textPlain)
        val notesPart = catatan.ifBlank { "Pesanan dibuat dari aplikasi GeCe Sablon" }.toRequestBody(textPlain)

        val customerNamePart = if (isManual && !namaPelanggan.isNullOrBlank()) {
            namaPelanggan.toRequestBody(textPlain)
        } else null

        val customerPhonePart = if (isManual && !noHpPelanggan.isNullOrBlank()) {
            noHpPelanggan.toRequestBody(textPlain)
        } else null

        // Bangun part file desain jika ada URI
        val designPart: MultipartBody.Part? = if (!uriDesain.isNullOrBlank()) {
            try {
                val uri = Uri.parse(uriDesain)
                val resolver = context.contentResolver
                val mimeType = resolver.getType(uri) ?: "image/png"
                val inputStream = resolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    val reqBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("design", "design_upload.png", reqBody)
                } else null
            } catch (_: Exception) {
                null
            }
        } else null

        val result = safeApiCall {
            api.createOrder(
                productId = productIdPart,
                colorId = colorIdPart,
                sizeId = sizeIdPart,
                materialId = materialIdPart,
                quantity = quantityPart,
                notes = notesPart,
                design = designPart,
                customerName = customerNamePart,
                customerPhone = customerPhonePart
            )
        }

        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }
}
