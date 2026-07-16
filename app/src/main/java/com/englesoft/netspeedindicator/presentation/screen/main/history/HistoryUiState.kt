package com.englesoft.netspeedindicator.presentation.screen.main.history

import com.englesoft.netspeedindicator.domain.model.UsageInfo

data class HistoryUiState(
    val dailyUsage: List<UsageInfo> = emptyList(),
    val isLoading: Boolean = false,
    val selectedMonthIndex: Int = 0
)
