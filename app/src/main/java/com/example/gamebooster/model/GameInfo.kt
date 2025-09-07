package com.example.gamebooster.model

import android.graphics.drawable.Drawable

data class GameInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isInstalled: Boolean = false,
    val rating: Float = 0.0f,
    val downloads: String = "",
    val category: String = "",
    val description: String = "",
    val size: String = "",
    val lastUsed: Long = 0L,
    val isRunning: Boolean = false,
    val isOptimized: Boolean = false,
    val trailerUrl: String? = null,
    val screenshots: List<String> = emptyList(),
    val releaseDate: String = "",
    val developer: String = "",
    val price: String = "Free",
    val inAppPurchases: Boolean = false,
    val multiplayer: Boolean = false,
    val controllerSupport: Boolean = false
)