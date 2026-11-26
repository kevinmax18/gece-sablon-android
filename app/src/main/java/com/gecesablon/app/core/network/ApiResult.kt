package com.gecesablon.app.core.network

import com.gecesablon.app.core.util.Resource
import com.gecesablon.app.data.remote.dto.ApiEnvelope
import com.gecesablon.app.data.remote.dto.ApiErrorResponse
import com.gecesablon.app.data.remote.dto.PaginatedResponse
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Helper pembungkus pemanggilan Retrofit API yang mengembalikan payload data di dalam ApiEnvelope.
 * Tidak pernah melempar exception ke luar repository.
 * Seluruh error dipetakan ke pesan berbahasa Indonesia ramah pengguna.
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> ApiEnvelope<T>
): Resource<T> {
    return try {
        val response = apiCall()
        if (response.success) {
            val data = response.data
            if (data != null) {
                Resource.Success(data)
            } else {
                Resource.Error(response.message ?: "Data tidak ditemukan.")
            }
        } else {
            Resource.Error(response.message ?: "Permintaan gagal diproses.")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        handleHttpException(e)
    } catch (e: SocketTimeoutException) {
        Resource.Error("Server tidak merespons. Coba lagi.")
    } catch (e: IOException) {
        Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Terjadi kesalahan tidak terduga.")
    }
}

/**
 * Helper pembungkus pemanggilan Retrofit API untuk endpoint tanpa payload data (seperti logout).
 * Mengembalikan Resource.Success(Unit) jika response.success bernilai true.
 */
suspend fun <T> safeApiCallUnit(
    apiCall: suspend () -> ApiEnvelope<T>
): Resource<Unit> {
    return try {
        val response = apiCall()
        if (response.success) {
            Resource.Success(Unit)
        } else {
            Resource.Error(response.message ?: "Permintaan gagal diproses.")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        handleHttpException(e)
    } catch (e: SocketTimeoutException) {
        Resource.Error("Server tidak merespons. Coba lagi.")
    } catch (e: IOException) {
        Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Terjadi kesalahan tidak terduga.")
    }
}

/**
 * Pemetaan status kode HTTP dan parsing body error dari Laravel.
 */
private fun handleHttpException(e: HttpException): Resource.Error {
    val code = e.code()
    val parsedError = parseErrorBody(e)
    val serverMessage = parsedError?.message

    return when (code) {
        401 -> Resource.Error(serverMessage?.takeIf { it.isNotBlank() } ?: "Sesi berakhir atau kredensial salah. Silakan login ulang.")
        403 -> Resource.Error("Anda tidak memiliki akses.")
        404 -> Resource.Error("Data tidak ditemukan.")
        422 -> {
            val validationMessages = parsedError?.errors
                ?.values
                ?.flatten()
                ?.filter { it.isNotBlank() }

            val combined = if (!validationMessages.isNullOrEmpty()) {
                validationMessages.joinToString("\n")
            } else {
                serverMessage ?: "Data yang dikirim tidak valid."
            }
            Resource.Error(combined)
        }
        in 500..599 -> Resource.Error("Terjadi kesalahan di server. Coba lagi.")
        else -> Resource.Error(serverMessage ?: "Terjadi kesalahan ($code). Coba lagi.")
    }
}

/**
 * Mencoba mengekstrak body JSON error dari HttpException.
 */
private fun parseErrorBody(e: HttpException): ApiErrorResponse? {
    return try {
        val raw = e.response()?.errorBody()?.string()
        if (!raw.isNullOrBlank()) {
            ApiClient.json.decodeFromString<ApiErrorResponse>(raw)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Helper generik untuk mengambil seluruh data dari endpoint berpaginasi Laravel ($query->paginate()).
 * Memanggil [fetchPage] berulang kali dari halaman 1 hingga [PaginatedResponse.lastPage]
 * dan menggabungkan seluruh item menjadi satu [List].
 *
 * @param fetchPage Lambda yang memanggil endpoint untuk halaman tertentu
 * @return Resource.Success berisi gabungan seluruh item, atau Resource.Error jika salah satu halaman gagal
 */
suspend fun <T> fetchAllPages(
    fetchPage: suspend (page: Int) -> Resource<PaginatedResponse<T>>
): Resource<List<T>> {
    val allItems = mutableListOf<T>()
    var currentPage = 1
    var lastPage = 1

    do {
        when (val result = fetchPage(currentPage)) {
            is Resource.Loading -> {
                // Abaikan intermediate loading
            }
            is Resource.Error -> {
                return Resource.Error(result.message)
            }
            is Resource.Success -> {
                val pagination = result.data
                allItems.addAll(pagination.data)
                lastPage = pagination.lastPage ?: currentPage
                currentPage++
            }
        }
    } while (currentPage <= lastPage)

    return Resource.Success(allItems)
}
