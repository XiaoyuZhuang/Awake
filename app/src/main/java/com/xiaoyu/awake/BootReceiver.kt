package com.xiaoyu.awake

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val remain = context.getSharedPreferences("awake_runtime", 0)
            .getLong("remain", 0L)

        if (remain > 0L) {
            context.startForegroundService(
                Intent(context, TimerService::class.java)
            )
        }
    }
}
