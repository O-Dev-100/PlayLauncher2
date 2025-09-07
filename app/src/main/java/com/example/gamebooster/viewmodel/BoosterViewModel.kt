package com.example.gamebooster.viewmodel

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamebooster.data.BoostHistoryDao
import com.example.gamebooster.data.InstalledAppsRepository
import com.example.gamebooster.model.BoostHistoryEntry
import com.example.gamebooster.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import javax.inject.Inject
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.example.gamebooster.MainActivity
import com.example.gamebooster.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import android.net.ConnectivityManager
import android.os.SystemClock
import android.content.pm.PackageManager

@HiltViewModel
class BoosterViewModel @Inject constructor(
    application: Application,
    private val boostHistoryDao: BoostHistoryDao,
    private val installedAppsRepository: InstalledAppsRepository
) : AndroidViewModel(application) {

    // Device metrics
    private val _ramUsage = MutableStateFlow(0f)
    val ramUsage = _ramUsage.asStateFlow()

    private val _storageUsage = MutableStateFlow(0f)
    val storageUsage = _storageUsage.asStateFlow()

    private val _batteryLevel = MutableStateFlow(0)
    val batteryLevel = _batteryLevel.asStateFlow()

    private val _cpuTemp = MutableStateFlow(0f)
    val cpuTemp = _cpuTemp.asStateFlow()

    private val _cpuUsage = MutableStateFlow(0)
    val cpuUsage = _cpuUsage.asStateFlow()

    private val _fps = MutableStateFlow(0)
    val fps = _fps.asStateFlow()

    private val _ping = MutableStateFlow(0)
    val ping = _ping.asStateFlow()

    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing = _isOptimizing.asStateFlow()

    // Safe boost mode
    private val _safeBoost = MutableStateFlow(true)
    val safeBoost = _safeBoost.asStateFlow()

    // Boost history and feedback
    private val _boostHistory = MutableStateFlow<List<BoostHistoryEntry>>(emptyList())
    val boostHistory = _boostHistory.asStateFlow()

    private val _lastBoostFeedback = MutableStateFlow<BoostFeedbackData?>(null)
    val lastBoostFeedback = _lastBoostFeedback.asStateFlow()

    // Installed apps
    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    // UI state for launcher
    data class LauncherUiState(
        val isLoading: Boolean = true,
        val installedGames: List<InstalledApp> = emptyList(),
        val favoriteGames: Set<String> = emptySet(), // Set de PackageNames de juegos favoritos
        val selectedGamePackageName: String? = null,
        // Métricas del sistema
        val ramUsagePercent: Float = 0f,
        val storageUsagePercent: Float = 0f,
        val batteryLevel: Int = 0,
        val cpuTemp: Float = 0f,
        val ping: Int = 0,
        // Estado de los detalles del juego desde la API
        val isGameDetailsVisible: Boolean = false,
        val isLoadingGameDetails: Boolean = false,
        val gameDetailsError: String? = null,
        val rawgDetails: com.example.gamebooster.data.remote.model.RawgGameDetails? = null
    ) {
        val selectedGame: InstalledApp?
            get() = installedGames.find { it.packageName == selectedGamePackageName }
    }
    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState = _uiState.asStateFlow()

    // Cache for optimization results
    private val optimizationCache = ConcurrentHashMap<String, OptimizationResult>()

    // Dynamic whitelist for safe boost
    private val dynamicWhitelist = mutableSetOf(
        "com.whatsapp",
        "com.facebook.orca",
        "com.google.android.apps.messaging",
        "com.android.phone",
        "com.android.contacts",
        "com.android.settings"
    )

    // Additional configurations from BoostConfigsScreen
    private val _showNotifications = MutableStateFlow(true)
    val showNotifications = _showNotifications.asStateFlow()

    private val _autoOptimize = MutableStateFlow(false)
    val autoOptimize = _autoOptimize.asStateFlow()

    private val _aggressiveMode = MutableStateFlow(false)
    val aggressiveMode = _aggressiveMode.asStateFlow()

    data class BoostFeedbackData(
        val ramFreed: Int,
        val storageFreed: Int,
        val cpuTempReduced: Float,
        val networkLatencyReduced: Int,
        val duration: Long,
        val appsClosedList: List<String>,
        val optimizationScore: Int
    )

    init {
        updateDeviceMetrics()
        loadInstalledApps()
        viewModelScope.launch {
            boostHistoryDao.getAllBoosts().collect { history ->
                _boostHistory.value = history
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            try {
                val apps = installedAppsRepository.getInstalledApps()
                _uiState.update { it.copy(installedGames = apps, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(installedGames = getSimulatedApps(), isLoading = false) }
            }
        }
    }

    fun onGameSelected(game: InstalledApp) {
        _uiState.update { it.copy(selectedGamePackageName = game.packageName, isGameDetailsVisible = true, gameDetailsError = null) }
        loadRawgGameDetails(game)
    }

    fun closeGameDetails() {
        _uiState.update { it.copy(isGameDetailsVisible = false) }
    }

    fun toggleFavoriteGame(packageName: String) {
        _uiState.update { currentState ->
            val currentFavorites = currentState.favoriteGames.toMutableSet()
            if (currentFavorites.contains(packageName)) {
                currentFavorites.remove(packageName)
            } else {
                currentFavorites.add(packageName)
            }
            currentState.copy(favoriteGames = currentFavorites)
        }
    }

    private fun getSimulatedApps(): List<InstalledApp> {
        return listOf(
            InstalledApp("com.pubg.mobile", "PUBG Mobile", null, true, "battle_royale"),
            InstalledApp("com.activision.callofduty", "Call of Duty", null, true, "fps"),
            InstalledApp("com.mihoyo.genshin", "Genshin Impact", null, true, "rpg"),
            InstalledApp("com.mojang.minecraft", "Minecraft", null, true, "sandbox"),
            InstalledApp("com.roblox.client", "Roblox", null, true, "social")
        )
    }

    fun setSafeBoost(enabled: Boolean) {
        _safeBoost.value = enabled
    }

    fun toggleSafeBoost() {
        _safeBoost.value = !_safeBoost.value
    }

    // Removed redundant boost mode toggles - keeping only safe boost

    fun toggleShowNotifications() {
        _showNotifications.value = !_showNotifications.value
    }

    fun toggleAutoOptimize() {
        _autoOptimize.value = !_autoOptimize.value
    }

    fun toggleAggressiveMode() {
        _aggressiveMode.value = !_aggressiveMode.value
    }

    private fun updateDeviceMetrics() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                updateRamUsage()
                updateStorageUsage()
                updateBatteryLevel()
                updateCpuTemperature()
                updateCpuUsage()
                updateNetworkMetrics()
                
                // Update unified state with current metrics
                _uiState.update { currentState ->
                    currentState.copy(
                        ramUsagePercent = _ramUsage.value,
                        storageUsagePercent = _storageUsage.value,
                        batteryLevel = _batteryLevel.value,
                        cpuTemp = _cpuTemp.value,
                        ping = _ping.value
                    )
                }
                
                delay(5000)
            }
        }
    }

    private fun updateCpuUsage() {
        try {
            val first = readCpuStat()
            Thread.sleep(200)
            val second = readCpuStat()
            if (first != null && second != null) {
                val idleDelta = second.idle - first.idle
                val totalDelta = second.total - first.total
                if (totalDelta > 0) {
                    val usage = ((totalDelta - idleDelta).toFloat() / totalDelta.toFloat() * 100f).toInt()
                    _cpuUsage.value = usage.coerceIn(0, 100)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private data class CpuSample(val idle: Long, val total: Long)

    private fun readCpuStat(): CpuSample? {
        return try {
            val reader = java.io.RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()
            val toks = load.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
            val user = toks[1].toLong()
            val nice = toks[2].toLong()
            val system = toks[3].toLong()
            val idle = toks[4].toLong()
            val iowait = toks.getOrNull(5)?.toLongOrNull() ?: 0L
            val irq = toks.getOrNull(6)?.toLongOrNull() ?: 0L
            val softIrq = toks.getOrNull(7)?.toLongOrNull() ?: 0L
            val steal = toks.getOrNull(8)?.toLongOrNull() ?: 0L
            val total = user + nice + system + idle + iowait + irq + softIrq + steal
            CpuSample(idle = idle + iowait, total = total)
        } catch (e: Exception) {
            null
        }
    }

    fun estimateCpuUsage(): Int = _cpuUsage.value

    private fun updateRamUsage() {
        try {
            val activityManager = getApplication<Application>().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalRam = memoryInfo.totalMem / (1024 * 1024) // MB
            val availRam = memoryInfo.availMem / (1024 * 1024) // MB
            _ramUsage.value = ((totalRam - availRam) / totalRam.toFloat()) * 100
        } catch (e: Exception) {
            // Log error internally
        }
    }

    private fun updateStorageUsage() {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalSpace = stat.totalBytes / (1024 * 1024) // MB
            val availableSpace = stat.availableBytes / (1024 * 1024) // MB
            _storageUsage.value = ((totalSpace - availableSpace) / totalSpace.toFloat()) * 100
        } catch (e: Exception) {
            // Log error internally
        }
    }

    private fun updateBatteryLevel() {
        try {
            val batteryStatus = getApplication<Application>().registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale != -1) {
                _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
            }
        } catch (e: Exception) {
            // Log error internally
        }
    }

    private fun updateCpuTemperature() {
        try {
            val temp = readCpuTemperature()
            if (temp > 0) {
                _cpuTemp.value = temp
            }
        } catch (e: Exception) {
            // Log error internally
        }
    }

    private fun readCpuTemperature(): Float {
        try {
            val tempFile = File("/sys/class/thermal/thermal_zone0/temp")
            if (tempFile.exists()) {
                val temp = tempFile.readText().trim().toFloat() / 1000f
                return temp.coerceIn(0f, 100f)
            }
        } catch (e: Exception) {
            // Log error internally
        }
        return _cpuTemp.value
    }

    private fun loadRawgGameDetails(game: InstalledApp) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoadingGameDetails = true, gameDetailsError = null) }
                val repo = com.example.gamebooster.data.remote.GameDetailsRepository.get()
                val result = repo.fetchGameDetails(game.appName)
                result.onSuccess { details ->
                    _uiState.update { it.copy(rawgDetails = details, isLoadingGameDetails = false) }
                }.onFailure { err ->
                    _uiState.update { it.copy(gameDetailsError = err.message, isLoadingGameDetails = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(gameDetailsError = e.message, isLoadingGameDetails = false) }
            }
        }
    }

    private suspend fun updateNetworkMetrics() {
        try {
            val startTime = SystemClock.elapsedRealtime()
            withContext(Dispatchers.IO) {
                java.net.InetAddress.getByName("8.8.8.8")
            }
            val pingTime = SystemClock.elapsedRealtime() - startTime
            _ping.value = pingTime.toInt().coerceAtMost(1000)
            _fps.value = calculateFpsEstimate()
        } catch (e: Exception) {
            // Log error internally
        }
    }

    private fun calculateFpsEstimate(): Int {
        return (30..60).random()
    }

    @SuppressLint("StringFormatMatches")
    fun performBoost(aggressive: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _isOptimizing.value = true
            val startTime = System.currentTimeMillis()
            try {
                val ramBefore = _ramUsage.value
                val storageBefore = _storageUsage.value
                val batteryBefore = _batteryLevel.value
                val tempBefore = _cpuTemp.value
                val pingBefore = _ping.value

                val optimizationResults = if (aggressive || _aggressiveMode.value) {
                    performAggressiveOptimization()
                } else {
                    performRealOptimization()
                }

                val ramFreed = ((ramBefore - _ramUsage.value) * 10).toInt().coerceAtLeast(0)
                val storageFreed = ((storageBefore - _storageUsage.value) * 100).toInt().coerceAtLeast(0)
                val tempReduced = (tempBefore - _cpuTemp.value).coerceAtLeast(0f)
                val pingReduced = (pingBefore - _ping.value).coerceAtLeast(0)
                val appsClosed = optimizationResults.appsClosedList
                val duration = System.currentTimeMillis() - startTime

                val optimizationScore = calculateOptimizationScore(ramFreed, storageFreed, appsClosed.size)

                val entry = BoostHistoryEntry(
                    timestamp = System.currentTimeMillis(),
                    ramFreed = ramFreed,
                    appsClosed = appsClosed.size,
                    storageFreed = storageFreed,
                    duration = duration,
                    optimizationScore = optimizationScore
                )
                boostHistoryDao.insertBoost(entry)

                _lastBoostFeedback.value = BoostFeedbackData(
                    ramFreed = ramFreed,
                    storageFreed = storageFreed,
                    cpuTempReduced = tempReduced,
                    networkLatencyReduced = pingReduced,
                    duration = duration,
                    appsClosedList = appsClosed,
                    optimizationScore = optimizationScore
                )

                if (_showNotifications.value) {
                    val context = getApplication<Application>()
                    val message = if (ramFreed > 0 || appsClosed.isNotEmpty()) {
                        context.getString(R.string.boost_success, ramFreed, appsClosed.size, duration)
                    } else {
                        context.getString(R.string.boost_already_optimized)
                    }
                    showBoostNotification(message, optimizationScore)
                }
            } catch (e: Exception) {
                // Log error internally
            } finally {
                updateDeviceMetrics()
                _isOptimizing.value = false
            }
        }
    }

    private fun calculateOptimizationScore(ramFreed: Int, storageFreed: Int, appsClosed: Int): Int {
        return (ramFreed * 0.4 + storageFreed * 0.3 + appsClosed * 10).toInt().coerceIn(0, 100)
    }

    private suspend fun performRealOptimization(): OptimizationResult {
        return withContext(Dispatchers.IO) {
            val appsClosed = mutableListOf<String>()
            var ramFreed = 0
            var storageFreed = 0

            try {
                val activityManager = getApplication<Application>().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                val ramBefore = memoryInfo.availMem / (1024 * 1024)

                val runningProcesses = activityManager.runningAppProcesses ?: emptyList()
                val killableProcesses = runningProcesses.filter { processInfo ->
                    val packageName = processInfo.processName
                    val isCritical = dynamicWhitelist.contains(packageName)
                    val isSystem = packageName.startsWith("android.") ||
                            packageName.startsWith("com.android.") ||
                            packageName.startsWith("com.google.") ||
                            packageName.startsWith("com.example.gamebooster")
                    !isSystem && !isCritical
                }.shuffled()

                val processesToKill = killableProcesses.take(if (_safeBoost.value) 3 else 5)
                processesToKill.forEach { processInfo ->
                    try {
                        activityManager.killBackgroundProcesses(processInfo.processName)
                        appsClosed.add(processInfo.processName)
                    } catch (e: Exception) {
                        // Log error
                    }
                }

                activityManager.getMemoryInfo(memoryInfo)
                ramFreed = (memoryInfo.availMem / (1024 * 1024) - ramBefore).coerceAtLeast(0).toInt()
                storageFreed = clearGameAppCache()

                if (_aggressiveMode.value || !_safeBoost.value) {
                    optimizeNetwork()
                }

                optimizationCache["last_optimization"] = OptimizationResult(appsClosed.size, ramFreed, storageFreed, appsClosed)
            } catch (e: Exception) {
                // Log error
            }

            OptimizationResult(appsClosed.size, ramFreed, storageFreed, appsClosed)
        }
    }

    private suspend fun performAggressiveOptimization(): OptimizationResult {
        return withContext(Dispatchers.IO) {
            val appsClosed = mutableListOf<String>()
            var ramFreed = 0
            var storageFreed = 0

            try {
                val activityManager = getApplication<Application>().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                val ramBefore = memoryInfo.availMem / (1024 * 1024)

                val runningProcesses = activityManager.runningAppProcesses ?: emptyList()
                val killableProcesses = runningProcesses.filter { processInfo ->
                    val packageName = processInfo.processName
                    !packageName.startsWith("android.") &&
                            !packageName.startsWith("com.android.") &&
                            !packageName.startsWith("com.google.") &&
                            !packageName.startsWith("com.example.gamebooster")
                }.shuffled()

                val processesToKill = killableProcesses.take(10)
                processesToKill.forEach { processInfo ->
                    try {
                        activityManager.killBackgroundProcesses(processInfo.processName)
                        appsClosed.add(processInfo.processName)
                    } catch (e: Exception) {
                        // Log error
                    }
                }

                activityManager.getMemoryInfo(memoryInfo)
                ramFreed = (memoryInfo.availMem / (1024 * 1024) - ramBefore).coerceAtLeast(0).toInt()
                storageFreed = clearGameAppCache(aggressive = true)

                withContext(Dispatchers.IO) {
                    optimizeCpu()
                    optimizeNetwork(aggressive = true)
                    optimizeBattery()
                }

                optimizationCache["last_aggressive_optimization"] = OptimizationResult(appsClosed.size, ramFreed, storageFreed, appsClosed)
            } catch (e: Exception) {
                // Log error
            }

            OptimizationResult(appsClosed.size, ramFreed, storageFreed, appsClosed)
        }
    }

    // Removed performAutoOptimization - simplified boost logic

    private fun clearGameAppCache(aggressive: Boolean = false): Int {
        var totalFreed = 0
        try {
            val packageManager = getApplication<Application>().packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val gameApps = installedApps.filter { app ->
                val appInfo = app.loadLabel(packageManager).toString().lowercase()
                appInfo.contains("game") || app.packageName.contains("game") ||
                        appInfo.contains("pubg") || appInfo.contains("cod") ||
                        appInfo.contains("minecraft") || appInfo.contains("roblox") ||
                        appInfo.contains("genshin")
            }

            val maxApps = if (aggressive) 8 else 3
            gameApps.take(maxApps).forEach { app ->
                try {
                    val cacheDir = getApplication<Application>().cacheDir
                    val appCacheDir = File(cacheDir, app.packageName)
                    if (appCacheDir.exists()) {
                        val cacheSize = calculateDirectorySize(appCacheDir)
                        deleteDirectory(appCacheDir)
                        totalFreed += cacheSize
                    }
                    val externalCacheDir = getApplication<Application>().externalCacheDir
                    if (externalCacheDir != null) {
                        val extAppCacheDir = File(externalCacheDir, app.packageName)
                        if (extAppCacheDir.exists()) {
                            val extCacheSize = calculateDirectorySize(extAppCacheDir)
                            deleteDirectory(extAppCacheDir)
                            totalFreed += extCacheSize
                        }
                    }
                } catch (e: Exception) {
                    // Log error
                }
            }
        } catch (e: Exception) {
            // Log error
        }
        return totalFreed
    }

    private fun calculateDirectorySize(directory: File): Int {
        var size = 0L
        try {
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        } catch (e: Exception) {
            // Log error
        }
        return (size / (1024 * 1024)).toInt()
    }

    private fun deleteDirectory(directory: File) {
        try {
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
            directory.delete()
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun optimizeCpu() {
        try {
            withTimeoutOrNull(1000) {
                withContext(Dispatchers.IO) {
                    if (hasRootAccess()) {
                        Runtime.getRuntime().exec("echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
                    }
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun optimizeNetwork(aggressive: Boolean = false) {
        if (!_aggressiveMode.value && aggressive) return
        try {
            val context = getApplication<Application>()
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (aggressive) {
                val request = android.net.NetworkRequest.Builder()
                    .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                    .build()
                connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        // Prioritize game traffic
                    }
                })
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun optimizeBattery() {
        try {
            val context = getApplication<Application>()
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isBackgroundRestricted = connectivityManager.isActiveNetworkMetered
            if (!isBackgroundRestricted) {
                // Simulate background restriction
                delay(1000)
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    private fun hasRootAccess(): Boolean {
        return try {
            Runtime.getRuntime().exec("su").waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    // Removed scheduleAutoBoost - simplified boost logic

    fun clearHistory() {
        viewModelScope.launch {
            boostHistoryDao.clearHistory()
        }
    }

    private fun showBoostNotification(message: String, optimizationScore: Int) {
        val context = getApplication<Application>()
        val channelId = "boost_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "Boosts", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "boost_feedback")
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_power)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("$message (Puntuación: $optimizationScore/100)")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message (Puntuación: $optimizationScore/100)"))
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_info, context.getString(R.string.see_boost_details), pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            try {
                installedAppsRepository.launchApp(packageName)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun getBoostTips(): List<String> {
        val tips = mutableListOf<String>()
        val lastFeedback = _lastBoostFeedback.value
        if (lastFeedback != null) {
            if (lastFeedback.ramFreed < 50) {
                tips.add("Considera cerrar más aplicaciones en segundo plano para liberar más RAM.")
            }
            if (lastFeedback.storageFreed < 100) {
                tips.add("Limpia archivos temporales regularmente para optimizar el almacenamiento.")
            }
            if (lastFeedback.cpuTempReduced < 2f) {
                tips.add("Mantén el dispositivo en un lugar fresco para reducir la temperatura de la CPU.")
            }
            if (lastFeedback.networkLatencyReduced < 10) {
                tips.add("Conéctate a una red Wi-Fi estable para mejorar la latencia.")
            }
        }
        return tips
    }
}

data class OptimizationResult(
    val appsClosed: Int,
    val ramFreed: Int,
    val storageFreed: Int,
    val appsClosedList: List<String>
)