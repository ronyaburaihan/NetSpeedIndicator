package com.englesoft.netspeedindicator.data.manager

import com.englesoft.netspeedindicator.domain.model.SpeedModel
import com.englesoft.netspeedindicator.domain.model.UsageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton manager to hold real-time traffic state.
 * Acts as a single source of truth for both the Foreground Service and UI.
 */
@Singleton
class TrafficStateManager @Inject constructor() {

    private val _speed = MutableStateFlow(SpeedModel())
    val speed = _speed.asStateFlow()

    private val _dailyUsage = MutableStateFlow<UsageModel?>(null)
    val dailyUsage = _dailyUsage.asStateFlow()

    fun updateSpeed(speed: SpeedModel) {
        _speed.value = speed
    }

    fun updateDailyUsage(usage: UsageModel) {
        _dailyUsage.value = usage
    }
}
