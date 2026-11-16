package com.rahatsharif.dalilapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.Toast
import com.rahatsharif.dalilapp.model.DalilRecord
import com.rahatsharif.dalilapp.model.GlobalHeader
import com.rahatsharif.dalilapp.model.PersonEntry
import com.rahatsharif.dalilapp.model.PrintSettings
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object PrintHelper {

    private val colWeights = floatArrayOf(
        4.5f,   // নং
        8.5f,   // দলিল নং
        11.0f,  // তারিখ
        20.25f, // গ্রহীতা(গণ)
        20.25f, // দাতা(গণ)
        8.0f,   // মোট জমি
        9.0f,   // খতিয়ান
        8.5f,   // খতিয়ানের জমি
        10.0f   // দাগ নং
    )

    private val colHeaders = arrayOf(
        "নং",
        "দলিল নং",
        "তারিখ",
        "গ্রহীতা(গণ)",
        "দাতা(গণ)",
        "মোট জমি",
        "খতিয়ান",
        "খতিয়ানের জমি",
        "দাগ নং"
    )

    fun printRecords(
        context: Context,
        header: GlobalHeader,
        records: List<DalilRecord>,
        settings: PrintSettings,
        jobName: String = "দলিল ফিগার প্রিন্ট ও সংরক্ষণ"
    ) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "প্রিন্ট সার্ভিস পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                return
            }

            val isLandscape = settings.orientation.equals("landscape", ignoreCase = true)
            val mediaSize = if (isLandscape) {
                PrintAttributes.MediaSize.ISO_A4.asLandscape()
            } else {
                PrintAttributes.MediaSize.ISO_A4.asPortrait()
            }

            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(mediaSize)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()

            val adapter = DalilPrintDocumentAdapter(
                context = context,
                header = header.copy(),
                records = records.map { it.copy() },
                settings = settings.copy(),
                jobName = jobName
            )
            printManager.print(jobName, adapter, printAttributes)
        } catch (e: Exception) {
            Toast.makeText(context, "প্রিন্ট চালু করা যায়নি: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private data class PersonBlockLayout(
        val layout: StaticLayout
    )

    private data class KhatianRowLayout(
        val col7: StaticLayout,
        val col8: StaticLayout,
        val col9: StaticLayout,
        var height: Float
    )

    private data class RecordRenderPlan(
        val col1: StaticLayout,
        val col2: StaticLayout,
        val col3: StaticLayout,
        val col4Blocks: List<PersonBlockLayout>,
        val col5Blocks: List<PersonBlockLayout>,
        val col6: StaticLayout,
        val khatianRows: List<KhatianRowLayout>,
        val totalHeight: Float
    )

    private data class PagePlan(
        val showDocHeader: Boolean,
        val rows: List<RecordRenderPlan>
    )

    private class DalilPrintDocumentAdapter(
        private val context: Context,
        private val header: GlobalHeader,
        private val records: List<DalilRecord>,
        private val settings: PrintSettings,
        private val jobName: String
    ) : PrintDocumentAdapter() {

        private var activeAttributes: PrintAttributes? = null
        private var pages: List<PagePlan> = emptyList()
        private var pageWidthPt = 595f
        private var pageHeightPt = 842f
        private var contentLeft = 26f
        private var contentTop = 26f
        private var contentWidth = 543f
        private var contentBottom = 816f
        private var outerBorderRect: RectF? = null
        private var colWidths = FloatArray(9)
        private var tableHeaderHeight = 26f
        private var headerColLayouts: Triple<StaticLayout, StaticLayout, StaticLayout>? = null
        private var docHeaderHeight = 0f
        private lateinit var tableHeaderLayouts: List<StaticLayout>

        private val baseFontSize = if (settings.orientation == "landscape") 9.2f else 8.4f
        private val headerFontSize = if (settings.orientation == "landscape") 9.2f else 8.5f

        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 0.85f
        }

        private val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
        }

        private val dottedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#777777")
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            pathEffect = DashPathEffect(floatArrayOf(2f, 2f), 0f)
        }

        private val headerBgPaint = Paint().apply {
            color = Color.parseColor("#EDEDED")
            style = Paint.Style.FILL
        }

        private val whiteBgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        private val cellTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = baseFontSize
            typeface = Typeface.SERIF
        }

        private val thTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = baseFontSize
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        private val docHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = headerFontSize
            typeface = Typeface.SERIF
        }

        private val docTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = if (settings.orientation == "landscape") 15f else 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }

            activeAttributes = newAttributes
            val mediaSize = newAttributes.mediaSize ?: PrintAttributes.MediaSize.ISO_A4
            pageWidthPt = mediaSize.widthMils * 72f / 1000f
            pageHeightPt = mediaSize.heightMils * 72f / 1000f

            val baseMargin = when (settings.marginMode) {
                "narrow" -> 16f
                "wide" -> 38f
                else -> 26f
            }

            if (settings.showOuterBorder) {
                outerBorderRect = RectF(
                    baseMargin,
                    baseMargin,
                    pageWidthPt - baseMargin,
                    pageHeightPt - baseMargin
                )
                val innerPad = 9f
                contentLeft = baseMargin + innerPad
                contentTop = baseMargin + innerPad
                contentWidth = pageWidthPt - 2f * (baseMargin + innerPad)
                contentBottom = pageHeightPt - (baseMargin + innerPad)
            } else {
                outerBorderRect = null
                contentLeft = baseMargin
                contentTop = baseMargin
                contentWidth = pageWidthPt - 2f * baseMargin
                contentBottom = pageHeightPt - baseMargin
            }

            for (i in colWeights.indices) {
                colWidths[i] = contentWidth * (colWeights[i] / 100f)
            }

            buildDocumentHeaderLayout()
            buildTableHeaderLayout()
            pages = buildPaginatedPlans()

            val info = PrintDocumentInfo.Builder(jobName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(max(1, pages.size))
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pageRanges: Array<PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            val attrs = activeAttributes ?: run {
                callback.onWriteFailed("Missing print attributes")
                return
            }

            val pdfDoc = PrintedPdfDocument(context, attrs)
            try {
                val safePages = if (pages.isEmpty()) listOf(PagePlan(true, emptyList())) else pages
                for (pageIdx in safePages.indices) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                        pdfDoc.close()
                        return
                    }
                    if (!containsPage(pageRanges, pageIdx)) continue

                    val page = pdfDoc.startPage(pageIdx)
                    drawPage(page.canvas, safePages[pageIdx])
                    pdfDoc.finishPage(page)
                }

                pdfDoc.writeTo(FileOutputStream(destination.fileDescriptor))
                callback.onWriteFinished(pageRanges)
            } catch (e: Exception) {
                callback.onWriteFailed(e.message)
            } finally {
                pdfDoc.close()
            }
        }

        private fun buildDocumentHeaderLayout() {
            val leftW = (contentWidth * 0.30f).roundToInt().coerceAtLeast(40)
            val centerW = (contentWidth * 0.40f).roundToInt().coerceAtLeast(40)
            val rightW = (contentWidth * 0.30f).roundToInt().coerceAtLeast(40)

            val leftSpan = SpannableStringBuilder().apply {
                appendBold("জেলা: "); append(header.district.ifBlank { "-" }); append("\n")
                appendBold("উপজেলা: "); append(header.upazila.ifBlank { "-" }); append("\n")
                appendBold("মৌজা: "); append(header.mouza.ifBlank { "-" })
            }

            val centerSpan = SpannableStringBuilder("দলিলে জমির হিসাব").apply {
                setSpan(UnderlineSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            val dateText = header.date.ifBlank { BanglaUtils.getTodayDateBangla() }
            val rightSpan = SpannableStringBuilder().apply {
                appendBold("প্রস্তুতকারক:\n")
                append(header.creator.ifBlank { "-" }); append("\n")
                appendBold("তারিখ: "); append(dateText)
            }

            val leftLayout = buildStaticLayout(leftSpan, docHeaderPaint, leftW, Layout.Alignment.ALIGN_NORMAL)
            val centerLayout = buildStaticLayout(centerSpan, docTitlePaint, centerW, Layout.Alignment.ALIGN_CENTER)
            val rightLayout = buildStaticLayout(rightSpan, docHeaderPaint, rightW, Layout.Alignment.ALIGN_OPPOSITE)

            headerColLayouts = Triple(leftLayout, centerLayout, rightLayout)
            docHeaderHeight = maxOf(leftLayout.height, centerLayout.height, rightLayout.height) + 10f
        }

        private fun buildTableHeaderLayout() {
            val hPad = 4f
            val vPad = 5f
            val list = colHeaders.mapIndexed { idx, title ->
                val maxW = (colWidths[idx] - 2 * hPad).roundToInt().coerceAtLeast(10)
                buildStaticLayout(title, thTextPaint, maxW, Layout.Alignment.ALIGN_CENTER)
            }
            tableHeaderLayouts = list
            val maxH = list.maxOfOrNull { it.height } ?: 14
            tableHeaderHeight = maxH + 2f * vPad
        }

        private fun buildPaginatedPlans(): List<PagePlan> {
            val recordPlans = records.mapIndexed { idx, rec -> buildRecordPlan(idx + 1, rec) }
            if (recordPlans.isEmpty()) {
                return listOf(PagePlan(showDocHeader = true, rows = emptyList()))
            }

            val result = mutableListOf<PagePlan>()
            var currentRows = mutableListOf<RecordRenderPlan>()
            var isFirstPage = true
            var availableHeight = (contentBottom - contentTop) - docHeaderHeight - tableHeaderHeight

            for (plan in recordPlans) {
                if (plan.totalHeight > availableHeight && currentRows.isNotEmpty()) {
                    result.add(PagePlan(showDocHeader = isFirstPage, rows = currentRows))
                    isFirstPage = false
                    currentRows = mutableListOf()
                    availableHeight = (contentBottom - contentTop) - tableHeaderHeight
                }
                currentRows.add(plan)
                availableHeight -= plan.totalHeight
            }

            if (currentRows.isNotEmpty()) {
                result.add(PagePlan(showDocHeader = isFirstPage, rows = currentRows))
            }
            return result
        }

        private fun buildRecordPlan(serial: Int, rec: DalilRecord): RecordRenderPlan {
            val hPad = 4f
            val vPad = 4.5f

            val l1 = buildStaticLayout(
                BanglaUtils.toBanglaNum(serial),
                cellTextPaint,
                (colWidths[0] - 2 * hPad).roundToInt().coerceAtLeast(8),
                Layout.Alignment.ALIGN_CENTER
            )
            val l2 = buildStaticLayout(
                BanglaUtils.toBanglaNum(rec.dalilNo.ifBlank { "-" }),
                cellTextPaint,
                (colWidths[1] - 2 * hPad).roundToInt().coerceAtLeast(8),
                Layout.Alignment.ALIGN_CENTER
            )
            val l3 = buildStaticLayout(
                BanglaUtils.toBanglaNum(rec.date.ifBlank { "-" }),
                cellTextPaint,
                (colWidths[2] - 2 * hPad).roundToInt().coerceAtLeast(8),
                Layout.Alignment.ALIGN_CENTER
            )

            val col4W = (colWidths[3] - 2 * hPad).roundToInt().coerceAtLeast(20)
            val col5W = (colWidths[4] - 2 * hPad).roundToInt().coerceAtLeast(20)

            val col4Blocks = buildPersonBlocks(rec.receivers, "জমি", col4W)
            val col5Blocks = buildPersonBlocks(rec.donors, "বিক্রি", col5W)

            val totalLandStr = if (rec.totalLand.isNotBlank() && rec.totalLand != "-") {
                BanglaUtils.formatShatak(rec.totalLand)
            } else {
                "-"
            }
            val l6 = buildStaticLayout(
                totalLandStr,
                cellTextPaint,
                (colWidths[5] - 2 * hPad).roundToInt().coerceAtLeast(8),
                Layout.Alignment.ALIGN_CENTER
            )

            val safeKhatians = if (rec.khatians.isEmpty()) {
                listOf(com.rahatsharif.dalilapp.model.KhatianEntry(type = "", no = "-", land = "-", dag = "-"))
            } else {
                rec.khatians
            }

            val kRows = safeKhatians.map { k ->
                val khSpan = SpannableStringBuilder().apply {
                    when {
                        k.type.isNotBlank() && k.no.isNotBlank() && k.no != "-" -> {
                            appendBold("${k.type}: "); append(BanglaUtils.toBanglaNum(k.no))
                        }
                        k.type.isNotBlank() -> appendBold(k.type)
                        k.no.isNotBlank() && k.no != "-" -> append(BanglaUtils.toBanglaNum(k.no))
                        else -> append("-")
                    }
                }
                val l7 = buildStaticLayout(
                    khSpan,
                    cellTextPaint,
                    (colWidths[6] - 2 * hPad).roundToInt().coerceAtLeast(8),
                    Layout.Alignment.ALIGN_CENTER
                )
                val l8 = buildStaticLayout(
                    if (k.land.isNotBlank() && k.land != "-") BanglaUtils.formatShatak(k.land) else "-",
                    cellTextPaint,
                    (colWidths[7] - 2 * hPad).roundToInt().coerceAtLeast(8),
                    Layout.Alignment.ALIGN_CENTER
                )
                val l9 = buildStaticLayout(
                    BanglaUtils.formatDagList(k.dag),
                    cellTextPaint,
                    (colWidths[8] - 2 * hPad).roundToInt().coerceAtLeast(8),
                    Layout.Alignment.ALIGN_CENTER
                )
                val kH = maxOf(l7.height, l8.height, l9.height) + 2f * vPad
                KhatianRowLayout(l7, l8, l9, kH)
            }

            val col4H = blocksTotalHeight(col4Blocks) + 2f * vPad
            val col5H = blocksTotalHeight(col5Blocks) + 2f * vPad
            val spanMinH = maxOf(
                l1.height + 2f * vPad,
                l2.height + 2f * vPad,
                l3.height + 2f * vPad,
                col4H,
                col5H,
                l6.height + 2f * vPad
            )
            val khatiansMinH = kRows.sumOf { it.height.toDouble() }.toFloat()
            val totalH = max(spanMinH, khatiansMinH)

            if (totalH > khatiansMinH && kRows.isNotEmpty()) {
                val extraPerRow = (totalH - khatiansMinH) / kRows.size
                kRows.forEach { it.height += extraPerRow }
            }

            return RecordRenderPlan(
                col1 = l1,
                col2 = l2,
                col3 = l3,
                col4Blocks = col4Blocks,
                col5Blocks = col5Blocks,
                col6 = l6,
                khatianRows = kRows,
                totalHeight = totalH
            )
        }

        private fun buildPersonBlocks(
            people: List<PersonEntry>,
            landPrefix: String,
            maxWidth: Int
        ): List<PersonBlockLayout> {
            if (people.isEmpty()) {
                return listOf(
                    PersonBlockLayout(
                        buildStaticLayout("-", cellTextPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL)
                    )
                )
            }
            val multi = people.size > 1
            return people.mapIndexed { idx, p ->
                val sb = SpannableStringBuilder()
                val prefix = if (multi) "${BanglaUtils.toBanglaNum(idx + 1)}. " else ""
                sb.appendBold("$prefix${p.name.ifBlank { "-" }}")
                if (p.guardian.isNotBlank()) {
                    sb.append("\n${p.relation}: ${p.guardian}")
                }
                if (p.mother.isNotBlank()) {
                    sb.append("\nমাতা: ${p.mother}")
                }
                val cleanedLand = BanglaUtils.cleanShatakForEdit(p.land)
                if (cleanedLand.isNotBlank()) {
                    sb.append("\n$landPrefix: ${BanglaUtils.formatShatak(cleanedLand)}")
                }
                PersonBlockLayout(
                    buildStaticLayout(sb, cellTextPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL)
                )
            }
        }

        private fun blocksTotalHeight(blocks: List<PersonBlockLayout>): Float {
            if (blocks.isEmpty()) return 0f
            val textSum = blocks.sumOf { it.layout.height.toDouble() }.toFloat()
            val dividersGap = (blocks.size - 1) * 6f
            return textSum + dividersGap
        }

        private fun drawPage(canvas: Canvas, pagePlan: PagePlan) {
            canvas.drawRect(0f, 0f, pageWidthPt, pageHeightPt, whiteBgPaint)
            outerBorderRect?.let { rect ->
                canvas.drawRect(rect, outerBorderPaint)
            }

            var cursorY = contentTop

            if (pagePlan.showDocHeader) {
                headerColLayouts?.let { (leftL, centerL, rightL) ->
                    val leftX = contentLeft
                    val centerX = contentLeft + contentWidth * 0.30f
                    val rightX = contentLeft + contentWidth * 0.70f

                    drawStaticLayout(canvas, leftL, leftX, cursorY)
                    drawStaticLayout(canvas, centerL, centerX, cursorY + 2f)
                    drawStaticLayout(canvas, rightL, rightX, cursorY)
                    cursorY += docHeaderHeight
                }
            }

            // Draw table header row (#EDEDED background + crisp borders)
            val thBottom = cursorY + tableHeaderHeight
            canvas.drawRect(contentLeft, cursorY, contentLeft + contentWidth, thBottom, headerBgPaint)

            var colX = contentLeft
            for (i in 0 until 9) {
                val cw = colWidths[i]
                val l = tableHeaderLayouts[i]
                val textY = cursorY + (tableHeaderHeight - l.height) / 2f
                drawStaticLayout(canvas, l, colX + 4f, textY)
                canvas.drawRect(colX, cursorY, colX + cw, thBottom, borderPaint)
                colX += cw
            }
            cursorY = thBottom

            // Draw records
            for (plan in pagePlan.rows) {
                val rowTop = cursorY
                val rowBottom = rowTop + plan.totalHeight

                // Draw columns 1..6 (rowspan across all khatians)
                var cx = contentLeft
                val spannedLayouts = listOf(plan.col1, plan.col2, plan.col3)
                for (i in 0..2) {
                    val cw = colWidths[i]
                    val l = spannedLayouts[i]
                    val ty = rowTop + (plan.totalHeight - l.height) / 2f
                    drawStaticLayout(canvas, l, cx + 4f, ty)
                    canvas.drawRect(cx, rowTop, cx + cw, rowBottom, borderPaint)
                    cx += cw
                }

                // Col 4: Receivers
                drawPersonBlocksCell(canvas, plan.col4Blocks, cx, rowTop, colWidths[3], plan.totalHeight)
                canvas.drawRect(cx, rowTop, cx + colWidths[3], rowBottom, borderPaint)
                cx += colWidths[3]

                // Col 5: Donors
                drawPersonBlocksCell(canvas, plan.col5Blocks, cx, rowTop, colWidths[4], plan.totalHeight)
                canvas.drawRect(cx, rowTop, cx + colWidths[4], rowBottom, borderPaint)
                cx += colWidths[4]

                // Col 6: Total Land
                val l6Y = rowTop + (plan.totalHeight - plan.col6.height) / 2f
                drawStaticLayout(canvas, plan.col6, cx + 4f, l6Y)
                canvas.drawRect(cx, rowTop, cx + colWidths[5], rowBottom, borderPaint)
                cx += colWidths[5]

                // Cols 7..9: Each Khatian sub-row
                var subTop = rowTop
                for (kRow in plan.khatianRows) {
                    val subBottom = subTop + kRow.height
                    var kx = cx
                    val kLayouts = listOf(kRow.col7, kRow.col8, kRow.col9)
                    for (j in 0..2) {
                        val cw = colWidths[6 + j]
                        val kl = kLayouts[j]
                        val ky = subTop + (kRow.height - kl.height) / 2f
                        drawStaticLayout(canvas, kl, kx + 4f, ky)
                        canvas.drawRect(kx, subTop, kx + cw, subBottom, borderPaint)
                        kx += cw
                    }
                    subTop = subBottom
                }

                cursorY = rowBottom
            }
        }

        private fun drawPersonBlocksCell(
            canvas: Canvas,
            blocks: List<PersonBlockLayout>,
            cellLeft: Float,
            cellTop: Float,
            cellWidth: Float,
            cellHeight: Float
        ) {
            val totalBlockH = blocksTotalHeight(blocks)
            var y = cellTop + max(4f, (cellHeight - totalBlockH) / 2f)
            blocks.forEachIndexed { idx, block ->
                drawStaticLayout(canvas, block.layout, cellLeft + 4f, y)
                y += block.layout.height
                if (idx < blocks.lastIndex) {
                    val lineY = y + 3f
                    canvas.drawLine(cellLeft + 4f, lineY, cellLeft + cellWidth - 4f, lineY, dottedPaint)
                    y += 6f
                }
            }
        }

        private fun drawStaticLayout(canvas: Canvas, layout: StaticLayout, x: Float, y: Float) {
            canvas.save()
            canvas.translate(x, y)
            layout.draw(canvas)
            canvas.restore()
        }

        private fun buildStaticLayout(
            text: CharSequence,
            paint: TextPaint,
            width: Int,
            alignment: Layout.Alignment
        ): StaticLayout {
            return StaticLayout.Builder
                .obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
                .setAlignment(alignment)
                .setLineSpacing(0f, 1.18f)
                .setIncludePad(false)
                .build()
        }

        private fun SpannableStringBuilder.appendBold(text: String) {
            val start = length
            append(text)
            setSpan(StyleSpan(Typeface.BOLD), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        private fun containsPage(ranges: Array<PageRange>, pageIndex: Int): Boolean {
            for (range in ranges) {
                if (pageIndex >= range.start && pageIndex <= range.end) {
                    return true
                }
            }
            return false
        }
    }
}
