package com.xiaoyu.awake

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat

object ReminderHelper {
    private const val ID = 1002

    fun notifyReminder(context: Context) {
        if (!AppSettings.notification(context)) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) return

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Awake")
            .setContentText("Time is up")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (AppSettings.sound(context)) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        context.getSystemService(NotificationManager::class.java)
            .notify(ID, builder.build())

        if (AppSettings.vibration(context)) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            vibrator.vibrate(300)
        }
    }
}
