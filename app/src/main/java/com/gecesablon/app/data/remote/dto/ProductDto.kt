package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO bahan produk (materials) dari Laravel API.
 */
@Serializable
data class ProductMaterialDto(
    @SerialName("id") val id: Int,
    @SerialName("product_id") val productId: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("extra_price") val extraPrice: Long? = 0L,
    @Serializable(with = LenientBooleanSerializer::class)
    @SerialName("is_default") val isDefault: Boolean? = false
)

/**
 * DTO variasi warna produk (colors) dari Laravel API.
 */
@Serializable
data class ProductColorDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("product_id") val productId: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("hex") val hex: String
)

/**
 * DTO variasi ukuran produk (sizes) dari Laravel API.
 */
@Serializable
data class ProductSizeDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("product_id") val productId: Int? = null,
    @SerialName("label") val label: String,
    @SerialName("extra_price") val extraPrice: Long? = 0L
)

/**
 * DTO produk lengkap dari Laravel API.
 */
@Serializable
data class ProductDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("material") val material: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("base_price") val basePrice: Long,
    @Serializable(with = LenientBooleanSerializer::class)
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("materials") val materials: List<ProductMaterialDto> = emptyList(),
    @SerialName("colors") val colors: List<ProductColorDto> = emptyList(),
    @SerialName("sizes") val sizes: List<ProductSizeDto> = emptyList()
)
