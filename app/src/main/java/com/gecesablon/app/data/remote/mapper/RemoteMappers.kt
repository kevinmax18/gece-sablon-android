package com.gecesablon.app.data.remote.mapper

import com.gecesablon.app.BuildConfig
import com.gecesablon.app.data.model.ActivityLogEntry
import com.gecesablon.app.data.model.AdminAccount
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderCreator
import com.gecesablon.app.data.model.OrderItem
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductMaterial
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.ProductionStage
import com.gecesablon.app.data.model.ProductionState
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.StockCategory
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionSource
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.remote.dto.ActivityLogDto
import com.gecesablon.app.data.remote.dto.AdminAccountDto
import com.gecesablon.app.data.remote.dto.OrderDto
import com.gecesablon.app.data.remote.dto.OrderItemDto
import com.gecesablon.app.data.remote.dto.ProductColorDto
import com.gecesablon.app.data.remote.dto.ProductDto
import com.gecesablon.app.data.remote.dto.ProductMaterialDto
import com.gecesablon.app.data.remote.dto.ProductSizeDto
import com.gecesablon.app.data.remote.dto.ProductionStageDto
import com.gecesablon.app.data.remote.dto.StockDto
import com.gecesablon.app.data.remote.dto.TransactionDto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Parsing aman tanggal ISO dari Laravel (misal "2024-01-01T12:00:00.000000Z" atau "2024-01-01 12:00:00").
 * Menggunakan SimpleDateFormat UTC pada 19 karakter pertama (kompatibel minSdk 24 tanpa java.time).
 */
