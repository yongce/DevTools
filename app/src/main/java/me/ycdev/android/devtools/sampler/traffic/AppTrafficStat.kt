package me.ycdev.android.devtools.sampler.traffic

class AppTrafficStat(var uid: Int) {
    var recvBytes: Long = 0
    var sendBytes: Long = 0
    val total: Long
        get() = recvBytes + sendBytes

    companion object {
        fun computeUsage(oldStat: AppTrafficStat, newStat: AppTrafficStat): AppTrafficStat {
            if (oldStat.uid != newStat.uid) {
                throw RuntimeException("Not same pkgName")
            }
            val usage = AppTrafficStat(newStat.uid)
            usage.recvBytes = newStat.recvBytes - oldStat.recvBytes
            usage.sendBytes = newStat.sendBytes - oldStat.sendBytes
            return usage
        }
    }
}
