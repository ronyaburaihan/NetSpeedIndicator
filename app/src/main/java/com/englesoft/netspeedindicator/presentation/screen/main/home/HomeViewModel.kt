package com.englesoft.netspeedindicator.presentation.screen.main.home

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.englesoft.netspeedindicator.core.service.SpeedMonitorService
import com.englesoft.netspeedindicator.data.manager.TrafficStateManager
import com.englesoft.netspeedindicator.domain.model.SpeedInfo
import com.englesoft.netspeedindicator.domain.model.UsageInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val trafficStateManager: TrafficStateManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    val currentSpeed: StateFlow<SpeedInfo> = trafficStateManager.speed
    val todayUsage: StateFlow<UsageInfo?> = trafficStateManager.dailyUsage

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