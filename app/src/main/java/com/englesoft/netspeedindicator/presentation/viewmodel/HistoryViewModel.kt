package com.englesoft.netspeedindicator.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.englesoft.netspeedindicator.data.manager.TrafficStateManager
import com.englesoft.netspeedindicator.domain.model.UsageModel
import com.englesoft.netspeedindicator.domain.usecase.GetDailyUsageUseCase
import com.englesoft.netspeedindicator.domain.usecase.GetMonthlyUsageUseCase
import com.englesoft.netspeedindicator.presentation.service.SpeedMonitorService
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
    private val application: Application,
    private val getDailyUsageUseCase: GetDailyUsageUseCase,
    private val getMonthlyUsageUseCase: GetMonthlyUsageUseCase,
    private val trafficStateManager: TrafficStateManager
) : AndroidViewModel(application) {

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

    fun stopService() {
        val intent = Intent(application, SpeedMonitorService::class.java)
        application.stopService(intent)
    }

    fun exitApp() {
        stopService()
        // Close the application process
        android.os.Process.killProcess(android.os.Process.myPid())
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

    fun loadDailyUsage(days: Int = 31) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch existing usage from DB
                val dbUsageList = getDailyUsageUseCase.getLastNDays(days)
                val dbUsageMap = dbUsageList.associateBy { it.date }

                // Generate full list of dates for the last N days
                val fullList = mutableListOf<UsageModel>()
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                for (i in 0 until days) {
                    val date = today.minusDays(i.toLong())
                    val dateStr = date.format(formatter)

                    // Use existing data or create zero-usage model
                    val usage = dbUsageMap[dateStr] ?: UsageModel(
                        date = dateStr,
                        wifiRxBytes = 0,
                        wifiTxBytes = 0,
                        mobileRxBytes = 0,
                        mobileTxBytes = 0
                    )
                    fullList.add(usage)
                }

                _dailyUsage.value = fullList

                // Immediately check if we have a fresher value from Manager (for Today)
                val liveUsage = trafficStateManager.dailyUsage.value
                if (liveUsage != null) {
                    val currentList = fullList.toMutableList()
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
