package com.englesoft.netspeedindicator.domain.usecase

import com.englesoft.netspeedindicator.domain.model.UsageInfo
import com.englesoft.netspeedindicator.domain.repository.UsageRepository
import javax.inject.Inject

/**
 * Use case to save usage data
 * Encapsulates business logic for persisting usage
 */
class SaveUsageUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke(usage: UsageInfo) {
        usageRepository.saveUsage(usage)
    }
}
