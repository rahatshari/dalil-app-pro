package com.rahatsharif.dalilapp.model

import java.util.UUID

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

data class PersonEntry(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var relation: String = "পিতা", // পিতা, স্বামী
    var guardian: String = "",
    var mother: String = "",
    var land: String = ""
)

data class KhatianEntry(
    val id: String = UUID.randomUUID().toString(),
    var type: String = "এস এ", // এস এ, সি এস, বি আর এস, আর এস
    var no: String = "",
    var land: String = "",
    var dag: String = ""
)

data class DalilRecord(
    val id: String = UUID.randomUUID().toString(),
    var serial: Int = 1,
    var dalilNo: String = "",
    var date: String = "",
    var receivers: MutableList<PersonEntry> = mutableListOf(PersonEntry()),
    var donors: MutableList<PersonEntry> = mutableListOf(PersonEntry()),
    var totalLand: String = "",
    var khatians: MutableList<KhatianEntry> = mutableListOf(KhatianEntry()),
    var remarks: String = ""
)

data class GlobalHeader(
    var district: String = "বাগেরহাট",
    var upazila: String = "মোড়েলগঞ্জ",
    var mouza: String = "৯৩ পাঠামারা",
    var creator: String = "মোঃ আব্দুল্লাহ আল রাহাত",
    var date: String = ""
)

data class SavedProject(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "দলিল প্রজেক্ট",
    var savedAt: String = "",
    var updatedAt: Long = System.currentTimeMillis(),
    var header: GlobalHeader = GlobalHeader(),
    var records: MutableList<DalilRecord> = mutableListOf()
)

data class PrintSettings(
    var orientation: String = "portrait", // portrait | landscape
    var marginMode: String = "normal", // narrow | normal | wide
    var showOuterBorder: Boolean = false,
    var previewZoomMode: String = "fit" // fit | full
)

data class ParsedBackupFile(
    val filename: String,
    val valid: Boolean,
    val records: List<DalilRecord> = emptyList(),
    val header: GlobalHeader? = null,
    val projectName: String? = null,
    val error: String? = null
)
