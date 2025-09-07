package com.example.gamebooster.utils

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gamebooster.model.BoostConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Handles system-level optimizations for better gaming performance
 */
class SystemOptimizer(private val context: Context) {

    companion object {
        private const val TAG = "SystemOptimizer"
        private const val ROOT_OPTIMIZATION_DELAY = 1000L
    }

    /**
     * Applies optimizations based on the provided configuration
     */
    suspend fun applyOptimizations(config: BoostConfig) = withContext(Dispatchers.IO) {
        val optimizations = mutableListOf<String>()
        
        try {
            // Apply CPU optimizations
            if (config.optimizeCpu) {
                optimizeCpu(config.aggressiveMode)
                optimizations.add("CPU")
            }

            // Apply memory optimizations
            if (config.optimizeMemory) {
                val memoryFreed = optimizeMemory(config.aggressiveMode)
                optimizations.add("Memory (Freed: ${memoryFreed / 1024}MB)")
            }

            // Apply network optimizations
            if (config.optimizeNetwork) {
                optimizeNetwork(config.aggressiveMode)
                optimizations.add("Network")
            }

            // Apply thermal optimizations if in aggressive mode
            if (config.aggressiveMode) {
                optimizeThermal()
                optimizations.add("Thermal")
            }

            // Apply game-specific optimizations if package name is provided
            config.packageName?.let { packageName ->
                optimizeForGame(packageName)
                optimizations.add("Game-specific")
            }

            // Additional aggressive optimizations
            if (config.aggressiveMode) {
                applyAggressiveOptimizations()
                optimizations.add("Aggressive")
            }

            optimizations
        } catch (e: Exception) {
            Log.e(TAG, "Error applying optimizations", e)
            throw e
        }
    }

