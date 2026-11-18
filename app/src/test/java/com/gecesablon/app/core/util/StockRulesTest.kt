package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.StockCategory
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.data.repository.FakeStockRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test untuk logika validasi persediaan dan status stok menipis pada [StockItem] dan [FakeStockRepository].
 */
class StockRulesTest {

    private lateinit var stockRepo: FakeStockRepository
    private val adminSession = Session(
        token = "token-admin",
        role = Role.ADMIN,
        name = "Admin Workshop",
        userId = "user-admin-001"
    )
    private val customerSession = Session(
        token = "token-customer",
        role = Role.CUSTOMER,
        name = "Pelanggan Demo",
        userId = "user-cust-001"
    )

    @Before
    fun setUp() {
        stockRepo = FakeStockRepository()
    }

    @Test
    fun testStokMenipisBilaJumlahKurangDariAtauSamaDenganBatasMinimum() {
        // Kasus 1: jumlah > batasMinimum -> false
        val itemAman = StockItem(
            nama = "Cotton Combed 24s",
            kategori = StockCategory.BAJU_POLOS,
            satuan = "pcs",
            jumlah = 50,
            batasMinimum = 20
        )
        assertFalse("Jumlah 50 dengan batas 20 tidak boleh dianggap menipis", itemAman.isMenipis)

        // Kasus 2: jumlah == batasMinimum -> true
        val itemPasBatas = itemAman.copy(jumlah = 20)
        assertTrue("Jumlah 20 sama dengan batas 20 harus berstatus menipis", itemPasBatas.isMenipis)

        // Kasus 3: jumlah < batasMinimum -> true
        val itemMenipis = itemAman.copy(jumlah = 5)
        assertTrue("Jumlah 5 di bawah batas 20 harus berstatus menipis", itemMenipis.isMenipis)

        // Kasus 4: jumlah 0 -> true
        val itemKosong = itemAman.copy(jumlah = 0)
        assertTrue("Jumlah 0 harus berstatus menipis", itemKosong.isMenipis)
    }

    @Test
    fun testAksesDitolakBukanStaf() = runBlocking {
        val res = stockRepo.getAllStock(customerSession).first()
        assertTrue("Pelanggan tidak boleh mengakses data inventaris stok", res is Resource.Error)

        val addRes = stockRepo.addStock(
            StockItem(nama = "Test", kategori = StockCategory.LAINNYA, satuan = "pcs", jumlah = 10, batasMinimum = 2),
            customerSession
        )
        assertTrue("Pelanggan tidak boleh menambah stok", addRes is Resource.Error)
    }

    @Test
    fun testUpdateQuantityTidakBolehNol() = runBlocking {
        val res = stockRepo.updateQuantity("stock-001", delta = 0, keterangan = "Coba 0", actor = adminSession)
        assertTrue("Delta 0 harus ditolak", res is Resource.Error)
    }

    @Test
    fun testUpdateQuantityTidakBolehMelebihiBatasMaksimal() = runBlocking {
        val res = stockRepo.updateQuantity("stock-001", delta = 150_000, keterangan = "Meluap", actor = adminSession)
        assertTrue("Delta di atas 100.000 harus ditolak", res is Resource.Error)
    }

    @Test
    fun testPenguranganStokMenghasilkanNegatifDitolak() = runBlocking {
        // stock-001 punya jumlah = 148
        val res = stockRepo.updateQuantity("stock-001", delta = -200, keterangan = "Kurang terlalu banyak", actor = adminSession)
        assertTrue("Pengurangan yang menghasilkan nilai negatif harus ditolak", res is Resource.Error)
    }

    @Test
    fun testPenambahanDanPenguranganStokValid() = runBlocking {
        // stock-002: awal 65 pcs
        val resTambah = stockRepo.updateQuantity("stock-002", delta = 15, keterangan = "Restock", actor = adminSession)
        assertTrue("Penambahan 15 harus sukses", resTambah is Resource.Success)
        assertEquals(80, (resTambah as Resource.Success).data.jumlah)

        val resKurang = stockRepo.updateQuantity("stock-002", delta = -10, keterangan = "Dipakai produksi", actor = adminSession)
        assertTrue("Pengurangan 10 harus sukses", resKurang is Resource.Success)
        assertEquals(70, (resKurang as Resource.Success).data.jumlah)
    }

    @Test
    fun testHapusItemStokValid() = runBlocking {
        val deleteRes = stockRepo.deleteStock("stock-006", adminSession)
        assertTrue("Hapus stok valid harus sukses", deleteRes is Resource.Success)

        val getRes = stockRepo.getStockById("stock-006", adminSession)
        assertTrue("Item yang sudah dihapus tidak boleh ditemukan", getRes is Resource.Error)
    }
}
