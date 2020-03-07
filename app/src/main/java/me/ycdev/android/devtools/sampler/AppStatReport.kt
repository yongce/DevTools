package me.ycdev.android.devtools.sampler

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Build.VERSION
import me.ycdev.android.lib.common.utils.DateTimeUtils.getReadableTimeUsage
import java.io.IOException
import java.io.Writer

class AppStatReport(var pkgName: String) {
    var sysTimeStampStart: String? = null
    var sysTimeStampEnd: String? = null

    var sampleCount = 0
    var totalTimeUsage: Long = 0
    var totalCpuTime: Long = 0
    var maxProcessCount = 0

    var minMemPss = 0
    var maxMemPss = 0
    var totalMemPss: Long = 0
    var minMemPrivate = 0
    var maxMemPrivate = 0

    var totalMemPrivate: Long = 0
    var totalTrafficRecv: Long = 0
    var totalTrafficSend: Long = 0

    @Throws(IOException::class)
    fun dumpStat(cxt: Context, writer: Writer) {
        var versionInfo: String? = null
        try {
            val pkgInfo = cxt.packageManager.getPackageInfo(pkgName, 0)
            versionInfo = pkgInfo.versionName + " & " + pkgInfo.versionCode
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }
        writer.append("System time: ").append(sysTimeStampStart).append(" ~ ")
            .append(sysTimeStampEnd).append("\n")
        writer.append("Package name: $pkgName").append("\n")
        writer.append("Version: $versionInfo").append("\n")
        writer.append("Phone model: " + Build.MODEL).append("\n")
        writer.append("Android OS: " + VERSION.RELEASE).append("\n")
        writer.append("Sample count: $sampleCount").append("\n")
        val timeUsageStr = getReadableTimeUsage(totalTimeUsage)
        writer.append("Time usage (ms): $totalTimeUsage").append(", str: ").append(timeUsageStr)
            .append("\n")
        writer.append("Total CPU time (ms): $totalCpuTime").append("\n")
        var averageCpu = totalCpuTime * 60 * 1000.toDouble()
        averageCpu /= totalTimeUsage.toDouble()
        writer.append("Average CPU (ms per minute): $averageCpu").append("\n")
        writer.append("Max process count: $maxProcessCount").append("\n")
        writer.append("Min Mem PSS (KB): $minMemPss").append("\n")
        writer.append("Max Mem PSS (KB): $maxMemPss").append("\n")
        writer.append("Average Mem PSS (KB): " + totalMemPss / sampleCount).append("\n")
        writer.append("Min Mem Private (KB): $minMemPrivate").append("\n")
        writer.append("Max Mem Private (KB): $maxMemPrivate").append("\n")
        writer.append("Average Mem Private (KB): " + totalMemPrivate / sampleCount).append("\n")
        writer.append("Traffic Recv (B): $totalTrafficRecv").append("\n")
        writer.append("Traffic Send (B): $totalTrafficSend").append("\n")
        var averageTraffic = totalTrafficRecv + totalTrafficSend.toDouble()
        averageTraffic = averageTraffic * 60 * 1000 / totalTimeUsage
        writer.append("Averate traffic (bytes per minute): $averageTraffic").append("\n")
        writer.append("\n")
    }
}