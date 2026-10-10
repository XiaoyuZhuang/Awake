package com.xiaoyu.awake

import android.content.Context
import android.content.Intent
import android.net.Uri

object UpdateChecker {
    private const val RELEASE_URL = "https://github.com/XiaoyuZhuang/Awake/releases"

    fun openReleasePage(context: Context){
        val uri = Uri.parse(RELEASE_URL)
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun check(context: Context) {
        // Release page based update flow.
        // User downloads the newest APK from GitHub Releases.
        openReleasePage(context)
    }
}
