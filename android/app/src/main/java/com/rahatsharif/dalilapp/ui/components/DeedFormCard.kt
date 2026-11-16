package com.rahatsharif.dalilapp.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahatsharif.dalilapp.model.DalilRecord
import com.rahatsharif.dalilapp.model.GlobalHeader
import com.rahatsharif.dalilapp.model.KhatianEntry
import com.rahatsharif.dalilapp.model.PersonEntry
import com.rahatsharif.dalilapp.util.BanglaUtils
import java.util.Calendar
import java.util.Locale

@Composable
fun HeaderSettingsAccordion(
    header: GlobalHeader,
    onHeaderChange: (GlobalHeader) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "প্রিন্ট পেজের হেডার তথ্য",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "সংকুচিত করুন" else "বিস্তারিত",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = header.district,
                            onValueChange = { onHeaderChange(header.copy(district = it)) },
                            label = { Text("জেলা", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = header.upazila,
                            onValueChange = { onHeaderChange(header.copy(upazila = it)) },
                            label = { Text("উপজেলা", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = header.mouza,
                            onValueChange = { onHeaderChange(header.copy(mouza = it)) },
                            label = { Text("মৌজা", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = header.creator,
                            onValueChange = { onHeaderChange(header.copy(creator = it)) },
                            label = { Text("প্রস্তুতকারক", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = header.date,
                            onValueChange = { onHeaderChange(header.copy(date = BanglaUtils.formatDateAuto(it))) },
                            label = { Text("প্রিন্ট তারিখ", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    val cal = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val formatted = String.format(Locale.US, "%02d/%02d/%04d", d, m + 1, y)
                                            onHeaderChange(header.copy(date = BanglaUtils.toBanglaNum(formatted)))
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "তারিখ নির্বাচন")
                                }
                            }
                        )
                        OutlinedButton(
                            onClick = {
                                onHeaderChange(header.copy(date = BanglaUtils.getTodayDateBangla()))
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("আজ", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeedEntryFormCard(
    nextSerial: Int,
    editingIndex: Int?,
    dalilNo: String,
    dateVal: String,
    totalLand: String,
    receivers: List<PersonEntry>,
    donors: List<PersonEntry>,
    khatians: List<KhatianEntry>,
    motherNameEnabled: Boolean,
    onDalilNoChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTotalLandChange: (String) -> Unit,
    onReceiversChange: (List<PersonEntry>) -> Unit,
    onDonorsChange: (List<PersonEntry>) -> Unit,
    onKhatiansChange: (List<KhatianEntry>) -> Unit,
    onSubmit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (editingIndex != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "নং ${BanglaUtils.toBanglaNum(editingIndex + 1)} দলিল সম্পাদনা চলছে",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Row 1: Serial | Dalil No | Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.width(64.dp)) {
                    Text(
                        text = "১. নং",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = BanglaUtils.toBanglaNum(if (editingIndex != null) editingIndex + 1 else nextSerial),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = dalilNo,
                    onValueChange = { onDalilNoChange(BanglaUtils.toBanglaNum(it)) },
                    label = { Text("২. দলিল নম্বর", fontSize = 12.sp) },
                    placeholder = { Text("দলিল নং", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Date input + Today + Native DatePicker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dateVal,
                    onValueChange = { onDateChange(BanglaUtils.formatDateAuto(it)) },
                    label = { Text("৩. তারিখ (যেমন: ২৩/১২/২০০১)", fontSize = 12.sp) },
                    placeholder = { Text("দিন/মাস/বছর", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val formatted = String.format(Locale.US, "%02d/%02d/%04d", d, m + 1, y)
                                    onDateChange(BanglaUtils.toBanglaNum(formatted))
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "ক্যালেন্ডার")
                        }
                    }
                )
                FilledTonalButton(
                    onClick = { onDateChange(BanglaUtils.getTodayDateBangla()) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("আজ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // 4. Receivers Section
            PeopleSectionBox(
                title = "৪. গ্রহীতার বিবরণ",
                itemLabelPrefix = "গ্রহীতা",
                landLabel = "জমি (যেমন: ৫)",
                addButtonText = "আরও গ্রহীতা",
                accentColor = MaterialTheme.colorScheme.primary,
                people = receivers,
                showMotherField = motherNameEnabled,
                onPeopleChange = onReceiversChange
            )

            // 5. Donors Section
            PeopleSectionBox(
                title = "৫. দাতার বিবরণ",
                itemLabelPrefix = "দাতা",
                landLabel = "বিক্রি (যেমন: ৫)",
                addButtonText = "আরও দাতা",
                accentColor = MaterialTheme.colorScheme.secondary,
                people = donors,
                showMotherField = motherNameEnabled,
                onPeopleChange = onDonorsChange
            )

            // 6. Total Land
            OutlinedTextField(
                value = totalLand,
                onValueChange = { onTotalLandChange(BanglaUtils.toBanglaNum(it)) },
                label = { Text("৬. মোট জমির পরিমাণ (শতক)", fontSize = 12.sp) },
                placeholder = { Text("শুধু সংখ্যা (যেমন: ৫)", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // 7. Khatians Section
            KhatianSectionBox(
                khatians = khatians,
                onKhatiansChange = onKhatiansChange
            )

            // Submit / Cancel Edit Actions
            if (editingIndex == null) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("তালিকায় যোগ করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("আপডেট করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeopleSectionBox(
    title: String,
    itemLabelPrefix: String,
    landLabel: String,
    addButtonText: String,
    accentColor: Color,
    people: List<PersonEntry>,
    showMotherField: Boolean,
    onPeopleChange: (List<PersonEntry>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        people.forEachIndexed { idx, person ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(accentColor)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${BanglaUtils.toBanglaNum(idx + 1)}. $itemLabelPrefix",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = accentColor
                            )
                            if (people.size > 1) {
                                IconButton(
                                    onClick = {
                                        val next = people.toMutableList()
                                        next.removeAt(idx)
                                        onPeopleChange(next)
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "মুছুন",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = person.name,
                            onValueChange = { newVal ->
                                val next = people.toMutableList()
                                next[idx] = person.copy(name = newVal)
                                onPeopleChange(next)
                            },
                            label = { Text("নাম", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RelationDropdown(
                                selected = person.relation,
                                onSelect = { rel ->
                                    val next = people.toMutableList()
                                    next[idx] = person.copy(relation = rel)
                                    onPeopleChange(next)
                                }
                            )
                            OutlinedTextField(
                                value = person.guardian,
                                onValueChange = { newVal ->
                                    val next = people.toMutableList()
                                    next[idx] = person.copy(guardian = newVal)
                                    onPeopleChange(next)
                                },
                                label = { Text("নাম (ঐচ্ছিক)", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        if (showMotherField) {
                            OutlinedTextField(
                                value = person.mother,
                                onValueChange = { newVal ->
                                    val next = people.toMutableList()
                                    next[idx] = person.copy(mother = newVal)
                                    onPeopleChange(next)
                                },
                                label = { Text("মাতার নাম (ঐচ্ছিক)", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = person.land,
                            onValueChange = { newVal ->
                                val next = people.toMutableList()
                                next[idx] = person.copy(land = BanglaUtils.toBanglaNum(newVal))
                                onPeopleChange(next)
                            },
                            label = { Text(landLabel, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onPeopleChange(people + PersonEntry()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(addButtonText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun RelationDropdown(
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("পিতা", "স্বামী")
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
            modifier = Modifier.width(96.dp)
        ) {
            Text(selected, fontSize = 13.sp, maxLines = 1)
            Spacer(modifier = Modifier.width(2.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun KhatianSectionBox(
    khatians: List<KhatianEntry>,
    onKhatiansChange: (List<KhatianEntry>) -> Unit
) {
    val khatianTypes = listOf("এস এ", "সি এস", "বি আর এস", "আর এস")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "৭. খতিয়ান ও দাগের বিবরণ",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        khatians.forEachIndexed { idx, kh ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color(0xFF188038).copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (khatians.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "খতিয়ান ${BanglaUtils.toBanglaNum(idx + 1)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF188038)
                            )
                            IconButton(
                                onClick = {
                                    val next = khatians.toMutableList()
                                    next.removeAt(idx)
                                    onKhatiansChange(next)
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "মুছুন",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var typeExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { typeExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 15.dp)
                            ) {
                                Text(kh.type.ifBlank { "ধরন" }, fontSize = 13.sp, maxLines = 1)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            DropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                khatianTypes.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t) },
                                        onClick = {
                                            val next = khatians.toMutableList()
                                            next[idx] = kh.copy(type = t)
                                            onKhatiansChange(next)
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = kh.no,
                            onValueChange = { newVal ->
                                val next = khatians.toMutableList()
                                next[idx] = kh.copy(no = BanglaUtils.toBanglaNum(newVal))
                                onKhatiansChange(next)
                            },
                            label = { Text("খতিয়ান নং", fontSize = 12.sp) },
                            placeholder = { Text("যেমন: ২১৮", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = kh.land,
                            onValueChange = { newVal ->
                                val next = khatians.toMutableList()
                                next[idx] = kh.copy(land = BanglaUtils.toBanglaNum(newVal))
                                onKhatiansChange(next)
                            },
                            label = { Text("জমি (শতক)", fontSize = 12.sp) },
                            placeholder = { Text("যেমন: ৩০", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = kh.dag,
                            onValueChange = { newVal ->
                                val next = khatians.toMutableList()
                                next[idx] = kh.copy(dag = BanglaUtils.toBanglaNum(newVal))
                                onKhatiansChange(next)
                            },
                            label = { Text("দাগ নং", fontSize = 12.sp) },
                            placeholder = { Text("যেমন: ১৬২০", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onKhatiansChange(khatians + KhatianEntry()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("আরও খতিয়ান", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}
