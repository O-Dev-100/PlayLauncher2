package com.example.gamebooster.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.gamebooster.model.GameInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import com.example.gamebooster.R // Added import for R.drawable

class GameDataService(private val context: Context) {
    
    companion object {
        private const val YOUTUBE_API_BASE = "https://www.googleapis.com/youtube/v3/search"
        private const val RAWG_API_BASE = "https://api.rawg.io/api"
        
        // Juegos populares con información real
        private val POPULAR_GAMES = listOf(
            GameInfo(
                packageName = "com.activision.callofduty.shooter",
                appName = "Call of Duty: Mobile",
                icon = null,
                isInstalled = false,
                rating = 4.2f,
                downloads = "100M+",
                category = "Action",
                description = "Experience the thrill of Call of Duty on mobile with intense multiplayer battles and stunning graphics.",
                size = "2.1 GB",
                trailerUrl = "https://www.youtube.com/watch?v=example1",
                developer = "Activision",
                price = "Free",
                inAppPurchases = true,
                multiplayer = true,
                controllerSupport = true
            ),
            GameInfo(
                packageName = "com.epicgames.fortnite",
                appName = "Fortnite",
                icon = null,
                isInstalled = false,
                rating = 4.0f,
                downloads = "50M+",
                category = "Battle Royale",
                description = "Build, fight, and survive in this epic battle royale with 100 players.",
                size = "3.5 GB",
                trailerUrl = "https://www.youtube.com/watch?v=example2",
                developer = "Epic Games",
                price = "Free",
                inAppPurchases = true,
                multiplayer = true,
                controllerSupport = true
            ),
            GameInfo(
                packageName = "com.tencent.ig",
                appName = "PUBG Mobile",
                icon = null,
                isInstalled = false,
                rating = 4.1f,
                downloads = "200M+",
                category = "Battle Royale",
                description = "The original battle royale game with realistic graphics and intense gameplay.",
                size = "2.8 GB",
                trailerUrl = "https://www.youtube.com/watch?v=example3",
                developer = "PUBG Corporation",
                price = "Free",
                inAppPurchases = true,
                multiplayer = true,
                controllerSupport = false
            ),
            GameInfo(
                packageName = "com.roblox.client",
                appName = "Roblox",
                icon = null,
                isInstalled = false,
                rating = 4.3f,
                downloads = "150M+",
                category = "Adventure",
                description = "Create, play, and imagine with millions of players in the ultimate virtual universe.",
                size = "1.2 GB",
                trailerUrl = "https://www.youtube.com/watch?v=example4",
                developer = "Roblox Corporation",
                price = "Free",
                inAppPurchases = true,
                multiplayer = true,
                controllerSupport = true
            ),
            GameInfo(
                packageName = "com.mojang.minecraftpe",
                appName = "Minecraft",
                icon = null,
                isInstalled = false,
                rating = 4.5f,
                downloads = "80M+",
                category = "Sandbox",
                description = "Build, explore, and survive in the blocky world of Minecraft.",
                size = "1.8 GB",
                trailerUrl = "https://www.youtube.com/watch?v=example5",
                developer = "Mojang",
                price = "$6.99",
                inAppPurchases = true,
                multiplayer = true,
                controllerSupport = true
            )
        )
    }
    
