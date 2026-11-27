package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO akun admin dari Laravel API (GET api/owner/admins).
 */
@Serializable
data class AdminAccountDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String? = null,
    @SerialName("role") val role: String = "admin",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * DTO request untuk membuat akun admin baru (POST api/owner/admins).
 */
@Serializable
data class CreateAdminAccountRequestDto(
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String,
    @SerialName("password") val password: String
)

/**
 * DTO request untuk memperbarui akun admin (PUT api/owner/admins/{id}).
 */
@Serializable
data class UpdateAdminAccountRequestDto(
    @SerialName("name") val name: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("password") val password: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null
)
