package com.xiaoyu.awake

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.CountDownTimer

class TimerService : Service() {
    private var remain = 0L
    private var timer: CountDownTimer? = null

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
            }
            override fun onFinish() {
                startReminderLoop()
            }
        }.start()
    }

    private fun startReminderLoop() {
        // Reminder notification loop will be connected with NotificationManager.
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
