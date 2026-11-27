package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO transaksi keuangan dari Laravel API.
 */
@Serializable
data class TransactionDto(
    @SerialName("id") val id: Int,
    @SerialName("order_id") val orderId: Int? = null,
    @SerialName("source") val source: String, // "manual" or "auto"
    @SerialName("type") val type: String, // "income" or "expense"
    @SerialName("category") val category: String,
    @SerialName("amount") val amount: Long,
    @SerialName("description") val description: String? = null,
    @SerialName("trx_date") val trxDate: String, // YYYY-MM-DD
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * DTO request untuk membuat atau mengubah transaksi keuangan manual.
 */
@Serializable
data class TransactionRequestDto(
    @SerialName("type") val type: String, // "income" or "expense"
    @SerialName("category") val category: String,
    @SerialName("amount") val amount: Long,
    @SerialName("description") val description: String? = null,
    @SerialName("trx_date") val trxDate: String // YYYY-MM-DD
)

/**
 * DTO ringkasan keuangan dari GET api/finance/summary.
 */
@Serializable
data class FinanceSummaryDto(
    @SerialName("income") val income: Long,
    @SerialName("expense") val expense: Long,
    @SerialName("profit") val profit: Long
)
