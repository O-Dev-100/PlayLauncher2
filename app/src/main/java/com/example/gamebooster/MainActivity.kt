package com.example.gamebooster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.gamebooster.navigation.NavGraph
import com.example.gamebooster.ui.theme.GameBoosterTheme
import com.example.gamebooster.viewmodel.BoosterViewModel
import com.example.gamebooster.viewmodel.AppSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.content.SharedPreferences
import android.content.pm.ActivityInfo

@AndroidEntryPoint 
class MainActivity : ComponentActivity() {
    
    private val viewModel: BoosterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
            val appSettingsViewModel: AppSettingsViewModel = viewModel()
            val isDarkTheme by appSettingsViewModel.isDarkTheme.collectAsState()
            val customThemeIndex by appSettingsViewModel.customThemeIndex.collectAsState()
            val selectedLanguage by appSettingsViewModel.selectedLanguage.collectAsState()
            val context = LocalContext.current

            // Aplicar cambio de idioma cuando cambie
            LaunchedEffect(selectedLanguage) {
                appSettingsViewModel.updateLocale(context, selectedLanguage)
            }

            GameBoosterTheme(darkTheme = isDarkTheme, customThemeIndex = customThemeIndex) {
                val navController = rememberNavController()
                val prefs = context.getSharedPreferences("onboarding", MODE_PRIVATE)
                val isFirstLaunch = remember { mutableStateOf(prefs.getBoolean("first_launch", true)) }

                // Manejar notificaciones y redirigir a DashboardScreen o WelcomeScreen
                LaunchedEffect(Unit) {
                    val intent = intent
                    if (isFirstLaunch.value) {
                        navController.navigate("welcome")
                        prefs.edit().putBoolean("first_launch", false).apply()
                        isFirstLaunch.value = false
                    } else if (intent != null && intent.hasExtra("navigate_to")) {
                        when (intent.getStringExtra("navigate_to")) {
                            "boost_feedback" -> navController.navigate("boost_feedback")
                            else -> navController.navigate("game_launcher")
                        }
                    } else if (intent.action != null) {
                        when (intent.action) {
                            "VIEW_STATS" -> navController.navigate("stats")
                            "READ_NEWS" -> navController.navigate("gaming_news")
                            "VIEW_CHANGES" -> navController.navigate("game_launcher")
                            else -> navController.navigate("game_launcher")
                        }
                    } else {
                        navController.navigate("game_launcher")
                    }
                }
                
                NavGraph(
                    navController = navController,
                    boosterViewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    selectedLanguage = selectedLanguage,
                    onThemeChange = { appSettingsViewModel.setDarkTheme(it) },
                    onLanguageChange = { appSettingsViewModel.setLanguage(it) }
                )
            }
        }
    }
}