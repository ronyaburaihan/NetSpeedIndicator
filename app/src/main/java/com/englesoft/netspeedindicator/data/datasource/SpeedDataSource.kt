package com.englesoft.netspeedindicator.data.datasource

import android.net.TrafficStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for real-time speed monitoring
 * Uses Android TrafficStats API to track network bytes
 */
@Singleton
class SpeedDataSource @Inject constructor() {
    
    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastTimestamp = 0L
    
    /**
     * Observe speed updates at specified interval
     * @param intervalMs Update interval in milliseconds
     */
    fun observeSpeed(intervalMs: Long = 1000L): Flow<Pair<Long, Long>> = flow {
        // Initialize baseline
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTimestamp = System.currentTimeMillis()
        
        while (true) {
            delay(intervalMs)
            
            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()
            val currentTimestamp = System.currentTimeMillis()
            
            // Calculate delta
            var rxDelta = currentRxBytes - lastRxBytes
            var txDelta = currentTxBytes - lastTxBytes
            val timeDelta = currentTimestamp - lastTimestamp
            
            // Handle counter reset (e.g., reboot or overflow)
            if (rxDelta < 0) rxDelta = currentRxBytes
            if (txDelta < 0) txDelta = currentTxBytes
            
            // Calculate bytes per second
            val downloadSpeed = if (timeDelta > 0) {
                (rxDelta * 1000) / timeDelta
            } else {
                0L
            }
            
            val uploadSpeed = if (timeDelta > 0) {
                (txDelta * 1000) / timeDelta
            } else {
                0L
            }
            
            // Update last values
            lastRxBytes = currentRxBytes
            lastTxBytes = currentTxBytes
            lastTimestamp = currentTimestamp
            
            // Emit speed (download, upload) in bytes/sec
            emit(Pair(downloadSpeed.coerceAtLeast(0), uploadSpeed.coerceAtLeast(0)))
        }
    }
    
    /**
     * Get current total bytes transferred
     * @return Pair of (rxBytes, txBytes)
     */
    fun getCurrentTotalBytes(): Pair<Long, Long> {
        return Pair(
            TrafficStats.getTotalRxBytes(),
            TrafficStats.getTotalTxBytes()
        )
    }
}
