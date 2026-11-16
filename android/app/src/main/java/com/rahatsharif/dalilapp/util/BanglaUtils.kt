package com.rahatsharif.dalilapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BanglaUtils {

    private val enToBnMap = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )

    private val bnToEnMap = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    fun toBanglaNum(value: Any?): String {
        if (value == null) return ""
        val str = value.toString()
        if (str.isEmpty()) return ""
        val sb = StringBuilder(str.length)
        for (char in str) {
            sb.append(enToBnMap[char] ?: char)
        }
        return sb.toString()
    }

    fun toEnglishNum(str: String?): String {
        if (str.isNullOrEmpty()) return ""
        val sb = StringBuilder(str.length)
        for (char in str) {
            sb.append(bnToEnMap[char] ?: char)
        }
        return sb.toString()
    }

    fun cleanShatakForEdit(value: String?): String {
        if (value.isNullOrBlank() || value.trim() == "-") return ""
        return toBanglaNum(
            value.replace("শতক", "")
                .replace("জমি:", "")
                .replace("বিক্রি:", "")
                .trim()
        )
    }

    fun formatShatak(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        val cleaned = cleanShatakForEdit(value)
        if (cleaned.isBlank() || cleaned == "-") return "-"
        return "$cleaned শতক"
    }

    fun parseNumericShatak(value: String?): Double? {
        if (value.isNullOrBlank()) return null
        val en = toEnglishNum(cleanShatakForEdit(value))
            .replace("[^0-9.]".toRegex(), "")
        return en.toDoubleOrNull()
    }

    fun formatDoubleBangla(value: Double): String {
        val rounded = if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
        }
        return toBanglaNum(rounded)
    }

    fun getTodayDateBangla(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return toBanglaNum(sdf.format(Date()))
    }

    fun getTodayTimestampBangla(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault())
        return toBanglaNum(sdf.format(Date()))
    }

    fun formatDateAuto(input: String): String {
        val cleaned = toEnglishNum(input).replace("[^0-9]".toRegex(), "")
        val capped = if (cleaned.length > 8) cleaned.substring(0, 8) else cleaned
        return when {
            capped.isEmpty() -> ""
            capped.length < 2 -> toBanglaNum(capped)
            capped.length == 2 -> toBanglaNum(capped) + "/"
            capped.length < 4 -> toBanglaNum(capped.substring(0, 2) + "/" + capped.substring(2))
            capped.length == 4 -> toBanglaNum(capped.substring(0, 2) + "/" + capped.substring(2, 4) + "/")
            else -> toBanglaNum(
                capped.substring(0, 2) + "/" + capped.substring(2, 4) + "/" + capped.substring(4)
            )
        }
    }

    fun formatDagList(raw: String): String {
        if (raw.isBlank() || raw.trim() == "-") return "-"
        return toBanglaNum(raw.trim().replace(",\\s*".toRegex(), ", "))
    }

    fun stripHtmlLines(html: String?): List<String> {
        if (html.isNullOrBlank()) return emptyList()
        return html
            .replace("<br\\s*/?>".toRegex(RegexOption.IGNORE_CASE), "\n")
            .replace("</div>", "\n")
            .replace("<[^>]*>".toRegex(), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