    /**
     * Optimizes CPU performance
     */
    private suspend fun optimizeCpu(aggressive: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                if (hasRootAccess()) {
                    // Set performance governor for all cores
                    for (i in 0 until Runtime.getRuntime().availableProcessors()) {
                        val governorFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor")
                        if (governorFile.exists()) {
                            governorFile.writeText("performance")
                        }
                    }
                    
                    // Set CPU boost parameters if available
                    val boostFile = File("/sys/devices/system/cpu/cpufreq/boost")
                    if (boostFile.exists()) {
                        boostFile.writeText("1")
                    }
                    
                    // Additional CPU tweaks for aggressive mode
                    if (aggressive) {
                        val minFreqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq")
                        val maxFreqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq")
                        
                        if (minFreqFile.exists() && maxFreqFile.exists()) {
                            val maxFreq = maxFreqFile.readText().trim().toIntOrNull()
                            maxFreq?.let {
                                minFreqFile.writeText(it.toString())
                            }
                        }
                    }
                }
                
                // Use Android's Performance Hint Manager if available (API 31+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        val performanceHintManager = context.getSystemService(Context.PERFORMANCE_HINT_SERVICE) 
                                as android.os.performance.PerformanceHintManager
                        
                        val session = performanceHintManager.createHintSession(
                            arrayOf(android.os.Process.myPid()),
                            arrayOf(android.os.Process.myTid()),
                            16666667 // 60fps in nanoseconds
                        )
                        
                        session.updateTargetWorkDuration(16666667) // 60fps
                        session.reportActualWorkDuration(10000000) // Report faster than target
                    } catch (e: Exception) {
                        Log.w(TAG, "PerformanceHintManager not available", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "CPU optimization failed", e)
                throw e
            }
        }
    }

    /**
     * Optimizes memory usage by clearing caches and trimming memory
     */
    private suspend fun optimizeMemory(aggressive: Boolean = false): Long {
        return withContext(Dispatchers.IO) {
            var freedMemory = 0L
            
            try {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val packageManager = context.packageManager
                
                // Get memory info before optimization
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                val beforeMem = memoryInfo.availMem / (1024 * 1024) // MB
                
                // Clear app caches
                val installedApps = packageManager.getInstalledApplications(0)
                
                for (app in installedApps) {
                    try {
                        // Skip system apps in non-aggressive mode
                        if (!aggressive && app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0) {
                            continue
                        }
                        
                        // Clear app cache
                        val cacheDir = File(app.dataDir, "cache")
                        if (cacheDir.exists() && cacheDir.isDirectory) {
                            freedMemory += calculateDirectorySize(cacheDir)
                            deleteDirectoryContents(cacheDir)
                        }
                        
                        // Clear code cache
                        val codeCacheDir = File(app.dataDir, "code_cache")
                        if (codeCacheDir.exists() && codeCacheDir.isDirectory) {
                            freedMemory += calculateDirectorySize(codeCacheDir)
                            deleteDirectoryContents(codeCacheDir)
                        }
                    } catch (e: Exception) {
                        // Ignore errors for individual apps
                        Log.d(TAG, "Error optimizing memory for ${app.packageName}: ${e.message}")
                    }
                }
                
                // Trim memory
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activityManager.clearApplicationUserData()
                } else {
                    activityManager.killBackgroundProcesses("")
                }
                
                // Trigger GC
                System.gc()
                
                // Get memory info after optimization
                activityManager.getMemoryInfo(memoryInfo)
                val afterMem = memoryInfo.availMem / (1024 * 1024) // MB
                
                // Calculate actual memory freed (in bytes)
                val actualFreed = (afterMem - beforeMem) * 1024 * 1024
                freedMemory = maxOf(freedMemory, actualFreed)
                
                Log.d(TAG, "Memory optimization complete. Freed: ${freedMemory / (1024 * 1024)}MB")
                
            } catch (e: Exception) {
                Log.e(TAG, "Memory optimization failed", e)
                throw e
            }
            
            freedMemory
        }
    }

    /**
     * Optimizes network performance
     */
    private suspend fun optimizeNetwork(aggressive: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Unbind from any existing network first
                    connectivityManager.bindProcessToNetwork(null)
                    
                    if (aggressive) {
                        // Request a high-performance network
                        val networkRequest = NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
                            .build()
                        
                        connectivityManager.requestNetwork(
                            networkRequest,
                            object : ConnectivityManager.NetworkCallback() {
                                override fun onAvailable(network: android.net.Network) {
                                    connectivityManager.bindProcessToNetwork(network)
                                }
                            }
                        )
                    }
                }
                
                // Set DNS to faster alternatives (requires root)
                if (hasRootAccess()) {
                    try {
                        Runtime.getRuntime().exec("settings put global private_dns_mode hostname")
                        Runtime.getRuntime().exec("settings put global private_dns_specifier dns.google")
                    } catch (e: Exception) {
                        Log.d(TAG, "DNS optimization not supported", e)
                    }
                }
                
                // Additional network optimizations for aggressive mode
                if (aggressive) {
                    // Increase TCP buffer sizes (requires root)
                    if (hasRootAccess()) {
                        try {
                            Runtime.getRuntime().exec("sysctl -w net.ipv4.tcp_rmem='4096 87380 16777216'")
                            Runtime.getRuntime().exec("sysctl -w net.ipv4.tcp_wmem='4096 65536 16777216'")
                            Runtime.getRuntime().exec("sysctl -w net.ipv4.tcp_window_scaling=1")
                            Runtime.getRuntime().exec("sysctl -w net.ipv4.tcp_sack=1")
                            Runtime.getRuntime().exec("sysctl -w net.ipv4.tcp_fack=1")
                        } catch (e: Exception) {
                            Log.d(TAG, "TCP optimization not supported", e)
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Network optimization failed", e)
                throw e
            }
        }
    }

    /**
     * Optimizes thermal performance
     */
    private suspend fun optimizeThermal() {
        withContext(Dispatchers.IO) {
            try {
                if (hasRootAccess()) {
                    // Disable thermal throttling (requires root)
                    val thermalFiles = arrayOf(
                        "/sys/class/thermal/thermal_zone0/mode",
                        "/sys/class/thermal/thermal_zone1/mode",
                        "/sys/class/thermal/thermal_zone2/mode"
                    )
                    
                    thermalFiles.forEach { filePath ->
                        val thermalFile = File(filePath)
                        if (thermalFile.exists()) {
                            thermalFile.writeText("disabled")
                        }
                    }
                    
                    // Set thermal profile to performance if available
                    val thermalProfile = File("/sys/class/thermal/thermal_message/sconfig")
                    if (thermalProfile.exists()) {
                        thermalProfile.writeText("10") // Performance mode
                    }
                    
                    // Additional thermal optimizations
                    val thermalDir = File("/sys/class/thermal")
                    if (thermalDir.exists() && thermalDir.isDirectory) {
                        thermalDir.listFiles()?.forEach { zone ->
                            try {
                                val policyFile = File(zone, "policy")
                                if (policyFile.exists()) {
                                    policyFile.writeText("step_wise")
                                }
                                
                                val modeFile = File(zone, "mode")
                                if (modeFile.exists()) {
                                    modeFile.writeText("disabled")
                                }
                            } catch (e: Exception) {
                                // Ignore errors for individual zones
                            }
                        }
                    }
                }
                
                // For non-root devices, use device-specific thermal APIs
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        val thermalManager = context.getSystemService(Context.THERMAL_SERVICE) 
                                as android.os.performance.ThermalManager
                        
                        val thermalStatus = thermalManager.currentThermalStatus
                        if (thermalStatus >= android.os.performance.ThermalManager.THERMAL_STATUS_SEVERE) {
                            // Device is throttling, take action
                            if (hasRootAccess()) {
                                // Reduce CPU frequency to cool down
                                Runtime.getRuntime().exec("echo 70 > /sys/devices/system/cpu/cpufreq/interactive/go_hispeed_load")
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Thermal API not available", e)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Thermal optimization failed", e)
                throw e
            }
        }
    }

    /**
     * Applies game-specific optimizations
     */
    private suspend fun optimizeForGame(packageName: String) {
        withContext(Dispatchers.IO) {
            try {
                // Get game info
                val packageManager = context.packageManager
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val isGame = appInfo.flags and android.content.pm.ApplicationInfo.FLAG_IS_GAME == 
                            android.content.pm.ApplicationInfo.FLAG_IS_GAME
                
                if (!isGame) return@withContext
                
                // Set game mode if available (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        val gameManager = context.getSystemService(Context.GAME_SERVICE) 
                                as android.app.GameManager
                        gameManager.gameMode = android.app.GameManager.GAME_MODE_PERFORMANCE
                    } catch (e: Exception) {
                        Log.d(TAG, "Game Mode API not available", e)
                    }
                }
                
                // Set as high priority process
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityManager.setProcessImportance(packageName, ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                
                // Disable battery optimization for this game
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                        val intent = android.content.Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = android.net.Uri.parse("package:$packageName")
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                }
                
                // Additional game-specific optimizations
                if (hasRootAccess()) {
                    try {
                        // Set GPU to performance mode
                        val gpuFile = File("/sys/class/kgsl/kgsl-3d0/devfreq/governor")
                        if (gpuFile.exists()) {
                            gpuFile.writeText("performance")
                        }
                        
                        // Increase GPU frequency
                        val gpuMaxFreqFile = File("/sys/class/kgsl/kgsl-3d0/devfreq/max_freq")
                        if (gpuMaxFreqFile.exists()) {
                            val maxFreq = gpuMaxFreqFile.readText().trim().toLongOrNull()
                            maxFreq?.let {
                                val minFreqFile = File("/sys/class/kgsl/kgsl-3d0/devfreq/min_freq")
                                minFreqFile.writeText(it.toString())
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "GPU optimization not supported", e)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Game-specific optimization failed", e)
                throw e
            }
        }
    }

    /**
     * Applies additional aggressive optimizations
     */
    private suspend fun applyAggressiveOptimizations() {
        withContext(Dispatchers.IO) {
            try {
                // Disable animations
                if (hasRootAccess()) {
                    Runtime.getRuntime().exec("settings put global window_animation_scale 0")
                    Runtime.getRuntime().exec("settings put global transition_animation_scale 0")
                    Runtime.getRuntime().exec("settings put global animator_duration_scale 0")
                }
                
                // Set I/O scheduler to noop for better performance
                if (hasRootAccess()) {
                    try {
                        val blockDirs = arrayOf("/sys/block/mmcblk0/queue/scheduler",
                                              "/sys/block/sda/queue/scheduler")
                        
                        blockDirs.forEach { path ->
                            val schedulerFile = File(path)
                            if (schedulerFile.exists()) {
                                schedulerFile.writeText("noop")
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "I/O scheduler optimization not supported", e)
                    }
                }
                
                // Increase file system readahead
                if (hasRootAccess()) {
                    try {
                        Runtime.getRuntime().exec("echo 2048 > /sys/block/mmcblk0/queue/read_ahead_kb")
                        Runtime.getRuntime().exec("echo 2048 > /sys/block/sda/queue/read_ahead_kb")
                    } catch (e: Exception) {
                        Log.d(TAG, "Filesystem optimization not supported", e)
                    }
                }
                
                // Disable kernel samepage merging (KSM)
                if (hasRootAccess()) {
                    try {
                        Runtime.getRuntime().exec("echo 0 > /sys/kernel/mm/ksm/run")
                    } catch (e: Exception) {
                        Log.d(TAG, "KSM optimization not supported", e)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Aggressive optimization failed", e)
                throw e
            }
        }
    }

    /**
     * Checks if device has root access
     */
    private fun hasRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c 'echo test'")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Calculates the size of a directory in bytes
     */
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        
        try {
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating directory size", e)
        }
        
        return size
    }

    /**
     * Deletes all contents of a directory
     */
    private fun deleteDirectoryContents(directory: File) {
        try {
            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        deleteDirectoryContents(file)
                        file.delete()
                    } else {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting directory contents", e)
        }
    }
}

/**
 * Data class representing boost configuration
 */
data class BoostConfig(
    val packageName: String? = null,
    val optimizeCpu: Boolean = true,
    val optimizeMemory: Boolean = true,
    val optimizeNetwork: Boolean = true,
    val aggressiveMode: Boolean = false,
    val gameMode: Boolean = false
)
