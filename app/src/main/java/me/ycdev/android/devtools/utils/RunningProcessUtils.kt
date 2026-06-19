package me.ycdev.android.devtools.utils

import android.app.ActivityManager

object RunningProcessUtils {
    fun validProcesses(processes: List<ActivityManager.RunningAppProcessInfo>?): List<ActivityManager.RunningAppProcessInfo> =
        processes.orEmpty().filter { packageNames(it).isNotEmpty() }

    fun packageNames(processInfo: ActivityManager.RunningAppProcessInfo): List<String> =
        processInfo.pkgList.orEmpty().filter { it.isNotEmpty() }

    fun primaryPackageName(processInfo: ActivityManager.RunningAppProcessInfo): String? = packageNames(processInfo).firstOrNull()
}
