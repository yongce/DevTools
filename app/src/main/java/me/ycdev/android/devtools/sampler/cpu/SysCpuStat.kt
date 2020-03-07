package me.ycdev.android.devtools.sampler.cpu

import timber.log.Timber

/**
 * We have to ignore "idle time" for CPU time stats. For devices with multiple CPU cores,
 * the idle time may become shorter. The reason is that some CPU cores may be enabled
 * and disabled dynamically.
 */
data class SysCpuStat(
    var utime: Long = 0, // user mode
    var ntime: Long = 0, // user mode with low priority (nice)
    var stime: Long = 0 // kernel mode
) {
    val timeUsed: Long get() = utime + ntime + stime

    fun sum(previousSnapshot: SysCpuStat, curSnapshot: SysCpuStat) {
        if (!checkStatSnapshot(previousSnapshot, curSnapshot)) {
            Timber.tag(TAG)
                .w("bad CPU stats, previous: %s, current: %s", previousSnapshot, curSnapshot)
            return
        }
        utime += curSnapshot.utime - previousSnapshot.utime
        ntime += curSnapshot.ntime - previousSnapshot.ntime
        stime += curSnapshot.stime - previousSnapshot.stime
    }

    companion object {
        private const val TAG = "SysCpuStat"

        fun checkStatSnapshot(
            previousSnapshot: SysCpuStat?,
            curSnapshot: SysCpuStat
        ): Boolean {
            return if (previousSnapshot == null) {
                curSnapshot.utime >= 0 && curSnapshot.ntime >= 0 && curSnapshot.stime >= 0
            } else {
                curSnapshot.utime >= previousSnapshot.utime &&
                        curSnapshot.ntime >= previousSnapshot.ntime &&
                        curSnapshot.stime >= previousSnapshot.stime
            }
        }
    }
}