package com.xiaoyu.awake

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat

class TimerService : Service() {
    private var remain = 0L
    private var timer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        startForeground(NotificationHelper.ID, NotificationHelper.notification(this, "Ready"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ADD_TIME") addTime()
        return START_STICKY
    }

    private fun addTime() {
        remain += AppSettings.addMinutes(this) * 60_000
        timer?.cancel()
        timer = object : CountDownTimer(remain, 1000) {
            override fun onTick(ms: Long) {
                remain = ms
                NotificationManagerCompat.from(this@TimerService)
                    .notify(NotificationHelper.ID, NotificationHelper.notification(this@TimerService, format(ms)))
            }

            override fun onFinish() {
                startReminderLoop()
            }
        }.start()
    }

    private fun startReminderLoop() {
        // next stage: configurable repeated reminder sound/vibration
    }

    private fun format(ms: Long): String {
        val sec = ms / 1000
        return String.format("%02d:%02d", sec / 60, sec % 60)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
