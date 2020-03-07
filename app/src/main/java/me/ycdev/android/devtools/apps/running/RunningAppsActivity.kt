package me.ycdev.android.devtools.apps.running

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.running.RunningAppInfo.AppNameComparator
import me.ycdev.android.devtools.apps.running.RunningAppInfo.ProcInfo
import me.ycdev.android.devtools.databinding.RunningAppsBinding
import me.ycdev.android.lib.common.utils.MiscUtils.calcProgressPercent
import me.ycdev.android.lib.commonui.base.LoadingAsyncTaskBase
import timber.log.Timber
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import kotlin.collections.set

class RunningAppsActivity : AppCompatBaseActivity() {
    private lateinit var binding: RunningAppsBinding
    private lateinit var adapter: RunningAppsAdapter
    private var appsLoader: AppsLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RunningAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = RunningAppsAdapter(this)
        binding.list.adapter = adapter
        loadApps()
    }

    private fun loadApps() {
        if (appsLoader != null && appsLoader?.status != AsyncTask.Status.FINISHED) {
            appsLoader?.cancel(true)
        }
        appsLoader = AppsLoader(this)
        appsLoader?.execute()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.running_apps_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_refresh) {
            loadApps()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (appsLoader != null && appsLoader?.status != AsyncTask.Status.FINISHED) {
            appsLoader?.cancel(true)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class AppsLoader(cxt: Activity) :
        LoadingAsyncTaskBase<Void, List<RunningAppInfo>?>(cxt) {

        override fun doInBackground(vararg params: Void): List<RunningAppInfo>? {
            val pm = activity.packageManager
            val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningProcesses = am.runningAppProcesses
            val runningApps = HashMap<String, RunningAppInfo>()
            // Get all running processes' info
            val totalProcesses = runningProcesses.size
            val pidsList = IntArray(totalProcesses)
            val procInfoList = arrayOfNulls<ProcInfo>(totalProcesses)
            for (i in 0 until totalProcesses) {
                if (isCancelled) {
                    return null // cancelled
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
                val percent = calcProgressPercent(1, 40, i + 1, totalProcesses)
                publishProgress(percent)
            }
            // Get memory usage of all the running processes
            val pidsMemInfo =
                am.getProcessMemoryInfo(pidsList)
            for (i in 0 until totalProcesses) {
                if (isCancelled) {
                    return null // cancelled
                }
                val memInfo = pidsMemInfo[i]
                procInfoList[i]!!.memPss = memInfo.totalPss
                val percent = calcProgressPercent(41, 80, i + 1, totalProcesses)
                publishProgress(percent)
            }

            // Convert the map to list
            val result: MutableList<RunningAppInfo> = ArrayList(runningApps.size)
            var i = 0
            val runningAppsCount = runningApps.size
            for (appInfo in runningApps.values) {
                if (isCancelled) {
                    return null // cancelled
                }
                result.add(appInfo)
                appInfo.totalMemPss = 0
                for (procInfo in appInfo.allProcesses) {
                    appInfo.totalMemPss += procInfo.memPss
                }
                i++
                val percent = calcProgressPercent(81, 95, i, runningAppsCount)
                publishProgress(percent)
            }
            Collections.sort(result, AppNameComparator())
            publishProgress(100)
            return result
        }

        override fun onPostExecute(result: List<RunningAppInfo>?) {
            super.onPostExecute(result)
            adapter.setData(result)
        }
    }

    companion object {
        private const val TAG = "RunningAppsActivity"
    }
}