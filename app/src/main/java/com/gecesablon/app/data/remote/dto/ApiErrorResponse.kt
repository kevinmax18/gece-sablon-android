package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Format payload error dari Laravel (termasuk validasi 422 Unprocessable Entity).
 *
 * @param message Pesan utama error dari Laravel
 * @param errors Rincian pesan error per field bila terjadi kegagalan validasi form
 */
@Serializable
data class ApiErrorResponse(
    @SerialName("message") val message: String? = null,
    @SerialName("errors") val errors: Map<String, List<String>>? = null
)
