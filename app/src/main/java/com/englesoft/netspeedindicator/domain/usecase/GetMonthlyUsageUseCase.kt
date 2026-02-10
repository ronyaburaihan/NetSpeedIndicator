package com.englesoft.netspeedindicator.domain.usecase

import com.englesoft.netspeedindicator.domain.model.UsageModel
import com.englesoft.netspeedindicator.domain.repository.UsageRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Use case to get monthly usage data
 * Handles month-based queries and aggregation
 */
class GetMonthlyUsageUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    /**
     * Get usage for current month
     */
    suspend fun getCurrentMonth(): List<UsageModel> {
        val yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return usageRepository.getMonthlyUsage(yearMonth)
    }
    
    /**
     * Get usage for a specific month
     * @param yearMonth Format: yyyy-MM (e.g., "2024-01")
     */
    suspend fun getByMonth(yearMonth: String): List<UsageModel> {
        return usageRepository.getMonthlyUsage(yearMonth)
    }
    
    /**
     * Get total usage for current month
     */
    suspend fun getCurrentMonthTotal(): UsageModel {
        val monthlyData = getCurrentMonth()
        return aggregateUsage(monthlyData)
    }
    
    /**
     * Aggregate multiple usage records into a single total
     */
    private fun aggregateUsage(usageList: List<UsageModel>): UsageModel {
        if (usageList.isEmpty()) {
            val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            return UsageModel(date = "$currentMonth-01")
        }
        
        return UsageModel(
            date = usageList.first().date,
            wifiRxBytes = usageList.sumOf { it.wifiRxBytes },
            wifiTxBytes = usageList.sumOf { it.wifiTxBytes },
            mobileRxBytes = usageList.sumOf { it.mobileRxBytes },
            mobileTxBytes = usageList.sumOf { it.mobileTxBytes }
        )
    }
}
