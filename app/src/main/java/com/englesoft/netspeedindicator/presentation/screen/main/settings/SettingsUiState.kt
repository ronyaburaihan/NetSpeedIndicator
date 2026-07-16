package com.englesoft.netspeedindicator.presentation.screen.main.settings

data class SettingsUiState(
    val appTheme: Int = 0, // 0: System, 1: Light, 2: Dark
    val dynamicColor: Boolean = true,
    val lockScreenNotification: Boolean = true,
    val showUploadSpeed: Boolean = false,
    val hasUsagePermission: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val isAutoStartAvailable: Boolean = false
)
