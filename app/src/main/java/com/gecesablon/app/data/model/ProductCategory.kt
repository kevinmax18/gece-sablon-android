package com.gecesablon.app.data.model

import androidx.annotation.StringRes
import com.gecesablon.app.R

/**
 * Kategori produk sablon GeCe.
 */
enum class ProductCategory(@StringRes val titleRes: Int) {
    KAOS_CUSTOM(R.string.category_kaos_custom),
    KAOS_OVERSIZE(R.string.category_kaos_oversize),
    BAJU_POLO(R.string.category_baju_polo)
}
