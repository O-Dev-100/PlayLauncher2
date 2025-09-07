package com.example.gamebooster.utils

import com.example.gamebooster.R

object GameIconUtils {
    fun getLocalGameIcon(packageName: String): Int? {
        return when (packageName) {
            "com.tencent.ig", "com.pubg.krmobile", "com.pubg.imobile" -> R.drawable.ic_pubg
            "com.activision.callofduty.shooter" -> R.drawable.ic_cod_mobile
            "com.epicgames.fortnite" -> R.drawable.ic_fortnite
            "com.roblox.client" -> R.drawable.ic_roblox
            "com.mojang.minecraftpe" -> R.drawable.ic_minecraft
            else -> null
        }
    }
} 