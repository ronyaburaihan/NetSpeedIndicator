package com.englesoft.netspeedindicator.presentation.screen.main.home

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.englesoft.netspeedindicator.core.service.SpeedMonitorService
import com.englesoft.netspeedindicator.data.manager.TrafficStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    trafficStateManager: TrafficStateManager
) : AndroidViewModel(application) {

    private val _isServiceRunning = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> =
        combine(
            _isServiceRunning,
            trafficStateManager.speed,
            trafficStateManager.dailyUsage
        ) { isRunning, speed, usage ->

            HomeUiState(
                isServiceRunning = isRunning,
                currentSpeed = speed,
                todayUsage = usage
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