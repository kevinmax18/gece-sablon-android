package com.gecesablon.app.ui.owner.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.gecesablon.app.core.util.Formatter
import com.gecesablon.app.core.util.PdfPaginationCalculator
import com.gecesablon.app.data.model.Transaction
import com.gecesablon.app.data.model.TransactionCategory
import com.gecesablon.app.data.model.TransactionType
import com.gecesablon.app.data.model.displayName
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Pembuat dokumen PDF laporan keuangan GeCe Screen Printing berbasis android.graphics.pdf.PdfDocument bawaan.
 *
 * Ditaruh di ui/owner/export agar tidak mencemari layer core/data/repository dengan Android SDK.
 * Menggunakan [PdfPaginationCalculator] murni untuk pembagian halaman.
 */
object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points at 72 dpi
    private const val PAGE_HEIGHT = 842 // A4 standard height in points at 72 dpi

    private const val MARGIN_X = 40f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_BOTTOM = 40f
    private val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2)

    private val localeId = Locale("id", "ID")

    fun generatePdf(
        rangeStart: Long,
        rangeEnd: Long,
        totalIncome: Long,
        totalExpense: Long,
        netProfit: Long,
        categoryTotals: Map<TransactionCategory, Long>,
        transactions: List<Transaction>,
        outputStream: OutputStream,
        timeZone: TimeZone = TimeZone.getDefault()
    ) {
        val pdfDocument = PdfDocument()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(27, 28, 29) // InkBlack
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(27, 28, 29)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(27, 28, 29)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(107, 114, 128) // TextSecondary
            textSize = 9f
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(229, 231, 235) // Border
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(249, 250, 251) // SurfaceCard
            style = Paint.Style.FILL
        }

        val dateFormat = SimpleDateFormat("d MMMM yyyy, HH.mm", localeId).apply {
            this.timeZone = timeZone
        }
        val dateOnlyFormat = SimpleDateFormat("d MMMM yyyy", localeId).apply {
            this.timeZone = timeZone
        }

        val pages = PdfPaginationCalculator.paginateTransactions(transactions)
        val totalPages = pages.size

        for (slice in pages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, slice.pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var currentY = MARGIN_TOP

            // Header Halaman
            if (slice.pageNumber == 1) {
                // Judul Laporan
                canvas.drawText("Laporan Keuangan GeCe Screen Printing", MARGIN_X, currentY + 14f, titlePaint)
                currentY += 20f

                val periodStr = "Periode: ${dateOnlyFormat.format(Date(rangeStart))} - ${dateOnlyFormat.format(Date(rangeEnd))}"
                canvas.drawText(periodStr, MARGIN_X, currentY + 10f, subtitlePaint)
                currentY += 14f

                val generatedStr = "Waktu Pembuatan: ${dateFormat.format(Date(System.currentTimeMillis()))}"
                canvas.drawText(generatedStr, MARGIN_X, currentY + 10f, subtitlePaint)
                currentY += 22f

                // Garis pemisah
                canvas.drawLine(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY, linePaint)
                currentY += 12f

                // Kotak Ringkasan Finansial (3 Kolom: Pemasukan, Pengeluaran, Laba/Rugi)
                val boxHeight = 52f
                val colWidth = (CONTENT_WIDTH - 16f) / 3f

                // Col 1: Pemasukan
                drawSummaryBox(
                    canvas, boxPaint, linePaint, boldPaint, textPaint,
                    x = MARGIN_X,
                    y = currentY,
                    w = colWidth,
                    h = boxHeight,
                    label = "Total Pemasukan",
                    value = Formatter.formatRupiah(totalIncome),
                    valueColor = Color.rgb(22, 101, 52) // Green
                )

                // Col 2: Pengeluaran
                drawSummaryBox(
                    canvas, boxPaint, linePaint, boldPaint, textPaint,
                    x = MARGIN_X + colWidth + 8f,
                    y = currentY,
                    w = colWidth,
                    h = boxHeight,
                    label = "Total Pengeluaran",
                    value = Formatter.formatRupiah(totalExpense),
                    valueColor = Color.rgb(185, 28, 28) // Red
                )

                // Col 3: Laba / Rugi
                val isProfit = netProfit >= 0L
                val profitLabel = if (isProfit) "Laba Bersih" else "Rugi Bersih"
                drawSummaryBox(
                    canvas, boxPaint, linePaint, boldPaint, textPaint,
                    x = MARGIN_X + (colWidth + 8f) * 2,
                    y = currentY,
                    w = colWidth,
                    h = boxHeight,
                    label = profitLabel,
                    value = Formatter.formatRupiah(netProfit),
                    valueColor = if (isProfit) Color.rgb(22, 101, 52) else Color.rgb(185, 28, 28)
                )

                currentY += boxHeight + 16f

                // Ringkasan per Kategori (Tabel kecil 2 Kolom)
                val activeCategories = TransactionCategory.values().filter { (categoryTotals[it] ?: 0L) > 0L }
                if (activeCategories.isNotEmpty()) {
                    canvas.drawText("Ringkasan per Kategori", MARGIN_X, currentY + 10f, boldPaint)
                    currentY += 16f

                    // Grid 2 kolom kategori
                    val catColWidth = (CONTENT_WIDTH - 20f) / 2f
                    var catX = MARGIN_X
                    var catY = currentY
                    var rowInCol = 0

                    activeCategories.forEachIndexed { idx, cat ->
                        val catTotal = categoryTotals[cat] ?: 0L
                        val textCat = "${cat.displayName()}: ${Formatter.formatRupiah(catTotal)}"
                        canvas.drawText("• $textCat", catX, catY + 9f, textPaint)

                        if (idx % 2 == 0) {
                            catX = MARGIN_X + catColWidth + 20f
                        } else {
                            catX = MARGIN_X
                            catY += 13f
                            rowInCol++
                        }
                    }
                    if (activeCategories.size % 2 != 0) {
                        catY += 13f
                    }
                    currentY = catY + 10f
                }

                canvas.drawLine(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY, linePaint)
                currentY += 14f

            } else {
                // Header halaman lanjutan
                canvas.drawText("Laporan Keuangan GeCe Screen Printing (Lanjutan)", MARGIN_X, currentY + 12f, boldPaint)
                currentY += 20f
                canvas.drawLine(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY, linePaint)
                currentY += 14f
            }

            // Header Tabel Transaksi
            canvas.drawText("Daftar Transaksi", MARGIN_X, currentY + 10f, boldPaint)
            currentY += 16f

            // Table Header Bar
            val tableTop = currentY
            canvas.drawRect(MARGIN_X, tableTop, MARGIN_X + CONTENT_WIDTH, tableTop + 18f, boxPaint)
            canvas.drawLine(MARGIN_X, tableTop, MARGIN_X + CONTENT_WIDTH, tableTop, linePaint)
            canvas.drawLine(MARGIN_X, tableTop + 18f, MARGIN_X + CONTENT_WIDTH, tableTop + 18f, linePaint)

            // Kolom: No (24), Tanggal (110), Tipe (65), Kategori (85), Keterangan (140), Nominal (sisa ~91)
            val colNoX = MARGIN_X + 4f
            val colDateX = MARGIN_X + 28f
            val colTypeX = MARGIN_X + 138f
            val colCatX = MARGIN_X + 203f
            val colKetX = MARGIN_X + 288f
            val colNomX = MARGIN_X + 428f

            canvas.drawText("No", colNoX, tableTop + 12f, boldPaint)
            canvas.drawText("Tanggal", colDateX, tableTop + 12f, boldPaint)
            canvas.drawText("Tipe", colTypeX, tableTop + 12f, boldPaint)
            canvas.drawText("Kategori", colCatX, tableTop + 12f, boldPaint)
            canvas.drawText("Keterangan", colKetX, tableTop + 12f, boldPaint)
            canvas.drawText("Nominal", colNomX, tableTop + 12f, boldPaint)

            currentY = tableTop + 18f

            if (slice.items.isEmpty()) {
                currentY += 16f
                canvas.drawText("Tidak ada transaksi pada periode ini.", MARGIN_X + 10f, currentY, subtitlePaint)
            } else {
                val rowHeight = 18f
                slice.items.forEachIndexed { itemIndex, tx ->
                    val globalIndex = slice.startIndex + itemIndex + 1
                    val rowY = currentY + (itemIndex * rowHeight)

                    val tglText = dateFormat.format(Date(tx.tanggal))
                    val tipeText = if (tx.tipe == TransactionType.PEMASUKAN) "Masuk" else "Keluar"
                    val katText = tx.kategori.displayName()
                    val ketText = if (tx.keterangan.length > 25) tx.keterangan.take(23) + "..." else tx.keterangan
                    val nomText = Formatter.formatRupiah(tx.nominal)

                    canvas.drawText(globalIndex.toString(), colNoX, rowY + 12f, textPaint)
                    canvas.drawText(tglText, colDateX, rowY + 12f, textPaint)
                    canvas.drawText(tipeText, colTypeX, rowY + 12f, textPaint)
                    canvas.drawText(katText, colCatX, rowY + 12f, textPaint)
                    canvas.drawText(ketText, colKetX, rowY + 12f, textPaint)
                    canvas.drawText(nomText, colNomX, rowY + 12f, textPaint)

                    canvas.drawLine(MARGIN_X, rowY + rowHeight, MARGIN_X + CONTENT_WIDTH, rowY + rowHeight, linePaint)
                }
            }

            // Footer Nomor Halaman
            val footerY = PAGE_HEIGHT - MARGIN_BOTTOM
            val pageNumStr = "Halaman ${slice.pageNumber} dari $totalPages"
            val pageNumWidth = subtitlePaint.measureText(pageNumStr)
            canvas.drawText(pageNumStr, (PAGE_WIDTH - pageNumWidth) / 2f, footerY, subtitlePaint)

            pdfDocument.finishPage(page)
        }

        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }

    private fun drawSummaryBox(
        canvas: Canvas,
        boxPaint: Paint,
        borderPaint: Paint,
        boldPaint: Paint,
        textPaint: Paint,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        label: String,
        value: String,
        valueColor: Int
    ) {
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 4f, 4f, boxPaint)
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

        val smallLabelPaint = Paint(textPaint).apply {
            textSize = 8.5f
            color = Color.rgb(107, 114, 128)
        }
        val valPaint = Paint(boldPaint).apply {
            textSize = 11f
            color = valueColor
        }

        canvas.drawText(label, x + 8f, y + 16f, smallLabelPaint)
        canvas.drawText(value, x + 8f, y + 36f, valPaint)
    }
}
