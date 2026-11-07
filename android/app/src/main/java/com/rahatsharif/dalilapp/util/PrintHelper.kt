package com.rahatsharif.dalilapp.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.rahatsharif.dalilapp.model.DalilProject
import com.rahatsharif.dalilapp.model.DalilRecord

object PrintHelper {

    fun generatePrintHtml(project: DalilProject, orientation: String = "portrait"): String {
        val h = project.header
        val records = project.records

        val sb = StringBuilder()
        sb.append("""
            <!DOCTYPE html>
            <html lang="bn">
            <head>
                <meta charset="UTF-8">
                <style>
                    @page { size: A4 $orientation; margin: 12mm; }
                    body { font-family: 'Roboto', 'Noto Sans Bengali', sans-serif; margin: 0; padding: 0; color: #111; font-size: 13px; }
                    .header-box { text-align: center; border-bottom: 2px solid #333; padding-bottom: 8px; margin-bottom: 12px; }
                    .header-title { font-size: 20px; font-weight: bold; margin: 0 0 6px 0; color: #1a73e8; }
                    .header-meta { display: flex; justify-content: center; flex-wrap: wrap; gap: 16px; font-size: 14px; font-weight: 500; }
                    table { width: 100%; border-collapse: collapse; margin-top: 8px; }
                    th, td { border: 1px solid #444; padding: 6px 8px; text-align: left; vertical-align: top; }
                    th { background-color: #f1f3f4; font-weight: bold; font-size: 13px; }
                    .person-item { margin-bottom: 4px; padding-bottom: 4px; border-bottom: 1px dashed #ccc; }
                    .person-item:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
                    .total-box { margin-top: 14px; text-align: right; font-weight: bold; font-size: 14px; }
                    .shatak-badge { color: #0d9488; font-weight: bold; }
                </style>
            </head>
            <body>
        """.trimIndent())

        if (h.isVisible) {
            sb.append("""
                <div class="header-box">
                    <div class="header-title">${project.name}</div>
                    <div class="header-meta">
                        ${if (h.district.isNotBlank()) "<span>জেলা: <b>${h.district}</b></span>" else ""}
                        ${if (h.upazila.isNotBlank()) "<span>উপজেলা: <b>${h.upazila}</b></span>" else ""}
                        ${if (h.mouza.isNotBlank()) "<span>মৌজা: <b>${h.mouza}</b></span>" else ""}
                        ${if (h.jlNo.isNotBlank()) "<span>জে.এল: <b>${BanglaUtils.toBanglaNum(h.jlNo)}</b></span>" else ""}
                        ${if (h.khatianNo.isNotBlank()) "<span>খতিয়ান: <b>${BanglaUtils.toBanglaNum(h.khatianNo)}</b></span>" else ""}
                        ${if (h.dalilYear.isNotBlank()) "<span>সাল: <b>${BanglaUtils.toBanglaNum(h.dalilYear)}</b></span>" else ""}
                    </div>
                </div>
            """.trimIndent())
        }

        sb.append("""
            <table>
                <thead>
                    <tr>
                        <th style="width: 5%; text-align: center;">নং</th>
                        <th style="width: 15%;">দলিল ও তারিখ</th>
                        <th style="width: 25%;">গ্রহীতা</th>
                        <th style="width: 25%;">দাতা</th>
                        <th style="width: 18%;">খতিয়ান ও দাগ</th>
                        <th style="width: 12%;">মন্তব্য</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        records.forEachIndexed { index, rec ->
            val bnSerial = BanglaUtils.toBanglaNum(index + 1)
            val bnDate = BanglaUtils.toBanglaNum(rec.date)
            val bnDalil = BanglaUtils.toBanglaNum(rec.dalilNo)

            val receiversHtml = rec.receivers.joinToString("") { p ->
                val landStr = if (p.land.isNotBlank()) "<br><span class='shatak-badge'>জমি: ${BanglaUtils.formatShatak(p.land)}</span>" else ""
                val motherStr = if (p.mother.isNotBlank()) ", মাতা: ${p.mother}" else ""
                "<div class='person-item'><b>${p.name}</b><br><small>${p.relation}: ${p.guardian}$motherStr$landStr</small></div>"
            }

            val donorsHtml = rec.donors.joinToString("") { p ->
                val landStr = if (p.land.isNotBlank()) "<br><span class='shatak-badge'>জমি: ${BanglaUtils.formatShatak(p.land)}</span>" else ""
                val motherStr = if (p.mother.isNotBlank()) ", মাতা: ${p.mother}" else ""
                "<div class='person-item'><b>${p.name}</b><br><small>${p.relation}: ${p.guardian}$motherStr$landStr</small></div>"
            }

            val khatianHtml = rec.khatians.joinToString("") { k ->
                val dagStr = if (k.dag.isNotBlank()) " দাগ: ${BanglaUtils.toBanglaNum(k.dag)}" else ""
                val landStr = if (k.land.isNotBlank()) " (${BanglaUtils.formatShatak(k.land)})" else ""
                "<div class='person-item'><small>${k.type}: <b>${BanglaUtils.toBanglaNum(k.no)}</b>$dagStr$landStr</small></div>"
            }

            sb.append("""
                <tr>
                    <td style="text-align: center; font-weight: bold;">$bnSerial</td>
                    <td><b>দলিল: $bnDalil</b><br><small>তারিখ: $bnDate</small></td>
                    <td>$receiversHtml</td>
                    <td>$donorsHtml</td>
                    <td>$khatianHtml</td>
                    <td><small>${rec.remarks}</small></td>
                </tr>
            """.trimIndent())
        }

        sb.append("""
                </tbody>
            </table>
            <div class="total-box">মোট দলিল সংখ্যা: ${BanglaUtils.toBanglaNum(records.size)} টি</div>
            </body>
            </html>
        """.trimIndent())

        return sb.toString()
    }

    fun printDocument(context: Context, htmlContent: String, jobName: String = "Dalil_Figure") {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()

                printManager?.print(jobName, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
    }
}