    suspend fun getInstalledGames(): List<GameInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val myPackageName = context.packageName
        val games = mutableListOf<GameInfo>()
        for (app in installedApps) {
            // Excluir apps del sistema y la propia app
            if ((app.flags and ApplicationInfo.FLAG_SYSTEM == 0) && app.packageName != myPackageName) {
                if (isGame(app, packageManager)) {
                    val gameInfo = GameInfo(
                        packageName = app.packageName,
                        appName = app.loadLabel(packageManager).toString(),
                        icon = app.loadIcon(packageManager),
                        isInstalled = true,
                        rating = getLocalGameRating(app.packageName),
                        downloads = "Installed",
                        category = "",
                        description = getLocalGameDescription(app.packageName),
                        size = getAppSize(app.packageName, packageManager),
                        lastUsed = getLastTimeUsed(app.packageName),
                        trailerUrl = getGameTrailerUrl(app.loadLabel(packageManager).toString()),
                        developer = getLocalGameDeveloper(app.packageName),
                        price = "Free",
                        inAppPurchases = hasInAppPurchases(app.packageName),
                        multiplayer = isMultiplayerGame(app.packageName),
                        controllerSupport = hasControllerSupport(app.packageName)
                    )
                    games.add(gameInfo)
                }
            }
        }
        games.sortedBy { it.appName }
    }

    private fun getLastTimeUsed(packageName: String): Long {
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val end = System.currentTimeMillis()
            val start = end - 1000L * 60L * 60L * 24L * 365L
            val stats = usageStatsManager.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_DAILY, start, end)
            stats?.filter { it.packageName == packageName }
                ?.maxByOrNull { it.lastTimeUsed }
                ?.lastTimeUsed ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun isGame(app: ApplicationInfo, packageManager: PackageManager): Boolean {
        // Categorías comunes de juegos en Android
        val gameCategories = listOf(
            "GAME",
            "GAME_ACTION",
            "GAME_ADVENTURE", 
            "GAME_ARCADE",
            "GAME_BOARD",
            "GAME_CARD",
            "GAME_CASINO",
            "GAME_CASUAL",
            "GAME_EDUCATIONAL",
            "GAME_MUSIC",
            "GAME_PUZZLE",
            "GAME_RACING",
            "GAME_ROLE_PLAYING",
            "GAME_SIMULATION",
            "GAME_SPORTS",
            "GAME_STRATEGY",
            "GAME_TRIVIA",
            "GAME_WORD"
        )
        
        // Palabras clave comunes en nombres de juegos
        val gameKeywords = listOf(
            "game", "play", "battle", "war", "fight", "race", "puzzle", "quiz",
            "poker", "chess", "checkers", "mahjong", "tetris", "snake", "mario",
            "pokemon", "minecraft", "roblox", "fortnite", "pubg", "cod", "fifa",
            "nba", "nfl", "mlb", "nhl", "soccer", "football", "basketball"
        )
        
        try {
            val category = app.category?.toString() ?: ""
            val appName = app.loadLabel(packageManager).toString().lowercase()
            val packageName = app.packageName.lowercase()
            
            return gameCategories.any { category.contains(it, ignoreCase = true) } ||
                   gameKeywords.any { appName.contains(it) || packageName.contains(it) } ||
                   appName.contains("game", ignoreCase = true) ||
                   packageName.contains("game", ignoreCase = true)
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun getAppSize(packageName: String, packageManager: PackageManager): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val file = java.io.File(packageInfo.applicationInfo.sourceDir)
            val sizeInBytes = file.length()
            val sizeInMB = sizeInBytes / (1024 * 1024)
            "${sizeInMB} MB"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private suspend fun getGameTrailerUrl(gameName: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiKey = ApiConfig.getYouTubeApiKey()
            if (apiKey == null) {
                return@withContext null // No API key configurada
            }

            val searchQuery = "$gameName official trailer"
            val encodedQuery = java.net.URLEncoder.encode(searchQuery, "UTF-8")
            val url = "$YOUTUBE_API_BASE?part=snippet&q=$encodedQuery&type=video&maxResults=1&key=$apiKey"

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val response = StringBuilder()
            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
            }

            val jsonResponse = JSONObject(response.toString())
            val items = jsonResponse.getJSONArray("items")

            if (items.length() > 0) {
                val videoId = items.getJSONObject(0).getJSONObject("id").getString("videoId")
                "https://www.youtube.com/watch?v=$videoId"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun searchGames(query: String): List<GameInfo> = withContext(Dispatchers.IO) {
        val games = mutableListOf<GameInfo>()
        
        // Buscar en juegos instalados
        val installedGames = getInstalledGames()
        val matchingInstalled = installedGames.filter { 
            it.appName.contains(query, ignoreCase = true) 
        }
        games.addAll(matchingInstalled)
        
        // Buscar en juegos populares
        val matchingPopular = POPULAR_GAMES.filter { 
            it.appName.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            it.developer.contains(query, ignoreCase = true)
        }
        games.addAll(matchingPopular)
        
        // Buscar en RAWG API si está configurada
        val rawgResults = searchRawgGames(query)
        games.addAll(rawgResults)
        
        games.distinctBy { it.packageName }.sortedBy { it.appName }
    }
    
    private suspend fun searchRawgGames(query: String): List<GameInfo> = withContext(Dispatchers.IO) {
        val games = mutableListOf<GameInfo>()
        
        try {
            val apiKey = ApiConfig.getRawgApiKey()
            if (apiKey == null) {
                return@withContext games
            }
            
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "${ApiConfig.RAWG_API_BASE}/games?search=$encodedQuery&key=$apiKey&page_size=10"
            
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val response = StringBuilder()
            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
            }
            
            val jsonResponse = JSONObject(response.toString())
            val results = jsonResponse.getJSONArray("results")
            
            for (i in 0 until results.length()) {
                val game = results.getJSONObject(i)
                val gameInfo = GameInfo(
                    packageName = "rawg.${game.getString("slug")}",
                    appName = game.getString("name"),
                    icon = null,
                    isInstalled = false,
                    rating = (game.getDouble("rating") * 2).toFloat(), // Convertir de 5 a 10
                    downloads = "Unknown",
                    category = if (game.has("genres") && game.getJSONArray("genres").length() > 0) {
                        game.getJSONArray("genres").getJSONObject(0).getString("name")
                    } else "Action",
                    description = game.optString("description", "").takeIf { it.isNotEmpty() } ?: "Un emocionante juego para disfrutar.",
                    size = "Unknown",
                    trailerUrl = game.optString("clip", null).takeIf { it != "null" && it.isNotEmpty() },
                    developer = if (game.has("developers") && game.getJSONArray("developers").length() > 0) {
                        game.getJSONArray("developers").getJSONObject(0).getString("name")
                    } else "Unknown Developer",
                    price = "Free",
                    inAppPurchases = false,
                    multiplayer = game.optBoolean("multiplayer", false),
                    controllerSupport = true
                )
                games.add(gameInfo)
            }
        } catch (e: Exception) {
            // Silenciar errores de API y usar datos locales
            println("Error fetching from RAWG API: ${e.message}")
        }
        
        games
    }
    
    // Funciones auxiliares para información local
    private fun getLocalGameRating(packageName: String): Float {
        val ratings = mapOf(
            "com.activision.callofduty.shooter" to 4.2f,
            "com.epicgames.fortnite" to 4.0f,
            "com.tencent.ig" to 4.1f,
            "com.roblox.client" to 4.3f,
            "com.mojang.minecraftpe" to 4.5f
        )
        return ratings[packageName] ?: 4.0f
    }
    
    private fun getLocalGameDescription(packageName: String): String {
        val descriptions = mapOf(
            "com.activision.callofduty.shooter" to "Experience the thrill of Call of Duty on mobile with intense multiplayer battles and stunning graphics.",
            "com.epicgames.fortnite" to "Build, fight, and survive in this epic battle royale with 100 players.",
            "com.tencent.ig" to "The original battle royale game with realistic graphics and intense gameplay.",
            "com.roblox.client" to "Create, play, and imagine with millions of players in the ultimate virtual universe.",
            "com.mojang.minecraftpe" to "Build, explore, and survive in the blocky world of Minecraft."
        )
        return descriptions[packageName] ?: "A fun mobile game for everyone."
    }
    
    private fun getLocalGameDeveloper(packageName: String): String {
        val developers = mapOf(
            "com.activision.callofduty.shooter" to "Activision",
            "com.epicgames.fortnite" to "Epic Games",
            "com.tencent.ig" to "PUBG Corporation",
            "com.roblox.client" to "Roblox Corporation",
            "com.mojang.minecraftpe" to "Mojang"
        )
        return developers[packageName] ?: "Unknown Developer"
    }
    
    private fun hasInAppPurchases(packageName: String): Boolean {
        val hasIAP = listOf(
            "com.activision.callofduty.shooter",
            "com.epicgames.fortnite",
            "com.tencent.ig",
            "com.roblox.client",
            "com.mojang.minecraftpe"
        )
        return hasIAP.contains(packageName)
    }
    
    private fun isMultiplayerGame(packageName: String): Boolean {
        val multiplayerGames = listOf(
            "com.activision.callofduty.shooter",
            "com.epicgames.fortnite",
            "com.tencent.ig",
            "com.roblox.client",
            "com.mojang.minecraftpe"
        )
        return multiplayerGames.contains(packageName)
    }
    
    private fun hasControllerSupport(packageName: String): Boolean {
        val controllerGames = listOf(
            "com.activision.callofduty.shooter",
            "com.epicgames.fortnite",
            "com.roblox.client",
            "com.mojang.minecraftpe"
        )
        return controllerGames.contains(packageName)
    }
    
    suspend fun getPopularGames(): List<GameInfo> = withContext(Dispatchers.IO) {
        POPULAR_GAMES
    }
    
    suspend fun getGameDetails(packageName: String): GameInfo? = withContext(Dispatchers.IO) {
        // Buscar en juegos instalados
        val installedGames = getInstalledGames()
        val installed = installedGames.find { it.packageName == packageName }
        if (installed != null) return@withContext installed
        
        // Buscar en juegos populares
        val popular = POPULAR_GAMES.find { it.packageName == packageName }
        if (popular != null) return@withContext popular
        
        null
    }

    // Lógica para obtener icono local de juegos recomendados
    private fun getLocalGameIcon(packageName: String): Int? {
        return when (packageName) {
            "com.activision.callofduty.shooter" -> R.drawable.ic_cod_mobile
            "com.epicgames.fortnite" -> R.drawable.ic_fortnite
            "com.tencent.ig" -> R.drawable.ic_pubg
            "com.roblox.client" -> R.drawable.ic_roblox
            "com.mojang.minecraftpe" -> R.drawable.ic_minecraft
            else -> null
        }
    }
} 