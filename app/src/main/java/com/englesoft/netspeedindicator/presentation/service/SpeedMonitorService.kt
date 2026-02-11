package com.englesoft.netspeedindicator.presentation.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.englesoft.netspeedindicator.data.manager.TrafficStateManager
import com.englesoft.netspeedindicator.domain.model.UsageModel
import com.englesoft.netspeedindicator.domain.usecase.GetCurrentSpeedUseCase
import com.englesoft.netspeedindicator.domain.usecase.GetDailyUsageUseCase
import com.englesoft.netspeedindicator.domain.usecase.SaveUsageUseCase
import com.englesoft.netspeedindicator.util.FormatUtils
import com.englesoft.netspeedindicator.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Foreground service for real-time speed monitoring
 * Runs continuously and updates notification every second
 */
@AndroidEntryPoint
class SpeedMonitorService : Service() {
    
    @Inject
    lateinit var getCurrentSpeedUseCase: GetCurrentSpeedUseCase
    
    @Inject
    lateinit var getDailyUsageUseCase: GetDailyUsageUseCase
    
    @Inject
    lateinit var saveUsageUseCase: SaveUsageUseCase

    @Inject
    lateinit var trafficStateManager: TrafficStateManager
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var monitoringJob: Job? = null
    
    // Session tracking
    private var sessionRxBytes = 0L
    private var sessionTxBytes = 0L
    
    private var sessionWifiRxBytes = 0L
    private var sessionWifiTxBytes = 0L
    private var sessionMobileRxBytes = 0L
    private var sessionMobileTxBytes = 0L

    // Base usage loaded from DB
    private var baseUsage: UsageModel? = null
    
    // System Services
    private lateinit var notificationManager: NotificationManager
    private lateinit var connectivityManager: ConnectivityManager
    
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationHelper.buildNotification(
            this,
            "0 B/s",
            "0 B",
            "0 B",
            "--%"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NotificationHelper.NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC // Use same type for R+ if appropriate or 0
                }
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID, notification)
        }
        
        startMonitoring()
        
        return START_STICKY
    }
    
    private fun startMonitoring() {
        monitoringJob?.cancel()
        
        monitoringJob = serviceScope.launch {
            // Load today's usage from DB
            loadTodayUsage()
            
            // Start speed monitoring
            getCurrentSpeedUseCase()
                .catch { e ->
                    e.printStackTrace()
                }
                .collect { speed ->
                    // 1. Update State Manager for Speed
                    trafficStateManager.updateSpeed(speed)

                    // 2. Attribute usage to network type
                    val isWifi = isWifiConnected()
                    
                    // Increment session counters
                    sessionRxBytes += speed.downloadBytesPerSecond
                    sessionTxBytes += speed.uploadBytesPerSecond
                    
                    if (isWifi) {
                        sessionWifiRxBytes += speed.downloadBytesPerSecond
                        sessionWifiTxBytes += speed.uploadBytesPerSecond
                    } else {
                        sessionMobileRxBytes += speed.downloadBytesPerSecond
                        sessionMobileTxBytes += speed.uploadBytesPerSecond
                    }
                    
                    // 3. Create Live Usage Model
                    // Base + Session
                    val liveUsage = UsageModel(
                        date = baseUsage?.date ?: LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        wifiRxBytes = (baseUsage?.wifiRxBytes ?: 0L) + sessionWifiRxBytes,
                        wifiTxBytes = (baseUsage?.wifiTxBytes ?: 0L) + sessionWifiTxBytes,
                        mobileRxBytes = (baseUsage?.mobileRxBytes ?: 0L) + sessionMobileRxBytes,
                        mobileTxBytes = (baseUsage?.mobileTxBytes ?: 0L) + sessionMobileTxBytes
                    )
                    
                    // 4. Update State Manager for Usage (Syncs UI)
                    trafficStateManager.updateDailyUsage(liveUsage)

                    // 5. Format strings for Notification
                    val downloadSpeed = FormatUtils.formatSpeed(speed.downloadBytesPerSecond)
                    
                    val mobileUsageTotal = liveUsage.mobileRxBytes + liveUsage.mobileTxBytes
                    val wifiUsageTotal = liveUsage.wifiRxBytes + liveUsage.wifiTxBytes
                    
                    val mobileUsageStr = FormatUtils.formatBytes(mobileUsageTotal)
                    val wifiUsageStr = FormatUtils.formatBytes(wifiUsageTotal)
                    
                    // Format for Status Bar Icon (Compact)
                    val (speedValue, speedUnit) = FormatUtils.formatSpeedCompact(speed.downloadBytesPerSecond)
                    
                    val signalStrength = getSignalStrength()

                    // 6. Update Notification (Use notify, NOT startForeground repeatedly)
                    val notification = NotificationHelper.buildNotification(
                        this@SpeedMonitorService,
                        downloadSpeed,
                        mobileUsageStr,
                        wifiUsageStr,
                        signalStrength,
                        speedValue,
                        speedUnit
                    )
                    notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)
                }
        }
        
        // Periodic save job (every 5 minutes)
        serviceScope.launch {
            while (true) {
                delay(5 * 60 * 1000L) // 5 minutes
                saveCurrentUsage()
            }
        }
    }
    
    private suspend fun loadTodayUsage() {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        baseUsage = getDailyUsageUseCase.getByDate(todayStr) ?: UsageModel(
            date = todayStr,
            wifiRxBytes = 0, wifiTxBytes = 0, mobileRxBytes = 0, mobileTxBytes = 0
        )
        // Reset session counters on load/reload
        sessionRxBytes = 0L
        sessionTxBytes = 0L
        sessionWifiRxBytes = 0L
        sessionWifiTxBytes = 0L
        sessionMobileRxBytes = 0L
        sessionMobileTxBytes = 0L
    }
    
    private suspend fun saveCurrentUsage() {
        // Do not save if baseUsage is not loaded yet
        val currentBase = baseUsage ?: return
        
        // Create current total model (Base + Session)
        // We do NOT reset session counters here to avoid race conditions with the collector
        val currentTotalUsage = UsageModel(
            date = currentBase.date,
            wifiRxBytes = currentBase.wifiRxBytes + sessionWifiRxBytes,
            wifiTxBytes = currentBase.wifiTxBytes + sessionWifiTxBytes,
            mobileRxBytes = currentBase.mobileRxBytes + sessionMobileRxBytes,
            mobileTxBytes = currentBase.mobileTxBytes + sessionMobileTxBytes
        )
        
        // Save to DB (Overwrite existing record for this date)
        saveUsageUseCase(currentTotalUsage)
        
        // Note: We do NOT update baseUsage to currentTotalUsage and reset session.
        // We keep baseUsage as the "snapshot at start" and session as "delta since start".
        // This is thread-safe(r) because only the collector writes to session counters.
    }
    
    private fun isWifiConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun getSignalStrength(): String {
        try {
            if (isWifiConnected()) {
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                val info = wifiManager.connectionInfo
                @Suppress("DEPRECATION")
                val level = android.net.wifi.WifiManager.calculateSignalLevel(info.rssi, 100)
                return "$level%"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "--%"
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Save usage before stopping
        // Use runBlocking to ensure save completes before service is destroyed
        runBlocking {
            saveCurrentUsage()
        }
        
        monitoringJob?.cancel()
        serviceScope.cancel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        const val ACTION_START = "com.englesoft.netspeedindicator.START_MONITORING"
        const val ACTION_STOP = "com.englesoft.netspeedindicator.STOP_MONITORING"
    }
}
