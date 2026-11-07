package com.rahatsharif.dalilapp.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rahatsharif.dalilapp.model.DalilProject
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
        val sb = StringBuilder()
        for (char in str) {
            sb.append(enToBnMap[char] ?: char)
        }
        return sb.toString()
    }

    fun toEnglishNum(str: String?): String {
        if (str == null) return ""
        val sb = StringBuilder()
        for (char in str) {
            sb.append(bnToEnMap[char] ?: char)
        }
        return sb.toString()
    }

    fun formatShatak(value: String?): String {
        if (value.isNullOrBlank()) return "উল্লেখ নেই"
        val trimmed = value.trim()
        val numOnly = toEnglishNum(trimmed).replace("[^0-9.]".toRegex(), "")
        return if (numOnly.isNotEmpty()) {
            "${toBanglaNum(numOnly)} শতক"
        } else {
            trimmed
        }
    }

    fun getTodayDateBangla(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return toBanglaNum(sdf.format(Date()))
    }

    fun formatDateAuto(input: String): String {
        val cleaned = toEnglishNum(input).replace("[^0-9]".toRegex(), "")
        return when {
            cleaned.length >= 8 -> {
                val d = cleaned.substring(0, 2)
                val m = cleaned.substring(2, 4)
                val y = cleaned.substring(4, 8)
                toBanglaNum("$d/$m/$y")
            }
            cleaned.length >= 4 -> {
                val d = cleaned.substring(0, 2)
                val m = cleaned.substring(2, 4)
                val rest = cleaned.substring(4)
                toBanglaNum("$d/$m/$rest")
            }
            cleaned.length >= 2 -> {
                val d = cleaned.substring(0, 2)
                val rest = cleaned.substring(2)
                toBanglaNum("$d/$rest")
            }
            else -> toBanglaNum(cleaned)
        }
    }

    fun projectToJson(project: DalilProject): String {
        return Gson().toJson(project)
    }

    fun jsonToProject(json: String): DalilProject? {
        return try {
            Gson().fromJson(json, DalilProject::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
