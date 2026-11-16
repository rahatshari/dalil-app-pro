package com.rahatsharif.dalilapp.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.rahatsharif.dalilapp.model.DalilRecord
import com.rahatsharif.dalilapp.model.GlobalHeader
import com.rahatsharif.dalilapp.model.KhatianEntry
import com.rahatsharif.dalilapp.model.ParsedBackupFile
import com.rahatsharif.dalilapp.model.PersonEntry
import com.rahatsharif.dalilapp.model.PrintSettings
import com.rahatsharif.dalilapp.model.SavedProject
import com.rahatsharif.dalilapp.model.ThemePreference
import java.util.UUID

class DalilRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "dalil_figure_native_prefs"
        private const val KEY_RECORDS = "dalil_records_v1"
        private const val KEY_HEADER = "dalil_header_v1"
        private const val KEY_SAVED_PROJECTS = "dalil_saved_projects_v1"
        private const val KEY_DRAFT = "dalil_form_draft_v1"
        private const val KEY_PRINT_SETTINGS = "dalil_print_settings_v1"
        private const val KEY_MOTHER_PREF = "mother_name_pref"
        private const val KEY_THEME_PREF = "theme_pref"
        private const val KEY_AUTO_UPDATE = "auto_update_enabled"
    }

    fun loadRecords(): MutableList<DalilRecord> {
        val raw = prefs.getString(KEY_RECORDS, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<DalilRecord>>() {}.type
            val list: MutableList<DalilRecord>? = gson.fromJson(raw, type)
            list?.onEachIndexed { index, r -> r.serial = index + 1 } ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveRecords(records: List<DalilRecord>) {
        val normalized = records.mapIndexed { idx, r -> r.copy(serial = idx + 1) }
        prefs.edit().putString(KEY_RECORDS, gson.toJson(normalized)).apply()
    }

    fun loadHeader(): GlobalHeader {
        val raw = prefs.getString(KEY_HEADER, null)
        val today = BanglaUtils.getTodayDateBangla()
        if (raw.isNullOrBlank()) {
            return GlobalHeader(date = today)
        }
        return try {
            val loaded = gson.fromJson(raw, GlobalHeader::class.java) ?: GlobalHeader(date = today)
            if (loaded.date.isBlank()) loaded.copy(date = today) else loaded
        } catch (_: Exception) {
            GlobalHeader(date = today)
        }
    }

    fun saveHeader(header: GlobalHeader) {
        prefs.edit().putString(KEY_HEADER, gson.toJson(header)).apply()
    }

    fun loadSavedProjects(): MutableList<SavedProject> {
        val raw = prefs.getString(KEY_SAVED_PROJECTS, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<SavedProject>>() {}.type
            gson.fromJson(raw, type) ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveSavedProjects(projects: List<SavedProject>) {
        prefs.edit().putString(KEY_SAVED_PROJECTS, gson.toJson(projects)).apply()
    }

    fun loadDraft(): DalilRecord? {
        val raw = prefs.getString(KEY_DRAFT, null) ?: return null
        return try {
            gson.fromJson(raw, DalilRecord::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun saveDraft(draft: DalilRecord?) {
        if (draft == null) {
            prefs.edit().remove(KEY_DRAFT).apply()
        } else {
            prefs.edit().putString(KEY_DRAFT, gson.toJson(draft)).apply()
        }
    }

    fun loadPrintSettings(): PrintSettings {
        val raw = prefs.getString(KEY_PRINT_SETTINGS, null) ?: return PrintSettings()
        return try {
            gson.fromJson(raw, PrintSettings::class.java) ?: PrintSettings()
        } catch (_: Exception) {
            PrintSettings()
        }
    }

    fun savePrintSettings(settings: PrintSettings) {
        prefs.edit().putString(KEY_PRINT_SETTINGS, gson.toJson(settings)).apply()
    }

    fun isMotherNameEnabled(): Boolean = prefs.getBoolean(KEY_MOTHER_PREF, false)

    fun setMotherNameEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MOTHER_PREF, enabled).apply()
    }

    fun getThemePreference(): ThemePreference {
        val raw = prefs.getString(KEY_THEME_PREF, ThemePreference.SYSTEM.name)
        return try {
            ThemePreference.valueOf(raw ?: ThemePreference.SYSTEM.name)
        } catch (_: Exception) {
            ThemePreference.SYSTEM
        }
    }

    fun setThemePreference(pref: ThemePreference) {
        prefs.edit().putString(KEY_THEME_PREF, pref.name).apply()
    }

    fun isAutoUpdateEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_UPDATE, true)

    fun setAutoUpdateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_UPDATE, enabled).apply()
    }

    /**
     * Exports current project in a JSON format compatible with BOTH the Native App and the Web App.
     */
    fun exportRecordsJson(header: GlobalHeader, records: List<DalilRecord>, projectName: String = "দলিল ব্যাকআপ"): String {
        val root = JsonObject()
        root.addProperty("projectName", projectName)
        root.addProperty("exportedAt", System.currentTimeMillis())

        val headerObj = JsonObject().apply {
            addProperty("district", header.district)
            addProperty("upazila", header.upazila)
            addProperty("mouza", header.mouza)
            addProperty("creator", header.creator)
            addProperty("date", header.date)
        }
        root.add("header", headerObj)

        val webRecordsArr = JsonArray()
        records.forEachIndexed { idx, rec ->
            val obj = JsonObject()
            obj.addProperty("sl_no", BanglaUtils.toBanglaNum(idx + 1))
            obj.addProperty("dalil_no", rec.dalilNo)
            obj.addProperty("dateVal", rec.date)
            obj.addProperty("total_land", rec.totalLand)
            obj.addProperty("remarks", rec.remarks)
            obj.addProperty("receiversHTML", buildPeopleHtml(rec.receivers, "জমি"))
            obj.addProperty("donorsHTML", buildPeopleHtml(rec.donors, "বিক্রি"))
            obj.add("rawReceivers", buildPeopleJsonArray(rec.receivers))
            obj.add("rawDonors", buildPeopleJsonArray(rec.donors))

            val khArr = JsonArray()
            rec.khatians.forEach { k ->
                val kObj = JsonObject().apply {
                    addProperty("type", k.type)
                    addProperty("no", k.no)
                    addProperty("land", k.land)
                    addProperty("dag", k.dag)
                }
                khArr.add(kObj)
            }
            obj.add("khatians", khArr)
            webRecordsArr.add(obj)
        }

        root.add("dalilRecords", webRecordsArr)
        return gson.toJson(root)
    }

    private fun buildPeopleJsonArray(people: List<PersonEntry>): JsonArray {
        val arr = JsonArray()
        people.forEach { p ->
            val obj = JsonObject().apply {
                addProperty("name", p.name)
                addProperty("relationType", p.relation)
                addProperty("father", p.guardian)
                addProperty("mother", p.mother)
                addProperty("land", BanglaUtils.cleanShatakForEdit(p.land))
            }
            arr.add(obj)
        }
        return arr
    }

    private fun buildPeopleHtml(people: List<PersonEntry>, landPrefix: String): String {
        if (people.isEmpty()) return "-"
        val count = people.size
        return people.mapIndexed { idx, p ->
            val prefix = if (count > 1) "${BanglaUtils.toBanglaNum(idx + 1)}. " else ""
            val parts = mutableListOf<String>()
            parts.add("<b>$prefix${p.name.ifBlank { "-" }}</b>")
            if (p.guardian.isNotBlank()) parts.add("${p.relation}: ${p.guardian}")
            if (p.mother.isNotBlank()) parts.add("মাতা: ${p.mother}")
            val cleanedLand = BanglaUtils.cleanShatakForEdit(p.land)
            if (cleanedLand.isNotBlank()) parts.add("$landPrefix: ${BanglaUtils.formatShatak(cleanedLand)}")
            "<div class=\"person-data\">${parts.joinToString("<br>")}</div>"
        }.joinToString("")
    }

    fun readBackupFileFromUri(context: Context, uri: Uri): ParsedBackupFile {
        val filename = resolveFilename(context, uri)
        return try {
            val rawText = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                it.readText()
            } ?: return ParsedBackupFile(filename, false, error = "ফাইলটি পড়া যায়নি")
            parseBackupJson(rawText, filename)
        } catch (e: Exception) {
            ParsedBackupFile(filename, false, error = "ফাইল পড়তে ত্রুটি: ${e.message ?: "অজানা ত্রুটি"}")
        }
    }

    private fun resolveFilename(context: Context, uri: Uri): String {
        var name: String? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) {
                    name = cursor.getString(idx)
                }
            }
        } catch (_: Exception) { }
        return name ?: uri.lastPathSegment ?: "backup.json"
    }

    fun parseBackupJson(rawText: String, filename: String = "backup.json"): ParsedBackupFile {
        if (rawText.isBlank()) {
            return ParsedBackupFile(filename, false, error = "ফাইলটি খালি বা পড়া যায়নি")
        }
        return try {
            val rootEl: JsonElement = JsonParser.parseString(rawText)
            var rawArray: JsonArray? = null
            var header: GlobalHeader? = null
            var projectName: String? = null

            when {
                rootEl.isJsonArray -> {
                    rawArray = rootEl.asJsonArray
                }
                rootEl.isJsonObject -> {
                    val obj = rootEl.asJsonObject
                    projectName = getStr(obj, "projectName") ?: getStr(obj, "name")
                    if (obj.has("header") && obj.get("header").isJsonObject) {
                        val hObj = obj.getAsJsonObject("header")
                        header = GlobalHeader(
                            district = getStr(hObj, "district") ?: "বাগেরহাট",
                            upazila = getStr(hObj, "upazila") ?: "মোড়েলগঞ্জ",
                            mouza = getStr(hObj, "mouza") ?: "৯৩ পাঠামারা",
                            creator = getStr(hObj, "creator") ?: "মোঃ আব্দুল্লাহ আল রাহাত",
                            date = getStr(hObj, "date") ?: BanglaUtils.getTodayDateBangla()
                        )
                    }
                    rawArray = when {
                        obj.has("dalilRecords") && obj.get("dalilRecords").isJsonArray -> obj.getAsJsonArray("dalilRecords")
                        obj.has("records") && obj.get("records").isJsonArray -> obj.getAsJsonArray("records")
                        obj.has("dalil_no") || obj.has("dalilNo") || obj.has("khatians") -> JsonArray().apply { add(obj) }
                        else -> null
                    }
                }
            }

            if (rawArray == null || rawArray.size() == 0) {
                return ParsedBackupFile(filename, false, error = "ফাইলে কোনো সঠিক দলিলের তথ্য পাওয়া যায়নি")
            }

            val parsedRecords = mutableListOf<DalilRecord>()
            for (el in rawArray) {
                if (!el.isJsonObject) continue
                val rObj = el.asJsonObject
                val dalilNo = getStr(rObj, "dalil_no") ?: getStr(rObj, "dalilNo") ?: ""
                val dateVal = getStr(rObj, "dateVal") ?: getStr(rObj, "date") ?: ""
                val totalLandRaw = getStr(rObj, "total_land") ?: getStr(rObj, "totalLand") ?: ""
                val remarks = getStr(rObj, "remarks") ?: ""

                val receivers = parsePeopleList(
                    arr = rObj.get("rawReceivers")?.takeIf { it.isJsonArray }?.asJsonArray
                        ?: rObj.get("receivers")?.takeIf { it.isJsonArray }?.asJsonArray,
                    fallbackHtml = getStr(rObj, "receiversHTML")
                )
                val donors = parsePeopleList(
                    arr = rObj.get("rawDonors")?.takeIf { it.isJsonArray }?.asJsonArray
                        ?: rObj.get("donors")?.takeIf { it.isJsonArray }?.asJsonArray,
                    fallbackHtml = getStr(rObj, "donorsHTML")
                )

                val khatians = mutableListOf<KhatianEntry>()
                val khArr = rObj.get("khatians")?.takeIf { it.isJsonArray }?.asJsonArray
                if (khArr != null && khArr.size() > 0) {
                    for (kEl in khArr) {
                        if (!kEl.isJsonObject) continue
                        val kObj = kEl.asJsonObject
                        khatians.add(
                            KhatianEntry(
                                id = getStr(kObj, "id") ?: UUID.randomUUID().toString(),
                                type = getStr(kObj, "type") ?: "",
                                no = getStr(kObj, "no") ?: "-",
                                land = BanglaUtils.cleanShatakForEdit(getStr(kObj, "land")),
                                dag = getStr(kObj, "dag") ?: "-"
                            )
                        )
                    }
                }
                if (khatians.isEmpty()) khatians.add(KhatianEntry())

                if (dalilNo.isNotBlank() || dateVal.isNotBlank() || receivers.isNotEmpty() || khatians.any { it.no != "-" }) {
                    parsedRecords.add(
                        DalilRecord(
                            id = getStr(rObj, "id") ?: UUID.randomUUID().toString(),
                            serial = parsedRecords.size + 1,
                            dalilNo = BanglaUtils.toBanglaNum(dalilNo),
                            date = BanglaUtils.toBanglaNum(dateVal),
                            receivers = if (receivers.isEmpty()) mutableListOf(PersonEntry()) else receivers.toMutableList(),
                            donors = if (donors.isEmpty()) mutableListOf(PersonEntry()) else donors.toMutableList(),
                            totalLand = BanglaUtils.cleanShatakForEdit(totalLandRaw),
                            khatians = khatians,
                            remarks = remarks
                        )
                    )
                }
            }

            if (parsedRecords.isEmpty()) {
                ParsedBackupFile(filename, false, error = "ফাইলে কোনো সঠিক দলিলের তথ্য পাওয়া যায়নি")
            } else {
                ParsedBackupFile(
                    filename = filename,
                    valid = true,
                    records = parsedRecords,
                    header = header,
                    projectName = projectName
                )
            }
        } catch (_: Exception) {
            ParsedBackupFile(filename, false, error = "ফাইলটি সঠিক JSON ফরম্যাটে নেই")
        }
    }

    private fun parsePeopleList(arr: JsonArray?, fallbackHtml: String?): List<PersonEntry> {
        val list = mutableListOf<PersonEntry>()
        if (arr != null && arr.size() > 0) {
            for (el in arr) {
                if (!el.isJsonObject) continue
                val pObj = el.asJsonObject
                val name = getStr(pObj, "name") ?: ""
                val rel = getStr(pObj, "relationType") ?: getStr(pObj, "relation") ?: "পিতা"
                val father = getStr(pObj, "father") ?: getStr(pObj, "guardian") ?: ""
                val mother = getStr(pObj, "mother") ?: ""
                val land = BanglaUtils.cleanShatakForEdit(getStr(pObj, "land"))
                if (name.isNotBlank() || father.isNotBlank() || land.isNotBlank()) {
                    list.add(
                        PersonEntry(
                            id = getStr(pObj, "id") ?: UUID.randomUUID().toString(),
                            name = name,
                            relation = if (rel == "স্বামী") "স্বামী" else "পিতা",
                            guardian = father,
                            mother = mother,
                            land = land
                        )
                    )
                }
            }
        } else if (!fallbackHtml.isNullOrBlank()) {
            val lines = BanglaUtils.stripHtmlLines(fallbackHtml)
            if (lines.isNotEmpty()) {
                list.add(PersonEntry(name = lines.joinToString("\n")))
            }
        }
        return list
    }

    private fun getStr(obj: JsonObject, key: String): String? {
        val el = obj.get(key) ?: return null
        if (el.isJsonNull) return null
        return try {
            el.asString
        } catch (_: Exception) {
            el.toString()
        }
    }

    fun mergeRecords(
        current: List<DalilRecord>,
        incoming: List<DalilRecord>,
        skipDuplicates: Boolean
    ): Triple<List<DalilRecord>, Int, Int> {
        if (!skipDuplicates) {
            val merged = (current + incoming).mapIndexed { idx, r ->
                r.copy(id = UUID.randomUUID().toString(), serial = idx + 1)
            }
            return Triple(merged, incoming.size, 0)
        }

        val existingNos = current
            .map { BanglaUtils.toEnglishNum(it.dalilNo).trim().lowercase() }
            .filter { it.isNotEmpty() && it != "-" }
            .toMutableSet()

        val toAdd = mutableListOf<DalilRecord>()
        var duplicateCount = 0

        for (r in incoming) {
            val dNo = BanglaUtils.toEnglishNum(r.dalilNo).trim().lowercase()
            if (dNo.isNotEmpty() && dNo != "-" && existingNos.contains(dNo)) {
                duplicateCount++
            } else {
                if (dNo.isNotEmpty() && dNo != "-") existingNos.add(dNo)
                toAdd.add(r.copy(id = UUID.randomUUID().toString()))
            }
        }

        val merged = (current + toAdd).mapIndexed { idx, r -> r.copy(serial = idx + 1) }
        return Triple(merged, toAdd.size, duplicateCount)
    }
}
