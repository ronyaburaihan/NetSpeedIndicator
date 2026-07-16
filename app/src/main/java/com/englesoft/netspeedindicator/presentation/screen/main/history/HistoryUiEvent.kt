package com.englesoft.netspeedindicator.presentation.screen.main.history

sealed interface HistoryUiEvent {
    data class OnSelectMonth(val monthsBack: Int) : HistoryUiEvent
}
