package com.englesoft.netspeedindicator.core.util

import kotlin.math.log10
import kotlin.math.pow

/**
 * Utility functions for formatting bytes and speeds
 */
object FormatUtils {
    
    /**
     * Format bytes to human-readable string
     * @param bytes Number of bytes
     * @return Formatted string (e.g., "1.5 GB", "250 MB", "10 KB")
     */
    fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "0 B"
        if (bytes < 1024) return "$bytes B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return String.format("%.1f %s", value, units[digitGroups])
    }
    
    /**
     * Format speed (bytes per second) to human-readable string
     * @param bytesPerSecond Speed in bytes per second
     * @return Formatted string (e.g., "2.1 MB/s", "480 KB/s", "5 Mbps")
     */
    fun formatSpeed(bytesPerSecond: Long): String {
        if (bytesPerSecond < 0) return "0 B/s"
        if (bytesPerSecond < 1024) return "$bytesPerSecond B/s"
        
        val units = arrayOf("B/s", "KB/s", "MB/s", "GB/s")
        val digitGroups = (log10(bytesPerSecond.toDouble()) / log10(1024.0)).toInt()
        
        val value = bytesPerSecond / 1024.0.pow(digitGroups.toDouble())
        return String.format("%.1f %s", value, units[digitGroups])
    }
    
    /**
     * Format speed to Mbps
     * @param bytesPerSecond Speed in bytes per second
     * @return Formatted string (e.g., "16.8 Mbps")
     */
    fun formatSpeedMbps(bytesPerSecond: Long): String {
        val mbps = (bytesPerSecond * 8) / 1_000_000.0
        return if (mbps < 0.1) {
            "${(bytesPerSecond * 8) / 1000.0} Kbps"
        } else {
            String.format("%.1f Mbps", mbps)
        }
    }

    /**
     * Format speed for status bar icon (compact)
     * @param bytesPerSecond Speed in bytes per second
     * @return Pair of (Value String, Unit String) e.g. ("50", "KB")
     */
    fun formatSpeedCompact(bytesPerSecond: Long): Pair<String, String> {
        val value: Double
        val unit: String
        
        if (bytesPerSecond >= 1024 * 1024 * 1024) { // GB
             value = bytesPerSecond / (1024.0 * 1024 * 1024)
             unit = "GB"
        } else if (bytesPerSecond >= 999 * 1024) { // Switch to MB at ~1000 KB to avoid 4 digits (e.g. 1023 KB)
             value = bytesPerSecond / (1024.0 * 1024)
             unit = "MB"
        } else {
             // KB (Default minimum unit)
             value = bytesPerSecond / 1024.0
             unit = "KB"
        }

        val formattedValue = if (unit == "KB") {
            String.format("%.0f", value) // Integer for KB
        } else {
             // MB or GB: Show decimal if < 100, else integer to save space
             if (value >= 99.95) {
                String.format("%.0f", value)
            } else {
                String.format("%.1f", value)
            }
        }
        
        return Pair(formattedValue, "$unit/s")
    }

    /**
     * Format speed - returns only the numeric value part
     */
    fun formatSpeedValue(bytesPerSecond: Long): String {
        if (bytesPerSecond < 0) return "0"
        if (bytesPerSecond < 1024) return "$bytesPerSecond"

        val digitGroups = (log10(bytesPerSecond.toDouble()) / log10(1024.0)).toInt()
        val value = bytesPerSecond / 1024.0.pow(digitGroups.toDouble())
        return String.format("%.1f", value)
    }

    /**
     * Format speed - returns only the unit part
     */
    fun formatSpeedUnit(bytesPerSecond: Long): String {
        if (bytesPerSecond < 0) return "B/s"
        if (bytesPerSecond < 1024) return "B/s"

        val units = arrayOf("B/s", "KB/s", "MB/s", "GB/s")
        val digitGroups = (log10(bytesPerSecond.toDouble()) / log10(1024.0)).toInt()
        return units[digitGroups]
    }

    /**
     * Format bytes - returns only the numeric value part
     */
    fun formatBytesValue(bytes: Long): String {
        if (bytes < 0) return "0"
        if (bytes < 1024) return "$bytes"

        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return String.format("%.1f", value)
    }

    /**
     * Format bytes - returns only the unit part
     */
    fun formatBytesUnit(bytes: Long): String {
        if (bytes < 0) return "B"
        if (bytes < 1024) return "B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return units[digitGroups]
    }

    /**
     * Format duration in seconds to HH:MM:SS
     */
    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}
