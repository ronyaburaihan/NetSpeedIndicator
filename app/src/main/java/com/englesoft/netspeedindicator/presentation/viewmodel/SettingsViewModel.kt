package com.englesoft.netspeedindicator.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import com.englesoft.netspeedindicator.util.PermissionUtils
import androidx.lifecycle.viewModelScope
import com.englesoft.netspeedindicator.data.preferences.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


/**
 * ViewModel for Settings screen
 * Manages app settings and permissions
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val preferenceManager: PreferenceManager
) : AndroidViewModel(application) {
    
    val appTheme = preferenceManager.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dynamicColor = preferenceManager.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val lockScreenNotification = preferenceManager.lockScreenNotification
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _isBatteryOptimizationDisabled = MutableStateFlow(false)
    val isBatteryOptimizationDisabled: StateFlow<Boolean> = _isBatteryOptimizationDisabled.asStateFlow()
    
    init {
        checkPermissions()
    }
    
    fun checkPermissions() {
        _hasUsagePermission.value = PermissionUtils.hasUsageStatsPermission(application)
        _isBatteryOptimizationDisabled.value = checkBatteryOptimization()
    }

    private fun checkBatteryOptimization(): Boolean {
        val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(application.packageName)
    }
    
    fun setAppTheme(theme: Int) {
        preferenceManager.setAppTheme(theme)
    }

    fun setDynamicColor(enabled: Boolean) {
        preferenceManager.setDynamicColor(enabled)
    }

    fun setLockScreenNotification(enabled: Boolean) {
        preferenceManager.setLockScreenNotification(enabled)
    }

    fun requestUsagePermission() {
        PermissionUtils.openUsageAccessSettings(application)
    }

    fun requestDisableBatteryOptimization() {
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:${application.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            application.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic settings if direct request fails
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                 flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            application.startActivity(intent)
        }
    }
}
