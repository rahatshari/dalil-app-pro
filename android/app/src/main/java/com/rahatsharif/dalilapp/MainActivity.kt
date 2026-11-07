package com.rahatsharif.dalilapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.rahatsharif.dalilapp.model.DalilProject
import com.rahatsharif.dalilapp.ui.components.UpdateDialog
import com.rahatsharif.dalilapp.ui.screens.DalilWebPrintScreen
import com.rahatsharif.dalilapp.ui.screens.HomeScreen
import com.rahatsharif.dalilapp.ui.theme.DalilFigureTheme
import com.rahatsharif.dalilapp.util.BanglaUtils
import com.rahatsharif.dalilapp.util.UpdateInfo
import com.rahatsharif.dalilapp.util.UpdateManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val PREFS_NAME = "dalil_app_prefs"
    private val KEY_PROJECT = "current_dalil_project"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load cached project or create default
        val initialProject = loadSavedProject() ?: DalilProject()

        setContent {
            DalilFigureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentProject by remember { mutableStateOf(initialProject) }
                    var currentViewMode by remember { mutableStateOf("home") } // "home" or "web"
                    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
                    val coroutineScope = rememberCoroutineScope()

                    val appVersionName = BuildConfig.VERSION_NAME

                    // Auto-check for updates on launch
                    LaunchedEffect(Unit) {
                        try {
                            val update = UpdateManager.checkForUpdate(appVersionName)
                            if (update != null) {
                                availableUpdate = update
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    fun saveProjectState() {
                        saveProject(currentProject)
                    }

                    if (currentViewMode == "web") {
                        DalilWebPrintScreen(
                            onBack = { currentViewMode = "home" }
                        )
                    } else {
                        HomeScreen(
                            project = currentProject,
                            currentVersion = appVersionName,
                            onProjectChange = {
                                saveProjectState()
                            },
                            onOpenWebViewMode = {
                                currentViewMode = "web"
                            },
                            onCheckUpdatesManual = {
                                coroutineScope.launch {
                                    val update = UpdateManager.checkForUpdate(appVersionName)
                                    if (update != null) {
                                        availableUpdate = update
                                    } else {
                                        android.widget.Toast.makeText(
                                            this@MainActivity,
                                            "আপনার অ্যাপটি আপ-টু-ডেট আছে (v$appVersionName)",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }

                    // Render Update Dialog if an update is found
                    availableUpdate?.let { updateInfo ->
                        UpdateDialog(
                            updateInfo = updateInfo,
                            currentVersion = appVersionName,
                            onDismiss = { availableUpdate = null }
                        )
                    }
                }
            }
        }
    }

    private fun saveProject(project: DalilProject) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = BanglaUtils.projectToJson(project)
        prefs.edit().putString(KEY_PROJECT, json).apply()
    }

    private fun loadSavedProject(): DalilProject? {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PROJECT, null) ?: return null
        return BanglaUtils.jsonToProject(json)
    }
}
