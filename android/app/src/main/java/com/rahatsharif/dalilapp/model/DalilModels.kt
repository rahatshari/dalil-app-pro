package com.rahatsharif.dalilapp.model

import java.util.UUID

data class PersonEntry(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var relation: String = "পিতা", // পিতা, স্বামী, মাতা
    var guardian: String = "",
    var mother: String = "",
    var land: String = ""
)

data class KhatianEntry(
    val id: String = UUID.randomUUID().toString(),
    var type: String = "এস এ", // সি এস, এস এ, আর এস, বি এস, সিটি
    var no: String = "",
    var land: String = "",
    var dag: String = ""
)

data class DalilRecord(
    val id: String = UUID.randomUUID().toString(),
    var serial: Int = 1,
    var date: String = "",
    var dalilNo: String = "",
    var receivers: MutableList<PersonEntry> = mutableListOf(PersonEntry()),
    var donors: MutableList<PersonEntry> = mutableListOf(PersonEntry()),
    var khatians: MutableList<KhatianEntry> = mutableListOf(KhatianEntry()),
    var remarks: String = ""
) {
    fun calculateTotalLand(): Double {
        // sum land from receivers or khatians
        return receivers.mapNotNull { it.land.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() }.sum()
    }
}

data class GlobalHeader(
    var district: String = "",
    var upazila: String = "",
    var mouza: String = "",
    var jlNo: String = "",
    var khatianNo: String = "",
    var dalilYear: String = "",
    var isVisible: Boolean = true
)

data class DalilProject(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "নতুন দলিল প্রজেক্ট",
    var updatedAt: Long = System.currentTimeMillis(),
    var header: GlobalHeader = GlobalHeader(),
    var records: MutableList<DalilRecord> = mutableListOf()
)
