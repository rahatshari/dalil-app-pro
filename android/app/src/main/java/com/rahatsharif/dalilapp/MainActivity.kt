package com.rahatsharif.dalilapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rahatsharif.dalilapp.ui.screens.MainScreen
import com.rahatsharif.dalilapp.ui.theme.DalilFigureTheme
import com.rahatsharif.dalilapp.util.DalilRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = DalilRepository(applicationContext)

        setContent {
            var themePreference by remember {
                mutableStateOf(repository.getThemePreference())
            }

            DalilFigureTheme(themePreference = themePreference) {
                MainScreen(
                    repository = repository,
                    themePreference = themePreference,
                    onThemePreferenceChange = { nextPref ->
                        themePreference = nextPref
                        repository.setThemePreference(nextPref)
                    }
                )
            }
        }
    }
}
