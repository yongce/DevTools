package me.ycdev.android.devtools.sampler

class StatFileLine {
    var sysTimeStamp: String? = null
    var timeUsage: Long = 0
    var cpuTime: Long = 0
    var processCount = 0
    var memPss = 0
    var memPrivate = 0
    var trafficRecv: Long = 0
    var trafficSend: Long = 0
}
