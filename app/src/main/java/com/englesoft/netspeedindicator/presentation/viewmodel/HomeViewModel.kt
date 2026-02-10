package com.englesoft.netspeedindicator.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.englesoft.netspeedindicator.data.manager.TrafficStateManager
import com.englesoft.netspeedindicator.domain.model.SpeedModel
import com.englesoft.netspeedindicator.domain.model.UsageModel
import com.englesoft.netspeedindicator.presentation.service.SpeedMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home screen
 * Manages UI state for current speed and today's usage
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val trafficStateManager: TrafficStateManager
) : AndroidViewModel(application) {
    
    // Expose flows directly from TrafficStateManager to ensure UI is perfectly synced with Service
    val currentSpeed: StateFlow<SpeedModel> = trafficStateManager.speed
    val todayUsage: StateFlow<UsageModel?> = trafficStateManager.dailyUsage
    
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()
    
    fun startService() {
        val intent = Intent(application, SpeedMonitorService::class.java)
        ContextCompat.startForegroundService(application, intent)
        _isServiceRunning.value = true
    }
    
    fun stopService() {
        val intent = Intent(application, SpeedMonitorService::class.java)
        application.stopService(intent)
        _isServiceRunning.value = false
    }
}

