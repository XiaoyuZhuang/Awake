package com.xiaoyu.awake

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon

object Shortcuts {
    fun pin(context: Context): Boolean {
        val manager = context.getSystemService(ShortcutManager::class.java)
        if (!manager.isRequestPinShortcutSupported) return false
        val english = AppSettings.language(context) == "en"
        val shortcut = ShortcutInfo.Builder(context, "awake_add_time")
            .setShortLabel(if (english) "Awake +1" else "Awake 加时")
            .setLongLabel(if (english) "Add time without opening settings" else "直接加时，不打开设置")
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(Intent(context, QuickLaunchActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            })
            .build()
        return manager.requestPinShortcut(shortcut, null)
    }
}
