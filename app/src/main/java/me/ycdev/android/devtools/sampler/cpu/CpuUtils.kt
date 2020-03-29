package me.ycdev.android.devtools.sampler.cpu

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.Locale
import me.ycdev.android.lib.common.utils.IoUtils.closeQuietly
import me.ycdev.android.lib.common.utils.StringUtils.parseLong
import timber.log.Timber

object CpuUtils {
    private const val TAG = "CpuUtils"

    // Please "man proc" for the detail info of the proc files.
    private const val FILE_SYS_CPU_STAT = "/proc/stat"
    private const val FILE_PROC_CPU_STAT = "/proc/%d/stat"

    @kotlin.jvm.JvmStatic
    val cpuStat: SysCpuStat?
        get() {
            var br: BufferedReader? = null
            try {
                br = BufferedReader(FileReader(FILE_SYS_CPU_STAT))
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    val fields = line!!.split("\\s+").toTypedArray()
                    if (fields.size >= 5 && fields[0] == "cpu") {
                        val stat = SysCpuStat()
                        stat.utime = parseLong(fields[1], 0)
                        stat.ntime = parseLong(fields[2], 0)
                        stat.stime = parseLong(fields[3], 0)
//                        stat.itime = StringUtils.parseLong(fields[4], 0);
                        return stat
                    }
                }
            } catch (e: IOException) {
                Timber.tag(TAG).w(e, "failed to read sys cpu stat")
            } finally {
                closeQuietly(br)
            }
            return null
        }

    /**
     * Get CPU stat of specified process.
     * @param pid Process ID
     * @return null may be returned if some unexpected things happens
     */
    private fun getProcCpuStat(pid: Int): ProcCpuStat? {
        val file = String.format(Locale.US, FILE_PROC_CPU_STAT, pid)
        var br: BufferedReader? = null
        try {
            br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                val fields = line!!.split("\\s+").toTypedArray()
                if (fields.size >= 15) {
                    val stat = ProcCpuStat(pid)
                    stat.utime = parseLong(fields[13], 0)
                    stat.stime = parseLong(fields[14], 0)
                    return stat
                }
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w(e, "failed to read proc cpu stat: %s", pid)
        } finally {
            closeQuietly(br)
        }
        return null
    }

    /**
     * Get CPU stats of specified processes.
     * @param pids
     * @return The array element may be null if some unexpected things happens
     */
    fun getProcCpuStats(pids: IntArray): Array<ProcCpuStat?> {
        val procCpuStats = arrayOfNulls<ProcCpuStat>(pids.size)
        for (i in pids.indices) {
            procCpuStats[i] = getProcCpuStat(pids[i])
        }
        return procCpuStats
    }
}
