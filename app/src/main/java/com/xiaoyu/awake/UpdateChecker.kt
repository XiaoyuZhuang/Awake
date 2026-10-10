package com.xiaoyu.awake

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object UpdateChecker {
    private var checking = false
    private const val API = "https://api.github.com/repos/XiaoyuZhuang/Awake/releases/latest"

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "Awake-Android")
            connection.setRequestProperty("Accept", "application/json")
            check(connection.responseCode == 200) { "HTTP " + connection.responseCode }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    fun check(activity: AppCompatActivity) {
        if (checking) return
        checking = true
        val english = AppSettings.language(activity) == "en"
        fun tr(zh: String, en: String) = if (english) en else zh
        Toast.makeText(activity, tr("正在检查更新…", "Checking for updates…"), Toast.LENGTH_SHORT).show()
        thread(name = "awake-update") {
            val result = runCatching {
                val release = JSONObject(get(API))
                val assets = release.getJSONArray("assets")
                var metadataUrl: String? = null
                var hasApk = false
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name") == "version.json") metadataUrl = asset.getString("browser_download_url")
                    if (asset.getString("name") == "Awake-release.apk") hasApk = true
                }
                check(hasApk) { "No APK available" }
                val latest = if (metadataUrl != null) JSONObject(get(metadataUrl)) else
                    JSONObject().put("versionCode", 1).put("versionName", "1.0.0")
                Triple(latest.getLong("versionCode"), latest.getString("versionName"), release.getString("html_url"))
            }
            activity.runOnUiThread {
                checking = false
                if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                result.onSuccess { (code, name, url) ->
                    val info = activity.packageManager.getPackageInfo(activity.packageName, 0)
                    val installed = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
                    if (code > installed) {
                        AlertDialog.Builder(activity)
                            .setTitle(tr("发现新版本", "Update available"))
                            .setMessage(tr("新版本 $name，是否打开发布页下载？", "Version $name is available. Open the release page?"))
                            .setNegativeButton(tr("暂不更新", "Later"), null)
                            .setPositiveButton(tr("打开发布页", "Open release page")) { _, _ ->
                                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }.show()
                    } else {
                        Toast.makeText(activity, tr("已经是最新版本", "Already up to date"), Toast.LENGTH_LONG).show()
                    }
                }.onFailure {
                    Toast.makeText(activity, tr("检查更新失败，请检查网络后重试", "Could not check for updates. Please try again."), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