fun parseIsoDateToMillis(dateStr: String?): Long {
    if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
    return try {
        val clean = dateStr.replace(" ", "T")
        val prefix = if (clean.length >= 19) clean.substring(0, 19) else clean
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        sdf.parse(prefix)?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

/**
 * Parsing tanggal ringkas YYYY-MM-DD dari transaksi keuangan ke epoch millis (00:00:00 UTC).
 */
fun parseDateOnlyToMillis(dateStr: String?): Long {
    if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
    return try {
        val prefix = if (dateStr.length >= 10) dateStr.substring(0, 10) else dateStr
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        sdf.parse(prefix)?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

/**
 * Memformat epoch millis ke format string tanggal "yyyy-MM-dd" untuk parameter API Laravel.
 */
fun formatMillisToDateOnly(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(millis)
}

/**
 * Membentuk URL gambar penuh dari path relatif yang disimpan di backend.
 */
fun resolveImageUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    val base = if (BuildConfig.BASE_URL.endsWith("/")) BuildConfig.BASE_URL else "${BuildConfig.BASE_URL}/"
    val cleanPath = if (path.startsWith("/")) path.substring(1) else path
    return if (cleanPath.startsWith("storage/")) {
        "$base$cleanPath"
    } else {
        "${base}storage/$cleanPath"
    }
}

// =============================================================================
// Mapper Produk
// =============================================================================

fun ProductMaterialDto.toDomain(): ProductMaterial = ProductMaterial(
    id = this.id.toString(),
    nama = this.name,
    tambahanHarga = this.extraPrice ?: 0L,
    isDefault = this.isDefault ?: false
)

fun ProductColorDto.toDomain(): ProductColor = ProductColor(
    nama = this.name,
    hex = this.hex
)

fun ProductSizeDto.toDomain(): ProductSize = ProductSize(
    label = this.label,
    tambahanHarga = this.extraPrice ?: 0L
)

fun ProductDto.toDomain(): Product = Product(
    id = this.id.toString(),
    nama = this.name,
    categoryId = this.category,
    bahan = this.material ?: "",
    deskripsi = this.description ?: "",
    hargaDasar = this.basePrice,
    gambarUrl = resolveImageUrl(this.imagePath),
    daftarBahan = this.materials.map { it.toDomain() },
    daftarWarna = this.colors.map { it.toDomain() },
    daftarUkuran = this.sizes.map { it.toDomain() },
    aktif = this.isActive
)

// =============================================================================
// Mapper Pesanan (Order)
// =============================================================================

fun OrderItemDto.toDomain(): OrderItem = OrderItem(
    id = this.id?.toString() ?: "",
    productId = this.productId.toString(),
    namaProduk = this.productName,
    bahanNama = this.materialName ?: "",
    warnaNama = this.colorName,
    ukuranLabel = this.sizeLabel,
    jumlah = this.quantity,
    hargaSatuan = this.unitPrice
)

fun ActivityLogDto.toDomain(): ActivityLogEntry = ActivityLogEntry(
    id = this.id?.toString() ?: java.util.UUID.randomUUID().toString(),
    waktu = parseIsoDateToMillis(this.createdAt),
    aktor = this.actorName,
    aksi = this.action
)

fun mapStageDtoToEnum(stageStr: String): ProductionStage? = when (stageStr) {
    "DESAIN_MOCKUP" -> ProductionStage.DESAIN_MOCKUP
    "PENGADAAN_BAJU" -> ProductionStage.PENGADAAN_BAJU
    "CETAK_DTF_MATERIAL" -> ProductionStage.CETAK_DTF_MATERIAL
    "PENYABLONAN" -> ProductionStage.PENYABLONAN
    "PEMERIKSAAN_QC" -> ProductionStage.PEMERIKSAAN_QC
    else -> null
}

fun mapStageEnumToString(stage: ProductionStage): String = when (stage) {
    ProductionStage.DESAIN_MOCKUP -> "DESAIN_MOCKUP"
    ProductionStage.PENGADAAN_BAJU -> "PENGADAAN_BAJU"
    ProductionStage.CETAK_DTF_MATERIAL -> "CETAK_DTF_MATERIAL"
    ProductionStage.PENYABLONAN -> "PENYABLONAN"
    ProductionStage.PEMERIKSAAN_QC -> "PEMERIKSAAN_QC"
}

fun mapOrderStatus(statusStr: String): OrderStatus = when (statusStr) {
    "DITERIMA" -> OrderStatus.DITERIMA
    "SEDANG_DIPROSES" -> OrderStatus.SEDANG_DIPROSES
    "CETAK_DTF" -> OrderStatus.CETAK_DTF
    "PRESSING" -> OrderStatus.PRESSING
    "SIAP_DIAMBIL" -> OrderStatus.SIAP_DIAMBIL
    "SELESAI" -> OrderStatus.SELESAI
    "DIBATALKAN" -> OrderStatus.DIBATALKAN
    else -> OrderStatus.DITERIMA
}

fun OrderDto.toDomain(): Order {
    val tahapanSet = this.stages
        .filter { it.isDone }
        .mapNotNull { mapStageDtoToEnum(it.stage) }
        .toSet()

    val prodUpdatedAt = parseIsoDateToMillis(this.productionUpdatedAt ?: this.updatedAt)
    val prodState = ProductionState(
        tahapanSelesai = tahapanSet,
        bajuTiba = this.shirtArrived,
        dtfTiba = this.dtfArrived,
        siapDisablon = this.readyToPress,
        updatedAt = if (this.productionUpdatedAt != null) prodUpdatedAt else 0L,
        updatedBy = this.productionUpdatedBy?.toString() ?: ""
    )

    val creator = if (this.createdVia == "manual") OrderCreator.STAFF else OrderCreator.CUSTOMER
    val statusEnum = mapOrderStatus(this.status)
    val selesaiTimestamp = if (statusEnum == OrderStatus.SELESAI) {
        parseIsoDateToMillis(this.updatedAt)
    } else null

    return Order(
        id = this.id.toString(),
        kode = this.code,
        namaPelanggan = this.customerName ?: "Pelanggan GeCe",
        noHpPelanggan = this.customerPhone,
        dibuatOleh = creator,
        userId = this.userId?.toString(),
        status = statusEnum,
        total = this.totalPrice,
        catatan = this.notes ?: "",
        uriDesain = resolveImageUrl(this.designPath),
        dibuatPada = parseIsoDateToMillis(this.createdAt),
        selesaiPada = selesaiTimestamp,
        items = this.items.map { it.toDomain() },
        produksi = prodState,
        riwayat = this.logs.map { it.toDomain() }
    )
}

// =============================================================================
// Mapper Inventaris Stok
// =============================================================================

fun inferStockCategory(name: String): StockCategory {
    val lower = name.lowercase(Locale.ROOT)
    return when {
        lower.contains("kaos") || lower.contains("baju") || lower.contains("polo") -> StockCategory.BAJU_POLOS
        lower.contains("dtf") || lower.contains("film") || lower.contains("tinta") || lower.contains("powder") || lower.contains("bubuk") -> StockCategory.MATERIAL_DTF
        else -> StockCategory.LAINNYA
    }
}

fun StockDto.toDomain(): StockItem = StockItem(
    id = this.id.toString(),
    nama = this.name,
    kategori = inferStockCategory(this.name),
    satuan = this.unit,
    jumlah = this.quantity.toInt(),
    batasMinimum = (this.minQuantity ?: 0.0).toInt()
)

// =============================================================================
// Mapper Transaksi Keuangan
// =============================================================================

fun mapTransactionType(typeStr: String): TransactionType = when (typeStr.lowercase(Locale.ROOT)) {
    "income" -> TransactionType.PEMASUKAN
    "expense" -> TransactionType.PENGELUARAN
    else -> TransactionType.PEMASUKAN
}

fun mapTransactionCategory(categoryStr: String, type: TransactionType, source: TransactionSource): TransactionCategory {
    if (source == TransactionSource.PESANAN) {
        return TransactionCategory.PESANAN
    }
    val lower = categoryStr.lowercase(Locale.ROOT)
    return when {
        lower.contains("bahan baku") || lower.contains("kain") || lower.contains("kaos") -> TransactionCategory.BAHAN_BAKU
        lower.contains("dtf") || lower.contains("film") || lower.contains("powder") -> TransactionCategory.MATERIAL_DTF
        lower.contains("operasional") || lower.contains("listrik") || lower.contains("sewa") -> TransactionCategory.OPERASIONAL
        lower.contains("gaji") || lower.contains("upah") -> TransactionCategory.GAJI
        lower.contains("penjualan lain") -> TransactionCategory.PENJUALAN_LAIN
        lower.contains("pemasukan lainnya") || (type == TransactionType.PEMASUKAN) -> TransactionCategory.PEMASUKAN_LAINNYA
        else -> TransactionCategory.PENGELUARAN_LAINNYA
    }
}

fun TransactionDto.toDomain(): Transaction {
    val typeEnum = mapTransactionType(this.type)
    val srcEnum = if (this.source == "auto") TransactionSource.PESANAN else TransactionSource.MANUAL
    val catEnum = mapTransactionCategory(this.category, typeEnum, srcEnum)

    return Transaction(
        id = this.id.toString(),
        tipe = typeEnum,
        kategori = catEnum,
        nominal = this.amount,
        tanggal = parseDateOnlyToMillis(this.trxDate),
        keterangan = this.description ?: "",
        sumber = srcEnum,
        orderId = this.orderId?.toString(),
        dibuatOleh = if (srcEnum == TransactionSource.PESANAN) "Sistem (Pesanan)" else "Owner"
    )
}

// =============================================================================
// Mapper Akun Staf Admin
// =============================================================================

fun AdminAccountDto.toDomain(): AdminAccount = AdminAccount(
    id = this.id.toString(),
    name = this.name,
    email = this.email,
    phone = this.phone ?: "",
    role = Role.ADMIN,
    isActive = this.isActive,
    createdAt = parseIsoDateToMillis(this.createdAt)
)
