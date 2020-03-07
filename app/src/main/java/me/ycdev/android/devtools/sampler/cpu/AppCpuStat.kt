package me.ycdev.android.devtools.sampler.cpu

import android.util.SparseArray

class AppCpuStat(var pkgName: String) {
    var procSetStats = SparseArray<ProcCpuStat?>()

    val total: Long
        get() {
            var total: Long = 0
            val size = procSetStats.size()
            for (i in 0 until size) {
                val pidStat = procSetStats.valueAt(i)
                total += pidStat?.total ?: 0
            }
            return total
        }

    companion object {
        fun computeUsage(oldStat: AppCpuStat, newStat: AppCpuStat): AppCpuStat {
            if (oldStat.pkgName != newStat.pkgName) {
                throw RuntimeException("Not same pkgName")
            }
            val usage = AppCpuStat(newStat.pkgName)
            val size = newStat.procSetStats.size()
            for (i in 0 until size) {
                val pid = newStat.procSetStats.keyAt(i)
                val newPidStat = newStat.procSetStats.valueAt(i)
                val oldPidStat = oldStat.procSetStats[pid]
                if (oldPidStat != null) {
                    val pidUsage: ProcCpuStat? =
                        ProcCpuStat.computeUsage(oldPidStat, newPidStat)
                    if (pidUsage != null) {
                        usage.procSetStats.put(pidUsage.pid, pidUsage)
                    }
                }
            }
            return usage
        }
    }
}