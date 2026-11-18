package com.gecesablon.app.data.repository

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.StockCategory
import com.gecesablon.app.data.model.StockItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlin.math.abs

/**
 * Implementasi in-memory untuk [StockRepository] pada Tahap 5B.
 * Menyimulasikan latensi jaringan ±400 ms dan memelihara StateFlow terpusat.
 *
 * TODO: ganti ke Retrofit di Tahap 8
 */
class FakeStockRepository : StockRepository {

    // Seed 6 item persediaan mencakup kaos polos, bahan DTF, dan material kemasan
    private val stockStateFlow = MutableStateFlow<List<StockItem>>(
        listOf(
            StockItem(
                id = "stock-001",
                nama = "Cotton Combed 24s Hitam",
                kategori = StockCategory.BAJU_POLOS,
                satuan = "pcs",
                jumlah = 148,
                batasMinimum = 20
            ),
            StockItem(
                id = "stock-002",
                nama = "Cotton Combed 24s Putih",
                kategori = StockCategory.BAJU_POLOS,
                satuan = "pcs",
                jumlah = 65,
                batasMinimum = 20
            ),
            StockItem(
                id = "stock-003",
                nama = "Polo Shirt Lacoste Navy",
                kategori = StockCategory.BAJU_POLOS,
                satuan = "pcs",
                jumlah = 85,
                batasMinimum = 15
            ),
            StockItem(
                id = "stock-004",
                nama = "PET Film DTF Roll 30cm",
                kategori = StockCategory.MATERIAL_DTF,
                satuan = "roll",
                jumlah = 1,
                batasMinimum = 2 // isMenipis = true
            ),
            StockItem(
                id = "stock-005",
                nama = "Hotmelt Powder Adhesive DTF",
                kategori = StockCategory.MATERIAL_DTF,
                satuan = "roll",
                jumlah = 2,
                batasMinimum = 1
            ),
            StockItem(
                id = "stock-006",
                nama = "Polybag Kemasan Baju Sablon",
                kategori = StockCategory.LAINNYA,
                satuan = "pcs",
                jumlah = 350,
                batasMinimum = 50
            )
        )
    )

    private fun checkStaffRole(actor: Session): Resource.Error? {
        // TODO: di API asli aktor dari token
        return if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            Resource.Error("Akses ditolak. Fitur ini hanya untuk staf workshop (Admin atau Owner).")
        } else {
            null
        }
    }

    override fun getAllStock(actor: Session): Flow<Resource<List<StockItem>>> {
        return stockStateFlow.asStateFlow().map { list ->
            // TODO: di API asli aktor dari token
            if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
                Resource.Error("Akses ditolak. Fitur ini hanya untuk staf workshop (Admin atau Owner).")
            } else {
                Resource.Success(list)
            }
        }
    }

    override suspend fun getStockById(id: String, actor: Session): Resource<StockItem> {
        delay(300)
        checkStaffRole(actor)?.let { return it }

        val item = stockStateFlow.value.find { it.id == id }
            ?: return Resource.Error("Item persediaan tidak ditemukan.")

        return Resource.Success(item)
    }

    override suspend fun addStock(item: StockItem, actor: Session): Resource<StockItem> {
        delay(400)
        checkStaffRole(actor)?.let { return it }

        if (item.nama.isBlank()) {
            return Resource.Error("Nama barang wajib diisi.")
        }
        if (item.jumlah < 0) {
            return Resource.Error("Jumlah stok awal tidak boleh bernilai negatif.")
        }
        if (item.batasMinimum < 0) {
            return Resource.Error("Batas minimum stok tidak boleh bernilai negatif.")
        }

        val currentList = stockStateFlow.value.toMutableList()
        currentList.add(item)
        stockStateFlow.value = currentList

        return Resource.Success(item)
    }

    override suspend fun updateStock(item: StockItem, actor: Session): Resource<StockItem> {
        delay(400)
        checkStaffRole(actor)?.let { return it }

        if (item.nama.isBlank()) {
            return Resource.Error("Nama barang wajib diisi.")
        }
        if (item.jumlah < 0) {
            return Resource.Error("Jumlah stok tidak boleh bernilai negatif.")
        }
        if (item.batasMinimum < 0) {
            return Resource.Error("Batas minimum stok tidak boleh bernilai negatif.")
        }

        val currentList = stockStateFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == item.id }
        if (index == -1) {
            return Resource.Error("Item persediaan tidak ditemukan.")
        }

        currentList[index] = item
        stockStateFlow.value = currentList

        return Resource.Success(item)
    }

    override suspend fun deleteStock(id: String, actor: Session): Resource<Unit> {
        delay(400)
        checkStaffRole(actor)?.let { return it }

        val currentList = stockStateFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index == -1) {
            return Resource.Error("Item persediaan tidak ditemukan.")
        }

        currentList.removeAt(index)
        stockStateFlow.value = currentList

        return Resource.Success(Unit)
    }

    override suspend fun updateQuantity(
        id: String,
        delta: Int,
        keterangan: String?,
        actor: Session
    ): Resource<StockItem> {
        delay(350)
        checkStaffRole(actor)?.let { return it }

        if (delta == 0) {
            return Resource.Error("Perubahan jumlah stok tidak boleh 0.")
        }
        if (abs(delta.toLong()) > 100_000L) {
            return Resource.Error("Perubahan jumlah stok maksimal 100.000 per penyesuaian.")
        }

        val currentList = stockStateFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index == -1) {
            return Resource.Error("Item persediaan tidak ditemukan.")
        }

        val target = currentList[index]
        val newQuantity = target.jumlah + delta
        if (newQuantity < 0) {
            return Resource.Error("Jumlah stok tidak mencukupi untuk dikurangi. Sisa stok saat ini: ${target.jumlah} ${target.satuan}.")
        }

        val updated = target.copy(jumlah = newQuantity)
        currentList[index] = updated
        stockStateFlow.value = currentList

        return Resource.Success(updated)
    }
}
