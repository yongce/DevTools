package me.ycdev.android.devtools.sampler.mem

class ProcMemStat(var pid: Int) {
    var memPss = 0 // KB
    var memPrivate = 0 // RSS (private dirty memory usage), KB
}
