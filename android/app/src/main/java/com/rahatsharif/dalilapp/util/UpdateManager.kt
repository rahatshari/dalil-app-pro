package com.rahatsharif.dalilapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkName: String
)

object UpdateManager {

    private const val GITHUB_REPO_OWNER = "rahatshari"
    private const val GITHUB_REPO_NAME = "dalil-app-pro"
    private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest"

    suspend fun checkForUpdate(currentVersionName: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "DalilFigureApp")
            }

            if (connection.responseCode != 200) return@withContext null

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = Gson().fromJson(responseText, JsonObject::class.java)

            val tagName = json.get("tag_name")?.asString ?: return@withContext null
            val cleanRemoteVersion = tagName.replace("[^0-9.]".toRegex(), "")
            val cleanCurrentVersion = currentVersionName.replace("[^0-9.]".toRegex(), "")

            if (isRemoteVersionNewer(cleanCurrentVersion, cleanRemoteVersion)) {
                val releaseNotes = json.get("body")?.asString ?: "নতুন সংস্করণ উপলব্ধ।"
                val assets = json.getAsJsonArray("assets")
                var downloadUrl: String? = null
                var apkName = "dalil-figure-v$cleanRemoteVersion.apk"

                if (assets != null) {
                    for (element in assets) {
                        val assetObj = element.asJsonObject
                        val name = assetObj.get("name")?.asString ?: ""
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = assetObj.get("browser_download_url")?.asString
                            apkName = name
                            break
                        }
                    }
                }

                if (downloadUrl != null) {
                    return@withContext UpdateInfo(
                        latestVersion = cleanRemoteVersion,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl,
                        apkName = apkName
                    )
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isRemoteVersionNewer(current: String, remote: String): Boolean {
        try {
            val currParts = current.split(".").mapNotNull { it.toIntOrNull() }
            val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(currParts.size, remoteParts.size)
            for (i in 0 until maxLen) {
                val c = currParts.getOrElse(i) { 0 }
                val r = remoteParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        } catch (e: Exception) {
            return remote != current
        }
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        apkName: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val apkFile = File(downloadDir, apkName)

            var currentUrl = downloadUrl
            var connection: HttpURLConnection? = null
            var redirectCount = 0
            while (redirectCount < 6) {
                val urlObj = URL(currentUrl)
                val conn = urlObj.openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = true
                val code = conn.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == 307 || code == 308
                ) {
                    val redirectUrl = conn.getHeaderField("Location")
                    if (redirectUrl != null) {
                        currentUrl = redirectUrl
                        redirectCount++
                        continue
                    }
                }
                connection = conn
                break
            }

            if (connection == null) return@withContext null

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
                            val progress = (total.toFloat() / fileLength.toFloat()).coerceIn(0f, 1f)
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                    output.flush()
                }
            }
            apkFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            // Check unknown sources permission on Android 8+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
