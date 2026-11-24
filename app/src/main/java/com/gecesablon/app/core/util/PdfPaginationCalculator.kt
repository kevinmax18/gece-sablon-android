package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Transaction

/**
 * Utilitas fungsi murni untuk menghitung pembagian halaman (pagination) pada laporan PDF.
 *
 * Logika pagination:
 * - Halaman 1 memuat:
 *   - Judul & Subjudul
 *   - Informasi Periode & Waktu Pembuatan Laporan
 *   - Kotak Ringkasan Finansial (Pemasukan, Pengeluaran, Laba/Rugi)
 *   - Tabel Ringkasan per Kategori (hingga N baris)
 *   - Header Tabel Transaksi
 *   - Sisa ruang untuk baris transaksi (misal kapasitas maxFirstPage baris)
 * - Halaman 2 dan seterusnya (halaman lanjutan):
 *   - Header ringkas / Judul kecil
 *   - Header Tabel Transaksi
 *   - Baris transaksi (kapasitas maxSubsequentPages baris)
 * - Jika daftar transaksi kosong, tetap menghasilkan 1 halaman.
 */
object PdfPaginationCalculator {

    const val DEFAULT_FIRST_PAGE_MAX_ROWS = 14
    const val DEFAULT_SUBSEQUENT_PAGE_MAX_ROWS = 28

    /**
     * Merepresentasikan satu slice halaman transaksi.
     * @param pageNumber Nomor halaman (1-indexed)
     * @param totalPages Total keseluruhan halaman
     * @param startIndex Indeks awal item transaksi pada halaman ini (inklusif)
     * @param endIndex Indeks akhir item transaksi pada halaman ini (eksklusif)
     * @param items Daftar transaksi pada halaman ini
     */
    data class PageSlice<T>(
        val pageNumber: Int,
        val totalPages: Int,
        val startIndex: Int,
        val endIndex: Int,
        val items: List<T>
    )

    /**
     * Membagi daftar transaksi menjadi beberapa halaman [PageSlice].
     *
     * @param transactions Daftar transaksi lengkap
     * @param firstPageMaxRows Batas kapasitas baris transaksi di halaman pertama
     * @param subsequentPageMaxRows Batas kapasitas baris transaksi di halaman ke-2 dst
     */
    fun paginateTransactions(
        transactions: List<Transaction>,
        firstPageMaxRows: Int = DEFAULT_FIRST_PAGE_MAX_ROWS,
        subsequentPageMaxRows: Int = DEFAULT_SUBSEQUENT_PAGE_MAX_ROWS
    ): List<PageSlice<Transaction>> {
        require(firstPageMaxRows > 0) { "firstPageMaxRows harus > 0" }
        require(subsequentPageMaxRows > 0) { "subsequentPageMaxRows harus > 0" }

        if (transactions.isEmpty()) {
            return listOf(
                PageSlice(
                    pageNumber = 1,
                    totalPages = 1,
                    startIndex = 0,
                    endIndex = 0,
                    items = emptyList()
                )
            )
        }

        val totalItems = transactions.size
        val slices = mutableListOf<Triple<Int, Int, List<Transaction>>>()

        // Halaman pertama
        val firstEnd = totalItems.coerceAtMost(firstPageMaxRows)
        slices.add(Triple(0, firstEnd, transactions.subList(0, firstEnd)))

        var currentStart = firstEnd
        while (currentStart < totalItems) {
            val nextEnd = (currentStart + subsequentPageMaxRows).coerceAtMost(totalItems)
            slices.add(Triple(currentStart, nextEnd, transactions.subList(currentStart, nextEnd)))
            currentStart = nextEnd
        }

        val totalPages = slices.size
        return slices.mapIndexed { index, (start, end, items) ->
            PageSlice(
                pageNumber = index + 1,
                totalPages = totalPages,
                startIndex = start,
                endIndex = end,
                items = items
            )
        }
    }
}
