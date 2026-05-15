package com.englesoft.netspeedindicator.presentation.screen.main.home

import com.englesoft.netspeedindicator.domain.model.SpeedInfo
import com.englesoft.netspeedindicator.domain.model.UsageInfo

data class HomeUiState(
    val isServiceRunning: Boolean = false,
    val currentSpeed: SpeedInfo = SpeedInfo(),
    val todayUsage: UsageInfo = UsageInfo(),
    val peakSpeed: Long = 0L,
    val sessionDurationSeconds: Long = 0L,
    val isLoading: Boolean = false,
    val message: String? = null,
    val showStopDialog: Boolean = false,
    val shouldFinishActivity: Boolean = false
)
