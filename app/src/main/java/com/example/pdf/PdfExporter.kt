package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.model.ChecklistItem
import com.example.data.local.model.TableData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExporter(private val context: Context) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun exportNoteToPdf(note: NoteEntity, category: CategoryEntity? = null): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width at 72dpi
        val pageHeight = 842 // A4 height at 72dpi
        val margin = 40f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.GRAY
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        var currentY = margin + 30f

        // Header Title
        val title = note.title.ifBlank { "Untitled Note" }
        canvas.drawText(title, margin, currentY, titlePaint)
        currentY += 24f

        // Metadata: Category & Date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(note.updatedAt))
        val categoryStr = category?.name ?: "General"
        val metaStr = "Category: $categoryStr  |  Updated: $dateStr"
        canvas.drawText(metaStr, margin, currentY, metaPaint)
        currentY += 15f

        // Horizontal Divider Line
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
        currentY += 25f

        // Helper to check page boundary and create new page if needed
        fun checkNewPage() {
            if (currentY > pageHeight - margin - 40f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin + 20f
            }
        }

        // Render based on Note Type
        when (note.type) {
            "CHECKLIST" -> {
                val checklistItems = parseChecklist(note.checklistJson)
                if (note.content.isNotBlank()) {
                    val lines = note.content.split("\n")
                    for (line in lines) {
                        checkNewPage()
                        canvas.drawText(line, margin, currentY, bodyPaint)
                        currentY += 20f
                    }
                    currentY += 10f
                }

                for (item in checklistItems) {
                    checkNewPage()
                    val checkboxSymbol = if (item.isCompleted) "[✓] " else "[  ] "
                    val itemText = checkboxSymbol + item.text
                    val paint = if (item.isCompleted) {
                        Paint(bodyPaint).apply {
                            color = Color.GRAY
                            isStrikeThruText = true
                        }
                    } else bodyPaint

                    canvas.drawText(itemText, margin, currentY, paint)
                    currentY += 22f
                }
            }

            "TABLE" -> {
                val tableData = parseTable(note.tableJson)
                if (note.content.isNotBlank()) {
                    canvas.drawText(note.content, margin, currentY, bodyPaint)
                    currentY += 25f
                }

                if (tableData != null && tableData.headers.isNotEmpty()) {
                    val cols = tableData.headers.size
                    val availableWidth = pageWidth - (margin * 2)
                    val colWidth = availableWidth / cols.coerceAtLeast(1)
                    val rowHeight = 28f

                    val headerBgPaint = Paint().apply {
                        color = Color.parseColor("#EEEEEE")
                        style = Paint.Style.FILL
                    }
                    val headerTextPaint = Paint(bodyPaint).apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }

                    // Draw Table Headers
                    checkNewPage()
                    canvas.drawRect(margin, currentY, pageWidth - margin, currentY + rowHeight, headerBgPaint)
                    for (i in tableData.headers.indices) {
                        val cellX = margin + (i * colWidth) + 8f
                        val headerText = tableData.headers[i]
                        canvas.drawText(headerText, cellX, currentY + 18f, headerTextPaint)
                        canvas.drawRect(
                            margin + (i * colWidth), currentY,
                            margin + ((i + 1) * colWidth), currentY + rowHeight,
                            linePaint
                        )
                    }
                    currentY += rowHeight

                    // Draw Table Rows
                    for (row in tableData.rows) {
                        checkNewPage()
                        for (i in 0 until cols) {
                            val cellText = row.getOrNull(i) ?: ""
                            val cellX = margin + (i * colWidth) + 8f
                            canvas.drawText(cellText, cellX, currentY + 18f, bodyPaint)
                            canvas.drawRect(
                                margin + (i * colWidth), currentY,
                                margin + ((i + 1) * colWidth), currentY + rowHeight,
                                linePaint
                            )
                        }
                        currentY += rowHeight
                    }
                }
            }

            else -> { // TEXT & RICH
                val lines = note.content.split("\n")
                for (line in lines) {
                    checkNewPage()
                    // Check heading styling in plain text rendering
                    val paint = when {
                        line.startsWith("# ") -> Paint(titlePaint).apply { textSize = 18f }
                        line.startsWith("## ") -> Paint(titlePaint).apply { textSize = 16f }
                        line.startsWith("### ") -> Paint(titlePaint).apply { textSize = 15f }
                        else -> bodyPaint
                    }
                    val textToDraw = line.removePrefix("# ").removePrefix("## ").removePrefix("### ")
                    canvas.drawText(textToDraw, margin, currentY, paint)
                    currentY += 22f
                }
            }
        }

        pdfDocument.finishPage(page)

        return try {
            val pdfDir = File(context.cacheDir, "exported_pdfs")
            if (!pdfDir.exists()) pdfDir.mkdirs()
            val safeFileName = "NoteFlow_${note.title.take(15).replace("[^a-zA-Z0-9]".toRegex(), "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(pdfDir, safeFileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun sharePdf(file: File) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Note PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun parseChecklist(json: String?): List<ChecklistItem> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, ChecklistItem::class.java)
            val adapter = moshi.adapter<List<ChecklistItem>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseTable(json: String?): TableData? {
        if (json.isNullOrBlank()) return null
        return try {
            val adapter = moshi.adapter(TableData::class.java)
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}
