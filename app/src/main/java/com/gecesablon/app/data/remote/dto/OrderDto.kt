package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO item dalam pesanan dari Laravel API.
 */
@Serializable
data class OrderItemDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("order_id") val orderId: Int? = null,
    @SerialName("product_id") val productId: Int,
    @SerialName("product_name") val productName: String,
    @SerialName("material_name") val materialName: String? = null,
    @SerialName("color_name") val colorName: String,
    @SerialName("size_label") val sizeLabel: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("unit_price") val unitPrice: Long
)

/**
 * DTO tahapan produksi pesanan dari Laravel API.
 */
@Serializable
data class ProductionStageDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("order_id") val orderId: Int? = null,
    @SerialName("stage") val stage: String,
    @SerialName("seq") val seq: Int = 0,
    @SerialName("is_done") val isDone: Boolean = false,
    @SerialName("done_at") val doneAt: String? = null,
    @SerialName("updated_by") val updatedBy: Int? = null
)

/**
 * DTO log aktivitas pesanan dari Laravel API.
 */
@Serializable
data class ActivityLogDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("order_id") val orderId: Int? = null,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("actor_name") val actorName: String,
    @SerialName("action") val action: String,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * DTO pesanan lengkap dari Laravel API.
 */
@Serializable
data class OrderDto(
    @SerialName("id") val id: Int,
    @SerialName("code") val code: String,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("created_via") val createdVia: String? = "customer",
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_phone") val customerPhone: String? = null,
    @SerialName("status") val status: String,
    @SerialName("total_price") val totalPrice: Long,
    @SerialName("notes") val notes: String? = null,
    @SerialName("design_path") val designPath: String? = null,
    @SerialName("shirt_arrived") val shirtArrived: Boolean = false,
    @SerialName("dtf_arrived") val dtfArrived: Boolean = false,
    @SerialName("ready_to_press") val readyToPress: Boolean = false,
    @SerialName("production_updated_at") val productionUpdatedAt: String? = null,
    @SerialName("production_updated_by") val productionUpdatedBy: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("items") val items: List<OrderItemDto> = emptyList(),
    @SerialName("stages") val stages: List<ProductionStageDto> = emptyList(),
    @SerialName("logs") val logs: List<ActivityLogDto> = emptyList()
)

/**
 * DTO request untuk pembaruan status pesanan.
 */
@Serializable
data class UpdateOrderStatusRequestDto(
    @SerialName("status") val status: String
)

/**
 * Item tahap untuk request pembaruan produksi.
 */
@Serializable
data class ProductionStageItemRequestDto(
    @SerialName("stage") val stage: String,
    @SerialName("is_done") val isDone: Boolean
)

/**
 * DTO request untuk pembaruan tahapan produksi pesanan.
 */
@Serializable
data class UpdateProductionRequestDto(
    @SerialName("stages") val stages: List<ProductionStageItemRequestDto>? = null,
    @SerialName("shirt_arrived") val shirtArrived: Boolean? = null,
    @SerialName("dtf_arrived") val dtfArrived: Boolean? = null,
    @SerialName("ready_to_press") val readyToPress: Boolean? = null
)
