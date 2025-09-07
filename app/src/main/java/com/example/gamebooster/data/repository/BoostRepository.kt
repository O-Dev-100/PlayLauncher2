package com.example.gamebooster.data.repository

import com.example.gamebooster.data.dao.BoostDao
import com.example.gamebooster.model.BoostHistoryEntry
import com.example.gamebooster.model.BoostStats
import com.example.gamebooster.utils.SystemOptimizer
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling boost-related data operations
 */
@Singleton
class BoostRepository @Inject constructor(
    private val boostDao: BoostDao,
    private val systemOptimizer: SystemOptimizer
) {
    
    //region Boost History Operations
    
    /**
     * Gets all boost history entries, ordered by timestamp (newest first)
     */
    fun getAllBoosts(): Flow<List<BoostHistoryEntry>> {
        return boostDao.getAllBoosts()
    }
    
    /**
     * Gets a boost history entry by ID
     */
    suspend fun getBoostById(id: Long): BoostHistoryEntry? {
        return boostDao.getBoostById(id)
    }
    
    /**
     * Gets boost history entries for a specific game package
     */
    fun getBoostsForGame(packageName: String): Flow<List<BoostHistoryEntry>> {
        return boostDao.getBoostsForGame(packageName)
    }
    
    /**
     * Gets boost history entries within a date range
     */
    fun getBoostsBetweenDates(startDate: Date, endDate: Date): Flow<List<BoostHistoryEntry>> {
        return boostDao.getBoostsBetweenDates(startDate.time, endDate.time)
    }
    
    /**
     * Gets the most recent boost history entry
     */
    suspend fun getMostRecentBoost(): BoostHistoryEntry? {
        return boostDao.getMostRecentBoost()
    }
    
    /**
     * Gets the average optimization score across all boosts
     */
    suspend fun getAverageOptimizationScore(): Float {
        return boostDao.getAverageOptimizationScore()
    }
    
    /**
     * Gets the total RAM freed across all boosts (in MB)
     */
    suspend fun getTotalRamFreed(): Long {
        return boostDao.getTotalRamFreed() ?: 0
    }
    
    /**
     * Gets the total storage freed across all boosts (in GB)
     */
    suspend fun getTotalStorageFreed(): Float {
        val bytes = boostDao.getTotalStorageFreed() ?: 0
        return bytes / (1024f * 1024f * 1024f) // Convert to GB
    }
    
    /**
     * Gets the total number of apps closed across all boosts
     */
    suspend fun getTotalAppsClosed(): Int {
        return boostDao.getTotalAppsClosed() ?: 0
    }
    
    /**
     * Gets boost history entries with optimization score above a threshold
     */
    fun getBoostsWithMinScore(minScore: Int): Flow<List<BoostHistoryEntry>> {
        return boostDao.getBoostsWithMinScore(minScore)
    }
    
    /**
     * Gets the count of boost history entries
     */
    suspend fun getBoostCount(): Int {
        return boostDao.getBoostCount()
    }
    
    /**
     * Gets boost history entries with a specific mode
     */
    fun getBoostsByMode(mode: String): Flow<List<BoostHistoryEntry>> {
        return boostDao.getBoostsByMode(mode)
    }
    
    /**
     * Gets the most boosted games (top N)
     */
    suspend fun getMostBoostedGames(limit: Int = 5): List<BoostedGameInfo> {
        return boostDao.getMostBoostedGames(limit)
    }
    
    //endregion
    
    //region Boost Operations
    
    /**
     * Performs a boost operation with the specified configuration
     */
    suspend fun performBoost(
        mode: BoostMode,
        gamePackage: String? = null,
        gameName: String? = null
    ): BoostResult {
        val config = when (mode) {
            BoostMode.NORMAL -> SystemOptimizer.BoostConfig(
                packageName = gamePackage,
                optimizeCpu = true,
                optimizeMemory = true,
                optimizeNetwork = true,
                aggressiveMode = false,
                gameMode = gamePackage != null
            )
            BoostMode.ULTRA -> SystemOptimizer.BoostConfig(
                packageName = gamePackage,
                optimizeCpu = true,
                optimizeMemory = true,
                optimizeNetwork = true,
                aggressiveMode = true,
                gameMode = gamePackage != null
            )
            BoostMode.GAMING -> SystemOptimizer.BoostConfig(
                packageName = gamePackage ?: throw IllegalArgumentException("Game package is required for gaming mode"),
                optimizeCpu = true,
                optimizeMemory = true,
                optimizeNetwork = true,
                aggressiveMode = true,
                gameMode = true
            )
        }
        
        // Get system stats before optimization
        val statsBefore = getCurrentSystemStats()
        
        // Apply optimizations
        val optimizations = systemOptimizer.applyOptimizations(config)
        
        // Get system stats after optimization
        val statsAfter = getCurrentSystemStats()
        
        // Calculate results
        val ramFreed = (statsBefore.availableRam - statsAfter.availableRam).coerceAtLeast(0)
        val storageFreed = (statsBefore.usedStorage - statsAfter.usedStorage).coerceAtLeast(0)
        
        // Create and save boost history entry
        val entry = BoostHistoryEntry(
            mode = mode.name,
            gameName = gameName,
            gamePackage = gamePackage,
            
            // Before metrics
            cpuBefore = statsBefore.cpuUsage,
            ramBefore = statsBefore.ramUsage,
            storageBefore = statsBefore.usedStorage,
            batteryBefore = statsBefore.batteryLevel,
            temperatureBefore = statsBefore.temperature,
            
            // After metrics
            cpuAfter = statsAfter.cpuUsage,
            ramAfter = statsAfter.ramUsage,
            storageAfter = statsAfter.usedStorage,
            batteryAfter = statsAfter.batteryLevel,
            temperatureAfter = statsAfter.temperature,
            
            // Optimization results
            ramFreed = ramFreed,
            storageFreed = storageFreed,
            appsClosed = 0, // This would be calculated based on actual apps closed
            optimizations = optimizations.joinToString(", "),
            optimizationScore = calculateOptimizationScore(mode, optimizations, ramFreed, storageFreed),
            
            // Additional info
            networkType = statsAfter.networkType,
            isRooted = statsAfter.isRooted,
            deviceModel = android.os.Build.MODEL,
            androidVersion = "${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})",
            notes = if (gameName != null) "Boosted $gameName in ${mode.name.lowercase()} mode" else "System boost in ${mode.name.lowercase()} mode"
        )
        
        // Save to database
        val id = boostDao.insert(entry)
        
        return BoostResult(
            id = id,
            mode = mode,
            optimizations = optimizations,
            ramFreed = ramFreed,
            storageFreed = storageFreed,
            statsBefore = statsBefore,
            statsAfter = statsAfter,
            timestamp = Date()
        )
    }
    
    /**
     * Gets the current system stats
     */
    private suspend fun getCurrentSystemStats(): BoostStats {
        // This would be implemented to get actual system stats
        // For now, return a mock
        return BoostStats.mock()
    }
    
    /**
     * Calculates an optimization score based on the boost results
     */
    private fun calculateOptimizationScore(
        mode: BoostMode,
        optimizations: List<String>,
        ramFreed: Int,
        storageFreed: Long
    ): Int {
        var score = when (mode) {
            BoostMode.NORMAL -> 30
            BoostMode.ULTRA -> 60
            BoostMode.GAMING -> 80
        }
        
        // Add points for each optimization
        optimizations.forEach {
            when {
                it.contains("CPU") -> score += 10
                it.contains("Memory") -> score += 15
                it.contains("Network") -> score += 10
                it.contains("Thermal") -> score += 10
                it.contains("Game-specific") -> score += 15
                it.contains("Aggressive") -> score += 10
            }
        }
        
        // Add points for RAM freed (up to 10 points)
        score += (ramFreed / 200).coerceAtMost(10)
        
        // Add points for storage freed (up to 5 points)
        score += ((storageFreed / (1024 * 1024 * 100))).toInt().coerceAtMost(5)
        
        // Cap the score at 100
        return score.coerceAtMost(100)
    }
    
    //endregion
    
    //region Data Classes
    
    /**
     * Data class representing a boosted game's information
     */
    data class BoostedGameInfo(
        val gameName: String,
        val gamePackage: String,
        val boostCount: Int
    )
    
    /**
     * Data class representing the result of a boost operation
     */
    data class BoostResult(
        val id: Long,
        val mode: BoostMode,
        val optimizations: List<String>,
        val ramFreed: Int, // in MB
        val storageFreed: Long, // in bytes
        val statsBefore: BoostStats,
        val statsAfter: BoostStats,
        val timestamp: Date
    )
    
    //endregion
}

/**
 * Boost modes
 */
enum class BoostMode {
    NORMAL,
    ULTRA,
    GAMING
}
