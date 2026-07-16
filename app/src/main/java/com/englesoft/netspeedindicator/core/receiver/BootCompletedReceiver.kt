package com.englesoft.netspeedindicator.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.englesoft.netspeedindicator.core.service.SpeedMonitorService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, SpeedMonitorService::class.java)
            )
        }
    }
}