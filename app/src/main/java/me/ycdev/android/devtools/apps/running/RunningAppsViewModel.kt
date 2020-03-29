package me.ycdev.android.devtools.apps.running

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ycdev.android.devtools.apps.running.RunningAppInfo.AppNameComparator
import me.ycdev.android.devtools.apps.running.RunningAppInfo.ProcInfo
import timber.log.Timber

class RunningAppsViewModel(val app: Application) : AndroidViewModel(app) {
    private var _apps: MutableLiveData<List<RunningAppInfo>> = MutableLiveData()
    val apps: LiveData<List<RunningAppInfo>> = _apps

    init {
        refreshApps()
    }

    fun refreshApps() {
        viewModelScope.launch {
            _apps.setValue(loadApps())
        }
    }
    private suspend fun loadApps(): List<RunningAppInfo> = withContext(Dispatchers.Default) {
        Timber.tag(TAG).d("loading apps...")
        val timeStart = SystemClock.elapsedRealtime()

        val pm = app.packageManager
        val am = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = am.runningAppProcesses
        val runningApps = HashMap<String, RunningAppInfo>()
        // Get all running processes' info
        val totalProcesses = runningProcesses.size
        val pidsList = IntArray(totalProcesses)
        val procInfoList = arrayOfNulls<ProcInfo>(totalProcesses)
        for (i in 0 until totalProcesses) {
            if (!isActive) {
                return@withContext emptyList<RunningAppInfo>() // cancelled
            }
            val procInfo = runningProcesses[i]
            val procItem = ProcInfo(procInfo.pid)
            procItem.procName = procInfo.processName
            procItem.multiplePkgNames = procInfo.pkgList.size > 1
            pidsList[i] = procInfo.pid
            procInfoList[i] = procItem
            val pkgName = procInfo.pkgList[0]
            var appItem = runningApps[pkgName]
            if (appItem == null) {
                appItem = RunningAppInfo(pkgName)
                try {
                    val pkgInfo = pm.getPackageInfo(pkgName, 0)
                    appItem.appInfo.appName = pkgInfo.applicationInfo.loadLabel(pm).toString()
                    appItem.appInfo.appIcon = pkgInfo.applicationInfo.loadIcon(pm)
                } catch (e: NameNotFoundException) {
                    Timber.tag(TAG).w(e, "unexpected exception")
                }
                runningApps[pkgName] = appItem
            }
            appItem.allProcesses.add(procItem)
        }
        // Get memory usage of all the running processes
        val pidsMemInfo =
            am.getProcessMemoryInfo(pidsList)
        for (i in 0 until totalProcesses) {
            if (!isActive) {
                return@withContext emptyList<RunningAppInfo>() // cancelled
            }
            val memInfo = pidsMemInfo[i]
            procInfoList[i]!!.memPss = memInfo.totalPss
        }

        // Convert the map to list
        val result: MutableList<RunningAppInfo> = ArrayList(runningApps.size)
        var i = 0
        for (appInfo in runningApps.values) {
            if (!isActive) {
                return@withContext emptyList<RunningAppInfo>() // cancelled
            }
            result.add(appInfo)
            appInfo.totalMemPss = 0
            for (procInfo in appInfo.allProcesses) {
                appInfo.totalMemPss += procInfo.memPss
            }
            i++
        }
        Collections.sort(result, AppNameComparator())

        val timeUsed = SystemClock.elapsedRealtime() - timeStart
        if (timeUsed < 500) {
            delay(500 - timeUsed)
        }
        return@withContext result
    }

    override fun onCleared() {
        Timber.tag(TAG).d("onCleared")
    }

    companion object {
        private const val TAG = "RunningAppsViewModel"
    }
}
