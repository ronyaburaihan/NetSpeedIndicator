package com.englesoft.netspeedindicator.domain.usecase

import com.englesoft.netspeedindicator.domain.model.SpeedModel
import com.englesoft.netspeedindicator.domain.repository.SpeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe current internet speed
 * Follows single responsibility principle
 */
class GetCurrentSpeedUseCase @Inject constructor(
    private val speedRepository: SpeedRepository
) {
    operator fun invoke(): Flow<SpeedModel> {
        return speedRepository.observeSpeed()
    }
}
