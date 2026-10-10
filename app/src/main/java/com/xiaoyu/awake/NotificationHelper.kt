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
            .setContentIntent(settingsIntent(context))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    private fun settingsIntent(context: Context): PendingIntent {
        val intent = Intent(context, SettingsActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
