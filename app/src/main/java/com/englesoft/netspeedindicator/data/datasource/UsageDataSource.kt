package com.englesoft.netspeedindicator.data.datasource

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for accurate usage tracking
 * Uses NetworkStatsManager for detailed network statistics
 * Requires PACKAGE_USAGE_STATS permission
 */
@Singleton
class UsageDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val networkStatsManager: NetworkStatsManager? by lazy {
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
    }
    
    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    /**
     * Get usage for a specific date
     * @param date Format: yyyy-MM-dd
     * @return Pair of (wifiBytes, mobileBytes) where each is Pair(rx, tx)
     */
    fun getUsageForDate(date: String): Pair<Pair<Long, Long>, Pair<Long, Long>> {
        val localDate = LocalDate.parse(date)
        val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        
        return getUsageForPeriod(startOfDay.toEpochMilli(), endOfDay.toEpochMilli())
    }
    
    /**
     * Get usage for a time period
     * @return Pair of (wifiBytes, mobileBytes) where each is Pair(rx, tx)
     */
    private fun getUsageForPeriod(startMillis: Long, endMillis: Long): Pair<Pair<Long, Long>, Pair<Long, Long>> {
        val statsManager = networkStatsManager
        
        if (statsManager == null) {
            // Fallback: return zeros if NetworkStatsManager not available
            return Pair(Pair(0L, 0L), Pair(0L, 0L))
        }
        
        try {
            // Get WiFi usage
            val wifiSummary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                statsManager.querySummaryForDevice(
                    ConnectivityManager.TYPE_WIFI,
                    null,
                    startMillis,
                    endMillis
                )
            } else {
                null
            }
            
            val wifiRx = wifiSummary?.rxBytes ?: 0L
            val wifiTx = wifiSummary?.txBytes ?: 0L
            
            // Get Mobile usage
            val mobileSummary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                statsManager.querySummaryForDevice(
                    ConnectivityManager.TYPE_MOBILE,
                    null,
                    startMillis,
                    endMillis
                )
            } else {
                null
            }
            
            val mobileRx = mobileSummary?.rxBytes ?: 0L
            val mobileTx = mobileSummary?.txBytes ?: 0L
            
            return Pair(Pair(wifiRx, wifiTx), Pair(mobileRx, mobileTx))
            
        } catch (e: Exception) {
            // Permission not granted or other error
            e.printStackTrace()
            return Pair(Pair(0L, 0L), Pair(0L, 0L))
        }
    }
    
    /**
     * Check if currently connected to WiFi
     */
    fun isWifiConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Check if currently connected to mobile data
     */
    fun isMobileConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
