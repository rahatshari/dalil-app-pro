package com.rahatsharif.dalilapp.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahatsharif.dalilapp.BuildConfig
import com.rahatsharif.dalilapp.R
import com.rahatsharif.dalilapp.model.*
import com.rahatsharif.dalilapp.ui.components.*
import com.rahatsharif.dalilapp.util.BanglaUtils
import com.rahatsharif.dalilapp.util.DalilRepository
import com.rahatsharif.dalilapp.util.PrintHelper
import com.rahatsharif.dalilapp.util.UpdateInfo
import com.rahatsharif.dalilapp.util.UpdateManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: DalilRepository,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Instant persistent state loaded synchronously from SharedPreferences (~1ms)
    var header by remember { mutableStateOf(repository.loadHeader()) }
    var records by remember { mutableStateOf<List<DalilRecord>>(repository.loadRecords()) }
    var savedProjects by remember { mutableStateOf<List<SavedProject>>(repository.loadSavedProjects()) }
    var printSettings by remember { mutableStateOf(repository.loadPrintSettings()) }
    var motherNameEnabled by remember { mutableStateOf(repository.isMotherNameEnabled()) }
    var autoUpdateEnabled by remember { mutableStateOf(repository.isAutoUpdateEnabled()) }

    // Form state (auto-restored from draft if available)
    val initialDraft = remember { repository.loadDraft() }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var dalilNo by remember { mutableStateOf(initialDraft?.dalilNo ?: "") }
    var dateVal by remember { mutableStateOf(initialDraft?.date ?: "") }
    var totalLand by remember { mutableStateOf(initialDraft?.totalLand ?: "") }
    var receivers by remember {
        mutableStateOf(
            if (initialDraft != null && initialDraft.receivers.isNotEmpty()) initialDraft.receivers.toList()
            else listOf(PersonEntry())
        )
    }
    var donors by remember {
        mutableStateOf(
            if (initialDraft != null && initialDraft.donors.isNotEmpty()) initialDraft.donors.toList()
            else listOf(PersonEntry())
        )
    }
    var khatians by remember {
        mutableStateOf(
            if (initialDraft != null && initialDraft.khatians.isNotEmpty()) initialDraft.khatians.toList()
            else listOf(KhatianEntry())
        )
    }

    // Dialogs & Menus
    var overflowExpanded by remember { mutableStateOf(false) }
    var showSaveProjectDialog by remember { mutableStateOf(false) }
    var showSavedProjectsDialog by remember { mutableStateOf(false) }
    var showBackupHubDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var pendingDeleteIndex by remember { mutableStateOf<Int?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    var stagedBackupFiles by remember { mutableStateOf<List<ParsedBackupFile>>(emptyList()) }
    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Silent background update check (never blocks startup)
    LaunchedEffect(Unit) {
        if (autoUpdateEnabled) {
            val info = UpdateManager.checkForUpdate(BuildConfig.VERSION_NAME)
            if (info != null) {
                availableUpdate = info
            }
        }
    }

    // Save draft automatically while typing new record
    fun persistDraftIfAdding() {
        if (editingIndex == null) {
            val draft = DalilRecord(
                dalilNo = dalilNo,
                date = dateVal,
                totalLand = totalLand,
                receivers = receivers.toMutableList(),
                donors = donors.toMutableList(),
                khatians = khatians.toMutableList()
            )
            repository.saveDraft(draft)
        }
    }

    fun resetForm() {
        editingIndex = null
        dalilNo = ""
        dateVal = ""
        totalLand = ""
        receivers = listOf(PersonEntry())
        donors = listOf(PersonEntry())
        khatians = listOf(KhatianEntry())
        repository.saveDraft(null)
    }

    fun submitForm() {
        val hasAnyInput = dalilNo.isNotBlank() ||
            dateVal.isNotBlank() ||
            receivers.any { it.name.isNotBlank() || it.guardian.isNotBlank() || it.land.isNotBlank() } ||
            donors.any { it.name.isNotBlank() || it.guardian.isNotBlank() || it.land.isNotBlank() } ||
            khatians.any { it.no.isNotBlank() || it.land.isNotBlank() || it.dag.isNotBlank() }

        if (!hasAnyInput) {
            Toast.makeText(context, "অনুগ্রহ করে অন্তত দলিলের কিছু তথ্য লিখুন!", Toast.LENGTH_SHORT).show()
            return
        }

        val computedTotal = if (totalLand.isNotBlank()) {
            BanglaUtils.cleanShatakForEdit(totalLand)
        } else {
            val sumRec = receivers.mapNotNull { BanglaUtils.parseNumericShatak(it.land) }.sum()
            val sumKh = khatians.mapNotNull { BanglaUtils.parseNumericShatak(it.land) }.sum()
            when {
                sumRec > 0 -> BanglaUtils.formatDoubleBangla(sumRec)
                sumKh > 0 -> BanglaUtils.formatDoubleBangla(sumKh)
                else -> "-"
            }
        }

        val cleanKhatians = khatians.map {
            it.copy(
                no = it.no.trim().ifBlank { "-" },
                land = BanglaUtils.cleanShatakForEdit(it.land),
                dag = it.dag.trim().ifBlank { "-" }
            )
        }.toMutableList()

        val record = DalilRecord(
            id = editingIndex?.let { records.getOrNull(it)?.id } ?: UUID.randomUUID().toString(),
            serial = (editingIndex ?: records.size) + 1,
            dalilNo = BanglaUtils.toBanglaNum(dalilNo.trim().ifBlank { "-" }),
            date = BanglaUtils.toBanglaNum(dateVal.trim().ifBlank { "-" }),
            receivers = receivers.map { it.copy() }.toMutableList(),
            donors = donors.map { it.copy() }.toMutableList(),
            totalLand = computedTotal,
            khatians = cleanKhatians
        )

        val currentEdit = editingIndex
        val nextList = if (currentEdit != null && currentEdit in records.indices) {
            records.toMutableList().apply { this[currentEdit] = record }
        } else {
            (records + record).toMutableList()
        }

        records = nextList.mapIndexed { idx, r -> r.copy(serial = idx + 1) }
        repository.saveRecords(records)
        resetForm()
        Toast.makeText(
            context,
            if (currentEdit != null) "তথ্য আপডেট করা হয়েছে!" else "দলিলে তথ্য যোগ করা হয়েছে!",
            Toast.LENGTH_SHORT
        ).show()
    }

    // File pickers for JSON Import / Export
    val pickBackupFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val parsed = uris.map { repository.readBackupFileFromUri(context, it) }
            stagedBackupFiles = stagedBackupFiles + parsed
            val validCount = parsed.count { it.valid }
            Toast.makeText(
                context,
                "${BanglaUtils.toBanglaNum(validCount)}টি ফাইল যুক্ত হয়েছে",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val json = repository.exportRecordsJson(
                    header = header,
                    records = records,
                    projectName = header.mouza.ifBlank { "দলিল ব্যাকআপ" }
                )
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "ব্যাকআপ ফাইল সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "ব্যাকআপ সেভ করতে ত্রুটি: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    BackHandler {
        if (editingIndex != null) {
            resetForm()
        } else {
            showExitConfirmDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    Box {
                        IconButton(onClick = { overflowExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "মেনু")
                        }
                        DropdownMenu(
                            expanded = overflowExpanded,
                            onDismissRequest = { overflowExpanded = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null) },
                                text = { Text("বর্তমান দলিল সেভ করুন") },
                                onClick = {
                                    overflowExpanded = false
                                    if (records.isEmpty()) {
                                        Toast.makeText(context, "সেভ করার মতো কোনো দলিল নেই!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showSaveProjectDialog = true
                                    }
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                                text = { Text("সংরক্ষিত দলিল (${BanglaUtils.toBanglaNum(savedProjects.size)})") },
                                onClick = {
                                    overflowExpanded = false
                                    showSavedProjectsDialog = true
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.SettingsBackupRestore, contentDescription = null) },
                                text = { Text("ব্যাকআপ ও রিস্টোর হাব") },
                                onClick = {
                                    overflowExpanded = false
                                    showBackupHubDialog = true
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                text = { Text("থিম ও অ্যাপ সেটিংস") },
                                onClick = {
                                    overflowExpanded = false
                                    showSettingsDialog = true
                                }
                            )
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFF111827)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "দলিল লোগো",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "দলিল ফিগার",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "নেটিভ অফলাইন v${BanglaUtils.toBanglaNum(BuildConfig.VERSION_NAME)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            if (availableUpdate != null) {
                                showUpdateDialog = true
                            } else {
                                Toast.makeText(context, "আপডেট চেক করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    val info = UpdateManager.checkForUpdate(BuildConfig.VERSION_NAME)
                                    if (info != null) {
                                        availableUpdate = info
                                        showUpdateDialog = true
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "আপনার অ্যাপটি আপ-টু-ডেট আছে (v${BanglaUtils.toBanglaNum(BuildConfig.VERSION_NAME)})!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (availableUpdate != null) Color(0xFF188038) else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(end = 8.dp).height(34.dp)
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (availableUpdate != null) "নতুন আপডেট" else "আপডেট চেক",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Print Header Accordion
            HeaderSettingsAccordion(
                header = header,
                onHeaderChange = { updated ->
                    header = updated
                    repository.saveHeader(updated)
                }
            )

            // 2. Main Deed Entry Form
            DeedEntryFormCard(
                nextSerial = records.size + 1,
                editingIndex = editingIndex,
                dalilNo = dalilNo,
                dateVal = dateVal,
                totalLand = totalLand,
                receivers = receivers,
                donors = donors,
                khatians = khatians,
                motherNameEnabled = motherNameEnabled,
                onDalilNoChange = { dalilNo = it; persistDraftIfAdding() },
                onDateChange = { dateVal = it; persistDraftIfAdding() },
                onTotalLandChange = { totalLand = it; persistDraftIfAdding() },
                onReceiversChange = { receivers = it; persistDraftIfAdding() },
                onDonorsChange = { donors = it; persistDraftIfAdding() },
                onKhatiansChange = { khatians = it; persistDraftIfAdding() },
                onSubmit = { submitForm() },
                onCancelEdit = { resetForm() }
            )

            // 3. Middle Quick Action Strip (Save / Saved Projects / Restore from File)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (records.isEmpty()) {
                                Toast.makeText(context, "সেভ করার মতো কোনো দলিল নেই!", Toast.LENGTH_SHORT).show()
                            } else {
                                showSaveProjectDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("বর্তমান দলিলগুলো সেভ করুন", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                    }

                    Button(
                        onClick = { showSavedProjectsDialog = true },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "সংরক্ষিত দলিল আনুন (${BanglaUtils.toBanglaNum(savedProjects.size)})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        )
                    }

                    Button(
                        onClick = { showBackupHubDialog = true },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ফাইল থেকে আনুন (রিস্টোর)", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                    }
                }
            }

            // 4. Live Native Full-Width A4 Print Preview + Native Vector PDF Print
            A4PreviewSection(
                header = header,
                records = records,
                printSettings = printSettings,
                onSettingsChange = { updated ->
                    printSettings = updated
                    repository.savePrintSettings(updated)
                },
                onPrintClick = {
                    PrintHelper.printRecords(
                        context = context,
                        header = header,
                        records = records,
                        settings = printSettings
                    )
                },
                onClearAllClick = {
                    if (records.isEmpty()) {
                        Toast.makeText(context, "মোছার মতো কোনো তথ্য নেই!", Toast.LENGTH_SHORT).show()
                    } else {
                        showClearAllConfirm = true
                    }
                },
                onMoveRecord = { idx, delta ->
                    val target = idx + delta
                    if (idx in records.indices && target in records.indices) {
                        val next = records.toMutableList()
                        val item = next.removeAt(idx)
                        next.add(target, item)
                        records = next.mapIndexed { i, r -> r.copy(serial = i + 1) }
                        repository.saveRecords(records)
                    }
                },
                onEditRecord = { idx ->
                    val rec = records.getOrNull(idx) ?: return@A4PreviewSection
                    editingIndex = idx
                    dalilNo = if (rec.dalilNo == "-") "" else rec.dalilNo
                    dateVal = if (rec.date == "-") "" else rec.date
                    totalLand = BanglaUtils.cleanShatakForEdit(rec.totalLand)
                    receivers = if (rec.receivers.isNotEmpty()) rec.receivers.map { it.copy() } else listOf(PersonEntry())
                    donors = if (rec.donors.isNotEmpty()) rec.donors.map { it.copy() } else listOf(PersonEntry())
                    khatians = if (rec.khatians.isNotEmpty()) rec.khatians.map { it.copy() } else listOf(KhatianEntry())
                    scope.launch {
                        scrollState.animateScrollTo(0)
                    }
                },
                onDeleteRecord = { idx ->
                    pendingDeleteIndex = idx
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (showSaveProjectDialog) {
        val suggested = "${header.mouza.ifBlank { "প্রজেক্ট" }} - ${BanglaUtils.toBanglaNum(records.size)}টি দলিল (${header.date.ifBlank { BanglaUtils.getTodayDateBangla() }})"
        SaveProjectDialog(
            defaultName = suggested,
            recordCount = records.size,
            onDismiss = { showSaveProjectDialog = false },
            onConfirmSave = { projName ->
                val proj = SavedProject(
                    name = projName,
                    savedAt = BanglaUtils.getTodayTimestampBangla(),
                    header = header.copy(),
                    records = records.map { it.copy() }.toMutableList()
                )
                savedProjects = listOf(proj) + savedProjects
                repository.saveSavedProjects(savedProjects)
                showSaveProjectDialog = false
                Toast.makeText(context, "\"$projName\" সফলভাবে সেভ করা হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showSavedProjectsDialog) {
        SavedProjectsDialog(
            projects = savedProjects,
            onDismiss = { showSavedProjectsDialog = false },
            onLoadReplace = { proj ->
                records = proj.records.mapIndexed { idx, r -> r.copy(id = UUID.randomUUID().toString(), serial = idx + 1) }
                header = proj.header.copy()
                repository.saveRecords(records)
                repository.saveHeader(header)
                showSavedProjectsDialog = false
                Toast.makeText(context, "\"${proj.name}\" টেবিলে আনা হয়েছে!", Toast.LENGTH_SHORT).show()
            },
            onLoadMerge = { proj ->
                val (merged, added, skipped) = repository.mergeRecords(records, proj.records, skipDuplicates = true)
                records = merged
                repository.saveRecords(records)
                showSavedProjectsDialog = false
                val msg = if (skipped > 0) {
                    "${BanglaUtils.toBanglaNum(added)}টি দলিল যোগ হয়েছে (${BanglaUtils.toBanglaNum(skipped)}টি ডুপ্লিকেট বাদ)"
                } else {
                    "${BanglaUtils.toBanglaNum(added)}টি দলিল টেবিলে যোগ হয়েছে!"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onDeleteProject = { proj ->
                savedProjects = savedProjects.filterNot { it.id == proj.id }
                repository.saveSavedProjects(savedProjects)
                Toast.makeText(context, "প্রজেক্টটি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showBackupHubDialog) {
        BackupRestoreHubDialog(
            stagedFiles = stagedBackupFiles,
            currentRecordCount = records.size,
            onDismiss = { showBackupHubDialog = false },
            onPickFilesClick = {
                pickBackupFilesLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            onExportBackupClick = {
                val dateSuffix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                exportBackupLauncher.launch("Dalil_Backup_$dateSuffix.json")
            },
            onRemoveStagedFile = { idx ->
                stagedBackupFiles = stagedBackupFiles.toMutableList().also {
                    if (idx in it.indices) it.removeAt(idx)
                }
            },
            onImportMerge = { skipDupes ->
                val valid = stagedBackupFiles.filter { it.valid }
                val allIncoming = valid.flatMap { it.records }
                val (merged, added, skipped) = repository.mergeRecords(records, allIncoming, skipDupes)
                records = merged
                repository.saveRecords(records)
                valid.firstOrNull { it.header != null }?.header?.let { h ->
                    if (records.size == added) {
                        header = h
                        repository.saveHeader(header)
                    }
                }
                stagedBackupFiles = emptyList()
                showBackupHubDialog = false
                val msg = if (skipped > 0) {
                    "${BanglaUtils.toBanglaNum(added)}টি দলিল যোগ হয়েছে (${BanglaUtils.toBanglaNum(skipped)}টি ডুপ্লিকেট বাদ)"
                } else {
                    "${BanglaUtils.toBanglaNum(added)}টি দলিল টেবিলে যোগ হয়েছে!"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onImportReplace = {
                val valid = stagedBackupFiles.filter { it.valid }
                val allIncoming = valid.flatMap { it.records }
                records = allIncoming.mapIndexed { idx, r -> r.copy(id = UUID.randomUUID().toString(), serial = idx + 1) }
                repository.saveRecords(records)
                valid.firstOrNull { it.header != null }?.header?.let { h ->
                    header = h
                    repository.saveHeader(header)
                }
                stagedBackupFiles = emptyList()
                showBackupHubDialog = false
                Toast.makeText(
                    context,
                    "তালিকা প্রতিস্থাপন করে ${BanglaUtils.toBanglaNum(records.size)}টি দলিল আনা হয়েছে!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onImportAsProjects = {
                val valid = stagedBackupFiles.filter { it.valid }
                val newProjects = valid.map { file ->
                    SavedProject(
                        name = file.projectName ?: file.filename.removeSuffix(".json"),
                        savedAt = BanglaUtils.getTodayTimestampBangla(),
                        header = file.header ?: header.copy(),
                        records = file.records.toMutableList()
                    )
                }
                savedProjects = newProjects + savedProjects
                repository.saveSavedProjects(savedProjects)
                stagedBackupFiles = emptyList()
                showBackupHubDialog = false
                Toast.makeText(
                    context,
                    "${BanglaUtils.toBanglaNum(newProjects.size)}টি ব্যাকআপ সংরক্ষিত তালিকায় সেভ হয়েছে!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            themePreference = themePreference,
            motherNameEnabled = motherNameEnabled,
            autoUpdateEnabled = autoUpdateEnabled,
            onThemeChange = onThemePreferenceChange,
            onMotherNameToggle = {
                motherNameEnabled = it
                repository.setMotherNameEnabled(it)
            },
            onAutoUpdateToggle = {
                autoUpdateEnabled = it
                repository.setAutoUpdateEnabled(it)
            },
            onResetHeader = {
                val def = GlobalHeader(date = BanglaUtils.getTodayDateBangla())
                header = def
                repository.saveHeader(def)
                Toast.makeText(context, "হেডার ডিফল্টে রিসেট করা হয়েছে", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showUpdateDialog && availableUpdate != null) {
        UpdateDialog(
            updateInfo = availableUpdate!!,
            currentVersion = BuildConfig.VERSION_NAME,
            onDismiss = { showUpdateDialog = false }
        )
    }

    pendingDeleteIndex?.let { idx ->
        AlertDialog(
            onDismissRequest = { pendingDeleteIndex = null },
            title = { Text("দলিল মুছুন", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিত যে ${BanglaUtils.toBanglaNum(idx + 1)} নং দলিলটি মুছতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingIndex == idx) resetForm()
                        else if (editingIndex != null && editingIndex!! > idx) editingIndex = editingIndex!! - 1
                        val next = records.toMutableList()
                        if (idx in next.indices) next.removeAt(idx)
                        records = next.mapIndexed { i, r -> r.copy(serial = i + 1) }
                        repository.saveRecords(records)
                        pendingDeleteIndex = null
                        Toast.makeText(context, "দলিলটি মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteIndex = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("সব তালিকা মুছুন?", fontWeight = FontWeight.Bold) },
            text = { Text("সতর্কতা: আপনি কি বর্তমান সব দলিলের তালিকা মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        records = emptyList()
                        repository.saveRecords(records)
                        resetForm()
                        showClearAllConfirm = false
                        Toast.makeText(context, "সম্পূর্ণ তালিকা মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("হ্যাঁ, সব মুছুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("অ্যাপ বন্ধ করুন", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি দলিল ফিগার অ্যাপ থেকে বের হতে চান?") },
            confirmButton = {
                Button(onClick = { (context as? Activity)?.finish() }) {
                    Text("হ্যাঁ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("না")
                }
            }
        )
    }
}
