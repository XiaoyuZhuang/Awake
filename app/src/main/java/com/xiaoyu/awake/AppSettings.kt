package com.xiaoyu.awake

import android.content.Context

object AppSettings {
    private const val PREF = "awake_settings"
    private fun p(c: Context)=c.getSharedPreferences(PREF,0)
    fun addMinutes(c:Context)=p(c).getLong("add_minutes",10)
    fun intervalSeconds(c:Context)=p(c).getLong("interval_seconds",10)
    fun vibration(c:Context)=p(c).getBoolean("vibration",true)
    fun sound(c:Context)=p(c).getBoolean("sound",true)
    fun notification(c:Context)=p(c).getBoolean("notification",true)
}
