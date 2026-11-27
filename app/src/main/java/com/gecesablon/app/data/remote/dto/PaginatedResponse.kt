package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representasi pembungkus paginasi bawaan Laravel Paginator ($query->paginate()).
 *
 * @param data Daftar data untuk halaman saat ini
 * @param currentPage Nomor halaman saat ini
 * @param lastPage Nomor halaman terakhir
 * @param perPage Jumlah item per halaman
 * @param total Total keseluruhan item di seluruh halaman
 * @param from Indeks item awal pada halaman saat ini
 * @param to Indeks item akhir pada halaman saat ini
 */
@Serializable
data class PaginatedResponse<T>(
    @SerialName("data") val data: List<T> = emptyList(),
    @SerialName("current_page") val currentPage: Int? = null,
    @SerialName("last_page") val lastPage: Int? = null,
    @SerialName("per_page") val perPage: Int? = null,
    @SerialName("total") val total: Int? = null,
    @SerialName("from") val from: Int? = null,
    @SerialName("to") val to: Int? = null
)
