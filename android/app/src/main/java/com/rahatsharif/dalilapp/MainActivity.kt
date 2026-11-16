package com.rahatsharif.dalilapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.rahatsharif.dalilapp.util.UpdateManager

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingBackupContent: String = "[]"

    private val fileChooserLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = filePathCallback
            filePathCallback = null
            if (callback == null) return@registerForActivityResult

            if (result.resultCode == RESULT_OK && result.data != null) {
                val data = result.data
                val uris = mutableListOf<Uri>()
                val clipData = data?.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        clipData.getItemAt(i)?.uri?.let { uris.add(it) }
                    }
                } else {
                    data?.data?.let { uris.add(it) }
                }
                callback.onReceiveValue(if (uris.isNotEmpty()) uris.toTypedArray() else null)
            } else {
                callback.onReceiveValue(null)
            }
        }

    private val createBackupLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                try {
                    contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(pendingBackupContent.toByteArray(Charsets.UTF_8))
                        out.flush()
                    }
                    Toast.makeText(
                        this,
                        "ব্যাকআপ ফাইল সফলভাবে সংরক্ষিত হয়েছে!",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "ব্যাকআপ সেভ করতে সমস্যা হয়েছে: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Force pure light mode so WebView & print output never get a gray/dark tint
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
        }
        setContentView(webView)

        configureWebView(webView)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::webView.isInitialized && webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })

        webView.loadUrl("file:///android_asset/www/index.html")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(view: WebView) {
        val settings = view.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        @Suppress("DEPRECATION")
        settings.allowFileAccessFromFileURLs = true
        @Suppress("DEPRECATION")
        settings.allowUniversalAccessFromFileURLs = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            settings.forceDark = WebSettings.FORCE_DARK_OFF
        }

        view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                wv: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return false
                val scheme = url.scheme?.lowercase() ?: ""
                if (scheme == "http" || scheme == "https") {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, url))
                    } catch (_: Exception) {
                    }
                    return true
                }
                return false
            }
        }

        view.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                wv: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback

                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain", "*/*"))
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }

                return try {
                    fileChooserLauncher.launch(Intent.createChooser(intent, "ব্যাকআপ ফাইল নির্বাচন করুন"))
                    true
                } catch (_: Exception) {
                    filePathCallback = null
                    false
                }
            }
        }

        view.setDownloadListener { url, _, _, _, _ ->
            if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (_: Exception) {
                }
            }
        }

        view.addJavascriptInterface(AndroidNativeBridge(), "AndroidNative")
    }

    inner class AndroidNativeBridge {

        @JavascriptInterface
        fun getAppVersion(): String {
            return BuildConfig.VERSION_NAME
        }

        @JavascriptInterface
        fun printPage(orientation: String?) {
            runOnUiThread {
                try {
                    val printManager = getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    if (printManager == null) {
                        Toast.makeText(
                            this@MainActivity,
                            "প্রিন্ট সার্ভিস পাওয়া যায়নি",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@runOnUiThread
                    }

                    val isLandscape = orientation.equals("landscape", ignoreCase = true)
                    val mediaSize = if (isLandscape) {
                        PrintAttributes.MediaSize.ISO_A4.asLandscape()
                    } else {
                        PrintAttributes.MediaSize.ISO_A4.asPortrait()
                    }

                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(mediaSize)
                        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                        .build()

                    val printAdapter = webView.createPrintDocumentAdapter("Dalil_Figure_Print")
                    printManager.print("Dalil_Figure_Print", printAdapter, printAttributes)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "প্রিন্ট চালু করা যায়নি: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        @JavascriptInterface
        fun checkUpdate() {
            UpdateManager.checkForUpdate(this@MainActivity, BuildConfig.VERSION_NAME)
        }

        @JavascriptInterface
        fun saveJsonFile(filename: String?, content: String?) {
            runOnUiThread {
                pendingBackupContent = content ?: "[]"
                val targetName = if (filename.isNullOrBlank()) "Dalil_Backup.json" else filename
                createBackupLauncher.launch(targetName)
            }
        }
    }

    override fun onDestroy() {
        filePathCallback?.onReceiveValue(null)
        filePathCallback = null
        super.onDestroy()
    }
}
