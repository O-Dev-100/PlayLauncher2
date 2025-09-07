package com.example.gamebooster.viewmodel

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.content.SharedPreferences

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val prefs: SharedPreferences
) : ViewModel() {
    private val _selectedLanguage = MutableStateFlow(prefs.getString("selected_language", "English") ?: "English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _customThemeIndex = MutableStateFlow(prefs.getInt("custom_theme_index", 0))
    val customThemeIndex: StateFlow<Int> = _customThemeIndex.asStateFlow()

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
        prefs.edit().putString("selected_language", language).apply()
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("is_dark_theme", enabled).apply()
    }

    fun setCustomThemeIndex(index: Int) {
        _customThemeIndex.value = index
        prefs.edit().putInt("custom_theme_index", index).apply()
    }

    fun updateLocale(context: Context, language: String) {
        val locale = when (language) {
            "Español" -> Locale("es")
            else -> Locale("en")
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
} 