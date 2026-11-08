package com.rahatsharif.dalilapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.rahatsharif.dalilapp.util.BanglaUtils
import com.rahatsharif.dalilapp.util.UpdateInfo
import com.rahatsharif.dalilapp.util.UpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    // Activity Result Launcher for File Upload / JSON Import
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, data)
            fileUploadCallback?.onReceiveValue(uris)
        } else {
            fileUploadCallback?.onReceiveValue(null)
        }
        fileUploadCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar styling
        window.statusBarColor = getColor(R.color.primary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = 0
        }

        // Layout container
        val rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // WebView hosting the authentic original app
        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(getColor(R.color.bg_light))
        }

        // Top loading indicator
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                8
            )
            isIndeterminate = false
            max = 100
            visibility = View.GONE
        }

        rootLayout.addView(webView)
        rootLayout.addView(progressBar)
        setContentView(rootLayout)

        configureWebView()

        // Handle hardware back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("অ্যাপ বন্ধ করুন")
                        .setMessage("আপনি কি দলিল ফিগার অ্যাপ থেকে বের হতে চান?")
                        .setPositiveButton("হ্যাঁ") { _, _ -> finish() }
                        .setNegativeButton("না", null)
                        .show()
                }
            }
        })

        // Check for updates in the background
        checkForAppUpdates()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // Javascript Interface for native bridges (printing, backup, updates)
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidNative")

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                } else {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onShowFileChooser(
                view: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback

                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }

                try {
                    filePickerLauncher.launch(intent)
                } catch (e: Exception) {
                    fileUploadCallback = null
                    Toast.makeText(this@MainActivity, "ফাইল সিলেক্টর চালু করা যায়নি", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE

                // Hook window.print() so the original button triggers Android's Print Spooler directly
                val printHookJs = """
                    (function() {
                        window.print = function() {
                            if (window.AndroidNative && window.AndroidNative.printPage) {
                                window.AndroidNative.printPage();
                            }
                        };
                    })();
                """.trimIndent()
                view?.evaluateJavascript(printHookJs, null)
            }
        }

        // Intercept blob / file download requests for JSON backups
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            handleDownload(url, contentDisposition, mimetype)
        }

        // Load the original offline HTML app
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    private fun handleDownload(url: String, contentDisposition: String, mimetype: String) {
        try {
            if (url.startsWith("blob:") || url.startsWith("data:")) {
                // Trigger download export bridge via JS
                Toast.makeText(this, "ব্যাকআপ ফাইল সংরক্ষণ হচ্ছে...", Toast.LENGTH_SHORT).show()
            } else {
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    addRequestHeader("User-Agent", webView.settings.userAgentString)
                    setDescription("ফাইল ডাউনলোড হচ্ছে...")
                    setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        URLUtil.guessFileName(url, contentDisposition, mimetype)
                    )
                }
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this, "ডাউনলোড শুরু হয়েছে", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerPrint() {
        runOnUiThread {
            try {
                val printManager = getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "দলিল ফিগার প্রিন্ট ও সংরক্ষণ"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                    .setMinMargins(PrintAttributes.Margins(400, 400, 400, 400))
                    .build()
                printManager?.print(jobName, printAdapter, printAttributes)
            } catch (e: Exception) {
                Toast.makeText(this, "প্রিন্ট সেবা চালু করা যায়নি: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkForAppUpdates(manual: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val currentVersion = BuildConfig.VERSION_NAME
            val updateInfo = UpdateManager.checkForUpdate(currentVersion)
            withContext(Dispatchers.Main) {
                if (updateInfo != null) {
                    showUpdatePrompt(updateInfo)
                } else if (manual) {
                    Toast.makeText(
                        this@MainActivity,
                        "আপনার অ্যাপটি আপ-টু-ডেট আছে (v${BanglaUtils.toBanglaNum(currentVersion)})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showUpdatePrompt(updateInfo: UpdateInfo) {
        val currentVer = BuildConfig.VERSION_NAME
        val message = StringBuilder().apply {
            append("বর্তমান ভার্সন: v${BanglaUtils.toBanglaNum(currentVer)}\n")
            append("নতুন ভার্সন: v${BanglaUtils.toBanglaNum(updateInfo.latestVersion)}\n\n")
            if (updateInfo.releaseNotes.isNotBlank()) {
                append("পরিবর্তনসমূহ:\n${updateInfo.releaseNotes}")
            }
        }.toString()

        AlertDialog.Builder(this)
            .setTitle("নতুন আপডেট উপলব্ধ!")
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton("এখনই আপডেট করুন") { _, _ ->
                downloadAndInstallApk(updateInfo)
            }
            .setNegativeButton("পরে", null)
            .show()
    }

    private fun downloadAndInstallApk(updateInfo: UpdateInfo) {
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("আপডেট ডাউনলোড হচ্ছে...")
            .setMessage("অনুগ্রহ করে অপেক্ষা করুন...")
            .setCancelable(false)
            .create()

        progressDialog.show()

        lifecycleScope.launch(Dispatchers.IO) {
            val apkFile = UpdateManager.downloadApk(
                context = this@MainActivity,
                downloadUrl = updateInfo.downloadUrl,
                apkName = updateInfo.apkName,
                onProgress = { progress ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        progressDialog.setMessage("ডাউনলোড হচ্ছে: ${(progress * 100).toInt()}%")
                    }
                }
            )

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (apkFile != null) {
                    UpdateManager.installApk(this@MainActivity, apkFile)
                } else {
                    Toast.makeText(this@MainActivity, "ডাউনলোড ব্যর্থ হয়েছে। ইন্টারনেট সংযোগ চেক করুন।", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // JavaScript interface class exposed to original index.html
    inner class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        fun printPage() {
            triggerPrint()
        }

        @JavascriptInterface
        fun checkUpdate() {
            checkForAppUpdates(manual = true)
        }

        @JavascriptInterface
        fun getAppVersion(): String {
            return BuildConfig.VERSION_NAME
        }

        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
