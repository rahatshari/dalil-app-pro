package com.rahatsharif.dalilapp.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class UpdateInfo(
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkName: String
)

object UpdateManager {

    private const val GITHUB_REPO_OWNER = "rahatshari"
    private const val GITHUB_REPO_NAME = "dalil-app-pro"
    private const val GITHUB_API_URL =
        "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest"

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var isChecking = false

    fun checkForUpdate(activity: Activity, currentVersionName: String) {
        if (isChecking) return
        isChecking = true

        executor.execute {
            val info = fetchLatestUpdate(currentVersionName)
            isChecking = false
            if (info != null) {
                mainHandler.post {
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        showUpdateDialog(activity, currentVersionName, info)
                    }
                }
            }
        }
    }

    private fun fetchLatestUpdate(currentVersionName: String): UpdateInfo? {
        return try {
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "DalilFigureApp")
            }

            if (connection.responseCode != 200) return null

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            val tagName = json.optString("tag_name", "")
            val cleanRemoteVersion = tagName.replace("[^0-9.]".toRegex(), "")
            val cleanCurrentVersion = currentVersionName.replace("[^0-9.]".toRegex(), "")
            if (cleanRemoteVersion.isBlank()) return null

            if (isRemoteVersionNewer(cleanCurrentVersion, cleanRemoteVersion)) {
                val releaseNotes = json.optString("body", "নতুন সংস্করণ উপলব্ধ।")
                val assets = json.optJSONArray("assets")
                var downloadUrl: String? = null
                var apkName = "dalil-figure-v$cleanRemoteVersion.apk"

                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val assetObj = assets.optJSONObject(i) ?: continue
                        val name = assetObj.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = assetObj.optString("browser_download_url", "")
                            if (downloadUrl.isNotBlank()) {
                                apkName = name
                                break
                            }
                        }
                    }
                }

                if (!downloadUrl.isNullOrBlank()) {
                    return UpdateInfo(
                        latestVersion = cleanRemoteVersion,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl,
                        apkName = apkName
                    )
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun showUpdateDialog(activity: Activity, currentVersion: String, info: UpdateInfo) {
        val density = activity.resources.displayMetrics.density
        val pad = (18 * density).toInt()

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, (10 * density).toInt(), pad, (6 * density).toInt())
        }

        val verText = TextView(activity).apply {
            text = "বর্তমান: v$currentVersion  ➜  নতুন: v${info.latestVersion}"
            textSize = 13.5f
            setPadding(0, 0, 0, (10 * density).toInt())
        }

        val notesText = TextView(activity).apply {
            text = info.releaseNotes
            textSize = 13f
        }

        val statusText = TextView(activity).apply {
            text = ""
            textSize = 12.5f
            setPadding(0, (12 * density).toInt(), 0, (6 * density).toInt())
            visibility = View.GONE
        }

        val progressBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            visibility = View.GONE
        }

        container.addView(verText)
        container.addView(notesText)
        container.addView(statusText)
        container.addView(progressBar)

        val dialog = AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
            .setTitle("নতুন আপডেট উপলব্ধ!")
            .setView(container)
            .setPositiveButton("এখনই আপডেট করুন", null)
            .setNegativeButton("পরে") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveBtn.setOnClickListener {
                positiveBtn.isEnabled = false
                negativeBtn.isEnabled = false
                dialog.setCancelable(false)
                statusText.visibility = View.VISIBLE
                statusText.text = "ডাউনলোড হচ্ছে: ০%"
                progressBar.visibility = View.VISIBLE

                executor.execute {
                    val apkFile = downloadApk(activity, info.downloadUrl, info.apkName) { pct ->
                        mainHandler.post {
                            progressBar.progress = pct
                            statusText.text = "ডাউনলোড হচ্ছে: $pct%"
                        }
                    }

                    mainHandler.post {
                        if (apkFile != null) {
                            dialog.dismiss()
                            installApk(activity, apkFile)
                        } else {
                            positiveBtn.isEnabled = true
                            negativeBtn.isEnabled = true
                            dialog.setCancelable(true)
                            statusText.text = "ডাউনলোড ব্যর্থ হয়েছে। ইন্টারনেট সংযোগ চেক করুন।"
                            Toast.makeText(
                                activity,
                                "ডাউনলোড ব্যর্থ হয়েছে। অনুগ্রহ করে ইন্টারনেট সংযোগ চেক করুন।",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    private fun isRemoteVersionNewer(current: String, remote: String): Boolean {
        return try {
            val currParts = current.split(".").mapNotNull { it.toIntOrNull() }
            val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(currParts.size, remoteParts.size)
            for (i in 0 until maxLen) {
                val c = currParts.getOrElse(i) { 0 }
                val r = remoteParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            false
        } catch (_: Exception) {
            remote != current
        }
    }

    private fun downloadApk(
        context: Context,
        downloadUrl: String,
        apkName: String,
        onProgress: (Int) -> Unit
    ): File? {
        return try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val apkFile = File(downloadDir, apkName)

            var currentUrl = downloadUrl
            var connection: HttpURLConnection? = null
            var redirectCount = 0
            while (redirectCount < 6) {
                val conn = URL(currentUrl).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = true
                val code = conn.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == 307 || code == 308
                ) {
                    val redirectUrl = conn.getHeaderField("Location")
                    if (!redirectUrl.isNullOrBlank()) {
                        currentUrl = redirectUrl
                        redirectCount++
                        continue
                    }
                }
                connection = conn
                break
            }

            if (connection == null) return null

            val fileLength = connection.contentLength
            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    while (input.read(buffer).also { count = it } != -1) {
                        total += count
                        output.write(buffer, 0, count)
                        if (fileLength > 0) {
                            val pct = ((total * 100L) / fileLength).toInt().coerceIn(0, 100)
                            onProgress(pct)
                        }
                    }
                    output.flush()
                }
            }
            apkFile
        } catch (_: Exception) {
            null
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    Toast.makeText(
                        context,
                        "আপডেট ইনস্টল করতে অনুমতি চালু করে আবার চেষ্টা করুন।",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "APK ইনস্টলার খোলা যায়নি।", Toast.LENGTH_SHORT).show()
        }
    }
}
