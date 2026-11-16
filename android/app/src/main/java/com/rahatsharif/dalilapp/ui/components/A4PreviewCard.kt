package com.rahatsharif.dalilapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahatsharif.dalilapp.model.DalilRecord
import com.rahatsharif.dalilapp.model.GlobalHeader
import com.rahatsharif.dalilapp.model.KhatianEntry
import com.rahatsharif.dalilapp.model.PersonEntry
import com.rahatsharif.dalilapp.model.PrintSettings
import com.rahatsharif.dalilapp.util.BanglaUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun A4PreviewSection(
    header: GlobalHeader,
    records: List<DalilRecord>,
    printSettings: PrintSettings,
    onSettingsChange: (PrintSettings) -> Unit,
    onPrintClick: () -> Unit,
    onClearAllClick: () -> Unit,
    onMoveRecord: (Int, Int) -> Unit,
    onEditRecord: (Int) -> Unit,
    onDeleteRecord: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Toolbar header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "প্রিন্ট প্রিভিউ সেটিংস",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${BanglaUtils.toBanglaNum(records.size)}টি দলিল",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Responsive Controls
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Orientation Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        SegmentedChipButton(
                            selected = printSettings.orientation != "landscape",
                            icon = Icons.Default.CropPortrait,
                            label = "পোর্ট্রেট (খাড়া)",
                            onClick = { onSettingsChange(printSettings.copy(orientation = "portrait")) }
                        )
                        SegmentedChipButton(
                            selected = printSettings.orientation == "landscape",
                            icon = Icons.Default.CropLandscape,
                            label = "ল্যান্ডস্কেপ",
                            onClick = { onSettingsChange(printSettings.copy(orientation = "landscape")) }
                        )
                    }
                }

                // Zoom Fit / 100% Zoom Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        SegmentedChipButton(
                            selected = printSettings.previewZoomMode != "full",
                            icon = Icons.Default.FitScreen,
                            label = "পূর্ণ পেজ",
                            onClick = { onSettingsChange(printSettings.copy(previewZoomMode = "fit")) }
                        )
                        SegmentedChipButton(
                            selected = printSettings.previewZoomMode == "full",
                            icon = Icons.Default.ZoomIn,
                            label = "১০০% জুম",
                            onClick = { onSettingsChange(printSettings.copy(previewZoomMode = "full")) }
                        )
                    }
                }

                // Margin Selector + Outer Border
                var marginMenuOpen by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { marginMenuOpen = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        val marginLabel = when (printSettings.marginMode) {
                            "narrow" -> "মার্জিন: কম"
                            "wide" -> "মার্জিন: বেশি"
                            else -> "মার্জিন: নর্মাল"
                        }
                        Text(marginLabel, fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = marginMenuOpen,
                        onDismissRequest = { marginMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("কম মার্জিন") },
                            onClick = {
                                onSettingsChange(printSettings.copy(marginMode = "narrow"))
                                marginMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("নর্মাল মার্জিন") },
                            onClick = {
                                onSettingsChange(printSettings.copy(marginMode = "normal"))
                                marginMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("বেশি মার্জিন") },
                            onClick = {
                                onSettingsChange(printSettings.copy(marginMode = "wide"))
                                marginMenuOpen = false
                            }
                        )
                    }
                }

                FilterChip(
                    selected = printSettings.showOuterBorder,
                    onClick = {
                        onSettingsChange(printSettings.copy(showOuterBorder = !printSettings.showOuterBorder))
                    },
                    label = { Text("পেজ বর্ডার", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.BorderOuter, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.height(38.dp)
                )

                // Print Button
                Button(
                    onClick = onPrintClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("প্রিন্ট করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Clear All Button
                Button(
                    onClick = onClearAllClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("মুছুন", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            // Authentic Pure White Paper Preview Sheet (always white paper + pure black ink)
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val isFullZoom = printSettings.previewZoomMode == "full"
                val isLandscape = printSettings.orientation == "landscape"
                val fullWidthDp: Dp = if (isLandscape) 940.dp else 700.dp
                val containerW = maxWidth
                val useHorizontalScroll = isFullZoom && containerW < fullWidthDp

                if (useHorizontalScroll) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        WhiteA4PaperSheet(
                            width = fullWidthDp,
                            compactScale = 1f,
                            header = header,
                            records = records,
                            showOuterBorder = printSettings.showOuterBorder,
                            onMoveRecord = onMoveRecord,
                            onEditRecord = onEditRecord,
                            onDeleteRecord = onDeleteRecord
                        )
                    }
                } else {
                    // Full-Width Fit Mode: 100% fills the desk with zero blank void!
                    val targetRefWidth = if (isLandscape) 900f else 680f
                    val ratio = (containerW.value / targetRefWidth).coerceIn(0.46f, 1.0f)
                    WhiteA4PaperSheet(
                        width = containerW,
                        compactScale = ratio,
                        header = header,
                        records = records,
                        showOuterBorder = printSettings.showOuterBorder,
                        onMoveRecord = onMoveRecord,
                        onEditRecord = onEditRecord,
                        onDeleteRecord = onDeleteRecord
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentedChipButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WhiteA4PaperSheet(
    width: Dp,
    compactScale: Float,
    header: GlobalHeader,
    records: List<DalilRecord>,
    showOuterBorder: Boolean,
    onMoveRecord: (Int, Int) -> Unit,
    onEditRecord: (Int) -> Unit,
    onDeleteRecord: (Int) -> Unit
) {
    val sheetPadding = (12f * compactScale).coerceAtLeast(6f).dp
    val headerTextSize = (11f * compactScale).coerceAtLeast(6.5f).sp
    val titleTextSize = (15.5f * compactScale).coerceAtLeast(9.5f).sp
    val thTextSize = (10.5f * compactScale).coerceAtLeast(6.2f).sp
    val cellTextSize = (10.5f * compactScale).coerceAtLeast(6.2f).sp
    val cellPadH = (4f * compactScale).coerceAtLeast(1.8f).dp
    val cellPadV = (5f * compactScale).coerceAtLeast(2.5f).dp
    val iconSize = (16f * compactScale).coerceAtLeast(10f).dp

    Surface(
        modifier = Modifier
            .width(width)
            .border(
                width = if (showOuterBorder) 1.5.dp else 1.dp,
                color = if (showOuterBorder) Color.Black else Color(0xFFDADCE0),
                shape = RoundedCornerShape(2.dp)
            ),
        color = Color.White,
        shape = RoundedCornerShape(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(sheetPadding),
            verticalArrangement = Arrangement.spacedBy((8f * compactScale).coerceAtLeast(4f).dp)
        ) {
            // 3-Column Header matching Google Chrome PDF output
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left column
                Column(modifier = Modifier.weight(1f)) {
                    HeaderMetaLine("জেলা:", header.district.ifBlank { "-" }, headerTextSize)
                    HeaderMetaLine("উপজেলা:", header.upazila.ifBlank { "-" }, headerTextSize)
                    HeaderMetaLine("মৌজা:", header.mouza.ifBlank { "-" }, headerTextSize)
                }

                // Center title
                Box(
                    modifier = Modifier.weight(1.2f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "দলিলে জমির হিসাব",
                        color = Color.Black,
                        fontSize = titleTextSize,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        textAlign = TextAlign.Center
                    )
                }

                // Right column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "প্রস্তুতকারক:",
                        color = Color.Black,
                        fontSize = headerTextSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = header.creator.ifBlank { "-" },
                        color = Color.Black,
                        fontSize = headerTextSize,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("তারিখ: ") }
                            append(header.date.ifBlank { BanglaUtils.getTodayDateBangla() })
                        },
                        color = Color.Black,
                        fontSize = headerTextSize,
                        textAlign = TextAlign.End
                    )
                }
            }

            // 10-column Table (weights match index.html: 4.5, 8, 11, 18.25, 18.25, 7.5, 8.5, 8.5, 8.5, 7)
            val weights = floatArrayOf(4.5f, 8f, 11f, 18.25f, 18.25f, 7.5f, 8.5f, 8.5f, 8.5f, 7f)
            val headers = arrayOf("নং", "দলিল নং", "তারিখ", "গ্রহীতা(গণ)", "দাতা(গণ)", "মোট জমি", "খতিয়ান", "খতিয়ানের জমি", "দাগ নং", "অ্যাকশন")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.9.dp, Color.Black)
            ) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .background(Color(0xFFEDEDED))
                ) {
                    headers.forEachIndexed { idx, title ->
                        Box(
                            modifier = Modifier
                                .weight(weights[idx])
                                .fillMaxHeight()
                                .border(0.4.dp, Color.Black)
                                .padding(horizontal = cellPadH, vertical = cellPadV),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = Color.Black,
                                fontSize = thTextSize,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = thTextSize * 1.2f
                            )
                        }
                    }
                }

                // Table Body Records
                records.forEachIndexed { recIdx, rec ->
                    val kList = if (rec.khatians.isEmpty()) listOf(KhatianEntry(type = "", no = "-", land = "-", dag = "-")) else rec.khatians
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        // Col 1: Serial
                        TableSpanCell(
                            modifier = Modifier.weight(weights[0]),
                            text = BanglaUtils.toBanglaNum(recIdx + 1),
                            fontSize = cellTextSize,
                            padH = cellPadH,
                            padV = cellPadV
                        )
                        // Col 2: Dalil No
                        TableSpanCell(
                            modifier = Modifier.weight(weights[1]),
                            text = BanglaUtils.toBanglaNum(rec.dalilNo.ifBlank { "-" }),
                            fontSize = cellTextSize,
                            padH = cellPadH,
                            padV = cellPadV
                        )
                        // Col 3: Date
                        TableSpanCell(
                            modifier = Modifier.weight(weights[2]),
                            text = BanglaUtils.toBanglaNum(rec.date.ifBlank { "-" }),
                            fontSize = cellTextSize,
                            padH = cellPadH,
                            padV = cellPadV
                        )
                        // Col 4: Receivers
                        PeopleSpanCell(
                            modifier = Modifier.weight(weights[3]),
                            people = rec.receivers,
                            landPrefix = "জমি",
                            fontSize = cellTextSize,
                            padH = cellPadH,
                            padV = cellPadV
                        )
                        // Col 5: Donors
                        PeopleSpanCell(
                            modifier = Modifier.weight(weights[4]),
                            people = rec.donors,
                            landPrefix = "বিক্রি",
                            fontSize = cellTextSize,
                            padH = cellPadH,
                            padV = cellPadV
                        )
                        // Col 6: Total Land
                        val totalLandDisplay = if (rec.totalLand.isNotBlank() && rec.totalLand != "-") {
                            BanglaUtils.formatShatak(rec.totalLand)
                        } else "-"
                        TableSpanCell(
                            modifier = Modifier.weight(weights[5]),
                            text = totalLandDisplay,
                            fontSize = cellTextSize,
                            padH = cellPadH,
                            padV = cellPadV
                        )

                        // Cols 7..9: Multi-khatian stacked sub-rows (rowspan equivalent)
                        val kWeightSum = weights[6] + weights[7] + weights[8]
                        Column(
                            modifier = Modifier
                                .weight(kWeightSum)
                                .fillMaxHeight()
                        ) {
                            kList.forEach { kh ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .height(IntrinsicSize.Min)
                                ) {
                                    // Col 7: Khatian Type + No
                                    val kLabel = buildAnnotatedString {
                                        when {
                                            kh.type.isNotBlank() && kh.no.isNotBlank() && kh.no != "-" -> {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("${kh.type}: ") }
                                                append(BanglaUtils.toBanglaNum(kh.no))
                                            }
                                            kh.type.isNotBlank() -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(kh.type) }
                                            kh.no.isNotBlank() && kh.no != "-" -> append(BanglaUtils.toBanglaNum(kh.no))
                                            else -> append("-")
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(weights[6])
                                            .fillMaxHeight()
                                            .border(0.4.dp, Color.Black)
                                            .padding(horizontal = cellPadH, vertical = cellPadV),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = kLabel,
                                            color = Color.Black,
                                            fontSize = cellTextSize,
                                            textAlign = TextAlign.Center,
                                            lineHeight = cellTextSize * 1.25f
                                        )
                                    }

                                    // Col 8: Khatian Land
                                    Box(
                                        modifier = Modifier
                                            .weight(weights[7])
                                            .fillMaxHeight()
                                            .border(0.4.dp, Color.Black)
                                            .padding(horizontal = cellPadH, vertical = cellPadV),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (kh.land.isNotBlank() && kh.land != "-") BanglaUtils.formatShatak(kh.land) else "-",
                                            color = Color.Black,
                                            fontSize = cellTextSize,
                                            textAlign = TextAlign.Center,
                                            lineHeight = cellTextSize * 1.25f
                                        )
                                    }

                                    // Col 9: Dag No
                                    Box(
                                        modifier = Modifier
                                            .weight(weights[8])
                                            .fillMaxHeight()
                                            .border(0.4.dp, Color.Black)
                                            .padding(horizontal = cellPadH, vertical = cellPadV),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = BanglaUtils.formatDagList(kh.dag),
                                            color = Color.Black,
                                            fontSize = cellTextSize,
                                            textAlign = TextAlign.Center,
                                            lineHeight = cellTextSize * 1.25f
                                        )
                                    }
                                }
                            }
                        }

                        // Col 10: Action Controls (Up / Down / Edit / Delete)
                        Box(
                            modifier = Modifier
                                .weight(weights[9])
                                .fillMaxHeight()
                                .border(0.4.dp, Color.Black)
                                .padding(horizontal = 1.dp, vertical = cellPadV),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable(enabled = recIdx > 0) { onMoveRecord(recIdx, -1) }
                                            .padding(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = "উপরে নিন",
                                            tint = if (recIdx > 0) Color(0xFF0F9D58) else Color(0xFFCCCCCC),
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable(enabled = recIdx < records.lastIndex) { onMoveRecord(recIdx, 1) }
                                            .padding(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = "নিচে নিন",
                                            tint = if (recIdx < records.lastIndex) Color(0xFF0F9D58) else Color(0xFFCCCCCC),
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { onEditRecord(recIdx) }
                                            .padding(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "এডিট করুন",
                                            tint = Color(0xFF1A73E8),
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { onDeleteRecord(recIdx) }
                                            .padding(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "ডিলিট করুন",
                                            tint = Color(0xFFD93025),
                                            modifier = Modifier.size(iconSize)
                                        )
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

@Composable
private fun HeaderMetaLine(
    label: String,
    value: String,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$label ") }
            append(value)
        },
        color = Color.Black,
        fontSize = fontSize,
        lineHeight = fontSize * 1.35f
    )
}

@Composable
private fun TableSpanCell(
    modifier: Modifier,
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    padH: Dp,
    padV: Dp
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(0.4.dp, Color.Black)
            .padding(horizontal = padH, vertical = padV),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            lineHeight = fontSize * 1.25f
        )
    }
}

