package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.PriceCalculator
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Order
import com.gecesablon.app.data.model.OrderItem
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductSize
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
 * Implementasi palsu in-memory untuk [OrderRepository] pada Tahap 4B.
 * Menyimulasikan latensi jaringan ±800 ms dan memelihara StateFlow pesanan.
 *
 * TODO: ganti ke Retrofit di Tahap 8
 */
class FakeOrderRepository : OrderRepository {

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
    private var orderCounter = 4 // Dimulai setelah 3 seed order awal

    // Seed 3 pesanan awal untuk pelanggan tetap: user-cust-001 (pelanggan@gece.test)
    private val ordersStateFlow = MutableStateFlow<List<Order>>(
        listOf(
            Order(
                id = "order-seed-001",
                kode = "GEC-20261103-001",
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
                        warnaNama = "Hitam Pekat",
                        ukuranLabel = "L",
                        jumlah = 24,
                        hargaSatuan = 65_000L
                    )
                )
            ),
            Order(
                id = "order-seed-002",
                kode = "GEC-20261102-014",
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
                )
            ),
            Order(
                id = "order-seed-003",
                kode = "GEC-20261028-005",
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
                )
            )
        )
    )

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
        userId: String, // TODO: di API asli diambil dari token
        product: Product,
        color: ProductColor,
        size: ProductSize,
        quantity: Int,
        catatan: String,
        uriDesain: String?
    ): Resource<Order> {
        delay(800) // Simulasi latensi jaringan ±800 ms

        // Hitung harga satuan dan total secara independen lewat PriceCalculator
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
            userId = userId,
            status = OrderStatus.DITERIMA,
            total = totalPrice,
            catatan = catatan.trim(),
            uriDesain = uriDesain,
            dibuatPada = System.currentTimeMillis(),
            items = listOf(orderItem)
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

        val updatedOrder = target.copy(status = OrderStatus.DIBATALKAN)
        currentList[index] = updatedOrder
        ordersStateFlow.value = currentList

        return Resource.Success(Unit)
    }
}
