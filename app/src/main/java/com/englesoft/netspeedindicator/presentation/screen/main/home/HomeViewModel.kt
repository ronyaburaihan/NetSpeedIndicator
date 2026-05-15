package com.englesoft.netspeedindicator.presentation.screen.main.home

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.englesoft.netspeedindicator.core.service.SpeedMonitorService
import com.englesoft.netspeedindicator.data.manager.TrafficStateManager
import com.englesoft.netspeedindicator.domain.model.SpeedInfo
import com.englesoft.netspeedindicator.domain.model.UsageInfo
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

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _isServiceRunning = MutableStateFlow(false)
    private val _peakSpeed = MutableStateFlow(0L)
    private val _sessionDuration = MutableStateFlow(0L)
    private val _showStopDialog = MutableStateFlow(false)
    private val _shouldFinishActivity = MutableStateFlow(false)

    init {
        // Track peak speed
        viewModelScope.launch {
            try {
                trafficStateManager.speed.collect { speed ->
                    if (speed.totalBytesPerSecond > _peakSpeed.value) {
                        _peakSpeed.value = speed.totalBytesPerSecond
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error tracking peak speed", e)
            }
        }

        // Session timer
        viewModelScope.launch {
            try {
                while (true) {
                    delay(1000)
                    _sessionDuration.value += 1
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in session timer", e)
            }
        }
    }

    val uiState: StateFlow<HomeUiState> =
        combine(
            _isServiceRunning,
            trafficStateManager.speed,
            trafficStateManager.dailyUsage,
            _peakSpeed,
            _sessionDuration,
            _showStopDialog,
            _shouldFinishActivity
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val isRunning = values[0] as Boolean
            val speed = values[1] as SpeedInfo
            val usage = values[2] as UsageInfo
            val peak = values[3] as Long
            val session = values[4] as Long
            val showDialog = values[5] as Boolean
            val shouldFinish = values[6] as Boolean

            HomeUiState(
                isServiceRunning = isRunning,
                currentSpeed = speed,
                todayUsage = usage,
                peakSpeed = peak,
                sessionDurationSeconds = session,
                showStopDialog = showDialog,
                shouldFinishActivity = shouldFinish
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HomeUiState()
        )

    fun showStopDialog() {
        _showStopDialog.value = true
    }

    fun hideStopDialog() {
        _showStopDialog.value = false
    }

    fun startService() {
        try {
            val intent = Intent(getApplication(), SpeedMonitorService::class.java)
            ContextCompat.startForegroundService(getApplication(), intent)
            _isServiceRunning.value = true
            Log.d(TAG, "Service started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service", e)
        }
    }

    fun stopService() {
        try {
            val intent = Intent(getApplication(), SpeedMonitorService::class.java)
            getApplication<Application>().stopService(intent)
            _isServiceRunning.value = false
            _showStopDialog.value = false
            Log.d(TAG, "Service stopped")
            
            // Signal to finish the activity
            viewModelScope.launch {
                // Small delay to ensure service stops gracefully
                delay(300)
                _shouldFinishActivity.value = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service", e)
        }
    }
    
    fun onActivityFinished() {
        _shouldFinishActivity.value = false
    }
}