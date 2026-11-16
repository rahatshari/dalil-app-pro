package com.rahatsharif.dalilapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rahatsharif.dalilapp.model.ParsedBackupFile
import com.rahatsharif.dalilapp.model.SavedProject
import com.rahatsharif.dalilapp.model.ThemePreference
import com.rahatsharif.dalilapp.util.BanglaUtils

@Composable
fun SaveProjectDialog(
    defaultName: String,
    recordCount: Int,
    onDismiss: () -> Unit,
    onConfirmSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text("প্রজেক্ট হিসেবে সেভ করুন", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "মোট ${BanglaUtils.toBanglaNum(recordCount)}টি দলিল ফোনের স্থায়ী মেমোরিতে সংরক্ষিত হবে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("প্রজেক্টের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = name.trim().ifBlank { defaultName }
                    onConfirmSave(trimmed)
                }
            ) {
                Text("সেভ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun SavedProjectsDialog(
    projects: List<SavedProject>,
    onDismiss: () -> Unit,
    onLoadReplace: (SavedProject) -> Unit,
    onLoadMerge: (SavedProject) -> Unit,
    onDeleteProject: (SavedProject) -> Unit
) {
    var selectedForStrategy by remember { mutableStateOf<SavedProject?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "সংরক্ষিত দলিল তালিকা (${BanglaUtils.toBanglaNum(projects.size)})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FolderOff,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোনো সংরক্ষিত প্রজেক্ট নেই",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(projects, key = { it.id }) { proj ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = proj.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { onDeleteProject(proj) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "মুছুন",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${BanglaUtils.toBanglaNum(proj.records.size)} টি দলিল • ${proj.savedAt}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = { selectedForStrategy = proj },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("টেবিলে আনুন", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedForStrategy?.let { target ->
        AlertDialog(
            onDismissRequest = { selectedForStrategy = null },
            title = { Text("কীভাবে আনতে চান?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    "\"${target.name}\" (${BanglaUtils.toBanglaNum(target.records.size)}টি দলিল) বর্তমান তালিকার সাথে যোগ করবেন নাকি আগের তালিকা মুছে নতুন করে আনবেন?",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(onClick = {
                    val p = target
                    selectedForStrategy = null
                    onLoadMerge(p)
                }) {
                    Text("তালিকায় যোগ করুন")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = { selectedForStrategy = null }) {
                        Text("বাতিল")
                    }
                    OutlinedButton(onClick = {
                        val p = target
                        selectedForStrategy = null
                        onLoadReplace(p)
                    }) {
                        Text("প্রতিস্থাপন করুন")
                    }
                }
            }
        )
    }
}

@Composable
fun BackupRestoreHubDialog(
    stagedFiles: List<ParsedBackupFile>,
    currentRecordCount: Int,
    onDismiss: () -> Unit,
    onPickFilesClick: () -> Unit,
    onExportBackupClick: () -> Unit,
    onRemoveStagedFile: (Int) -> Unit,
    onImportMerge: (Boolean) -> Unit,
    onImportReplace: () -> Unit,
    onImportAsProjects: () -> Unit
) {
    var skipDuplicates by remember { mutableStateOf(true) }
    val validFiles = stagedFiles.filter { it.valid }
    val totalRecords = validFiles.sumOf { it.records.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.86f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("ব্যাকআপ ও রিস্টোর হাব", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "এক বা একাধিক .json ব্যাকআপ আনুন বা সেভ করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPickFilesClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ফাইল বাছুন (.json)", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onExportBackupClick,
                        enabled = currentRecordCount > 0,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ব্যাকআপ সেভ (${BanglaUtils.toBanglaNum(currentRecordCount)})", fontSize = 12.sp)
                    }
                }

                if (stagedFiles.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "রিস্টোর করার জন্য উপরের \"ফাইল বাছুন\" বাটনে চাপ দিয়ে\nপূর্বের JSON ব্যাকআপ ফাইল সিলেক্ট করুন।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${BanglaUtils.toBanglaNum(stagedFiles.size)}টি ফাইল • মোট ${BanglaUtils.toBanglaNum(totalRecords)}টি দলিল",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stagedFiles.indices.toList()) { idx ->
                            val file = stagedFiles[idx]
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                border = BorderStroke(
                                    1.dp,
                                    if (file.valid) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(file.filename, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(
                                            text = if (file.valid) "✓ ${BanglaUtils.toBanglaNum(file.records.size)} টি দলিল পাওয়া গেছে"
                                            else (file.error ?: "ত্রুটি"),
                                            fontSize = 12.sp,
                                            color = if (file.valid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                    IconButton(onClick = { onRemoveStagedFile(idx) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "বাদ দিন", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (totalRecords > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = skipDuplicates,
                                onCheckedChange = { skipDuplicates = it }
                            )
                            Text("একই দলিল নম্বর থাকলে ডুপ্লিকেট বাদ দিন", fontSize = 12.5.sp)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onImportMerge(skipDuplicates) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("বর্তমান তালিকায় যোগ করুন (${BanglaUtils.toBanglaNum(totalRecords)}টি)")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onImportReplace,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("সব মুছে প্রতিস্থাপন", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = onImportAsProjects,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("প্রজেক্টে সেভ রাখুন", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    themePreference: ThemePreference,
    motherNameEnabled: Boolean,
    autoUpdateEnabled: Boolean,
    onThemeChange: (ThemePreference) -> Unit,
    onMotherNameToggle: (Boolean) -> Unit,
    onAutoUpdateToggle: (Boolean) -> Unit,
    onResetHeader: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text("অ্যাপ সেটিংস ও থিম", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("থিম মোড (ডার্ক / লাইট)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf(
                        Triple(ThemePreference.SYSTEM, "অটো", Icons.Default.BrightnessAuto),
                        Triple(ThemePreference.LIGHT, "লাইট", Icons.Default.LightMode),
                        Triple(ThemePreference.DARK, "ডার্ক", Icons.Default.DarkMode)
                    )
                    modes.forEach { (pref, label, icon) ->
                        FilterChip(
                            selected = themePreference == pref,
                            onClick = { onThemeChange(pref) },
                            label = { Text(label, fontSize = 11.5.sp) },
                            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("মাতার নাম ফিল্ড দেখান", fontWeight = FontWeight.Medium, fontSize = 13.5.sp)
                        Text("দাতা ও গ্রহীতার অংশে মাতার নাম ইনপুট", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = motherNameEnabled, onCheckedChange = onMotherNameToggle)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("অটোমেটিক আপডেট চেক", fontWeight = FontWeight.Medium, fontSize = 13.5.sp)
                        Text("অ্যাপ চালুর সময় নতুন ভার্সন চেক করবে", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoUpdateEnabled, onCheckedChange = onAutoUpdateToggle)
                }

                OutlinedButton(
                    onClick = onResetHeader,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("হেডার তথ্য ডিফল্টে রিসেট করুন", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("সম্পন্ন")
            }
        }
    )
}
