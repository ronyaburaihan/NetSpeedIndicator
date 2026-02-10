package com.englesoft.netspeedindicator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englesoft.netspeedindicator.data.manager.TrafficStateManager
import com.englesoft.netspeedindicator.domain.model.UsageModel
import com.englesoft.netspeedindicator.domain.usecase.GetDailyUsageUseCase
import com.englesoft.netspeedindicator.domain.usecase.GetMonthlyUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for History screen
 * Manages daily and monthly usage history
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getDailyUsageUseCase: GetDailyUsageUseCase,
    private val getMonthlyUsageUseCase: GetMonthlyUsageUseCase,
    private val trafficStateManager: TrafficStateManager
) : ViewModel() {
    
    private val _dailyUsage = MutableStateFlow<List<UsageModel>>(emptyList())
    val dailyUsage: StateFlow<List<UsageModel>> = _dailyUsage.asStateFlow()
    
    private val _monthlyUsage = MutableStateFlow<List<UsageModel>>(emptyList())
    val monthlyUsage: StateFlow<List<UsageModel>> = _monthlyUsage.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadDailyUsage()
        loadMonthlyUsage()
        observeRealtimeUsage()
    }
    
    private fun observeRealtimeUsage() {
        viewModelScope.launch {
            trafficStateManager.dailyUsage.collect { liveUsage ->
                if (liveUsage != null) {
                    // Update the "Today" entry in the current list
                    val currentList = _dailyUsage.value.toMutableList()
                    val todayStr = liveUsage.date
                    
                    val index = currentList.indexOfFirst { it.date == todayStr }
                    if (index != -1) {
                        currentList[index] = liveUsage
                    } else {
                        // If today isn't in the list yet (e.g. new day), add it at the top
                        // Assuming list is sorted desc
                        currentList.add(0, liveUsage)
                    }
                    _dailyUsage.value = currentList
                }
            }
        }
    }
    
    fun loadDailyUsage(days: Int = 30) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val usage = getDailyUsageUseCase.getLastNDays(days)
                _dailyUsage.value = usage
                
                // Immediately check if we have a fresher value from Manager
                val liveUsage = trafficStateManager.dailyUsage.value
                if (liveUsage != null) {
                    val currentList = usage.toMutableList()
                    val index = currentList.indexOfFirst { it.date == liveUsage.date }
                    if (index != -1) {
                        currentList[index] = liveUsage
                        _dailyUsage.value = currentList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadMonthlyUsage() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val usage = getMonthlyUsageUseCase.getCurrentMonth()
                _monthlyUsage.value = usage
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
