package com.englesoft.netspeedindicator.data.repository

import com.englesoft.netspeedindicator.data.datasource.SpeedDataSource
import com.englesoft.netspeedindicator.domain.model.SpeedModel
import com.englesoft.netspeedindicator.domain.repository.SpeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SpeedRepository
 * Bridges data source and domain layer
 */
@Singleton
class SpeedRepositoryImpl @Inject constructor(
    private val speedDataSource: SpeedDataSource
) : SpeedRepository {
    
    override fun observeSpeed(): Flow<SpeedModel> {
        return speedDataSource.observeSpeed(intervalMs = 1000L)
            .map { (downloadSpeed, uploadSpeed) ->
                SpeedModel(
                    downloadBytesPerSecond = downloadSpeed,
                    uploadBytesPerSecond = uploadSpeed,
                    timestamp = System.currentTimeMillis()
                )
            }
    }
    
    override suspend fun getCurrentSpeed(): SpeedModel {
        // For snapshot, we return zero speed
        // Real-time monitoring should use observeSpeed()
        return SpeedModel(
            downloadBytesPerSecond = 0L,
            uploadBytesPerSecond = 0L,
            timestamp = System.currentTimeMillis()
        )
    }
}
