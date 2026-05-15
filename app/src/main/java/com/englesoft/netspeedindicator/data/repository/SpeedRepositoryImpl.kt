package com.englesoft.netspeedindicator.data.repository

import com.englesoft.netspeedindicator.data.datasource.SpeedDataSource
import com.englesoft.netspeedindicator.domain.model.SpeedInfo
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

    override fun observeSpeed(): Flow<SpeedInfo> {
        return speedDataSource.observeSpeed(intervalMs = 1000L)
            .map { (downloadSpeed, uploadSpeed) ->
                SpeedInfo(
                    downloadBytesPerSecond = downloadSpeed,
                    uploadBytesPerSecond = uploadSpeed,
                    totalBytesPerSecond = downloadSpeed + uploadSpeed,
                    timestamp = System.currentTimeMillis()
                )
            }
    }

    override suspend fun getCurrentSpeed(): SpeedInfo {
        // For snapshot, we return zero speed
        // Real-time monitoring should use observeSpeed()
        return SpeedInfo(
            downloadBytesPerSecond = 0L,
            uploadBytesPerSecond = 0L,
            timestamp = System.currentTimeMillis()
        )
    }
}
