package com.gecesablon.app.ui.staff

import androidx.annotation.StringRes
import com.gecesablon.app.R
import com.gecesablon.app.data.model.ProductionStage

/**
 * Pemetaan resource string untuk [ProductionStage] pada lapisan antarmuka pengguna (UI).
 */
@get:StringRes
val ProductionStage.titleRes: Int
    get() = when (this) {
        ProductionStage.DESAIN_MOCKUP -> R.string.stage_desain_mockup
        ProductionStage.PENGADAAN_BAJU -> R.string.stage_pengadaan_baju
        ProductionStage.CETAK_DTF_MATERIAL -> R.string.stage_cetak_dtf
        ProductionStage.PENYABLONAN -> R.string.stage_penyablonan
        ProductionStage.PEMERIKSAAN_QC -> R.string.stage_qc
    }

@get:StringRes
val ProductionStage.descRes: Int
    get() = when (this) {
        ProductionStage.DESAIN_MOCKUP -> R.string.stage_desain_mockup_desc
        ProductionStage.PENGADAAN_BAJU -> R.string.stage_pengadaan_baju_desc
        ProductionStage.CETAK_DTF_MATERIAL -> R.string.stage_cetak_dtf_desc
        ProductionStage.PENYABLONAN -> R.string.stage_penyablonan_desc
        ProductionStage.PEMERIKSAAN_QC -> R.string.stage_qc_desc
    }
