package com.rahatsharif.dalilapp.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DalilWebPrintScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var webViewRef: WebView? = null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("দলিল ফিগার - ওয়েব ভিউ ও প্রিন্ট") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        webViewRef?.let { wv ->
                            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                            val printAdapter = wv.createPrintDocumentAdapter("Dalil_Figure_Web")
                            val printAttributes = PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .build()
                            printManager?.print("Dalil_Figure_Print", printAdapter, printAttributes)
                        }
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "প্রিন্ট করুন")
                    }
                }
            )
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT

                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()

                    // Bridge Android native printing to javascript
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun nativePrint() {
                            post {
                                val printManager = ctx.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                                val printAdapter = createPrintDocumentAdapter("Dalil_Figure_Print")
                                printManager?.print("Dalil_Figure", printAdapter, PrintAttributes.Builder().build())
                            }
                        }
                    }, "AndroidPrint")

                    // Load bundled offline html
                    loadUrl("file:///android_asset/www/index.html")
                    webViewRef = this
                }
            }
        )
    }
}
