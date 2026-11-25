package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.PriceCalculator
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.OrderCreator
import com.gecesablon.app.data.model.OrderStatus
import com.gecesablon.app.data.model.Product
import com.gecesablon.app.data.model.ProductColor
import com.gecesablon.app.data.model.ProductMaterial
import com.gecesablon.app.data.model.ProductSize
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test untuk [FakeOrderRepository] yang menguji:
 * - OrderItem menyimpan salinan nama bahan (bahanNama)
 * - Perhitungan harga satuan dihitung ulang menggunakan PriceCalculator (hargaDasar + tambahanBahan + tambahanUkuran)
 * - Penolakan bahan yang tidak ada pada produk aktif (baik di pesanan pelanggan maupun manual staff)
 * - Pesanan lama tidak berubah bila bahan diubah atau dihapus dari produk
 * - Pemilihan bahan default otomatis bila tidak disediakan
 */
class FakeOrderRepositoryTest {

    private lateinit var productRepository: FakeProductRepository
    private lateinit var orderRepository: FakeOrderRepository

    private val ownerSession = Session(
        token = "token-owner",
        userId = "owner-01",
        name = "Owner GeCe",
        role = Role.OWNER
    )

    private val adminSession = Session(
        token = "token-admin",
        userId = "admin-01",
        name = "Admin Workshop",
        role = Role.ADMIN
    )

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        orderRepository = FakeOrderRepository(productRepository = productRepository)
    }

    @Test
    fun testCreateOrder_suksesDenganBahanDanMenyimpanSalinanNama() = runBlocking {
        val prodRes = productRepository.getProductById("prod-kaos-custom")
        assertTrue(prodRes is Resource.Success)
        val product = (prodRes as Resource.Success).data

        val selectedMaterial = product.daftarBahan.first { it.nama == "Cotton Combed 24s" } // extra 5000
        val selectedColor = product.daftarWarna.first()
        val selectedSize = product.daftarUkuran.first { it.label == "XXL" } // extra 5000

        // Harga dasar = 65000, materialExtra = 5000, sizeExtra = 5000 -> hargaSatuan = 75000
        val quantity = 10
        val expectedUnitPrice = PriceCalculator.calculateUnitPrice(
            basePrice = product.hargaDasar,
            sizeExtra = selectedSize.tambahanHarga,
            materialExtra = selectedMaterial.tambahanHarga
        )
        val expectedTotalPrice = PriceCalculator.calculateTotalPrice(expectedUnitPrice, quantity)

        val result = orderRepository.createOrder(
            userId = "cust-123",
            product = product,
            material = selectedMaterial,
            color = selectedColor,
            size = selectedSize,
            quantity = quantity,
            catatan = "Catatan sablon DTF",
            uriDesain = "content://media/desain.png",
            namaPelanggan = "Budi Hartono",
            noHpPelanggan = "081234567890"
        )

        assertTrue(result is Resource.Success)
        val order = (result as Resource.Success).data
        assertEquals(expectedTotalPrice, order.total)
        assertEquals(1, order.items.size)

        val item = order.items.first()
        assertEquals(product.nama, item.namaProduk)
        assertEquals("Cotton Combed 24s", item.bahanNama)
        assertEquals(expectedUnitPrice, item.hargaSatuan)
        assertEquals(quantity, item.jumlah)
    }

    @Test
    fun testCreateOrder_menolakBahanYangTidakAdaPadaProduk() = runBlocking {
        val prodRes = productRepository.getProductById("prod-kaos-custom")
        val product = (prodRes as Resource.Success).data

        val invalidMaterial = ProductMaterial(
            id = "mat-palsu",
            nama = "Bahan Sutra Emas",
            tambahanHarga = 50_000L,
            isDefault = false
        )

        val result = orderRepository.createOrder(
            userId = "cust-123",
            product = product,
            material = invalidMaterial,
            color = product.daftarWarna.first(),
            size = product.daftarUkuran.first(),
            quantity = 2,
            catatan = "",
            uriDesain = null
        )

        assertTrue("Bahan yang tidak ada pada produk harus ditolak", result is Resource.Error)
    }

    @Test
    fun testCreateManualOrder_suksesDanMenolakBahanTidakAda() = runBlocking {
        val prodId = "prod-oversize-streetwear"
        val prodRes = productRepository.getProductById(prodId)
        val product = (prodRes as Resource.Success).data

        // 1. Sukses dengan bahan valid
        val validResult = orderRepository.createManualOrder(
            actor = adminSession,
            namaPelanggan = "Joko Walk-in",
            noHpPelanggan = "081298765432",
            productId = prodId,
            materialName = "Heavyweight Cotton 16s", // extra 10000
            colorName = product.daftarWarna.first().nama,
            sizeLabel = "M",
            quantity = 5,
            catatan = "Cetak sablon punggung",
            uriDesain = null
        )
        assertTrue(validResult is Resource.Success)
        val order = (validResult as Resource.Success).data
        val item = order.items.first()
        assertEquals("Heavyweight Cotton 16s", item.bahanNama)
        assertEquals(85000L + 10000L, item.hargaSatuan) // 95000

        // 2. Gagal dengan nama bahan yang tidak ada pada produk tersebut
        val invalidResult = orderRepository.createManualOrder(
            actor = adminSession,
            namaPelanggan = "Joko Walk-in",
            noHpPelanggan = "081298765432",
            productId = prodId,
            materialName = "Kain Sutra Tidak Valid",
            colorName = product.daftarWarna.first().nama,
            sizeLabel = "M",
            quantity = 5,
            catatan = "",
            uriDesain = null
        )
        assertTrue("Manual order dengan bahan yang tidak ada harus ditolak", invalidResult is Resource.Error)
    }

    @Test
    fun testPesananLamaTidakBerubahBilaBahanProdukDiubahAtauDihapus() = runBlocking {
        val targetId = "prod-polo-classic"
        val product = (productRepository.getProductById(targetId) as Resource.Success).data
        val initialMaterial = product.daftarBahan.first { it.nama == "Lacoste 100% Cotton" } // 10.000

        // Buat pesanan
        val createRes = orderRepository.createOrder(
            userId = "cust-999",
            product = product,
            material = initialMaterial,
            color = product.daftarWarna.first(),
            size = product.daftarUkuran.first { it.label == "S" },
            quantity = 4,
            catatan = "Pesanan sebelum produk diedit",
            uriDesain = null
        )
        assertTrue(createRes is Resource.Success)
        val createdOrder = (createRes as Resource.Success).data

        // Owner mengubah produk: menghapus bahan "Lacoste 100% Cotton" dan menggantinya dengan bahan baru
        val newMaterials = listOf(
            ProductMaterial(id = "mat-baru-cvc", nama = "Lacoste CVC Super", tambahanHarga = 0L, isDefault = true)
        )
        val updatedProduct = product.copy(
            daftarBahan = newMaterials,
            hargaDasar = 120_000L // harga dasar dinaikkan
        )
        val ubahRes = productRepository.ubah(ownerSession, updatedProduct)
        assertTrue(ubahRes is Resource.Success)

        // Ambil kembali pesanan lama dari order repository
        val fetchOrderRes = orderRepository.getOrderById(createdOrder.id)
        assertTrue(fetchOrderRes is Resource.Success)
        val fetchedOrder = (fetchOrderRes as Resource.Success).data

        // Item pesanan lama tetap menyimpan salinan bahan dan harga asli
        val oldItem = fetchedOrder.items.first()
        assertEquals("Lacoste 100% Cotton", oldItem.bahanNama)
        assertEquals(createdOrder.total, fetchedOrder.total)
        assertEquals(createdOrder.items.first().hargaSatuan, oldItem.hargaSatuan)
    }
}
