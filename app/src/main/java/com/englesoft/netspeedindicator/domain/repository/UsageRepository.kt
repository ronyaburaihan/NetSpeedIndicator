package com.englesoft.netspeedindicator.domain.repository

import com.englesoft.netspeedindicator.domain.model.UsageModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for usage data operations
 * Domain layer - defines contract without implementation details
 */
interface UsageRepository {
    /**
     * Save or update usage data for a specific date
     */
    suspend fun saveUsage(usage: UsageModel)
    
    /**
     * Get usage data for today
     */
    suspend fun getTodayUsage(): UsageModel?
    
    /**
     * Get usage data for a specific date
     */
    suspend fun getUsageByDate(date: String): UsageModel?
    
    /**
     * Get usage data for a date range
     */
    suspend fun getUsageByDateRange(startDate: String, endDate: String): List<UsageModel>
    
    /**
     * Observe today's usage in real-time
     */
    fun observeTodayUsage(): Flow<UsageModel?>
    
    /**
     * Get current month's total usage
     */
    suspend fun getMonthlyUsage(yearMonth: String): List<UsageModel>
}