@Composable
private fun PeopleSpanCell(
    modifier: Modifier,
    people: List<PersonEntry>,
    landPrefix: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    padH: Dp,
    padV: Dp
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(0.4.dp, Color.Black)
            .padding(horizontal = padH, vertical = padV),
        contentAlignment = Alignment.CenterStart
    ) {
        if (people.isEmpty()) {
            Text("-", color = Color.Black, fontSize = fontSize)
        } else {
            val multi = people.size > 1
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                people.forEachIndexed { idx, p ->
                    val prefix = if (multi) "${BanglaUtils.toBanglaNum(idx + 1)}. " else ""
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("$prefix${p.name.ifBlank { "-" }}")
                            }
                            if (p.guardian.isNotBlank()) {
                                append("\n${p.relation}: ${p.guardian}")
                            }
                            if (p.mother.isNotBlank()) {
                                append("\nমাতা: ${p.mother}")
                            }
                            val cleanLand = BanglaUtils.cleanShatakForEdit(p.land)
                            if (cleanLand.isNotBlank()) {
                                append("\n$landPrefix: ${BanglaUtils.formatShatak(cleanLand)}")
                            }
                        },
                        color = Color.Black,
                        fontSize = fontSize,
                        lineHeight = fontSize * 1.25f
                    )
                    if (idx < people.lastIndex) {
                        HorizontalDivider(color = Color(0xFF999999), thickness = 0.6.dp)
                    }
                }
            }
        }
    }
}
