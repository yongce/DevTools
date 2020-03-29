package me.ycdev.android.devtools.sampler.mem

import android.app.ActivityManager
import android.content.Context

object MemoryUtils {
    /**
     * Get memory stats of specified processes.
     */
    fun getProcessMemoryStat(
        cxt: Context,
        pids: IntArray
    ): Array<ProcMemStat?> {
        val am = cxt.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pidsMemInfo = am.getProcessMemoryInfo(pids)
        val pidsMemStat = arrayOfNulls<ProcMemStat>(pids.size)
        for (i in pids.indices) {
            val pid = pids[i]
            val memInfo = pidsMemInfo[i]
            pidsMemStat[i] = ProcMemStat(pid)
            pidsMemStat[i]!!.memPss = memInfo.totalPss
            pidsMemStat[i]!!.memPrivate = memInfo.totalPrivateDirty
        }
        return pidsMemStat
    }
}
