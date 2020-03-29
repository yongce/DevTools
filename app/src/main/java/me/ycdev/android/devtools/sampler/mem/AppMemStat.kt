package me.ycdev.android.devtools.sampler.mem

import android.util.SparseArray

class AppMemStat(var pkgName: String) {
    var procSetStats = SparseArray<ProcMemStat?>()

    val totalPss: Int
        get() {
            var totalPss = 0
            for (i in 0 until procSetStats.size()) {
                val pidStat = procSetStats.valueAt(i)
                totalPss += pidStat!!.memPss
            }
            return totalPss
        }

    val totalPrivate: Int
        get() {
            var totalRss = 0
            for (i in 0 until procSetStats.size()) {
                val pidStat = procSetStats.valueAt(i)
                totalRss += pidStat!!.memPrivate
            }
            return totalRss
        }
}
