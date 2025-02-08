package com.duuolf.launcher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

object LauncherUtils {

    // 判断是否为默认 Launcher
    fun isDefaultLauncher(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    // 引导用户设置默认 Launcher
    fun requestSetDefaultLauncher(context: Context) {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        context.startActivity(intent)
    }
}