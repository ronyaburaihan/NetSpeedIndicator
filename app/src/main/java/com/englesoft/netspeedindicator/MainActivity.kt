package com.englesoft.netspeedindicator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.englesoft.netspeedindicator.presentation.ui.navigation.NavGraph
import com.englesoft.netspeedindicator.ui.theme.NetSpeedIndicatorTheme
import dagger.hilt.android.AndroidEntryPoint

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.englesoft.netspeedindicator.data.preferences.PreferenceManager
import com.englesoft.netspeedindicator.presentation.service.SpeedMonitorService
import javax.inject.Inject

/**
 * Main activity with Hilt integration
 * Entry point for the app UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle permission result if needed
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start monitoring service automatically
        startService(Intent(this, SpeedMonitorService::class.java))
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            val appTheme by preferenceManager.appTheme.collectAsState(initial = 0)
            val dynamicColor by preferenceManager.dynamicColor.collectAsState(initial = true)

            val darkTheme = when (appTheme) {
                1 -> false // Light
                2 -> true  // Dark
                else -> isSystemInDarkTheme() // System
            }

            NetSpeedIndicatorTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }
}