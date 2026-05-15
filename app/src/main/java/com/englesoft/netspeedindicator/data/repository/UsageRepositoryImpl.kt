package com.englesoft.netspeedindicator.data.repository

import com.englesoft.netspeedindicator.data.datasource.UsageDataSource
import com.englesoft.netspeedindicator.data.local.dao.UsageDao
import com.englesoft.netspeedindicator.data.mapper.toDomain
import com.englesoft.netspeedindicator.data.mapper.toEntity
import com.englesoft.netspeedindicator.domain.model.UsageInfo
import com.englesoft.netspeedindicator.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UsageRepository
 * Combines Room database and NetworkStatsManager data
 */
@Singleton
class UsageRepositoryImpl @Inject constructor(
    private val usageDao: UsageDao,
    private val usageDataSource: UsageDataSource
) : UsageRepository {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    override suspend fun saveUsage(usage: UsageInfo) {
        usageDao.insertOrUpdate(usage.toEntity())
    }
    
    override suspend fun getTodayUsage(): UsageInfo? {
        val today = LocalDate.now().format(dateFormatter)
        
        // Try to get accurate data from NetworkStatsManager first
        val systemUsage = usageDataSource.getUsageForDate(today)
        if (systemUsage != null) {
            // Sync with DB
            saveUsage(systemUsage)
            return systemUsage
        }
        
        // Fallback to DB (manual tracking)
        return usageDao.getByDate(today)?.toDomain()
    }
    
    override suspend fun getUsageByDate(date: String): UsageInfo? {
        // Try to get accurate data from NetworkStatsManager first
        val systemUsage = usageDataSource.getUsageForDate(date)
        if (systemUsage != null) {
            // Sync with DB
            saveUsage(systemUsage)
            return systemUsage
        }
        
        return usageDao.getByDate(date)?.toDomain()
    }
    
    override suspend fun getUsageByDateRange(startDate: String, endDate: String): List<UsageInfo> {
        // Sync history with system data
        val start = LocalDate.parse(startDate, dateFormatter)
        val end = LocalDate.parse(endDate, dateFormatter)
        
        var current = start
        while (!current.isAfter(end)) {
            val dateStr = current.format(dateFormatter)
            // Try to fetch accurate usage from system
            val systemUsage = usageDataSource.getUsageForDate(dateStr)
            if (systemUsage != null) {
                saveUsage(systemUsage)
            }
            current = current.plusDays(1)
        }
        
        return usageDao.getByDateRange(startDate, endDate).map { it.toDomain() }
    }
    
    override fun observeTodayUsage(): Flow<UsageInfo?> {
        val today = LocalDate.now().format(dateFormatter)
        return usageDao.observeByDate(today).map { it?.toDomain() }
    }
    
    override suspend fun getMonthlyUsage(yearMonth: String): List<UsageInfo> {
        // Sync monthly data with system
        val yearMonthObj = java.time.YearMonth.parse(yearMonth)
        val startDate = yearMonthObj.atDay(1)
        val endDate = yearMonthObj.atEndOfMonth().coerceAtMost(LocalDate.now()) // Don't sync future dates
        
        var current = startDate
        while (!current.isAfter(endDate)) {
            val dateStr = current.format(dateFormatter)
            val systemUsage = usageDataSource.getUsageForDate(dateStr)
            if (systemUsage != null) {
                saveUsage(systemUsage)
            }
            current = current.plusDays(1)
        }
        
        return usageDao.getByMonth(yearMonth).map { it.toDomain() }
    }
}
