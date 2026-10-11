package com.xiaoyu.awake

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object ReminderHelper {
    const val CHANNEL_ID = "awake_expiry_reminders_v1"
    private const val ID = 1002
    private val handler = Handler(Looper.getMainLooper())
    private var ringtone: Ringtone? = null

    fun createChannel(context: Context) {
        val english = AppSettings.language(context) == "en"
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, if (english) "Time-up reminders" else "到时提醒（横幅）",
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = if (english) "Repeating banners after the countdown ends" else
                    "倒计时结束后按设定间隔弹出横幅；加时不弹横幅"
                // Sound and vibration are handled once below, using the app's switches.
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(false)
            })
    }

    fun notifyReminder(context: Context) {
        val english = AppSettings.language(context) == "en"
        if (AppSettings.notification(context) &&
            (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED)) {
            createChannel(context)
            val manager = context.getSystemService(NotificationManager::class.java)
            // Repost a single transient notification: each interval can alert again
            // without accumulating entries in the shade.
            manager.cancel(ID)
            manager.notify(ID,
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle(if (english) "Awake · Time is up" else "Awake · 倒计时结束")
                    .setContentText(if (english) "Time is up — tap to open settings" else "倒计时结束，点击进入设置")
                    .setContentIntent(NotificationHelper.settingsIntent(context))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(false)
                    .setWhen(System.currentTimeMillis())
                    .setTimeoutAfter(4000)
                    .addAction(0, if (english) "Add time" else "增加时间",
                        NotificationHelper.serviceIntent(context, "ADD_TIME", 1))
                    .addAction(0, if (english) "Stop" else "停止提醒",
                        NotificationHelper.serviceIntent(context, "STOP_ALL", 2))
                    .setAutoCancel(true)
                    .build())
        }
        if (AppSettings.sound(context)) {
            ringtone?.stop()
            ringtone = RingtoneManager.getRingtone(context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            ringtone?.play()
            val playing = ringtone
            handler.postDelayed({ playing?.stop() }, 1500)
        }
        VibrationFeedback.reminder(context)
    }

    fun stop(context: Context) {
        handler.removeCallbacksAndMessages(null)
        ringtone?.stop()
        ringtone = null
        VibrationFeedback.cancel(context)
        context.getSystemService(NotificationManager::class.java).cancel(ID)
    }
}
