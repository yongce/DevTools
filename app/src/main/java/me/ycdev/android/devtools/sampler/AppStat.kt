package me.ycdev.android.devtools.sampler

import me.ycdev.android.devtools.sampler.cpu.AppCpuStat
import me.ycdev.android.devtools.sampler.mem.AppMemStat
import me.ycdev.android.devtools.sampler.traffic.AppTrafficStat
import java.io.FileWriter
import java.io.IOException
import java.util.HashSet

class AppStat(var pkgName: String, var uid: Int) {
    var pidsSet = HashSet<Int?>()
    lateinit var memStat: AppMemStat
    lateinit var cpuStat: AppCpuStat
    lateinit var trafficStat: AppTrafficStat

    @Throws(IOException::class)
    fun dumpStat(writer: FileWriter, timeStamp: String, timeUsage: Long) {
        val cpuTime = cpuStat.total
        val memPss = memStat.totalPss
        val memPrivate = memStat.totalPrivate
        writer.append(timeStamp).append(STAT_FILE_COLUMNS_SEP)
            .append(timeUsage.toString())
            .append(STAT_FILE_COLUMNS_SEP).append(cpuTime.toString())
            .append(STAT_FILE_COLUMNS_SEP).append(pidsSet.size.toString())
            .append(STAT_FILE_COLUMNS_SEP).append(memPss.toString())
            .append(STAT_FILE_COLUMNS_SEP).append(memPrivate.toString())
            .append(STAT_FILE_COLUMNS_SEP)
            .append(trafficStat.recvBytes.toString())
            .append(STAT_FILE_COLUMNS_SEP)
            .append(trafficStat.sendBytes.toString())
            .append("\n")
    }

    companion object {
        private const val STAT_FILE_COLUMNS_SEP = "\t"

        private const val COLUMN_NAME_TIME_STAMP = "Time Stamp(ms)"
        private const val COLUMN_NAME_CPU_TIME = "CPU Time(ms)"
        private const val COLUMN_NAME_PROCESS_COUNT = "Process Count"
        private const val COLUMN_NAME_MEM_PSS = "Mem PSS(KB)"
        private const val COLUMN_NAME_MEM_PRIVATE = "Mem Private(KB)"
        private const val COLUMN_NAME_TRAFFIC_RECV = "Traffic Recv(B)"
        private const val COLUMN_NAME_TRAFFIC_SEND = "Traffic Send(B)"

        fun computeUsage(oldStat: AppStat, newStat: AppStat): AppStat? {
            if (oldStat.pkgName != newStat.pkgName || oldStat.uid != newStat.uid) {
                return null
            }
            val usage = AppStat(newStat.pkgName, newStat.uid)
            usage.pidsSet.addAll(newStat.pidsSet)
            usage.memStat = newStat.memStat
            usage.cpuStat = AppCpuStat.computeUsage(oldStat.cpuStat, newStat.cpuStat)
            usage.trafficStat =
                AppTrafficStat.computeUsage(oldStat.trafficStat, newStat.trafficStat)
            return usage
        }

        @Throws(IOException::class)
        fun appendHeader(writer: FileWriter) {
            writer.append(
                COLUMN_NAME_TIME_STAMP + STAT_FILE_COLUMNS_SEP + COLUMN_NAME_CPU_TIME +
                        STAT_FILE_COLUMNS_SEP + COLUMN_NAME_PROCESS_COUNT +
                        STAT_FILE_COLUMNS_SEP + COLUMN_NAME_MEM_PSS +
                        STAT_FILE_COLUMNS_SEP + COLUMN_NAME_MEM_PRIVATE +
                        STAT_FILE_COLUMNS_SEP + COLUMN_NAME_TRAFFIC_RECV +
                        STAT_FILE_COLUMNS_SEP + COLUMN_NAME_TRAFFIC_SEND
            )
            writer.append("\n")
        }

        @Throws(IOException::class)
        fun readStat(statLine: String): StatFileLine? {
            if (statLine.startsWith(COLUMN_NAME_TIME_STAMP)) {
                return null
            }
            val columns = statLine.split(STAT_FILE_COLUMNS_SEP).toTypedArray()
            val entry = StatFileLine()
            entry.sysTimeStamp = columns[0]
            entry.timeUsage = columns[1].toLong()
            entry.cpuTime = columns[2].toLong()
            entry.processCount = columns[3].toInt()
            entry.memPss = columns[4].toInt()
            entry.memPrivate = columns[5].toInt()
            entry.trafficRecv = columns[6].toLong()
            entry.trafficSend = columns[7].toLong()
            return entry
        }
    }
}