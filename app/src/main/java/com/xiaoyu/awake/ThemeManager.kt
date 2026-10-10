package com.xiaoyu.awake

import androidx.appcompat.app.AppCompatDelegate
import android.content.Context

object ThemeManager {
    fun apply(context: Context) {
        when (AppSettings.theme(context)) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
