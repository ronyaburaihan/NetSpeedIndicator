package com.englesoft.netspeedindicator.presentation.screen.main.history

import android.app.Application
import android.content.Intent
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.englesoft.netspeedindicator.core.service.SpeedMonitorService
import com.englesoft.netspeedindicator.data.manager.TrafficStateManager
import com.englesoft.netspeedindicator.domain.model.UsageInfo
import com.englesoft.netspeedindicator.domain.usecase.GetDailyUsageUseCase
import com.englesoft.netspeedindicator.domain.usecase.GetMonthlyUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
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

    private val _dailyUsage = MutableStateFlow<List<UsageInfo>>(emptyList())
    val dailyUsage: StateFlow<List<UsageInfo>> = _dailyUsage.asStateFlow()

    private val _monthlyUsage = MutableStateFlow<List<UsageInfo>>(emptyList())
    val monthlyUsage: StateFlow<List<UsageInfo>> = _monthlyUsage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedMonthIndex = MutableStateFlow(0)
    val selectedMonthIndex: StateFlow<Int> = _selectedMonthIndex.asStateFlow()

    init {
        loadDailyUsage(0)
        observeRealtimeUsage()
    }

    fun selectMonth(index: Int) {
        _selectedMonthIndex.value = index
        loadDailyUsage(index)
    }

    fun stopService() {
        val intent = Intent(application, SpeedMonitorService::class.java)
        application.stopService(intent)
    }

    fun exitApp() {
        stopService()
        // Close the application process
        Process.killProcess(Process.myPid())
    }

    private fun observeRealtimeUsage() {
        viewModelScope.launch {
            trafficStateManager.dailyUsage.collect { liveUsage ->
                if (_selectedMonthIndex.value == 0) {
                    // Update the "Today" entry in the current list ONLY if viewing Current Month
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

    fun loadDailyUsage(monthOffset: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Calculate target month
                val targetMonth = YearMonth.now().minusMonths(monthOffset.toLong())
                val yearMonthStr = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

                // Fetch usage for that month
                val dbUsageList = getMonthlyUsageUseCase.getByMonth(yearMonthStr)
                val dbUsageMap = dbUsageList.associateBy { it.date }

                // Generate full list of dates for the month
                val fullList = mutableListOf<UsageInfo>()
                val daysInMonth = targetMonth.lengthOfMonth()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                // Iterate from last day to first day (descending)
                // Use current day if in current month to avoid future dates?
                // UI shows full month usually, but maybe restrict to "today" if current month?
                // The requirement says "exact same as given ui", usually history shows all days or up to today.
                // Let's show all days of the month for consistency, or up to today for current month.

                val lastDayToShow = if (monthOffset == 0) {
                    LocalDate.now().dayOfMonth
                } else {
                    daysInMonth
                }

                for (day in lastDayToShow downTo 1) {
                    val date = targetMonth.atDay(day)
                    val dateStr = date.format(formatter)

                    val usage = dbUsageMap[dateStr] ?: UsageInfo(
                        date = dateStr,
                        wifiRxBytes = 0, wifiTxBytes = 0, mobileRxBytes = 0, mobileTxBytes = 0
                    )
                    fullList.add(usage)
                }

                _dailyUsage.value = fullList

                // Only sync realtime for current month
                if (monthOffset == 0) {
                    val liveUsage = trafficStateManager.dailyUsage.value
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
}