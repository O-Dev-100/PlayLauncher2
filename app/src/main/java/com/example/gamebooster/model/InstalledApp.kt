package com.example.gamebooster.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isGame: Boolean = false,
    val category: String = "other"
) 