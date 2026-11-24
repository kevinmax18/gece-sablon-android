package com.gecesablon.app.core.util

import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfPaginationCalculatorTest {

    private fun dummyTransaction(id: String): Transaction {
        return Transaction(
            id = id,
            tipe = TransactionType.PEMASUKAN,
            kategori = TransactionCategory.PESANAN,
            nominal = 50000L,
            tanggal = System.currentTimeMillis(),
            keterangan = "Order $id",
            dibuatOleh = "Owner"
        )
    }

    @Test
    fun `paginateTransactions with empty list returns single empty page`() {
        val pages = PdfPaginationCalculator.paginateTransactions(emptyList())
        assertEquals(1, pages.size)
        assertEquals(1, pages[0].pageNumber)
        assertEquals(1, pages[0].totalPages)
        assertTrue(pages[0].items.isEmpty())
    }

    @Test
    fun `paginateTransactions fitting first page returns 1 page`() {
        val list = (1..10).map { dummyTransaction("tx-$it") }
        val pages = PdfPaginationCalculator.paginateTransactions(
            transactions = list,
            firstPageMaxRows = 14,
            subsequentPageMaxRows = 28
        )

        assertEquals(1, pages.size)
        assertEquals(1, pages[0].pageNumber)
        assertEquals(1, pages[0].totalPages)
        assertEquals(10, pages[0].items.size)
    }

    @Test
    fun `paginateTransactions overflowing first page splits into multiple pages correctly`() {
        // 14 on page 1, 28 on page 2, 5 on page 3 = 47 items
        val list = (1..47).map { dummyTransaction("tx-$it") }
        val pages = PdfPaginationCalculator.paginateTransactions(
            transactions = list,
            firstPageMaxRows = 14,
            subsequentPageMaxRows = 28
        )

        assertEquals(3, pages.size)

        // Page 1
        assertEquals(1, pages[0].pageNumber)
        assertEquals(3, pages[0].totalPages)
        assertEquals(14, pages[0].items.size)
        assertEquals("tx-1", pages[0].items.first().id)
        assertEquals("tx-14", pages[0].items.last().id)

        // Page 2
        assertEquals(2, pages[1].pageNumber)
        assertEquals(3, pages[1].totalPages)
        assertEquals(28, pages[1].items.size)
        assertEquals("tx-15", pages[1].items.first().id)
        assertEquals("tx-42", pages[1].items.last().id)

        // Page 3
        assertEquals(3, pages[2].pageNumber)
        assertEquals(3, pages[2].totalPages)
        assertEquals(5, pages[2].items.size)
        assertEquals("tx-43", pages[2].items.first().id)
        assertEquals("tx-47", pages[2].items.last().id)
    }
}
