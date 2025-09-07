package com.example.gamebooster.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a boost operation entry in the history
 */
@Entity(tableName = "boost_history")
data class BoostHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Timestamp and duration
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0, // in milliseconds
    
    // Boost configuration
    val mode: String = "", // NORMAL, ULTRA, GAMING
    val gameName: String? = null,
    val gamePackage: String? = null,
    
    // System metrics before boost
    val cpuBefore: Float = 0f, // in percentage
    val ramBefore: Int = 0, // in MB
    val storageBefore: Long = 0, // in bytes
    val batteryBefore: Int = 0, // in percentage
    val temperatureBefore: Float = 0f, // in Celsius
    
    // System metrics after boost
    val cpuAfter: Float = 0f, // in percentage
    val ramAfter: Int = 0, // in MB
    val storageAfter: Long = 0, // in bytes
    val batteryAfter: Int = 0, // in percentage
    val temperatureAfter: Float = 0f, // in Celsius
    
    // Optimization results
    val ramFreed: Int = 0, // in MB
    val storageFreed: Int = 0, // in bytes
    val appsClosed: Int = 0,
    val optimizations: String = "", // Comma-separated list of optimizations
    val optimizationScore: Int = 0, // 0-100
    
    // Additional metrics
    val networkType: String = "Unknown",
    val isRooted: Boolean = false,
    val deviceModel: String = "",
    val androidVersion: String = "",
    val notes: String = ""
) {
    /**
     * Gets a formatted date string from the timestamp
     */
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Gets a formatted duration string
     */
    fun getFormattedDuration(): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
    
    /**
     * Gets a list of optimizations
     */
    fun getOptimizationList(): List<String> {
        return optimizations.split(", ").filter { it.isNotBlank() }
    }
    
    /**
     * Gets a human-readable string of the boost mode
     */
    fun getModeDisplayName(): String {
        return when (mode.uppercase()) {
            "ULTRA" -> "Ultra Boost"
            "GAMING" -> "Gaming Mode"
            else -> "Normal Boost"
        }
    }
    
    /**
     * Gets a color representing the optimization score
     */
    fun getScoreColor(): Long {
        return when {
            optimizationScore >= 80 -> 0xFF4CAF50 // Green
            optimizationScore >= 50 -> 0xFFFFC107 // Amber
            else -> 0xFFF44336 // Red
        }
    }
    
    /**
     * Gets a formatted string of the RAM freed
     */
    fun getFormattedRamFreed(): String {
        return if (ramFreed >= 1024) {
            String.format("%.1f GB", ramFreed / 1024f)
        } else {
            "$ramFreed MB"
        }
    }
    
    /**
     * Gets a formatted string of the storage freed
     */
    fun getFormattedStorageFreed(): String {
        return when {
            storageFreed >= 1073741824 -> String.format("%.1f GB", storageFreed / 1073741824f)
            storageFreed >= 1048576 -> String.format("%.1f MB", storageFreed / 1048576f)
            storageFreed >= 1024 -> String.format("%.1f KB", storageFreed / 1024f)
            else -> "$storageFreed B"
        }
    }
    
    companion object {
        /**
         * Creates a mock entry for testing and previews
         */
        fun mock(): BoostHistoryEntry {
            return BoostHistoryEntry(
                id = 1,
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                duration = 45000, // 45 seconds
                
                mode = "ULTRA",
                gameName = "Asphalt 9",
                gamePackage = "com.gameloft.android.ANMP.GloftA9HML",
                
                cpuBefore = 78.5f,
                ramBefore = 6500,
                storageBefore = 45000000000, // 45GB
                batteryBefore = 65,
                temperatureBefore = 42.3f,
                
                cpuAfter = 45.2f,
                ramAfter = 4200,
                storageAfter = 44800000000, // 44.8GB
                batteryAfter = 63,
                temperatureAfter = 38.7f,
                
                ramFreed = 2300, // 2.3GB
                storageFreed = 200000000, // 200MB
                appsClosed = 12,
                optimizations = "CPU, Memory, Network, Thermal, Game-specific",
                optimizationScore = 88,
                
                networkType = "Wi-Fi 5GHz",
                isRooted = true,
                deviceModel = "Samsung Galaxy S21",
                androidVersion = "Android 13",
                notes = "Great performance boost for Asphalt 9"
            )
        }
    }
}