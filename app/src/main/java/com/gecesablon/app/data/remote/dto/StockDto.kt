package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO item inventaris stok dari Laravel API.
 */
@Serializable
data class StockDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("unit") val unit: String,
    @SerialName("quantity") val quantity: Double,
    @SerialName("min_quantity") val minQuantity: Double? = 0.0,
    @SerialName("updated_by") val updatedBy: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * DTO request untuk membuat atau mengubah data item stok.
 */
@Serializable
data class StockRequestDto(
    @SerialName("name") val name: String,
    @SerialName("unit") val unit: String,
    @SerialName("quantity") val quantity: Double,
    @SerialName("min_quantity") val minQuantity: Double? = 0.0
)
