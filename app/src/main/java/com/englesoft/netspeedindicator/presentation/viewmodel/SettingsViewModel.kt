package com.englesoft.netspeedindicator.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.englesoft.netspeedindicator.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 * Manages app settings and permissions
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {
    
    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()
    
    init {
        checkPermissions()
    }
    
    fun checkPermissions() {
        _hasUsagePermission.value = PermissionUtils.hasUsageStatsPermission(application)
    }
    
    fun requestUsagePermission() {
        PermissionUtils.openUsageAccessSettings(application)
    }
}
