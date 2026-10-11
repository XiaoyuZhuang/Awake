package com.xiaoyu.awake

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Explicit attention usages allow feedback from the headless foreground service. */
object VibrationFeedback {
    @Suppress("DEPRECATION")
    private fun vibrator(context: Context): Vibrator =
        if (Build.VERSION.SDK_INT >= 31)
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        else context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun timeAdded(context: Context) = play(context, 100L, false)
    fun reminder(context: Context) = play(context, 300L, true)

    @Suppress("DEPRECATION")
    private fun play(context: Context, duration: Long, alarm: Boolean) {
        if (!AppSettings.vibration(context)) return
        val device = vibrator(context)
        if (!device.hasVibrator()) return
        val effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
        if (Build.VERSION.SDK_INT >= 33) {
            device.vibrate(effect, VibrationAttributes.Builder()
                .setUsage(if (alarm) VibrationAttributes.USAGE_ALARM else VibrationAttributes.USAGE_NOTIFICATION)
                .build())
        } else {
            device.vibrate(effect, AudioAttributes.Builder()
                .setUsage(if (alarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                .build())
        }
    }

    fun cancel(context: Context) { vibrator(context).cancel() }
}
