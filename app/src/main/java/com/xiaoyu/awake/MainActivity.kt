package com.xiaoyu.awake

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AppSettings.startupMode(this) == AppSettings.MODE_ADD_TIME) {
            startService(Intent(this, TimerService::class.java).apply {
                action = "ADD_TIME"
            })
        }

        finish()
    }
}
