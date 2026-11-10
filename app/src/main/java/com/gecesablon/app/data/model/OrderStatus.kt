package com.gecesablon.app.data.model

import androidx.annotation.StringRes
import com.gecesablon.app.R

/**
 * Model enum status pesanan resmi GeCe Screen Printing sesuai spesifikasi.
 * Urutan tahapan: DITERIMA -> SEDANG_DIPROSES -> CETAK_DTF -> PRESSING -> SIAP_DIAMBIL -> SELESAI, serta DIBATALKAN.
 */
enum class OrderStatus(@StringRes val labelResId: Int) {
    DITERIMA(R.string.status_diterima),
    SEDANG_DIPROSES(R.string.status_sedang_diproses),
    CETAK_DTF(R.string.status_cetak_dtf),
    PRESSING(R.string.status_pressing),
    SIAP_DIAMBIL(R.string.status_siap_diambil),
    SELESAI(R.string.status_selesai),
    DIBATALKAN(R.string.status_dibatalkan)
}
