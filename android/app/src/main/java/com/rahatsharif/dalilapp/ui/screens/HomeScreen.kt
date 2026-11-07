package com.rahatsharif.dalilapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahatsharif.dalilapp.model.DalilProject
import com.rahatsharif.dalilapp.model.DalilRecord
import com.rahatsharif.dalilapp.ui.theme.PrimaryBlue
import com.rahatsharif.dalilapp.ui.theme.RedDelete
import com.rahatsharif.dalilapp.ui.theme.SecondaryTeal
import com.rahatsharif.dalilapp.util.BanglaUtils
import com.rahatsharif.dalilapp.util.PrintHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    project: DalilProject,
    currentVersion: String = "1.0.0",
    onProjectChange: () -> Unit,
    onOpenWebViewMode: () -> Unit,
    onCheckUpdatesManual: () -> Unit = {}
) {
    val context = LocalContext.current
    var showFormDialog by remember { mutableStateOf(false) }
    var editingRecordIndex by remember { mutableStateOf<Int?>(null) }
    var showHeaderSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "দলিল ফিগার",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "v${BanglaUtils.toBanglaNum(currentVersion)}",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "জমি জমা দলিল সংরক্ষণ ও প্রিন্ট",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onCheckUpdatesManual) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = "আপডেট চেক করুন", tint = SecondaryTeal)
                    }
                    IconButton(onClick = onOpenWebViewMode) {
                        Icon(Icons.Default.Web, contentDescription = "ওয়েব ভিউ", tint = PrimaryBlue)
                    }
                    IconButton(onClick = { showHeaderSettings = !showHeaderSettings }) {
                        Icon(Icons.Default.Settings, contentDescription = "হেডার তথ্য", tint = Color.DarkGray)
                    }
                    IconButton(onClick = {
                        if (project.records.isEmpty()) {
                            Toast.makeText(context, "প্রিন্ট করার মত কোন দলিল রেকর্ড নেই", Toast.LENGTH_SHORT).show()
                        } else {
                            val html = PrintHelper.generatePrintHtml(project)
                            PrintHelper.printDocument(context, html, project.name)
                        }
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "প্রিন্ট ও PDF", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecordIndex = null
                    showFormDialog = true
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "নতুন দলিল যোগ")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নতুন দলিল", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF2F2F7))
        ) {
            // Optional Collapsible Header Settings Card
            if (showHeaderSettings) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("মৌজা ও খতিয়ান হেডার তথ্য", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = project.header.district,
                                onValueChange = { project.header.district = it; onProjectChange() },
                                label = { Text("জেলা") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = project.header.upazila,
                                onValueChange = { project.header.upazila = it; onProjectChange() },
                                label = { Text("উপজেলা/থানা") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = project.header.mouza,
                                onValueChange = { project.header.mouza = it; onProjectChange() },
                                label = { Text("মৌজা") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = project.header.jlNo,
                                onValueChange = { project.header.jlNo = BanglaUtils.toBanglaNum(it); onProjectChange() },
                                label = { Text("জে.এল নং") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Stats and quick actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "মোট সংরক্ষিত দলিল: ${BanglaUtils.toBanglaNum(project.records.size)} টি",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "PDF তৈরি করতে উপরের প্রিন্ট আইকন চাপুন",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    if (project.records.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                project.records.clear()
                                onProjectChange()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDelete)
                        ) {
                            Text("সব মুছুন", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Record List
            if (project.records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "কোন দলিল রেকর্ড নেই",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "নিচের 'নতুন দলিল' বাটনে চাপ দিয়ে যোগ করুন",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    itemsIndexed(project.records) { index, record ->
                        RecordCardItem(
                            index = index,
                            record = record,
                            isFirst = index == 0,
                            isLast = index == project.records.lastIndex,
                            onMoveUp = {
                                if (index > 0) {
                                    val temp = project.records[index]
                                    project.records[index] = project.records[index - 1]
                                    project.records[index - 1] = temp
                                    onProjectChange()
                                }
                            },
                            onMoveDown = {
                                if (index < project.records.lastIndex) {
                                    val temp = project.records[index]
                                    project.records[index] = project.records[index + 1]
                                    project.records[index + 1] = temp
                                    onProjectChange()
                                }
                            },
                            onEdit = {
                                editingRecordIndex = index
                                showFormDialog = true
                            },
                            onDelete = {
                                project.records.removeAt(index)
                                onProjectChange()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        val initialRecord = editingRecordIndex?.let { project.records.getOrNull(it) }
        RecordFormDialog(
            initialRecord = initialRecord,
            onDismiss = { showFormDialog = false },
            onSave = { updated ->
                if (editingRecordIndex != null && editingRecordIndex!! in project.records.indices) {
                    project.records[editingRecordIndex!!] = updated
                } else {
                    project.records.add(updated)
                }
                showFormDialog = false
                onProjectChange()
            }
        )
    }
}

@Composable
fun RecordCardItem(
    index: Int,
    record: DalilRecord,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaUtils.toBanglaNum(index + 1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "দলিল নং: ${BanglaUtils.toBanglaNum(record.dalilNo)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF202124)
                        )
                        if (record.date.isNotBlank()) {
                            Text(
                                text = "তারিখ: ${BanglaUtils.toBanglaNum(record.date)}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Move & Action Controls
                Row {
                    if (!isFirst) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "উপরে নিন", modifier = Modifier.size(16.dp))
                        }
                    }
                    if (!isLast) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "নিচে নিন", modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = RedDelete, modifier = Modifier.size(16.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Receivers summary
            if (record.receivers.isNotEmpty()) {
                Text(text = "গ্রহীতা:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SecondaryTeal)
                record.receivers.forEach { p ->
                    val landStr = if (p.land.isNotBlank()) " [${BanglaUtils.formatShatak(p.land)}]" else ""
                    Text(
                        text = "• ${p.name} (${p.relation}: ${p.guardian})$landStr",
                        fontSize = 13.sp,
                        color = Color(0xFF333333)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Donors summary
            if (record.donors.isNotEmpty()) {
                Text(text = "দাতা:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFB45309))
                record.donors.forEach { p ->
                    val landStr = if (p.land.isNotBlank()) " [${BanglaUtils.formatShatak(p.land)}]" else ""
                    Text(
                        text = "• ${p.name} (${p.relation}: ${p.guardian})$landStr",
                        fontSize = 13.sp,
                        color = Color(0xFF333333)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Khatians summary
            if (record.khatians.isNotEmpty()) {
                Text(text = "খতিয়ান ও দাগ:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = PrimaryBlue)
                record.khatians.forEach { k ->
                    val dagStr = if (k.dag.isNotBlank()) " (দাগ: ${BanglaUtils.toBanglaNum(k.dag)})" else ""
                    val landStr = if (k.land.isNotBlank()) " [${BanglaUtils.formatShatak(k.land)}]" else ""
                    Text(
                        text = "• ${k.type}: ${BanglaUtils.toBanglaNum(k.no)}$dagStr$landStr",
                        fontSize = 13.sp,
                        color = Color(0xFF333333)
                    )
                }
            }

            if (record.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "মন্তব্য: ${record.remarks}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
