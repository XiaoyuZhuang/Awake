package com.xiaoyu.awake

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat

/** Pinned shortcut: no window, no splash screen, and never opens settings. */
class QuickLaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTimeAndFinish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        addTimeAndFinish()
    }

    @Suppress("DEPRECATION")
    private fun addTimeAndFinish() {
        ContextCompat.startForegroundService(applicationContext,
            Intent(this, TimerService::class.java).apply { action = "ADD_TIME" })
        finish()
        overridePendingTransition(0, 0)
    }
}
