package com.example.gamebooster.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.gamebooster.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppsRepository @Inject constructor(
    private val context: Context
) {
    
    suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val installedApps = mutableListOf<InstalledApp>()
        
        try {
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            for (appInfo in packages) {
                // Filtrar solo aplicaciones del usuario (no del sistema)
                if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    val appName = appInfo.loadLabel(packageManager).toString()
                    val icon = appInfo.loadIcon(packageManager)
                    
                    // Detectar si es un juego basado en el nombre o categoría
                    val isGame = isGameApp(appName, appInfo.packageName)
                    val category = getAppCategory(appName, appInfo.packageName)
                    
                    installedApps.add(
                        InstalledApp(
                            packageName = appInfo.packageName,
                            appName = appName,
                            icon = icon,
                            isGame = isGame,
                            category = category
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Ordenar por nombre y devolver solo los primeros 10 juegos
        installedApps
            .filter { it.isGame }
            .sortedBy { it.appName }
            .take(10)
    }
    
    private fun isGameApp(appName: String, packageName: String): Boolean {
        val gameKeywords = listOf(
            "game", "juego", "play", "gaming", "battle", "war", "fight", "racing",
            "puzzle", "adventure", "rpg", "strategy", "arcade", "simulator",
            "pubg", "cod", "fortnite", "minecraft", "roblox", "genshin",
            "clash", "candy", "subway", "temple", "angry", "fruit", "ninja"
        )
        
        val lowerAppName = appName.lowercase()
        val lowerPackageName = packageName.lowercase()
        
        return gameKeywords.any { keyword ->
            lowerAppName.contains(keyword) || lowerPackageName.contains(keyword)
        }
    }
    
    private fun getAppCategory(appName: String, packageName: String): String {
        val lowerAppName = appName.lowercase()
        val lowerPackageName = packageName.lowercase()
        
        return when {
            lowerAppName.contains("pubg") || lowerPackageName.contains("pubg") -> "battle_royale"
            lowerAppName.contains("cod") || lowerPackageName.contains("cod") -> "fps"
            lowerAppName.contains("genshin") || lowerPackageName.contains("genshin") -> "rpg"
            lowerAppName.contains("minecraft") || lowerPackageName.contains("minecraft") -> "sandbox"
            lowerAppName.contains("roblox") || lowerPackageName.contains("roblox") -> "social"
            lowerAppName.contains("racing") || lowerAppName.contains("car") -> "racing"
            lowerAppName.contains("puzzle") -> "puzzle"
            lowerAppName.contains("strategy") -> "strategy"
            lowerAppName.contains("arcade") -> "arcade"
            else -> "action"
        }
    }
    
    suspend fun launchApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
} 