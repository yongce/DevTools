package me.ycdev.android.devtools.apps.running

import java.text.Collator
import java.util.ArrayList
import java.util.Comparator
import me.ycdev.android.lib.common.apps.AppInfo

data class RunningAppInfo(val pkgName: String) {
    data class ProcInfo(val pid: Int) {
        var procName: String? = null
        var multiplePkgNames = false
        var memPss = 0 // KB
    }

    var appInfo: AppInfo = AppInfo(pkgName)
    val allProcesses: ArrayList<ProcInfo> = ArrayList()
    var totalMemPss = 0 // KB

    class AppNameComparator : Comparator<RunningAppInfo> {
        private val collator = Collator.getInstance()

        override fun compare(lhs: RunningAppInfo, rhs: RunningAppInfo): Int {
            return collator.compare(lhs.appInfo.appName, rhs.appInfo.appName)
        }
    }
}
