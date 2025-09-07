package com.example.gamebooster.screens
import android.content.Context
import java.util.Locale


fun setLocale(context: Context, language: String) {
    val locale = when (language) {
        "Spanish" -> Locale("es")
        else -> Locale("en")
    }
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}