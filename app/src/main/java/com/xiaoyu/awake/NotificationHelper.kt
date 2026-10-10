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
    private const val ADDED_CHANNEL = "awake_added_time"
    private const val ADDED_ID = 1003

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            .setOnlyAlertOnce(true)
            .build()

    fun timeAdded(context: Context, remaining: String, minutes: Long) {
        val english = AppSettings.language(context) == "en"
        val message = if (english) "Time +1 (+$minutes min) · Remaining $remaining" else
            "时间加 1（+$minutes 分钟）· 当前倒计时 $remaining"
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(ADDED_CHANNEL, if (english) "Time added" else "增加时间提示",
                NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
                enableVibration(false)
            })
        manager.notify(ADDED_ID, NotificationCompat.Builder(context, ADDED_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(if (english) "Time +1 (+$minutes min)" else "时间加 1（+$minutes 分钟）")
            .setContentText(if (english) "Remaining $remaining" else "当前倒计时 $remaining")
            .setContentIntent(settingsIntent(context))
            .setAutoCancel(true)
            .setTimeoutAfter(4000)
            .build())
    }

    private fun serviceIntent(context: Context, action: String, code: Int): PendingIntent =
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
