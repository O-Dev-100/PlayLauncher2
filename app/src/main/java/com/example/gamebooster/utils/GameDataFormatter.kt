package com.example.gamebooster.utils

import android.content.Context
import com.example.gamebooster.R
import java.text.CharacterIterator
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for formatting game-related data
 */
object GameDataFormatter {

    /**
     * Format bytes to a human-readable string (e.g., 1.2 GB)
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        
        return String.format(
            "%.1f %s",
            bytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups.coerceAtMost(units.size - 1)]
        )
    }

    /**
     * Format install count to a human-readable string (e.g., 1.2M+)
     */
    fun formatInstallCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM+", count / 1_000_000f)
            count >= 1_000 -> String.format("%dK+", count / 1_000)
            count > 0 -> "$count+"
            else -> ""
        }
    }

    /**
     * Format rating to a star display (e.g., "4.5 ★")
     */
    fun formatRating(rating: Double): String {
        return String.format(Locale.getDefault(), "%.1f ★", rating)
    }

    /**
     * Format date to a relative time string (e.g., "2 days ago")
     */
    fun formatRelativeTime(date: Date, context: Context): String {
        val now = Date()
        val diffInSeconds = (now.time - date.time) / 1000
        
        return when {
            diffInSeconds < 60 -> context.getString(R.string.just_now)
            diffInSeconds < 3600 -> {
                val minutes = (diffInSeconds / 60).toInt()
                context.resources.getQuantityString(
                    R.plurals.minutes_ago,
                    minutes,
                    minutes
                )
            }
            diffInSeconds < 86400 -> {
                val hours = (diffInSeconds / 3600).toInt()
                context.resources.getQuantityString(
                    R.plurals.hours_ago,
                    hours,
                    hours
                )
            }
            diffInSeconds < 2592000 -> { // 30 days
                val days = (diffInSeconds / 86400).toInt()
                context.resources.getQuantityString(
                    R.plurals.days_ago,
                    days,
                    days
                )
            }
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                sdf.format(date)
            }
        }
    }

    /**
     * Format a string to title case (e.g., "hello world" -> "Hello World")
     */
    fun toTitleCase(text: String?): String {
        if (text.isNullOrBlank()) return ""
        
        val result = StringBuilder()
        var nextTitleCase = true
        
        for (c in text) {
            if (c == ' ') {
                nextTitleCase = true
                result.append(' ')
            } else if (nextTitleCase) {
                result.append(Character.toTitleCase(c))
                nextTitleCase = false
            } else {
                result.append(Character.toLowerCase(c))
            }
        }
        
        return result.toString()
    }

    /**
     * Format file size string from Play Store format (e.g., "12 MB" -> 12582912)
     */
    fun parseFileSize(size: String?): Long {
        if (size.isNullOrBlank()) return 0L
        
        val pattern = "([0-9]+(?:\\.?[0-9]+)?)\\s*([KMGTP]?B)".toRegex(RegexOption.IGNORE_CASE)
        val matchResult = pattern.find(size.trim()) ?: return 0L
        
        val (valueStr, unit) = matchResult.destructured
        val value = valueStr.toDouble()
        
        return when (unit.uppercase()) {
            "KB" -> (value * 1024).toLong()
            "MB" -> (value * 1024 * 1024).toLong()
            "GB" -> (value * 1024 * 1024 * 1024).toLong()
            "TB" -> (value * 1024 * 1024 * 1024 * 1024).toLong()
            "B" -> value.toLong()
            else -> 0L
        }
    }

    /**
     * Format install count string from Play Store format (e.g., "1,000,000+" -> 1000000)
     */
    fun parseInstallCount(installs: String?): Int {
        if (installs.isNullOrBlank()) return 0
        
        // Remove commas and + sign
        val cleanString = installs.replace("[+,]+".toRegex(), "")
        return try {
            cleanString.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    /**
     * Format price text (e.g., "$0.99" -> "$0.99" or "Free")
     */
    fun formatPrice(priceText: String?, isFree: Boolean): String {
        return if (isFree || priceText.isNullOrBlank()) {
            "Free"
        } else {
            priceText
        }
    }

    /**
     * Format version information with optional version code
     */
    fun formatVersion(version: String?, versionCode: Int? = null): String {
        return if (versionCode != null) {
            "$version ($versionCode)"
        } else {
            version ?: "N/A"
        }
    }
}
