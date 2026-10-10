package com.xiaoyu.awake

import android.content.Context

object AppSettings {
    private const val PREF = "awake_settings"

    const val MODE_ADD_TIME = "ADD_TIME"
    const val MODE_NOTIFICATION_ONLY = "NOTIFICATION_ONLY"

    private fun p(c: Context)=c.getSharedPreferences(PREF,0)

    fun addMinutes(c:Context)=p(c).getLong("add_minutes",10)
    fun intervalSeconds(c:Context)=p(c).getLong("interval_seconds",10)

    fun vibration(c:Context)=p(c).getBoolean("vibration",true)
    fun sound(c:Context)=p(c).getBoolean("sound",true)
    fun notification(c:Context)=p(c).getBoolean("notification",true)

    fun settingsEntry(c: Context) = p(c).getString("settings_entry", "notification") ?: "notification"
    fun setSettingsEntry(c: Context, value: String) { p(c).edit().putString("settings_entry", value).apply() }

    fun startupMode(c:Context)=p(c).getString("launch_mode", MODE_ADD_TIME) ?: MODE_ADD_TIME
    fun language(c:Context)=p(c).getString("language","zh") ?: "zh"
    fun theme(c:Context)=p(c).getString("theme","system") ?: "system"

    fun setAddMinutes(c:Context,value:Long){ p(c).edit().putLong("add_minutes",value.coerceAtLeast(1)).apply() }
    fun setIntervalSeconds(c:Context,value:Long){ p(c).edit().putLong("interval_seconds",value.coerceAtLeast(1)).apply() }
    fun setVibration(c:Context,value:Boolean){ p(c).edit().putBoolean("vibration",value).apply() }
    fun setSound(c:Context,value:Boolean){ p(c).edit().putBoolean("sound",value).apply() }
    fun setNotification(c:Context,value:Boolean){ p(c).edit().putBoolean("notification",value).apply() }
    fun setLaunchMode(c:Context,value:String){ p(c).edit().putString("launch_mode",value).apply() }
    fun setLanguage(c:Context,value:String){ p(c).edit().putString("language",value).apply() }
    fun setTheme(c:Context,value:String){ p(c).edit().putString("theme",value).apply() }
}
