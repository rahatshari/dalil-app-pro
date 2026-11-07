package com.rahatsharif.dalilapp.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahatsharif.dalilapp.ui.theme.PrimaryBlue
import com.rahatsharif.dalilapp.ui.theme.SecondaryTeal
import com.rahatsharif.dalilapp.util.BanglaUtils
import com.rahatsharif.dalilapp.util.UpdateInfo
import com.rahatsharif.dalilapp.util.UpdateManager
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    currentVersion: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    AlertDialog(
        onDismissRequest = {
            if (!isDownloading) onDismiss()
        },
        icon = {
            Icon(
                Icons.Default.SystemUpdate,
                contentDescription = "আপডেট",
                tint = PrimaryBlue,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "নতুন আপডেট উপলব্ধ!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "বর্তমান: v${BanglaUtils.toBanglaNum(currentVersion)}  ➜  নতুন: v${BanglaUtils.toBanglaNum(updateInfo.latestVersion)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Text(
                        text = "নতুন যা যুক্ত হয়েছে:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = SecondaryTeal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = updateInfo.releaseNotes,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ডাউনলোড হচ্ছে: ${(downloadProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isDownloading) {
                        isDownloading = true
                        scope.launch {
                            val apkFile = UpdateManager.downloadApk(
                                context = context,
                                downloadUrl = updateInfo.downloadUrl,
                                apkName = updateInfo.apkName,
                                onProgress = { progress ->
                                    downloadProgress = progress
                                }
                            )
                            isDownloading = false
                            if (apkFile != null) {
                                UpdateManager.installApk(context, apkFile)
                                onDismiss()
                            } else {
                                Toast.makeText(context, "ডাউনলোড ব্যর্থ হয়েছে। অনুগ্রহ করে ইন্টারনেট সংযোগ চেক করুন।", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                enabled = !isDownloading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(if (isDownloading) "ডাউনলোড হচ্ছে..." else "এখনই আপডেট করুন")
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("পরে")
                }
            }
        }
    )
}
