package com.englesoft.netspeedindicator.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.englesoft.netspeedindicator.MainActivity
import com.englesoft.netspeedindicator.R

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.createBitmap

/**
 * Helper class for creating and managing notifications
 * Handles notification channel creation and notification building
 */
object NotificationHelper {

    const val CHANNEL_ID = "speed_monitor_channel_v2"
    const val CHANNEL_NAME = "Speed Monitor"
    const val NOTIFICATION_ID = 1001

    /**
     * Create notification channel (required for Android O+)
     */
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT // Required to show in status bar
        ).apply {
            description = "Shows real-time internet speed"
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Build notification for speed monitoring
     * @param downloadSpeed Download speed text (e.g., "2.1 MB/s")
     * @param uploadSpeed Upload speed text (e.g., "480 KB/s")
     * @param todayUsage Today's total usage (e.g., "650 MB")
     * @param speedValue Speed value string (e.g., "1.5") - for icon
     * @param speedUnit Speed unit string (e.g., "MB") - for icon
     */
    fun buildNotification(
        context: Context,
        downloadSpeed: String,
        uploadSpeed: String,
        todayUsage: String,
        speedValue: String? = null,
        speedUnit: String? = null
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Net Speed Monitor")
            .setContentText("↓ $downloadSpeed   ↑ $uploadSpeed")
            .setSubText("Today: $todayUsage")
            .setOngoing(true) // Cannot be dismissed
            .setOnlyAlertOnce(true) // No sound/vibration on updates
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        if (speedValue != null && speedUnit != null) {
            val icon = createStatusIcon(speedValue, speedUnit)
            builder.setSmallIcon(icon)
        } else {
            builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        }

        return builder.build()
    }

    private fun createStatusIcon(
        value: String,
        unit: String
    ): IconCompat {
        val bitmap = createBitmap(96, 96)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        // Draw Value
        paint.textSize = 38f // Reduced further to ensure fit
        canvas.drawText(value, 48f, 48f, paint)

        // Draw Unit
        paint.textSize = 22f // Reduced size
        canvas.drawText(unit, 48f, 78f, paint)

        return IconCompat.createWithBitmap(bitmap)
    }
}
