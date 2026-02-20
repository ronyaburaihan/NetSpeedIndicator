package com.englesoft.netspeedindicator.presentation.screen.main.home

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.englesoft.netspeedindicator.core.service.SpeedMonitorService
import com.englesoft.netspeedindicator.data.manager.TrafficStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    trafficStateManager: TrafficStateManager
) : AndroidViewModel(application) {

    private val _isServiceRunning = MutableStateFlow(false)
    private val _peakSpeed = MutableStateFlow(0L)
    private val _sessionDuration = MutableStateFlow(0L)

    init {
        // Track peak speed
        viewModelScope.launch {
            trafficStateManager.speed.collect { speed ->
                if (speed.totalBytesPerSecond > _peakSpeed.value) {
                    _peakSpeed.value = speed.totalBytesPerSecond
                }
            }
        }

        // Session timer
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _sessionDuration.value += 1
            }
        }
    }

    val uiState: StateFlow<HomeUiState> =
        combine(
            _isServiceRunning,
            trafficStateManager.speed,
            trafficStateManager.dailyUsage,
            _peakSpeed,
            _sessionDuration
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val isRunning = values[0] as Boolean
            val speed = values[1] as com.englesoft.netspeedindicator.domain.model.SpeedInfo
            val usage = values[2] as com.englesoft.netspeedindicator.domain.model.UsageInfo
            val peak = values[3] as Long
            val session = values[4] as Long

            HomeUiState(
                isServiceRunning = isRunning,
                currentSpeed = speed,
                todayUsage = usage,
                peakSpeed = peak,
                sessionDurationSeconds = session
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HomeUiState()
        )

    fun startService() {
        val intent = Intent(getApplication(), SpeedMonitorService::class.java)
        ContextCompat.startForegroundService(getApplication(), intent)
        _isServiceRunning.value = true
    }

    fun stopService() {
        val intent = Intent(getApplication(), SpeedMonitorService::class.java)
        getApplication<Application>().stopService(intent)
        _isServiceRunning.value = false
    }
}