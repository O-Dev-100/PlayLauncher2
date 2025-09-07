package com.example.gamebooster.model

import com.example.gamebooster.viewmodel.BoostMode
import java.util.*

/**
 * Represents the result of a boost operation
 */
data class BoostResult(
    val mode: BoostMode,
    val optimizations: List<String>,
    val durationMs: Long,
    val timestamp: Date,
    val game: InstalledApp? = null,
    val stats: BoostStats = BoostStats()
) {
    /**
     * Calculates a score from 0-100 based on the boost results
     */
    fun calculateScore(): Int {
        var score = 0
        
        // Base score based on mode
        score += when (mode) {
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
        
        // Cap the score at 100
        return score.coerceAtMost(100)
    }
    
    /**
     * Gets a human-readable description of the boost result
     */
    fun getDescription(): String {
        return when (mode) {
            BoostMode.NORMAL -> "Standard optimization completed successfully"
            BoostMode.ULTRA -> "Ultra optimization completed with aggressive settings"
            BoostMode.GAMING -> "Gaming optimization applied for ${game?.appName ?: "selected game"}"
        }
    }
    
    /**
     * Gets a list of improvements made during the boost
     */
    fun getImprovements(): List<String> {
        return optimizations.map {
            when {
                it.startsWith("Memory") -> "Freed $it of memory"
                it.startsWith("CPU") -> "Optimized CPU performance"
                it.startsWith("Network") -> "Improved network connectivity"
                it.startsWith("Thermal") -> "Optimized thermal performance"
                it.startsWith("Game-specific") -> "Applied game-specific optimizations"
                it.startsWith("Aggressive") -> "Applied aggressive optimizations"
                else -> it
            }
        }
    }
}
