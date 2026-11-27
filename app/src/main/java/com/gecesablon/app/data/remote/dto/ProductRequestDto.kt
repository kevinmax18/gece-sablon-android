package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Item material untuk request produk.
 */
@Serializable
data class ProductMaterialRequestDto(
    @SerialName("name") val name: String,
    @SerialName("extra_price") val extraPrice: Long? = 0L,
    @SerialName("is_default") val isDefault: Boolean? = false
)

/**
 * Item variasi warna untuk request produk.
 */
@Serializable
data class ProductColorRequestDto(
    @SerialName("name") val name: String,
    @SerialName("hex") val hex: String
)

/**
 * Item variasi ukuran untuk request produk.
 */
@Serializable
data class ProductSizeRequestDto(
    @SerialName("label") val label: String,
    @SerialName("extra_price") val extraPrice: Long? = 0L
)

/**
 * DTO request untuk menambah atau mengubah data produk oleh Owner.
 */
@Serializable
data class ProductRequestDto(
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("material") val material: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("base_price") val basePrice: Long,
    @SerialName("is_active") val isActive: Boolean? = true,
    @SerialName("materials") val materials: List<ProductMaterialRequestDto>? = null,
    @SerialName("colors") val colors: List<ProductColorRequestDto>? = null,
    @SerialName("sizes") val sizes: List<ProductSizeRequestDto>? = null
)
