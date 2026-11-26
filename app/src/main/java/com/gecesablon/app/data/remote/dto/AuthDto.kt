package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO untuk login ke API Laravel.
 * Field diberi nama "login" (sesuai validasi di AuthController Laravel).
 */
@Serializable
data class LoginRequestDto(
    @SerialName("login") val login: String,
    @SerialName("password") val password: String
)

/**
 * Request DTO untuk registrasi akun pelanggan baru di API Laravel.
 */
@Serializable
data class RegisterRequestDto(
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String,
    @SerialName("password") val password: String
)

/**
 * DTO data user yang dikembalikan oleh backend Laravel.
 */
@Serializable
data class UserDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("role") val role: String,
    @SerialName("is_active") val isActive: Boolean? = true
)

/**
 * DTO muatan data respons login dan register (token + user).
 */
@Serializable
data class AuthResponseDataDto(
    @SerialName("token") val token: String,
    @SerialName("user") val user: UserDto
)

/**
 * DTO stub autentikasi bawaan proyek.
 */
@Serializable
data class AuthDto(
    @SerialName("token") val token: String? = null
)
