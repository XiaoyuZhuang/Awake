package com.xiaoyu.awake

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val CHANNEL_ID = "awake_timer"
    const val ID = 1001

    fun createChannel(context: Context) {
        ReminderHelper.createChannel(context)
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Clean up the old heads-up channel when upgrading.
            manager.cancel(1003)
            manager.deleteNotificationChannel("awake_added_time")
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Awake Timer", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    fun notification(context: Context, remaining: String) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Awake")
            .setContentText(remaining)
            .addAction(0, if (AppSettings.language(context) == "en") "Add time" else "增加时间", serviceIntent(context, "ADD_TIME", 1))
            .addAction(0, if (AppSettings.language(context) == "en") "Stop" else "停止", serviceIntent(context, "STOP_ALL", 2))
            .setContentIntent(settingsIntent(context))
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .build()

    fun timeAdded(context: Context, remaining: String, minutes: Long) {
        val english = AppSettings.language(context) == "en"
        val message = if (english) "Time +1 (+$minutes min) · Remaining $remaining" else
            "时间加 1（+$minutes 分钟）· 当前倒计时 $remaining"
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    internal fun serviceIntent(context: Context, action: String, code: Int): PendingIntent =
        PendingIntent.getService(context, code,
            Intent(context, TimerService::class.java).apply { this.action = action },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    fun settingsIntent(context: Context): PendingIntent {
        val intent = Intent(context, SettingsActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
