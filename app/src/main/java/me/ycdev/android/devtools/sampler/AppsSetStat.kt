package me.ycdev.android.devtools.sampler

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import kotlin.collections.set
import me.ycdev.android.devtools.sampler.cpu.CpuUtils
import me.ycdev.android.devtools.sampler.mem.MemoryUtils
import me.ycdev.android.devtools.sampler.traffic.TrafficUtils

class AppsSetStat private constructor() {
    var sampleTime: Long = 0 // in System.currentTimeMillis()
    var clockTime: Long = 0
    var targetApps = HashSet<String>()
    var appsStat = HashMap<String, AppStat>()

    companion object {
        fun computeUsage(oldStat: AppsSetStat, newStat: AppsSetStat): AppsSetStat {
            val usage = AppsSetStat()
            usage.sampleTime = newStat.sampleTime
            usage.clockTime = newStat.clockTime - oldStat.clockTime
            usage.targetApps.addAll(newStat.targetApps)
            for (newAppStat in newStat.appsStat.values) {
                val oldAppStat = oldStat.appsStat[newAppStat.pkgName]
                if (oldAppStat != null) {
                    val appUsage: AppStat? = AppStat.computeUsage(oldAppStat, newAppStat)
                    if (appUsage != null) {
                        usage.appsStat[appUsage.pkgName] = appUsage
                    }
                }
            }
            return usage
        }

        fun createSnapshot(cxt: Context, pkgNames: ArrayList<String>?): AppsSetStat {
            val appsSetStat = AppsSetStat()
            appsSetStat.sampleTime = System.currentTimeMillis()
            appsSetStat.clockTime = SystemClock.uptimeMillis()
            if (pkgNames != null) {
                appsSetStat.targetApps.addAll(pkgNames)
            }
            val am = cxt.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = am.runningAppProcesses
            val allPidsSet = HashSet<Int>()
            for (procInfo in runningApps) {
                for (pkgName in procInfo.pkgList) {
                    if (appsSetStat.targetApps.contains(pkgName)) {
                        allPidsSet.add(procInfo.pid)
                        var appStat = appsSetStat.appsStat[pkgName]
                        if (appStat == null) {
                            appStat = AppStat(pkgName, procInfo.uid)
                            appsSetStat.appsStat[pkgName] = appStat
                        }
                        appStat.pidsSet.add(procInfo.pid)
                    }
                }
            }
            val pidsCount = allPidsSet.size
            val pids = IntArray(pidsCount)
            run {
                for ((i, pid) in allPidsSet.withIndex()) {
                    pids[i] = pid
                }
            }
            // cpu stats
            val pidsCpuStats = CpuUtils.getProcCpuStats(pids)
            for (i in 0 until pidsCount) {
                val pid = pids[i]
                val procCpu = pidsCpuStats[i] ?: continue
                for (appStat in appsSetStat.appsStat.values) {
                    if (appStat.pidsSet.contains(pid)) {
                        appStat.cpuStat.procSetStats.put(pid, procCpu)
                        break
                    }
                }
            }
            // memory stats
            val pidsMemStats = MemoryUtils.getProcessMemoryStat(cxt, pids)
            for (i in 0 until pidsCount) {
                val pid = pids[i]
                val procMem = pidsMemStats[i]
                for (appStat in appsSetStat.appsStat.values) {
                    if (appStat.pidsSet.contains(pid)) {
                        appStat.memStat.procSetStats.put(pid, procMem)
                        break
                    }
                }
            }
            // traffic stats
            for (appStat in appsSetStat.appsStat.values) {
                appStat.trafficStat = TrafficUtils.getTrafficStat(appStat.uid)
            }
            return appsSetStat
        }
    }
}
