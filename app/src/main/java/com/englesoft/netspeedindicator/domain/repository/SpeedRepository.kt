package com.englesoft.netspeedindicator.domain.repository

import com.englesoft.netspeedindicator.domain.model.SpeedModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for speed monitoring
 * Domain layer - defines contract without implementation details
 */
interface SpeedRepository {
    /**
     * Observe current internet speed in real-time
     * Emits speed updates periodically
     */
    fun observeSpeed(): Flow<SpeedModel>
    
    /**
     * Get current speed snapshot
     */
    suspend fun getCurrentSpeed(): SpeedModel
}
