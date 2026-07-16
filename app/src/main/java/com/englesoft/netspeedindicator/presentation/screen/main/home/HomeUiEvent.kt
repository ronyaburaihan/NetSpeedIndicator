package com.englesoft.netspeedindicator.presentation.screen.main.home

sealed interface HomeUiEvent {
    data object OnStopClick : HomeUiEvent
    data object OnDismissDialog : HomeUiEvent
    data object OnConfirmStop : HomeUiEvent
    data object OnActivityFinished : HomeUiEvent
}
