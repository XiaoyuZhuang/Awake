package com.xiaoyu.awake

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun apply(context: Context): Context {
        val lang = AppSettings.language(context)
        val locale = Locale(if (lang == "en") "en" else "zh")
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
