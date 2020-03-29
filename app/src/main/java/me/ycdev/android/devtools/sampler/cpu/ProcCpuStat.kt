package me.ycdev.android.devtools.sampler.cpu

data class ProcCpuStat(var pid: Int) {
    var utime: Long = 0 // user mode
    var stime: Long = 0 // kernel mode

    val total: Long get() = utime + stime

    companion object {
        fun computeUsage(oldStat: ProcCpuStat, newStat: ProcCpuStat?): ProcCpuStat? {
            if (oldStat.pid != newStat!!.pid) {
                return null
            }
            val usage = ProcCpuStat(newStat.pid)
            usage.utime = newStat.utime - oldStat.utime
            usage.stime = newStat.stime - oldStat.stime
            return usage
        }
    }
}
