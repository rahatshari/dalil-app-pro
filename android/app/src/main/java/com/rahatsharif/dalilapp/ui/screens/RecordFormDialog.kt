package com.rahatsharif.dalilapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rahatsharif.dalilapp.model.DalilRecord
import com.rahatsharif.dalilapp.model.KhatianEntry
import com.rahatsharif.dalilapp.model.PersonEntry
import com.rahatsharif.dalilapp.ui.theme.PrimaryBlue
import com.rahatsharif.dalilapp.ui.theme.RedDelete
import com.rahatsharif.dalilapp.ui.theme.SecondaryTeal
import com.rahatsharif.dalilapp.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordFormDialog(
    initialRecord: DalilRecord? = null,
    onDismiss: () -> Unit,
    onSave: (DalilRecord) -> Unit
) {
    var date by remember { mutableStateOf(initialRecord?.date ?: "") }
    var dalilNo by remember { mutableStateOf(initialRecord?.dalilNo ?: "") }
    var remarks by remember { mutableStateOf(initialRecord?.remarks ?: "") }

    val receivers = remember {
        mutableStateListOf<PersonEntry>().apply {
            if (initialRecord != null && initialRecord.receivers.isNotEmpty()) {
                addAll(initialRecord.receivers.map { it.copy() })
            } else {
                add(PersonEntry())
            }
        }
    }

    val donors = remember {
        mutableStateListOf<PersonEntry>().apply {
            if (initialRecord != null && initialRecord.donors.isNotEmpty()) {
                addAll(initialRecord.donors.map { it.copy() })
            } else {
                add(PersonEntry())
            }
        }
    }

    val khatians = remember {
        mutableStateListOf<KhatianEntry>().apply {
            if (initialRecord != null && initialRecord.khatians.isNotEmpty()) {
                addAll(initialRecord.khatians.map { it.copy() })
            } else {
                add(KhatianEntry())
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialRecord == null) "নতুন দলিল তথ্য যোগ" else "দলিল তথ্য পরিবর্তন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Date & Dalil No Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = BanglaUtils.formatDateAuto(it) },
                            label = { Text("তারিখ (দিন/মাস/বছর)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { date = BanglaUtils.getTodayDateBangla() }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "আজকের তারিখ", tint = PrimaryBlue)
                                }
                            }
                        )

                        OutlinedTextField(
                            value = dalilNo,
                            onValueChange = { dalilNo = BanglaUtils.toBanglaNum(it) },
                            label = { Text("দলিল নং") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Receivers Section (গ্রহীতা)
                    SectionCard(
                        title = "গ্রহীতার বিবরণ",
                        onAdd = { receivers.add(PersonEntry()) }
                    ) {
                        receivers.forEachIndexed { index, person ->
                            PersonItemRow(
                                title = "গ্রহীতা ${BanglaUtils.toBanglaNum(index + 1)}",
                                person = person,
                                canDelete = receivers.size > 1,
                                onDelete = { receivers.removeAt(index) }
                            )
                        }
                    }

                    // Donors Section (দাতা)
                    SectionCard(
                        title = "দাতার বিবরণ",
                        onAdd = { donors.add(PersonEntry()) }
                    ) {
                        donors.forEachIndexed { index, person ->
                            PersonItemRow(
                                title = "দাতা ${BanglaUtils.toBanglaNum(index + 1)}",
                                person = person,
                                canDelete = donors.size > 1,
                                onDelete = { donors.removeAt(index) }
                            )
                        }
                    }

                    // Khatians & Dag Section
                    SectionCard(
                        title = "খতিয়ান ও দাগের বিবরণ",
                        onAdd = { khatians.add(KhatianEntry()) }
                    ) {
                        khatians.forEachIndexed { index, khatian ->
                            KhatianItemRow(
                                title = "খতিয়ান ${BanglaUtils.toBanglaNum(index + 1)}",
                                khatian = khatian,
                                canDelete = khatians.size > 1,
                                onDelete = { khatians.removeAt(index) }
                            )
                        }
                    }

                    // Remarks
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("মন্তব্য (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val record = DalilRecord(
                                id = initialRecord?.id ?: java.util.UUID.randomUUID().toString(),
                                serial = initialRecord?.serial ?: 1,
                                date = date,
                                dalilNo = dalilNo,
                                receivers = receivers.toMutableList(),
                                donors = donors.toMutableList(),
                                khatians = khatians.toMutableList(),
                                remarks = remarks
                            )
                            onSave(record)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(if (initialRecord == null) "যোগ করুন" else "সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    onAdd: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = PrimaryBlue)
                IconButton(onClick = onAdd, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "যোগ করুন", tint = SecondaryTeal)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
fun PersonItemRow(
    title: String,
    person: PersonEntry,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(person.name) }
    var rel by remember { mutableStateOf(person.relation) }
    var guardian by remember { mutableStateOf(person.guardian) }
    var land by remember { mutableStateOf(person.land) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = Color.Gray)
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = RedDelete, modifier = Modifier.size(16.dp))
                    }
                }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; person.name = it },
                label = { Text("নাম") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = guardian,
                    onValueChange = { guardian = it; person.guardian = it },
                    label = { Text("পিতা / স্বামীর নাম") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = land,
                    onValueChange = { land = BanglaUtils.toBanglaNum(it); person.land = land },
                    label = { Text("জমি (শতক)") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}

@Composable
fun KhatianItemRow(
    title: String,
    khatian: KhatianEntry,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    var type by remember { mutableStateOf(khatian.type) }
    var no by remember { mutableStateOf(khatian.no) }
    var dag by remember { mutableStateOf(khatian.dag) }
    var land by remember { mutableStateOf(khatian.land) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = Color.Gray)
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = RedDelete, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it; khatian.type = it },
                    label = { Text("ধরণ (সিএস/এসএ/আরএস)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = no,
                    onValueChange = { no = BanglaUtils.toBanglaNum(it); khatian.no = no },
                    label = { Text("খতিয়ান নং") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dag,
                    onValueChange = { dag = BanglaUtils.toBanglaNum(it); khatian.dag = dag },
                    label = { Text("দাগ নং") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = land,
                    onValueChange = { land = BanglaUtils.toBanglaNum(it); khatian.land = land },
                    label = { Text("জমি (শতক)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}
