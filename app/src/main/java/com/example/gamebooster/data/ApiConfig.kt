package com.example.gamebooster.data

object ApiConfig {
    // APIs GRATUITAS disponibles
    const val YOUTUBE_API_KEY = "AIzaSyDEhv9jxNAh7prmBVZZXgzAX2rrBodHOmw"
    const val RAWG_API_KEY = "72a0017314fd430a90c980fdad51a23b"
    const val IGDB_CLIENT_ID = "YOUR_IGDB_CLIENT_ID_HERE"
    const val IGDB_CLIENT_SECRET = "YOUR_IGDB_CLIENT_SECRET_HERE"
    
    // URLs de las APIs gratuitas
    const val YOUTUBE_API_BASE = "https://www.googleapis.com/youtube/v3"
    const val RAWG_API_BASE = "https://api.rawg.io/api"
    const val IGDB_API_BASE = "https://api.igdb.com/v4"
    
    // Configuración de la app
    const val APP_PACKAGE_NAME = "com.example.gamebooster"
    const val APP_VERSION_CODE = 1
    const val APP_VERSION_NAME = "1.0.0"
    
    // Configuración de notificaciones
    const val FIREBASE_SENDER_ID = "YOUR_FIREBASE_SENDER_ID"
    const val FIREBASE_PROJECT_ID = "YOUR_FIREBASE_PROJECT_ID"
    
    // Configuración de caché local
    const val CACHE_DURATION_HOURS = 24
    const val MAX_CACHE_SIZE_MB = 50
    
    fun isApiConfigured(): Boolean {
        return YOUTUBE_API_KEY != "YOUR_YOUTUBE_API_KEY_HERE" ||
               RAWG_API_KEY != "YOUR_RAWG_API_KEY_HERE"
    }
    
    fun getYouTubeApiKey(): String? {
        return if (YOUTUBE_API_KEY != "YOUR_YOUTUBE_API_KEY_HERE") YOUTUBE_API_KEY else null
    }
    
    fun getRawgApiKey(): String? {
        return if (RAWG_API_KEY != "YOUR_RAWG_API_KEY_HERE") RAWG_API_KEY else null
    }
    
    fun getIgdbCredentials(): Pair<String, String>? {
        return if (IGDB_CLIENT_ID != "YOUR_IGDB_CLIENT_ID_HERE" && 
                   IGDB_CLIENT_SECRET != "YOUR_IGDB_CLIENT_SECRET_HERE") {
            Pair(IGDB_CLIENT_ID, IGDB_CLIENT_SECRET)
        } else null
    }
} 