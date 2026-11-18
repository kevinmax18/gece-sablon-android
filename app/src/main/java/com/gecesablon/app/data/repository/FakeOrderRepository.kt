package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.PriceCalculator
import com.gecesablon.app.core.util.ProductionRules
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.ActivityLogEntry
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderCreator
import com.gecesablon.app.data.model.OrderItem
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.ProductionStage
import com.gecesablon.app.data.model.ProductionState
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Implementasi palsu in-memory untuk [OrderRepository] pada Tahap 5A.
 * Menyimulasikan latensi jaringan ±800 ms dan memelihara StateFlow terpusat
 * sehingga perubahan staf langsung terlihat oleh pelanggan secara reaktif.
 *
 * TODO: ganti ke Retrofit di Tahap 8
 */
class FakeOrderRepository(
    private val productRepository: ProductRepository = FakeProductRepository()
) : OrderRepository {

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
    private var orderCounter = 11 // Dimulai setelah 10 pesanan seed awal

    // Seed 10 pesanan contoh dari 5 pelanggan mencakup semua variasi status
    private val ordersStateFlow = MutableStateFlow<List<Order>>(
        listOf(
            // Seed 001: Pelanggan Demo (user-cust-001) - SEDANG_DIPROSES
            Order(
                id = "order-seed-001",
                kode = "GEC-20261103-001",
                namaPelanggan = "Pelanggan Demo",
                noHpPelanggan = "081234567890",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-001",
                status = OrderStatus.SEDANG_DIPROSES,
                total = 1_560_000L,
                catatan = "Posisi sablon: Dada kiri ukuran A6 (10x10 cm) & punggung belakang ukuran A3 (30x42 cm). Warna aksen kuning menyala.",
                uriDesain = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=800&q=80",
                dibuatPada = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000), // 2 hari lalu
                items = listOf(
                    OrderItem(
                        id = "item-001",
                        productId = "prod-combed-24s",
                        namaProduk = "Kaos Custom Cotton Combed 24s",
                        warnaNama = "Hitam Solid",
                        ukuranLabel = "L",
                        jumlah = 24,
                        hargaSatuan = 65_000L
                    )
                ),
                produksi = ProductionState(
                    tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP),
                    bajuTiba = true,
                    dtfTiba = false,
                    siapDisablon = false,
                    updatedAt = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000),
                    updatedBy = "Admin Workshop"
                ),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000),
                        aktor = "Pelanggan Demo",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    ),
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000) + 3600000L,
                        aktor = "Admin Workshop",
                        aksi = "Pesanan diterima & mulai proses produksi"
                    ),
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000),
                        aktor = "Admin Workshop",
                        aksi = "Desain disetujui, kain tiba di workshop"
                    )
                )
            ),

            // Seed 002: Pelanggan Demo (user-cust-001) - SIAP_DIAMBIL
            Order(
                id = "order-seed-002",
                kode = "GEC-20261102-014",
                namaPelanggan = "Pelanggan Demo",
                noHpPelanggan = "081234567890",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-001",
                status = OrderStatus.SIAP_DIAMBIL,
                total = 2_700_000L,
                catatan = "Logo dada kiri DTF presisi. Diambil sore hari setelah jam kerja.",
                uriDesain = "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=800&q=80",
                dibuatPada = System.currentTimeMillis() - (4L * 24 * 60 * 60 * 1000), // 4 hari lalu
                items = listOf(
                    OrderItem(
                        id = "item-002",
                        productId = "prod-polo-pique",
                        namaProduk = "Baju Polo Pique Premium",
                        warnaNama = "Navy Blue",
                        ukuranLabel = "XL",
                        jumlah = 36,
                        hargaSatuan = 75_000L
                    )
                ),
                produksi = ProductionState(
                    tahapanSelesai = ProductionStage.orderedStages.toSet(),
                    bajuTiba = true,
                    dtfTiba = true,
                    siapDisablon = true,
                    updatedAt = System.currentTimeMillis() - (12 * 60 * 60 * 1000),
                    updatedBy = "Admin Workshop"
                ),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (4L * 24 * 60 * 60 * 1000),
                        aktor = "Pelanggan Demo",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    ),
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (12 * 60 * 60 * 1000),
                        aktor = "Admin Workshop",
                        aksi = "QC Lolos. Pesanan selesai diproduksi dan siap diambil di rak B2"
                    )
                )
            ),

            // Seed 003: Pelanggan Demo (user-cust-001) - SELESAI
            Order(
                id = "order-seed-003",
                kode = "GEC-20261028-005",
                namaPelanggan = "Pelanggan Demo",
                noHpPelanggan = "081234567890",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-001",
                status = OrderStatus.SELESAI,
                total = 650_000L,
                catatan = "Sablon depan tengah A4. Pembelian kaos sampel komunitas.",
                uriDesain = "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?auto=format&fit=crop&w=800&q=80",
                dibuatPada = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000), // 10 hari lalu
                items = listOf(
                    OrderItem(
                        id = "item-003",
                        productId = "prod-combed-24s",
                        namaProduk = "Kaos Custom Cotton Combed 24s",
                        warnaNama = "Putih Bersih",
                        ukuranLabel = "M",
                        jumlah = 10,
                        hargaSatuan = 65_000L
                    )
                ),
                produksi = ProductionState(
                    tahapanSelesai = ProductionStage.orderedStages.toSet(),
                    bajuTiba = true,
                    dtfTiba = true,
                    siapDisablon = true,
                    updatedAt = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
                    updatedBy = "Owner GeCe"
                ),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000),
                        aktor = "Pelanggan Demo",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    ),
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
                        aktor = "Owner GeCe",
                        aksi = "Pesanan telah diambil oleh pemesan"
                    )
                )
            ),

            // Seed 004: Siti Rahma (user-cust-002) - DITERIMA
            Order(
                id = "order-seed-004",
                kode = "GEC-20261103-008",
                namaPelanggan = "Siti Rahma",
                noHpPelanggan = "081200000002",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-002",
                status = OrderStatus.DITERIMA,
                total = 780_000L,
                catatan = "Sablon A4 satu sisi di bagian depan. Warna sablon putih dan abu muda.",
                uriDesain = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=800&q=80",
                dibuatPada = System.currentTimeMillis() - (3 * 60 * 60 * 1000), // 3 jam lalu
                items = listOf(
                    OrderItem(
                        id = "item-004",
                        productId = "prod-combed-24s",
                        namaProduk = "Kaos Custom Cotton Combed 24s",
                        warnaNama = "Broken White",
                        ukuranLabel = "S",
                        jumlah = 12,
                        hargaSatuan = 65_000L
                    )
                ),
                produksi = ProductionState(),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (3 * 60 * 60 * 1000),
                        aktor = "Siti Rahma",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    )
                )
            ),

            // Seed 005: Komunitas Lari Urban (user-cust-003) - SEDANG_DIPROSES (hanya DESAIN_MOCKUP selesai, bajuTiba=false)
            Order(
                id = "order-seed-005",
                kode = "GEC-20261103-009",
                namaPelanggan = "Komunitas Lari Urban",
                noHpPelanggan = "081200000003",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-003",
                status = OrderStatus.SEDANG_DIPROSES,
                total = 3_250_000L,
                catatan = "Logo pelari di dada kanan dan nomor punggung A4 belakang.",
                uriDesain = "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?auto=format&fit=crop&w=800&q=80",
                dibuatPada = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000),
                items = listOf(
                    OrderItem(
                        id = "item-005",
                        productId = "prod-combed-24s",
                        namaProduk = "Kaos Custom Cotton Combed 24s",
                        warnaNama = "Hitam Solid",
                        ukuranLabel = "L",
                        jumlah = 50,
                        hargaSatuan = 65_000L
                    )
                ),
                produksi = ProductionState(
                    tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP),
                    bajuTiba = false,
                    dtfTiba = false,
                    siapDisablon = false,
                    updatedAt = System.currentTimeMillis() - (18 * 60 * 60 * 1000),
                    updatedBy = "Admin Workshop"
                ),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000),
                        aktor = "Komunitas Lari Urban",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    ),
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (18 * 60 * 60 * 1000),
                        aktor = "Admin Workshop",
                        aksi = "Desain mockup telah diverifikasi tim workshop"
                    )
                )
            ),

            // Seed 006: Komunitas Lari Urban (user-cust-003) - CETAK_DTF (tahap 1 & 2 selesai, bajuTiba=true)
            Order(
                id = "order-seed-006",
                kode = "GEC-20261102-007",
                namaPelanggan = "Komunitas Lari Urban",
                noHpPelanggan = "081200000003",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-003",
                status = OrderStatus.CETAK_DTF,
                total = 1_875_000L,
                catatan = "Jersey lari warna maroon, sablon DTF anti-pecah tahan keringat.",
                uriDesain = null,
                dibuatPada = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000),
                items = listOf(
                    OrderItem(
                        id = "item-006",
                        productId = "prod-polo-pique",
                        namaProduk = "Baju Polo Pique Premium",
                        warnaNama = "Maroon Red",
                        ukuranLabel = "M",
                        jumlah = 25,
                        hargaSatuan = 75_000L
                    )
                ),
                produksi = ProductionState(
                    tahapanSelesai = setOf(ProductionStage.DESAIN_MOCKUP, ProductionStage.PENGADAAN_BAJU),
                    bajuTiba = true,
                    dtfTiba = false,
                    siapDisablon = false,
                    updatedAt = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000),
                    updatedBy = "Admin Workshop"
                ),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000),
                        aktor = "Komunitas Lari Urban",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    ),
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000),
                        aktor = "Admin Workshop",
                        aksi = "Baju tiba di workshop, file dikirim ke vendor cetak DTF"
                    )
                )
            ),

            // Seed 007: Arya Perkasa Studio (user-cust-004) - PRESSING (tahap 1-3 selesai, bajuTiba=true, dtfTiba=true, siapDisablon=false)
            Order(
                id = "order-seed-007",
                kode = "GEC-20261101-011",
                namaPelanggan = "Arya Perkasa Studio",
                noHpPelanggan = "081200000004",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-004",
                status = OrderStatus.PRESSING,
                total = 2_600_000L,
                catatan = "Kaos crew studio, sablon oversize belakang A3+ dan detail logo depan dada.",
                uriDesain = "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=800&q=80",
                dibuatPada = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000),
                items = listOf(
                    OrderItem(
                        id = "item-007",
                        productId = "prod-combed-24s",
                        namaProduk = "Kaos Custom Cotton Combed 24s",
                        warnaNama = "Forest Green",
                        ukuranLabel = "XL",
                        jumlah = 40,
                        hargaSatuan = 65_000L
                    )
                ),
                produksi = ProductionState(
                    tahapanSelesai = setOf(
                        ProductionStage.DESAIN_MOCKUP,
                        ProductionStage.PENGADAAN_BAJU,
                        ProductionStage.CETAK_DTF_MATERIAL
                    ),
                    bajuTiba = true,
                    dtfTiba = true,
                    siapDisablon = false,
                    updatedAt = System.currentTimeMillis() - (8 * 60 * 60 * 1000),
                    updatedBy = "Admin Workshop"
                ),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000),
                        aktor = "Arya Perkasa Studio",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    ),
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (8 * 60 * 60 * 1000),
                        aktor = "Admin Workshop",
                        aksi = "Film DTF tiba di workshop, antre meja heat press"
                    )
                )
            ),

            // Seed 008: Dewi Lestari (user-cust-005) - PRESSING (tahap 1-4 selesai, bajuTiba=true, dtfTiba=true, siapDisablon=true, aktif QC)
            Order(
                id = "order-seed-008",
                kode = "GEC-20261101-003",
                namaPelanggan = "Dewi Lestari",
                noHpPelanggan = "081200000005",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-005",
                status = OrderStatus.PRESSING,
                total = 975_000L,
                catatan = "Warna sablon pastel, pastikan curing merata dan tekstur lembut.",
                uriDesain = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=800&q=80",
                dibuatPada = System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000),
                items = listOf(
                    OrderItem(
                        id = "item-008",
                        productId = "prod-combed-24s",
                        namaProduk = "Kaos Custom Cotton Combed 24s",
                        warnaNama = "Baby Blue",
                        ukuranLabel = "M",
                        jumlah = 15,
                        hargaSatuan = 65_000L
                    )
                ),
                produksi = ProductionState(
                    tahapanSelesai = setOf(
                        ProductionStage.DESAIN_MOCKUP,
                        ProductionStage.PENGADAAN_BAJU,
                        ProductionStage.CETAK_DTF_MATERIAL,
                        ProductionStage.PENYABLONAN
                    ),
                    bajuTiba = true,
                    dtfTiba = true,
                    siapDisablon = true,
                    updatedAt = System.currentTimeMillis() - (4 * 60 * 60 * 1000),
                    updatedBy = "Admin Workshop"
                ),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000),
                        aktor = "Dewi Lestari",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    ),
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (4 * 60 * 60 * 1000),
                        aktor = "Admin Workshop",
                        aksi = "Proses heat press selesai, masuk antrean pemeriksaan QC"
                    )
                )
            ),

            // Seed 009: Dewi Lestari (user-cust-005) - DIBATALKAN
            Order(
                id = "order-seed-009",
                kode = "GEC-20261030-019",
                namaPelanggan = "Dewi Lestari",
                noHpPelanggan = "081200000005",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-005",
                status = OrderStatus.DIBATALKAN,
                total = 520_000L,
                catatan = "Pembatalan karena perubahan konsep desain apparel.",
                uriDesain = null,
                dibuatPada = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000),
                items = listOf(
                    OrderItem(
                        id = "item-009",
                        productId = "prod-combed-24s",
                        namaProduk = "Kaos Custom Cotton Combed 24s",
                        warnaNama = "Hitam Solid",
                        ukuranLabel = "S",
                        jumlah = 8,
                        hargaSatuan = 65_000L
                    )
                ),
                produksi = ProductionState(),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000),
                        aktor = "Dewi Lestari",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    ),
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000) + 1800000L,
                        aktor = "Dewi Lestari",
                        aksi = "Pesanan dibatalkan oleh pemesan"
                    )
                )
            ),

            // Seed 010: Pelanggan Demo (user-cust-001) - DITERIMA (untuk uji pembatalan staf/pelanggan)
            Order(
                id = "order-seed-010",
                kode = "GEC-20261103-010",
                namaPelanggan = "Pelanggan Demo",
                noHpPelanggan = "081200000010",
                dibuatOleh = OrderCreator.CUSTOMER,
                userId = "user-cust-001",
                status = OrderStatus.DITERIMA,
                total = 325_000L,
                catatan = "Coba sablon logo kecil di dada kiri.",
                uriDesain = null,
                dibuatPada = System.currentTimeMillis() - (1 * 60 * 60 * 1000), // 1 jam lalu
                items = listOf(
                    OrderItem(
                        id = "item-010",
                        productId = "prod-kaos-custom",
                        namaProduk = "Kaos Custom Cotton Combed",
                        warnaNama = "Hitam Pekat",
                        ukuranLabel = "L",
                        jumlah = 5,
                        hargaSatuan = 65_000L
                    )
                ),
                produksi = ProductionState(),
                riwayat = listOf(
                    ActivityLogEntry(
                        waktu = System.currentTimeMillis() - (1 * 60 * 60 * 1000),
                        aktor = "Pelanggan Demo",
                        aksi = "Pesanan dibuat melalui aplikasi"
                    )
                )
            )
        )
    )

    // =========================================================================
    // Implementasi Fungsi Pelanggan
    // =========================================================================

    override fun getOrdersByUserId(userId: String): Flow<Resource<List<Order>>> {
        return ordersStateFlow.asStateFlow().map { allOrders ->
            val userOrders = allOrders
                .filter { it.userId == userId }
                .sortedByDescending { it.dibuatPada }
            Resource.Success(userOrders)
        }
    }

    override suspend fun getOrderById(orderId: String): Resource<Order> {
        delay(300)
        val order = ordersStateFlow.value.find { it.id == orderId }
            ?: return Resource.Error("Pesanan tidak ditemukan.")
        return Resource.Success(order)
    }

    override suspend fun createOrder(
        userId: String,
        product: Product,
        color: ProductColor,
        size: ProductSize,
        quantity: Int,
        catatan: String,
        uriDesain: String?,
        namaPelanggan: String,
        noHpPelanggan: String?
    ): Resource<Order> {
        delay(800)

        val unitPrice = PriceCalculator.calculateUnitPrice(product.hargaDasar, size.tambahanHarga)
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, quantity)

        val dateString = dateFormat.format(Date())
        val orderCode = String.format(Locale.US, "GEC-%s-%03d", dateString, orderCounter++)

        val orderItem = OrderItem(
            id = UUID.randomUUID().toString(),
            productId = product.id,
            namaProduk = product.nama,
            warnaNama = color.nama,
            ukuranLabel = size.label,
            jumlah = quantity,
            hargaSatuan = unitPrice
        )

        val newOrder = Order(
            id = UUID.randomUUID().toString(),
            kode = orderCode,
            namaPelanggan = namaPelanggan.ifBlank { "Pelanggan GeCe" },
            noHpPelanggan = noHpPelanggan?.takeIf { it.isNotBlank() },
            dibuatOleh = OrderCreator.CUSTOMER,
            userId = userId,
            status = OrderStatus.DITERIMA,
            total = totalPrice,
            catatan = catatan.trim(),
            uriDesain = uriDesain,
            dibuatPada = System.currentTimeMillis(),
            items = listOf(orderItem),
            produksi = ProductionState(),
            riwayat = listOf(
                ActivityLogEntry(
                    waktu = System.currentTimeMillis(),
                    aktor = namaPelanggan.ifBlank { "Pelanggan GeCe" },
                    aksi = "Pesanan dibuat melalui aplikasi"
                )
            )
        )

        val currentList = ordersStateFlow.value.toMutableList()
        currentList.add(0, newOrder)
        ordersStateFlow.value = currentList

        return Resource.Success(newOrder)
    }

    override suspend fun cancelOrder(orderId: String, userId: String): Resource<Unit> {
        delay(500)
        val currentList = ordersStateFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index == -1) {
            return Resource.Error("Pesanan tidak ditemukan.")
        }

        val target = currentList[index]
        if (target.userId != userId) {
            return Resource.Error("Anda tidak memiliki izin membatalkan pesanan ini.")
        }

        if (target.status != OrderStatus.DITERIMA) {
            return Resource.Error("Hanya pesanan berstatus Diterima yang dapat dibatalkan.")
        }

        val newLog = ActivityLogEntry(
            waktu = System.currentTimeMillis(),
            aktor = target.namaPelanggan,
            aksi = "Pesanan dibatalkan oleh pelanggan"
        )
        val updatedOrder = target.copy(
            status = OrderStatus.DIBATALKAN,
            riwayat = target.riwayat + newLog
        )
        currentList[index] = updatedOrder
        ordersStateFlow.value = currentList

        return Resource.Success(Unit)
    }

    // =========================================================================
    // Implementasi Fungsi Staf Workshop (Admin & Owner)
    // =========================================================================

    override fun getAllOrders(actor: Session): Flow<Resource<List<Order>>> {
        return ordersStateFlow.asStateFlow().map { allOrders ->
            // TODO: di API asli aktor dari token
            if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
                Resource.Error("Akses ditolak. Fitur ini hanya untuk staf workshop (Admin atau Owner).")
            } else {
                val sorted = allOrders.sortedByDescending { it.dibuatPada }
                Resource.Success(sorted)
            }
        }
    }

    override suspend fun getStaffOrderDetail(orderId: String, actor: Session): Resource<Order> {
        delay(300)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk staf workshop (Admin atau Owner).")
        }

        val order = ordersStateFlow.value.find { it.id == orderId }
            ?: return Resource.Error("Pesanan tidak ditemukan.")

        return Resource.Success(order)
    }

    override suspend fun acceptAndStartProcess(orderId: String, actor: Session): Resource<Order> {
        delay(500)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk staf workshop (Admin atau Owner).")
        }

        val currentList = ordersStateFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index == -1) {
            return Resource.Error("Pesanan tidak ditemukan.")
        }

        val target = currentList[index]
        if (target.status == OrderStatus.DIBATALKAN) {
            return Resource.Error("Pesanan telah dibatalkan dan tidak dapat diproses lagi.")
        }
        if (target.status == OrderStatus.SELESAI) {
            return Resource.Error("Pesanan telah selesai dan tidak dapat diubah lagi.")
        }
        if (target.status != OrderStatus.DITERIMA) {
            return Resource.Error("Hanya pesanan berstatus Diterima yang dapat dimulai prosesnya.")
        }

        val newLog = ActivityLogEntry(
            waktu = System.currentTimeMillis(),
            aktor = actor.name,
            aksi = "Pesanan diterima & mulai proses produksi"
        )
        val updatedOrder = target.copy(
            status = OrderStatus.SEDANG_DIPROSES,
            produksi = target.produksi.copy(
                updatedAt = System.currentTimeMillis(),
                updatedBy = actor.name
            ),
            riwayat = target.riwayat + newLog
        )

        currentList[index] = updatedOrder
        ordersStateFlow.value = currentList

        return Resource.Success(updatedOrder)
    }

    override suspend fun updateProductionState(
        orderId: String,
        newState: ProductionState,
        actor: Session
    ): Resource<Order> {
        delay(500)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk staf workshop (Admin atau Owner).")
        }

        val currentList = ordersStateFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index == -1) {
            return Resource.Error("Pesanan tidak ditemukan.")
        }

        val target = currentList[index]

        // Periksa status pesanan (DITERIMA, DIBATALKAN, SELESAI tidak boleh dimanipulasi tahapannya)
        val checkStatus = ProductionRules.bolehUbahProduksi(target.status)
        if (!checkStatus.diizinkan) {
            return Resource.Error(checkStatus.pesan ?: "Pesanan tidak dapat diubah.")
        }

        // Validasi aturan produksi
        val validation = ProductionRules.validasiKeadaan(newState)
        if (!validation.diizinkan) {
            return Resource.Error(validation.pesan ?: "Kondisi tahapan produksi tidak valid.")
        }

        // Turunkan status pelanggan secara otomatis
        val newStatus = ProductionRules.turunkanStatus(target.status, newState)

        val newLog = ActivityLogEntry(
            waktu = System.currentTimeMillis(),
            aktor = actor.name,
            aksi = "Pembaruan tahapan produksi (${newState.tahapanSelesai.size}/5 selesai)"
        )

        val updatedOrder = target.copy(
            status = newStatus,
            produksi = newState.copy(
                updatedAt = System.currentTimeMillis(),
                updatedBy = actor.name
            ),
            riwayat = target.riwayat + newLog
        )

        currentList[index] = updatedOrder
        ordersStateFlow.value = currentList

        return Resource.Success(updatedOrder)
    }

    override suspend fun markOrderCompleted(orderId: String, actor: Session): Resource<Order> {
        delay(500)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk staf workshop (Admin atau Owner).")
        }

        val currentList = ordersStateFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == orderId }
        if (index == -1) {
            return Resource.Error("Pesanan tidak ditemukan.")
        }

        val target = currentList[index]
        if (target.status == OrderStatus.DIBATALKAN) {
            return Resource.Error("Pesanan telah dibatalkan dan tidak dapat diselesaikan.")
        }
        if (target.status == OrderStatus.SELESAI) {
            return Resource.Error("Pesanan telah berstatus Selesai.")
        }
        if (target.status != OrderStatus.SIAP_DIAMBIL) {
            return Resource.Error("Hanya pesanan berstatus Siap Diambil yang dapat ditandai selesai.")
        }

        val newLog = ActivityLogEntry(
            waktu = System.currentTimeMillis(),
            aktor = "${actor.name} (${if (actor.role == Role.ADMIN) "Admin" else "Owner"})",
            aksi = "Pesanan telah diambil oleh pemesan (Selesai)"
        )
        val updatedOrder = target.copy(
            status = OrderStatus.SELESAI,
            produksi = target.produksi.copy(
                updatedAt = System.currentTimeMillis(),
                updatedBy = actor.name
            ),
            riwayat = target.riwayat + newLog
        )

        currentList[index] = updatedOrder
        ordersStateFlow.value = currentList

        return Resource.Success(updatedOrder)
    }

    override suspend fun createManualOrder(
        actor: Session,
        namaPelanggan: String,
        noHpPelanggan: String,
        productId: String,
        colorName: String,
        sizeLabel: String,
        quantity: Int,
        catatan: String,
        uriDesain: String?
    ): Resource<Order> {
        delay(600)
        // TODO: di API asli aktor dari token
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak. Fitur ini hanya untuk staf workshop (Admin atau Owner).")
        }

        if (namaPelanggan.isBlank()) {
            return Resource.Error("Nama pelanggan wajib diisi.")
        }
        val cleanPhone = noHpPelanggan.trim()
        val isValidPhone = (cleanPhone.startsWith("08") || cleanPhone.startsWith("628")) &&
                cleanPhone.length in 10..14 && cleanPhone.all { it.isDigit() }
        if (!isValidPhone) {
            return Resource.Error("Nomor HP tidak valid. Gunakan format Indonesia (misal: 08123456789).")
        }
        if (quantity !in 1..999) {
            return Resource.Error("Jumlah pesanan harus antara 1 sampai 999 pcs.")
        }

        val productRes = productRepository.getProductById(productId)
        if (productRes !is Resource.Success) {
            return Resource.Error("Produk tidak ditemukan.")
        }
        val product = productRes.data

        val color = product.daftarWarna.find { it.nama.equals(colorName, ignoreCase = true) }
            ?: return Resource.Error("Varian warna tidak ditemukan pada produk ini.")

        val size = product.daftarUkuran.find { it.label.equals(sizeLabel, ignoreCase = true) }
            ?: return Resource.Error("Varian ukuran tidak ditemukan pada produk ini.")

        val unitPrice = PriceCalculator.calculateUnitPrice(product.hargaDasar, size.tambahanHarga)
        val totalPrice = PriceCalculator.calculateTotalPrice(unitPrice, quantity)

        val dateString = dateFormat.format(Date())
        val orderCode = String.format(Locale.US, "GEC-%s-%03d", dateString, orderCounter++)

        val orderItem = OrderItem(
            id = UUID.randomUUID().toString(),
            productId = product.id,
            namaProduk = product.nama,
            warnaNama = color.nama,
            ukuranLabel = size.label,
            jumlah = quantity,
            hargaSatuan = unitPrice
        )

        val peranLabel = if (actor.role == Role.ADMIN) "Admin" else "Owner"
        val initialLog = ActivityLogEntry(
            waktu = System.currentTimeMillis(),
            aktor = "${actor.name} ($peranLabel)",
            aksi = "Pesanan manual dicatat oleh staf"
        )

        val manualOrder = Order(
            id = UUID.randomUUID().toString(),
            kode = orderCode,
            namaPelanggan = namaPelanggan.trim(),
            noHpPelanggan = cleanPhone,
            dibuatOleh = OrderCreator.STAFF,
            userId = null,
            status = OrderStatus.DITERIMA,
            total = totalPrice,
            catatan = catatan.trim(),
            uriDesain = uriDesain,
            dibuatPada = System.currentTimeMillis(),
            items = listOf(orderItem),
            produksi = ProductionState(),
            riwayat = listOf(initialLog)
        )

        val currentList = ordersStateFlow.value.toMutableList()
        currentList.add(0, manualOrder)
        ordersStateFlow.value = currentList

        return Resource.Success(manualOrder)
    }
}
