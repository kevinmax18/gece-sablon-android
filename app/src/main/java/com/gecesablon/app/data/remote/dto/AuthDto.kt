package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) untuk respons/request autentikasi pengguna.
 */
@Serializable
data class AuthDto(
    val token: String? = null
)
