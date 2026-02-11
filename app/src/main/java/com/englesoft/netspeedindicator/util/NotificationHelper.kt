package com.englesoft.netspeedindicator.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import com.englesoft.netspeedindicator.MainActivity
import com.englesoft.netspeedindicator.R

/**
 * Helper class for creating and managing notifications
 * Handles notification channel creation and notification building
 */
object NotificationHelper {

    const val CHANNEL_ID = "speed_monitor_channel_v2"
    const val CHANNEL_NAME = "Speed Monitor"
    const val NOTIFICATION_ID = 1001
    private var cachedTypeface: Typeface? = null

    private fun getStatusTypeface(context: Context): Typeface {
        return cachedTypeface ?: ResourcesCompat.getFont(
            context,
            R.font.quicksand_semi_bold
        )!!.also { cachedTypeface = it }
    }


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
     * @param speed Current speed text (e.g., "29 KB/s")
     * @param mobileUsage Mobile data usage text (e.g., "57.7 MB")
     * @param wifiUsage WiFi data usage text (e.g., "1.35 GB")
     * @param signal Signal strength text (e.g., "100%")
     * @param speedValue Speed value string (e.g., "1.5") - for icon
     * @param speedUnit Speed unit string (e.g., "MB") - for icon
     */
    fun buildNotification(
        context: Context,
        speed: String,
        mobileUsage: String,
        wifiUsage: String,
        signal: String,
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
            .setContentTitle("Speed: $speed   Signal $signal")
            .setContentText("Mobile: $mobileUsage   WiFi: $wifiUsage")
            .setOngoing(true) // Cannot be dismissed
            .setOnlyAlertOnce(true) // No sound/vibration on updates
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        if (speedValue != null && speedUnit != null) {
            val icon = createStatusIcon(context, speedValue, speedUnit)
            builder.setSmallIcon(icon)
        } else {
            builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        }

        return builder.build()
    }

    private fun createStatusIcon(context: Context, value: String, unit: String): IconCompat {
        val size = 96
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        
        // Use condensed font to match system status bar style
        val typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

        // Dynamic text size based on length
        // Balance: Value needs to be smaller to allow readable unit size
        val baseTextSize = if (value.length >= 3) 58f else 68f
        val unitTextSize = if (value.length >= 3) 36f else 40f // Increased for readability

        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = baseTextSize
            style = Paint.Style.FILL
        }

        val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = unitTextSize
            style = Paint.Style.FILL
        }

        // 1. Get exact pixel-perfect boundaries
        val vBounds = Rect()
        valuePaint.getTextBounds(value, 0, value.length, vBounds)
        val uBounds = Rect()
        unitPaint.getTextBounds(unit, 0, unit.length, uBounds)

        // 2. Calculate heights (using actual ink pixels)
        val vHeight = vBounds.height()
        val uHeight = uBounds.height()

        // 3. Set spacing - smaller gap to fit larger text
        val spacing = 4f
        val totalHeight = vHeight + uHeight + spacing

        val centerX = size / 2f
        // Start drawing from vertical center - half total height
        // No extra offset to maximize space
        val startY = (size - totalHeight) / 2f

        // 4. Draw Value
        // Align top of value to startY
        val valueBaseline = startY - vBounds.top
        canvas.drawText(value, centerX, valueBaseline, valuePaint)

        // 5. Draw Unit (Immediately below the value's bottom edge)
        val unitBaseline = valueBaseline + vBounds.bottom + spacing - uBounds.top
        canvas.drawText(unit, centerX, unitBaseline, unitPaint)

        return IconCompat.createWithBitmap(bitmap)
    }
}
