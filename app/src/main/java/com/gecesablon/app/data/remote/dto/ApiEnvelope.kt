package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pembungkus generik respons JSON standar dari REST API Laravel GeCe Sablon.
 *
 * @param success Menandakan keberhasilan operasi di backend
 * @param message Pesan informatif atau notifikasi dari server (opsional)
 * @param data Muatan utama data respons (opsional/nullable)
 */
@Serializable
data class ApiEnvelope<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null
)
