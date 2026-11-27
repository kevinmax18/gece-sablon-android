package com.gecesablon.app.data.repository

import com.gecesablon.app.core.network.safeApiCall
import com.gecesablon.app.core.network.safeApiCallUnit
import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.model.Role
import com.gecesablon.app.data.model.Session
import com.gecesablon.app.data.model.StockItem
import com.gecesablon.app.data.remote.GeceRemoteApi
import com.gecesablon.app.data.remote.dto.StockRequestDto
import com.gecesablon.app.data.remote.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.abs

/**
 * Implementasi StockRepository yang terhubung ke REST API Laravel GeCe Sablon.
 */
class RemoteStockRepository(
    private val api: GeceRemoteApi
) : StockRepository {

    override fun getAllStock(actor: Session): Flow<Resource<List<StockItem>>> = flow {
        emit(Resource.Loading)
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            emit(Resource.Error("Akses ditolak: Hanya staf workshop yang dapat melihat inventaris stok."))
            return@flow
        }
        val result = safeApiCall { api.getStock() }
        when (result) {
            is Resource.Success -> emit(Resource.Success(result.data.map { it.toDomain() }))
            is Resource.Error -> emit(Resource.Error(result.message))
            is Resource.Loading -> emit(Resource.Loading)
        }
    }

    override suspend fun getStockById(id: String, actor: Session): Resource<StockItem> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat melihat item stok.")
        }
        val allResult = safeApiCall { api.getStock() }
        return when (allResult) {
            is Resource.Success -> {
                val found = allResult.data.find { it.id.toString() == id }
                if (found != null) {
                    Resource.Success(found.toDomain())
                } else {
                    Resource.Error("Item stok dengan ID $id tidak ditemukan.")
                }
            }
            is Resource.Error -> Resource.Error(allResult.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun addStock(item: StockItem, actor: Session): Resource<StockItem> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat menambahkan stok.")
        }
        val req = StockRequestDto(
            name = item.nama,
            unit = item.satuan,
            quantity = item.jumlah.toDouble(),
            minQuantity = item.batasMinimum.toDouble()
        )
        val result = safeApiCall { api.createStock(req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun updateStock(item: StockItem, actor: Session): Resource<StockItem> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat mengubah stok.")
        }
        val intId = item.id.toIntOrNull()
            ?: return Resource.Error("ID stok tidak valid: ${item.id}")

        val req = StockRequestDto(
            name = item.nama,
            unit = item.satuan,
            quantity = item.jumlah.toDouble(),
            minQuantity = item.batasMinimum.toDouble()
        )
        val result = safeApiCall { api.updateStock(intId, req) }
        return when (result) {
            is Resource.Success -> Resource.Success(result.data.toDomain())
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun deleteStock(id: String, actor: Session): Resource<Unit> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat menghapus stok.")
        }
        val intId = id.toIntOrNull()
            ?: return Resource.Error("ID stok tidak valid: $id")

        return safeApiCallUnit { api.deleteStock(intId) }
    }

    override suspend fun updateQuantity(
        id: String,
        delta: Int,
        keterangan: String?,
        actor: Session
    ): Resource<StockItem> {
        if (actor.role != Role.ADMIN && actor.role != Role.OWNER) {
            return Resource.Error("Akses ditolak: Hanya staf workshop yang dapat menyesuaikan jumlah stok.")
        }
        if (delta == 0) {
            return Resource.Error("Selisih penyesuaian stok tidak boleh 0.")
        }
        if (abs(delta) > 100_000) {
            return Resource.Error("Nilai perubahan stok melebihi batas maksimum (100.000).")
        }

        val current = getStockById(id, actor)
        if (current !is Resource.Success) {
            return Resource.Error((current as? Resource.Error)?.message ?: "Item stok tidak ditemukan.")
        }

        val item = current.data
        val newQty = item.jumlah + delta
        if (newQty < 0) {
            return Resource.Error("Jumlah stok tidak boleh bernilai negatif (stok saat ini: ${item.jumlah}, penyesuaian: $delta).")
        }

        return updateStock(item.copy(jumlah = newQty), actor)
    }
}
