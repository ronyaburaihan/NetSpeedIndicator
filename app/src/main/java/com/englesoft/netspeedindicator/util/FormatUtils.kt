package com.englesoft.netspeedindicator.util

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
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        
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
        val digitGroups = (Math.log10(bytesPerSecond.toDouble()) / Math.log10(1024.0)).toInt()
        
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
     * @return Pair of (Value String, Unit String) e.g. ("1.5", "MB")
     */
    fun formatSpeedCompact(bytesPerSecond: Long): Pair<String, String> {
        if (bytesPerSecond < 1024) return Pair(bytesPerSecond.toString(), "B")

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytesPerSecond.toDouble()) / Math.log10(1024.0)).toInt()

        val value = bytesPerSecond / 1024.0.pow(digitGroups.toDouble())
        val unit = units[digitGroups]
        
        // If value is >= 100, no decimal (e.g. "120 KB")
        // If value is < 10, 1 decimal (e.g. "1.5 MB")
        // If value is >= 10, no decimal (e.g. "15 MB") - simpler for small icon
        // Handle rounding edge case: 9.95 -> 10.0 (4 chars) -> show 10 (2 chars)
        val formattedValue = if (value >= 9.95) {
            String.format("%.0f", value)
        } else {
            String.format("%.1f", value)
        }
        
        return Pair(formattedValue, unit)
    }
}
