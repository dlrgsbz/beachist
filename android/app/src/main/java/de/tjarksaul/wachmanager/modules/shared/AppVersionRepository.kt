package de.tjarksaul.wachmanager.modules.shared

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

class AppVersionRepository(context: Context) {
    private val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    fun getAppVersionName(): String = packageInfo.versionName

    fun getAppVersionCode(): Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getNewAppVersionCode() - 300000000
    } else {
        getLegacyAppVersionCode() - 300000000
    }

    @Suppress("DEPRECATION")
    private fun getLegacyAppVersionCode(): Long = packageInfo.versionCode.toLong()

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getNewAppVersionCode(): Long = packageInfo.longVersionCode
}