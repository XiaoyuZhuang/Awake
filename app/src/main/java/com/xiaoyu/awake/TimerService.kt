package com.xiaoyu.awake

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import kotlin.concurrent.fixedRateTimer

class TimerService : Service() {
    private var remain = 0L
    private var timer: CountDownTimer? = null
    private var reminderTimer: java.util.Timer? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        startForeground(NotificationHelper.ID, NotificationHelper.notification(this, "Ready"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ADD_TIME" -> addTime()
            "STOP_REMINDER" -> stopReminder()
        }
        return START_STICKY
    }

    private fun addTime() {
        stopReminder()
        remain += AppSettings.addMinutes(this) * 60_000
        timer?.cancel()
        timer = object : CountDownTimer(remain, 1000) {
            override fun onTick(ms: Long) {
                remain = ms
                NotificationManagerCompat.from(this@TimerService)
                    .notify(NotificationHelper.ID, NotificationHelper.notification(this@TimerService, format(ms)))
            }

            override fun onFinish() {
                remain = 0
                startReminderLoop()
            }
        }.start()
    }

    private fun startReminderLoop() {
        reminderTimer?.cancel()
        reminderTimer = fixedRateTimer(
            name = "awake-reminder",
            initialDelay = 0,
            period = AppSettings.reminderSeconds(this) * 1000L
        ) {
            ReminderHelper.notifyReminder(this@TimerService)
        }
    }

    private fun stopReminder() {
        reminderTimer?.cancel()
        reminderTimer = null
    }

    private fun format(ms: Long): String {
        val sec = ms / 1000
        return String.format("%02d:%02d", sec / 60, sec % 60)
    }

    override fun onDestroy() {
        timer?.cancel()
        stopReminder()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
