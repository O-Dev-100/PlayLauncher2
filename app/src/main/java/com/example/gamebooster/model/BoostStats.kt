package com.example.gamebooster.model

/**
 * Data class representing system statistics and metrics
 */
data class BoostStats(
    // Memory metrics
    val ramUsage: Int = 0, // in MB
    val totalRam: Int = 0, // in MB
    val availableRam: Int = 0, // in MB
    val ramUsagePercentage: Int = 0,
    
    // Storage metrics
    val storageUsage: String = "0 GB / 0 GB",
    val usedStorage: Long = 0, // in bytes
    val totalStorage: Long = 0, // in bytes
    val storageUsagePercentage: Int = 0,
    
    // Network metrics
    val ping: Int = 0, // in ms
    val downloadSpeed: Float = 0f, // in Mbps
    val uploadSpeed: Float = 0f, // in Mbps
    val networkType: String = "Unknown",
    
    // CPU metrics
    val cpuUsage: Float = 0f, // in percentage
    val cpuCores: Int = 0,
    val cpuMaxFreq: Long = 0, // in KHz
    val cpuAvgFreq: Long = 0, // in KHz
    
    // GPU metrics
    val gpuUsage: Float = 0f, // in percentage
    val gpuFreq: Long = 0, // in MHz
    
    // Battery metrics
    val batteryLevel: Int = 0, // in percentage
    val isCharging: Boolean = false,
    val batteryHealth: String = "Good",
    val batteryTemp: Float = 0f, // in Celsius
    
    // Thermal metrics
    val temperature: Float = 0f, // in Celsius
    val thermalStatus: String = "Normal",
    
    // System metrics
    val uptime: Long = 0, // in seconds
    val loadAverage: Float = 0f,
    val isRooted: Boolean = false,
    
    // Game-specific metrics (if applicable)
    val gameFps: Float = 0f,
    val gameLatency: Int = 0, // in ms
    val gameJitter: Int = 0 // in ms
) {
    /**
     * Gets a formatted string of RAM usage
     */
    fun getFormattedRamUsage(): String {
        val usedGB = ramUsage / 1024f
        val totalGB = totalRam / 1024f
        return String.format("%.1f GB / %.1f GB (%.1f%%)", usedGB, totalGB, ramUsagePercentage.toFloat())
    }
    
    /**
     * Gets a formatted string of storage usage
     */
    fun getFormattedStorageUsage(): String {
        val usedGB = usedStorage / (1024 * 1024 * 1024f)
        val totalGB = totalStorage / (1024 * 1024 * 1024f)
        return String.format("%.1f GB / %.1f GB (%.1f%%)", usedGB, totalGB, storageUsagePercentage.toFloat())
    }
    
    /**
     * Gets a formatted string of CPU usage
     */
    fun getFormattedCpuUsage(): String {
        return String.format("%.1f%%", cpuUsage)
    }
    
    /**
     * Gets a formatted string of GPU usage
     */
    fun getFormattedGpuUsage(): String {
        return String.format("%.1f%%", gpuUsage)
    }
    
    /**
     * Gets a formatted string of network speed
     */
    fun getFormattedNetworkSpeed(): String {
        return String.format("%.1f↓ / %.1f↑ Mbps", downloadSpeed, uploadSpeed)
    }
    
    /**
     * Gets a formatted string of battery status
     */
    fun getFormattedBatteryStatus(): String {
        val status = if (isCharging) "Charging" else "Not charging"
        return "$batteryLevel% • $status • $batteryHealth • ${String.format("%.1f°C", batteryTemp)}"
    }
    
    /**
     * Gets a formatted string of system temperature
     */
    fun getFormattedTemperature(): String {
        return String.format("%.1f°C • %s", temperature, thermalStatus)
    }
    
    companion object {
        /**
         * Creates a default instance with all values set to zero
         */
        fun empty(): BoostStats {
            return BoostStats()
        }
        
        /**
         * Creates a mock instance for previews and testing
         */
        fun mock(): BoostStats {
            return BoostStats(
                ramUsage = 3500,
                totalRam = 8000,
                availableRam = 4500,
                ramUsagePercentage = 44,
                
                usedStorage = 32000000000, // 32GB
                totalStorage = 128000000000, // 128GB
                storageUsagePercentage = 25,
                
                ping = 24,
                downloadSpeed = 56.8f,
                uploadSpeed = 12.3f,
                networkType = "Wi-Fi 5GHz",
                
                cpuUsage = 42.5f,
                cpuCores = 8,
                cpuMaxFreq = 2800000,
                cpuAvgFreq = 1800000,
                
                gpuUsage = 35.2f,
                gpuFreq = 800,
                
                batteryLevel = 78,
                isCharging = true,
                batteryHealth = "Good",
                batteryTemp = 32.4f,
                
                temperature = 42.1f,
                thermalStatus = "Warm",
                
                uptime = 12345,
                loadAverage = 1.2f,
                isRooted = true,
                
                gameFps = 59.8f,
                gameLatency = 32,
                gameJitter = 4
            )
        }
    }
}